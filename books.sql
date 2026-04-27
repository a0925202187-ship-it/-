CREATE TABLE books (
  id INT(11) NOT NULL AUTO_INCREMENT,
  user_id INT(11) NOT NULL,

  title VARCHAR(100) NOT NULL,
  isbn VARCHAR(30),
  category VARCHAR(50),
  price DECIMAL(10,2),
  book_condition VARCHAR(50),
  status VARCHAR(20) DEFAULT 'available',
  description TEXT,
  is_required TINYINT(1) DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  PRIMARY KEY (id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
