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
import java.util.HashMap;
import java.util.Map;

import dbc.PhraseDB;

public class FrequencyMap implements Serializable {
	
	static {
		dir = System.getProperty("user.dir");
	}
	
	private static final String dir;
	private static final String saveFile = "saves/frequencyMap.tmp";
	private static final long serialVersionUID = 1L;
	private Map<Integer, Integer> map;
	private transient Map<String, Integer> dbMap;
	private transient static DebugLog dl;
	private transient boolean dbFlag;
	
	private FrequencyMap(DebugLog dl) {
		map = new HashMap<>();
		FrequencyMap.dl = dl;
		initMaps();
	}
	
	private void initMaps() {
		try {
			PhraseDB pdb = new PhraseDB(dl);
			this.dbMap = pdb.getMap();
			
			if (map == null) {
				map = new HashMap<Integer, Integer>();
				if (dbMap != null) 
					dbMap.forEach((k,v) -> map.put(v, 0));
				
				dl.logLn("Initialized a new map.");
			} else {
				updateMapID();
				dl.logLn("FreqMap was loaded from save file.");
			}
			this.dbFlag = true;
		}catch (Exception e) {
			dl.logLn("Unable to initMaps() " + e.getLocalizedMessage());
			this.dbFlag = false;
		}
	}
	
	protected void updateMap(String str) {
		int id = dbMap.get(str);
		dl.logLn("map == null " + (map == null));
		
		if (map.containsKey(id)) {
			int temp = map.get(id);
			map.replace(id, temp+1);
		} else { 
			map.put(id, 1);
		}
	}
	
	private void updateMapID() {
		for(String str : dbMap.keySet()) {
			int id = dbMap.get(str);
			if(!map.containsKey(id)) {
				map.put(id, 0);
				dl.logLn("Updated map with id: "+id);
			}
		}
	}
	
	public Map<Integer, Integer> getMap() {
		return map;
	}
	
	protected Map<String, Integer> getDBMap() {
		return dbMap;
	}
	
	public boolean getFlag() {
		return dbFlag;
	}
	
	public static FrequencyMap init(DebugLog dl) {
		FrequencyMap fm = null;
		if(Files.notExists(Paths.get(dir,"saves"))) {
			try {
				Files.createDirectory(Paths.get(dir,"saves"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		Path p = Paths.get(dir,saveFile);
		if(Files.notExists(p)) {
			dl.logLn("saveFile for FreqMap DNE...creating the file");
			try {
				Files.createFile(p);
			} catch(Exception e) {
				e.printStackTrace();
			}
			
			fm = new FrequencyMap(dl);
		} else { // File exists
			try (var ois = new ObjectInputStream(
					new BufferedInputStream(
						new FileInputStream(p.toFile())))) {
						
				Object o = ois.readObject();
				if(o instanceof FrequencyMap) {
						fm = (FrequencyMap)o;
						FrequencyMap.dl = dl;
						fm.initMaps();
						
						FrequencyMap.dl.logLn("Creating FreqMap via parsing the save file");
				}
			} catch(ClassNotFoundException e) {
				System.out.println("ClassNotFound while trying to parse from file for FreqMap");
			} catch(EOFException e) {
				System.out.println("Reached EOF of the File: " + p.getFileName());
				fm = new FrequencyMap(dl);
			} catch(IOException e) {
				System.err.println("Something happened while trying to parse the FreqMap");
				e.printStackTrace();
			} 	
		}
		return fm;
	
	}
	
	protected static void exit(FrequencyMap fm) {
		if(fm == null) throw new IllegalArgumentException("FreqMap is null.  Cannot write a null object to the file.");
		
		FrequencyMap.dl.logMap(fm.getMap(), t -> t > 0);
		fm.getMap().forEach((k,v) -> System.out.println(k+"\t"+v));
		
		Path p = Paths.get(dir,saveFile);
		try (var oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(
								p.toFile())))) {
			dl.logLn("Wrote FreqMap to save file");
			oos.writeObject(fm);
			System.out.println("");

		} catch (IOException e) {
			dl.logLn(e.getMessage());
		}
	}

}
