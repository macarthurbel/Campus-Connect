# CampusConnect - School Management System

A complete JavaFX-based school management system with role-based dashboards for administrators, teachers, and students.

## Quick Navigation

This repository contains two versions of the CampusConnect application:

### 1. **Full Application (Recommended)** 📦
**Location**: `javafx-school-dashboard-backend/javafx-school-dashboard-connected/`

- ✅ Complete JavaFX frontend UI
- ✅ Integrated backend services
- ✅ SQLite database with automatic initialization
- ✅ Data Access Objects (DAO) layer
- ✅ Role-based authentication and dashboards
- ✅ Ready-to-use out of the box

**[👉 Go to Full Application README](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md)**

---

### 2. **Frontend Only** 🎨
**Location**: `javafx-school-dashboard/`

- ✅ JavaFX user interface components
- ✅ UI styling and layout
- ✅ Dashboard factory patterns
- ⚠️ No database or backend services
- ⚠️ For UI development and learning

**[👉 Go to Frontend README](javafx-school-dashboard/README.md)**

---

## Which Version Should I Use?

### Choose the **Full Application** if you want to:
- Run a complete, working school management system
- Access role-based dashboards (admin, teacher, student)
- Store and manage data
- Get up and running quickly

### Choose the **Frontend Only** if you want to:
- Learn JavaFX fundamentals
- Develop and customize UI components
- Integrate with your own backend
- Study the DashboardFactory pattern

---

## 5-Minute Quick Start

### Prerequisites
- Java JDK 17+
- Maven 3.9+

### Install & Run (Full Application)

```bash
# Navigate to the full application directory
cd javafx-school-dashboard-backend/javafx-school-dashboard-connected

# Download dependencies and run
mvn clean install
mvn javafx:run
```

That's it! The application will:
1. Download all dependencies
2. Create the SQLite database automatically
3. Launch the login screen

---

## Project Overview

### Features

- **Multi-Role Support**
  - Administrator Dashboard
  - Teacher Dashboard
  - Student Dashboard

- **Core Functionality**
  - User authentication and authorization
  - Course management
  - Student enrollment
  - Grade management
  - Class scheduling
  - Classroom management

- **Technology Stack**
  - **UI Framework**: JavaFX 17+
  - **Database**: SQLite
  - **Architecture**: Layered (UI → Services → DAO → Database)
  - **Build Tool**: Maven
  - **Language**: Java 17+

### Project Structure

```
react-admin-dashboard-master/
├── README_MAIN.md (This file)
├── JAVA_JAVAFX_PROJECT_GUIDE.md (Learning guide for Java/JavaFX)
│
├── javafx-school-dashboard/
│   ├── README.md (Frontend-only setup instructions)
│   ├── pom.xml
│   └── src/
│       └── main/java/com/schooldashboard/
│           ├── App.java
│           └── ui/DashboardFactory.java
│
└── javafx-school-dashboard-backend/
    └── javafx-school-dashboard-connected/
        ├── README.md (Full application setup instructions)
        ├── pom.xml
        ├── data/ (SQLite database location)
        └── src/
            └── main/java/
                ├── com/schooldashboard/
                │   ├── App.java
                │   └── ui/DashboardFactory.java
                ├── dao/ (Data Access Layer)
                ├── models/ (Domain Models)
                ├── services/ (Business Logic)
                └── utils/ (Database & Utilities)
```

---

## Common Tasks

### Run the Full Application
```bash
cd javafx-school-dashboard-backend/javafx-school-dashboard-connected
mvn javafx:run
```

### Build Without Running
```bash
mvn clean install
# or for faster compilation
mvn -DskipTests compile
```

### Check Java/Maven Versions
```bash
java -version    # Should be 17+
mvn -version     # Should be 3.9+
```

### Clean and Rebuild
```bash
mvn clean
mvn install
mvn javafx:run
```

### Reset Database (Delete and Recreate)
```bash
# Navigate to the full application directory
cd javafx-school-dashboard-backend/javafx-school-dashboard-connected
# Delete the database file
rm data/projet_java.db
# Run the app - it will recreate the database
mvn javafx:run
```

---

## Getting Started Guides

### 📘 Learning Java & JavaFX?
Read the **[Java & JavaFX Complete Guide](JAVA_JAVAFX_PROJECT_GUIDE.md)** for comprehensive tutorials on:
- Java fundamentals and OOP
- JavaFX framework
- Project architecture
- Database design
- Best practices

### 🚀 Just Want to Run It?
Follow the **5-Minute Quick Start** section above, then check the full application [README](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md).

### 🛠️ Troubleshooting Issues?
- **Full Application**: See [Troubleshooting Guide](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md#troubleshooting)
- **Frontend Only**: See [Troubleshooting Guide](javafx-school-dashboard/README.md#troubleshooting)

---

## System Requirements

| Requirement | Minimum | Recommended |
|---|---|---|
| Java | JDK 17 | JDK 21+ |
| Maven | 3.9 | 3.9+ |
| RAM | 1 GB | 2 GB+ |
| Disk Space | 300 MB | 1 GB |
| OS | Windows/Mac/Linux | Windows 10+, macOS 10.14+, Ubuntu 20.04+ |

---

## Installation Verification

After installation, verify everything is set up correctly:

```bash
# Check Java installation
java -version
# Should output: java version "17.X.X" or higher

# Check Maven installation
mvn -version
# Should output: Maven 3.9.X or higher

# Test Maven project build
cd javafx-school-dashboard-backend/javafx-school-dashboard-connected
mvn clean compile
# Should end with: BUILD SUCCESS
```

---

## Database Information

### Full Application Only

- **Type**: SQLite (serverless, file-based)
- **Location**: `data/projet_java.db` (created automatically)
- **Initialization**: Automatic on first run
- **Tables Created**: 8 core tables (students, teachers, courses, grades, etc.)

---

## Key Dependencies

```xml
<!-- JavaFX (UI Framework) -->
<dependency>
    <groupId>org.openjfx</groupId>
    <artifactId>javafx-controls</artifactId>
    <version>17.0.10</version>
</dependency>

<!-- SQLite JDBC (Database) -->
<dependency>
    <groupId>org.xerial</groupId>
    <artifactId>sqlite-jdbc</artifactId>
    <version>3.49.1.0</version>
</dependency>
```

---

## Documentation

- **[JAVA_JAVAFX_PROJECT_GUIDE.md](JAVA_JAVAFX_PROJECT_GUIDE.md)** - Comprehensive learning guide
- **[Full App README](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md)** - Complete setup and reference
- **[Frontend README](javafx-school-dashboard/README.md)** - UI-only version documentation

---

## Troubleshooting Quick Links

- **"Java: command not found"** → Install Java JDK 17+
- **"Maven: command not found"** → Install Maven 3.9+
- **"JavaFX library not found"** → Run `mvn clean install -U`
- **"Cannot start application"** → Check [Full App Troubleshooting](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md#troubleshooting)
- **Database issues** → Ensure `data/` directory exists or run `mkdir data`

---

## Development Workflow

### 1. **First Time Setup**
```bash
mvn clean install
```

### 2. **Development Cycle**
```bash
mvn javafx:run
# Make code changes, save, and restart the app
```

### 3. **Testing & Verification**
```bash
mvn -DskipTests compile    # Quick compilation check
mvn compile                 # Full compilation
```

### 4. **Production Build**
```bash
mvn clean install
# JAR file created in: target/javafx-school-dashboard-1.0.0.jar
```

---

## Support & Contact

For issues, questions, or contributions:
1. Check the [Troubleshooting](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md#troubleshooting) section
2. Review the [Learning Guide](JAVA_JAVAFX_PROJECT_GUIDE.md)
3. Contact the development team

---

## Version Information

| Component | Version |
|---|---|
| Java | 17+ |
| Maven | 3.9+ |
| JavaFX | 17.0.10 |
| SQLite JDBC | 3.49.1.0 |
| Project Version | 1.0.0 |

---

## License & Credits

CampusConnect School Management System - A comprehensive JavaFX application for educational institution management.

**Last Updated**: April 2026

---

**Ready to get started?** 🚀

→ **[Go to Full Application README](javafx-school-dashboard-backend/javafx-school-dashboard-connected/README.md)**

→ **[Read the Learning Guide](JAVA_JAVAFX_PROJECT_GUIDE.md)**
