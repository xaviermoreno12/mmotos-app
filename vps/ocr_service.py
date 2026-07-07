from flask import Flask, request, jsonify
import easyocr
import base64
import io
from PIL import Image

app = Flask(__name__)

# Carga los modelos una sola vez al iniciar (~30s, ~500MB)
print("Cargando modelos EasyOCR (español + inglés)...")
reader = easyocr.Reader(['es', 'en'], gpu=False)
print("Modelos cargados. Servicio listo en puerto 5050.")


@app.post('/ocr')
def ocr():
    data = request.json.get('image_base64', '')
    if not data:
        return jsonify({'error': 'image_base64 requerido'}), 400
    try:
        img_bytes = base64.b64decode(data)
        img = Image.open(io.BytesIO(img_bytes))
        resultados = reader.readtext(img, detail=0, paragraph=True)
        texto = '\n'.join(resultados)
        return jsonify({'texto': texto})
    except Exception as e:
        return jsonify({'error': str(e)}), 500


@app.get('/health')
def health():
    return jsonify({'status': 'ok'})


if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5050)
