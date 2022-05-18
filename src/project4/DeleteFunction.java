package project4;

import java.sql.ResultSet;
import java.sql.SQLException;

import database.OperationDatabase;

public class DeleteFunction {
	private OperationDatabase od;
	
	public DeleteFunction(OperationDatabase od) {
		this.od = od;
	}
	
	/**Function thats deletes one part in a certain quantity from a table with 3 COLUMNS : part_num, color, quantity. 
	 * Displays messages if boolean PrintMessages is true*/
	public void deletePartsFromATable (String table, boolean boolDisplayMsg, String part_num, String color, String quantity_part_delete) {
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
			
			//If part exists in table, modify the exisiting record
			if (rs.next()) { 							
				String quantity_part_have = rs.getString("quantity");
				int quantity_part_have_int = Integer.parseInt(quantity_part_have);	
				int quantity_part_delete_int = Integer.parseInt(quantity_part_delete);			
				
				if(quantity_part_have_int>quantity_part_delete_int) { 
					//If quantity that we want to delete < quantity that we have, reduce quantity in the record, which will lead to a positive remaining quantity
					if(boolDisplayMsg) System.out.println(	"You have "+ quantity_part_have +" times this part:"+part_num +"\n " +"You will now delete " + quantity_part_delete + " unity/unities \n"); 
					sql = "UPDATE "+ table +" "
					    	+ "SET quantity = ? "
							+ "WHERE part_num = ? "
							+ "AND color = ? ";
					strArr = new String[]{String.valueOf(quantity_part_have_int-quantity_part_delete_int), part_num, color};
					od.update(sql, strArr);
					if(boolDisplayMsg) System.out.printf("SUCCESS deleting part %s with color code %s\n", part_num, color);
				}else if(quantity_part_have_int==quantity_part_delete_int) {
					//If quantity that we want to delete = quantity that we have, delete all information of this part
					String sql2 = "delete from "+ table + " where part_num=? and color=?";
				    String[] str=new String[]{part_num,color};
				    od.update(sql2, str);
				    if(boolDisplayMsg) System.out.printf("SUCCESS deleting part %s with color code %s\n", part_num, color);
				}else{
					//If quantity that we want to delete > quantity that we have, do nothing, invalid operation.
					if(boolDisplayMsg) System.out.printf("FAILURE deleting part %s with color code %s because quantity invalid. You only have %s unity/unities \n", part_num, color, quantity_part_have);
				}
			}
			//If part does not exist in table, do nothing, invalid operation.
			else {
				if(boolDisplayMsg) System.out.printf("FAILURE deleting part %s with color code %s because does not exist\n", part_num, color);
			}
			rs.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
	
	/**Overload of the previous function addPartsToATable. 
	 * Deletes all parts associated with set from TABLE mypartsFromSet which contains TWO EXTRA COLUMNS with different deleting logic*/
	public void deletePartsFromATable(String set_num, String quantity_set_delete) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		
		try {
			sql = 
				  "SELECT quantity_set "
				+ "FROM mypartsFromSet "
				+ "WHERE set_num = ? ";
			strArr = new String[] {set_num}; 
			rs = od.search(sql, strArr);

			//If there are parts associated with set in table, modify the existing records of these parts
			if (rs.next()) { 	
				String quantity_set_have = rs.getString("quantity_set"); 
				int quantity_set_have_int = Integer.parseInt(quantity_set_have);	
				int quantity_set_delete_int = Integer.parseInt(quantity_set_delete);			
				
				if(quantity_set_have_int>quantity_set_delete_int) { 
					//If quantity that we want to delete < quantity that we have, reduce quantity in the record, which will lead to a positive remaining quantity
					sql = "UPDATE mypartsFromSet "
					    	+ "SET quantity_set = ? "
							+ "WHERE set_num = ? ";
					strArr = new String[]{String.valueOf(quantity_set_have_int-quantity_set_delete_int), set_num};
					od.update(sql, strArr);
				}else if(quantity_set_have_int==quantity_set_delete_int) {
					//If quantity that we want to delete = quantity that we have, delete all information of this part
					String sql2 = "DELETE FROM mypartsFromSet where set_num=? ";
				    String[] str=new String[]{set_num};
				    od.update(sql2, str);
				}
				//If quantity that we want to delete > quantity that we have, do nothing, invalid operation.
			}
			//If part-set does not exist in table, do nothing, invalid operation.
			rs.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}

	/** Function that deletes all the parts that make up a set (including parts of component sets) from tables myparts and mypartsFromSet */
	public void deletePartsOfSet(String set_num, String quantity_set_delete) {
		String sql = null;
		String strArr[] = null;
		ResultSet rs = null;
		
		try {
			sql = "SELECT part_num, color, quantity " //TODO No need to use PartsHaveAllSets. Use mypartsFromSet
				+ "FROM PartsHaveAllSets " //TODO Change table to PartsHaveAllSets to include spare parts
				+ "WHERE set_num = ? ";
			strArr = new String[]{set_num};
			rs = od.search(sql, strArr);
			while(rs.next()) {
				String part_num = rs.getString("part_num"); 
				String color = rs.getString("color");
				String quantity_part_per_set = rs.getString("quantity");
				String total_quantity_part = String.valueOf(Integer.parseInt(quantity_part_per_set) * Integer.parseInt(quantity_set_delete));
				
				//Delete parts associated in myparts
				deletePartsFromATable("myparts", false, part_num, color, total_quantity_part);
			}
			rs.close();
			//Delete parts associated in mypartsFromSet
			deletePartsFromATable(set_num, quantity_set_delete);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	
	}
	
	/**Function thats deletes a set in a certain quantity from table mysets. Deletes associated parts as well from tables myparts and mypartsFromSet */
	public void deleteSetsFromATable(String set_num, String quantity_set_delete) {
		String sql = null;
		ResultSet rs = null;
		String[] strArr = null;
		
		try {
			sql = 
				  "SELECT quantity "
				+ "FROM mysets "
				+ "WHERE set_num = ? ";
			strArr = new String[] {set_num}; 
			rs = od.search(sql, strArr);
			//If set exists in mysets, modify the exisiting record
			if (rs.next()) { 							
				String quantity_set_have = rs.getString("quantity");
				int quantity_set_have_int = Integer.parseInt(quantity_set_have);	
				int quantity_set_delete_int = Integer.parseInt(quantity_set_delete);
				
				//If quantity that we want to delete < quantity that we have, reduce quantity in the record, which will lead to a positive remaining quantity
				if(quantity_set_have_int>quantity_set_delete_int) {
					System.out.println(	"You have "+ quantity_set_have +" times this set:"+set_num +"\n "
							   +"You will now delete " + quantity_set_delete + " unity/unities \n"); 		
					sql = "UPDATE mysets "
				    	+ "SET quantity = ? "
						+ "WHERE set_num = ? ";
					strArr = new String[]{String.valueOf(quantity_set_have_int-quantity_set_delete_int), set_num};
					od.update(sql, strArr);
					
					//Deletes parts associated as well from myparts and mypartsFromSet
					deletePartsOfSet(set_num, quantity_set_delete);
					System.out.printf("SUCCESS deleting set %s\n", set_num);
				}
				//If quantity that we want to delete = quantity that we have, delete all information of this set
				else if(quantity_set_have_int==quantity_set_delete_int) {
					String sql2 = "delete from mysets where set_num=?";
				    String[] str=new String[]{set_num};
				    od.update(sql2, str);
				    
				    //Deletes component parts as well from myparts
				    deletePartsOfSet(set_num, quantity_set_delete);
					System.out.printf("SUCCESS deleting set %s\n", set_num);
				}
				//If quantity that we want to delete > quantity that we have, do nothing, invalid operation.
				else System.out.printf("FAILURE deleting set %s because quantity invalid. You only have %s unity/unities \n", set_num, quantity_set_have);
			}
			//If set does not exist in mysets, do nothing, invalid operation.
			else System.out.printf("FAILURE deleting set %s because does not exist\n", set_num);
			rs.close();
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
}
