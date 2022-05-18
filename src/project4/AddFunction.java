package project4;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import database.OperationDatabase;

public class AddFunction {
	private OperationDatabase od;
	
	public AddFunction(OperationDatabase od) {
		this.od = od;
	}
	
	/**Function that verifies if set exists in database*/
	public boolean verifyPartExists(String part_num, String color) {
		try {
			String sql = "SELECT P.part_num "
					   + "FROM  parts AS P "
					   + "INNER JOIN inventory_parts AS INVP "
					   + "ON P.part_num = INVP.part_num "
					   + "WHERE INVP.part_num = ? "
					   + "AND INVP.color_id = ? ";
			String[] strArr = new String[] {part_num, color}; 
			ResultSet rs = od.search(sql, strArr);
			if(rs.next()) {
				rs.close();
				return true;
			}
			rs.close();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**Function that verifies if set exists in database*/
	public boolean verifySetExists(String set_num) {
		try {
			String sql = "SELECT set_num "
					   + "FROM  PartsHaveAllSets " // TODO : wait for table to be finished
					   + "WHERE set_num = ? ";
			String[] strArr = new String[] {set_num}; 
			ResultSet rs = od.search(sql, strArr);
			if(rs.next()) {
				rs.close();
				return true;
			}
			rs.close();
			return false;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**Function thats adds one part in a certain quantity in ONE table. Displays messages if boolean PrintMessages is true
	 * Note : FAST but two precautions:
	 * 1. Table contains ONLY three columns part_num, color and quantity
	 * 2. User can add any code color he wants. (We don't verify if part exists in database or not here because it takes too much time)*/
	public void addPartsToATable(String table, boolean boolDisplayMsg, String part_num, String color, String quantity_part_add) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		
		try {
			sql = 
				  "SELECT quantity "
				+ "FROM " + table + " "
				+ "WHERE part_num = ? "
				+ "AND color = ? ";
			strArr = new String[] {part_num, color}; 
			rs = od.search(sql, strArr);

			//If part already exists in table, modify the existing record (Note : rs.next() returns true if result non empty)
			if (rs.next()) { 	
				String quantity_part_have = rs.getString("quantity"); 
				String total_quantity = String.valueOf(Integer.parseInt(quantity_part_have) + Integer.parseInt(quantity_part_add));
				
				if (boolDisplayMsg) System.out.println(	"You already have "+ quantity_part_have +" times this part:"+part_num +"\n "
						   +"You will now add another " + quantity_part_add + " more\n"); 			
				sql = "UPDATE " + table + " "
			    	+ "SET quantity = ? "
					+ "WHERE part_num = ? "
					+ "AND color = ? ";
				strArr = new String[]{total_quantity, part_num, color};
				od.update(sql, strArr);
			}
			//If part does not exist in table, create a new record.
			else {		
					sql = "insert into " + table +" (part_num,color,quantity) values(?,?,?) ";
				    strArr =new String[]{part_num,color, quantity_part_add};
				    od.update(sql, strArr);
				} 
			rs.close();
			if(boolDisplayMsg) {
				//Show what you add in case of success	
				System.out.printf("\nSUCCESS adding part %s with color code %s\nBelow is the updated information:\n\n", part_num, color);
				sql = 
						  "SELECT * "
						+ "FROM " +table+ " "
						+ "WHERE part_num = ? "
						+ "AND color = ? ";
				strArr = new String[] {part_num, color}; 
				rs = od.search(sql, strArr);
				od.printResult(rs);
				rs.close();
			}
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
	
	/**Overload of the previous function addPartsToATable. 
	 * Adds one part in a certain quantity in TABLE mypartsFromSet which contains TWO EXTRA COLUMNS with different adding logic*/
	public void addPartsToATable(String part_num, String color, String quantity_part_per_set, String set_num, String quantity_set_add) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		
		try {
			sql = 
				  "SELECT quantity_set "
				+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? "
				+ "AND set_num = ? ";
			strArr = new String[] {part_num, color, set_num}; 
			rs = od.search(sql, strArr);

			//If part-set already exists in table, modify the existing record 
			if (rs.next()) { 	
				String quantity_set_have = rs.getString("quantity_set"); 
				String total_quantity = String.valueOf(Integer.parseInt(quantity_set_have) + Integer.parseInt(quantity_set_add));
						
				sql = "UPDATE mypartsFromSet "
			    	+ "SET quantity_set = ? "
					+ "WHERE part_num = ? "
					+ "AND color = ? "
					+ "AND set_num = ? ";
				strArr = new String[]{total_quantity, part_num, color, set_num};
				od.update(sql, strArr);
			}
			//If part-set does not exist in table, create a new record.
			else {		
				sql = "INSERT INTO mypartsFromSet (part_num,color,quantity, set_num, quantity_set) values(?,?,?,?,?) ";
			    strArr =new String[]{part_num,color, quantity_part_per_set, set_num, quantity_set_add};
			    od.update(sql, strArr);
				} 
			rs.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}

	/** Function that adds all the parts that make up a set (including parts of component sets) to tables myparts and mypartsFromSet */
	public List<String> addPartsOfSet(String set_num, String quantity_set_add) {
		String sql = null;
		String strArr[] = null;
		ResultSet rs = null;
		List<String> listPartsOfSet = new ArrayList<>();
		
		try {
			sql = "SELECT part_num, color, quantity "
				+ "FROM PartsHaveAllSets " //TODO Change table to PartsHaveAllSets to include spare parts
				+ "WHERE set_num = ? ";
			strArr = new String[]{set_num};
			rs = od.search(sql, strArr);
			while(rs.next()) {
				String part_num = rs.getString("part_num"); 
				String color = rs.getString("color");
				String quantity_part_per_set = rs.getString("quantity");
				String total_quantity_part = String.valueOf(Integer.parseInt(quantity_part_per_set) * Integer.parseInt(quantity_set_add));
				
				addPartsToATable("myparts", false, part_num, color, total_quantity_part);
				addPartsToATable(part_num, color, quantity_part_per_set, set_num, quantity_set_add);
				listPartsOfSet.add(part_num);
				listPartsOfSet.add(color);
				listPartsOfSet.add(total_quantity_part);
			}
			return listPartsOfSet;
		} catch (SQLException e) {
			e.printStackTrace();
			return listPartsOfSet;
		}
	} 
	
	public List<String> findPartsConstitutingASet(String set_num, String quantity_set_add) {
		String sql = null;
		String strArr[] = null;
		ResultSet rs = null;
		List<String> listPartsOfSet = new ArrayList<>();
		
		try {
			sql = "SELECT part_num, color, quantity "
				+ "FROM PartsHaveAllSets " //TODO Change table to PartsHaveAllSets to include spare parts
				+ "WHERE set_num = ? ";
			strArr = new String[]{set_num};
			rs = od.search(sql, strArr);
			while(rs.next()) {
				String part_num = rs.getString("part_num"); 
				String color = rs.getString("color");
				String quantity_part_per_set = rs.getString("quantity");
				String total_quantity_part = String.valueOf(Integer.parseInt(quantity_part_per_set) * Integer.parseInt(quantity_set_add));
				
				listPartsOfSet.add(part_num);
				listPartsOfSet.add(color);
				listPartsOfSet.add(total_quantity_part);
			}
			return listPartsOfSet;
		} catch (SQLException e) {
			e.printStackTrace();
			return listPartsOfSet;
		}
	} 
	
	
	/**Function thats adds one set in a certain quantity in mysets. Displays messages if boolean PrintMessages is true
	 * Note : Table mysets contains columns set_num, color, quantity, name, years, theme_id, num_parts
	 * Possible to generalize to any tables and include boolean boolDisplayMsg to block display of messages just like addPartsToATable */
	public void addSetsToATable(String set_num, String quantity_set_add) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		
		try {
			sql = 
				  "SELECT quantity "
				+ "FROM mysets "
				+ "WHERE set_num = ? ";
			strArr = new String[] {set_num}; 
			rs = od.search(sql, strArr);
			//If set already exists in mysets, modify the exisiting record (Note : rs.next() returns true if result non empty)
			if (rs.next()) { 								
				String quantity_set_have = rs.getString("quantity");	
				String total_quantity = String.valueOf(Integer.parseInt(quantity_set_have) + Integer.parseInt(quantity_set_add));
				
				System.out.println(	"You already have "+ quantity_set_have +" times this part:"+set_num +"\n "
						   +"You will now add another " + quantity_set_add + " more\n"); 				
				sql = "UPDATE mysets "
				    	+ "SET quantity = ? "
						+ "WHERE set_num = ? ";
				strArr = new String[]{total_quantity, set_num};
				od.update(sql, strArr);
				rs.close();
			}
			//If set does not exist in mysets, create a new record.
			else {
				//Find complementary information about this set in sets
				sql = "SELECT name, years, theme_id, num_parts "
					+ "FROM sets "
				    + "WHERE set_num = ? ";
				strArr = new String[]{set_num};
			    ResultSet rsInfo = od.search(sql, strArr);
			    
			    //If this set exists in database, proceed to create a new record
			    if(rsInfo.next()) { 	
					String name = rsInfo.getString("name");
					String years = rsInfo.getString("years");
					String theme_id = rsInfo.getString("theme_id");
					String num_parts = rsInfo.getString("num_parts");
					
					String sql2 = "insert into mysets (set_num,name,years,theme_id,num_parts,quantity) values(?,?,?,?,?,?)";
				    String[] str=new String[]{set_num,name,years,theme_id,num_parts,quantity_set_add};
				    od.update(sql2, str);
				    rsInfo.close();
				} 
			    //If this set does not exist in database, inform user.
			    else {				
					System.out.printf("FAILURE adding set %s because does not exist in database\n", set_num);
					return;
				}
			}		
			//Adds information of parts related to this set into tables myparts and mypartsFromSet 
			addPartsOfSet(set_num, quantity_set_add); 
			
			//Show what you add in case of success	
			System.out.printf("SUCCESS adding set %s\nBelow is the updated information:\n\n", set_num);
			sql = "select * from mysets where set_num ='"+set_num+"'";	
			rs = od.search(sql, null);
			od.printResult(rs);
			
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
	
}
