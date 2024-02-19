# lob

*Limit Order Book implemented in Java*

## Project structure

The source code are mainly in `app/src/main/java/lob` with several data structures being used written in `app/src/main/java/lob/ds`. The test code can be found at `app/src/test/java/lob` and there is one example which measures the performance of the LOB in `app/src/main/java/lob/example/App.java`.

## Running the tests 

This project uses gradle as its build tool so to run the tests you can do 

```sh
# On unix systems
./gradlew test

# On windows
./gradlew.bat test
```