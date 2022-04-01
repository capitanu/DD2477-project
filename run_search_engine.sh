#!/bin/sh
java -cp build -Xmx4g src.Engine -d corpus/ -l ir22.png -p patterns.txt
