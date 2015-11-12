package chip8;

import java.io.IOException;

public class Run {
	public static void main(String[] args) throws IOException {
		Chip8 emulator = new Chip8(new Chip8GUI());
		
		emulator.initialize();
		emulator.load("Chip-8_Resources/pong2.c8");
		
		// loop
		while (true) {
			emulator.emulateCycle();
			
			if (emulator.drawFlag) {
				emulator.gui.repaint();
			}
			
//			emulator.setKeys();
		}
		
//		Chip8 emulator = new Chip8(new TestGUI());
	}
}
