package util;

import java.util.HashMap;
import java.util.Map;

public class UnsignedByte {
	private byte value;

	private UnsignedByte(int i) {
		value = (byte) i;
	}
	
	public int toInt() {
		return value & 0xFF;
	}
	
	public boolean equals(UnsignedByte other) {
		return toInt() == other.toInt();
	}
	public boolean equals(int other) {
		return toInt() == other;
	}
	public boolean equals(char other) {
		return toInt() == (int) other;
	}

	public UnsignedByte add(UnsignedByte other) {
		return get(toInt() + other.toInt());
	}
	public UnsignedByte subtract(UnsignedByte other) {
		return get(toInt() - other.toInt());
	}
	public UnsignedByte subtract(int other) {
		return get(toInt() - other);
	}
	public UnsignedByte bitwiseOr(UnsignedByte other) {
		return get(toInt() | other.toInt());
	}
	public UnsignedByte bitwiseAnd(UnsignedByte other) {
		return get(toInt() & other.toInt());
	}
	public UnsignedByte bitwiseAnd(int other) {
		return get(toInt() & other);
	}
	public UnsignedByte bitwiseXor(UnsignedByte other) {
		return get(toInt() ^ other.toInt());
	}

	public UnsignedByte shiftRight(int numShifts) {
		return get(toInt() >> numShifts);
	}
	public UnsignedByte shiftLeft(int numShifts) {
		return get(toInt() << numShifts);
	}
	
	private static Map<Integer, UnsignedByte> ubMap = new HashMap<>();
	public static UnsignedByte get(int i) {
		UnsignedByte ub = ubMap.get(i);
		if (ub == null) {
			ub = new UnsignedByte(i);
			ubMap.put(i, ub);
		}
		return ub;
	}
}
