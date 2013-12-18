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
        this.id = id;
        this.raceID = raceID;
    }

    public void run() {
        Main.runnerCounter.incrementAndGet();
   
        Document doc = null;
        final String url = "http://www.scottishhillracing.co.uk/RaceDetails.aspx?";
 
        
        try {
            doc = Jsoup.connect(url+"RaceID="+raceID).timeout(5*1000).get();

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
        
        Main.deleteOldCsv(Main.jarPath+"CSVFiles/RaceList/"+raceID);
        
        Main.writeOutCsv("CSVFiles/RaceList/"+raceID, ven + "," + distance.text() + "," + climb.text() + "," + mensRecordTime.text() + "," + mensRecordYear.text()
         + "," + womensRecordTime.text() + "," + womensRecordYear.text() + "," + womensRecordName.text());

        if(Main.runnerCounter.get() == 0){
            System.out.println("Scrape properly finished");
        }
        
        
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