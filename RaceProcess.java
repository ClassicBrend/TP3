import java.io.*;
import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

class RaceProcess implements Runnable {
    private int id;
    private String raceID;
    
    public RaceProcess(int id, String raceID){
        System.out.println("RACE PROCESS - RACEID = " + raceID);
        this.id = id;
        this.raceID = raceID;
    }

    public void run() {
        Main.raceCounter.incrementAndGet();
   
        Document doc = null;
        final String url = "http://www.scottishhillracing.co.uk/RaceDetails.aspx?";
 
        
        try {
            doc = Jsoup.connect(url+"RaceID="+raceID).timeout(Main.timeOutPeriod).ignoreHttpErrors(true).get();

        } catch (IOException ex) {
            Main.lblError.setText(ex.getMessage());
        }
        
        Element raceName = doc.getElementById("lblRaceName");
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
        ven = ven.replace(",", "");
//        ven = ven.replace("-", "");
        
        String dist = distance.text();
        dist = dist.replace("km","");
        dist.trim();
        String cl = climb.text();
        cl = cl.replace("m", "");
        cl.trim();
        String race = raceName.text();
        race = race.replace(",","");
        race = race.replace("  "," ");

        
        
        
        String mensTime = mensRecordTime.text();
//        mensTime = mensTime.replace(" -", "");
        String womensTime = womensRecordTime.text();
//        womensTime = womensTime.replace(" -","");

        Main.writeOutCsv("CSVFiles/RaceList/races",raceID + "," + race + "," + cleanString(ven) + "," + cleanString(dist) + "," + cleanString(cl) + "," + cleanString(mensTime) + "," + cleanString(mensRecordYear.text())
         + "," +  cleanString(mensRecordName.text()) + "," + cleanString(womensTime) + "," + cleanString(womensRecordYear.text()) + "," + cleanString(womensRecordName.text()));
        Main.raceCounter.decrementAndGet();
        if(Main.raceCounter.get() == 0){
            Main.btnDatabase.setEnabled(true);
            Main.btnDrop.setEnabled(true);
            Main.scrapeCompleted = true;
            Main.btnStart.setText("Visit website");
            Main.lblScrape.setText("Scrape completed");
            //Main.uploadTables();
        }      
    }
    
    public String cleanString(String s){
        s = s.replace("-","");
        s = s.replace("(","");
        s = s.replace(")","");
        s = s.trim();        
        return s;
    }

}