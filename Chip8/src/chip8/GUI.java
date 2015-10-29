package chip8;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class GUI {
	public static int NUM_COLS = 64;
	public static int NUM_ROWS = 32;
	public static int PIXEL_WIDTH = 8;
	public static int PIXEL_HEIGHT = 8;
	
	private JFrame frame;
	private JPanel panel;
	private boolean[] pixelGrid;
	
	public GUI(boolean[] pixelGrid) {
		this.pixelGrid = pixelGrid;
		this.frame = new JFrame();
		this.frame.setBackground(Color.RED);
		
		this.panel = new Chip8Panel();
		this.frame.add(panel);

//		this.frame.addWindowListener();
//		this.frame.addKeyListener();
		this.frame.pack();
		this.frame.setVisible(true);
	}
	
	@SuppressWarnings("serial")
	public class Chip8Panel extends JPanel {
		
		private Rectangle[] sprites;
		
		public Chip8Panel() {
			this.sprites = new Rectangle[NUM_COLS * NUM_ROWS];
			for (int y=0; y<NUM_ROWS; y++) {
				for (int x=0; x<NUM_COLS; x++) {
					this.sprites[NUM_COLS*y + x] = new Rectangle(x * PIXEL_WIDTH,
																 y * PIXEL_HEIGHT,
																 x * PIXEL_WIDTH  + PIXEL_WIDTH  - 1,
																 y * PIXEL_HEIGHT + PIXEL_HEIGHT - 1);
				}
			}
			this.setPreferredSize(new Dimension(NUM_COLS * PIXEL_WIDTH, NUM_ROWS * PIXEL_HEIGHT));
		}
		
		@Override
		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            for (int i=0; i<pixelGrid.length; i++) {
            	if (pixelGrid[i]) {
            		g2.setColor(Color.BLACK);
        		} else {
        			g2.setColor(Color.WHITE);
    			}
            	g2.fill(sprites[i]);
            }
		}
	}
}
