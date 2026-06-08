import os, sys
backend_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, backend_dir)
os.chdir(backend_dir)

import tensorflow as tf
from algorithms.cnn.utils import IMAGE_SIZE, CLASS_NAMES as CNN_CLASS_NAMES

DATA_DIR = os.path.join(backend_dir, "train", "dataset", "plant")
SAVE_DIR = os.path.join(backend_dir, "save_models")
BATCH_SIZE = 32

dataset = tf.keras.preprocessing.image_dataset_from_directory(
    DATA_DIR, shuffle=True, image_size=(IMAGE_SIZE, IMAGE_SIZE),
    batch_size=BATCH_SIZE, label_mode='categorical',
)

train_size = int(0.8 * len(dataset))
val_size = int(0.1 * len(dataset))
test_ds = dataset.skip(train_size).skip(val_size)
test_ds = test_ds.cache().prefetch(buffer_size=tf.data.AUTOTUNE)

model_configs = [
    ("CNN (Custom)", os.path.join(SAVE_DIR, "custom_cnn", "potatoes.h5")),
    ("EfficientNetB3", os.path.join(SAVE_DIR, "efficientnet", "EfficientNetB3_best.h5")),
    ("ResNet50", os.path.join(SAVE_DIR, "resnet50", "ResNet50_finetuned.h5")),
    ("DenseNet121", os.path.join(SAVE_DIR, "densenet121", "DenseNet121_finetuned.h5")),
]

results = []
for name, path in model_configs:
    if not os.path.exists(path):
        print(f"WARNING: {path} not found. Skipping {name}.")
        continue
    print(f"\nEvaluating {name}...")
    model = tf.keras.models.load_model(path)
    test_loss, test_acc = model.evaluate(test_ds, verbose=1)
    results.append((name, test_acc, test_loss))
    print(f"  {name}: Loss={test_loss:.4f}, Accuracy={test_acc:.4f}")

print(f"\n{'='*60}")
print("MODEL COMPARISON RESULTS")
print(f"{'='*60}")
print(f"{'Model':20s} {'Accuracy':12s} {'Loss':12s}")
print(f"{'-'*44}")
for name, acc, loss in sorted(results, key=lambda x: -x[1]):
    print(f"{name:20s} {acc:.4f}       {loss:.4f}")
print(f"{'='*60}")
