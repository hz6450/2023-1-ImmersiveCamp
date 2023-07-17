from flask import Flask, render_template

app = Flask(__name__)

@app.route('/')
def logo():
    return render_template('logo.html')

@app.route('/ocr')
def ocr():
    return render_template('ocr.html')

@app.route('/index')
def index():
    return render_template('index.html')

if __name__ == '__main__':
    app.run()
