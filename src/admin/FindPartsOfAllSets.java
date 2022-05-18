package admin;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.OperationDatabase;

/** A class that finds component parts of all sets in database.  
 * Created to speed up the actual user run time of application later on*/
public class FindPartsOfAllSets {
	/**Function that affirms if a table is empty*/
	public static boolean isEmptyTable(OperationDatabase od, String table) {
		ResultSet rs = null;
		String sql = null;
		sql = "SELECT * "
	    	+ "FROM " + table + " ";  
	    rs = od.search(sql, null);
	    try {
			if(rs.next()) {
				rs.close();
				return true;
			}
			rs.close();
			return false;
		} catch (SQLException e) {
			System.out.printf("Error while affirming table %s is empty\n", table);
			e.printStackTrace();
			return false;
		} 
	}
	
	/**Function thats adds a record in table of PartsNeedAllSets, carrying information about a composant part of a set source. 
	 * Note : Similar to addPartsToATable in SurFonction.java but here we store the extra information of set source*/
	public static void addComposantParts(OperationDatabase od, String table, String set_num_origin, String part_num, String color, String quantity_part_to_add) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		
		try {
			sql = 
					  "SELECT quantity "
					+ "FROM " + table +" "
					+ "WHERE part_num = ? "
					+ "AND color = ? "
					+ "AND set_num = ? ";
			strArr = new String[] {part_num, color, set_num_origin}; 
			rs = od.search(sql, strArr);
			
			//If part already exists in table, modify the exisiting record (Note : rs.next() returns true if result non empty)
			if (rs.next()) { 	
				String quantity_part_current = rs.getString("quantity"); 		
				sql = "UPDATE " + table +" " 
				    	+ "SET quantity = ? "
						+ "WHERE part_num = ? "
						+ "AND color = ? "
						+ "AND set_num = ? ";
				strArr = new String[]{String.valueOf(Integer.parseInt(quantity_part_current) + Integer.parseInt(quantity_part_to_add)), part_num, color, set_num_origin};
				od.update(sql, strArr);
			}
			//If part does not exist in table, create a new record. 	
			else {
				sql = "INSERT INTO " + table +" (part_num,color,quantity,set_num) values(?,?,?,?) ";
			    strArr =new String[]{part_num,color,quantity_part_to_add,set_num_origin};
			    od.update(sql, strArr);	
			}
			rs.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		} 	
	}
	
	/** Function that finds all the parts that make up a set, including parts of component sets and store them in Table PartsNeedAllSets. 
	 * Note : We store the extra information of set source for the part as well*/
	public static void findComposantParts(OperationDatabase od,String set_num_origin) {
		//Strategy : Break the set into component parts and component sets. Store the information of the component parts. 
		//Then, further break the component sets into their own component sets and component parts.	Store the information of the component parts. 
		//Repeat the process recurrently until there no more component sets and all the parts have been stored.
		ResultSet rs = null;
		String sql = null;
		String strArr[] = null;
		boolean stillHasComponentSets; 
		
		try {	
			//Clean dirty tables that might result from previous run being stopped violently. These tables should be empty.
			sql = "DELETE FROM tempcomposantsets";
			od.update(sql, null);
			
			sql = "DELETE FROM tempcomposantsetsbuffer";
			od.update(sql, null);
			
			//Initialization of table tempComposantSets
			sql = "INSERT INTO tempcomposantsets (set_num,quantity) values(?,?) ";  
			strArr = new String[]{set_num_origin, "1"}; 	//Quantity of set = 1 because we want to find component parts for 1 unity of main set
		    od.update(sql, strArr);
		    
		    //Initialization of boolean stillHasComponentSets. True if tempComposantSets non empty.  
		    stillHasComponentSets = isEmptyTable(od, "tempcomposantsets");
		    while(stillHasComponentSets) {
				//Add parts of all component sets to PartsNeedAllSets
		    	System.out.println("Inserting in both tables");
		    	sql = 
					  "SELECT INVP.part_num, INVP.color_id, TCS.quantity * INVP.quantity AS quantity, INVP.is_spare AS is_spare "
					+ "FROM tempcomposantsets AS TCS "
					+ "INNER JOIN inventories AS INV "
					+ "ON TCS.set_num = INV.set_num "
					+ "INNER JOIN inventory_parts AS INVP "
					+ "ON INV.id = INVP.inventory_id "; 
				rs = od.search(sql, null); 
				
				while (rs.next()) {
					String part_num = rs.getString("part_num"); 
					String color = rs.getString("color_id");
					String quantity_part = rs.getString("quantity");
					String is_spare = rs.getString("is_spare");
					//System.out.printf("Part %s color %s quantity %s added\n", part_num.trim(), color, quantity_part);
					if((is_spare.trim()).equals("f")) {
//						System.out.printf("Ah false : Part %s color %s quantity %s added\n", part_num.trim(), color, quantity_part);
						addComposantParts(od, "PartsHaveAllSets", set_num_origin, part_num, color, quantity_part);
						addComposantParts(od, "PartsNeedAllSets", set_num_origin, part_num, color, quantity_part);
					}
					else {
//						System.out.printf("Ah true : Part %s color %s quantity %s added\n", part_num.trim(), color, quantity_part);
						addComposantParts(od, "PartsHaveAllSets", set_num_origin, part_num, color, quantity_part);
					}
				}
				rs.close();
				System.out.println("Finished inserting in both tables");
						
				//Break the sets in tempComposantSets into further component sets and store them in the same table by using buffer	
				sql = 
					  "INSERT INTO tempcomposantsetsbuffer (set_num,quantity) "  
				    + "SELECT S2.set_num, INVS.quantity AS quantity "
					+ "FROM tempcomposantsets AS TCS "
					+ "INNER JOIN inventories AS INV "
					+ "ON TCS.set_num = INV.set_num "
					+ "INNER JOIN inventory_sets AS INVS "
					+ "ON INV.id = INVS.inventory_id "
					+ "INNER JOIN sets AS S2 "
					+ "ON INVS.set_num = S2.set_num ";
			    od.update(sql, null);
			 
			    sql = "DELETE from tempcomposantsets";
			    od.update(sql, null);
			    
			    sql = 
					  "INSERT INTO tempcomposantsets (set_num,quantity) "  
				    + "SELECT * "
					+ "FROM tempcomposantsetsbuffer " ;
			    od.update(sql, null);
			    
			    sql = "DELETE from tempcomposantsetsbuffer";
			    od.update(sql, null);
			    
			    //Update boolean stillHasComponentSets
			    stillHasComponentSets = isEmptyTable(od, "tempcomposantsets");
			}	
		} catch (SQLException e) {
			e.printStackTrace();
		} finally {
			sql = "delete from tempcomposantsets";
			od.update(sql, null);
			
			sql = "delete from tempcomposantsetsbuffer";
			od.update(sql, null);
		}
	}
	
	/** Fonction that inserts all records needed in Table PartsNeedAllSets*/
	public static void completeTablePartsNeedAllSets(OperationDatabase od) {
		String sql = null;
		ResultSet rs = null;
		String set_num_current = null;
		
		try {
			sql = "SELECT set_num FROM sets ";
			rs = od.search(sql, null);
			int row = 1;
			while(rs.next()) {
				if(row >= 15093 ) {
					set_num_current = rs.getString("set_num");
					System.out.printf("Finding composant parts of set %d with set_num %s\n", row, set_num_current);
					findComposantParts(od, set_num_current);
					System.out.printf("Finished finding composant parts of set %d with set_num %s\n\n", row, set_num_current);
				}
				row++;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}