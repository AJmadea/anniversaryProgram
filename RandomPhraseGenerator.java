package cutie;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.EOFException;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import dbc.PhraseDB;

public final class RandomPhraseGenerator implements Serializable {

	private static final String dir;
	private static final String saveFile = "saves/rpgSave.tmp";
	
	private static final long serialVersionUID = -8776924841646664955L;
	private static final String sayingsPath = "cuteSayings.txt";
	private transient List<String> sayings;
	private String currentString;
	private LocalDate lastUpdated;
	private static transient DebugLog dl;
	
	static {
		dir = System.getProperty("user.dir");
	}
	
	private RandomPhraseGenerator(LocalDate ld, Random rand, DebugLog dl) {
		RandomPhraseGenerator.dl = dl;
		sayings = new ArrayList<>();
		lastUpdated = ld;
		loadSayings();
		setSayingDateIndependent(rand);
		dl.logLn("Creating rpg via constructor");
	}
	
	private void setSayingDateIndependent(Random rand) {
		currentString = sayings.get(rand.nextInt(sayings.size()-1));
	}

	protected static RandomPhraseGenerator init(Random r, DebugLog dl) {
		RandomPhraseGenerator rpg = null;
		if(Files.notExists(Paths.get(dir,"saves"))) {
			try {
				Files.createDirectory(Paths.get(dir,"saves"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		Path p = Paths.get(dir,saveFile);
		if(Files.notExists(p)) {
			dl.logLn("saveFile for RPG DNE...creating the file");
			try {
				Files.createFile(p);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			rpg = new RandomPhraseGenerator(LocalDate.now(), r, dl);
		} else { // File exists
			try (var ois = new ObjectInputStream(
					new BufferedInputStream(
						new FileInputStream(p.toFile())))) {
						
				Object o = ois.readObject();
				if(o instanceof RandomPhraseGenerator) {
						rpg = (RandomPhraseGenerator)o;
						RandomPhraseGenerator.dl = dl;
						rpg.loadSayings();
						rpg.setSaying(r);	
						rpg.updateDate(LocalDate.now());
						RandomPhraseGenerator.dl.logLn("Creating rpg via parsing the save file :)");
						RandomPhraseGenerator.dl.logLn(rpg.toString());
				}
			} catch(ClassNotFoundException e) {
				System.out.println("ClassNotFound while trying to parse from file");
			} catch(EOFException e) {
				System.out.println("Reached EOF of the File: " + p.getFileName());
				rpg = new RandomPhraseGenerator(LocalDate.now(), r,dl);
			} catch(IOException e) {
				System.err.println("Something happened while trying to parse the RPG");
				e.printStackTrace();
			} 	
		}
		return rpg;
	}
	
	protected static void exit(RandomPhraseGenerator rpg) {
		// Exit will always run after init
		if(rpg == null) throw new IllegalArgumentException("RPG is null.  I shouldn't write a null object to the file.");
		
		Path p = Paths.get(dir,saveFile);
		try (var oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(
								p.toFile())))) {
			dl.logLn("Wrote RPG to save file");
			oos.writeObject(rpg);
			System.out.println("");
		} catch (IOException e) {
			dl.logLn(e.getMessage());
		}
	}
	
	public void debug() {
		sayings.forEach(System.out::println);
		System.out.println(sayingsPath);
	}
	
	private void loadSayings() {
		PhraseDB pdb = new PhraseDB();
		dl.logLn("Attempting to connect to the DataBase...");
		
		try {
			sayings = pdb.get_phrases(dl);
		} catch(Exception e) {
			dl.logLn("Something went wrong when trying to connect.  Will try to parse from the normal file");
			Path p = Paths.get(dir,sayingsPath);
			try {
				sayings = Files.lines(p).collect(Collectors.toList());
				dl.logLn("Created sayings log from the .txt file");
			} catch (Exception e2) {
				System.err.println("Something Went Wrong Trying To Load The Cute Sayings :(");
			} 
		}
	}
	
	public String toString() {
		return "RPG: " + lastUpdated + " " + getSaying();
	}
	
	public void updateDate(LocalDate ld) {
		this.lastUpdated = ld;
	}
	
	public String getSaying() { return currentString; }
	
	private boolean shouldUpdateSaying() {
		return LocalDate.now().isAfter(lastUpdated);
	}
	
	private void setSaying(Random rand) {
		if(sayings != null && sayings.size() == 0) {
			currentString = "<3";
			return;
		}
		if(shouldUpdateSaying()) {
			currentString = sayings.get(rand.nextInt(sayings.size()));
			dl.logLn("Updated the phrase in RPG");
		} else {
			dl.logLn("Not enough time has passed to update the phrase");
		}
	}
}