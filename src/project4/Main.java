package project4;

import java.sql.Connection;

import java.util.Scanner;

import database.EstablishConnection;
import database.OperationDatabase;

public class Main {
	public static void main(String[] args) {
		//Establishes connection to database
		EstablishConnection.loadDriver();
		Connection con = EstablishConnection.connectDatabase();
		OperationDatabase od = new OperationDatabase(con);
		
		//Clean dirty tables that might result from previous run being stopped violently	
		String sql = null;
		sql = "DELETE FROM PartsNeed";
		od.update(sql, null);
		
		sql = "DELETE FROM tempcomposantsets";
		od.update(sql, null);
		
		sql = "DELETE FROM tempcomposantsetsbuffer";
		od.update(sql, null);	
					
		//Interface
		MyFunction myFunction = new MyFunction(od);
		Scanner sc = new Scanner(System.in);
		String selectionKey = "initial_random_value";
		
		while(!selectionKey.equals("Q")) {
			System.out.println("\n~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n \n "
					+ "Please enter your choice: \n   "
					+ "Show my collection(CS)\n   "
					+ "Add set(AS) 	\n   "
					+ "Add part(AP) \n   "
					+ "Delete set(DS) \n   "
					+ "Delete part(DP) \n   "
					+ "Find out if you can do a set(JSDN) \n   "
					+ "Find all the sets you can do(FSCD) \n   "
					+ "Find out the parts you are missing to do a set(LS) \n   "
					+ "Obtain percentage of completion of a set(SFPM) \n   "
					+ "Obtain percentage of completion of ALL sets(SFPMALL) \n   "
					+ "QUIT(Q) \n ");
			selectionKey = sc.nextLine();
			if (selectionKey.equals("AS")) {
				//Add set
				System.out.println("Please Enter set_num:");
				String set_num = sc.nextLine();
				System.out.println("Please Enter quantity to add:");
				String quantity = sc.nextLine();
				myFunction.addMySets(set_num, quantity);
			}
			else if(selectionKey.equals("AP")) {
				//Add part
				System.out.println("Please Enter part_num:");
				String part_num = sc.nextLine();
				System.out.println("Please Enter color:");
				String color = sc.nextLine();
				System.out.println("Please Enter quantity to add:");
				String quantity = sc.nextLine();
				myFunction.addMyParts(part_num, color, quantity);
			}
			else if(selectionKey.equals("DS")){
				//Delete set
				System.out.println("You want to delete all of your sets(ALL) or just one(ONE)?");
				String times = sc.nextLine();
				
				if(times.equals("ALL")) {
					myFunction.deleteAllSets();
				}
				else if (times.equals("ONE")) {
					System.out.println("Please Enter the set_num which you want delete");
					String set_num = sc.nextLine();
					System.out.println("Please Enter quantity to delete");
					String quantity = sc.nextLine();
					myFunction.deleteMySets(set_num, quantity);
				}
				else {
					System.out.println("Invalid choice. Please re-enter your choice\n");
				}
			}
			else if(selectionKey.equals("DP")){
				//Delete part
				System.out.println("You want to delete all of your parts(ALL) or just one(ONE)?");
				String times = sc.nextLine();
				
				if(times.equals("ALL")) {
					myFunction.deleteAllParts();
				}
				else if (times.equals("ONE")) {
					System.out.println("Please Enter the part_num which you want delete");
					String part_num = sc.nextLine();
					System.out.println("Please Enter the color which you want delete");
					String color = sc.nextLine();
					System.out.println("Please Enter quantity to delete");
					String quantity = sc.nextLine();
					myFunction.deleteMyParts(part_num, color, quantity);
				}
				else {
					System.out.println("Invalid choice. Please re-enter your choice\n");
				}	
				
			}
			else if(selectionKey.equals("JSDN")) {
				//Find out if you can do a set
				System.out.println("Please Enter which ~~~set_num~~~~ you want to find it can do or not:");
				String set_num = sc.nextLine();
				myFunction.canDoSet(set_num);
			}
			else if(selectionKey.equals("CS")) {
				//Show my collection
				myFunction.showMyCollection();
			}
			else if(selectionKey.equals("FSCD")) {
				//Find all the sets you can do			
				myFunction.findRealizableSets();
			}
			else if(selectionKey.equals("SFPM")) {
				//Obtain percentage of completion of a set
				System.out.println("Please Enter the set_num which you want find");
				String set_num = sc.nextLine();
				myFunction.findMissingPercentageSet(set_num);
			}
			else if(selectionKey.equals("SFPMALL")) {
				//Obtain percentage of completion of ALL sets
				myFunction.findMissingPercentageAllSets();
			}
			else if(selectionKey.equals("LS")) {
				//Find out the parts you are missing to do a set
				System.out.println("Please Enter the set_num which you want to do");
				String set_num = sc.nextLine();
				myFunction.findMissingParts(set_num);
			}
			else if(selectionKey.equals("Q")) {
				System.out.println("Application terminated\n");
			}
			else {
				System.out.println("Invalid choice. Please re-enter your choice\n");
			}
			 //Make sure function does not return before this line. Otherwise, might not be called.
		}	
		sc.close();	
		
		//Closes the connection before exiting
		EstablishConnection.closeConnection(con);
	}
}

//TODO 
//progress bar for is realisable
//verify set names and part names are shown ; printing set_num or part_num : do trim()
//cut short deleteMyParts then transfer to delete fonction
//cut short show missing parts
//deletePartsOfSet : No need to use PartsHaveAllSets. Use mypartsFromSet

//TODO report : Summary problem solved
//add part : user can't just add any parts. if myparts and mypartsT become 6 columns how slower it gets?
//add part : need extra info : name material category?
