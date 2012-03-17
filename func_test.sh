#!/bin/bash
for c in $(ls src/cymbol/test/functional/should_run/*.cymbol)
do
    echo
    echo "======"$c"======"; 
    java -jar cymbol.jar $c; 
    echo
done
