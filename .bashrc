alias runScrape="java -cp .:./jsoup-1.7.2.jar InitialScrape"
alias buildScrape="javac -cp ./jsoup-1.7.2.jar InitialScrape.java Process.java"
alias cleanDir="rm *.class"
alias delCsv="find . -name "*.csv" -type f|xargs rm -f"
