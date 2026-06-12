import sys, os, logging, time

_SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
_BACKEND_DIR = os.path.dirname(_SCRIPT_DIR)

if not sys.executable.startswith(os.path.join(_BACKEND_DIR, ".venv")):
    _venv_python = os.path.join(_BACKEND_DIR, ".venv", "bin", "python")
    if os.path.exists(_venv_python):
        os.execv(_venv_python, [_venv_python, __file__, *sys.argv[1:]])

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s [%(levelname)s] %(message)s",
)
log = logging.getLogger("krishinetra")

from fastapi import FastAPI, File, UploadFile, HTTPException
from fastapi.middleware.cors import CORSMiddleware
import uvicorn
import numpy as np
from io import BytesIO
from PIL import Image
import tensorflow as tf

app = FastAPI()

origins = [
    "http://localhost",
    "http://localhost:3000",
    "http://192.168.0.100:19006",
    "http://192.168.1.210:3000",
    "http://192.168.1.219:3000",
    "http://172.16.0.27:3000",
]

app.add_middleware(
    CORSMiddleware,
    allow_origins=origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# ============================================================
# MODEL SELECTION
# ============================================================
# Uncomment ONE of the following model configs and comment out
# the rest to switch which model the API serves.
#
# Make sure IMG_SIZE matches the model's expected input size.
# ============================================================

_MODEL_DIR = os.path.join(_BACKEND_DIR, "save_models")

_MODEL_NAME = "ResNet50"

# # --- MODEL: Custom CNN (default, active) ---
# MODEL = tf.keras.models.load_model(os.path.join(_MODEL_DIR, "custom_cnn", "potatoes.h5"))
# CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
# IMG_SIZE = 256
# _MODEL_NAME = "Custom CNN"

# --- MODEL: EfficientNetB3 ---
# MODEL = tf.keras.models.load_model(os.path.join(_MODEL_DIR, "efficientnet", "EfficientNetB3_best.h5"))
# CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
# IMG_SIZE = 224
# _MODEL_NAME = "EfficientNetB3"

# --- MODEL: ResNet50 ---
MODEL = tf.keras.models.load_model(os.path.join(_MODEL_DIR, "resnet50", "ResNet50_finetuned.h5"))
CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
IMG_SIZE = 224
_MODEL_NAME = "ResNet50"

# --- MODEL: DenseNet121 ---
# MODEL = tf.keras.models.load_model(os.path.join(_MODEL_DIR, "densenet121", "DenseNet121_finetuned.h5"))
# CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
# IMG_SIZE = 224
# _MODEL_NAME = "DenseNet121"


@app.get("/ping")
async def ping():
    return {"message": "Hello, I am alive"}


def read_file_as_image(data) -> np.ndarray:
    try:
        image = Image.open(BytesIO(data)).convert("RGB")
        image = image.resize((IMG_SIZE, IMG_SIZE))
        image = np.array(image)
        return image
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Invalid image: {e}")


@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    start = time.time()
    contents = await file.read()
    elapsed_read = time.time() - start

    log.info("Received file: name=%s, size=%dKB, read_time=%.3fs", file.filename, len(contents) // 1024, elapsed_read)

    image = read_file_as_image(contents)
    img_batch = np.expand_dims(image, 0)

    t0 = time.time()
    predictions = MODEL.predict(img_batch, verbose=0)
    elapsed_infer = time.time() - t0

    predicted_class = CLASS_NAMES[np.argmax(predictions[0])]
    confidence = float(np.max(predictions[0]))
    raw_probs = {CLASS_NAMES[i]: float(p) for i, p in enumerate(predictions[0])}
    total = time.time() - start

    log.info(
        "Prediction: class=%s, confidence=%.4f, raw=%s, model=%s, total=%.3fs (read=%.3fs, infer=%.3fs)",
        predicted_class, confidence, raw_probs, _MODEL_NAME, total, elapsed_read, elapsed_infer,
    )

    return {
        "class": predicted_class,
        "confidence": confidence,
        "probabilities": raw_probs,
        "model": _MODEL_NAME,
        "processing_time_s": round(total, 3),
    }


if __name__ == "__main__":
    uvicorn.run(app, host='localhost', port=8000)
    