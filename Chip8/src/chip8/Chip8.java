package chip8;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;
import java.util.TimerTask;
import java.util.Timer;

import util.UnsignedByte;

public class Chip8 {
	public static int NUM_COLS = 64;
	public static int NUM_ROWS = 32;
	
	public UnsignedByte[] M, V, keys;
	public char opcode, I, pc, sp;
	public Stack<Character> stack;
	public boolean[] pixels;
	public UI ui;
	public boolean drawFlag = false;
	
	public static Timer timer;
	public UnsignedByte delayTimer, soundTimer;

	public interface UI {
		public void refresh();
	}
	
	public Chip8() {
		this.M = new UnsignedByte[4096];
		this.V = new UnsignedByte[16];
		this.keys = new UnsignedByte[16];
		
		this.stack = new Stack<Character>();
		
		this.pixels = new boolean[NUM_COLS * NUM_ROWS];
		this.ui = new Chip8GUI(pixels, NUM_COLS, NUM_ROWS);
	}
	
	public void initialize() {
		pc		= 0x200; // Program expected at 0x200
		opcode	= 0;
		I		= 0;
		sp		= 0;
		stack.clear();
		
		UnsignedByte UB_0 = UnsignedByte.get(0);
		// clear memory
		for (int i=0, memSize=this.M.length; i<memSize; i++) {
			this.M[i] = UB_0;
		}
		// clear registers
		for (int i=0, numRegisters=this.V.length; i<numRegisters; i++) {
			this.V[i] = UB_0;
		}
		// clear keys
		for (int i=0, numKeys=this.keys.length; i<numKeys; i++) {
			this.keys[i] = UB_0;
		}
		// clear graphics
		clearPixels();
		
		// load fontset
		loadFonts();
		
		// reset timers
		this.delayTimer = UnsignedByte.get(0);
		this.soundTimer = UnsignedByte.get(0);
		timer = new Timer();
		timer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				if (delayTimer.toInt() > 0) { delayTimer = delayTimer.subtract(1); }
				if (soundTimer.toInt() > 0) { soundTimer = soundTimer.subtract(1); }
				System.out.println(delayTimer.toInt());
			}
		}, 0, 1000/60);
	}
	
	public void load(String filename) throws IOException {
		char programIndex = 0x200;
		FileInputStream fis = new FileInputStream(filename);
		int data, i=0;
		while ((data=fis.read()) != -1) {
			M[programIndex] = UnsignedByte.get(data);
			programIndex++;
		}
	}

	public void emulateCycle() throws Exception {
		opcode = getOpcode();
		executeOpcode(opcode);
	}
	
	private char getOpcode() {
		char opcode = (char) ((M[pc].toInt() << 8) | M[pc+1].toInt());
		return opcode;
	}
	
	// opcode = char (unsigned 16-bit)
	private void executeOpcode(char opcode) throws Exception {
		
		int mSigBit = (0x8000 & opcode) >> 15,
			lSigBit	= (0x0001 & opcode),

			mSigNibble	= (0xF000 & opcode) >> 12,
			x			= (0x0F00 & opcode) >> 8,
			y			= (0x00F0 & opcode) >> 4,
			lSigNibble	= (0x000F & opcode),

			mSigByte = (0xFF00 & opcode) >> 8,
			lSigByte = (0x00FF & opcode),

			address = (0x0FFF & opcode);
				
//		System.out.println((int) opcode);
		
		switch (mSigNibble) {
		case 0x0:
			if (opcode == 0x00E0) {
				// Clear screen
				clearPixels();
				pc += 2;
			} else if (opcode == 0x00EE) {
				// Return from a subroutine
				pc = (char) (stack.pop() + 2);
			} else { // 0x0NNN
				// Call RCA 1802 program at address NNN.
				pc += 2;
				System.out.println("RCA 1802 program");
			}
			break;
		case 0x1: // 1NNN
			// Jump to NNN
			pc = (char) address;
			break;
		case 0x2: // 2NNN
			// Call subroutine at NNN
			stack.push(pc);
			pc = (char) address;
			break;
		case 0x3: // 3XNN
			// Skips the next instruction if VX equals NN.
			if (V[x].equals(lSigByte)) {
				pc += 4;
			} else {
				pc += 2;
			}
			break;
		case 0x4: // 4XNN
			// Skips the next instruction if VX doesn't equal NN.
			if (!V[x].equals(lSigByte)) {
				pc += 4;
			} else {
				pc += 2;
			}
			break;
		case 0x5: // 5XY0
			// Skips the next instruction if VX equals VY.
			if (V[x].equals(V[y])) {
				pc += 4;
			} else {
				pc += 2;
			}
			break;
		case 0x6: // 6XNN
			// Sets VX to NN.
			V[x] = UnsignedByte.get(lSigByte);
			pc += 2;
			break;
		case 0x7: // 7XNN
			// Adds NN to VX.
			V[x] =  UnsignedByte.get(V[x].toInt() + lSigByte);
			pc += 2;
			break;
		case 0x8: // 8XY~
			switch (opcode | 0x000F) {
			case 0x0:
				// Sets VX to the value of VY.
				V[x] = V[y];
				pc += 2;
				break;
			case 0x1:
				// Sets VX to VX or VY.
				V[x] = V[x].bitwiseOr(V[y]);
				pc += 2;
				break;
			case 0x2:
				// Sets VX to VX and VY.
				V[x] = V[x].bitwiseAnd(V[y]);
				pc += 2;
				break;
			case 0x3:
				// Sets VX to VX xor VY.
				V[x] = V[x].bitwiseXor(V[y]);
				pc += 2;
				break;
			case 0x4:
				// Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
				if (V[x].bitwiseAnd(V[y]).equals(0)) { // no carry
					V[15] = UnsignedByte.get(0);
				} else { 
					V[15] = UnsignedByte.get(1);
				}
				V[x] = V[x].add(V[y]);
				pc += 2;
				break;
			case 0x5: {
				// VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
				// check for borrow
				int vx = V[x].toInt(),
					vy = V[y].toInt();
				boolean borrow = false;
				while (vx != 0 || vy != 0) {
					if ((0x0001 & vx) < (0x0001 & vy)) {
						borrow = true;
						break;
					} else {
						vx >>= 1;
						vy >>= 1;
					}
				}
				if (borrow) { V[15] = UnsignedByte.get(0); }
				else		{ V[15] = UnsignedByte.get(1); }
				V[x] = V[x].subtract(V[y]);
				break;
			}
			case 0x6:
				// Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.
				V[15] = V[x].bitwiseAnd(0x0001);
				V[x] = V[x].shiftRight(1);
				pc += 2;
				break;
			case 0x7: {
				// Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
				int vx = V[x].toInt();
				int vy = V[y].toInt();
				// check for borrow
				boolean borrow = false;
				while (vy != 0 || vx != 0) {
					if ((0x0001 & vy) < (0x0001 & vx)) {
						borrow = true;
						break;
					} else {
						vx >>= 1;
						vy >>= 1;
					}
				}
				if (borrow) { V[15] = UnsignedByte.get(0); }
				else		{ V[15] = UnsignedByte.get(1); }
				V[x] = V[y].subtract(V[x]);
				break;
			}
			case 0xE:
				// Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.
				V[15] = V[x].bitwiseAnd(0x8000);
				V[x] = V[x].shiftLeft(1);
				pc += 2;
				break;
			}
		case 0x9: // 9XY0
			// Skips the next instruction if VX doesn't equal VY.
			if (!V[x].equals(V[y])) { pc += 4; }
			else 					{ pc += 2; }
			break;
		case 0xA: // ANNN
			// Sets I to the address NNN.
			I = (char) address;
			pc += 2;
			break;
		case 0xB: // BNNN
			// Jumps to the address NNN plus V0.
			pc = (char) (V[0].toInt() + address);
			break;
		case 0xC: // CXNN
			// Sets VX to the result of a bitwise and operation on a random number and NN.
			V[x] = UnsignedByte.get(lSigByte & (int) (Math.random() * 256));
			pc += 2;
			break;
		case 0xD: // DXYN
			// Sprites stored in memory at location in index register (I), 8bits wide.
			// Wraps around the screen.
			// If when drawn, clears a pixel, register VF is set to 1 otherwise it is zero.
			// All drawing is XOR drawing (i.e. it toggles the screen pixels).
			// Sprites are drawn starting at position VX, VY. N is the number of 8bit rows that need to be drawn.
			// If N is greater than 1, second line continues at position VX, VY+1, and so on.
			V[15] = UnsignedByte.get(0);
			for (int i=0; i<lSigNibble; i++) {
				int row = M[I].toInt();
				for (int j=7; j>=0; j--) {
					boolean draw = ((0x0001 << j) & row) != 0;
					if (draw) {
						if (drawPixel(V[x].toInt()+(7-j), V[y].toInt() + i)) {
							V[15] = UnsignedByte.get(1);
						}
					}
				}
			}
			ui.refresh();
			pc += 2;
			break;
		case 0xE:
			if ((opcode & 0x00FF) == 0x009E) {  // EX9E
				// Skips the next instruction if the key stored in VX is pressed.
				// TODO
				System.out.println("Skip if keypress");
				pc += 2;
			} else { // EXA1
				// Skips the next instruction if the key stored in VX isn't pressed.
				// TODO
				System.out.println("Skip if not keypress");
				pc += 2;
			}
			break;
		case 0xF:
			switch (lSigByte) {
			case 0x07:
				// Sets VX to the value of the delay timer.
				V[x] = delayTimer;
				pc += 2;
				break;
			case 0x0A:
				// A key press is awaited, and then stored in VX.
				// TODO
				System.out.println("Wait for keypress");
				pc += 2;
				break;
			case 0x15:
				// Sets the delay timer to VX.
				delayTimer = V[x];
				pc += 2;
				break;
			case 0x18:
				// Sets the sound timer to VX.
				soundTimer = V[x];
				pc += 2;
				break;
			case 0x1E:
				// Adds VX to I.
				I += V[x].toInt();
				pc += 2;
				break;
			case 0x29:
				// Sets I to the location of the sprite for the character in VX.
				// Characters 0-F (in hexadecimal) are represented by a 4x5 font.
				I = (char) (V[x].toInt() * 5);
				pc += 2;
				break;
			case 0x33:
				// Stores the Binary-coded decimal representation of VX, 
				// with the most significant of three digits at the address in I, 
				// the middle digit at I plus 1, and the least significant digit at I plus 2.
				// (In other words, take the decimal representation of VX,
				// place the hundreds digit in memory at location in I,
				// the tens digit at location I+1, and the ones digit at location I+2.)
				int dec = V[x].toInt();
				M[I] = UnsignedByte.get(dec / 100);
				dec %= 100;
				M[I+1] = UnsignedByte.get(dec / 10);
				dec %= 10;
				M[I+2] = UnsignedByte.get(dec);
				pc += 2;
				break;
			case 0x55:
				// Stores V0 to VX in memory starting at address I.
				for (int i=0; i<=x; i++) {
					M[I+i] = V[i];
				}
				pc += 2;
				break;
			case 0x65:
				// Fills V0 to VX with values from memory starting at address I.
				for (int i=0; i<=x; i++) {
					V[i] = M[I+i];
				}
				pc += 2;
				break;
			}
			break;
			
		default:
			throw new Exception("Unidentifiable opcode: " + (int) opcode);
		}
	}
	
	private void clearPixels() {
		for (int i=0, gridLength=pixels.length; i<gridLength; i++) {
			pixels[i] = false;
		}
		this.ui.refresh();
	}
	
	private boolean getPixel(int col, int row) {
		return pixels[NUM_COLS * row + (col % NUM_COLS)];
	}
	
	private boolean drawPixel(int col, int row) {
		/*** XOR draw pixel.  Return whether collision occurred ***/
		boolean pixel = getPixel(col, row);
		pixels[NUM_COLS * row + (col % NUM_COLS)] = !pixel;
		return pixel;
	}
	


	private void loadFonts() {
		int[] sprites = {
			0xF0, 0x90, 0x90, 0x90, 0xF0, 
			0x20, 0x60, 0x20, 0x20, 0x70, 
			0xF0, 0x10, 0xF0, 0x80, 0xF0, 
			0xF0, 0x10, 0xF0, 0x10, 0xF0, 
			0x90, 0x90, 0xF0, 0x10, 0x10, 
			0xF0, 0x80, 0xF0, 0x10, 0xF0, 
			0xF0, 0x80, 0xF0, 0x90, 0xF0, 
			0xF0, 0x10, 0x20, 0x40, 0x40, 
			0xF0, 0x90, 0xF0, 0x90, 0xF0, 
			0xF0, 0x90, 0xF0, 0x10, 0xF0, 
			0xF0, 0x90, 0xF0, 0x90, 0x90, 
			0xE0, 0x90, 0xE0, 0x90, 0xE0, 
			0xF0, 0x80, 0x80, 0x80, 0xF0, 
			0xE0, 0x90, 0x90, 0x90, 0xE0, 
			0xF0, 0x80, 0xF0, 0x80, 0xF0, 
			0xF0, 0x80, 0xF0, 0x80, 0x80
		};
		for (int i=0; i<sprites.length; i++) {
			M[i] = UnsignedByte.get(sprites[i]);
		}
	}
	
//	public void sillyCounter() {
//		boolean[] grid = this.gui.getPixelGrid();
//		for (int i=0; i<grid.length; i++) {
//			grid[i] = true;
//			this.gui.repaint();
//			try {
//				Thread.sleep(5);
//			} catch (InterruptedException e) {
//				e.printStackTrace();
//			}
//		}
//	}
}
