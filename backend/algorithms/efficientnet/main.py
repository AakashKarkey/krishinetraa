import os, sys, argparse
import numpy as np
from PIL import Image

backend_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, backend_dir)

from algorithms.efficientnet.utils import CLASS_NAMES, IMAGE_SIZE

MODEL_PATH = os.path.join(backend_dir, "save_models", "efficientnet", "EfficientNetB3_best.h5")

def load_image(image_path):
    img = Image.open(image_path).convert("RGB")
    img = img.resize((IMAGE_SIZE, IMAGE_SIZE))
    img = np.array(img) / 255.0
    return np.expand_dims(img, axis=0)

def predict(image_path):
    import tensorflow as tf
    model = tf.keras.models.load_model(MODEL_PATH)
    img_batch = load_image(image_path)
    preds = model.predict(img_batch, verbose=0)[0]
    class_idx = np.argmax(preds)
    return CLASS_NAMES[class_idx], float(preds[class_idx]), preds.tolist()

if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Predict using EfficientNetB3")
    parser.add_argument("image_path", help="Path to image file")
    args = parser.parse_args()
    if not os.path.exists(args.image_path):
        print(f"Error: {args.image_path} not found"); sys.exit(1)
    label, confidence, all_probs = predict(args.image_path)
    print(f"Prediction: {label}\nConfidence: {confidence:.4f}\nProbabilities: {dict(zip(CLASS_NAMES, all_probs))}")
