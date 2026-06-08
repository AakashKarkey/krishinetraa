# KrishiNetra — Potato Disease Classification

Deep learning backend that identifies potato leaf diseases (Early Blight, Late Blight, Healthy) using 4 different CNN architectures. Each algorithm is self-contained with its own model definition, training script, and inference CLI.

---

## 1. Directory Structure

```
backend/
│
├── algorithms/                          # Self-contained per-algorithm modules
│   ├── cnn/                             # Custom CNN from scratch (256x256 input)
│   │   ├── model.py                     #   build_cnn() — 6 Conv + Pool layers
│   │   ├── train.py                     #   Training entrypoint
│   │   ├── main.py                      #   Inference CLI: python main.py image.jpg
│   │   └── utils.py                     #   Config, callbacks, class weights
│   │
│   ├── efficientnet/                    # EfficientNetB3 transfer learning (224x224)
│   │   ├── model.py                     #   build_efficientnet() + preprocess_input
│   │   ├── train.py                     #   Two-phase: frozen head → full fine-tune
│   │   ├── main.py                      #   Inference CLI
│   │   └── utils.py                     #   Config, callbacks, augmentations
│   │
│   ├── resnet/                          # ResNet50 transfer learning (224x224)
│   │   ├── model.py                     #   build_resnet() + preprocess_input
│   │   ├── train.py                     #   Two-phase training
│   │   ├── main.py                      #   Inference CLI
│   │   └── utils.py                     #   Config, callbacks, augmentations
│   │
│   └── densenet/                        # DenseNet121 transfer learning (224x224)
│       ├── model.py                     #   build_densenet() + preprocess_input
│       ├── train.py                     #   Two-phase training
│       ├── main.py                      #   Inference CLI
│       └── utils.py                     #   Config, callbacks, augmentations
│
├── save_models/                         # Trained .h5 weight files
│   ├── custom_cnn/                      #   potatoes.h5
│   ├── efficientnet/                    #   EfficientNetB3_best.h5, EfficientNetB3_pretrained.h5
│   ├── resnet50/                        #   ResNet50_best.h5, ResNet50_finetuned.h5
│   └── densenet121/                     #   DenseNet121_best.h5, DenseNet121_finetuned.h5
│
├── evaluate/                            # Model evaluation tools
│   └── compare_models.py                #   Load all models → test set → ranked table
│
├── api/
│   └── main.py                          # FastAPI server (serves Custom CNN by default)
│
├── train/
│   └── dataset/plant/                   # Dataset: Potato___Early_blight / Late_blight / healthy
│
├── frontend/                            # React Native / Next.js mobile app
│
└── README.md                            # This file
```

---

## 2. Setup

**Requirements:** Python 3.11+, TensorFlow 2.13.0 (installed in `.venv/`)

```bash
# Activate the pre-configured virtual environment
source .venv/bin/activate

# Or use the venv python directly
.venv/bin/python --version
```

Dataset is in `train/dataset/plant/` with 3 subdirectories:
| Directory | Label | Samples |
|-----------|-------|---------|
| `Potato___Early_blight` | Early Blight | ~1000 |
| `Potato___Late_blight` | Late Blight | ~1000 |
| `Potato___healthy` | Healthy | ~152 |

---

## 3. Run Inference (CLI)

Each algorithm has a `main.py` that accepts an image path and prints predictions.

```bash
# Custom CNN
.venv/bin/python algorithms/cnn/main.py path/to/leaf.jpg

# EfficientNetB3
.venv/bin/python algorithms/efficientnet/main.py path/to/leaf.jpg

# ResNet50
.venv/bin/python algorithms/resnet/main.py path/to/leaf.jpg

# DenseNet121
.venv/bin/python algorithms/densenet/main.py path/to/leaf.jpg
```

**Output example:**
```
Prediction: Early Blight
Confidence: 0.9621
Probabilities: {'Early Blight': 0.9621, 'Late Blight': 0.0312, 'Healthy': 0.0067}
```

---

## 4. Train Models

Each algorithm has its own `train.py`. Run from the `backend/` root.

### Custom CNN
```bash
.venv/bin/python algorithms/cnn/train.py
```
Single-phase training from scratch. No fine-tuning step.

### EfficientNetB3 / ResNet50 / DenseNet121
```bash
.venv/bin/python algorithms/efficientnet/train.py
.venv/bin/python algorithms/resnet/train.py
.venv/bin/python algorithms/densenet/train.py
```
Two-phase training:
1. **Phase 1** — Backbone frozen, train head only (lr=1e-3)
2. **Phase 2** — Unfreeze backbone, fine-tune all layers (lr=1e-5)

### Training Config

Each `utils.py` contains:

| Parameter | CNN | Transfer Learning |
|-----------|-----|-------------------|
| `IMAGE_SIZE` | 256 | 224 |
| `BATCH_SIZE` | 32 | 32 |
| `EPOCHS` | 15 | 15 |
| `N_CLASSES` | 3 | 3 |
| `CLASS_WEIGHTS` | Auto-balanced | Auto-balanced |

**Callbacks:**
- `EarlyStopping(patience=2)` — stops if val_loss doesn't improve for 2 epochs
- `ReduceLROnPlateau(patience=1)` — reduces LR by 0.3x when plateau
- `ModelCheckpoint` — saves best weights based on val_accuracy

---

## 5. Compare All Models

```bash
.venv/bin/python evaluate/compare_models.py
```

Loads every saved model, evaluates on the test set (10% of data), and prints a ranked table:

```
============================================================
MODEL COMPARISON RESULTS
============================================================
Model                Accuracy     Loss
--------------------------------------------
DenseNet121          0.9948       0.0188
ResNet50             0.9948       0.0171
EfficientNetB3       0.9896       0.0285
CNN (Custom)         0.9583       0.1205
============================================================
```

*(Actual results depend on training run.)*

---

## 6. API Server

The FastAPI server serves the Custom CNN model by default.

### Start the server
```bash
.venv/bin/python api/main.py
# Server runs at http://localhost:8000
```

### Endpoints

#### GET /ping
Health check.
```bash
curl http://localhost:8000/ping
# {"message": "Hello, I am alive"}
```

#### POST /predict
Upload a leaf image for classification.
```bash
curl -X POST http://localhost:8000/predict \
  -F "file=@path/to/leaf.jpg"
# {"class":"Early Blight","confidence":0.9621}
```

### Python client example
```python
import requests

url = "http://localhost:8000/predict"
files = {"file": open("leaf.jpg", "rb")}
resp = requests.post(url, files=files)
print(resp.json())
# {'class': 'Early Blight', 'confidence': 0.9621}
```

### Frontend (React Native / Next.js)
```javascript
const formData = new FormData();
formData.append('file', { uri: imageUri, type: 'image/jpeg', name: 'leaf.jpg' });

const response = await fetch('http://<server-ip>:8000/predict', {
  method: 'POST',
  body: formData,
});
const result = await response.json();
// { class: "Early Blight", confidence: 0.9621 }
```

### Notes
- The server currently loads `save_models/custom_cnn/potatoes.h5` (256x256 input).
- To serve a different model, change the `MODEL` path and `image.resize()` size in `api/main.py`.
- CORS is configured for `localhost`, `localhost:3000`, and common LAN addresses.

---

## 7. Models Overview

| Algorithm | Directory | Input Size | Approach | Two-Phase |
|-----------|-----------|------------|----------|-----------|
| CNN | `algorithms/cnn/` | 256×256 | 6 Conv layers from scratch | No |
| EfficientNetB3 | `algorithms/efficientnet/` | 224×224 | ImageNet pretrained | Yes |
| ResNet50 | `algorithms/resnet/` | 224×224 | ImageNet pretrained | Yes |
| DenseNet121 | `algorithms/densenet/` | 224×224 | ImageNet pretrained | Yes |

All models output 3-class softmax: `["Early Blight", "Late Blight", "Healthy"]`.

---

## 8. Class Imbalance Handling

The dataset has fewer healthy samples (~152) vs blight samples (~1000 each). Each training script applies **class weights** to compensate:

```python
counts = [1000, 1000, 152]
total = sum(counts)
CLASS_WEIGHTS = {i: total / (N_CLASSES * c) for i, c in enumerate(counts)}
```

This gives higher weight to the minority class (Healthy) during loss calculation.

---

## 9. Saved Model Files

| Save Directory | File | Description |
|----------------|------|-------------|
| `save_models/custom_cnn/` | `potatoes.h5` | Final trained weights |
| `save_models/efficientnet/` | `EfficientNetB3_best.h5` | Best from phase 1 |
| `save_models/efficientnet/` | `EfficientNetB3_pretrained.h5` | Phase 1 final |
| `save_models/resnet50/` | `ResNet50_best.h5` | Best from phase 1 |
| `save_models/resnet50/` | `ResNet50_finetuned.h5` | Best from phase 2 |
| `save_models/densenet121/` | `DenseNet121_best.h5` | Best from phase 1 |
| `save_models/densenet121/` | `DenseNet121_finetuned.h5` | Best from phase 2 |
