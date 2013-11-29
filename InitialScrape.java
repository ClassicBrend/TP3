import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;
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
    
    public static void main(String[] args) throws IOException{
        deleteOldCsv("CSVFiles/racesRan");
        getRunners();
        checkDupes();
        System.out.println("RunnerList and individual runner details scraped successfully!\n\n");
    }
    
    /*
        passed in variable: runnerID (String)
        Grabs all the races ran by one runner which is chosen by the runnerID
        
    */
    public static void getIndividualRunnerDetails(String runnerID) throws IOException{
        String nameOfRace = null;
        final String runnerURL ="http://www.scottishhillracing.co.uk/RunnerDetails.aspx?FromSearch=true&RunnerID=" + runnerID;
        Document doc = Jsoup.connect(runnerURL).get();
        Element racesRan = doc.select("span[id=lblGridRacesRun]").first();
        Element table = doc.select("table[id=dgRunnerResults]").first();
        
        String racesSplit = racesRan.text();
        if(racesSplit.contains("races"))
            racesSplit = racesSplit.replace(" races","");
        else
            racesSplit = racesSplit.replace(" race", "");
        
        int ran = Integer.parseInt(racesSplit);
        
        Iterator<Element> position = table.select("td[width=50]").iterator();
        Iterator<Element> raceName = table.select("td[width=275]").iterator();
        Iterator<Element> dateOfRace = table.select("td[width=80]").iterator();
        Iterator<Element> timeOfRace = table.select("td[width=60]").iterator();
        Iterator<Element> percentWin = table.select("td[width=95]").iterator();
       
        String content = "Position, RaceName,Date,Time,Winner\n";
        
        for(int i = 0; i < ran; i++){
            String name = raceName.next().text();
            content += position.next().text();
            if(name.contains(","))
                name = name.replace(","," ");
            content += "," + name;
            content += "," + dateOfRace.next().text();
            content += "," + timeOfRace.next().text();
            content += "," + percentWin.next().text();
            if(i != 11)
                content += "\n";
            nameOfRace = name;
        }
        
        //System.out.print(content);
        deleteOldCsv("CSVFiles/Individual/" + runnerID);
        writeOutCsv("CSVFiles/Individual/" + runnerID, content);
        writeOutCsv("CSVFiles/racesRan", nameOfRace);
    }
    
    /*
        Grabs the page for the relevant club, in this instance its only the westies club
        Grabs al links and the runner table
        Selects the data needed with doc select and/or pattern matching
        calls writeOutCsv with the relevant data
    */
    public static void getRunners() throws IOException{
        final String clubURL = "http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076";
        Document doc = Jsoup.connect(clubURL).get();
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
            //System.out.println(matchedNumber);
            count++;
        } 
        deleteOldCsv("CSVFiles/RunnerList/runnerDetails");
        writeOutCsv("CSVFiles/RunnerList/runnerDetails", "Surname,Forename,runnerID,averageWin,racesRecorded,gender");

        for (int i = 0; i < runners.length;i++) {
            String name = runners[i];
            RunnerDetails(name,matchArray[i],perWinEl.next().text(),racRecEl.next().text(), genEl.next().text(), runners.length, i);
            getIndividualRunnerDetails(matchArray[i]);
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
//        System.out.println("first name:\t" + firstName + "\nsecond name:\t" + 
//                secondName + "\nRunner ID:\t" + rID + "\nAvg % Win:\t" + avgWin +  
//                "\nRaces Recorded:\t" + racRec + "\nGender:\t\t" + gen + "\n-----\n");
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
            
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    public static void checkDupes(){
        File file = new File("CSVFiles/racesRan.csv");
        ArrayList raceList = new ArrayList();
        HashSet raceSet = new HashSet();
        try{
            Scanner input = new Scanner(file);
            
            while(input.hasNextLine()){
                String line = input.nextLine();
                raceList.add(line);
                System.out.println(line);
                
            }
            
            
        }catch(Exception e){
            
        }
        
        raceSet.addAll(raceList);
        raceList.clear();
        raceList.addAll(raceSet);
        deleteOldCsv("CSVFiles/racesRan");
        for(int i = 0; i < raceList.size();i++){
            writeOutCsv("CSVFiles/racesRan", raceList.get(i).toString());
        }
    }
}
