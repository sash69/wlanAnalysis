/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package wlananalysis;

import java.sql.Date;
import java.sql.Timestamp;

/**
 *
 * @author Alja
 */
public class ProbeRequest {
    private long index;
    private Date date;
    private Timestamp timestamp;
    private String sourceMAC;
    private int ssi;
    private String ssid;
    
    public ProbeRequest(Date date, Timestamp timestamp, String sourceMAC, int ssi, String ssid)
    {
        this.date = date;
        this.timestamp = timestamp;
        this.sourceMAC = sourceMAC;
        this.ssi = ssi;
        this.ssid = ssid;
    }
    
    public ProbeRequest(long index, Date date, Timestamp timestamp, String sourceMAC, int ssi, String ssid)
    {
        this.index = index;
        this.date = date;
        this.timestamp = timestamp;
        this.sourceMAC = sourceMAC;
        this.ssi = ssi;
        this.ssid = ssid;
    }
    
    public long getIndex() {return this.index;}
    public Date getDate() {return this.date;}
    public Timestamp getTimestamp() {return this.timestamp;}
    public String getSourceMAC() {return this.sourceMAC;}
    public int getSsi() {return this.ssi;}
    public String getSSid() {return this.ssid;}
    
    public boolean equals(ProbeRequest probe)
    {        
        if (probe == null)
            return false;
        else if (probe == this)
            return true;
        
        Date probeDate = probe.getDate();
        Timestamp probeTimestamp = probe.getTimestamp();
        String probeSourceMAC = probe.getSourceMAC();
        String probeSSid = probe.getSSid();
        
        if (probeDate.compareTo(this.date) == 0 &&
             probeTimestamp.compareTo(this.timestamp) == 0 &&
             probeSourceMAC.equals(this.sourceMAC) &&
             probeSSid.equals(this.ssid))
            return true;
        else
            return false;
    }
}
