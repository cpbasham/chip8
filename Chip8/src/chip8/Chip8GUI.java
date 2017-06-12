package chip8;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.WindowConstants;

public class Chip8GUI implements Chip8.UI {
	public static int PIXEL_WIDTH = 8;
	public static int PIXEL_HEIGHT = 8;
	
	public int NUM_COLS, NUM_ROWS;
	
	private JFrame frame;
	private JPanel panel;
	private boolean[] pixels;
	
	public Chip8GUI(boolean[] pixels, int NUM_COLS, int NUM_ROWS) {
		this.pixels = pixels;
		this.NUM_COLS = NUM_COLS;
		this.NUM_ROWS = NUM_ROWS;
		
		this.frame = new JFrame();
		this.frame.setBackground(Color.RED);
		
		this.panel = new Chip8Panel();
		this.frame.add(panel);

		this.frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
//		this.frame.addWindowListener();
//		this.frame.addKeyListener();
		this.frame.pack();
		this.frame.setVisible(true);
	}
	
	
	// *******************************
	// *** GUI Interface functions ***
	// *******************************
	public void refresh() {
		this.frame.repaint();
	}
	// *******************************
	
	@SuppressWarnings("serial")
	public class Chip8Panel extends JPanel {
		
		private Rectangle[] sprites; // Grid of Rectangles, visual representation of pixelgrid
		
		public Chip8Panel() {
			this.sprites = new Rectangle[NUM_COLS * NUM_ROWS];
			for (int y=0; y<NUM_ROWS; y++) {
				for (int x=0; x<NUM_COLS; x++) {
					this.sprites[NUM_COLS*y + x] = new Rectangle(x * PIXEL_WIDTH, y * PIXEL_HEIGHT, PIXEL_WIDTH, PIXEL_HEIGHT);
				}
			}
			this.setPreferredSize(new Dimension(NUM_COLS * PIXEL_WIDTH, NUM_ROWS * PIXEL_HEIGHT));
		}
		
		@Override
		public void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            
            for (int i=0; i<pixels.length; i++) {
            	if (pixels[i]) {
            		g2.setColor(Color.BLACK);
        		} else {
        			g2.setColor(Color.WHITE);
    			}
            	g2.fill(sprites[i]);
            }
		}
	}
}
