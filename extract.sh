#!/bin/bash

directory="parsed"
output="parsed/outputParsed.text"
processed="parsed/processedFiles"

if [ ! -d "$directory" ]; then
	mkdir $directory
fi

for i in $(ls);do
    if [[ $i = *.cap ]];then
			filedate=${i:6:10}
			filename=$(basename "$i")
			filename="${filename%.*}"
			tcpdump -e -tttt -r $i | awk '{for(i=1;i<=NF;i++){rssi=match($i,/\-?[0-9]*dB/); source=match($i,/SA:[^:][^:]:[^:][^:]:[^:][^:]:[^:][^:]:[^:][^:]:[^:][^:]/); if(rssi || source || i==1 || i==2) {printf "%s",$i"||"}}	SSid = match($0,/t \(.*\) \[/);	if(SSid){printf "%s", substr($0,SSid+2,RLENGTH-4)}{printf "\n"}}' > ${directory}/${filename}.parsed
    fi
done
