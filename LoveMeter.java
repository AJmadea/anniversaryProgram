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
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.Period;
import java.util.Random;

public final class LoveMeter implements Serializable {

	private static final long serialVersionUID = -1517739600205874437L;
	
	private static final String dir;
	private static final Path p;
	private long love;
	private LocalDate lastUpdated;
	private static transient DebugLog dl;
	private static transient NumberFormat f = NumberFormat.getInstance();
	
	static {
		dir = System.getProperty("user.dir");
		p = Paths.get(dir, "saves","loveMeterSave.tmp");
	}
	
	private LoveMeter(LocalDate ld, DebugLog dl) {
		LoveMeter.dl = dl;
		lastUpdated = ld;
		love = 100;
		LoveMeter.dl.logLn("created LM via constructor");
	}
	
	public static LoveMeter init(Random r, DebugLog dl) {
		LoveMeter lm = null;
		// Needs to initialize love and lastUpdated for it to work!
		if(Files.notExists(Paths.get(dir,"saves"))) {
			try {
				Files.createDirectory(Paths.get(dir,"saves"));
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		if(Files.notExists(p)) {
			// Creating the file if it does not exist
			try {
				Files.createFile(p);
			} catch(Exception e) {
				e.printStackTrace();
			}
			lm = new LoveMeter(LocalDate.now(), dl);
			LoveMeter.dl.logLn("LM Save file DNE...creating save file" + p.toAbsolutePath());
			System.out.println("Returning: " + lm);
			return lm;
		} else {
			// File exists
			try (var ois = new ObjectInputStream(
					new BufferedInputStream(
							new FileInputStream(p.toFile())))) {
				
				Object o = ois.readObject();
				if(o instanceof LoveMeter) { // Order matters here: increase love then update the date
					lm = (LoveMeter)o;
					LoveMeter.dl = dl;
					LoveMeter.dl.logLn("Sucesfuly parsed LM from the save file");
					LoveMeter.dl.logLn(lm.toString());
					
					lm.increaseLove(r);
					lm.updateLastTimeUpdated();
					return lm;
				}
			} catch(ClassNotFoundException e) {
				System.out.println("ClassNotFound while trying to parse from file");
				dl.log(e.getMessage());
			} catch(EOFException e) {
				System.out.println("Reached EOF of the File: " + p.getFileName());
				dl.logLn(e.getMessage());
				return new LoveMeter(LocalDate.now(), dl);
			} catch(IOException e) {
				System.err.println("Something happened while trying to parse the LoveMeter");
				dl.logLn(e.getMessage());
			} 
		}
		return lm;
	}
	
	private void updateLastTimeUpdated() {
		lastUpdated = LocalDate.now();
	}

	public static void exit(LoveMeter lm) {
		if(lm == null)
			throw new IllegalArgumentException("I should'nt save a null object to the save file");

		try (var oos = new ObjectOutputStream(
				new BufferedOutputStream(
						new FileOutputStream(
								p.toFile())))) {
			
			oos.writeObject(lm);
			dl.logLn("Wrote LM to save file");
		} catch (IOException e) {
			dl.log(e.getMessage());
		}
	}
	
	public void increaseLove(Random r) {
		dl.log("Attempting to increase field...");
		if(LocalDate.now().isAfter(lastUpdated)) {
			int inc = r.nextInt(1000)+1;
			Period p = Period.between(lastUpdated, LocalDate.now());
			int m = p.getYears()*365 + p.getMonths()*12 + p.getDays();
			love += m * inc;
			dl.log("successful");
		} else {
			dl.log("Not enough time has passed");
		}
		dl.log("\n");
	}

	public String toString() { return "LM: " + lastUpdated + " " +love;}
	public long getLove() {return love;}
	public String getLoveMessage() {return "I have " + f.format(love) + " love for You!"; }
}