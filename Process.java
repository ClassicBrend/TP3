import java.io.*;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

class Process implements Runnable {
    private int id;
    private String runnerID;
    
    public Process(int id, String runnerID){
        this.id = id;
        this.runnerID = runnerID;
    }

    public void run() {
String nameOfRace = null;
        final String runnerURL ="http://www.scottishhillracing.co.uk/RunnerDetails.aspx?FromSearch=true&RunnerID=" + runnerID;
        Document doc = null;
        try {
            doc = Jsoup.connect(runnerURL).timeout(5*1000).get();
        } catch (IOException ex) {
            Logger.getLogger(Process.class.getName()).log(Level.SEVERE, null, ex);
        }
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
            if(i != (ran-1))
                content += "\n";
            nameOfRace = name;
        }
        
        //System.out.print(content);
        InitialScrape.deleteOldCsv("CSVFiles/Individual/" + runnerID);
        InitialScrape.writeOutCsv("CSVFiles/Individual/" + runnerID, content);
        InitialScrape.writeOutCsv("CSVFiles/racesRan", nameOfRace);
    
    }
}