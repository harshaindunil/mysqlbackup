package classes;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 *
 * @author Harsha Indunil P.A
 */

// <editor-fold defaultstate="collapsed" desc="MySQL Database Backup and Restore">
public class DatabaseBackup {
    public static int SaveBackup(String path){
        BufferedWriter writer = null;
        ArrayList<String> tables = new ArrayList<String>();
        try {   
            //File
            File logFile = new File(path);      
            writer = new BufferedWriter(new FileWriter(logFile));
                
            ResultSet r = DB.Search("Show tables");
            while(r.next()){
                tables.add(r.getString("Tables_in_"+DB.dbName));
            }
            
            writer.write("-- MYSQL BACKUP BY <HARSHA INDUNIL>" ); //Change the Name
            writer.write("\n");
            writer.write("\n");
            
            for (String table : tables){
                writer.write("-- Insert data for table `"+table+"`");               
                writer.write("\n");

                int i=0; //recorde count
                
                //Delete all data from table
                String SQLQInsert="DELETE FROM `"+table+"`;\n";
                
                String colNames="";
                ResultSet r4 = DB.Search("SHOW columns FROM "+  table);
                while(r4.next()){
                    colNames+=r4.getString("Field")+",";
                }
                colNames=colNames.substring(0, colNames.length()-1);
                
                SQLQInsert += "LOCK TABLES `"+table+"` WRITE;\n"
                        + "INSERT INTO `"+table+"` (" +colNames+ ") VALUES ";
                
                ResultSet r3 = DB.Search("Select * From "+  table);
                while(r3.next()){
                    String SQLQ="";
                    ResultSet r2 = DB.Search("SHOW columns FROM "+  table);
                    while(r2.next()){
                        if(SQLQ.isEmpty()){
                            SQLQ += "('";
                        }else{
                            SQLQ += "'";
                        }
                        SQLQ += r3.getString(r2.getString("Field")).trim();
                        SQLQ += "',";
                        i++;
                    }
                    SQLQ=SQLQ.substring(0, SQLQ.length()-1);
                    SQLQ += ")";
                    
                    SQLQInsert += SQLQ + ",";
                }
                SQLQInsert = SQLQInsert.substring(0, SQLQInsert.length()-1);
                SQLQInsert += ";";
                SQLQInsert += "\nUNLOCK TABLES;";
                
                if(i>0){
                    writer.write(SQLQInsert);
                    writer.write("\n\n");
                }else{
                    writer.write("-- No recode in `"+table+"`");               
                    writer.write("\n\n");
                }              
            }
            
            //can add date time
            writer.write("-- Backup completed");
            writer.write("\n");
            
            
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        } finally {
            try {
                // Close the writer regardless of what happens...
                writer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        System.gc();
        return 1; // Success
    }
    
    public static int RestoreBackup(String path){
        try {
            BufferedReader br = new BufferedReader(new FileReader(path));
            String line;
            while ((line = br.readLine()) != null) {
               if(!(line.trim().isEmpty())){
                   if(!(line.trim().substring(0, 2).equals("--"))){
                       DB.Update(line);
                   }
               }
            }
            br.close();
        } catch (Exception e) {            
            e.printStackTrace();
            return 0;
        }
        System.gc();
        return 1;
    }
}
// </editor-fold>


