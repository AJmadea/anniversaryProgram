package source;

import java.awt.Graphics;
import java.util.Random;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Gui extends JPanel {
	
	private RandomImage ri;
	private String[] values;

	public Gui(Random rand, DebugLog dl, String... values) {
		this.ri = new RandomImage(rand,dl);
		this.values = values;
	}
	
	@Override public void paintComponent(Graphics g) {
		int w = getWidth();
		int h = getHeight();
		
		int pixelsPerString = h/2/values.length;
		g.drawImage(ri.getImage(),0,0,w,h/2, null);
		for(int i = 0; i < values.length; i++)
			g.drawString(values[i] == null ? "" : values[i], 10, h/2 + i*pixelsPerString+20);
	}
}