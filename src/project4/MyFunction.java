package project4;


import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;



import database.OperationDatabase;


public class MyFunction {
	private OperationDatabase od;
	private AddFunction addFunction;
	private DeleteFunction deleteFunction;
	private AnalyseFunction analyseFunction;
	
	public MyFunction(OperationDatabase od) {
		this.od = od;
		addFunction = new AddFunction(od);
		deleteFunction = new DeleteFunction(od);
		analyseFunction = new AnalyseFunction(od);
	}

	/**Function that adds a part in certain quantity in both tables myparts and mypartsT*/
	public void addMyParts(String part_num, String color, String quantity) {
		if(addFunction.verifyPartExists(part_num, color)) {
			addFunction.addPartsToATable("myparts", true, part_num, color, quantity);
			addFunction.addPartsToATable("mypartsT", false, part_num, color, quantity);
		}
		else System.out.printf("\nFAILURE adding part %s with color code %s because does not exist in database\n", part_num, color);
	}
	
	/**Function that adds a set in certain quantity in table mysets  */
	public void addMySets(String set_num, String quantity) {
		if(addFunction.verifySetExists(set_num)) {
			addFunction.addSetsToATable(set_num, quantity);
		}
		else System.out.printf("\nFAILURE adding set %s because does not exist in database\n", set_num);
	}
	
	/**Function that deletes all parts and hence all sets*/
	public void deleteAllParts() {
		String sql = null;

		sql = "DELETE FROM myparts ";
		od.update(sql, null);
		
		sql = "DELETE FROM mypartsT ";
		od.update(sql, null);
		
		sql = "DELETE FROM mypartsFromSet ";
		od.update(sql, null);
		
		sql = "DELETE FROM mysets ";
		od.update(sql, null);
		
		System.out.println("SUCCESS deleting all parts and hence all sets too");
	}
	
	public void deleteMyPartsShowWarning(String part_num, String color, String quantity_part_delete, String quantity_part_have, String quantity_part_isolate) {
		String sql = null;
		String[] strArr = null;
		try {						
			System.out.printf("MESSAGE : You want to delete %s part(s) and you have %s part(s) in total :\n\n"
					+ "DETAILS ON PARTS DECOMPOSITION:\n"
					+ "You have %s part(s) that don't make up sets\n", 
					quantity_part_delete, quantity_part_have, quantity_part_isolate);
			
			sql = 
				  "SELECT quantity, set_num, quantity_set, quantity*quantity_set AS total_quantity "
				+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? ";
			strArr = new String[] {part_num, color}; 
			ResultSet rs4 = od.search(sql, strArr);
			if(rs4.next()){
				System.out.printf("WARNING : You may destroy the following sets containing part %s with color code %s", part_num, color);
				do {
					String quantity_per_set = rs4.getString("quantity");
					String set_num = rs4.getString("set_num");
					String quantity_set = rs4.getString("quantity_set");
					String total_quantity = rs4.getString("total_quantity");
					
					System.out.printf("You have %s part(s) coming from Set %s (You have %s set(s) and each set contains %s part(s))\n", total_quantity, set_num, quantity_set, quantity_per_set);
				}while(rs4.next());
				rs4.close();
			}
		}catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
	
	//If quantity of isolated parts is not enough, delete parts from mypartsT and then mypartsFromSet while destroying set
	public int deleteMyPartsAux1(String part_num, String color, String quantity_part_delete, String set_num_destroy) {
		//NOTE : return quantity_will_not_delete_int; if = 0 , everything is ok, quantity to delete respected ; if > 0, quantity to delete not respected, there is still quantity_will_not_delete_int left 
		String sql = null;
		String[] strArr = null;
		int quantity_part_delete_int = Integer.parseInt(quantity_part_delete);
		String quantity_part_isolate = "0";
		int quantity_will_not_delete_int = 0;
		
		try {	
			//Retrieve quantity_part_isolate and delete isolated parts
			sql = 
				  "SELECT quantity "
				+ "FROM mypartsT "
				+ "WHERE part_num = ? "
				+ "AND color = ? ";
			strArr = new String[] {part_num, color}; 
			ResultSet rs2 = od.search(sql, strArr);
			if(rs2.next()) quantity_part_isolate = rs2.getString("quantity");
				
			sql = "DELETE FROM mypartsT where part_num=? and color=?";
			strArr = new String[]{part_num,color};
			od.update(sql, strArr);
			//Continue
			sql = 
				  "SELECT quantity, set_num, quantity_set "
				+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? "
				+ "AND set_num = ? ";
			strArr = new String[] {part_num, color, set_num_destroy}; 
			ResultSet rs5 = od.search(sql, strArr);
			
			if(rs5.next()) {
				String quantity_per_set = rs5.getString("quantity");
				int quantity_per_set_int = Integer.parseInt(quantity_per_set);
				
				String quantity_set = rs5.getString("quantity_set");
				int quantity_set_int = Integer.parseInt(quantity_set);
				
				int quantity_delete_remaining_int = Integer.parseInt(quantity_part_delete)-Integer.parseInt(quantity_part_isolate);
				String quantity_delete_remaining = String.valueOf(quantity_delete_remaining_int);
				
				boolean notRespectingDemand = false;
				
				rs5.close();
				if(quantity_per_set_int < quantity_delete_remaining_int) {
					notRespectingDemand = true;
					quantity_will_not_delete_int = quantity_delete_remaining_int - quantity_per_set_int;
					quantity_part_delete_int -= quantity_will_not_delete_int;
					quantity_part_delete = String.valueOf(quantity_part_delete_int);
					quantity_delete_remaining_int = quantity_per_set_int;
					quantity_delete_remaining = String.valueOf(quantity_delete_remaining_int);
				}

				//Before destroying a unity of related set, copy all parts, including the ones we'll delete, from mypartsFromSet to mypartsT
			    sql = "SELECT part_num, color, quantity " 	//Note : Can't do "insert into" sql right away because parts may already exist in mypartsT
		    		+ "FROM mypartsFromSet "				//Note : quantity is quantity per set because we only delete a unity of set
					+ "WHERE set_num = ? ";
			    strArr = new String[] {set_num_destroy}; 
			    ResultSet rs6 = od.search(sql, strArr);
			    
			    while(rs6.next()) {
			    	String part_num_others = rs6.getString("part_num");
			    	String color_others = rs6.getString("color");
			    	String quantity_others = rs6.getString("quantity");
			    	
			    	addFunction.addPartsToATable("mypartsT", false, part_num_others, color_others, quantity_others);
			    }
			    rs6.close();
			    
			    if(quantity_set_int==1) {
			    	//Destroy set 
				    sql = "DELETE FROM mysets "
			    		+ "WHERE set_num = ? ";
				    strArr = new String[] {set_num_destroy};
				    od.update(sql, strArr);
				    
			    	//Destroy parts related
				    sql = "DELETE FROM mypartsFromSet "
			    		+ "WHERE set_num = ? ";
				    od.update(sql, strArr);
				    
				    //Destroy parts that we wanted to delete
				    
				    deleteFunction.deletePartsFromATable ("mypartsT", false, part_num, color, quantity_delete_remaining);
				    deleteFunction.deletePartsFromATable ("myparts", false, part_num, color, quantity_part_delete);  	
			    } 
			    else if (quantity_set_int>1) {
			    	//Reduce number of set by 1 in mysets
			    	sql = "UPDATE mysets "
			    		+ "SET quantity = ?"
			    		+ "WHERE set_num = ? ";
			    	String new_quantity_set_int = String.valueOf(quantity_set_int - 1);
				    strArr = new String[] {new_quantity_set_int, set_num_destroy};
				    od.update(sql, strArr);
				    
				    //Reduce number of set by 1 in mysets in mypartsFromSet
				    sql = "UPDATE mypartsFromSet "
				    	+ "SET quantity_set = ?"
			    		+ "WHERE set_num = ? ";
				    od.update(sql, strArr);
				    
				    //Destroy parts that we wanted to delete
				    deleteFunction.deletePartsFromATable ("mypartsT", false, part_num, color, quantity_delete_remaining);
				    deleteFunction.deletePartsFromATable ("myparts", false, part_num, color, quantity_part_delete);  	
			    } 
			    else {
			    	//Given the calculation, system should never reach this else
					System.out.printf("FAILURE deleting part %s with color code %s. Error processing quantity of set\n", part_num, color);
					return quantity_will_not_delete_int;
			    }	
			    
			    //Inform user if the quantity to delete has not been respected
			    if(notRespectingDemand) {
				    System.out.printf("SUCCESS deleting part %s with color code %s\n"
				    		+ "But WARNING : Total asked deleting quantity has not been respected\n"
				    		+ "%d parts has NOT been deleted\n"
				    		+ "You can rechoose deleting option to delete them\n", part_num, color, quantity_will_not_delete_int);
				    return quantity_will_not_delete_int;
			    }
			    
			    //Inform user if the quantity to delete has indeed been respected
			    System.out.printf("SUCCESS deleting part %s with color code %s in total asked quantity of %s\n", part_num, color, quantity_part_delete);
			    
			    
			} 
			else {
				System.out.println("Failure deleting because invalid set_num");
				return quantity_will_not_delete_int;
			}
			return quantity_will_not_delete_int;
		} catch (SQLException e) {	
			e.printStackTrace();
			return quantity_will_not_delete_int;
		} 
	}
	
	public void deleteMyPartsAux2(String part_num, String color, String quantity_part_delete) {
		String sql = null;
		String[] strArr = null;
		
		try {				    
		    //Before destroying related sets, copy all parts, including the ones we'll delete, from mypartsFromSet to mypartsT
		    sql = "SELECT part_num, color, quantity*quantity_set AS quantity " //Note :  Can't do "insert into" sql right away because parts may already exist in mypartsT
	    		+ "FROM mypartsFromSet "
				+ "WHERE set_num IN ( "
				+ "SELECT DISTINCT set_num "
				+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? ) ";
		    strArr = new String[] {part_num, color};
		    ResultSet rs4 = od.search(sql, strArr);
		    while(rs4.next()) {
		    	String part_num_others = rs4.getString("part_num");
		    	String color_others = rs4.getString("color");
		    	String quantity_others = rs4.getString("quantity");
		    	
		    	addFunction.addPartsToATable("mypartsT", false, part_num_others, color_others, quantity_others);
		    }
		    rs4.close();
		    
		    //Destroy related sets
		    sql = "DELETE FROM mysets "
	    		+ "WHERE set_num IN ( "
	    		+ "SELECT DISTINCT set_num "
	    		+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? ) ";
		    od.update(sql, strArr);
		       
		    //Destroy concerned parts
		    sql = "DELETE FROM mypartsFromSet "
	    		+ "WHERE set_num IN ( "
				+ "SELECT DISTINCT set_num "
				+ "FROM mypartsFromSet "
				+ "WHERE part_num = ? "
				+ "AND color = ? ) ";
		    od.update(sql, strArr);
		    
		    sql = "DELETE FROM mypartsT where part_num=? and color=?";
			strArr = new String[]{part_num,color};
			od.update(sql, strArr);
		    
			sql = "DELETE FROM myparts where part_num=? and color=?";
		    od.update(sql, strArr);
		    
		    System.out.printf("SUCCESS deleting part %s with color code %s\n", part_num, color);
		
		} catch (SQLException e) {	
			e.printStackTrace();
		} 
	}
	
	/**Function that deletes a part in certain quantity. Can't simply use deletePartsFromATable from SurFunction.java.
	 * due to client's demand to be able to delete parts from set. Hence the function code is longer to specify program's behavior.
	 * Note : Needs scanner to ask for information from user according to the situation*/
	public int deleteMyParts(String part_num, String color, String quantity_part_delete) {
		String sql = null;
		String[] strArr = null;
		
		try {
			sql = 
				  "SELECT quantity "
				+ "FROM myparts "
				+ "WHERE part_num = ? "
				+ "AND color = ? ";
			strArr = new String[] {part_num, color}; 
			ResultSet rs = od.search(sql, strArr);

			//If part exists in table, continue
			if(rs.next()) {
				String quantity_part_have = rs.getString("quantity");
				int quantity_part_have_int = Integer.parseInt(quantity_part_have);		//Includes all parts : parts that make up sets and parts that don't
				int quantity_part_delete_int = Integer.parseInt(quantity_part_delete);			
				rs.close();
				
				//If quantity that we want to delete > quantity that we have, do nothing, invalid operation.
				if(quantity_part_have_int<quantity_part_delete_int) {
					System.out.printf("FAILURE deleting part %s with color code %s because quantity invalid. You only have %s unity/unities \n", part_num, color, quantity_part_have);
					return 0;
				}
				
				//First, obtain number of isolated parts that don't make up sets
				String quantity_part_isolate = "0";
				int quantity_part_isolate_int = 0;
				sql = 
					  "SELECT quantity "
					+ "FROM mypartsT "
					+ "WHERE part_num = ? "
					+ "AND color = ? ";
				strArr = new String[] {part_num, color}; 
				ResultSet rs2 = od.search(sql, strArr);
				if(rs2.next()) {
					quantity_part_isolate = rs2.getString("quantity");
					quantity_part_isolate_int = Integer.parseInt(quantity_part_isolate);
				}
				rs2.close();
				
				//If quantity that we want to delete < quantity that we have, reduce quantity in the record : Prioritize deleting parts that don't make up set first
				if(quantity_part_have_int>quantity_part_delete_int) {
					//If quantity of isolated parts is enough, delete parts only from mypartsT
					if(quantity_part_isolate_int > 0) {		
						if(quantity_part_isolate_int > quantity_part_delete_int) {
							sql = "UPDATE mypartsT "
						    	+ "SET quantity = ? "
								+ "WHERE part_num = ? "
								+ "AND color = ? ";
							strArr = new String[]{String.valueOf(quantity_part_isolate_int-quantity_part_delete_int), part_num, color};
							od.update(sql, strArr);
							
							sql = "UPDATE myparts "
						    	+ "SET quantity = ? "
								+ "WHERE part_num = ? "
								+ "AND color = ? ";
							strArr = new String[]{String.valueOf(quantity_part_have_int-quantity_part_delete_int), part_num, color};
							od.update(sql, strArr);
								
							System.out.printf("SUCCESS deleting part %s with color code %s\n", part_num, color);
							return 10;
						}
						else if(quantity_part_isolate_int == quantity_part_delete_int) {
							sql = "DELETE FROM mypartsT "
								+ "WHERE part_num=? "
								+ "AND color=? ";
						    strArr = new String[]{part_num,color};
						    od.update(sql, strArr);
						    
						    sql = "UPDATE myparts "
						    	+ "SET quantity = ? "
								+ "WHERE part_num = ? "
								+ "AND color = ? ";
							strArr = new String[]{String.valueOf(quantity_part_have_int-quantity_part_delete_int), part_num, color};
							od.update(sql, strArr);
								
						    System.out.printf("SUCCESS deleting part %s with color code %s\n", part_num, color);
						    return 10;
						}
					}
					
					//If quantity of isolated parts is not enough, delete parts from mypartsT and then mypartsFromSet while destroying set
//-----------------------------------------BOOKMARK1/+----------------------------------------------------------------------
					return 2;
//					deleteMyPartsShowWarning(part_num, color, quantity_part_delete, quantity_part_have, quantity_part_isolate);
//					boolean reponseUser = false; //IHM ASKS USER
//					if(reponseUser) {
//						String set_num_destroy = "asks user for this value";//IHM ASKS USER
//						deleteMyPartsAux1(part_num, color, quantity_part_delete, quantity_part_have, quantity_part_isolate, set_num_destroy);
//					}
//					else System.out.println("Deleting operation CANCELLED");
//-----------------------------------------BOOKMARK1----------------------------------------------------------------------
				}
				//If quantity that we want to delete = quantity that we have, delete all parts : parts that make up sets and parts that don't
				else if(quantity_part_have_int==quantity_part_delete_int) {
//-----------------------------------------BOOKMARK2----------------------------------------------------------------------
					return 3;
//					deleteMyPartsShowWarning(part_num, color, quantity_part_delete, quantity_part_have, quantity_part_isolate);
//					boolean reponseUser = false; //IHM ASKS USER
//					if(reponseUser) deleteMyPartsAux2(part_num, color, quantity_part_delete);
//					else System.out.println("Deleting operation CANCELLED");
//-----------------------------------------BOOKMARK2----------------------------------------------------------------------

				}
			}
			//If part does not exist in table, do nothing, invalid operation
			else {
				rs.close();
				System.out.printf("FAILURE deleting part %s with color code %s because does not exist\n", part_num, color);
				return 1;
			} return 21;
		} catch (SQLException e) {	
			e.printStackTrace();
			return 20;
		} 
	}
	
	/** Function that deletes all sets and all parts associated with sets. Called by main*/
	public void deleteAllSets() { 
		String sql = null;
		ResultSet rs = null;
		
		try {
			//Delete all parts that make up sets in table myparts. We can't delete the records straight away because there may be some identical parts left that don't make up set.
			sql = "SELECT part_num, color, quantity*quantity_set AS total_quantity_part "
				+ "FROM mypartsFromSet ";
			rs = od.search(sql, null);
			while(rs.next()) {
				String part_num = rs.getString("part_num"); 
				String color = rs.getString("color");
				String quantity_part_delete = rs.getString("total_quantity_part");
				
				deleteFunction.deletePartsFromATable("myparts", false, part_num, color, quantity_part_delete);
			}
			rs.close();
			
			//Delete all parts that make up sets in table mypartsFromSet
			sql = "DELETE FROM mypartsFromSet ";
			od.update(sql, null);
			
			//Finally delete all sets
			sql = "DELETE FROM mysets ";
			od.update(sql, null);
			
			System.out.println("SUCCESS deleting all sets and parts associated");
		} catch (SQLException e) {
			e.printStackTrace();
			System.out.println("FAILURE deleting all sets and parts associated");
		
		}
	}
	
	/**Function that deletes/reduces quantity of one single set in mysets and deletes/reduces quantity of its parts in myparts*/
	public void deleteMySets(String set_num, String quantity) {
		deleteFunction.deleteSetsFromATable(set_num, quantity);
	}
	
	/**Function that shows all sets and parts collection. Parts that make up sets and that don't are shown all at once in display*/
	public void showMyCollection() {
		ResultSet rs = null;
		String sql = null;
		
		System.out.println("***************Myparts*****************(Including parts from mysets)\n\n");
		//Note : Avoid doing inner join with table inventory_parts as this would slow down massively the program
		sql = "SELECT P.name, MP.part_num, MP.color, P.part_cat_id, P.part_material_id, MP.quantity "
			+ "FROM myparts AS MP "
			+ "INNER JOIN parts AS P "
			+ "ON MP.part_num = P.part_num ";
		rs = od.search(sql, null);	
		od.printResult(rs);
		
		sql = "select * from mysets";
		rs = od.search(sql, null);
		System.out.println("***************Mysets*****************\n\n");
		od.printResult(rs);
	}
	
	/**Function that shows al parts collection. Parts that make up sets and that don't are shown all at once in display*/
	public List<String> showMyParts() {
		ResultSet rs = null;
		String sql = null;
		List<String> listMyParts = new ArrayList<>();
		
		try {
			//Note : Avoid doing inner join with table inventory_parts as this would slow down massively the program
			sql = "SELECT P.name, MP.part_num, MP.color, P.part_cat_id, P.part_material_id, MP.quantity "
				+ "FROM myparts AS MP "
				+ "INNER JOIN parts AS P "
				+ "ON MP.part_num = P.part_num ";
			rs = od.search(sql, null);	
			while(rs.next()) {
				String part_num = rs.getString("part_num");
				String color = rs.getString("color");
				String quantity = rs.getString("quantity");
				String name = rs.getString("name");
				String part_cat_id = rs.getString("part_cat_id");
				String part_material_id = rs.getString("part_material_id");
				
				listMyParts.add(part_num);
				listMyParts.add(color);
				listMyParts.add(quantity);
				listMyParts.add(name);
				listMyParts.add(part_cat_id);
				listMyParts.add(part_material_id);			
			}
			return listMyParts;
		} catch (SQLException e) {
			e.printStackTrace();
			return listMyParts;
		}
	
		
	}
	
	/**Function that shows all sets*/
	public List<String> showMySets() {
		ResultSet rs = null;
		String sql = null;
		List<String> listMyParts = new ArrayList<>();
		
		try {
			sql = "select * from mysets";
			rs = od.search(sql, null);
			while(rs.next()) {
				String set_num = rs.getString("set_num");
				String quantity = rs.getString("quantity");
				String name = rs.getString("name");
				
				listMyParts.add(set_num);
				listMyParts.add(quantity);
				listMyParts.add(name);
			}
			return listMyParts;
		} catch (SQLException e) {
			e.printStackTrace();
			return listMyParts;
		}
	}
	
	/**Function that count how many type of parts are in my collection*/
	public int  countTypeParts() {

		String sql = null;
		ResultSet rs = null;
		int numTypeI;
		try {
			sql = "select count(*) from myparts ";
			rs = od.search(sql, null);
			if(rs.next()) {
				String numType = rs.getString(1);
				numTypeI= Integer.parseInt(numType);
			}else {
			numTypeI = 0;
			}
		} catch (SQLException e) {
			e.printStackTrace();
			numTypeI = 0;
		} 
		return numTypeI;
	}
	/**Function that affirms and displays if a set is exactly realizable based on user's collection*/
	public boolean canDoSet(String set_num) {
		if(analyseFunction.isSetRealizable(set_num)) {
			System.out.println("Yes you can do this set ");
			return true;
		}
		else {
			System.out.println("NO you can't do this set ");
			return false;
		}
	}
	
	/**Function that finds and displays all exactly realizable sets based on user's collection*/
	public List<String> findRealizableSets() 	{
		
		int numTypePart = countTypeParts();
		if (numTypePart>377) {
			System.out.println("Case : more than 377");
			return analyseFunction.showRealizableSets();
		}else {
			System.out.println("Case : less than 377");
			//return analyseFunction.showRealizableSets();
			return analyseFunction.showRealizableSets2();
		}
		
	}

	/**Function that calculates and displays the percentage of completion of a set from user's collection.
	 * Note : Similar to canDoSet but dividing the two double variables (nbRowsPartsSatisfied/nbRowsPartsNeed) instead of only comparing two int variables*/
	public double findMissingPercentageSet(String set_num) {
		return analyseFunction.showMissingPercentageSet(set_num);
	}
	
	/**Function that calculates and displays the percentage of completion of all sets based on user's collection.*/
	public List<String> findMissingPercentageAllSets() {
		int numTypePart = countTypeParts();
		if (numTypePart>377) {
			System.out.println("more than 377");
			return analyseFunction.showMissingPercentageAllSets();
		}else {
			System.out.println("less than 377");

			//return analyseFunction.showMissingPercentageAllSets2();
			return analyseFunction.showMissingPercentageAllSets2();
		}
		
	}
	
	/**Function that finds and displays the missing parts to do a set based on user's collection.*/
	public List<String> findMissingParts(String set_num) {  
		return analyseFunction.showMissingParts(set_num);
	}
}