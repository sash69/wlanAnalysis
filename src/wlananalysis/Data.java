/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wlananalysis;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import org.apache.commons.io.FilenameUtils;

/**
 *
 * @author Alja
 */
public class Data {
    
    public static ArrayList<ProbeRequest> parseSourceRequests(String sourceDirectory, ProbeRequest lastProbeRequest) 
    {
        ArrayList<ProbeRequest> probeRequestList = new ArrayList<>();
        ArrayList<ProbeRequest> differentPrWithin1Second = new ArrayList<>();
        //differentProbeRequestsWithin 1 second - ProbeRequests can be duplicatet inside 1 second but not conncurent
        //ex.: Time1, MAC1, SSID1 | Time1, MAC1, SSID2 | Time1, MAC1, SSID1
        
        try
        {
            File folder = new File(sourceDirectory);
            File[] listOfFiles = folder.listFiles();
            ArrayList<File> listOfParsedFiles = new ArrayList<>();
            
            String macRGX = "SA:\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2}:\\w{2}";
            String ssiRGX = "\\-??\\w{1,3}dB";
            
            int errorNo = 0;
            long totalPR = 0;
            
            Date fileDate, lastDate;
            
            //get date from DB-1 - in file containing last entry, entry could be recorded after "fileNameDate" (FND+1)
            //so we must parse whole last file again to check if any new entries have been added to it
            //if lastProbeRequest == null (datbase is empty), set lastDate to 1999-12-12 so every file is added to parse queue 
            SimpleDateFormat sfd = new SimpleDateFormat("yyyy-MM-dd");
            Calendar c = Calendar.getInstance();
            if (lastProbeRequest != null)
                c.setTime(sfd.parse(lastProbeRequest.getDate().toString()));
            else
                c.setTime(sfd.parse("1999-12-12"));
            c.add(Calendar.DATE, -1);
            lastDate = new Date(c.getTimeInMillis());
            
            for (File file : listOfFiles)
                if (file.isFile() && FilenameUtils.getExtension(file.getName()).equals("parsed"))
                {
                    fileDate = Date.valueOf(FilenameUtils.getBaseName(file.getName()).substring(6));
                    if (fileDate.compareTo(lastDate) == 0 || fileDate.compareTo(lastDate) > 0)
                        listOfParsedFiles.add(file);
                }
            
            if (listOfParsedFiles.isEmpty())
                throw new Exception("No .parsed file in directory!");

            for (File file : listOfParsedFiles)
            {
                BufferedReader reader = new BufferedReader(new FileReader(file));
                String line;

                Date date;
                Timestamp timestamp;
                String sourceMac;
                int ssi;
                String ssid;
                ProbeRequest currentPR;

                ProbeRequest previousPR = lastProbeRequest;
                Timestamp lastTimestampInDB = lastProbeRequest == null ? null : lastProbeRequest.getTimestamp();
                Timestamp previousTimestamp = lastProbeRequest == null ? null : lastProbeRequest.getTimestamp(); //on the beggining, previous TS in last TS in DB

                String[] requestStrings;

                while ((line = reader.readLine()) != null)
                {
                    if ((requestStrings = line.split("\\|\\|")).length != 5)
                    {
                        errorNo++;
                        continue;
                    }
                    totalPR++;
                    boolean prOK = true;
                    date = getDate(requestStrings[0]);
                    timestamp = Timestamp.valueOf(requestStrings[0]+" "+requestStrings[1].substring(0,requestStrings[1].length()-7));
                    if (requestStrings[2].matches(ssiRGX))
                        ssi = Integer.parseInt(requestStrings[2].substring(0,requestStrings[2].length()-2));
                    else
                    {
                        ssi = 0;
                        prOK = false;
                    }
                    if (requestStrings[3].matches(macRGX))
                        sourceMac = requestStrings[3].substring(3,requestStrings[3].length());
                    else
                    {
                        sourceMac = "";
                        prOK = false;
                    }
                    ssid = requestStrings[4].substring(1, requestStrings[4].length()-1);

                    if (!prOK)
                    {
                        errorNo++;
                        continue;
                    }

                    currentPR = new ProbeRequest(date, timestamp, sourceMac, ssi, ssid);

                    if (lastProbeRequest != null) //if entry exists in table
                        if (timestamp.before(lastTimestampInDB) || timestamp.equals(lastTimestampInDB)) //filter those that are already inside
                            continue;
                    if (timestamp.equals(previousTimestamp)) //filter duplicated entries within 1 second
                    {
                        boolean found = false;
                        for(ProbeRequest pr : differentPrWithin1Second)
                            if(pr.equals(currentPR))
                                found = true;
                        if (!found)
                            differentPrWithin1Second.add(currentPR);
                    }
                    else
                    {
                        probeRequestList.addAll(differentPrWithin1Second);
                        differentPrWithin1Second.clear();
                        differentPrWithin1Second.add(currentPR);
                    }
                    //previousPR = currentPR;
                    previousTimestamp = currentPR.getTimestamp();
                }
                probeRequestList.addAll(differentPrWithin1Second); //add last entries in file
                reader.close();
            }
            return probeRequestList;
        } catch (Exception e) {
            //System.out.println("parseSourceRequests failed! " + e.getMessage());
            return null;
        }
    }
    
    private static Date getDate(String dateString)
    {
        try
        {
            Date date;
            DateFormat formatter;
            formatter = new SimpleDateFormat("yyyy-MM-dd");
            java.util.Date utilDate = formatter.parse(dateString);
            date = new Date(utilDate.getTime());
            
            return date;
        } catch (Exception e)
        {
            System.out.println(e.getMessage());
            return null;
        }
    }  
}
