rem mongoexport.exe -d cdb -c companies -o companies.json
mongoimport -d cdb -c companies --drop --stopOnError --file companies.json
rem mongoexport.exe -d cdb -c computers -o computers.json
mongoimport -d cdb -c computers --drop --stopOnError --file computers.json