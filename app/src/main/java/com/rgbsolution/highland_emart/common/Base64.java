package com.rgbsolution.highland_emart.common;

/**
 * @since 2016.06.27
 * @version 1.0
 * @see
 */
public class Base64 {
	
	static byte[] ENCODE_TABLE = { 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,
			0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 0x50, 0x51, 0x52,
			0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x61, 0x62, 0x63,
			0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e,
			0x6f, 0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79,
			0x7a, 0x30, 0x31, 0x32, 0x33, 0x34, 0x35, 0x36, 0x37, 0x38, 0x39,
			0x2b, 0x2f, 0x3d };

	static byte[] DECODE_TABLE = {
			// 0 1 2 3 4 5 6 7 8 9 a b c d e f
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 0x
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 1x
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 62, 0, 0, 0, 63, // 2x
			52, 53, 54, 55, 56, 57, 58, 59, 60, 61, 0, 0, 0, 0, 0, 0, // 3x
			0, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, // 4x
			15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 0, 0, 0, 0, 0, // 5x
			0, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, // 6x
			41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 0, 0, 0, 0, 0, // 7x
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 8x
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // 9x
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // ax
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // bx
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // cx
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // dx
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, // ex
			0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 }; // fx

	private static int getDecodeBlockSize(int size) {
		return ((size + 3) / 4) * 3;
	}

	private static int decodeBlock(byte[] inputBlock, int inputOffset,
			byte[] outputBlock, int outputOffset) {

		byte d0, d1, d2, d3;
		int size = 1;
		d0 = DECODE_TABLE[inputBlock[inputOffset + 0]];
		d1 = DECODE_TABLE[inputBlock[inputOffset + 1]];
		outputBlock[outputOffset] = (byte) ((d0 << 2) | (d1 >> 4));
		if ((char) inputBlock[inputOffset + 2] != '=') {
			size++;
			d2 = DECODE_TABLE[inputBlock[inputOffset + 2]];
			outputBlock[outputOffset + 1] = (byte) ((d1 << 4) | (d2 >> 2));
			if ((char) inputBlock[inputOffset + 3] != '=') {
				size++;
				d3 = DECODE_TABLE[inputBlock[inputOffset + 3]];
				outputBlock[outputOffset + 2] = (byte) ((d2 << 6) | d3);
			}
		}
		return size;
	}

	private static int getEncodeBlockSize(int size) {
		return ((size + 2) / 3) * 4;
	}

	private static int encodeBlock(byte[] inputBlock, int inputOffset,
			byte[] outputBlock, int outputOffset) {
		outputBlock[outputOffset + 0] = ENCODE_TABLE[inputBlock[inputOffset + 0] >> 2 & 0x3f];
		if (inputOffset + 1 < inputBlock.length) {
			outputBlock[outputOffset + 1] = ENCODE_TABLE[(inputBlock[inputOffset + 0] << 4 & 0x30)
					| (inputBlock[inputOffset + 1] >> 4 & 0x0f)];
			if (inputOffset + 2 < inputBlock.length) {
				outputBlock[outputOffset + 2] = ENCODE_TABLE[(inputBlock[inputOffset + 1] << 2 & 0x3c)
						| (inputBlock[inputOffset + 2] >> 6 & 0x03)];
				outputBlock[outputOffset + 3] = ENCODE_TABLE[(inputBlock[inputOffset + 2] & 0x3f)];
			} else {
				outputBlock[outputOffset + 2] = ENCODE_TABLE[(inputBlock[inputOffset + 1] << 2 & 0x3c)];
				outputBlock[outputOffset + 3] = (byte) '=';
			}
		} else {
			outputBlock[outputOffset + 1] = ENCODE_TABLE[(inputBlock[inputOffset + 0] << 4 & 0x30)];
			outputBlock[outputOffset + 2] = outputBlock[outputOffset + 3] = (byte) '=';
		}
		return 4;
	}
	
	/**
	 * 데이터를 Base64로 인코딩.
	 * @param byte[]
	 * @return byte[]
	 */
	public static byte[] Base64Encode(byte[] source) {
		byte[] buffer = new byte[getEncodeBlockSize(source.length)];
		int inputOffset = 0;
		int outputOffset = 0;
		while (inputOffset < source.length) {
			outputOffset += encodeBlock(source, inputOffset, buffer,
					outputOffset);
			inputOffset += 3;
		}
		byte[] outputBlock = new byte[outputOffset];
		System.arraycopy(buffer, 0, outputBlock, 0, outputOffset);
		return outputBlock;
	}

	/**
	 * Base64로 인코딩된 데이터를 디코딩.
	 * @param byte[]
	 * @return byte[]
	 * @throws TransportException
	 */
	public static byte[] Base64Decode(byte[] source) {
		byte[] buffer = new byte[getDecodeBlockSize(source.length)];
		int inputOffset = 0;
		int outputOffset = 0;
		while (inputOffset < source.length) {
			outputOffset += decodeBlock(source, inputOffset, buffer, outputOffset);
			inputOffset += 4;
		}
		byte[] outputBlock = new byte[outputOffset];
		System.arraycopy(buffer, 0, outputBlock, 0, outputOffset);
		return outputBlock;
	}
}
