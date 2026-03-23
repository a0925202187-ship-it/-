const mysql = require('mysql2');

const pool = mysql.createPool({
  host: 'localhost',
  user: 'root',
  password: 'ccps931008', 
  database: 'book_system',
  waitForConnections: true,
  connectionLimit: 10
});

module.exports = pool.promise();

console.log("資料庫連線設定已就緒！");
