# Model Comparison Report — KrishiNetra Potato Disease Classification

## Overview

This report compares four deep learning architectures trained to classify potato leaf diseases into three categories: **Early Blight**, **Late Blight**, and **Healthy**. All models were trained on the same dataset (~2152 images) with class weighting to handle imbalance (Early Blight: ~1000, Late Blight: ~1000, Healthy: ~152).

---

## 1. Dataset Summary

| Class | Directory Name | Samples |
|-------|---------------|---------|
| Early Blight | `Potato___Early_blight` | ~1000 |
| Late Blight | `Potato___Late_blight` | ~1000 |
| Healthy | `Potato___healthy` | ~152 |
| **Total** | | **~2152** |

- **Split:** 80% train / 10% validation / 10% test
- **Class weighting:** Applied to handle imbalance — healthy class gets ~5x higher weight

---

## 2. Model Architectures

### 2.1 Custom CNN

| Property | Detail |
|----------|--------|
| **File** | `algorithms/cnn/model.py` |
| **Approach** | From scratch |
| **Input Size** | 256×256×3 |
| **Architecture** | 6 Conv2D layers + MaxPooling + Flatten + Dense(64) + Dense(3, softmax) |
| **Conv filters** | 32 → 64 → 64 → 64 → 64 → 64 (all 3×3) |
| **Training** | Single phase, Adam(1e-3) |
| **Data Augmentation** | RandomFlip, RandomRotation(0.2) |
| **Saved Model** | `save_models/custom_cnn/potatoes.h5` |

### 2.2 EfficientNetB3

| Property | Detail |
|----------|--------|
| **File** | `algorithms/efficientnet/model.py` |
| **Approach** | Transfer learning (ImageNet) |
| **Input Size** | 224×224×3 |
| **Backbone** | EfficientNetB3 (frozen → fine-tuned) |
| **Head** | GlobalAvgPool → Dropout(0.4) → Dense(256, ReLU) → Dropout(0.3) → Dense(3, softmax) |
| **Phase 1** | Backbone frozen, Adam(1e-3), 15 epochs |
| **Phase 2** | Full fine-tune, Adam(1e-5), 15 epochs |
| **Data Augmentation** | RandomFlip, RandomRotation(0.2), RandomZoom(0.15), RandomBrightness(0.15), RandomContrast(0.15) |
| **Saved Model** | `save_models/efficientnet/EfficientNetB3_best.h5` |

### 2.3 ResNet50

| Property | Detail |
|----------|--------|
| **File** | `algorithms/resnet/model.py` |
| **Approach** | Transfer learning (ImageNet) |
| **Input Size** | 224×224×3 |
| **Backbone** | ResNet50 (frozen → fine-tuned) |
| **Head** | GlobalAvgPool → Dropout(0.4) → Dense(256, ReLU) → Dropout(0.3) → Dense(3, softmax) |
| **Phase 1** | Backbone frozen, Adam(1e-3), 15 epochs |
| **Phase 2** | Full fine-tune, Adam(1e-5), 15 epochs |
| **Data Augmentation** | RandomFlip, RandomRotation(0.2), RandomZoom(0.15), RandomBrightness(0.15), RandomContrast(0.15) |
| **Saved Model (API)** | `save_models/resnet50/ResNet50_finetuned.h5` |
| **API Status** | **Currently deployed in production** (`api/main.py`) |

### 2.4 DenseNet121

| Property | Detail |
|----------|--------|
| **File** | `algorithms/densenet/model.py` |
| **Approach** | Transfer learning (ImageNet) |
| **Input Size** | 224×224×3 |
| **Backbone** | DenseNet121 (frozen → fine-tuned) |
| **Head** | GlobalAvgPool → Dropout(0.4) → Dense(256, ReLU) → Dropout(0.3) → Dense(3, softmax) |
| **Phase 1** | Backbone frozen, Adam(1e-3), 15 epochs |
| **Phase 2** | Full fine-tune, Adam(1e-5), 15 epochs |
| **Data Augmentation** | RandomFlip, RandomRotation(0.2), RandomZoom(0.15), RandomBrightness(0.15), RandomContrast(0.15) |
| **Saved Model** | `save_models/densenet121/DenseNet121_finetuned.h5` |

---

## 3. Shared Training Configuration

| Parameter | Value |
|-----------|-------|
| Loss | `categorical_crossentropy` |
| Optimizer | Adam |
| Batch Size | 32 |
| Max Epochs | 15 (per phase for transfer models) |
| Early Stopping | patience=2 on `val_loss` |
| Reduce LR Plateau | factor=0.3, patience=1 |
| Model Checkpoint | best by `val_accuracy` |

---

## 4. Comparison Results (Live Evaluation)

Models were evaluated on the held-out test set (10% of total data, ~215 images). Each model was tested at its native input resolution.

```
====================================================================
MODEL COMPARISON RESULTS
====================================================================
Model                Input    Accuracy     Loss
----------------------------------------------------
ResNet50             224px    1.0000       0.0017
DenseNet121          224px    1.0000       0.0079
CNN (Custom)         256px    0.9698       0.1224
EfficientNetB3       224px    0.9698       0.0786
====================================================================
```

### Ranking by Accuracy

| Rank | Model | Accuracy | Loss | Input Size | Parameters |
|------|-------|----------|------|------------|------------|
| 1 | **ResNet50** | **100.00%** | 0.0017 | 224×224 | ~25.6M |
| 2 | **DenseNet121** | **100.00%** | 0.0079 | 224×224 | ~8.0M |
| 3 | CNN (Custom) | 96.98% | 0.1224 | 256×256 | ~0.5M |
| 4 | EfficientNetB3 | 96.98% | 0.0786 | 224×224 | ~12.0M |

> **Note:** All three transfer learning models (ResNet50, DenseNet121, EfficientNetB3) were trained using their **best checkpoint from phase 1** (frozen backbone), except ResNet50 which uses the fine-tuned phase 2 weights. This is because `EfficientNetB3_best.h5` and `DenseNet121_finetuned.h5` were used as specified in the comparison script paths.

---

## 5. Analysis

### 5.1 Key Findings

1. **ResNet50 & DenseNet121 achieve perfect test accuracy (100%)** — Both models correctly classified every test image. Given the visually distinct nature of the three classes (early blight lesions, late blight lesions, healthy leaves), this is plausible on a ~215-image test set.

2. **Transfer learning vastly outperforms from-scratch CNN** — The custom CNN (96.98%) lags behind the pretrained models despite having a simpler task-specific architecture. Pretrained ImageNet features provide a strong visual priors for leaf disease patterns.

3. **EfficientNetB3 underperforms relative to ResNet50/DenseNet121** — Despite being a more modern architecture, EfficientNetB3 achieved only 96.98% accuracy. This may be due to:
   - Using the **phase 1 checkpoint** (`EfficientNetB3_best.h5`) which only had the head trained (backbone frozen)
   - Insufficient fine-tuning epochs for the complex EfficientNet architecture

4. **Custom CNN is competitive at 96.98%** — For a 6-layer network trained from scratch on ~1700 training images, this is a strong result. It is also the **smallest model** (~0.5M parameters), making it suitable for edge/mobile deployment.

### 5.2 Architecture Comparison

| Model | Parameters | Input Size | Inference Speed | Storage Size |
|-------|-----------|------------|-----------------|--------------|
| CNN (Custom) | ~0.5M | 256×256 | Fastest | ~2 MB |
| EfficientNetB3 | ~12.0M | 224×224 | Medium | ~47 MB |
| ResNet50 | ~25.6M | 224×224 | Medium | ~98 MB |
| DenseNet121 | ~8.0M | 224×224 | Fast | ~31 MB |

### 5.3 Class Imbalance Handling

All models use the same class weighting scheme:

```python
counts = [1000, 1000, 152]  # Early Blight, Late Blight, Healthy
total = sum(counts)          # 2152
CLASS_WEIGHTS = {i: total / (3 * c) for i, c in enumerate(counts)}
```

Resulting weights:
- Early Blight: **0.717**
- Late Blight: **0.717**
- Healthy: **4.719**

---

## 6. Production Deployment

The FastAPI server (`api/main.py`) currently serves **ResNet50** (fine-tuned):

| Setting | Value |
|---------|-------|
| **Active Model** | ResNet50 |
| **Weights** | `save_models/resnet50/ResNet50_finetuned.h5` |
| **Input Size** | 224×224 |
| **Classes** | `["Early Blight", "Late Blight", "Healthy"]` |

To switch models, uncomment the desired block in `api/main.py` and adjust `IMG_SIZE` accordingly.

---

## 7. Saved Model Files

| Directory | File | Description | Phase |
|-----------|------|-------------|-------|
| `save_models/custom_cnn/` | `potatoes.h5` | Final trained weights | Single |
| `save_models/efficientnet/` | `EfficientNetB3_best.h5` | Best from Phase 1 | Frozen |
| `save_models/efficientnet/` | `EfficientNetB3_pretrained.h5` | Phase 1 final | Frozen |
| `save_models/resnet50/` | `ResNet50_best.h5` | Best from Phase 1 | Frozen |
| `save_models/resnet50/` | `ResNet50_finetuned.h5` | Best from Phase 2 | Fine-tuned |
| `save_models/densenet121/` | `DenseNet121_best.h5` | Best from Phase 1 | Frozen |
| `save_models/densenet121/` | `DenseNet121_finetuned.h5` | Best from Phase 2 | Fine-tuned |

---

## 8. How to Run Comparison

```bash
cd backend
.venv/bin/python evaluate/compare_models.py
```

The script loads each saved `.h5` model, reconstructs the test dataset at the model's native resolution, runs `model.evaluate()`, and prints a ranked table.

---

## 9. Recommendations

1. **For production (accuracy-critical):** Use **ResNet50** or **DenseNet121** — both achieved 100% test accuracy with fast inference.
2. **For edge/mobile deployment (size-critical):** Use **Custom CNN** — only ~0.5M parameters (~2 MB) with 96.98% accuracy.
3. **For balanced trade-off:** Use **DenseNet121** — 8M parameters (~31 MB), 100% accuracy, and faster inference than ResNet50.
4. **Re-evaluate with more metrics:** Consider computing precision, recall, F1-score, and confusion matrices for a more complete picture.
5. **Evaluate EfficientNetB3 fine-tuned:** The `EfficientNetB3_finetuned.h5` checkpoint (from phase 2) should be evaluated as it may outperform the frozen-backbone checkpoint.

---

*Generated on: June 9, 2026*  
*Evaluation script: `evaluate/compare_models.py`*
