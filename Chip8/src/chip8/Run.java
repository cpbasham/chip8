package chip8;


public class Run {
	public static void main(String[] args) throws Exception {
		Chip8 emulator = new Chip8();
		
		emulator.initialize();
		emulator.load("games/PONG2");
		
		// loop
		while (true) {
			emulator.emulateCycle();
			
			if (emulator.drawFlag) {
				emulator.ui.refresh();
			}
			
//			emulator.setKeys();
		}
	}
}
