package source;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

public final class Calendar {

	private static transient final LocalDate now = LocalDate.now();
	private static transient final int currentYear = now.getYear();
	private static transient final String dir = System.getProperty("user.dir");
	private static transient final DateTimeFormatter dtf = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT);
	
	private static DebugLog dl;
	
	private static Map<LocalDate, String> importantDates;
	
	static {
		importantDates = new TreeMap<>();		
	}
	
	public Calendar(DebugLog dl) {
		Calendar.dl = dl;
		dl.logLn("Parsed the dates from the path "+setMap());
	}
	
	private boolean setMap() {
		try {
			Path p = Paths.get(dir,"config");
			Path filePath = Paths.get(dir,"config","dateWithMessages.txt");
			if(Files.notExists(p)) {
				Files.createDirectories(p);
				Files.createFile(filePath);
				Files.writeString(filePath, "--- Format in this style please  DATE:SAYING");
				return false;
			}
			
			importantDates = Files.lines(filePath)
				.skip(1)
				.map(x-> x.split(":"))
				.filter(t -> t.length == 2)
				.collect(Collectors.toMap(
						a -> {
							int y = LocalDate.parse(a[0],dtf).getYear();
							return LocalDate.parse(a[0],dtf).plusYears(currentYear-y);
						},
						b -> b[1]
						));
			
			return true;
		} catch(IOException e) {
			dl.logLn(e.getMessage());
			return false;
		}
	}

	public static String getString() {
		if(importantDates.containsKey(now)) {
			return importantDates.get(now);
		} 
		return ":)";
	}

	public static void debug() {
		importantDates.forEach((a,b) -> System.out.println(a + "\t" + b));
	}
}