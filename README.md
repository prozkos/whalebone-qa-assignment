# Whalebone QA Assignment Test Suite

Test automation suite for Whalebone QA Assignment using **Java, TestNG, RestAssured, and Playwright**.

## Tech Stack

- **Java 25**
- **TestNG 7.8.0** - Test framework
- **RestAssured 5.4.0** - API testing
- **Playwright 1.40.0** - Web automation and scraping
- **Jackson 2.16.0** - JSON parsing
- **Maven** - Build and dependency management

## Project Structure

```
whalebone-qa-assignment/
├── pom.xml                           # Maven dependencies
├── README.md                         # This file
├── testng.xml                        # TestNG settings
└── src/
    └── test/
        └── java/
            └── com/
                └── whalebone/
                    └── tests/
                        └── NHLTeamsTestSuite.java                        # API and web scrape test class
                        └── UitestingplaygroundSampleAppTestSuite.java    # UI testing playground Sample App test class
                        └── UitestingplaygroundTestSuite.java             # UI testing playground Load Delay and Progress Bar test class
```

## Setup & Installation

### Prerequisites

- **Java 25** or higher
- **Maven 3.6+**
- Internet connection

### Installation Steps

```bash
# 1. Clone the repository
git clone https://github.com/prozkos/whalebone-qa-assignment.git
cd whalebone-qa-assignment

# 2. Install Maven dependencies
mvn clean install -DskipTests

# 3. Install Playwright browsers
mvn exec:java -e -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install"
```

## Running Tests

### Run All Tests
```bash
mvn clean test
```

### Run with Verbose Output
```bash
mvn clean test -X
```

## Test Reports

TestNG generates HTML reports after test execution:

```bash
# View test results
open target/surefire-reports/index.html
```

## Contact

**Author**: Peter Rozkos
**Repository**: https://github.com/prozkos/whalebone-qa-assignment  
**Assignment**: Whalebone QA Assignment

## License

This project is created for the Whalebone QA Assignment evaluation.
