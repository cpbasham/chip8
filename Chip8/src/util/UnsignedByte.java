package util;

public class UnsignedByte {
	private byte value;

	public UnsignedByte(int i) {
		set(i);
	}
	
	public int get() {
		return value & 0xFF;
	}
	public void set(int i) {
		value = (byte) i;
	}
}
