package source;

import java.io.IOException;
import java.util.Random;

import javax.swing.JFrame;

@SuppressWarnings("serial") public class Main extends JFrame {
	public static void main(String... cute) {
		Random rand = new Random();
		DebugLog dl = new DebugLog();
		
		JFrame frame = new JFrame("Cute Program");
		frame.setDefaultCloseOperation(EXIT_ON_CLOSE);
		
		LoveMeter lm = LoveMeter.init(rand,dl);
		RandomPhraseGenerator rpg = RandomPhraseGenerator.init(rand,dl);
		Calendar c = new Calendar(dl);
		
		String loveMessage = lm.getLoveMessage();
		String phrase = rpg.getSaying();
		String calendarMessage = Calendar.getString();
		
		Gui gui = new Gui(rand,dl,loveMessage,phrase,calendarMessage);
		LoveMeter.exit(lm);
		RandomPhraseGenerator.exit(rpg);
		
		frame.setSize(500,700);
		frame.add(gui);
		frame.setVisible(true);
		
		try {
			dl.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}