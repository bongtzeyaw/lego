package admin;

import java.sql.Connection;

import database.EstablishConnection;
import database.OperationDatabase;

/** A class for administrator that creates a database, creates tables in it and insert rows to these tables 
 * 	Should only be run one single time
 **/
public class MainAdmin {
	public static void main(String[] args) {
		EstablishConnection.loadDriver();
		Connection con = EstablishConnection.connectDatabase(";create = true");    //Note: If database is already created and admin wishes to modify it, you only need to erase the argument
		OperationDatabase od = new OperationDatabase(con);		
		String sqlCreate = null;
		String sqlInsert = null;
		String csvFile = null;
		System.out.print("Database created \nConnection established \n");
		
		/*
		//Table sets
		sqlCreate = "CREATE TABLE sets (set_num char(100) primary key, name char(100), years int,theme_id int ,num_parts int)";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/sets.csv";
		sqlInsert = "INSERT INTO sets (set_num,name,years,theme_id,num_parts) values(?,?,?,?,?)";
		InputCSV.insertRows(od, csvFile, sqlInsert);
		System.out.print("Table sets created \n");
		
		//Table parts 
		sqlCreate = "CREATE TABLE parts (part_num char(100) ,name char(250), part_cat_id int, part_material_id int)";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/parts.csv";
		sqlInsert="INSERT INTO parts (part_num,name,part_cat_id,part_material_id) values(?,?,?,?)";
        InputCSV.insertRows(od, csvFile, sqlInsert);
        System.out.print("Table parts created \n");
        
		//Table inventories
		sqlCreate = "CREATE TABLE inventories (id int primary key, version int, set_num char(100))";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/inventories.csv";
		sqlInsert = "INSERT INTO inventories (id,version,set_num) values(?,?,?)";
		InputCSV.insertRows(od, csvFile, sqlInsert);
		System.out.print("Table inventories created \n");
				
		//Table inventory_sets
		sqlCreate = "CREATE TABLE inventory_sets (inventory_id int, set_num char(100), quantity int)";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/inventory_sets.csv";
		sqlInsert = "INSERT INTO inventory_sets (inventory_id,set_num,quantity) values(?,?,?)";
		InputCSV.insertRows(od, csvFile, sqlInsert);
		System.out.print("Table inventory_sets created \n");
		
		//Table inventory_parts
		sqlCreate = "CREATE TABLE inventory_parts (inventory_id int, part_num char(100), color_id int,quantity int, is_spare char(100))";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/inventory_parts.csv";
		sqlInsert = "INSERT INTO inventory_parts (inventory_id,part_num,color_id,quantity,is_spare) values(?,?,?,?,?)";
		InputCSV.insertRows(od, csvFile, sqlInsert);
		System.out.print("Table inventory_parts created \n");
		
		//Table colors
		sqlCreate = "CREATE TABLE colors (id int primary key, name char(100), rgb char(100),is_trans char(100))";
		od.update(sqlCreate, null);
		csvFile = "data-YAW/csv/colors.csv";
		sqlInsert = "INSERT INTO colors (id,name,rgb,is_trans) values(?,?,?,?)";
		InputCSV.insertRows(od, csvFile, sqlInsert);
		System.out.print("Table colors created \n");
		
		//Auxiliary tables needed for program
		sqlCreate = "CREATE TABLE tempcomposantsets (set_num char(100), quantity int)"; //No need primary key because even if we a repeating set, parts will still be added accordingly
		od.update(sqlCreate, null);

		sqlCreate = "CREATE TABLE tempcomposantsetsbuffer (set_num char(100), quantity int)"; 
		od.update(sqlCreate, null);

		sqlCreate = "CREATE TABLE Mysets (set_num char(100) primary key, name char(100), years int,theme_id int ,num_parts int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE Myparts (part_num char(100) ,name char(250), part_cat_id int, part_material_id int,color int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE MypartsT (part_num char(100) ,name char(250), part_cat_id int, part_material_id int,color int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE PartsNeed (part_num char(100) ,color int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE PartsNotHave (part_num char(100) ,color int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE SetMaybeDo (set_num char(100),percent double)";
		od.update(sqlCreate, null);
				
		//Modify auxiliary tables to speed up program: Change myparts and mypartsT to 3 columns  
		sqlCreate = "DROP TABLE myparts ";
		od.update(sqlCreate, null);
		
		sqlCreate = "DROP TABLE mypartsT ";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE myparts (part_num char(100),color int,quantity int)";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE mypartsT (part_num char(100) ,color int,quantity int)";
		od.update(sqlCreate, null);
		
		//Auxiliary table to know originating set of parts 
		sqlCreate = "CREATE TABLE mypartsFromSet (part_num char(100),color int,quantity int, set_num char(100), quantity_set int)";
		od.update(sqlCreate, null);
		
		//Auxiliary table to speed up program: PartsNeedAllSets 
		sqlCreate = "CREATE TABLE PartsNeedAllSets (part_num char(100) ,color int,quantity int, set_num char(100))";
		od.update(sqlCreate, null);
		
		sqlCreate = "CREATE TABLE PartsHaveAllSets (part_num char(100) ,color int,quantity int, set_num char(100))";
		od.update(sqlCreate, null);
		*/	
		
		String[] strArr = null;
		
		//Clean dirty tables that might result from previous run being stopped violently
		sqlCreate = "DELETE FROM tempcomposantsets";
		od.update(sqlCreate, null);
		
		sqlCreate = "DELETE FROM tempcomposantsetsbuffer";
		od.update(sqlCreate, null);
		
		//Clean interrupted rows
		sqlCreate = "DELETE FROM PartsNeedAllSets WHERE set_num = ? ";
		strArr = new String[] {"K1383-1"}; 
		od.update(sqlCreate, strArr);
		
		sqlCreate = "DELETE FROM PartsHaveAllSets WHERE set_num = ? ";
		od.update(sqlCreate, strArr);
		
		//Continue completing table
		FindPartsOfAllSets.completeTablePartsNeedAllSets(od);
						
		//Closes the connection before exiting
		EstablishConnection.closeConnection(con);
	}
}
