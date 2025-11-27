package com.rgbsolution.highland_emart.print;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * 빅솔론 라벨 프린터 서비스 - 블루투스 소켓 통신 방식
 * SLCS 명령어로 직접 통신
 */
public class BixolonSocketPrinter {
    private static final String TAG = "BixolonSocketPrinter";
    private static final boolean D = true;

    // SPP UUID (Serial Port Profile)
    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // Connection states
    public static final int STATE_NONE = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    // Message types
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_DEVICE_NAME = 2;
    public static final int MESSAGE_TOAST = 3;
    public static final int MESSAGE_PRINT_COMPLETE = 4;

    private Context mContext;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothSocket mSocket;
    private OutputStream mOutputStream;
    private InputStream mInputStream;
    private int mState;
    private String mConnectedDeviceName;
    private ConnectThread mConnectThread;

    public BixolonSocketPrinter(Context context, Handler handler) {
        mContext = context;
        mHandler = handler;
        mState = STATE_NONE;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private synchronized void setState(int state) {
        if (D) Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(MESSAGE_STATE_CHANGE);
            msg.arg1 = state;
            mHandler.sendMessage(msg);
        }
    }

    public synchronized int getState() {
        return mState;
    }

    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    /**
     * 블루투스 디바이스에 연결
     */
    public void connect(BluetoothDevice device) {
        if (D) Log.d(TAG, "connect to: " + device.getName());

        disconnect();

        mConnectedDeviceName = device.getName();
        mConnectThread = new ConnectThread(device);
        mConnectThread.start();
        setState(STATE_CONNECTING);
    }

    /**
     * MAC 주소로 연결
     */
    public void connect(String address) {
        if (D) Log.d(TAG, "connect to address: " + address);

        if (mBluetoothAdapter == null) {
            sendToast("블루투스를 지원하지 않는 기기입니다.");
            return;
        }

        try {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            connect(device);
        } catch (Exception e) {
            Log.e(TAG, "Invalid Bluetooth address: " + address, e);
            sendToast("잘못된 블루투스 주소입니다.");
        }
    }

    /**
     * 연결 해제
     */
    public synchronized void disconnect() {
        if (D) Log.d(TAG, "disconnect");

        if (mConnectThread != null) {
            mConnectThread.cancel();
            mConnectThread = null;
        }

        closeSocket();
        setState(STATE_NONE);
    }

    private void closeSocket() {
        try {
            if (mOutputStream != null) {
                mOutputStream.close();
                mOutputStream = null;
            }
            if (mInputStream != null) {
                mInputStream.close();
                mInputStream = null;
            }
            if (mSocket != null) {
                mSocket.close();
                mSocket = null;
            }
        } catch (IOException e) {
            Log.e(TAG, "close socket error", e);
        }
    }

    /**
     * 연결 완료 처리
     */
    private void connectionEstablished(BluetoothSocket socket) {
        mSocket = socket;
        try {
            mOutputStream = socket.getOutputStream();
            mInputStream = socket.getInputStream();
            setState(STATE_CONNECTED);

            if (mHandler != null) {
                Message m = mHandler.obtainMessage(MESSAGE_DEVICE_NAME);
                m.obj = mConnectedDeviceName;
                mHandler.sendMessage(m);
            }

            if (D) Log.d(TAG, "Connection established to " + mConnectedDeviceName);
        } catch (IOException e) {
            Log.e(TAG, "Failed to get streams", e);
            disconnect();
        }
    }

    /**
     * 연결 실패 처리
     */
    private void connectionFailed() {
        setState(STATE_NONE);
        sendToast("프린터 연결에 실패했습니다.");
    }

    /**
     * 블루투스 연결 스레드
     */
    private class ConnectThread extends Thread {
        private BluetoothSocket mmSocket;
        private final BluetoothDevice mmDevice;

        public ConnectThread(BluetoothDevice device) {
            mmDevice = device;
            BluetoothSocket tmp = null;

            try {
                tmp = device.createRfcommSocketToServiceRecord(SPP_UUID);
            } catch (IOException e) {
                Log.e(TAG, "Socket create() failed", e);
            }
            mmSocket = tmp;
        }

        public void run() {
            if (D) Log.d(TAG, "BEGIN ConnectThread");
            setName("ConnectThread");

            mBluetoothAdapter.cancelDiscovery();

            boolean connected = false;

            // 방법 1: 표준 방식
            try {
                Log.d(TAG, "방법1: 표준 연결 시도 - " + mmDevice.getName() + " [" + mmDevice.getAddress() + "]");
                mmSocket.connect();
                Log.d(TAG, "방법1: 연결 성공");
                connected = true;
            } catch (IOException e) {
                Log.e(TAG, "방법1 실패: " + e.getMessage());
                try {
                    mmSocket.close();
                } catch (IOException e2) {
                    Log.e(TAG, "Socket close error", e2);
                }
            }

            // 방법 2: Reflection 방식
            if (!connected) {
                try {
                    Log.d(TAG, "방법2: Reflection 연결 시도");
                    mmSocket = (BluetoothSocket) mmDevice.getClass()
                            .getMethod("createRfcommSocket", new Class[]{int.class})
                            .invoke(mmDevice, 1);
                    mmSocket.connect();
                    Log.d(TAG, "방법2: 연결 성공");
                    connected = true;
                } catch (Exception e) {
                    Log.e(TAG, "방법2 실패: " + e.getMessage());
                    try {
                        if (mmSocket != null) mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Socket close error", e2);
                    }
                }
            }

            // 방법 3: Insecure 방식
            if (!connected) {
                try {
                    Log.d(TAG, "방법3: Insecure 연결 시도");
                    mmSocket = mmDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);
                    mmSocket.connect();
                    Log.d(TAG, "방법3: 연결 성공");
                    connected = true;
                } catch (IOException e) {
                    Log.e(TAG, "방법3 실패: " + e.getMessage());
                    sendToast("연결 실패: 모든 방법 실패");
                    try {
                        if (mmSocket != null) mmSocket.close();
                    } catch (IOException e2) {
                        Log.e(TAG, "Socket close error", e2);
                    }
                    connectionFailed();
                    return;
                }
            }

            synchronized (BixolonSocketPrinter.this) {
                mConnectThread = null;
            }

            connectionEstablished(mmSocket);
        }

        public void cancel() {
            try {
                if (mmSocket != null) mmSocket.close();
            } catch (IOException e) {
                Log.e(TAG, "close() of connect socket failed", e);
            }
        }
    }

    /**
     * 바이트 배열 전송
     */
    private void write(byte[] data) throws IOException {
        if (mOutputStream == null) {
            throw new IOException("OutputStream is null");
        }
        mOutputStream.write(data);
        mOutputStream.flush();
    }

    /**
     * SLCS 명령어 전송
     */
    public void sendCommand(String command) {
        if (!isConnected()) {
            sendToast("프린터가 연결되지 않았습니다.");
            return;
        }

        try {
            byte[] data = command.getBytes("UTF-8");
            write(data);
            Log.d(TAG, "명령어 전송 완료");

            if (mHandler != null) {
                mHandler.sendEmptyMessage(MESSAGE_PRINT_COMPLETE);
            }
        } catch (IOException e) {
            Log.e(TAG, "sendCommand error: " + e.getMessage(), e);
            sendToast("명령어 전송 오류: " + e.getMessage());
        }
    }

    /**
     * 테스트 라벨 출력 - preview.bmp 이미지 비트맵 방식
     */
    public void printTestLabel() {
        if (!isConnected()) {
            sendToast("프린터가 연결되지 않았습니다.");
            return;
        }

        /*
        // 기존 텍스트 기반 SLCS 명령어 방식
        try {
            StringBuilder cmd = new StringBuilder();
            cmd.append("CB\r\n");
            cmd.append("CS13,0\r\n");
            cmd.append("SW600\r\n");
            cmd.append("SL480\r\n");
            cmd.append("V252,54,K,55,55,0,N,B,N,0,L,0,'장림점'\r\n");
            cmd.append("V243,149,K,94,94,0,N,B,N,0,L,0,'1790'\r\n");
            cmd.append("V395,234,K,16,16,0,N,B,N,0,L,0,'옥수수먹고자란돼지목심(미국산)_100g'\r\n");
            cmd.append("V577,335,K,29,29,0,N,B,N,0,L,0,'10.43/0525'\r\n");
            cmd.append("V239,334,K,35,35,0,N,B,N,0,L,0,'미국'\r\n");
            cmd.append("V242,384,K,31,31,0,N,B,N,0,L,0,'2022년04월26일'\r\n");
            cmd.append("V243,434,K,29,29,0,N,B,N,0,L,0,'(주)하이랜드푸드'\r\n");
            cmd.append("V242,284,K,36,36,0,N,B,N,0,L,0,'BOX'\r\n");
            cmd.append("P1\r\n");

            byte[] data = cmd.toString().getBytes("EUC-KR");
            write(data);

            Log.d(TAG, "테스트 라벨 명령어 전송 완료");

            if (mHandler != null) {
                mHandler.sendEmptyMessage(MESSAGE_PRINT_COMPLETE);
            }

        } catch (Exception e) {
            Log.e(TAG, "printTestLabel error: " + e.getMessage(), e);
            sendToast("테스트 라벨 출력 오류: " + e.getMessage());
        }
        */

        // 이미지 비트맵 방식 - preview.bmp 파일을 1비트 모노크롬 HEX 변환 후 출력
        try {
            InputStream is = mContext.getAssets().open("preview.bmp");
            Bitmap bitmap = BitmapFactory.decodeStream(is);
            is.close();

            if (bitmap == null) {
                sendToast("preview.bmp 파일을 로드할 수 없습니다.");
                return;
            }

            Log.d(TAG, "preview.bmp 로드 완료: " + bitmap.getWidth() + "x" + bitmap.getHeight());
            printBitmap(bitmap, 0, 0);
            bitmap.recycle();

        } catch (Exception e) {
            Log.e(TAG, "printTestLabel error: " + e.getMessage(), e);
            sendToast("테스트 라벨 출력 오류: " + e.getMessage());
        }
    }

    private void sendToast(String message) {
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage(MESSAGE_TOAST);
            msg.obj = message;
            mHandler.sendMessage(msg);
        }
    }

    public String getConnectedDeviceName() {
        return mConnectedDeviceName;
    }

    /**
     * Bitmap 이미지를 라벨로 출력 (EG 명령어 사용)
     * @param bitmap 출력할 비트맵 (자동으로 1비트 모노크롬 변환)
     * @param x 출력 X 좌표
     * @param y 출력 Y 좌표
     */
    public void printBitmap(Bitmap bitmap, int x, int y) {
        if (!isConnected()) {
            sendToast("프린터가 연결되지 않았습니다.");
            return;
        }

        try {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int widthBytes = (width + 7) / 8;

            Log.d(TAG, "printBitmap: " + width + "x" + height + ", widthBytes=" + widthBytes);

            StringBuilder cmd = new StringBuilder();
            cmd.append("CB\r\n");
            cmd.append("EG ").append(x).append(",").append(y).append(",")
               .append(width).append(",").append(height).append(",");

            // Bitmap -> 1bit 모노크롬 HEX 변환
            for (int row = 0; row < height; row++) {
                for (int col = 0; col < widthBytes; col++) {
                    int byteVal = 0;
                    for (int bit = 0; bit < 8; bit++) {
                        int px = col * 8 + bit;
                        if (px < width) {
                            int pixel = bitmap.getPixel(px, row);
                            int gray = (int) (0.299 * Color.red(pixel)
                                            + 0.587 * Color.green(pixel)
                                            + 0.114 * Color.blue(pixel));
                            // EG 명령어: 1=검정, 0=흰색 (gray >= 128이면 흰색이므로 비트 0)
                            if (gray >= 128) {
                                byteVal |= (1 << (7 - bit));
                            }
                        }
                    }
                    cmd.append(String.format("%02X", byteVal));
                }
            }

            cmd.append("\r\n");
            cmd.append("P1\r\n");

            byte[] data = cmd.toString().getBytes("EUC-KR");
            write(data);

            Log.d(TAG, "이미지 라벨 전송 완료, 데이터 크기: " + data.length + " bytes");

            if (mHandler != null) {
                mHandler.sendEmptyMessage(MESSAGE_PRINT_COMPLETE);
            }

        } catch (Exception e) {
            Log.e(TAG, "printBitmap error: " + e.getMessage(), e);
            sendToast("이미지 출력 오류: " + e.getMessage());
        }
    }

}
