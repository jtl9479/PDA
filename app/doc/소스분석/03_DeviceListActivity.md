# DeviceListActivity

## 개요
블루투스 프린터를 선택하기 위한 Activity입니다. 페어링된 장치 목록을 표시하고 새로운 장치를 검색할 수 있으며, 사용자가 선택한 장치의 MAC 주소를 반환합니다.

**파일 위치**: `D:\PDA\PDA-INNO\app\src\main\java\com\rgbsolution\highland_emart\print\DeviceListActivity.java`
**전체 라인 수**: 179줄

## 클래스 구조

### 상속
```java
public class DeviceListActivity extends Activity
```

### 주요 필드

#### 상수
- `TAG` (String): 로그 태그 - "DeviceListActivity"
- `D` (boolean): 디버그 모드 플래그 - true
- `EXTRA_DEVICE_ADDRESS` (String): Intent Extra 키 - "device_address"

#### Instance 필드
- `mBtAdapter` (BluetoothAdapter): 블루투스 어댑터
- `mPairedDevicesArrayAdapter` (ArrayAdapter<String>): 페어링된 장치 목록 어댑터
- `mNewDevicesArrayAdapter` (ArrayAdapter<String>): 새로 발견된 장치 목록 어댑터
- `mReceiver` (BroadcastReceiver): 블루투스 장치 검색 결과 수신 리시버

## 주요 메서드

### 1. onCreate()
**위치**: 40-98줄

Activity 생성 시 UI 및 블루투스 초기화를 수행합니다.

**초기화 순서**:

#### 1) 윈도우 및 레이아웃 설정 (44-45줄)
```java
requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
setContentView(R.layout.device_list);
```

#### 2) 기본 결과값 설정 (48줄)
```java
setResult(Activity.RESULT_CANCELED);
```
- 사용자가 뒤로가기로 취소하는 경우 대비

#### 3) 스캔 버튼 초기화 (51-57줄)
```java
Button scanButton = (Button) findViewById(R.id.button_scan);
scanButton.setOnClickListener(new OnClickListener() {
    public void onClick(View v) {
        doDiscovery();
        v.setVisibility(View.GONE);
    }
});
```
- 클릭 시 `doDiscovery()` 호출하여 장치 검색 시작
- 버튼은 클릭 후 숨김 처리

#### 4) ArrayAdapter 초기화 (61-62줄)
```java
mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.device_name);
```

#### 5) ListView 설정 (65-72줄)
- **페어링된 장치 ListView** (65-67줄)
- **새로 발견된 장치 ListView** (70-72줄)
- 둘 다 `mDeviceClickListener` 연결

#### 6) BroadcastReceiver 등록 (75-80줄)
```java
IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
this.registerReceiver(mReceiver, filter);

filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
this.registerReceiver(mReceiver, filter);
```
- `ACTION_FOUND`: 장치 발견 시
- `ACTION_DISCOVERY_FINISHED`: 검색 완료 시

#### 7) 블루투스 어댑터 및 페어링된 장치 조회 (83-97줄)
```java
mBtAdapter = BluetoothAdapter.getDefaultAdapter();
Set<BluetoothDevice> pairedDevices = mBtAdapter.getBondedDevices();

if (pairedDevices.size() > 0) {
    findViewById(R.id.title_paired_devices).setVisibility(View.VISIBLE);
    for (BluetoothDevice device : pairedDevices) {
        mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    }
} else {
    String noDevices = getResources().getText(R.string.none_paired).toString();
    mPairedDevicesArrayAdapter.add(noDevices);
}
```
- 페어링된 장치가 있으면 목록에 추가
- 없으면 "페어링된 장치 없음" 메시지 표시

### 2. doDiscovery()
**위치**: 113-130줄

새로운 블루투스 장치를 검색합니다.

**동작 과정**:
1. 진행 표시줄 활성화 (117줄)
   ```java
   setProgressBarIndeterminateVisibility(true);
   ```
2. 타이틀을 "스캔 중..."으로 변경 (118줄)
3. 새 장치 타이틀 표시 (121줄)
4. 이미 검색 중이면 중단 (124-126줄)
   ```java
   if (mBtAdapter.isDiscovering()) {
       mBtAdapter.cancelDiscovery();
   }
   ```
5. 장치 검색 시작 (129줄)
   ```java
   mBtAdapter.startDiscovery();
   ```

### 3. mDeviceClickListener
**위치**: 133-150줄

ListView 항목 클릭 시 호출되는 리스너입니다.

**동작 과정**:
1. 장치 검색 중단 (136줄)
   ```java
   mBtAdapter.cancelDiscovery();
   ```
2. MAC 주소 추출 (139-140줄)
   ```java
   String info = ((TextView) v).getText().toString();
   String address = info.substring(info.length() - 17);
   ```
   - 텍스트의 마지막 17자리가 MAC 주소 (예: "AA:BB:CC:DD:EE:FF")

3. Intent에 MAC 주소 담기 (143-144줄)
   ```java
   Intent intent = new Intent();
   intent.putExtra(EXTRA_DEVICE_ADDRESS, address);
   ```

4. 결과 반환 및 종료 (147-148줄)
   ```java
   setResult(Activity.RESULT_OK, intent);
   finish();
   ```

### 4. mReceiver (BroadcastReceiver)
**위치**: 154-177줄

블루투스 장치 검색 결과를 수신하는 리시버입니다.

**처리하는 액션**:

#### 1) ACTION_FOUND (장치 발견) - 160-166줄
```java
if (BluetoothDevice.ACTION_FOUND.equals(action)) {
    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
    if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
        mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
    }
}
```
- 발견된 장치가 페어링되지 않은 경우에만 목록에 추가
- 이미 페어링된 장치는 onCreate()에서 표시됨

#### 2) ACTION_DISCOVERY_FINISHED (검색 완료) - 168-175줄
```java
else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
    setProgressBarIndeterminateVisibility(false);
    setTitle(R.string.select_device);
    if (mNewDevicesArrayAdapter.getCount() == 0) {
        String noDevices = getResources().getText(R.string.none_found).toString();
        mNewDevicesArrayAdapter.add(noDevices);
    }
}
```
- 진행 표시줄 숨김
- 타이틀을 "장치 선택"으로 변경
- 발견된 장치가 없으면 "장치를 찾을 수 없음" 메시지 표시

### 5. onDestroy()
**위치**: 101-111줄

Activity 종료 시 리소스 정리를 수행합니다.

**정리 작업**:
1. 장치 검색 중단 (105-107줄)
   ```java
   if (mBtAdapter != null) {
       mBtAdapter.cancelDiscovery();
   }
   ```
2. BroadcastReceiver 등록 해제 (110줄)
   ```java
   this.unregisterReceiver(mReceiver);
   ```

## 블루투스 장치 검색 흐름

```
[Activity 시작]
    ↓
[페어링된 장치 목록 표시]
    ↓
[사용자가 "스캔" 버튼 클릭]
    ↓
[doDiscovery() 호출]
    ↓
[BluetoothAdapter.startDiscovery()]
    ↓
[mReceiver: ACTION_FOUND 수신]
    ↓ (반복)
[새로운 장치 목록에 추가]
    ↓
[mReceiver: ACTION_DISCOVERY_FINISHED 수신]
    ↓
[검색 완료 표시]
    ↓
[사용자가 장치 선택]
    ↓
[mDeviceClickListener 호출]
    ↓
[MAC 주소 추출 및 반환]
    ↓
[Activity 종료]
```

## 반환값

### Intent Extra
- **키**: `EXTRA_DEVICE_ADDRESS` ("device_address")
- **값**: 선택된 블루투스 장치의 MAC 주소 (17자리 문자열)
- **예시**: "AA:BB:CC:DD:EE:FF"

### Result Code
- `Activity.RESULT_OK`: 장치 선택 성공
- `Activity.RESULT_CANCELED`: 사용자가 취소 (기본값)

## 사용 방법

### Activity 호출
```java
Intent serverIntent = new Intent(this, DeviceListActivity.class);
startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE);
```

### 결과 수신
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if (requestCode == REQUEST_CONNECT_DEVICE) {
        if (resultCode == Activity.RESULT_OK) {
            String address = data.getExtras()
                .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
            // address를 이용하여 블루투스 연결 수행
        }
    }
}
```

## UI 구조

### ListView
1. **페어링된 장치 목록** (`R.id.paired_devices`)
   - 제목: `R.id.title_paired_devices`
   - 앱 시작 시 자동으로 표시

2. **새로 발견된 장치 목록** (`R.id.new_devices`)
   - 제목: `R.id.title_new_devices`
   - 스캔 버튼 클릭 시 표시

### 버튼
- **스캔 버튼** (`R.id.button_scan`)
  - 클릭 시 장치 검색 시작
  - 클릭 후 숨김 처리

### 장치 표시 형식
```
장치이름
AA:BB:CC:DD:EE:FF
```

## 권한 요구사항

이 Activity를 사용하려면 AndroidManifest.xml에 다음 권한이 필요합니다:

```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```

Android 6.0 이상:
```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## 코드 위치 요약

| 항목 | 라인 |
|------|------|
| 클래스 선언 | 26 |
| 상수 정의 | 28-32 |
| 필드 선언 | 35-37 |
| onCreate() | 40-98 |
| onDestroy() | 101-111 |
| doDiscovery() | 113-130 |
| mDeviceClickListener | 133-150 |
| mReceiver | 154-177 |

## 주요 특징

1. **이중 목록 구조**: 페어링된 장치와 새로 발견된 장치를 별도 표시
2. **MAC 주소 파싱**: TextView에서 마지막 17자리 추출
3. **중복 방지**: 페어링된 장치는 새 장치 목록에서 제외
4. **자동 정리**: onDestroy()에서 검색 중단 및 리시버 해제
