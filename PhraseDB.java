package dbc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import source.DebugLog;

public class PhraseDB {

	static String dir = System.getProperty("user.dir");
	private Map<String, Integer> map;
	
	public PhraseDB(DebugLog dl) {
		map = new HashMap<>();
		List<String> credentials = read_credentials(dl);
		Iterator<String> it = credentials.iterator();
		String url = it.next();
		String username = it.next();
		String pass = it.next();

		Connection c = null;
		try {
			c = DriverManager.getConnection(url, username, pass);
			Statement state = c.createStatement();
			String query = "SELECT ID,PHRASE FROM PHRASES";
			ResultSet rs = state.executeQuery(query);
			
			while (rs.next())
				this.map.put(rs.getString(2), rs.getInt(1));
			
		} catch(Exception e) {
			dl.logLn(e.getLocalizedMessage());
			e.printStackTrace();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
		dl.logLn("Creted a map from db with size " + map.size());
	}
	
	public void update_with_new_sayings(DebugLog dl) {
		List<String> credentials = read_credentials(dl);
		Iterator<String> it = credentials.iterator();
		String url = it.next();
		String username = it.next();
		String pass = it.next();
		Connection c = null;
		try {
			ArrayList<String> queries = create_query_for_update();
			if (queries == null || queries.size() == 0) {
				dl.logLn("Something was wrong with at least one of the query(ies)...");
				throw new Exception("");
			}
			
			// Get connection to db
			c = DriverManager.getConnection(url, username, pass);
			
			// create statement
			Statement state = c.createStatement();
			
			// execute query
			queries.forEach(System.out::println);
			ArrayList<Integer> results = new ArrayList<>();
			
			for (String query : queries) {
				results.add(state.executeUpdate(query));
			}
			
			results.forEach(dl::log);
			
		} catch(Exception e) {
			e.printStackTrace();
		} finally {
			if (c != null) {
				try {
					c.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private ArrayList<String> create_query_for_update() throws IOException {
		List<String> new_sayings = load_new_sayings();
		ArrayList<String> queries = new ArrayList<>();
		String base = "INSERT INTO PHRASES(PHRASE) VALUES(\'";
		StringBuilder sb = new StringBuilder();
		for (String str : new_sayings) {
			sb.append(base).append(str).append("\')");
			queries.add(sb.toString());
			sb.delete(0, sb.length());
		}
		return queries;
	}
	
	public List<String> load_new_sayings() throws IOException {
		Path p = Paths.get(dir, "db_info","new_sayings.txt");
		List<String> l = Files.lines(p)
				.filter(x -> x.length() < 42)
				.map(a -> a.replace("\'", ""))
				.collect(Collectors.toList());
		Files.writeString(p, "");
		return l;
	}
	
	private List<String> read_credentials(DebugLog dl) {
		// JDBC URL, Username, Password
		Path p = Paths.get(dir, "db_info", "db2_stuff.txt");
		List<String> l = null;
		try {
			System.out.println(p.toString());
			l  = Files.readAllLines(p);
			System.out.println(l);
			dl.logLn("Credentials for the db were read from the file!");
		} catch(Exception e) {
			dl.logLn("Credentials were not read from the file...");
			dl.logLn(e.getMessage());
		}
		return l;
	}
	
	public List<Integer> getIDs() {
		return map.values().stream().collect(Collectors.toList());
	}
	
	public Map<String, Integer> getMap() {
		return map;
	}

	public List<String> getPhrases() {
		return map.keySet().stream().collect(Collectors.toList());
	}
	
}
