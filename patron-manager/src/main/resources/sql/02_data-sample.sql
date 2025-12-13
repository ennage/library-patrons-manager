USE PatronManagerDB;

-- ----------------------------------------------------------------------
-- 1. Insert Sample Data into CATEGORIES (Parent Table)
-- CategoryID (varchar, Primary Key), CategoryName (varchar, Unique)
-- ----------------------------------------------------------------------
INSERT INTO categories (CategoryID, CategoryName) VALUES
('ACAD-01','Action & Adventure'),
('ARTS-02','Arts'),
('AUTO-03','Autobiography'),
('BIOG-04','Biography'),
('CHLD-05','Children''s Literature'),
('CLSC-06','Classic Fiction'),
('COMC-07','Comics'),
('CNTP-08','Contemporary Fiction'),
('COOK-09','Cookbooks'),
('CRIM-10','Crime Fiction'),
('DICT-11','Dictionaries'),
('DRAM-12','Drama'),
('DYST-13','Dystopian'),
('ENCY-14','Encyclopedias'),
('ESSY-15','Essays'),
('FANT-16','Fantasy'),
('GNOV-17','Graphic Novels'),
('HFIC-18','Historical Fiction'),
('HIST-19','History'),
('HORR-20','Horror'),
('HUMR-21','Humor'),
('LITF-22','Literary Fiction'),
('MEMO-23','Memoir'),
('MDGR-24','Middle Grade'),
('MYST-25','Mystery'),
('NATR-26','Nature'),
('NVLA-27','Novellas'),
('PDEV-28','Personal Development'),
('PHIL-29','Philosophy'),
('PHOT-30','Photography'),
('PICB-31','Picture Books'),
('PLAY-32','Plays'),
('POET-33','Poetry'),
('PSYC-34','Psychology'),
('REFR-35','Reference'),
('RELG-36','Religion'),
('ROMN-37','Romance'),
('SCIE-38','Science'),
('SCFI-39','Science Fiction'),
('SLHP-40','Self-Help'),
('SHRT-41','Short Stories'),
('SOCI-42','Sociology'),
('SPIR-43','Spirituality'),
('SUSP-44','Suspense'),
('TECH-45','Technology'),
('THRL-46','Thriller'),
('TRVL-47','Travel Guides'),
('TCRM-48','True Crime'),
('WEST-49','Western'),
('YALT-50','Young Adult');
ON DUPLICATE KEY UPDATE CategoryName=CategoryName; -- Prevents error if run twice



-- ----------------------------------------------------------------------
-- 3. Insert Sample Data into BOOKS (References CategoryID)
-- BookID (varchar, PK), Title, Author, ISBN (Unique), PublicationYear, CategoryID (FK)
-- ----------------------------------------------------------------------
INSERT INTO books (BookID, Title, Author, ISBN, PublicationYear, CategoryID) VALUES
('BK-0001', 'Noli Me Tangere', 'Jose Rizal', '978-6214151234', 1887, 'CLSC-06'),
('BK-0002', 'El Filibusterismo', 'Jose Rizal', '978-6214151235', 1891, 'CLSC-06'),
('BK-0003', 'Ibong Adarna', 'Unknown', '978-9715112345', 1800, 'DRAM-12'),
('BK-0004', 'Florante at Laura', 'Francisco Balagtas', '978-9715116789', 1838, 'POET-33'),
('BK-0005', 'All Tomorrows: A Billion Year Chronicle of the Pósthuman Future', 'C.M. Kosemen', '978-1032338662', 2006, 'SCFI-39'),
('BK-0006', 'The Little Prince', 'Antoine de Saint-Exupéry', '978-0156012195', 1943, 'CHLD-05'),
('BK-0007', 'Alice''s Adventures in Wonderland', 'Lewis Carroll', '978-0141439765', 1865, 'FANT-16'),
('BK-0008', 'The Works of Sherlock Holmes', 'Arthur Conan Doyle', '978-0553212459', 1987, 'MYST-25'),
('BK-0009', 'Comet in Moominland', 'Tove Jansson', '978-0140301323', 1946, 'CHLD-05'),
('BK-0010', 'Moominsummer Madness', 'Tove Jansson', '978-0140301743', 1954, 'CHLD-05'),
('BK-0011', 'Moominpappa at Sea', 'Tove Jansson', '978-0140302324', 1965, 'CHLD-05'),
('BK-0012', 'Finn Family Moomintroll', 'Tove Jansson', '978-0140300486', 1948, 'CHLD-05');



-- ----------------------------------------------------------------------
-- 2. Insert Sample Data into PATRONS (Parent Table)
-- PatronID (varchar, Primary Key), FirstName, LastName, Address, Email (Unique), PhoneNumber (int, Unique)
-- ----------------------------------------------------------------------
INSERT INTO patrons (PatronID, FirstName, LastName, PhoneNumber, Email, Address) VALUES
('PT-0001', 'Shinichi', 'Kudo', '9881112222', 'shinichi.kudo@dcmanga.jp', 'Beika Town, Tokyo'),
('PT-0002', 'Heiji', 'Hattori', '9883334444', 'heiji.hattori@dcmanga.jp', 'Osaka Prefecture'),
('PT-0003', 'Saguru', 'Hakuba', '9885556666', 'saguru.hakuba@dcmanga.jp', 'London/Tokyo Mansion'),
('PT-0004', 'Kaito', 'Kuroba', '9887778888', 'kaito.kuroba@dcmanga.jp', 'Ekoda Town, Tokyo'),
('PT-0005', 'Ran', 'Mouri', '9889990000', 'ran.mouri@dcmanga.jp', 'Beika Town, Tokyo'),
('PT-0006', 'Kazuha', 'Toyama', '9880001111', 'kazuha.toyama@dcmanga.jp', 'Osaka Prefecture'),
('PT-0007', 'Aoko', 'Nakamori', '9882223333', 'aoko.nakamori@dcmanga.jp', 'Ekoda Town, Tokyo'),
('PT-0008', 'Shiho', 'Miyano', '9884445555', 'shiho.miyano@dcmanga.jp', 'Beika Town, Tokyo');



-- ----------------------------------------------------------------------
-- 4. Insert Sample Data into TRANSACTIONS (References BookID and PatronID)
-- TransactionID (PK), BookID (FK), PatronID (FK), DateBorrowed, DueDate, DateReturned (NULLABLE)
-- ----------------------------------------------------------------------
-- P0001 borrows B0003 (still out)
INSERT INTO transactions (TransactionID, BookID, PatronID, DateBorrowed, DueDate, DateReturned) VALUES
('T-0001', 'BK-0008', 'PT-0001', '2025-12-10', '2025-12-24', NULL),         -- Shinichi borrows Sherlock Holmes (still out)
('T-0002', 'BK-0006', 'PT-0005', '2025-11-01', '2025-11-15', '2025-11-12'), -- Ran borrows Little Prince (returned)
('T-0003', 'BK-0001', 'PT-0002', '2025-11-21', '2025-12-05', NULL),         -- Heiji borrows Noli Me Tangere (overdue, due 12/05)
('T-0004', 'BK-0004', 'PT-0006', '2025-12-08', '2025-12-22', NULL),         -- Kazuha borrows Florante at Laura (still out)
('T-0005', 'BK-0012', 'PT-0004', '2025-10-15', '2025-10-29', '2025-10-25'), -- Kaito borrows Moomin book (returned)
('T-0006', 'BK-0005', 'PT-0007', '2025-12-13', '2025-12-27', NULL);         -- Aoko borrows All Tomorrows (still out)