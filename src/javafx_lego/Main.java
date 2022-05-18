package javafx_lego;

import java.sql.Connection;
import java.util.Iterator;
import java.util.List;
import database.EstablishConnection;
import database.OperationDatabase;
import project4.*;
	
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;


public class Main extends Application {
	private TableView<Part> tableParts = new TableView<>();
    private ObservableList<Part> dataParts = FXCollections.observableArrayList(
//   	    new Part("1111", "1", "1"),
//   	    new Part("2222", "2", "1"),
//   	    new Part("3333", "3", "2")
   		);	
    private TableView<Set> tableSets = new TableView<>();
    private ObservableList<Set> dataSets = FXCollections.observableArrayList(
//   	    new Set("555", "2"),
//   	    new Set("666", "3"),
//   	    new Set("777", "4")
   		);	
    private TableView<Part> tableOperations = new TableView<>();
    private ObservableList<Part> dataOperations = FXCollections.observableArrayList(

   		);	
    private TableView<Set> tableOperationsALL = new TableView<>();
    private ObservableList<Set> dataOperationsALL = FXCollections.observableArrayList(

   		);	

	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) {
		try {
			
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
			AddFunction addFunction = new AddFunction(od);
			myFunction.deleteAllParts(); // clear database at the beginning
			

			// creating a tabpane and add 4 tabs
			TabPane tabPane = new TabPane(); 
 		    Tab tabParts = new Tab("My parts");
 		    Tab tabSets = new Tab("My Sets");
 		    Tab tabOperations = new Tab("Try a set");
 		    Tab tabOperationsALL = new Tab("Try all sets");
            Scene scene_parts = new Scene(tabPane,380,500);
	        
		    /* My parts Tab */
			
			  Label labelPart = new Label("My collection : Parts");
			  labelPart.setFont(new Font("Arial", 20));
			  Label msg_part = new Label(".");
		      //labelSet.setFont(new Font("Arial", 20));
			 
	        tableParts.setEditable(true);
	        
	        // creating tableParts colums and adding them
	        TableColumn<Part, String> part_num_col = new TableColumn<>("Part Number");
	        part_num_col.setMinWidth(140);
	        part_num_col.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("part_num")
	    		);
	      
	        TableColumn<Part, String> color_id_col = new TableColumn<>("Color ID");
	        color_id_col.setMinWidth(100);
	        color_id_col.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("color_id")
	    		);
	       
   	      
	        TableColumn<Part, String> quantity_col_part = new TableColumn<>("Quantity");
	        quantity_col_part.setMinWidth(100);
	        quantity_col_part.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("quantity")
	    		);
	        	      

	        tableParts.setItems(dataParts);
	        tableParts.getColumns().add(part_num_col);
	        tableParts.getColumns().add(color_id_col);
	        tableParts.getColumns().add(quantity_col_part);

	        
	        TextField addPart_num = new TextField();
	      addPart_num.setPromptText("Part Number");
	      addPart_num.setMaxWidth(part_num_col.getPrefWidth());
	       TextField addColor_id = new TextField();
	     addColor_id.setMaxWidth(color_id_col.getPrefWidth());
	    addColor_id.setPromptText("Color ID");
	       TextField addQuantity_part = new TextField();
	       addQuantity_part.setMaxWidth(quantity_col_part.getPrefWidth());
	       addQuantity_part.setPromptText("Quantity");
	       
	       // When launching an event "Add" button click
	       // 1. Verify that the informations given are adequate
	       // 2. verify that part exists in our database
	       // 3. add part to my collection and to tableParts
	       Button addPartButton = new Button("Add");
	      addPartButton.setOnAction(new EventHandler<ActionEvent>() {
	          @Override public void handle(ActionEvent e) {
	        	  if (!(isNumeric(addColor_id.getText().trim())) || !(isNumeric(addQuantity_part.getText().trim())) || addColor_id.getText().equals("") || addQuantity_part.getText().equals("") || addPart_num.getText().equals("")) {
						 msg_part.setText("Delete failure : select positif quantity/color");
		        	}
		          else {
	        	  if(!addFunction.verifyPartExists(addPart_num.getText().trim(), addColor_id.getText().trim())) {
					 msg_part.setText("Add failure : part inexistant in database");
	        	  }
	        	  else {
				  myFunction.addMyParts(addPart_num.getText().trim(),
		                  addColor_id.getText().trim(),
		                  addQuantity_part.getText().trim()
		                  ); 
				  String extractedQuantity = extractQuantityParts(addPart_num.getText().trim(), addColor_id.getText().trim());
			      int extractedIndex = extractIndexParts(addPart_num.getText().trim(), addColor_id.getText().trim());
				  String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity.trim()) + Integer.parseInt(addQuantity_part.getText().trim()));
					 
				  if (extractedIndex<0) {
	              dataParts.add(new Part(
	                  addPart_num.getText().trim(),
	                  addColor_id.getText().trim(),
	                  addQuantity_part.getText().trim()
	              ));
				  }
				  else {
						 dataParts.remove(extractedIndex); 
						 dataParts.add(new Part(
				                addPart_num.getText().trim(),
				                addColor_id.getText().trim(),
				                differenceQuantity.trim()
				            ));        
					}
			     msg_part.setText("Part added successfully !");
	             addPart_num.clear();
	            addColor_id.clear();
	            addQuantity_part.clear();
	          }
	          }
	          }
	      });
	     String bstyle=String.format("-fx-background-color: %s;","#03CA45");
	    addPartButton.setStyle(bstyle);
	    
//		   Label msg_deleteInfo = new Label(".");
//	        //labelSet.setFont(new Font("Arial", 20));
//		      
//		 	  Button deleteInfoButton = new Button("Ok");
//		 	 deleteInfoButton.setOnAction(new EventHandler<ActionEvent>() {
//				        @Override public void handle(ActionEvent e) {
//				        }
//				    });
		 	 
	    TextField deleteInfo = new TextField();
		   deleteInfo.setPromptText("Info");
		   deleteInfo.setMaxWidth(120);

		// When launching an event "Delete" button click
	       // 1. Verify that the informations given are adequate
	       // 2. verify that part exists in our database
	       // 3. delete part from my collection and from tableParts
	    Button deletePartButton = new Button("Delete");
	    deletePartButton.setOnAction(new EventHandler<ActionEvent>() {
		        @Override public void handle(ActionEvent e) {
		        	if (!(isNumeric(addColor_id.getText().trim())) || !(isNumeric(addQuantity_part.getText().trim())) || addColor_id.getText().equals("") || addQuantity_part.getText().equals("") || addPart_num.getText().equals("")) {
						 msg_part.setText("Delete failure : select positif quantity/color");
		        	}
		        	else if(!addFunction.verifyPartExists(addPart_num.getText().trim(), addColor_id.getText().trim())) {
						 msg_part.setText("Delete failure : part inexistant in database");
		        	}
		        	else {
					int verifyCase = myFunction.deleteMyParts(addPart_num.getText().trim(), addColor_id.getText().trim(), addQuantity_part.getText().trim());
					if(verifyCase==0) {
						 msg_part.setText("Delete failure : Quantity inexistant in collection");
											}
					else if(verifyCase==1) {
						 msg_part.setText("Delete failure : Part inexistant in collection");
											}
					else if(verifyCase==10) {
						
						 String extractedQuantity = extractQuantityParts(addPart_num.getText(), addColor_id.getText().trim());
						 int extractedIndex = extractIndexParts(addPart_num.getText(), addColor_id.getText().trim());
						 String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity) - Integer.parseInt(addQuantity_part.getText()));
				         dataParts.remove(extractedIndex); 
						 if(Integer.parseInt(differenceQuantity.trim())>0) {
				            dataParts.add(new Part(
				                addPart_num.getText().trim(),
				                addColor_id.getText().trim(),
				                differenceQuantity.trim()
				            )); 
						 }
				         msg_part.setText("Parts deleted successfully !");

				         
					}
					else if(verifyCase==2) {
			         msg_part.setText("Select set_num you want to destroy or STOP if you want to cancel deleting");
						if (deleteInfo.getText().equals("STOP")){msg_part.setText("Delete Cancelled");}
						
						else if(!addFunction.verifySetExists(deleteInfo.getText())) {
					         msg_part.setText("Select a valid set_num you want to destroy (or STOP to cancel)");
			        	  }
						else { 
						String set_num_destroy = deleteInfo.getText().trim();
						myFunction.deleteMyPartsAux1(addPart_num.getText().trim(), addColor_id.getText().trim(), addQuantity_part.getText().trim(), set_num_destroy.trim());
						 String extractedQuantity = extractQuantityParts(addPart_num.getText().trim(), addColor_id.getText().trim());
						 int extractedIndex = extractIndexParts(addPart_num.getText().trim(), addColor_id.getText().trim());
						 String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity.trim()) - Integer.parseInt(addQuantity_part.getText().trim()));
				         dataParts.remove(extractedIndex); 
						 if(Integer.parseInt(differenceQuantity.trim())>0) {
				            dataParts.add(new Part(
				                addPart_num.getText().trim(),
				                addColor_id.getText().trim(),
				                differenceQuantity.trim()
				            )); 
						 }
						 int extractedIndexSet = extractIndexSets(set_num_destroy);
						 String extractedQuantitySet = extractQuantitySets(set_num_destroy);
						 String differenceQuantitySet = String.valueOf(Integer.parseInt(extractedQuantitySet.trim()) - 1);
						 dataSets.remove(extractedIndexSet); 
						 if(Integer.parseInt(differenceQuantitySet.trim())>0) {
				            dataSets.add(new Set(
				            		set_num_destroy,
				                differenceQuantitySet.trim()
				            )); 
						 }
				         msg_part.setText("Parts deleted successfully !");				
						}
			
					}
					else if(verifyCase==3) {

						msg_part.setText("Select YES in Info if you want to continue (or ignore to cancel)");
							if(deleteInfo.getText().equals("YES")) { 
								System.out.println(deleteInfo.getText().trim());
//						        deleteInfo.clear();
								myFunction.deleteMyPartsAux2(addPart_num.getText().trim(), addColor_id.getText().trim(), addQuantity_part.getText().trim()); // TODO error here
								int extractedIndex3 = extractIndexParts(addPart_num.getText().trim(), addColor_id.getText().trim()); 
								dataParts.remove(extractedIndex3); 
								msg_part.setText("Success deleting all quantity you have of this part");
							}
																
					}
					
					
					
//					 String extractedQuantity = extractQuantityParts(addPart_num.getText());
//					 int extractedIndex = extractIndexParts(addPart_num.getText());
//					 String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity) - Integer.parseInt(addQuantity_part.getText()));
//					 if (Integer.parseInt(extractedQuantity)<0) {
//						 msg_part.setText("Delete failure : Part inexistant in collection");
//						 }
//					 else if (Integer.parseInt(differenceQuantity)<0){
//						 msg_part.setText("Delete failure : Quantity inexistant in collection");
//						 }
//					 else   {dataParts.remove(extractedIndex); 
//						 if(Integer.parseInt(differenceQuantity)>0) {
//				            dataParts.add(new Part(
//				                addPart_num.getText(),
//				                addColor_id.getText(),
//				                differenceQuantity
//				            )); 
//						 }
//					         msg_part.setText("Part deleted successfully !");
//					         addPart_num.clear();
//					         addColor_id.clear();
//					         addQuantity_part.clear(); 
//					         
//					 }
		        	}
					 
					 				 
		        }
		    });
		   String bstyle10=String.format("-fx-background-color: %s;","#E82106");
		   deletePartButton.setStyle(bstyle10);
	    
	    


		  
		   
	    
		// When launching an event "Add" button click
	    // Delete all parts (and so all sets) from my collection and from tableSets and tableParts
	    	    
	   Button deleteAllPartsButton = new Button("Delete All Parts and Sets");
	   deleteAllPartsButton.setOnAction(new EventHandler<ActionEvent>() {
	          @Override public void handle(ActionEvent e) {
				  myFunction.deleteAllParts();
	        	  tableParts.getItems().clear();
	        	  tableSets.getItems().clear();
		 	      msg_part.setText("All parts (and by consequent all sets) deleted successfully !");

	          }
	      });
	   
	   
	    String bstyle2=String.format("-fx-background-color: %s;","#E82106");
	    deleteAllPartsButton.setStyle(bstyle2);
	    
//	    HBox hbMsg_part = new HBox();	    
//	    hbMsg_part.getChildren().addAll(deleteAllPartsButton, msg_part);
//	    hbMsg_part.setSpacing(5);
	    
	    HBox hbAdd_part = new HBox();	    
	    hbAdd_part.getChildren().addAll(addPart_num, addColor_id, addQuantity_part, addPartButton, deletePartButton);
	    hbAdd_part.setSpacing(3);
	     
	    /* My Sets Tab */
        
	        Label labelSet = new Label("My collection : Sets");
	        labelSet.setFont(new Font("Arial", 20));
	        Label msg_set = new Label(".");
	        //labelSet.setFont(new Font("Arial", 20));
			 
	      tableSets.setEditable(true);
	      TableColumn<Set, String> set_num_col = new TableColumn<>("Set Number");
	      set_num_col.setMinWidth(120);
	      set_num_col.setCellValueFactory(
	  		    new PropertyValueFactory<Set,String>("set_num")
	  		);
	      	    
	      		      
	      TableColumn<Set, String> quantity_col_set = new TableColumn<>("Quantity");
	      quantity_col_set.setMinWidth(200);
	      quantity_col_set.setCellValueFactory(
	  		    new PropertyValueFactory<Set,String>("quantity")
	  		);
	      
	
	      tableSets.setItems(dataSets);
	      tableSets.getColumns().add(set_num_col);
	      tableSets.getColumns().add(quantity_col_set);

	      
	      TextField addSet_num = new TextField();
	      addSet_num.setPromptText("Set Number");
	      addSet_num.setMaxWidth(set_num_col.getPrefWidth());
	      TextField addQuantity_set = new TextField();
	      addQuantity_set.setMaxWidth(quantity_col_set.getPrefWidth());
	      addQuantity_set.setPromptText("Quantity");
	      
	   // When launching an event "Add" button click
	       // 1. Verify that the informations given are adequate
	       // 2. verify that set exists in our database
	       // 3. add set to my collection and to tableSets
	     Button addSetButton = new Button("Add");
	     addSetButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override public void handle(ActionEvent e) {
				msg_set.setText("adding.... ");
	        	if (!(isNumeric(addQuantity_set.getText().trim())) || addQuantity_set.getText().equals("") || addSet_num.getText().equals("")) {
					 msg_set.setText("Add failure : select a positif integer quantity");
	        	}
	        	else {
					System.out.println("" + addFunction.verifySetExists(addSet_num.getText().trim()));	        		
					if(!addFunction.verifySetExists(addSet_num.getText().trim())) {
					 msg_set.setText("Add failure : Set inexistant in database");
	        	  }
	        	else{
				myFunction.addMySets(
						addSet_num.getText().trim(),
		                addQuantity_set.getText().trim()
		                );
				 String extractedQuantity = extractQuantitySets(addSet_num.getText().trim());
				 int extractedIndex = extractIndexSets(addSet_num.getText().trim());
				 String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity.trim()) + Integer.parseInt(addQuantity_set.getText().trim()));
				 
				if (extractedIndex<0) {
					dataSets.add(new Set(
			                addSet_num.getText().trim(),
			                addQuantity_set.getText().trim()
			            ));					
					}
				else {
					 dataSets.remove(extractedIndex); 
					 dataSets.add(new Set(
			                addSet_num.getText().trim(),
			                differenceQuantity.trim()
			            ));        
				}
        	List<String> myList = addFunction.findPartsConstitutingASet(addSet_num.getText().trim(), addQuantity_set.getText().trim());
        	for(int i = 0; i < myList.size(); i=i+3) {

            	Part myPart = new Part((myList.get(i)).trim(), (myList.get(i+1)).trim(), (myList.get(i+2)).trim());
            	
        		 String extractedQuantity2 = extractQuantityParts((myList.get(i)).trim(), (myList.get(i+1)).trim());
			      int extractedIndex2 = extractIndexParts((myList.get(i)).trim(), (myList.get(i+1)).trim());
				  String differenceQuantity2 = String.valueOf(Integer.parseInt(extractedQuantity2.trim()) + Integer.parseInt(myList.get(i+2).trim()));
					 
				  if (extractedIndex2<0) {
				        dataParts.add(myPart);	
				  }
				  else {
						 dataParts.remove(extractedIndex2); 
						 dataParts.add(new Part(
								 (myList.get(i)).trim(),
								 (myList.get(i+1)).trim(),
				                differenceQuantity2.trim()
				            ));        
					}
        	
        	}
	         msg_set.setText("Set added successfully !");
	         addSet_num.clear();
	         addQuantity_set.clear();
	        }
	        }
	        }
	    });
	   String bstyle3=String.format("-fx-background-color: %s;","#03CA45");
	   addSetButton.setStyle(bstyle3);
	   
	// When launching an event "Delete" button click
       // 1. Verify that the informations given are adequate
       // 2. verify that Set exists in our database
       // 3. delete Set from my collection and from tableSets	   
	   Button deleteSetButton = new Button("Delete");
	   deleteSetButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override public void handle(ActionEvent e) {
				msg_set.setText("deleting.... ");
	        	if (!(isNumeric(addQuantity_set.getText().trim())) || addQuantity_set.getText().equals("") || addSet_num.getText().equals("")) {
					 msg_set.setText("Delete failure : select a positif integer quantity");
	        	}
	        	else if(!addFunction.verifySetExists(addSet_num.getText().trim())) {
	        		 msg_set.setText("Delete failure : Set inexistant in database");
	        	  }
	        	else {
				 String extractedQuantity = extractQuantitySets(addSet_num.getText().trim());
				 int extractedIndex = extractIndexSets(addSet_num.getText().trim());
				 String differenceQuantity = String.valueOf(Integer.parseInt(extractedQuantity.trim()) - Integer.parseInt(addQuantity_set.getText().trim()));
				 if (Integer.parseInt(extractedQuantity.trim())<0) {
					 msg_set.setText("Delete failure : Set inexistant in collection");
					 }
				 else if (Integer.parseInt(differenceQuantity.trim())<0){
					 msg_set.setText("Delete failure : Quantity inexistant in collection");
					 }
				 else   {
						myFunction.deleteMySets(addSet_num.getText().trim(), addQuantity_set.getText().trim());
					 
						dataSets.remove(extractedIndex); 
					 if(Integer.parseInt(differenceQuantity.trim())>0) {
			            dataSets.add(new Set(
			                addSet_num.getText().trim(),
			                differenceQuantity.trim()
			            )); 
					 }

		 List<String> myList = addFunction.findPartsConstitutingASet(addSet_num.getText().trim(), addQuantity_set.getText().trim());
        	for(int i = 0; i < myList.size(); i=i+3) {
        		 String extractedQuantity2 = extractQuantityParts(myList.get(i).trim(), myList.get(i+1).trim());
			      int extractedIndex2 = extractIndexParts(myList.get(i).trim(), myList.get(i+1).trim());
				  String differenceQuantity2 = String.valueOf(Integer.parseInt(extractedQuantity2.trim()) - Integer.parseInt(myList.get(i+2).trim()));
				 dataParts.remove(extractedIndex2); 
				 if(Integer.parseInt(differenceQuantity2.trim())>0) {
		            dataParts.add(new Part(
		            		myList.get(i).trim(),
							 myList.get(i+1).trim(),
			                differenceQuantity2.trim()
		            )); 
				 }
        	
    	}
									 
				         msg_set.setText("Set deleted successfully !");
				         addSet_num.clear();
				         addQuantity_set.clear();
				 }
	        	}
				 
				 				 
	        }
	    });
	   String bstyle5=String.format("-fx-background-color: %s;","#E82106");
	   deleteSetButton.setStyle(bstyle5);
	  
	  
	  
	// When launching an event "Delete All Sets" button click
       // 3. delete all sets from my collection and from tableSets
	 Button deleteAllSetsButton = new Button("Delete All Sets");
	 deleteAllSetsButton.setOnAction(new EventHandler<ActionEvent>() {
	        @Override public void handle(ActionEvent e) {
				  myFunction.deleteAllSets();
				  
			for (Iterator<Set> ind = dataSets.iterator(); ind.hasNext();) {
				    Set unSet = ind.next();
				    List<String> myList = addFunction.findPartsConstitutingASet(unSet.getSet_num().trim(), unSet.getQuantity().trim());
				  	for(int i = 0; i < myList.size(); i=i+3) {
				  		 String extractedQuantity2 = extractQuantityParts(myList.get(i).trim(),myList.get(i+1).trim());
						      int extractedIndex2 = extractIndexParts(myList.get(i).trim(), myList.get(i+1).trim());
							  String differenceQuantity2 = String.valueOf(Integer.parseInt(extractedQuantity2.trim()) - Integer.parseInt(myList.get(i+2).trim()));
							 dataParts.remove(extractedIndex2); 
							 if(Integer.parseInt(differenceQuantity2.trim())>0) {
					            dataParts.add(new Part(
					            		myList.get(i).trim(),
										 myList.get(i+1).trim(),
						                differenceQuantity2.trim()
					            )); 
							 }
				  	
					}
				    
			}
	  
	        	  tableSets.getItems().clear();
	 	         msg_set.setText("All sets deleted successfully !");

	        }
	    });
	  String bstyle4=String.format("-fx-background-color: %s;","#E82106");
	  deleteAllSetsButton.setStyle(bstyle4);
	  
//	  HBox hbMsg_set = new HBox();	    
//	  hbMsg_set.getChildren().addAll(deleteAllSetsButton, msg_set);
//	  hbMsg_set.setSpacing(5);
	  
	  
	  HBox hbAdd_set = new HBox();	    
	  hbAdd_set.getChildren().addAll(addSet_num, addQuantity_set, addSetButton, deleteSetButton);
	  hbAdd_set.setSpacing(3);
			    			
	  
	  /* Operations Tab */
	  Label labelOperation = new Label("Operations on a particular set :");
	  labelOperation.setFont(new Font("Arial", 20));
      Label msg_verify = new Label(".");
      
      TextField VerifySet_num = new TextField();
      VerifySet_num.setPromptText("Set Number");
      VerifySet_num.setMaxWidth(120);
      
 	  Button testCanDoSet = new Button("Can I do ?");
 	  testCanDoSet.setOnAction(new EventHandler<ActionEvent>() {
		        @Override public void handle(ActionEvent e) {
					 if(myFunction.canDoSet(VerifySet_num.getText())) {
						 msg_verify.setText("Yes you can do this set");
					 }
					 else {
						 msg_verify.setText("NO you can't do this set");
					 }
//					 VerifySet_num.clear();
		        }
		    });

      // Calculation of percentage realisation for a certain set
 	 Button testPurcentageSet = new Button("Percentage");
 	testPurcentageSet.setOnAction(new EventHandler<ActionEvent>() {
		        @Override public void handle(ActionEvent e) {
					msg_verify.setText("Calculation... (please be sure that this Set exists in database)");
		        	if(!addFunction.verifySetExists(VerifySet_num.getText())) {
						 msg_part.setText("Calculation failure : Set inexistant in database");
		        	  }	
		        	else {
		        	 Double p = myFunction.findMissingPercentageSet(VerifySet_num.getText());
					 msg_verify.setText("Realisation percentage (%): " + p);
		        	}
//					 VerifySet_num.clear();
		        }
		    });
	  
    // Show parts missing to do a certain set, in tableOperations
	  Button testPartsMissing = new Button("Parts Missing");
	  testPartsMissing.setOnAction(new EventHandler<ActionEvent>() {
		        @Override public void handle(ActionEvent e) {
					msg_verify.setText("Calculation... ");
			      	  tableOperations.getItems().clear(); // clear tableOperations for every new entry

		        	if(!addFunction.verifySetExists(VerifySet_num.getText().trim())) {
						 msg_part.setText("Calculation failure : Set inexistant in database");
		        	  }	
		        	else {
		        		List<String> myList = myFunction.findMissingParts(VerifySet_num.getText().trim());
		        		if(myList.size()<1) {msg_verify.setText("You are not missing any part (table below is blank)");}
		        		else {
		        		for(int i = 0; i < myList.size(); i=i+3) {
		        			msg_verify.setText("You can find the parts missing in the table below");
		        			Part myPart = new Part((myList.get(i)).trim(), myList.get(i+1).trim(), myList.get(i+2).trim());
			        		dataOperations.add(myPart);	
		        		}
		        		}
		        	}
//					 VerifySet_num.clear();
		        }
		    });
		  
		  HBox hbMsg_operation = new HBox();	    
		  hbMsg_operation.getChildren().addAll(VerifySet_num, testCanDoSet, testPurcentageSet, testPartsMissing);
		  hbMsg_operation.setSpacing(3);
		  
	      tableOperations.setEditable(true);
	        TableColumn<Part, String> part_miss_col = new TableColumn<>("Part Number");
	        part_miss_col.setMinWidth(140);
	        part_miss_col.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("part_num")
	    		);
	      
	        TableColumn<Part, String> color_miss_col = new TableColumn<>("Color ID");
	        color_miss_col.setMinWidth(100);
	        color_miss_col.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("color_id")
	    		);
	       
 	      
	        TableColumn<Part, String> quantity_miss_part = new TableColumn<>("Quantity Missing");
	        quantity_miss_part.setMinWidth(130);
	        quantity_miss_part.setCellValueFactory(
	    		    new PropertyValueFactory<Part,String>("quantity")
	    		);
	        	      

	        tableOperations.setItems(dataOperations);
	        tableOperations.getColumns().add(part_miss_col);
	        tableOperations.getColumns().add(color_miss_col);
	        tableOperations.getColumns().add(quantity_miss_part);

	       
		  
		  
		  
	  
	  	  
	  
      /* Operations On All Tab */
	        Label labelOperationALL = new Label("Operations on a All existing sets :");
	  	    labelOperationALL.setFont(new Font("Arial", 20));
	        Label msg_verifyALL = new Label(".");
	        
	        // Show all sets we can do in tableOperationsALL
	   	  Button testCanDoSetALL = new Button("What sets can I do ?");
	   	testCanDoSetALL.setOnAction(new EventHandler<ActionEvent>() {
	  		        @Override public void handle(ActionEvent e) {
	  		        tableOperationsALL.getItems().clear();
  		        	msg_verifyALL.setText("Calculing all the sets you can do... ");
  	        		List<String> myList = myFunction.findRealizableSets();
  	        		if(myList.size()<1) {msg_verifyALL.setText("You can't do any set");}
  	        		else {
  	        		msg_verifyALL.setText("You can do the sets in the table below");
  	        		for(int i = 0; i < myList.size(); i=i+2) {
  	        			Set mySet = new Set(myList.get(i).trim(), myList.get(i+1).trim());
  		        		dataOperationsALL.add(mySet);	
  	        		}
  	        		}
  	        	
//	  				VerifySet_num.clear();
	  		        }
	  		    });

	   	
        // Show all sets percentage realisation in tableOperationsALL
	   	 Button testPurcentageSetALL = new Button("Percentage realisation for all");
	   	testPurcentageSetALL.setOnAction(new EventHandler<ActionEvent>() {
	  		        @Override public void handle(ActionEvent e) {
		  		    tableOperationsALL.getItems().clear();
  		        	msg_verifyALL.setText("Calculing Percentage realisation of all Sets... ");
  	        		List<String> myList = myFunction.findMissingPercentageAllSets();
  	        		if(myList.size()<1) {msg_verifyALL.setText("You can't do any set");}
  	        		else {
  	        		msg_verifyALL.setText("Percentage realisation of all sets can be seen in the table below");
  	        		for(int i = 0; i < myList.size(); i=i+2) {
  	        			Set mySet = new Set(myList.get(i).trim(), myList.get(i+1).trim());
  		        		dataOperationsALL.add(mySet);	
  	        		}
  	        		}
  	        		
  	        	
//		  			VerifySet_num.clear();
        	
	  		        }
	  		    });
	    HBox hbMsg_operationALL = new HBox();	    
	    hbMsg_operationALL.getChildren().addAll(testCanDoSetALL, testPurcentageSetALL);
	    hbMsg_operationALL.setSpacing(3);
	   	
	    tableOperationsALL.setEditable(true);
	      TableColumn<Set, String> set_all_col = new TableColumn<>("Set Number");
	      set_all_col.setMinWidth(140);
	      set_all_col.setCellValueFactory(
	  		    new PropertyValueFactory<Set,String>("set_num")
	  		);
	      	    
	      		      
	      TableColumn<Set, String> quantity_all_set = new TableColumn<>("Percentage");
	      quantity_all_set.setMinWidth(200);
	      quantity_all_set.setCellValueFactory(
	  		    new PropertyValueFactory<Set,String>("quantity")
	  		);
	      
	
	      tableOperationsALL.setItems(dataOperationsALL);
	      tableOperationsALL.getColumns().add(set_all_col);
	      tableOperationsALL.getColumns().add(quantity_all_set);

	  	  

	  /* Tabs insertion to the stage */
			VBox vboxParts = new VBox();
 		    vboxParts.setSpacing(5);
  	        vboxParts.setPadding(new Insets(10, 0, 0, 10));
 	        vboxParts.getChildren().addAll(labelPart, tableParts, msg_part, hbAdd_part, deleteInfo, deleteAllPartsButton);
 	        
 	       VBox vboxSets = new VBox();
 		   vboxSets.setSpacing(5);
  	       vboxSets.setPadding(new Insets(10, 0, 0, 10));
 	       vboxSets.getChildren().addAll(labelSet, tableSets, msg_set, hbAdd_set, deleteAllSetsButton);
 	        
 	      VBox vboxOperations = new VBox();
 	      vboxOperations.setSpacing(5);
 	      vboxOperations.setPadding(new Insets(10, 0, 0, 10));
 	      vboxOperations.getChildren().addAll(labelOperation, hbMsg_operation, msg_verify, tableOperations); //to be added
	       
 	     VBox vboxOperationsALL = new VBox();
 	    vboxOperationsALL.setSpacing(5);
 	   vboxOperationsALL.setPadding(new Insets(10, 0, 0, 10));
 	  vboxOperationsALL.getChildren().addAll(labelOperationALL, hbMsg_operationALL, msg_verifyALL, tableOperationsALL); //to be added
	       
 	       tabParts.setContent(vboxParts);
 	       tabSets.setContent(vboxSets);
 	       tabOperations.setContent(vboxOperations);
 	       tabOperationsALL.setContent(vboxOperationsALL);


 	       tabPane.getTabs().add(tabSets);
 	       tabPane.getTabs().add(tabParts);
 	       tabPane.getTabs().add(tabOperations);
 	       tabPane.getTabs().add(tabOperationsALL);


 	       
 	        Stage stage_parts = new Stage();
 	        stage_parts.setTitle("LEGO");
 	        stage_parts.setScene(scene_parts);
 	        stage_parts.show();
 
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	// get quantity of a certain set in tableSets
	public String extractQuantitySets(String setn) {
		for (Iterator<Set> i = dataSets.iterator(); i.hasNext();) {
	    Set set1 = i.next();
	    if (set1.getSet_num().equals(setn)) return set1.getQuantity();
	}
		return "-1";
		}
	// get index of a certain set in tableSets
	public int extractIndexSets(String setn) {
		int j=-1;
		for (Iterator<Set> i = dataSets.iterator(); i.hasNext();) {
	    Set set1 = i.next();
	    j++;
	    if (set1.getSet_num().equals(setn)) return j;
	}
		return -1;
		}
	// get quantity of a certain part in tableParts
	public String extractQuantityParts(String partn, String colorn) {
		for (Iterator<Part> i = dataParts.iterator(); i.hasNext();) {
	    Part part1 = i.next();
	    if (part1.getPart_num().equals(partn) && part1.getColor_id().equals(colorn)) return part1.getQuantity();
	}
		return "-1";
		}
	// get Index of a certain part in tableParts
	public int extractIndexParts(String partn, String colorn) {
		int j=-1;
		for (Iterator<Part> i = dataParts.iterator(); i.hasNext();) {
			Part part1 = i.next();
	    j++;
	    if (part1.getPart_num().equals(partn) && part1.getColor_id().equals(colorn)) return j;
	}
		return -1;
		}	
	//verify if a string contains only numeric caracters (useful for colod_id and quantity verification)
	public static boolean isNumeric(String str)
	{
	    for (char c : str.toCharArray())
	    {
	        if (!Character.isDigit(c)) return false;
	    }
	    return true;
	}
	
	
}
