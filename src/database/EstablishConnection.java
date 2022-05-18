package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** A class that establishes a connection to a database with the name given by dbName. 
Note : This class is usable for admin and users. 
The only difference for admin is that if he wishes to create a database, he should include ";create = true" as argument in connectDatabase*/
public class EstablishConnection {
	static String driver = "org.apache.derby.jdbc.EmbeddedDriver"; 	//Drive settings
	static String protocol = "jdbc:derby:";						 	//Protocol settings
	static String dbName = "database/seconddb";						//Database location settings
	static String URL = protocol+dbName;
	
	/** Loads the database driver*/
	public static void loadDriver() {
		try { 
			Class.forName(driver);									//Call driver
		}
		catch(ClassNotFoundException e) {   						//Database driver exception handling
			System.out.println("ERROR : Can't find the Driver!\n");   
        	e.printStackTrace();   
        }
        catch(Exception e) {
        	System.out.println("ERROR : Can't load charger\n");
        	e.printStackTrace();
       }
	}
	
	/** Establishes a connection to an existing database */
	public static Connection connectDatabase() {
		try {
			Connection con = DriverManager.getConnection(URL);
			return con;
		}
        catch (SQLException e) {
        	System.out.println("ERROR : Can't connect database : SQL Operation error");
           	e.printStackTrace();
           	return null; 
        }
        catch (Exception e) {
        	System.out.println("ERROR : Can't connect database\n");
        	e.printStackTrace();
        	return null;
        }	
	}
	
	/** Establishes a connection after creating a database. Only for admin*/
	public static Connection connectDatabase(String create) {
		try {
			Connection con = DriverManager.getConnection(URL+create);
			return con;
		}
        catch (SQLException e) {
        	System.out.println("ERROR : Can't connect database : SQL Operation error");
           	e.printStackTrace();
           	return null; 
        }
        catch (Exception e) {
        	System.out.println("ERROR : Can't connect database\n");
        	e.printStackTrace();
        	return null;
        }	
	}
	
	/** Closes the connection established */ 
	public static void closeConnection(Connection con) {
		try {
			con.close();
		}
		catch (SQLException e) {
			System.out.println("ERROR : Can't close connection : SQL Operation error");
           	e.printStackTrace();
		}
	}
}
