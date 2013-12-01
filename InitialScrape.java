import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author  David Drouin
 *          V0.2
 */
public class InitialScrape { 
    
    public static String[] publicRunners = new String[75];
    
    public static void main(String[] args) throws IOException{
        ExecutorService exec = Executors.newCachedThreadPool();
        deleteOldCsv("CSVFiles/racesRan");
        getRunners();
        for(int i = 0; i < publicRunners.length; i++){
            exec.submit(new Process(i,publicRunners[i]));;
        }
        
        checkDupes();
        exec.shutdown();
        System.out.println("RunnerList and individual runner details scraped successfully!\n\n");

    }

    /*
        Grabs the page for the relevant club, in this instance its only the westies club
        Grabs al links and the runner table
        Selects the data needed with doc select and/or pattern matching
        calls writeOutCsv with the relevant data
    */
    public static void getRunners() throws IOException{
        final String clubURL = "http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076";
        Document doc = Jsoup.connect(clubURL).timeout(10*1000).get();
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
        int count = 0;
        
        while(match.find()){
            matchArray[count] = match.group(0);
            count++;
        } 
        deleteOldCsv("CSVFiles/RunnerList/runnerDetails");
        writeOutCsv("CSVFiles/RunnerList/runnerDetails", "Surname,Forename,runnerID,averageWin,racesRecorded,gender");

        for (int i = 0; i < runners.length;i++) {
            String name = runners[i];
            RunnerDetails(name,matchArray[i],perWinEl.next().text(),racRecEl.next().text(), genEl.next().text(), runners.length, i);
            publicRunners[i] = matchArray[i];
            //getIndividualRunnerDetails(matchArray[i]);
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
        System.out.println(runner + "\\" + runners + " processed");
        writeOutCsv("CSVFiles/RunnerList/runnerDetails",secondName + "," +
                firstName + "," +rID + "," + avgWin + "," + racRec + "," + gen);
    }
    
    /*
        passed in variables: path, content(String,String)
        Writes out what was passed in as content to what was given as path.
    */
    public static void writeOutCsv(String path, String content){
        try{
            File output = new File(path + ".csv");

            if(!output.exists())
                output.createNewFile();

            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(output, true)));
            out.println(content);
            out.close();
            } catch (IOException e) {
                    e.printStackTrace();
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
            
        }catch (Exception e){e.printStackTrace();}
    }
    
    /*
        Inefficient, will fix
    */
    public static void checkDupes(){
        File file = new File("CSVFiles/racesRan.csv");
        ArrayList raceList = new ArrayList();
        HashSet raceSet = new HashSet();
        try{
            Scanner input = new Scanner(file);
            
            while(input.hasNextLine()){
                String line = input.nextLine();
                raceList.add(line);                
            }

        }catch(Exception e){}

        
        raceSet.addAll(raceList);
        raceList.clear();
        raceList.addAll(raceSet);
        deleteOldCsv("CSVFiles/racesRan");
        for(int i = 0; i < raceList.size();i++){
            writeOutCsv("CSVFiles/racesRan", raceList.get(i).toString());
        }
    }
}
