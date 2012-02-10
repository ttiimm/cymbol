#!/bin/bash
for c in $(ls src/cymbol/test/*.cymbol)
do
    echo
    echo "======"$c"======"; 
    java -jar cymbol.jar $c; 
    echo
done
