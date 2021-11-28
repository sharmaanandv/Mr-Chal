#Package-Challenge

##Technology Used

Java 8
Maven

## Prerequisite

Java 8
Maven

## How to run project
```bash
mvn clean install
java -jar package-challenge-0.0.1-SNAPSHOT.jar "<file-path>"
e.g. java -jar package-challenge-0.0.1-SNAPSHOT.jar "D:\\project\\sample-input.txt"

```

## Key Component

Application is the entry point , It will accept only one argument, Application will call Analyzer to analyze and find the output.

Analyser will read file, validate input and find if any combinations of items is suitable as per the requirement.

PackageDetails is pojo used to store package details like index, weight and cost of package.


## Debugging/ Running code
For running the code , set file path in variable args[0] in Application.main()
