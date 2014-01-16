import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.net.URISyntaxException;
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
    public static  JButton btnStart,btnWeb;
    public static JLabel lblScrape,lblError;
    
    public static String[] publicRunners = new String[165];
    static AtomicInteger runnerCounter = new AtomicInteger(0);
    static AtomicInteger raceCounter = new AtomicInteger(0);
    
    public static HashSet raceSet = new HashSet();
    
    public static boolean ranScrape = false;
    
    public static String jarPath;
    
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
        

        
        btnWeb = new JButton("View Website");
        btnWeb.setBounds(180,130,150,30);
        btnWeb.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                btnWebActionPerformed();
            }
            
        });
        
        btnWeb.setEnabled(false);
        
        lblScrape = new JLabel("Data has not been scraped...");
        lblScrape.setBounds(10,100,300,30);
        
        lblError = new JLabel("");
        lblError.setBounds(10,50,500,30);
        
        pnlButtons.add(btnStart);
        pnlButtons.add(btnWeb);
        pnlButtons.add(lblScrape);
        pnlButtons.add(lblError);
        

        
        setTitle("Westies Runners Scrape");
        setSize(350,200);
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
            try {
                
                ExecutorService exec = Executors.newCachedThreadPool();
                deleteOldCsv("CSVFiles/racesRan");
                deleteOldCsv("CSVFiles/RunnerList/runnerDetails");
                deleteOldCsv(jarPath+"CSVFiles/racesRan");
                deleteOldCsv(jarPath+"CSVFiles/RunnerList/runnerDetails");

                beginScrape();
                for(int i = 0; i < publicRunners.length; i++){
                    exec.submit(new RunnerProcess(i,publicRunners[i]) ); 
                }

                exec.shutdown();
                System.out.println("RunnerList created, creating individual runners files\n");

            } catch (IOException ex) {
                Main.lblError.setText(ex.getMessage());
            }
        }
    }
    
    private void btnWebActionPerformed(){
        try{
            String url = "http://www.westiesrunners.com";
            java.awt.Desktop.getDesktop().browse(java.net.URI.create(url));
        }catch(java.io.IOException e){
            Main.lblError.setText(e.getMessage());
        }
    }
        
    public static void beginScrape() throws IOException{
        int amount = 0;
        final String clubURL = "http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076";
        Document doc = Jsoup.connect(clubURL).timeout(5*1000).get();
        writeOutCsv("CSVFiles/RunnerList/runnerDetails", "Surname,Forename,runnerID,averageWin,racesRecorded,gender");


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
                            .timeout(5*1000)
                        .post();

                getDetails(docu,amount);
            }
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
            RunnerDetails(name,matchArray[i],perWinEl.next().text(),racRecEl.next().text(), genEl.next().text(), runners.length, i);
            publicRunners[count] = matchArray[i];
        }
        
    }
    
    
    /*
        passed in variables: name, rID, avgWin, racRec, gen, runners, runner(String, String,String,String,String,int,int)
        Splits up the name into first name and second name
        Prints out scraping progress
        then calls writeOutCsv with the relevant data.
    */
    public static void RunnerDetails(String name, String rID,String avgWin, String racRec,String gen, int runners, int runner){
        String fullName = name;
        String[] split = fullName.split(",");
        String secondName = split[0];
        String firstName = split[1];
        writeOutCsv("CSVFiles/RunnerList/runnerDetails",secondName + "," +
                firstName + "," +rID + "," + avgWin + "," + racRec + "," + gen);

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
            File file = new File(path + ".csv");
            
            if(file.exists()){
               file.delete();
            }
            
        }catch (Exception e){Main.lblError.setText(e.getMessage());}
    }
    
    /*
        Takes in the hashset of races and writes them out to a csv file.
    */
    public static void GenerateRaces(HashSet races){
        btnWeb.setEnabled(true);
        lblScrape.setText("Scraping completed");
        //File file = new File("CSVFiles/racesRan.csv");
        File file = new File("racesRan.csv");
        Iterator hashIter = races.iterator();
        
        ExecutorService exec2 = Executors.newCachedThreadPool();
        
        int count = 1;
        while(hashIter.hasNext()){
            String raceInfo = hashIter.next().toString();
            writeOutCsv("CSVFiles/racesRan", raceInfo);
            
            exec2.submit(new RaceProcess(count,raceInfo));
            count++;
            
        }

        exec2.shutdown();
        
        
    }
    
    public static void getRaceInformation(String raceInfo){

        Document doc = null;
        final String url = "http://www.scottishhillracing.co.uk/RaceDetails.aspx?";
 
        
        try {
            doc = Jsoup.connect(url+"RaceID="+raceInfo).timeout(5*1000).get();

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
    
}