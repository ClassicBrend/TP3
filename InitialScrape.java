package Scrape;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.io.*;
import java.util.Iterator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 *
 * @author David
 */
public class InitialScrape {
    
    public static void main(String[] args) throws IOException{
        getRunners();
        //getIndividualRunnerDetails("R12708");




    }
    
    public static void getIndividualRunnerDetails(String runnerID) throws IOException{
        final String runnerURL ="http://www.scottishhillracing.co.uk/RunnerDetails.aspx?FromSearch=true&RunnerID=" + runnerID;
        Document doc = Jsoup.connect(runnerURL).get();
        Element table = doc.select("table[id=dgRunnerResults]").first();
        
        Iterator<Element> position = table.select("td[width=50]").iterator();
        System.out.println(position.next().text());
    }
    
    public static void getRunners() throws IOException{
        final String clubURL = "http://www.scottishhillracing.co.uk/Runners.aspx?ClubID=C1076";
        Document doc = Jsoup.connect(clubURL).get();
        Elements links = doc.select("a[href*=RunnerDetails]");
        Element runnerTable = doc.select("table[id=tblGrid]").first();
        
        Iterator<Element> perWinEl = runnerTable.select("td[width=135]").iterator();
        Iterator<Element> racRecEl = runnerTable.select("td[width=140]").iterator();
        Iterator<Element> genEl = runnerTable.select("td[width=60]").iterator();
       
        String linkContent = links.outerHtml();
        String pattern = ".\\d\\d\\d\\d";
        
        // Create the pattern object
        Pattern pat = Pattern.compile(pattern);
        
        // Create a lovely matcher object
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
        deleteOldCSV("runnerDetails");
        for (int i = 0; i < runners.length;i++) {
            String name = runners[i];
            RunnerDetails(name,matchArray[i],perWinEl.next().text(),racRecEl.next().text(), genEl.next().text());
        }
    }
    
    public static void RunnerDetails(String name, String rID,String avgWin, String racRec,String gen){
        String fullName = name;
        String[] split = fullName.split(",");
        String secondName = split[0];
        String firstName = split[1];
        
        System.out.println("first name:\t" + firstName + "\nsecond name:\t" + 
                secondName + "\nRunner ID:\t" + rID + "\nAvg % Win:\t" + avgWin +  
                "\nRaces Recorded:\t" + racRec + "\nGender:\t\t" + gen + "\n-----\n");
        writeOutCsv("runnerDetails",firstName + "," +
                secondName + "," +rID + "," + avgWin + "," + racRec + "," + gen);
    }
    
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
    
    public static void deleteOldCSV(String path){
        try{
            File file = new File(path + ".csv");
            
            if(file.exists()){
               file.delete();
            }
            
            
        }catch (Exception e){
            e.printStackTrace();
        }
    }
    
    

}
