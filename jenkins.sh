#/bin/bash
ant -f build.xml
tar -zcvf Pgdt.tar.gz build/Pgdt.jar 
rm -rf build
