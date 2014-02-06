import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.swing.*;

public class Main extends JFrame{
   
    JFrame testFrame;
    public static  JButton btnStart,btnDatabase,btnDrop;
    public static JLabel lblScrape,lblError;
    
    //public static String[] publicRunners = new String[164];
    public static ArrayList<String> publicRunners = new ArrayList<String>();
    static AtomicInteger runnerCounter = new AtomicInteger(0);
    static AtomicInteger raceCounter = new AtomicInteger(0);
    
    public static HashSet raceSet = new HashSet();
    
    public static boolean ranScrape = false;
    public static boolean scrapeCompleted = false;
    
    public static String jarPath;
    
    public static int timeOutPeriod = 0;
    public static DatabaseAccess db = new DatabaseAccess();

    
    public Main() throws URISyntaxException{
        jarPath = Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        jarPath = jarPath.substring(0,jarPath.lastIndexOf('/')+1);

        initUI();
    }
    
    private void initUI(){
                
        JPanel pnlButtons = new JPanel();
        getContentPane().add(pnlButtons);
        
        pnlButtons.setLayout(null);

        btnStart = new JButton("Start Scrape");
        btnStart.setBounds(10,130,150,30);
        
        btnStart.addActionListener(new ActionListener(){
           @Override
            public void actionPerformed(ActionEvent event){
                btnStartActionPerformed();
            }
        });
        

        
        btnDatabase = new JButton("Update Tables");
        btnDatabase.setBounds(180,130,150,30);
        btnDatabase.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                btnTableActionPerformed();
            }
            
        });
        btnDrop = new JButton("Clear Table");
        btnDrop.setBounds(350,130,150,30);
        btnDrop.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                btnDropActionPerformed();
            }
            
        });
        
        btnDatabase.setEnabled(false);
        btnDrop.setEnabled(false);
        
        lblScrape = new JLabel("Data has not been scraped...");
        lblScrape.setBounds(10,100,300,30);
        
        lblError = new JLabel("");
        lblError.setBounds(10,50,500,30);
        
        pnlButtons.add(btnStart);
        pnlButtons.add(btnDatabase);
        pnlButtons.add(btnDrop);
        pnlButtons.add(lblScrape);
        pnlButtons.add(lblError);
        

        
        setTitle("Westies Runners Scrape");
        setSize(530,200);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);

    }
    
    public static void main(String[] args) throws URISyntaxException {
         SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Main ex = new Main();
                    ex.setVisible(true);
                } catch (URISyntaxException ex1) {
                    Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex1);
                }
            }
        });
        

    }
    
    private void btnStartActionPerformed() { 
        if(scrapeCompleted == false){
            // Creates the necessary folders if they don't already exist
            File Individual = new File(jarPath + "CSVFiles/Individual");
            File RunnerList = new File(jarPath + "CSVFiles/RunnerList");
            File RaceList = new File(jarPath + "CSVFiles/RaceList");
            Individual.mkdirs();
            RunnerList.mkdirs();
            RaceList.mkdirs();

            if(!ranScrape){
                lblScrape.setText("Scraping...");
                lblError.setText("");
                ranScrape = true;
                ExecutorService exec = Executors.newCachedThreadPool();
                deleteOldCsv("CSVFiles/RaceList/races");
                deleteOldCsv("CSVFiles/racesRan");
                deleteOldCsv("CSVFiles/RunnerList/runnerDetails");
                beginScrape();
                for(int i = 0; i < publicRunners.size(); i++){
                    exec.submit(new RunnerProcess(i, publicRunners.get(i)) );
                }
                exec.shutdown();
                System.out.println("RunnerList created, creating individual runners files\n");
            }
        } else {
                String page = "http://www.westiesrunners.com";
            try {
                java.awt.Desktop.getDesktop().browse(java.net.URI.create(page));
            } catch (IOException ex) {
                Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    private void btnTableActionPerformed(){
/////////////ORIGINALCODE///////////////////////////
//        DatabaseAccess db = new DatabaseAccess();
//        db.go();
//        db.dropTable("test");
//        db.dropTable("races");
//        db.updateRaceTable();
//        db.updateRunnerTable();
//        System.out.println("GREAT SUCCESS");
/////////////////////////////////////////////////////
        
        db.go();
        db.dropTable("test");
        db.dropTable("races");
        db.updateRaceTable();
        db.updateRunnerTable();
        db.createRunnerTable(publicRunners);
        
    }
    
    private void btnDropActionPerformed(){
        db.dropRaceTable();
        lblScrape.setText("Race table cleared");
    }
        
    public static void beginScrape(){  
        try {
            int amount = 0;
            final String clubURL = "http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076";
            //Document doc = Jsoup.connect(clubURL).timeout(timeOutPeriod).get();
            Document doc = Jsoup.connect(clubURL).timeout(timeOutPeriod).ignoreHttpErrors(true).get();
            //writeOutCsv("CSVFiles/RunnerList/runnerDetails", "Surname,Forename,runnerID,averageWin,racesRecorded,gender");
            
            
            getDetails(doc,amount);
            
            Element postBackCol = doc.select("td[colspan=6]").first();
            Elements postBackLinks = postBackCol.select("a[href*=doPostBack");
            int linkCount = Integer.parseInt(postBackLinks.last().text());
            
            
            if(linkCount > 0){
                for(int i = 1; i < linkCount;i++){
                    amount += 75;
                    String eventVal = doc.select("input[id=__EVENTVALIDATION]").first().val();
                    String vState = doc.select("input[id=__VIEWSTATE]").first().val();
                    
                    
                    Document docu = Jsoup.connect("http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076")
                            .data("__EVENTTARGET", "dgRunners$ctl79$ctl0"+(i))
                            .data("__EVENTARGUMENT","")
                            .data("__LASTFOCUS","")
                            .data("__VIEWSTATE",vState)
                            .data("__EVENTVALIDATION",eventVal)
                            .userAgent("Mozilla")
                            .timeout(0)
                            .post();
                    
                    getDetails(docu,amount);
                }
            }
        } catch (IOException ex) { 
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    public static void getDetails(Document doc,int count){
        Elements links = doc.select("a[href*=RunnerDetails]");
        Element runnerTable = doc.select("table[id=tblGrid]").first();
        
        Iterator<Element> perWinEl = runnerTable.select("td[width=135]").iterator();
        Iterator<Element> racRecEl = runnerTable.select("td[width=140]").iterator();
        Iterator<Element> genEl = runnerTable.select("td[width=60]").iterator();
       
        String linkContent = links.outerHtml();
        String pattern = ".\\d\\d\\d\\d*";
        
        // Create the pattern object
        Pattern pat = Pattern.compile(pattern);
        
        // Create a matcher object
        Matcher match = pat.matcher(linkContent);
        
        String linkText = links.text();
        linkText = linkText.replace(", ",",");
        linkText = linkText.replace(" ","\n");

        String[] runners = linkText.split("\n");
        String[] matchArray;
        matchArray = new String[runners.length];
        int c = 0;
        
        while(match.find()){
            matchArray[c] = match.group(0);
            c++;
        } 
        

        for (int i = 0; i < runners.length;i++,count++) {
            String name = runners[i];
            String avgWin = perWinEl.next().text();
            avgWin = avgWin.substring(0, avgWin.length()-1);
            double avg = Double.parseDouble(avgWin);
            String races = racRecEl.next().text();
            int racesRan = Integer.parseInt(races);
            RunnerDetails(name,matchArray[i],avg,racesRan, genEl.next().text(), runners.length, i);
            //publicRunners[count] = matchArray[i];
            publicRunners.add(matchArray[i]);
        }
        
    }
    
    
    /*
        passed in variables: name, rID, avgWin, racRec, gen, runners, runner(String, String,String,String,String,int,int)
        Splits up the name into first name and second name
        Prints out scraping progress
        then calls writeOutCsv with the relevant data.
    */
    public static void RunnerDetails(String name, String rID,double avgWin, int racRec,String gen, int runners, int runner){
        //DatabaseAccess db = new DatabaseAccess();
       // db.go();
        
        
        
        String fullName = name;
        String[] split = fullName.split(",");
        String secondName = split[0];
        String firstName = split[1];
        writeOutCsv("CSVFiles/RunnerList/runnerDetails",rID + "," + secondName + "," +
                firstName +  "," + avgWin + "," + racRec + "," + gen);
        //db.updateRunnerTable(rID, secondName, firstName, avgWin, racRec, gen);

    }
    
    /*
        passed in variables: path, content(String,String)
        Writes out what was passed in as content to what was given as path.
    */
    public static void writeOutCsv(String path, String content){
        try{
            File output = new File(jarPath + path + ".csv");

            if(!output.exists())
                output.createNewFile();

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
            out.println(content);
            out.close();
            } catch (IOException e) {
                    e.printStackTrace();
                    Main.lblError.setText(e.getMessage());
        }
    }
    
    /*
        passed in variable: path(String)
        checks if the file requested for deletion exists
        if it does then the file is deleted.
    */
    public static void deleteOldCsv(String path){
        try{
            File file = new File(jarPath + path + ".csv");
            
            if(file.exists()){
               file.delete();
            }
            
        }catch (Exception e){Main.lblError.setText(e.getMessage());}
    }
    
    /*
        Takes in the hashset of races and writes them out to a csv file.
    */
    public static void GenerateRaces(HashSet races){
        File file = new File("racesRan.csv");
        Iterator hashIter = races.iterator();
        
        ExecutorService exec2 = Executors.newCachedThreadPool();
        
        int count = 0;
        while(hashIter.hasNext()){
            String raceInfo = hashIter.next().toString();
            writeOutCsv("CSVFiles/racesRan", raceInfo);
            
            exec2.submit(new RaceProcess(count,raceInfo));
            count++;
            System.out.println(count);
            
        }

        exec2.shutdown();
        
        
    }
    
    public static void getRaceInformation(String raceInfo){

        Document doc = null;
        final String url = "http://www.scottishhillracing.co.uk/RaceDetails.aspx?";
 
        
        try {
            doc = Jsoup.connect(url+"RaceID="+raceInfo).timeout(timeOutPeriod).ignoreHttpErrors(true).get();

        } catch (IOException ex) {
            Main.lblError.setText(ex.getMessage());
        }
        
        Element menRecordHolderlbl = doc.getElementById("lblMensRecordHolder");
        Element menRecordHolderhyp = doc.getElementById("hypMensRecordHolder");
        Element womenRecordHolderlbl = doc.getElementById("lblWomensRecordHolder");
        Element womenRecordHolderhyp = doc.getElementById("hypWomensRecordHolder");
        Element mensRecordName = null;
        Element womensRecordName = null;



        Element venue = doc.select("span[id=lblVenue]").first();
        Element distance = doc.select("span[id=lblDistance]").first();
        Element climb = doc.select("span[id=lblClimb]").first();
        Element mensRecordTime = doc.select("span[id=lblMensRecordTime]").first();
        Element mensRecordYear = doc.select("span[id=lblMensRecordYear]").first();
        if(menRecordHolderlbl == null){
            mensRecordName = doc.select("a[id=hypMensRecordHolder]").first();
        }
        else if(menRecordHolderhyp == null){
            mensRecordName = doc.select("span[id=lblMensRecordHolder]").first();
        }
        Element womensRecordTime = doc.select("span[id=lblWomensRecordTime]").first();
        Element womensRecordYear = doc.select("span[id=lblWomensRecordYear]").first();
        if(womenRecordHolderlbl == null){
          womensRecordName = doc.select("a[id=hypWomensRecordHolder]").first();
        }
        else if(womenRecordHolderhyp == null){
          womensRecordName = doc.select("span[id=lblWomensRecordHolder]").first();
        }
        
        String ven = venue.text();
        ven = ven.replace(",", "-");
        
        deleteOldCsv(jarPath+"CSVFiles/RaceList/"+raceInfo);
        
        writeOutCsv("CSVFiles/RaceList/"+raceInfo, ven + "," + distance.text() + "," + climb.text() + "," + mensRecordTime.text() + "," + mensRecordYear.text()
         + "," + womensRecordTime.text() + "," + womensRecordYear.text() + "," + womensRecordName.text());

//        System.out.println(venue.text());
//        System.out.println(distance.text());
//        System.out.println(climb.text());
//        System.out.println(mensRecordTime.text());
//        System.out.println(mensRecordYear.text());
//        System.out.println(mensRecordName.text());
//        System.out.println(womensRecordTime.text());
//        System.out.println(womensRecordYear.text());
//        System.out.println(womensRecordName.text());
//        System.out.println("-------");

        
    }
    
    
    public static void uploadTables() {
//        DatabaseAccess db = new DatabaseAccess();
//        db.go();
//        //db.dropRaceTable();
//        db.updateRaceTable();
//        System.out.println("GREAT SUCCESS");
        
    }
    
}