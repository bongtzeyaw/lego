package admin;

import database.OperationDatabase;
import java.io.*;

/** A class that creates table and inserts rows in a table in an existing database associated with the current connection.
    The rows to be added are given in CSV files */
public class InputCSV {
	/** Adds input rows */
	public static void insertRows(OperationDatabase od, String csvFile, String sqlInsertRows) {
		try {
			//Open CSV File
	        File file = new File(csvFile);
	        FileReader fr = new FileReader(file);
	        BufferedReader br = new BufferedReader(fr);
	        String line = "";
	        String delimiter = ",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)";		//Cf note below to understand the delimiter used
	        String[] strArr;
	        line = br.readLine(); 										//Ignores title row 
	        
	        //Read CSV row by row
	        while((line = br.readLine()) != null) {
	        	strArr = line.split(delimiter, -1);
	        	od.update(sqlInsertRows, strArr); 						//Insert a data row into database with the sql provided
	        }
	        br.close();
	        } catch (Exception e) {
	        	e.printStackTrace();
        }
	}
}

/*
NOTE on delimiter:

For a simple Comma-Separated Value (CSV) file, an example of line would be : 001-1,Gears,1965,1,43
Hence, the String delimiter is simply ",".  						

For our case however, the CSV file may contain "," within the field value and this is signaled by "".
An example of line would be : 0241363764-1,"NINJAGO Visual Dictionary, New Edition",2019,497,5
Hence, a more complicated String delimiter is required. For comprehension, below is the equivalent definition of the delimiter used.

String otherThanQuote = " [^\"] ";
String quotedString = String.format(" \" %s* \" ", otherThanQuote);
String delimiter = String.format("(?x) "+ // enable comments, ignore white spaces
        ",                         "+ // match a comma
        "(?=                       "+ // start positive look ahead
        "  (?:                     "+ //   start non-capturing group 1
        "    %s*                   "+ //     match 'otherThanQuote' zero or more times
        "    %s                    "+ //     match 'quotedString'
        "  )*                      "+ //   end group 1 and repeat it zero or more times
        "  %s*                     "+ //   match 'otherThanQuote'
        "  $                       "+ // match the end of the string
        ")                         ", // stop positive look ahead
        otherThanQuote, quotedString, otherThanQuote);
 */
