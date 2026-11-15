CREATE DATABASE IF NOT EXISTS library_management
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE library_management;

CREATE TABLE IF NOT EXISTS librarians (
  id INT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(100) NOT NULL,
  full_name VARCHAR(100) NOT NULL
);

CREATE TABLE IF NOT EXISTS books (
  id INT AUTO_INCREMENT PRIMARY KEY,
  isbn VARCHAR(30) NOT NULL UNIQUE,
  title VARCHAR(180) NOT NULL,
  author VARCHAR(120) NOT NULL,
  category VARCHAR(80) NOT NULL,
  published_year INT NOT NULL,
  quantity INT NOT NULL,
  available_quantity INT NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS members (
  id INT AUTO_INCREMENT PRIMARY KEY,
  member_code VARCHAR(30) NOT NULL UNIQUE,
  full_name VARCHAR(120) NOT NULL,
  email VARCHAR(120) NOT NULL UNIQUE,
  phone VARCHAR(30) NOT NULL,
  address VARCHAR(255) NOT NULL,
  registered_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS borrowings (
  id INT AUTO_INCREMENT PRIMARY KEY,
  member_id INT NOT NULL,
  book_id INT NOT NULL,
  issue_date DATE NOT NULL,
  due_date DATE NOT NULL,
  return_date DATE NULL,
  status ENUM('BORROWED','RETURNED') NOT NULL DEFAULT 'BORROWED',
  CONSTRAINT fk_borrow_member FOREIGN KEY (member_id) REFERENCES members(id),
  CONSTRAINT fk_borrow_book FOREIGN KEY (book_id) REFERENCES books(id)
);

INSERT IGNORE INTO librarians(username,password,full_name)
VALUES ('admin','admin123','Library Administrator');

INSERT IGNORE INTO books(isbn,title,author,category,published_year,quantity,available_quantity)
VALUES ('9780134685991','Effective Java','Joshua Bloch','Programming',2018,5,5),
       ('9780596009205','Head First Java','Kathy Sierra','Programming',2005,3,3);

INSERT IGNORE INTO members(member_code,full_name,email,phone,address)
VALUES ('M001','Demo Member','member@example.com','0771234567','Kandy');
