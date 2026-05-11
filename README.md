# REST Assured API Automation Framework

BDD API automation framework built with Java, REST Assured, Cucumber, TestNG and Gradle.

## Objective

Provide a scalable and reusable API automation framework using BDD practices and REST API validations.

## Technologies

- Java
- REST Assured
- Cucumber
- TestNG
- Gradle
- Gson
- Log4j

## Project Structure

```text
src
 └── test
      ├── java
      │    └── automation
      │         ├── cucumber
      │         │    ├── features
      │         │    ├── hook
      │         │    ├── runner
      │         │    └── steps
      │         ├── generic
      │         └── petstore
      │              ├── collections
      │              └── constants
      └── resources
           ├── jsonBody
           ├── jsonEstructure
           └── jsonHeader
```

## Run Tests

### Windows (PowerShell)

```powershell
.\gradlew test
```

### Git Bash / Linux / macOS

```bash
./gradlew test
```

## Features

- Dynamic query parameters
- Header management
- JSON-driven test data
- Reusable request methods
- BDD scenarios with Cucumber
- API validations
- Logging support

## Future Improvements

- CI/CD integration
- Docker execution
- Parallel execution
- Allure reports
- Environment management
