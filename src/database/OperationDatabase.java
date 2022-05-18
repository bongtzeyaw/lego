package database;

import java.sql.PreparedStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/** 
A class that handles all operations of search and update on an existing database with the given connection 
*/
public class OperationDatabase {
	private Connection con;
	
    public OperationDatabase(Connection con) {
    	this.con = con;
    }
	
    //
    //
    /** Executes a parametered sql search query. Sql phrase is indicated by String sql and parameters by String str[].
    Note : String str[] = null if no parameters */ 
    public ResultSet search(String sql, String str[]) 
    {
    	ResultSet rs = null;
        try {
            PreparedStatement pst =con.prepareStatement(sql);
            if (str != null) {
                for (int i = 0; i < str.length; i++) {
                    pst.setString(i + 1, str[i]);
                }
            }
            rs = pst.executeQuery();
        } catch (Exception e) {
        	System.out.printf("Error while searching : %s\n", e.getMessage()); 
        }
        return rs;
    }
    
	/** Executes a parametered sql update query (create/insert/delete/...) */ 
    public int update(String sql, String str[]) {
    	//Database();
        int a = 0;
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            if (str != null) {
                for (int i = 0; i < str.length; i++) {
                    pst.setString(i + 1, str[i]);
                }
            }
            a = pst.executeUpdate();
            /*
            if (a>=1) {
            	System.out.println("\n Success ^_^ \n ");
            }else {
            	System.out.println("\n Failure (o_o) \n");
            }*/
        } catch (Exception e) {
        	System.out.printf("Error while updating : %s\n", e.getMessage());
        }
        return a;
    }
    public int update2(String sql, String str[]) {
    	//Database();
        int a = 0;
        try {
            PreparedStatement pst = con.prepareStatement(sql);
            if (str != null) {
                for (int i = 0; i < str.length; i++) {
                    pst.setString(i + 1, str[i]);
                }
            }
            a = pst.executeUpdate();
            if (a>=1) {
            	System.out.println("\n Success ^_^ \n ");
            }else {
            	System.out.println("\n Failure (o_o) \n");
            }
        } catch (Exception e) {
        	System.out.printf("Error while updating : %s\n", e.getMessage());
        }
        return a;
    }
    
    /** Prints the search result row by row */
    public void printResult(ResultSet rs) {
        try {
			ResultSetMetaData rsmd = rs.getMetaData();
			int nbCols = rsmd.getColumnCount();
			int  row = 1;
	        while(rs.next())
	        {
				System.out.printf("%d\n",row);
				for (int i = 1; i <= nbCols; i++) {
					System.out.printf("%s : ", rsmd.getColumnName(i));
					System.out.print(rs.getString(i)); 
					System.out.println();
				}
				System.out.println();
				row++;
	        }
	        rs.close();
		} catch (SQLException e) {
			System.out.printf("Error while printing : %s\n", e.getMessage()); 
		}
    }
    
}
