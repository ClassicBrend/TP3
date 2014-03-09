import java.sql.*;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

public class DatabaseAccess {

    static int lport;
    static String rhost;
    static int rport;
    
    public DatabaseAccess(){
        System.out.println("DATABASE");
    }

    public static void go() {
        String user = "westies";
        String password = "TeamBA2013";
        String host = "5.9.130.155";
        int port = 22;
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(user, host, port);
            lport = 3306;
            rhost = "127.0.0.1";
            rport = 3306;
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            System.out.println("Establishing Connection...");
            session.connect();
            int assinged_port = session.setPortForwardingL(lport, rhost, rport);
            System.out.println("localhost:" + assinged_port + " -> " + rhost + ":" + rport);
        } catch (Exception e) {
            System.err.print(e);
        }
    }

    public static void main(String[] args) {
        try {
            go();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
    public void updateRaceTable(){
       Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);

            Statement st = con.createStatement();
            String path = Main.jarPath+"CSVFiles/RaceList/races.csv";
            String sql = " LOAD DATA LOCAL INFILE '" + path +
                "' INTO TABLE races " +
                " FIELDS TERMINATED BY \',\' ENCLOSED BY \'\"'" +
                " LINES TERMINATED BY \'\\n\'";

            boolean update = st.execute(sql);
           
            System.out.println("All runner tables uploaded");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    // Create individual tables for the runners
    public void createRunnersTable(ArrayList<String> publicRunners){
        Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);

            for(int i = 0; i < publicRunners.size(); i++){
           
                    String runnerID = publicRunners.get(i);

                    File file = new File(Main.jarPath+"CSVFiles/Individual/" + runnerID + ".csv");
                    Scanner input = new Scanner(file);
                    
                    Statement st = con.createStatement();
                    String sql = "CREATE TABLE IF NOT EXISTS " + runnerID + 
                            "(position int(3),"
                            + "raceName varchar(128),"
                            + "raceDate date,"
                            + "time varchar(7),"
                            + "winner double(10,1),"
                            + "UNIQUE(raceDate)"
                            + ")";
                   
                    boolean update = st.execute(sql);
                    Main.lblScrape.setText("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
                    System.out.println("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
                    
                    while(input.hasNextLine()){
                        String line = input.nextLine();
                        String parts[] = line.split(",");
                        String origDate = parts[2];
                        String year = origDate.substring(6,10);
                        String month = origDate.substring(3,5);
                        String day = origDate.substring(0,2);
                        String formattedDate = (year + "-" + month + "-" + day);
                        
                         
                        st = con.createStatement();
                        sql = "REPLACE INTO " + runnerID 
                                + " Values('"+parts[0]+"','"+parts[1]+"','"+ formattedDate+"', '"+parts[3]+"','"+parts[4]+"')";

                        
                        update = st.execute(sql);

                       
                    }
            }
            System.out.println("All runner tables uploaded");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void oldCreateRunnerTable(ArrayList<String> publicRunners){
        Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);


           
                    //String runnerID = publicRunners.get(i);

                    File file = new File(Main.jarPath+"CSVFiles/Individual/runners.csv");
                    Scanner input = new Scanner(file);
                    
                    Statement st = con.createStatement();
                    String sql = "CREATE TABLE IF NOT EXISTS runner" + 
                            "(runnerID varchar(10),"
                            + "position int(3),"
                            + "raceName varchar(128),"
                            + "raceDate date,"
                            + "time varchar(7),"
                            + "winner double(10,1)"
                            + ""
                            + ")";
                   
                    boolean update = st.execute(sql);
                   // Main.lblScrape.setText("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
                    //System.out.println("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
                    int counter = 0;
                    while(input.hasNextLine()){
                        counter++;
                        System.out.println("Processing:" + counter + " out of " + publicRunners.size());
                        String line = input.nextLine();
                        String parts[] = line.split(",");
                        String origDate = parts[3];
                        String year = origDate.substring(6,10);
                        String month = origDate.substring(3,5);
                        String day = origDate.substring(0,2);
                        String formattedDate = (year + "-" + month + "-" + day);
                        
                         
                        st = con.createStatement();
                        sql = "REPLACE INTO runner "
                                + " Values('"+parts[0]+"','"+parts[1]+"','"+ parts[2]+"', '"+formattedDate+"','"+parts[4]+"','"+parts[5]+"')";

                        
                        update = st.execute(sql);

                       
                    }
            
            System.out.println("All runner tables uploaded");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void createRunnerTable(ArrayList<String> publicRunners){
        Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);

            Statement st = con.createStatement();
            String sql = "CREATE TABLE IF NOT EXISTS runner" + 
                    "(runnerID varchar(10),"
                    + "position int(3),"
                    + "raceName varchar(128),"
                    + "raceDate date,"
                    + "time varchar(7),"
                    + "winner double(10,1)"
                    + ""
                    + ")";

            boolean update = st.execute(sql);
           // Main.lblScrape.setText("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
            //System.out.println("Processing " + runnerID + "("+i+"/"+publicRunners.size() + ")");
            int counter = 0;
                    
            st = con.createStatement();
            String path = Main.jarPath+"CSVFiles/Individual/runners.csv";
            sql = " LOAD DATA LOCAL INFILE '" + path +
                "' INTO TABLE runner " +
                " FIELDS TERMINATED BY \',\' ENCLOSED BY \'\"'" +
                " LINES TERMINATED BY \'\\n\'";

            update = st.execute(sql);
           
            System.out.println("All runner tables uploaded");
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void dropRaceTable(){
        Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);


            Statement st = con.createStatement();
            String sql = "TRUNCATE TABLE races;";

            boolean update = st.execute(sql);

     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void dropTable(String tableName){
                Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);


            Statement st = con.createStatement();
            String sql = "TRUNCATE TABLE "+tableName+";";

            boolean update = st.execute(sql);

     
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public void updateRunnerTable(){
       Connection con = null;
        String driver = "com.mysql.jdbc.Driver";
        //String url = "jdbc:mysql://" + rhost +":" + lport + "/";
        String url = "jdbc:mysql://" + rhost + ":" + lport + "/" + "westies_db";
        System.out.println(url);
        String db = "westies_db";
        String dbUser = "westies_user";
        String dbPasswd = "TeamBA2013";
        try {
            Class.forName(driver);
            //con = DriverManager.getConnection(url+db, dbUser, dbPasswd);
            con = DriverManager.getConnection(url, dbUser, dbPasswd);

            Statement st = con.createStatement();
            String path = Main.jarPath+"CSVFiles/RunnerList/runnerDetails.csv";
            String sql = " LOAD DATA LOCAL INFILE '" + path +
                "' INTO TABLE test " +
                " FIELDS TERMINATED BY \',\' ENCLOSED BY \'\"'" +
                " LINES TERMINATED BY \'\\n\'";

            boolean update = st.execute(sql);
           
            System.out.println("All runner tables uploaded");
            Main.lblScrape.setText("Tables uploaded successfully");
            
            

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
