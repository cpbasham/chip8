package chip8;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Stack;

import util.UnsignedByte;

public class Chip8 {
	public UnsignedByte delayTimer, soundTimer;
	public UnsignedByte[] M, V, keys;
	public char opcode, I, pc, sp;
	public Stack<Character> stack;
	public GUI gui;
	public boolean drawFlag = false;

	public interface GUI {
		public boolean[] getPixelGrid();
		public void repaint();
	}
	
	public Chip8(GUI gui) {
		this.M = new UnsignedByte[4096];
		this.V = new UnsignedByte[16];
		this.keys = new UnsignedByte[16];
		
		this.stack = new Stack<Character>();
		
		this.gui = gui;
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
		boolean[] grid = this.gui.getPixelGrid();
		for (int i=0, gridLength=grid.length; i<gridLength; i++) {
			grid[i] = false;
		}
		this.gui.repaint();
		
		// TODO load fontset
		
		// TODO reset timers
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

	public void emulateCycle() {
		opcode = getOpcode();
	}
	
	private char getOpcode() {
		char opcode = (char) ((M[pc].value() << 8) | M[pc+1].value());
		pc += 2;
		return opcode;
	}
	
	// opcode = char (unsigned 16-bit)
	private void executeOpcode() throws Exception {
		
		switch ((opcode | 0xf000) >> 12) {
		case 0x0:
			if ((opcode | 0x0f00) >> 8 != 0) {
				// Call RCA 1802 program at address NNN.
			} else if ((opcode | 0x000f) == 0) {
				// Clear screen
			} else {
				// Return from a subroutine
			}
			break;
		case 0x1:
			// Jump to NNN
			break;
		case 0x2:
			// Call subroutine at NNN
			break;
		case 0x3: // 3XNN
			// Skips the next instruction if VX equals NN.
			break;
		case 0x4: // 4XNN
			// Skips the next instruction if VX doesn't equal NN.
			break;
		case 0x5: // 5XY0
			// Skips the next instruction if VX equals VY.
			break;
		case 0x6: // 6XNN
			// Sets VX to NN.
			break;
		case 0x7: // 7XNN
			// Adds NN to VX.
			break;
		case 0x8: // 8XY~
			switch (opcode | 0xf) {
			case 0x0:
				// Sets VX to the value of VY.
				break;
			case 0x1:
				// Sets VX to VX or VY.
				break;
			case 0x2:
				// Sets VX to VX and VY.
				break;
			case 0x3:
				// Sets VX to VX xor VY.
				break;
			case 0x4:
				// Adds VY to VX. VF is set to 1 when there's a carry, and to 0 when there isn't.
				break;
			case 0x5:
				// VY is subtracted from VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
				break;
			case 0x6:
				// Shifts VX right by one. VF is set to the value of the least significant bit of VX before the shift.
				break;
			case 0x7:
				// Sets VX to VY minus VX. VF is set to 0 when there's a borrow, and 1 when there isn't.
				break;
			case 0xE:
				// Shifts VX left by one. VF is set to the value of the most significant bit of VX before the shift.
				break;
			}
			break;
		case 0x9: // 9XY0
			// Skips the next instruction if VX doesn't equal VY.
			break;
		case 0xA: // ANNN
			// Sets I to the address NNN.
			break;
		case 0xB: // BNNN
			// Jumps to the address NNN plus V0.
			break;
		case 0xC: // CXNN
			// Sets VX to the result of a bitwise and operation on a random number and NN.
			break;
		case 0xD: // DXYN
			// Sprites stored in memory at location in index register (I), 8bits wide.
			// Wraps around the screen.
			// If when drawn, clears a pixel, register VF is set to 1 otherwise it is zero.
			// All drawing is XOR drawing (i.e. it toggles the screen pixels).
			// Sprites are drawn starting at position VX, VY. N is the number of 8bit rows that need to be drawn.
			// If N is greater than 1, second line continues at position VX, VY+1, and so on.
			break;
		case 0xE:
			if ((opcode & 0x00FF) == 0x009E) {  // EX9E
				// Skips the next instruction if the key stored in VX is pressed.
			} else { // EXA1
				// Skips the next instruction if the key stored in VX isn't pressed.
			}
			break;
		case 0xF:
			int x = opcode & 0x0F00;
			switch (opcode & 0x00FF) {
			case 0x07:
				// Sets VX to the value of the delay timer.
				break;
			case 0x0A:
				// A key press is awaited, and then stored in VX.
				break;
			case 0x15:
				// Sets the delay timer to VX.
				break;
			case 0x18:
				// Sets the sound timer to VX.
				break;
			case 0x1E:
				// Adds VX to I.
				break;
			case 0x29:
				// Sets I to the location of the sprite for the character in VX.
				// Characters 0-F (in hexadecimal) are represented by a 4x5 font.
				break;
			case 0x33:
				// Stores the Binary-coded decimal representation of VX, 
				// with the most significant of three digits at the address in I, 
				// the middle digit at I plus 1, and the least significant digit at I plus 2.
				// (In other words, take the decimal representation of VX,
				// place the hundreds digit in memory at location in I,
				// the tens digit at location I+1, and the ones digit at location I+2.)
				break;
			case 0x55:
				// Stores V0 to VX in memory starting at address I.
				break;
			case 0x65:
				// Fills V0 to VX with values from memory starting at address I.
				break;
			}
			break;
			
		default:
			throw new Exception("Unidentifiable opcode: " + (int) opcode);
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
