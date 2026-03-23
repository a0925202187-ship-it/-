const express = require('express');
const mysql = require('mysql2');
const axios = require('axios');
const cors = require('cors');

const app = express();
app.use(cors());
app.use(express.json());
app.use(express.static('public'));

const db = mysql.createConnection({
    host: 'localhost',
    user: 'root', 
    password: 'ccps931008', 
    database: 'book_system'
});

db.connect(err => {
    if (err) {
        console.error('資料庫連線失敗：', err.message);
    } else {
        console.log('成功連線到 MySQL 資料庫！');
    }
});

app.get('/search/:isbn', async (req, res) => {
    const isbn = req.params.isbn;
    try {
        const url = `https://openlibrary.org/api/books?bibkeys=ISBN:${isbn}&format=json&jscmd=data`;
        const response = await axios.get(url);
        
        const bookKey = `ISBN:${isbn}`;
        const data = response.data[bookKey];

        if (data) {
            res.json({
                title: data.title,
                author: data.authors ? data.authors[0].name : '未知作者',
                cover: data.cover ? data.cover.large : ''
            });
        } else {
            res.status(404).json({ message: '查無此書' });
        }
    } catch (error) {
        res.status(500).json({ message: 'API 抓取失敗' });
    }
});

const PORT = 3000;
app.listen(PORT, () => {
    console.log(`伺服器已在 http://localhost:${PORT} 啟動`);
});
