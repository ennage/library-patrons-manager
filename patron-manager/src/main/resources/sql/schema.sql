CREATE DATABASE PatronRecordsDB;  -- 1. Create the database
USE PatronRecordsDB;              -- 2. Use the database (Important!)

CREATE TABLE Categories(
    CategoryID VARCHAR(10) PRIMARY KEY,         -- Primary Key, ID
    CategoryName VARCHAR(20) NOT NULL UNIQUE    -- NOT NULL and UNIQUE are mandatory for the category name
);

CREATE TABLE Books(
    BookID VARCHAR(10) PRIMARY KEY,     -- Primary Key, ID
    Title VARCHAR(255) NOT NULL,        -- Title and Author are mandatory
    Author VARCHAR(50) NOT NULL,
    ISBN VARCHAR(17) UNIQUE,            -- ISBN must be unique for every book
    PublicationYear INTEGER,
    CategoryID VARCHAR(10) NOT NULL,    -- Foreign Key: Must be NOT NULL because every book MUST belong to a category
    FOREIGN KEY (CategoryID) REFERENCES Categories(CategoryID)
);

CREATE TABLE Patrons(
    PatronID VARCHAR(10) PRIMARY KEY,   -- Primary Key, ID
    FirstName VARCHAR(50) NOT NULL,     -- First and Last names are mandatory
    LastName VARCHAR(50) NOT NULL,
    Address VARCHAR(255),               -- OPTIONAL, can be long
    Email VARCHAR(50) UNIQUE,           -- Email must be unique for each user
    PhoneNumber INTEGER UNIQUE          -- OPTIONAL, UNIQUE (one number per patron)
);

CREATE TABLE Transactions(
    TransactionID VARCHAR(10) PRIMARY KEY,  -- Primary Key, ID
    BookID VARCHAR(10) NOT NULL,            -- Foreign Keys: Both must be NOT NULL for the transaction to be valid
    PatronID VARCHAR(10) NOT NULL,
    DateBorrowed DATE NOT NULL,             -- both dates are needed for a transaction
    DueDate DATE NOT NULL,
    DateReturned DATE,                      -- NULL until the book is checked in
    FOREIGN KEY (BookID) REFERENCES Books(BookID),
    FOREIGN KEY (PatronID) REFERENCES Patrons(PatronID)
);