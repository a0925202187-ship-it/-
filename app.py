from flask import Flask, request, jsonify
from flask_sqlalchemy import SQLAlchemy
from flask_bcrypt import Bcrypt

app = Flask(__name__)

# ================= 配置區 =================
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///users.db'  # 資料庫檔案位置
app.config['SECRET_KEY'] = 'your_secret_key'  # 系統安全密鑰
app.json.ensure_ascii = False  # 讓瀏覽器可以直接顯示正常中文 (不會變 \u606d...)

db = SQLAlchemy(app)
bcrypt = Bcrypt(app)


# ================= 資料表設計 =================
class User(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    username = db.Column(db.String(50), unique=True, nullable=False)
    email = db.Column(db.String(120), unique=True, nullable=False)
    password = db.Column(db.String(60), nullable=False)


# 初始化並建立資料庫檔案
with app.app_context():
    db.create_all()


# ================= API 路由區 =================

# 1. 註冊功能 API
@app.route('/register', methods=['GET', 'POST'])
def register():
    # 支援瀏覽器 GET 網址測試
    if request.method == 'GET':
        username = request.args.get('u', 'default_user')
        email = request.args.get('e', 'default@test.com')
        password = request.args.get('p', '123456')
        data = {"username": username, "email": email, "password": password}
    # 未來給 Android App 使用的 POST 方式
    else:
        data = request.get_json()

    # 檢查帳號或 Email 是否重複
    existing_user = User.query.filter((User.username == data['username']) | (User.email == data['email'])).first()
    if existing_user:
        return jsonify({"message": "註冊失敗：帳號或 Email 已被註冊過！", "status": "error"}), 400

    # 密碼加密並存入資料庫
    hashed_password = bcrypt.generate_password_hash(data['password']).decode('utf-8')
    new_user = User(username=data['username'], email=data['email'], password=hashed_password)
    db.session.add(new_user)
    db.session.commit()

    return jsonify({"message": f"恭喜 {data['username']}，註冊成功！", "status": "success"}), 201


# 2. 登入功能 API
@app.route('/login', methods=['GET', 'POST'])
def login():
    # 支援瀏覽器 GET 網址測試
    if request.method == 'GET':
        username = request.args.get('u', '')
        password = request.args.get('p', '')
    # 未來給 Android App 使用的 POST 方式
    else:
        data = request.get_json()
        username = data.get('username')
        password = data.get('password')

    # 去資料庫尋找使用者
    user = User.query.filter_by(username=username).first()

    # 比對密碼
    if user and bcrypt.check_password_hash(user.password, password):
        return jsonify({"message": f"歡迎回來，{user.username}！登入成功。", "status": "success"}), 200
    else:
        return jsonify({"message": "登入失敗：帳號或密碼錯誤！", "status": "error"}), 401


# ================= 啟動伺服器 =================
if __name__ == '__main__':
    # debug=True 可以在你修改程式碼時自動重新啟動伺服器
    app.run(debug=True)
