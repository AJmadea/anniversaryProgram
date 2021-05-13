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
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import source.DebugLog;


public class PhraseDB {

	static String dir = System.getProperty("user.dir");
	
	public void update_with_new_sayings(DebugLog dl) {
		List<String> credentials = read_credentials(dl);
		Iterator<String> it = credentials.iterator();
		String url = it.next();
		String username = it.next();
		String pass = it.next();
		
		try {
			// Get connection to db
			Connection c = DriverManager.getConnection(url, username, pass);
			
			// create statement
			Statement state = c.createStatement();
			
			// execute query
			ArrayList<String> queries = create_query_for_update();
			queries.forEach(System.out::println);
			ArrayList<Integer> results = new ArrayList<>();
			
			for (String query : queries) {
				results.add(state.executeUpdate(query));
			}
			
			results.forEach(System.out::println);
			
		} catch(Exception e) {
			e.printStackTrace();
		} 
	}
	
	private ArrayList<String> create_query_for_update() throws IOException {
		List<String> new_sayings = load_new_sayings();
		ArrayList<String> queries = new ArrayList<>();
		String base = "INSERT INTO PHRASES(PHRASE) VALUES(\'";
		StringBuilder sb = new StringBuilder();
		for (String str : new_sayings) {
			sb.append(base);
			sb.append(str);
			sb.append("\')");
			queries.add(sb.toString());
			sb.delete(0, sb.length());
		}
		return queries;
	}
	
	private List<String> load_new_sayings() throws IOException {
		Path p = Paths.get(dir, "db_info","new_sayings.txt");
		return Files.lines(p).collect(Collectors.toList());
	}
	
	private List<String> read_credentials(DebugLog dl) {
		// JDBC URL
		// Username
		// Password
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
	
	public List<String> get_phrases(DebugLog dl) {
		List<String> credentials = read_credentials(dl);
		Iterator<String> it = credentials.iterator();
		String url = it.next();
		String username = it.next();
		String pass = it.next();
		ArrayList<String> list = new ArrayList<>();
		Connection c = null;
		try {
			// >:^( Get connection to db
			dl.logLn("Creating connection,statment,query...");
			c = DriverManager.getConnection(url, username, pass);
			
			// create statement
			Statement state = c.createStatement();
			
			// execute query
			String query = "SELECT ID,PHRASE FROM PHRASES";
			
			ResultSet rs = state.executeQuery(query);

			// process result set
			while (rs.next()) {
				list.add(rs.getString(2));
			}
		} catch(Exception e) {
			dl.logLn("D: Something went wrong");
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
		dl.logLn("Received a list from db of size " + list.size());
		return list;
	}
}
