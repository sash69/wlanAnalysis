wlanAnalysis
============

Program that reads captured probe requests and displays analysis

##Dependencies:

1.  jfreechart-1.0.16 - [JFreeChart](http://www.jfree.org/jfreechart/download.html)
2.  jcommon-1.0.20 - [JCommon](http://www.jfree.org/jcommon/) - included in JFreeChart download
3.  commons-io-2.4 - [Apache Commons IO](http://commons.apache.org/proper/commons-io/)
4.  MySQL JDBC driver - part of Netbeans IDE
5.  MySQL server

###Usage

Before running application make sure that MySQL server is running and change MySQL connection variables inside Database class accordingly (if needed).

Application will then try to connect to MySQL server, search for "wlananalysis" database and create it if doesn't find one. Same goes for "requests" table inside "wlananalysis" database.

If there are is any data inside database, you can start analyzing. Otherwise you have to input data first.

###Input

Input directory structure:

/.<br>
/manufacturers<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;apple<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;samsung<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;htc<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;nokia<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;sony<br>
/*.parsed

Files inside manufacturers folder contain that manufacturer MAC address spaces, separated by new lines. Example:

00:03:93<br>
00:05:02<br>
00:0A:27<br>

.parsed file name format:

outputYYYY-MM-DD.parsed

.parsed files contain extracted data (date, timestamp, SSI, source MAC, SSID) from captured probe request frames in text form, separated by "||". You can create parsed files from tcpdump output with provided bash script - extract.sh - put the script into folder where your tcpdump creates output files.

.parsed file line example:

`2013-07-05||10:24:18.881959||-77dB||SA:00:21:5c:1e:f0:71||(AMIS-01D066)`

tcpdump usage:

`tcpdump -i ath0 subtype probe-req -G 86400 -w /path/to/folder/output%F.cap`

tcpdump will write probe request frames into files, which names are rotated every 24 hours.

###Optional: capturing probe request frames with router

Full guide can be found [here](http://www.dd-wrt.com/phpBB2/viewtopic.php?t=86912). In a nutshell:

1.  Install [DD-WRT](http://www.dd-wrt.com/site/)
2.  Prepare USB drive with an empty partition, formatted with ext-3 filesystem
3.  Login to DD-WRT and enable SSH access and USB support (services)
4.  SSH to DD-WRT (putty, ssh command in linux) with "root" username and password you chose after DD-WRT install
5.  Prepare neccessary structure:  

```
cd /sda_part1
mkdir etc opt root
mkdir /opt/lib
chmod 755 etc opt root
chmod 755 /opt/lib
cp –a /etc/* /mnt/sda_part1/etc
mount -o bind /mnt/sda_part1/etc /etc 
mount -o bind /mnt/sda_part1/opt /jffs
```

6\.  Install required libraries and opkg package manager:

```
cd /tmp
wget http://downloads.openwrt.org/snapshots/trunk/ar71xx/packages/libc_0.9.33.2-1_ar71xx.ipk
wget http://downloads.openwrt.org/snapshots/trunk/ar71xx/packages/opkg_618-5_ar71xx.ipk
ipkg install libc_0.9.33.2-1_ar71xx.ipk opkg_618-5_ar71xx.ipk
```

7\.  Set up opkg configuration file:

```
cat > /etc/opkg.conf << EOF
src/gz snapshots http://downloads.openwrt.org/snapshots/trunk/ar71xx/packages
dest root /opt 
dest ram /opt/tmp
lists_dir ext /opt/tmp/var/opkg-lists
EOF
```

8\.  Run opkg and update repository list:

```
umount /jffs
mount -o bind /mnt/sda_part1/root /tmp/root
mount -o bind /mnt/sda_part1/opt /opt
export LD_LIBRARY_PATH='/opt/lib:/opt/usr/lib:/lib:/usr/lib'
opkg update
opkg list
```

9\.  Optional - if needed - install libc library:

```
cd /tmp
wget http://downloads.openwrt.org/snapshots/trunk/ar71xx/packages/libc_0.9.33.2-1_ar71xx.ipk
opkg install libc_0.9.33.2-1_ar71xx.ipk
```

10\.  Install tcpdump:

```
opkg install tcpdump
```

11\.  Configure routers wireless interface to monitoring mode:

```
ifconfig ath0 down
iwconfg ath0 mode Monitor
ifconfig ath0 up
```

12\.  Start with capture:

```
tcpdump -i ath0 subtype probe-req -G 86400 -w /tmp/mnt/sda_part1/output%F.cap
```

13\.  Optional - configure router to load proper paths and libraries after reboot, set time (if needed) and wireless interface and start with capture. Insert script provided into DD-WRT -> Administration -> Commands and click "Save startup".

```
#!/bin/sh 

 sleep 5 
 if [ -f /mnt/sda_part1/optware.enable ]; then
 mount -o bind /mnt/sda_part1/etc /etc 
 mount -o bind /mnt/sda_part1/root /tmp/root 
 mount -o bind /mnt/sda_part1/opt /opt 
 else
 exit
 fi

 if [ -d /opt/usr ]; then
 export LD_LIBRARY_PATH='/opt/lib:/opt/usr/lib:/lib:/usr/lib' 
 export PATH='/opt/bin:/opt/usr/bin:/opt/sbin:/opt/usr/sbin:/bin:/sbin:/usr/sbin:/usr/bin'
 else
 exit
 fi

 date –s 1306280945

 sleep 2
 ifconfig ath0 down
 iwconfig ath0 mode Monitor
 ifconfig ath0 up
 sleep 2
 tcpdump -i ath0 subtype probe-req -G 86400 -w /tmp/mnt/sda_part1/output%F.cap
```
