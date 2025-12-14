This is a complete and excellent set of documentation. I have combined the existing information with the required "DOCUMENTATION" sections (User manual, Reflection, and Screenshots) to create a single, professional **`README.md`** file.

-----

# 📚 Library Management System (Patron-Manager)

This is a desktop application developed using **JavaFX** and **Maven** for the management of library assets (Books, Categories) and patron records, including the tracking of book loans and returns (Transactions).

The application uses a modular architecture with **Model-View-Controller (MVC)** design and a **Data Access Object (DAO)** pattern for clean separation of concerns.

## ✨ Features

  * **Category Management:** Create, read, update, and delete (CRUD) book categories.
  * **Book Management:** CRUD for books, linking them to specific categories via a ComboBox.
  * **Patron Management:** CRUD for library users (patrons).
  * **Transaction Tracking:** Record book loans and returns, ensuring books are marked as unavailable/available.
  * **Database Integration:** Uses a SQLite database for persistent storage (managed by `DBConnector`).
  * **Global Refresh:** Instantly update all displayed data from the database using a centralized event manager.
  * **Modern UI:** Styled using external JavaFX CSS for a clean, professional look.

## 🛠️ Technology Stack

  * **Language:** Java 21+
  * **Build Tool:** Apache Maven
  * **Framework:** JavaFX 21+
  * **Database:** SQLite (embedded)

## 📦 Project Structure

The core functionality is organized as follows:

```
src/main/java/
├── application/           (Main JavaFX Application entry point)
├── configuration/         (DBConnector, GlobalEventManager, MainController)
├── library/controllers/   (BookController, PatronController, CategoryController, etc.)
├── library/models/        (Book, Patron, Category model classes)
└── library/utilities/     (DAO classes for database interaction)

src/main/resources/
├── ui/                    (Main FXML files)
│   ├── MainApplication.fxml
│   └── pages/             (Embedded FXML for tabs)
│       └── ...View.fxml
├── styles/
│   └── application.css    (External CSS theme)
└── library.db             (SQLite database file)
```

## ⚙️ Setup and Running the Application

### Prerequisites

You must have the following software installed:

  * **Java Development Kit (JDK) 21 or newer**
  * **Apache Maven** (Configured in your system environment path)

### Step 1: Database Setup

The application is configured to use a local SQLite file named `library.db` located in the `src/main/resources/` directory.

Before running, ensure this file exists and contains the necessary tables (`categories`, `patrons`, `books`, `transactions`). If your database is empty, you may need a script to create the initial tables.

### Step 2: Build and Run

Since this is a modular JavaFX application managed by the Maven FXML Plugin, it can be run directly from the command line.

Navigate to the root directory of the project (`/patron-manager`):

```bash
# Clean, compile all code, and package the application
mvn clean package

# Run the JavaFX application
mvn javafx:run
```

The main application window, titled "Library Management System," should launch, displaying the four primary navigation tabs.

## 📝 Usage Notes

  * **Category ID:** Category IDs must be manually input when creating a new category and must be unique.
  * **Data Consistency:** The application implements Foreign Key constraints to prevent deleting a **Category** that is currently assigned to a **Book**, or deleting a **Patron** that has an outstanding **Loan**.
  * **Global Refresh:** Click the **`⟳ Refresh Data`** button in the header bar to force all tabs to reload their data from the database, useful for syncing changes across tabs or external database modifications.

-----

# 📖 DOCUMENTATION

## User Manual / Guide

The application is organized into four main tabs, accessible via the `TabPane` in the center of the window:

### 1\. Tags: Categories

This tab is used to manage the types of books in the library (e.g., Fiction, Science, History).

  * **Create:** Clear the selection, input a unique **Category ID** and **Category Name**, then click "Save New Category."
  * **Update:** Select a category from the table, edit the **Category Name** field on the left, and click "Update Category."
  * **Delete:** Select a row from the table and click the red "Delete Selected Category" button. (Deletion is blocked if books are linked to the category.)

### 2\. Records: Books

This tab manages the library's inventory.

  * **Create:** Input Title, Author, ISBN, Publication Year, and select an existing **Category** from the dropdown list. Click "Save New Book."
  * **Update:** Select a book from the table and modify any details on the left, including re-selecting the Category. Click "Update Book."

### 3\. Records: Patrons

This tab manages the library user accounts.

  * **Create:** Input details (Name, Email, Phone, Address). Patron ID is typically auto-generated or manually input if required by the controller logic. Click "Save New Patron."

### 4\. Transactions: Record & Return

This tab handles book lending and returns.

  * **Record Loan:** Select the **Book** and the **Patron** from the respective combo boxes. Click "Record New Loan." The book will then appear in the table below and be marked unavailable in the Book tab.
  * **Record Return:** Select an outstanding loan from the table at the bottom. Click "Record Return." The loan is removed from the table, and the book's status is reset.

## Short Reflection on the Design and Implementation Process

The development process emphasized establishing a robust and maintainable foundation through adherence to the **Model-View-Controller (MVC)** pattern. The separate **DAO (Data Access Object)** layer ensured that all database logic was isolated, making the application easy to switch to a different database (e.g., MySQL or PostgreSQL) with minimal changes to the controller logic.

Initial hurdles involved **FXML synchronization**, where minor mismatches between FXML `fx:id` attributes and Java controller variable names caused repeated `NullPointerExceptions`. The final structural fix involved implementing a **Global Event Manager (Singleton)** using `SimpleBooleanProperty` to create a decoupled communication channel between the main application bar and all the sub-controllers. This approach allows any controller to instantly synchronize its data without direct coupling to other controllers, enhancing stability and user experience through the one-click refresh feature.

## Screenshots of Running System

| Description | Screenshot |
| :--- | :--- |
| **Main Window/Category View** (Showing general layout and tabs) | ![Screenshot of the Main Category Tab](screenshots/1-view.png) |
| **Book View** (Showing Book list, input form, and Category ComboBox) | ![Screenshot of the Book Management Tab](screenshots/2-view.png) |
| **Transaction View** (Showing Loan/Return functionality) | ![Screenshot of the Transaction Tracking Tab](screenshots/3-view.png) |