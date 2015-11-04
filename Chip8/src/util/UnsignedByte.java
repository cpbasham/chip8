package util;

import java.util.HashMap;
import java.util.Map;

public class UnsignedByte {
	private byte value;

	private UnsignedByte(int i) {
		value = (byte) i;
	}
	
	public int value() {
		return value & 0xFF;
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
