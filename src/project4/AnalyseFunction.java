package project4;


import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;


import database.OperationDatabase;

public class AnalyseFunction {
	private OperationDatabase od;
	public AnalyseFunction(OperationDatabase od) {
		this.od = od;
	}
	
	/** Function that finds all the parts that make up a set, including parts of component sets ; and add it to table 
	 * Used while analyzing realizability. Mostly used on table PartsNeed when it's empty
	 * Table ONLY contains 3 columns : part_num, color, quantity
	 * Table MUST NOT contain another part with the same part_num and color. This function doesn't "merge" quantity of the same part*/
	public void findPartsOfSet(String W_set, String quantity, String table) {
		String sql = null;
		String strArr[] = null;
		
		sql = "INSERT INTO " + table +" "
			+ "SELECT part_num, color, quantity "
			+ "FROM PartsNeedAllSets "
			+ "WHERE set_num = ? ";
		strArr = new String[]{W_set};
		od.update(sql, strArr);
		return;
	}
	
	/**Function that counts number of rows of the table PartsNeed*/
	public int countRowsPartsNeed() {
		String sql = null;
		ResultSet rs = null;
		int nbRowsPartsNeed = 0;
		
		try {
			sql = "SELECT quantity  "
				+ "FROM PartsNeed";
			rs = od.search(sql, null);
			while (rs.next()) {
				nbRowsPartsNeed = nbRowsPartsNeed + Integer.parseInt(rs.getString("quantity"));
			}
			
			rs.close();
			return nbRowsPartsNeed;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**Function that counts number of rows for the below query*/
	public int countRowsPartsSatisfied() {
		String sql = null;
		ResultSet rs = null;
		int nbRowsPartsSatisfied=0;
		
		try {
			sql = "SELECT PN.quantity  "
				+ "FROM PartsNeed as PN "
				+ "INNER JOIN myparts AS MP "
				+ "ON PN.part_num = MP.part_num "
				+ "AND PN.color = MP.color "
				+ "WHERE PN.quantity <= MP.quantity ";
			rs = od.search(sql, null);
			while (rs.next()) {
				nbRowsPartsSatisfied = nbRowsPartsSatisfied + Integer.parseInt(rs.getString("quantity"));
			}
			sql = "SELECT MP.quantity  "
					+ "FROM PartsNeed as PN "
					+ "INNER JOIN myparts AS MP "
					+ "ON PN.part_num = MP.part_num "
					+ "AND PN.color = MP.color "
					+ "WHERE PN.quantity > MP.quantity ";
			rs = od.search(sql, null);
			while (rs.next()) {
				nbRowsPartsSatisfied = nbRowsPartsSatisfied + Integer.parseInt(rs.getString("quantity"));
			}		
			rs.close();
			return nbRowsPartsSatisfied;
		} catch (SQLException e) {
			e.printStackTrace();
			return 0;
		}
	}
	
	/**Function that affirms if a set is exactly realizable based on user's collection*/
	public boolean isSetRealizable(String set_num) {
		//Strategy : First, disassemble MAIN SET into parts and store them in table PartsNeed and then compare PartsNeed VS myparts ie parts that we need VS parts that we have  //Note : Finding the exact same MAIN SET in user's mysets is not necessary and too time-consuming.
		String sql = null;
		int nbRowsPartsNeed = 0;
		int nbRowsPartsSatisfied = 0;
		boolean result = false;
														
		findPartsOfSet(set_num,"1", "PartsNeed"); 				
		nbRowsPartsNeed = countRowsPartsNeed();
		if(nbRowsPartsNeed == 0) {
			//Optional : You can warn user : System.out.printf("Invalid set_num %s. Set doesn't exist in database or Set is not ultimately made up of parts\n", set_num.trim());
		}
		else {
			nbRowsPartsSatisfied = countRowsPartsSatisfied();
			if(nbRowsPartsSatisfied == nbRowsPartsNeed) result = true;
		}
		sql = "delete from PartsNeed";	//MUST clean auxiliary table before returning
		od.update(sql, null);
		return result;
	}
	
	/**find the list of the set which can do > 0%*/
	public List<String> surSetList() {
		
		ResultSet rs = null;
		String sql = null;
		ResultSetMetaData rsmd = null;
		int nbCols = 0;
		
		List<String> listMyParts= new ArrayList<>();
		List<String> listMaybeSet= new ArrayList<>();
		List<String> listMaybeId= new ArrayList<>();
		List<String> listMaybeSetD2 = new ArrayList<>();
		//search part_num and color from MYPARTS store in List_My_parts
		try {
			
			sql = "select part_num,color from myparts";
			rs = od.search(sql, null);
			rsmd = rs.getMetaData();
			nbCols = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= nbCols; i++) {
					listMyParts.add(rs.getString(i));//first part_num then color
				}
			}
			rs.close();
			
			
		//with 	List_My_parts(part_num) search inventory_id in I_PARTS 
			for (int j = 0; j < listMyParts.size();j=j+2) {
				//Look for component parts of "Second level" set and store in table PartsNeed
				sql = "select inventory_id from inventory_parts where part_num = \'"+listMyParts.get(j)+"\' AND is_spare = 'f' AND color_id = "+listMyParts.get(j+1)+"";
				rs = od.search(sql, null);
				rsmd = rs.getMetaData();
				nbCols = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= nbCols; i++) {
						listMaybeId.add(rs.getString(i));
					}
				}
					
			}
			//Deduplication
			
			LinkedHashSet<String> Time1 = new LinkedHashSet<>(listMaybeId);
			ArrayList<String> listMaybeIdD1 = new ArrayList<>(Time1);

			
			//with List_Maybe_id_last search set_num from inventory store in List_Maybe_Set
			
			for (int j = 0; j < listMaybeIdD1.size();j++) {
				
				int id = Integer.parseInt(listMaybeIdD1.get(j));
				sql = "select set_num from inventories where id = "+id+"";
				rs = od.search(sql, null);
				rsmd = rs.getMetaData();
				nbCols = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= nbCols; i++) {
						listMaybeSet.add(rs.getString(i));
					}
				}
					
			}
			//Deduplication
			
			LinkedHashSet<String> Time2 = new LinkedHashSet<>(listMaybeSet);
			ArrayList<String> listMaybeSetD1 = new ArrayList<>(Time2);
			
			//with List_Maybe_Set_last(set_num) search id from I_SETS store in List_Maybe_id
			for (int j = 0; j < listMaybeSetD1.size();j++) {
				//Look for component parts of "Second level" set and store in table PartsNeed
				sql = "select inventory_id from inventory_sets where set_num = \'"+listMaybeSetD1.get(j)+"\'";
				rs = od.search(sql, null);
				rsmd = rs.getMetaData();
				nbCols = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= nbCols; i++) {
						listMaybeIdD1.add(rs.getString(i));
					}
				}
					
			}
			//Deduplication
			
			LinkedHashSet<String> Time3 = new LinkedHashSet<>(listMaybeIdD1);
			ArrayList<String> listMaybeIdD2 = new ArrayList<>(Time3);
			
			//with List_Maybe_id_D2 (id) search set_num from inventories
			
			for (int j = 0; j < listMaybeIdD2.size();j++) {
				
				int id = Integer.parseInt(listMaybeIdD2.get(j));
				sql = "select set_num from inventories where id = "+id+"";
				rs = od.search(sql, null);
				rsmd = rs.getMetaData();
				nbCols = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= nbCols; i++) {
						listMaybeSetD1.add(rs.getString(i));
					}
				}
					
			}
			//Deduplication
			
			LinkedHashSet<String> Time4 = new LinkedHashSet<>(listMaybeSetD1);
			listMaybeSetD2 = new ArrayList<>(Time4);			
					
			rs.close();
			
			/*for (int i=0;i <List_Maybe_Set_D2.size();i++) {
				try {
					s_f.writeTXT("test", "No_Data", List_Maybe_Set_D2.get(i) );
					}catch(IOException e) {
						
					}
				}
				*/

		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listMaybeSetD2;
		
	}
	/**find the list of the set which can do > 0%*/
	public List<String> surSetList2() {
		
		ResultSet rs = null;
		String sql = null;
		ResultSetMetaData rsmd = null;
		int nbCols = 0;
		
		List<String> listMyParts= new ArrayList<>();
		List<String> listMaybeSet= new ArrayList<>();
		List<String> listMaybeId= new ArrayList<>();
	
		//search part_num and color from MYPARTS store in List_My_parts
		try {
			
			sql = "select part_num,color from myparts";
			rs = od.search(sql, null);
			rsmd = rs.getMetaData();
			nbCols = rsmd.getColumnCount();
			while (rs.next()) {
				for (int i = 1; i <= nbCols; i++) {
					listMyParts.add(rs.getString(i));//first part_num then color
				}
			}
			rs.close();
			
			
		//with 	List_My_parts(part_num) search inventory_id in I_PARTS 
			for (int j = 0; j < listMyParts.size();j=j+2) {
				//Look for component parts of "Second level" set and store in table PartsNeed
				sql = "select set_num from PartsNeedAllSets where part_num = \'"+listMyParts.get(j)+"\'  AND color = "+listMyParts.get(j+1)+"";
				rs = od.search(sql, null);
				rsmd = rs.getMetaData();
				nbCols = rsmd.getColumnCount();
				while (rs.next()) {
					for (int i = 1; i <= nbCols; i++) {
						listMaybeId.add(rs.getString(i));
					}
				}
					
			}
			//Deduplication
			
			LinkedHashSet<String> Time1 = new LinkedHashSet<>(listMaybeId);
			listMaybeSet = new ArrayList<>(Time1);

		}catch (SQLException e) {
			e.printStackTrace();
		}
		
		return listMaybeSet;
		
	}
	/**Function that finds and displays all exactly realizable sets based on user's collection
	 * type of mypart < 377*/
	public List<String> showRealizableSets2() 	{		
		List<String> listMaybeSet = surSetList();
		List<String> listRealizableSets = new ArrayList<>();
		double rowCurrentProgression = 1.0;

		double rowTotalSets = (double)listMaybeSet.size();
		for(int i=0;i <listMaybeSet.size();i++) {
			double theProgression = rowCurrentProgression/rowTotalSets;
			System.out.printf("\nLoading %.7f", theProgression*100);
			if (isSetRealizable(listMaybeSet.get(i))) {
				listRealizableSets.add(listMaybeSet.get(i));				
			}
			rowCurrentProgression++;
		}
		System.out.println("");
		System.out.println("END of list\n");
		return listRealizableSets;
	}
	/**Function that finds and displays all exactly realizable sets based on user's collection
	 * type of mypart < 377*/
	public List<String> showRealizableSets() 	{
		String sql = null;
		ResultSet rs = null;
		List<String> listRealizableSets = new ArrayList<>();
		
		double rowCurrentProgression = 1.0;
		double rowTotalSets = 15660.0; //or sql = "SELECT COUNT(*) FROM sets"; rs = od.search(sql, null); rs.next(); Integer.parseInt(rs.getString(1));
		
		try {
			sql = "SELECT set_num FROM sets";
			rs = od.search(sql, null);
			while (rs.next()) { 
				double theProgression = rowCurrentProgression/rowTotalSets;
				System.out.printf("\nLoading %.7f", theProgression*100);
				String set_num = rs.getString("set_num");
				if (isSetRealizable(set_num)) {
					listRealizableSets.add(set_num);
				}
				rowCurrentProgression++;
			}
			rs.close();
			return listRealizableSets;
		} 	catch (SQLException e) {
			e.printStackTrace();
			return listRealizableSets;
		}
	}
	/**Function that gets a set name by its set_num. Doing this, instead of sql inner joining tables to obtain the extra column of set name,
	 * can speed up program in certain cases*/
	public String getSetName(String set_num) {
		String sql = null;
		String[] strArr = null;
		ResultSet rs = null;
		String result = null;
		
		try {
			sql = "SELECT name FROM sets WHERE set_num = ? ";
			strArr = new String[] {set_num}; 
			rs = od.search(sql, strArr);
			if(rs.next()) result = rs.getString("name");
			rs.close();
			return result;
		} catch (SQLException e) {
			e.printStackTrace();
			return result;
		} 
	}
	
	/**Function that calculates and displays the percentage of completion of a set from user's collection.
	 * Note : Similar to canDoSet but dividing the two double variables (nbRowsPartsSatisfied/nbRowsPartsNeed) instead of only comparing two int variables*/
	public double showMissingPercentageSet(String set_num) {
		String sql = null;
		double nbRowsPartsNeed = 0.0;
		double nbRowsPartsSatisfied = 0.0;
		double percent = 0.0;
		
		findPartsOfSet(set_num,"1", "PartsNeed"); 
		nbRowsPartsNeed = countRowsPartsNeed();   	//Implicit conversion from int to double without typecasting in Java
		if(nbRowsPartsNeed == 0.0) {
			//Optional : You can warn user : System.out.printf("Invalid set_num %s. Set doesn't exist in database or Set is not ultimately made up of parts\n", set_num.trim());
		}
		else {
			//Optional : You can get set name : String set_name = getSetName(set_num); 		
			nbRowsPartsSatisfied = countRowsPartsSatisfied();
			percent = nbRowsPartsSatisfied / nbRowsPartsNeed;
		}
		sql = "delete from PartsNeed";
		od.update(sql, null);
		//Optional : You can print : System.out.printf("%.2f %s for Set %s : %s\n", percent*100, "%", set_num.trim(), set_name);
		return percent*100;
	}
	
	/**Function that calculates the percentage of completion of all sets based on user's collection.
	 * mypart < 370*/
	public List<String> showMissingPercentageAllSets2() {


		List<String> listMaybeSet = surSetList();
		List<String> listMissingPercentageAllSets = new ArrayList<>();
		
		double rowCurrentProgression = 1.0;
		double rowTotalSets = (double)listMaybeSet.size();
		System.out.println(listMaybeSet.size());
		
		for(int i=0;i <listMaybeSet.size();i++) {
			double theProgression = rowCurrentProgression/rowTotalSets;
			System.out.printf("\nLoading %.7f", theProgression*100);
			double percentage = showMissingPercentageSet(listMaybeSet.get(i));
			listMissingPercentageAllSets.add(listMaybeSet.get(i));
			listMissingPercentageAllSets.add(String.valueOf(percentage));
			rowCurrentProgression++;
		}
		return listMissingPercentageAllSets;	

	}
	/**Function that calculates the percentage of completion of all sets based on user's collection.
	 * mypart > 370*/
	public List<String> showMissingPercentageAllSets() {
		String sql = null;
		ResultSet rs = null;
		String set_num  = null;
		List<String> listMissingPercentageAllSets = new ArrayList<>();
		
		double rowCurrentProgression = 1.0;
		double rowTotalSets = 15660.0; //or sql = "SELECT COUNT(*) FROM sets"; rs = od.search(sql, null); rs.next(); Integer.parseInt(rs.getString(1));
		
		try {
			sql = "SELECT set_num FROM sets";
			rs = od.search(sql, null);
			while (rs.next()) {		
				double theProgression = rowCurrentProgression/rowTotalSets;
				System.out.printf("\nLoading %.7f", theProgression*100);
				set_num = rs.getString("set_num");
				double percentage = showMissingPercentageSet(set_num);
				listMissingPercentageAllSets.add(set_num);
				listMissingPercentageAllSets.add(String.valueOf(percentage));
				rowCurrentProgression++;
			}
			rs.close();
			return listMissingPercentageAllSets;
		} 	catch (SQLException e) {
			e.printStackTrace();
			return listMissingPercentageAllSets;
		}
	}
	/**Function that finds and displays the missing parts to do a set based on user's collection.*/
	public List<String> showMissingParts(String set_num) {
		ResultSet rs = null;
		String sql = null;
		String[] strArr = null;
		List<String> listMissingParts = new ArrayList<>();
		
		int nbRowsPartsNeed = 0;
		int nbRowsPartsSatisfied = 0;
		
		try {
			findPartsOfSet(set_num,"1", "PartsNeed"); 
			nbRowsPartsNeed = countRowsPartsNeed();
			if(nbRowsPartsNeed == 0) {
				System.out.printf("Invalid set_num %s. Set doesn't exist in database or Set is not ultimately made up of parts\n", set_num.trim());
				return listMissingParts; 
			}
			nbRowsPartsSatisfied = countRowsPartsSatisfied();
			if(nbRowsPartsSatisfied == nbRowsPartsNeed) {
				System.out.printf("You can do set %s. You are not missing any parts\n", set_num.trim());
				sql = "delete from PartsNeed";
				od.update(sql, null);
				return listMissingParts;
			}
			System.out.println("\nBelow are the parts and its quantity that you need:\n");	
			//Parts that you have but not enough
			System.out.println("Parts that you have but not enough:");
			sql = "SELECT PN.part_num, PN.color, PN.quantity - MP.quantity AS quantity, P.name  "
				+ "FROM PartsNeed as PN "
				+ "INNER JOIN parts AS P "
				+ "ON PN.part_num = P.part_num "
				+ "INNER JOIN myparts AS MP "
				+ "ON PN.part_num = MP.part_num "
				+ "AND PN.color = MP.color "
				+ "WHERE PN.quantity > MP.quantity ";
			rs = od.search(sql, null);
			while(rs.next()) {
				String quantity_missing = rs.getString("quantity");
				String part_num = rs.getString("part_num");
				String color = rs.getString("color");
				String name = rs.getString("name");
				System.out.printf("%s x Part of part_num %s and color code %s : %s\n", quantity_missing, part_num.trim(), color, name);
				
				listMissingParts.add(part_num);
				listMissingParts.add(color);
//				listMissingParts.add(name);
				listMissingParts.add(quantity_missing);
			}	
			rs.close();
			
			//Parts that you don't have at all
			System.out.println("\nParts that you don't have at all:");
			sql = "SELECT PN.part_num, PN.color, PN.quantity, P.name "
				+ "FROM PartsNeed AS PN "
				+ "INNER JOIN parts AS P "
				+ "ON PN.part_num = P.part_num ";
			rs = od.search(sql, null);
			while(rs.next()) {
				String part_num_need = rs.getString("part_num");
				String color_need = rs.getString("color");
				String quantity_need = rs.getString("quantity");
				String name = rs.getString("name");
				
				sql = "SELECT *  "
					+ "FROM myparts "
					+ "WHERE part_num = ? "
					+ "AND color = ? ";
				strArr = new String[] {part_num_need, color_need}; 
				ResultSet rs2 = od.search(sql, strArr);
				
				if(!rs2.next()) {
					System.out.printf("%s x Part of part_num %s and color code %s : %s\n", quantity_need, part_num_need.trim(), color_need,name);
					listMissingParts.add(part_num_need);
					listMissingParts.add(color_need);
//					listMissingParts.add(name);
					listMissingParts.add(quantity_need);
				}	
				rs2.close();
			}
			rs.close();
			return listMissingParts;
		} catch (SQLException e) {
			e.printStackTrace();
			return listMissingParts;
		} finally {
			sql = "delete from PartsNeed";
			od.update(sql, null);
		} 
	}
}
