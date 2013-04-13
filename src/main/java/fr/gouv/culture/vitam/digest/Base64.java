/**
 * This file is part of Vitam Project.
 * 
 * Copyright 2012, Frederic Bregier, and individual contributors by the @author tags. See the
 * COPYRIGHT.txt in the distribution for a full listing of individual contributors.
 * 
 * All Vitam Project is free software: you can redistribute it and/or modify it under the terms of
 * the GNU General Public License as published by the Free Software Foundation, either version 3 of
 * the License, or (at your option) any later version.
 * 
 * Vitam is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even
 * the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with Vitam. If not, see
 * <http://www.gnu.org/licenses/>.
 */
/*
 * Written by Robert Harder and released to the public domain, as explained at
 * http://creativecommons.org/licenses/publicdomain
 */
package fr.gouv.culture.vitam.digest;

/**
 * Code from Netty, adapted to be a standard algorithm from byte arrays. The encoding and decoding
 * algorithm in this class has been derived from <a
 * href="http://iharder.sourceforge.net/current/java/base64/">Robert Harder's Public Domain Base64
 * Encoder/Decoder</a>.
 * 
 * @author Netty
 * @author "Frederic Bregier"
 * 
 */
public final class Base64 {

	/** Maximum line length (76) of Base64 output. */
	private static final int MAX_LINE_LENGTH = 76;

	/** The equals sign (=) as a byte. */
	private static final byte EQUALS_SIGN = (byte) '=';

	/** The new line character (\n) as a byte. */
	private static final byte NEW_LINE = (byte) '\n';

	private static final byte WHITE_SPACE_ENC = -5; // Indicates white space in encoding

	private static final byte EQUALS_SIGN_ENC = -1; // Indicates equals sign in encoding

	private static byte[] alphabet(Base64Dialect dialect) {
		if (dialect == null) {
			throw new NullPointerException("dialect");
		}
		return dialect.alphabet;
	}

	private static byte[] decodabet(Base64Dialect dialect) {
		if (dialect == null) {
			throw new NullPointerException("dialect");
		}
		return dialect.decodabet;
	}

	private static boolean breakLines(Base64Dialect dialect) {
		if (dialect == null) {
			throw new NullPointerException("dialect");
		}
		return dialect.breakLinesByDefault;
	}

	public static String encode(byte[] src) {
		return encode(src, Base64Dialect.STANDARD);
	}

	public static String encode(byte[] src, Base64Dialect dialect) {
		return encode(src, breakLines(dialect), dialect);
	}

	public static String encode(byte[] src, boolean breakLines) {
		return encode(src, breakLines, Base64Dialect.STANDARD);
	}

	public static String encode(byte[] src, boolean breakLines, Base64Dialect dialect) {
		if (src == null) {
			throw new NullPointerException("src");
		}
		return encode(src, 0, src.length, breakLines, dialect);
	}

	public static String encode(byte[] src, int off, int len) {
		return encode(src, off, len, Base64Dialect.STANDARD);
	}

	public static String encode(byte[] src, int off, int len, Base64Dialect dialect) {
		return encode(src, off, len, breakLines(dialect), dialect);
	}

	public static String encode(byte[] src, int off, int len, boolean breakLines) {
		return encode(src, off, len, breakLines, Base64Dialect.STANDARD);
	}

	public static String encode(byte[] src, int off, int len, boolean breakLines,
			Base64Dialect dialect) {
		if (src == null) {
			throw new NullPointerException("src");
		}
		if (dialect == null) {
			throw new NullPointerException("dialect");
		}
		int len43 = len * 4 / 3;
		byte[] dest = new byte[
				len43 +
						(len % 3 > 0 ? 4 : 0) + // Account for padding
						(breakLines ? len43 / MAX_LINE_LENGTH : 0)]; // New lines
		int d = 0;
		int e = 0;
		int len2 = len - 2;
		int lineLength = 0;
		for (; d < len2; d += 3, e += 4) {
			encode3to4(src, d + off, 3, dest, e, dialect);

			lineLength += 4;
			if (breakLines && lineLength == MAX_LINE_LENGTH) {
				dest[e + 4] = NEW_LINE;
				e++;
				lineLength = 0;
			} // end if: end of line
		} // end for: each piece of array
		if (d < len) {
			encode3to4(src, d + off, len - d, dest, e, dialect);
			e += 4;
		} // end if: some padding needed

		return new String(dest).substring(0, e);
	}

	private static void encode3to4(
			byte[] src, int srcOffset, int numSigBytes,
			byte[] dest, int destOffset, Base64Dialect dialect) {
		byte[] ALPHABET = alphabet(dialect);

		// 1 2 3
		// 01234567890123456789012345678901 Bit position
		// --------000000001111111122222222 Array position from threeBytes
		// --------| || || || | Six bit groups to index ALPHABET
		// >>18 >>12 >> 6 >> 0 Right shift necessary
		// 0x3f 0x3f 0x3f Additional AND

		// Create buffer with zero-padding if there are only one or two
		// significant bytes passed in the array.
		// We have to shift left 24 in order to flush out the 1's that appear
		// when Java treats a value as negative that is cast from a byte to an int.
		int inBuff =
				(numSigBytes > 0 ? src[srcOffset] << 24 >>> 8 : 0) |
						(numSigBytes > 1 ? src[srcOffset + 1] << 24 >>> 16 : 0) |
						(numSigBytes > 2 ? src[srcOffset + 2] << 24 >>> 24 : 0);
		switch (numSigBytes) {
			case 3:
				dest[destOffset] = ALPHABET[inBuff >>> 18];
				dest[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
				dest[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3f];
				dest[destOffset + 3] = ALPHABET[inBuff & 0x3f];
				break;
			case 2:
				dest[destOffset] = ALPHABET[inBuff >>> 18];
				dest[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
				dest[destOffset + 2] = ALPHABET[inBuff >>> 6 & 0x3f];
				dest[destOffset + 3] = EQUALS_SIGN;
				break;
			case 1:
				dest[destOffset] = ALPHABET[inBuff >>> 18];
				dest[destOffset + 1] = ALPHABET[inBuff >>> 12 & 0x3f];
				dest[destOffset + 2] = EQUALS_SIGN;
				dest[destOffset + 3] = EQUALS_SIGN;
				break;
		}
	}

	public static byte[] decode(String src) {
		return decode(src, Base64Dialect.STANDARD);
	}

	public static byte[] decode(String src, Base64Dialect dialect) {
		if (src == null) {
			throw new NullPointerException("src");
		}
		return decode(src, 0, src.length(), dialect);
	}

	public static byte[] decode(String src, int off, int len) {
		return decode(src, off, len, Base64Dialect.STANDARD);
	}

	public static byte[] decode(String ssrc, int off, int len, Base64Dialect dialect) {
		if (ssrc == null) {
			throw new NullPointerException("src");
		}
		if (dialect == null) {
			throw new NullPointerException("dialect");
		}
		byte[] src = ssrc.getBytes();
		byte[] DECODABET = decodabet(dialect);

		int len34 = len * 3 / 4;
		byte[] dest = new byte[len34]; // Upper limit on size of output
		int outBuffPosn = 0;

		byte[] b4 = new byte[4];
		int b4Posn = 0;
		int i;
		byte sbiCrop;
		byte sbiDecode;
		for (i = off; i < off + len; i++) {
			sbiCrop = (byte) (src[i] & 0x7f); // Only the low seven bits
			sbiDecode = DECODABET[sbiCrop];

			if (sbiDecode >= WHITE_SPACE_ENC) { // White space, Equals sign or better
				if (sbiDecode >= EQUALS_SIGN_ENC) { // Equals sign or better
					b4[b4Posn++] = sbiCrop;
					if (b4Posn > 3) { // Quartet built
						outBuffPosn += decode4to3(
								b4, 0, dest, outBuffPosn, dialect);
						b4Posn = 0;

						// If that was the equals sign, break out of 'for' loop
						if (sbiCrop == EQUALS_SIGN) {
							break;
						}
					}
				}
			} else {
				throw new IllegalArgumentException(
						"bad Base64 input character at " + i + ": " +
								(src[i] & 0xFF) + " (decimal)");
			}
		}
		if (outBuffPosn == src.length) {
			return src;
		} else {
			byte[] result = new byte[outBuffPosn];
			System.arraycopy(src, 0, result, 0, outBuffPosn);
			return result;
		}
	}

	private static int decode4to3(
			byte[] src, int srcOffset,
			byte[] dest, int destOffset, Base64Dialect dialect) {

		byte[] DECODABET = decodabet(dialect);

		if (src[srcOffset + 2] == EQUALS_SIGN) {
			// Example: Dk==
			int outBuff =
					(DECODABET[src[srcOffset]] & 0xFF) << 18 |
							(DECODABET[src[srcOffset + 1]] & 0xFF) << 12;

			dest[destOffset] = (byte) (outBuff >>> 16);
			return 1;
		} else if (src[srcOffset + 3] == EQUALS_SIGN) {
			// Example: DkL=
			int outBuff =
					(DECODABET[src[srcOffset]] & 0xFF) << 18 |
							(DECODABET[src[srcOffset + 1]] & 0xFF) << 12 |
							(DECODABET[src[srcOffset + 2]] & 0xFF) << 6;

			dest[destOffset] = (byte) (outBuff >>> 16);
			dest[destOffset + 1] = (byte) (outBuff >>> 8);
			return 2;
		} else {
			// Example: DkLE
			int outBuff;
			try {
				outBuff =
						(DECODABET[src[srcOffset]] & 0xFF) << 18 |
								(DECODABET[src[srcOffset + 1]] & 0xFF) << 12 |
								(DECODABET[src[srcOffset + 2]] & 0xFF) << 6 |
								DECODABET[src[srcOffset + 3]] & 0xFF;
			} catch (IndexOutOfBoundsException e) {
				throw new IllegalArgumentException("not encoded in Base64");
			}

			dest[destOffset] = (byte) (outBuff >> 16);
			dest[destOffset + 1] = (byte) (outBuff >> 8);
			dest[destOffset + 2] = (byte) outBuff;
			return 3;
		}
	}

	private Base64() {
		// Unused
	}
}
