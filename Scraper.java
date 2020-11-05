import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.*;

public class Scraper {
    // Initializes DB from persistent storage for continuity purposes
    private static void InitializeDB (String storagePath, HashMap<String, URLInfo> ss) throws IOException {
        Path sPath = Paths.get(storagePath, "storage.txt");
        Scanner dbScanner = new Scanner(new File(sPath.toString())).useDelimiter("\\|");
        ArrayList<URLInfo> restoreList = new ArrayList<>(); // Array List of URLs from persistent storage to be restored

        while (dbScanner.hasNext()) {
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
        for (URLInfo urlInfo : restoreList) {
            ss.put(urlInfo.getURL(), urlInfo);
        }
        dbScanner.close();
    }

    public static BufferedReader read(String url) throws Exception {

        return new BufferedReader(
                new InputStreamReader(
                        new URL(url).openStream()));

    } // read


    public static BufferedImage fetchImageFromURL (URL url) {
        BufferedImage image = null;
        try {
            // Read from a URL
            image = ImageIO.read(url);
        } catch (IOException e) {
        } // catch
        return image;
    } // fetchImageFromURL

    // Main code
    public static void main(String[] args) throws Exception {
        HashMap<String, URLInfo> storageStructure = new HashMap<>(); // Storage structure to record information from desired inputs
        ArrayList<Transaction> transactionList = new ArrayList<>(); // Array List of transactions to be printed to the transaction log

        String oDirectory = ""; // Stores directory path for output location
        String sDirectory; // Stores directory path for storage location
        String tType = "";
        Date tTime = new Date(); // Keeps track of the time a transaction is done

        //Asks user to choose directory path for storage.txt to be created or where it already exists
        Scanner storageSC = new Scanner(System.in);
        System.out.println("Please enter the folder path in quotations of where your storage.txt is located or where you would like it to be created: ");

        sDirectory = storageSC.nextLine();
        sDirectory = sDirectory.substring(1, sDirectory.length() - 1);
        System.out.print("\n");

        // Initializes DB from persistent storage for continuity purposes
        InitializeDB(sDirectory, storageStructure);

        // Shows a list of flags and their input format/functionality
        System.out.println("Flags: ");
        System.out.println("-d [desired absolute destination path for output files in quotations]");
        System.out.println("-o");
        System.out.println("-i [absolute file path of input text file in quotations]");
        System.out.println("-exit" + "\n");

        String exitSignal =""; // Variable used to check for -exit flag; signals program to terminate
        while (!exitSignal.equals("-exit")) {
            Scanner inputScanner = new Scanner(System.in);
            System.out.println("Please enter flag command (before any other flag is used, -d must be used to set output directory): ");
            String flag = inputScanner.next(); // Stores what flag the user inputted
            String argument = inputScanner.nextLine(); // Stores what flag specific command the user inputted
            exitSignal = flag; // Variable used to check for -exit flag; signals program to terminate

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
                Path sPath = Paths.get(sDirectory, "storage.txt");
                FileWriter db = new FileWriter(sPath.toString());

                for (URLInfo urlInfo : urlList) {
                    if (!storageStructure.containsKey(urlInfo.getURL())) {
                        transactionList.add(new Transaction(tTime, tType, urlInfo.getURL()));
                    }
                    storageStructure.put(urlInfo.getURL(), urlInfo);
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
                    int lineCount = 0;

                    String webpage = entry.getValue().getURL(); // The URL in String format used to check file type
                    String urlFileName = webpage.substring(webpage.lastIndexOf("/") + 1); // Used to extract last part of URL for file saving purposes
                    URL url = new URL(webpage); // URL format of URL

                    // Outputs image file of a certain type from url to output directory if it is an image
                    if(entry.getValue().getContentType().equals("image/jpeg")) {
                        Path imgPath = Paths.get(oDirectory, urlFileName);
                        File outputImageFile = new File(imgPath.toString());
                        ImageIO.write(fetchImageFromURL(url), "jpg", outputImageFile);

                        //Retrieves file size
                        long bytes = Files.size(imgPath);

                        // Writes URL information to output file
                        sOutput.write("URL: " + entry.getValue().getURL() + "|" + "File size: " + String.format("%,d bytes", bytes) + "|" + "Content Type: " + entry.getValue().getContentType() + "|" + "Content Length: " + entry.getValue().getContentLength() + "|" + "Last Modified: " + entry.getValue().getLastModified() + "|" + "Expiration: " + entry.getValue().getExpiration() + "|" + "Content Encoding: " + entry.getValue().getContentEncoding());
                        sOutput.write("\r\n");
                    }
                    // Reads in txt files line by line then saves it
                    else if(entry.getValue().getContentType().equals("text/plain")){
                        Path pPath = Paths.get(oDirectory, (urlFileName));
                        FileWriter pageWriter = new FileWriter(pPath.toString());
                        BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));

                        String line = reader.readLine();
                        while (line != null) {
                            pageWriter.write(line);
                            lineCount++;
                            line = reader.readLine();
                        }
                        reader.close();
                        pageWriter.close();

                        //Retrieves file size
                        long bytes = Files.size(pPath);

                        // Writes URL information to output file
                        sOutput.write("URL: " + entry.getValue().getURL() + "|" + "Line count: " + lineCount + "|" + "File size: " + String.format("%,d bytes", bytes) + "|" + "Content Type: " + entry.getValue().getContentType() + "|" + "Content Length: " + entry.getValue().getContentLength() + "|" + "Last Modified: " + entry.getValue().getLastModified() + "|" + "Expiration: " + entry.getValue().getExpiration() + "|" + "Content Encoding: " + entry.getValue().getContentEncoding());
                        sOutput.write("\r\n");
                    }
                    // Reads in pdf or docx files from a URL then saves it to the output directory
                    else if(entry.getValue().getContentType().equals("application/pdf")){
                        Path pPath = Paths.get(oDirectory, (urlFileName));
                        try (BufferedInputStream in = new BufferedInputStream(url.openStream());
                             FileOutputStream fileOutputStream = new FileOutputStream(pPath.toString())) {
                            byte dataBuffer[] = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                                fileOutputStream.write(dataBuffer, 0, bytesRead);
                            }
                        } catch (IOException e) {
                            // handle exception
                        }

                        //Retrieves file size
                        long bytes = Files.size(pPath);

                        // Writes URL information to output file
                        sOutput.write("URL: " + entry.getValue().getURL() + "|" + "File size: " + String.format("%,d bytes", bytes) + "|" + "Content Type: " + entry.getValue().getContentType() + "|" + "Content Length: " + entry.getValue().getContentLength() + "|" + "Last Modified: " + entry.getValue().getLastModified() + "|" + "Expiration: " + entry.getValue().getExpiration() + "|" + "Content Encoding: " + entry.getValue().getContentEncoding());
                        sOutput.write("\r\n");
                    }
                    else {
                        urlFileName = urlFileName + ".html";
                        Path pPath = Paths.get(oDirectory, (urlFileName));
                        FileWriter pageWriter = new FileWriter(pPath.toString());
                        BufferedReader reader = read(webpage);

                        String line = reader.readLine();
                        while (line != null) {
                            pageWriter.write(line);
                            lineCount++;
                            line = reader.readLine();
                        }
                        pageWriter.close();
                        reader.close();

                        //Retrieves file size
                        long bytes = Files.size(pPath);

                        // Writes URL information to output file
                        sOutput.write("URL: " + entry.getValue().getURL() + "|" + "Line count: " + lineCount + "|" + "File size: " + String.format("%,d bytes", bytes) + "|" + "Content Type: " + entry.getValue().getContentType() + "|" + "Content Length: " + entry.getValue().getContentLength() + "|" + "Last Modified: " + entry.getValue().getLastModified() + "|" + "Expiration: " + entry.getValue().getExpiration() + "|" + "Content Encoding: " + entry.getValue().getContentEncoding());
                        sOutput.write("\r\n");
                    }

                    // Adds to transaction list
                    transactionList.add(new Transaction(tTime, tType, entry.getValue().getURL()));
                }
                System.out.print("\n");
                sOutput.close();
            }
        }

        //Prints transactions done in the session to transaction log within user-specified file path (from -d flag)
        Path tPath = Paths.get(oDirectory, "transactionLog.txt");
        FileWriter tLogWriter = new FileWriter(tPath.toString());
        for (Transaction transaction : transactionList) {
            tLogWriter.write(transaction.getTransactionTime() + "|" + transaction.transactionType + "|" + transaction.getUrl());
            tLogWriter.write("\r\n");
        }
        tLogWriter.close();
        storageSC.close();
    }

}