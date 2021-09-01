package source;

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
	private transient Map<String, Integer> db_map;
	private HashMap<Integer, Integer> map;
	private static transient DebugLog dl;
	public transient boolean db_fail_flag = true;
	
	static {
		dir = System.getProperty("user.dir");
	}
	
	private RandomPhraseGenerator(LocalDate ld, Random rand, DebugLog dl) {
		RandomPhraseGenerator.dl = dl;
		sayings = new ArrayList<>();
		lastUpdated = ld;
		loadSayings();
		initMap();
		setSayingDateIndependent(rand);
		
		dl.logLn("Creating rpg via constructor");
	}
	
	private void setSayingDateIndependent(Random rand) {
		setSayingMapDependent(rand);
		updateMap(currentString);
	}

	/*
	 * Gets a saying from the database where the chance is inversely proportional to how many times the phrase
	 * appeared.
	 */
	private void setSayingMapDependent(Random rand) {
		RandomCollection<String> rc = new RandomCollection<>(rand);
		
		// Reverse the db_map
		Map<Integer, String> rev_db_map = db_map.entrySet().stream()
				.collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		/*
		 * Creating the weights map.  the weight is equal to 1/(V+1) / Sum
		 */
		Map<Integer, Double> weights = new HashMap<>();
		for (Integer i : map.keySet()) {
			weights.put(i,  1.0/(map.get(i)+1));
		}

		// Normalizing the weights such that the sum is 1.0
		for (Integer i : weights.keySet()) {
			double w = weights.get(i);
			weights.put(i, w);
		}
		
		dl.logLn("Weights map:");
		dl.logMap(weights, t -> true);
		
		// Adding the weights and strings to the RandomCollection object.
		for (Integer i : rev_db_map.keySet()) 
			rc.add(weights.get(i), rev_db_map.get(i));
		
		currentString = rc.next();
	}
	
	private void getRandomSayingFromList(Random rand) {
		currentString = sayings.get(rand.nextInt(sayings.size()-1));
	}
	
	private void updateMap(String str) {
		int id = db_map.get(str);
		dl.logLn("map == null " + (map == null));
		//System.out.println(map);
		
		if (map == null) initMap();
		
		if (map.containsKey(id)) {
			int temp = map.get(id);
			map.replace(id, temp+1);
		} else { 
			map.put(id, 1);
		}
		dl.logLn("Freq Map");
		dl.logMap(map, t -> t > 0);
	}
	
	private void updateMapID() {
		for(String str : db_map.keySet()) {
			int id = db_map.get(str);
			if(!map.containsKey(id)) 
				map.put(id, 0);
		}
	}
	
	private void initMap() {
		if (map == null) {
			map = new HashMap<Integer, Integer>();
			if (db_map != null) 
				db_map.forEach((k,v) -> map.put(v, 0));
			
			dl.logLn("Initialized a new map.");
			System.out.println("Init new map");
		} else {
			dl.logLn("Map was loaded from save file.");
			System.out.println("Map loaded from file.");
		}
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
						rpg.initMap();
						rpg.setSaying(r);	
						rpg.updateDate(LocalDate.now());
						RandomPhraseGenerator.dl.logLn("Creating rpg via parsing the save file");
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
		if(rpg == null) throw new IllegalArgumentException("RPG is null.  Cannot write a null object to the file.");
		
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
		dl.logLn("Attempting to connect to the DataBase...");
		
		try {
			PhraseDB pdb = new PhraseDB(dl);
			this.db_map = pdb.getMap();
			sayings = pdb.getPhrases();
			
		} catch(Exception e) {
			dl.logLn("Something went wrong when trying to connect.  Will try to parse from the normal file");
			db_fail_flag = false;
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
			if(db_fail_flag) {
				updateMapID();
				setSayingMapDependent(rand);
				updateMap(currentString);
			} else {
				getRandomSayingFromList(rand);
			}
			
			dl.logLn("Updated the phrase");
		} else {
			dl.logLn("Not enough time has passed to update the phrase");
		}
	}
}
