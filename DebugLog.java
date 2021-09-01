package source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class DebugLog {

	private static final String dir = System.getProperty("user.dir");
	private StringBuilder sb;
	private static String folder = "logs";
	
	public DebugLog() {
		sb = new StringBuilder();
		this.logLn("Creating DebugLog @" + LocalDateTime.now());
	}
	
	protected void log(String str) {
		sb.append(str);
	}
	
	protected void logLn(String str) {
		sb.append(str).append("\n");
	}	
	
	public <K,V> void logMap(Map<K, V> map, Predicate<V> pred) {
		if (map != null) {
			for(K k : map.keySet()) {
				if (pred.test(map.get(k))) 
					this.logLn(k.toString() + "\t" + map.get(k).toString());
			}
		} else {
			this.logLn("Tried to write Map to log file, but the map object is null");
		}
	}
	
	protected void flush() throws IOException {
		Path p = Paths.get(folder);
		if(Files.notExists(p)) {
			Files.createDirectory(p);
			this.logLn("Directory logs was created");
		} else {
			// Lets see if there's 11 or more files in /logs
			long count = Files.list(p).count();
			if(count > 9) {
				TreeSet<String> set = Files.list(p)
						.map(x -> x.toString())
						.collect(Collectors.toCollection(TreeSet::new));
				Files.deleteIfExists(Paths.get(set.first()));	
			}
		}
		
		Path pp = Paths.get(dir,folder, getFileNameFromDate(LocalDateTime.now()));
		Files.createFile(pp);
		this.logLn("Writing all this info to a log...");
		this.logLn("Log Ended " + LocalDateTime.now());
		
		try {
			Files.writeString(pp, sb.toString());
		} finally {
			System.out.println(sb);
		}
	}	
	
	private String getFileNameFromDate(LocalDateTime ldt) {
		return ldt.toString().replaceAll(":", "_")+".txt";
	}
}
