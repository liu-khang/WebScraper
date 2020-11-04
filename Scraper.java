import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class Scraper {
    private static void InitializeDB (Date tTime, HashMap<String, URLInfo> ss) throws IOException {
        Scanner dbScanner = new Scanner(new File("C:\\Users\\Midnight Toker\\Desktop\\Test Text Files\\storage.txt")).useDelimiter("\\|");
        ArrayList<URLInfo> restoreList = new ArrayList<>(); // Array List of URLs from persistent storage to be restored

        while (dbScanner.hasNext()) {
            tTime = new Date(System.currentTimeMillis());

            String urlLink = "", cType = "", lModified = "", cEncoding = "";
            int cLength = 0;
            long expir = 0;

            for (int i = 0; i < 6; i++) {
                if (i == 0 && dbScanner.hasNext()) {
                    urlLink = dbScanner.next();
                }
                if (i == 1 && dbScanner.hasNext()) {
                    cType = dbScanner.next();
                }
                if (i == 2 && dbScanner.hasNextInt()) {
                    cLength = dbScanner.nextInt();
                }
                if (i == 3 && dbScanner.hasNext()) {
                    lModified = dbScanner.next();
                }
                if (i == 4 && dbScanner.hasNextLong()) {
                    expir = dbScanner.nextLong();
                }
                if (i == 5 && dbScanner.hasNextLine()) {
                    cEncoding = dbScanner.nextLine();
                }
            }
            restoreList.add(new URLInfo(urlLink, cType, cLength, lModified, expir, cEncoding));
        }
        for (int i = 0; i < restoreList.size(); i++) {
            ss.put(restoreList.get(i).getURL(), restoreList.get(i));
        };
        dbScanner.close();
    }

    // Main code
    public static void main(String[] args) throws Exception, MalformedURLException, IOException {
        HashMap<String, URLInfo> storageStructure = new HashMap<>(); // Storage structure to record information from desired inputs
        ArrayList<Transaction> transactionList = new ArrayList<>(); // Array List of transactions to be printed to the transaction log

        String oDirectory = ""; // Stores directory path for output location
        String tType = "";
        Date tTime = new Date();

        // Initializes DB from persistent storage for continuity purposes
        InitializeDB(tTime, storageStructure);

        // Shows a list of flags and their input format/functionality
        System.out.println("Flags: ");
        System.out.println("-d [desired absolute destination path for output files in quotations]");
        System.out.println("-o [all] or [Specific keyword in quotations]");
        System.out.println("-i [absolute file path of input text file in quotations]");
        System.out.println("-exit" + "\n");

        String command ="";
        while (!command.equals("-exit")) {
            Scanner inputScanner = new Scanner(System.in);
            System.out.println("Please entered flag command (before any other flag is used, -d must be used to set output directory): ");
            String flag = inputScanner.next(); // Stores what flag the user inputted
            String argument = inputScanner.nextLine(); // Stores what flag specific command the user inputted
            command = flag; // Variable used to check for -exit flag; signals program to terminate

            // Checks if flag/command entered by the user is valid
            if (!flag.equals("-i") && !flag.equals("-o") && !flag.equals("-d") && !flag.equals("-exit")) {
                System.err.println("Not a valid flag command." + "\n");
            }

            // -d flag; allows user to choose directory where output files will be stored
            if (flag.equals("-d")) {
                if (!argument.equals("") && !argument.equals(" ")) {
                    oDirectory = argument.substring(2, argument.length() - 1);
                    System.out.print("\n");
                } else {
                    System.err.println("-d requires the desired output destination as the second argument." + "\n");
                }
            }

            // -i flag; processes URLs added to the input file by the user and records them in the storage structure
            if (flag.equals("-i")) {
                ArrayList<URLInfo> urlList = new ArrayList<>(); // Array List of URLs and their information
                if (!argument.equals("") && !argument.equals(" ")) {
                    Path iPath = Paths.get(argument.substring(2, argument.length() - 1));
                    Scanner webLink = new Scanner(new File(iPath.toString()));
                    Format dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String webpage = null;

                    // Create a URL from the specified address, open a connection to it,
                    // and then display information about the URL.
                    while (webLink.hasNextLine()) {
                        tTime = new Date(System.currentTimeMillis());
                        tType = "ADD";

                        String webLine = webLink.nextLine();

                        URL url = new URL(webLine);
                        URLConnection connection = url.openConnection();

                        urlList.add(new URLInfo(connection.getURL().toExternalForm(), connection.getContentType(), connection.getContentLength(), dateFormat.format(connection.getLastModified()), connection.getExpiration(), connection.getContentEncoding()));
                    }
                    webLink.close();
                    System.out.print("\n");
                } else {
                    System.err.println("-i requires the input file path in quotations.");
                }

                // Updates storage structure with new URL info from input.txt,
                // also makes sure duplicates don't get stored and recorded in the transaction log.
                FileWriter db = new FileWriter("C:\\Users\\Midnight Toker\\Desktop\\Test Text Files\\storage.txt");

                for (int i = 0; i < urlList.size(); i++) {
                    if(!storageStructure.containsKey(urlList.get(i).getURL())) {
                        transactionList.add(new Transaction(tTime, tType, urlList.get(i).getURL()));
                    }
                    storageStructure.put(urlList.get(i).getURL(), urlList.get(i));
                }
                for (Map.Entry<String, URLInfo> entry : storageStructure.entrySet()) {
                    db.write(entry.getValue().getURL() + "|" + entry.getValue().getContentType() + "|" + entry.getValue().getContentLength() + "|" + entry.getValue().getLastModified() + "|" + entry.getValue().getExpiration() + "|" + entry.getValue().getContentEncoding());
                    db.write("\r\n");
                }
                db.close();
            }

            // Prints out processed data to output file
            if (flag.equals("-o")) {
                Path oPath = Paths.get(oDirectory, "output.txt");
                FileWriter sOutput = new FileWriter(oPath.toString());
                tType = "PRINT";
                tTime = new Date(System.currentTimeMillis());

                for (Map.Entry<String, URLInfo> entry : storageStructure.entrySet()) {
                    sOutput.write("URL: " + entry.getValue().getURL() + "|" + "Content Type: " + entry.getValue().getContentType() + "|" + "Content Length: " + entry.getValue().getContentLength() + "|" + "Last Modified: " + entry.getValue().getLastModified() + "|" + "Expiration: " + entry.getValue().getExpiration() + "|" + "Content Encoding: " + entry.getValue().getContentEncoding());
                    sOutput.write("\r\n");
                    transactionList.add(new Transaction(tTime, tType, entry.getValue().getURL()));
                }
                System.out.print("\n");
                sOutput.close();
            }
        }

        //Prints transactions done in the session to transaction log within user-specified file path (from -d flag)
        Path tPath = Paths.get(oDirectory, "transactionLog.txt");
        FileWriter tLogWriter = new FileWriter(tPath.toString());
        for (int i = 0; i < transactionList.size(); i++) {
            tLogWriter.write(transactionList.get(i).getTransactionTime() + "|" + transactionList.get(i).transactionType + "|" + transactionList.get(i).getUrl());
            tLogWriter.write("\r\n");
        }
        tLogWriter.close();
    }

}