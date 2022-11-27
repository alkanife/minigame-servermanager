clear
java -jar Builder-1.0.0-jar-with-dependencies.jar debug
BUILT=built.txt
if [ ! -f "$BUILT" ]; then
    echo "No built.txt file, the builder may have had a problem, aborting"
    exit
fi
echo "Starting server"
cd server/
java -jar paper.jar
cd ../
java -jar Archiver-1.0.0-jar-with-dependencies.jar debug