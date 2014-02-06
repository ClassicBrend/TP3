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

            try {
                
                    File file = new File(Main.jarPath+"CSVFiles/RaceList/races.csv");
                    Scanner input = new Scanner(file);
                   
                    while(input.hasNextLine()){
                        String line = input.nextLine();
                        String parts[] = line.split(",");
                        
                        for(int i = 4; i < 10;i++){
                            if(i==4){
                                if("N/A".equals(parts[i])){
                                    System.out.println("FFFUUUUUU");
                                    parts[i] = "0";
                                }
                            }
                            if(i==6||i==9){
                                if(parts[i].isEmpty()){
                                    parts[i] = "0";
                                }
                            }
                            else{
                                if(parts[i].isEmpty()){
                                    parts[i] = "0";
                                }
                            }
                        }
                        Statement st = con.createStatement();
                        String sql = "Insert into races "
                                + "Values('"+parts[0]+"','"+parts[1]+"','"+ parts[2]+"', '"+parts[3]+"','"+parts[4]+"','"+parts[5]+"','"+parts[6]+"','"+parts[7]+"','"+parts[8]+"','"+parts[9]+"','"+parts[10]+"')";

                        System.out.println(sql + parts[4]);
                        boolean update = st.execute(sql);

                       
                    }
                    

                    
                    
            } catch (SQLException s) {
                System.out.println("SQL statement is not executed!");
                s.printStackTrace();
            }
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

            try {
                
                    File file = new File(Main.jarPath+"CSVFiles/RunnerList/runnerDetails.csv");
                    Scanner input = new Scanner(file);
                   
                    while(input.hasNextLine()){
                        String line = input.nextLine();
                        String parts[] = line.split(",");
                         
                        Statement st = con.createStatement();
                        String sql = "Insert into test "
                                + "Values('"+parts[0]+"','"+parts[1]+"','"+ parts[2]+"', '"+parts[3]+"','"+parts[4]+"','"+parts[5]+"')";

                        System.out.println(sql);
                        boolean update = st.execute(sql);

                       
                    }
                    

                    
                    
            } catch (SQLException s) {
                System.out.println("SQL statement is not executed!");
                s.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
