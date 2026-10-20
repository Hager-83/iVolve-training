# Lab 1: Calculator Maven Application

## Overview

This lab demonstrates a simple Java calculator project managed with Maven. The project includes calculator operations and a JUnit 5 test suite.

![lab](../images/lab.png)

## Prerequisites

The project requires Java 17 and Maven. Check that both are installed:

```bash
java -version
mvn -version
```

![Java and Maven versions](../images/1.png)

## Run the Unit Tests

From the `CalculatorMavenApp` directory, run:

```bash
mvn test
```

### Test Results

The Maven test run completed successfully:

- Test suite: `com.example.calculator.CalculatorTest`
- Tests run: 5
- Failures: 0
- Errors: 0
- Skipped: 0
- Build status: `BUILD SUCCESS`

![Unit test results](../images/2.png)

## Build the Application

Create the executable JAR with:

```bash
mvn package
```

The build creates `target/calculator.jar`.

![Maven package build](../images/3.png)

The generated artifact is shown below:

![Generated JAR file](../images/4.png)

## Run the Application

Run the JAR from the `CalculatorMavenApp` directory:

```bash
java -jar target/calculator.jar
```

Enter two numbers when prompted. The application displays their sum, difference, product, and quotient.

![Calculator application running](../images/lab1.gif)

## Conclusion

Successfully tested, built, and packaged the Java calculator application using Maven. All five unit tests passed, and the generated JAR file was executed successfully.

This lab provided hands-on experience with the basic Maven workflow, from testing source code to packaging and running a Java application.