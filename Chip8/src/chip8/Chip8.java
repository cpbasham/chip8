package chip8;

import java.util.Stack;

import util.UnsignedByte;

public class Chip8 {

	public UnsignedByte delayTimer, soundTimer;
	public UnsignedByte[] M, V, key;
	public char opcode, I, pc, sp;
	public Stack<Character> stack;
	public boolean[] gfx;
	
	public Chip8() {
		this.M = new UnsignedByte[4096];
		this.V = new UnsignedByte[16];
		this.key = new UnsignedByte[16];
		
		this.stack = new Stack<Character>();
		
		this.gfx = new boolean[64 * 32];
	}
	
	public static void main(String[] args) {
		UnsignedByte b = new UnsignedByte(100);
		System.out.println(b.get());
		char c = 100;
		UnsignedByte d = new UnsignedByte(c);
		System.out.println(d.get());
	}
}
