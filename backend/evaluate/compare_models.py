import os, sys
backend_dir = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
sys.path.insert(0, backend_dir)
os.chdir(backend_dir)

import tensorflow as tf
from algorithms.cnn.utils import DIR_CLASS_NAMES

DATA_DIR = os.path.join(backend_dir, "train", "dataset", "plant")
SAVE_DIR = os.path.join(backend_dir, "save_models")
BATCH_SIZE = 32

model_configs = [
    ("CNN (Custom)", os.path.join(SAVE_DIR, "custom_cnn", "potatoes.h5"), 256),
    ("EfficientNetB3", os.path.join(SAVE_DIR, "efficientnet", "EfficientNetB3_best.h5"), 224),
    ("ResNet50", os.path.join(SAVE_DIR, "resnet50", "ResNet50_finetuned.h5"), 224),
    ("DenseNet121", os.path.join(SAVE_DIR, "densenet121", "DenseNet121_finetuned.h5"), 224),
]

results = []
for name, path, img_size in model_configs:
    if not os.path.exists(path):
        print(f"WARNING: {path} not found. Skipping {name}.")
        continue

    dataset = tf.keras.preprocessing.image_dataset_from_directory(
        DATA_DIR, shuffle=True, image_size=(img_size, img_size),
        batch_size=BATCH_SIZE, label_mode='int',
    )
    train_size = int(0.8 * len(dataset))
    val_size = int(0.1 * len(dataset))
    test_ds = dataset.skip(train_size).skip(val_size)

    def one_hot_encode(images, labels):
        return images, tf.one_hot(labels, depth=3)

    test_ds = test_ds.map(one_hot_encode)
    test_ds = test_ds.cache().prefetch(buffer_size=tf.data.AUTOTUNE)

    print(f"\nEvaluating {name} (input {img_size}x{img_size})...")
    model = tf.keras.models.load_model(path)
    model.compile(loss='categorical_crossentropy', metrics=['categorical_accuracy'])
    test_loss, test_acc = model.evaluate(test_ds, verbose=1)
    results.append((name, test_acc, test_loss, img_size))
    print(f"  {name}: Loss={test_loss:.4f}, Accuracy={test_acc:.4f}")

print(f"\n{'='*60}")
print("MODEL COMPARISON RESULTS")
print(f"{'='*60}")
print(f"{'Model':20s} {'Input':8s} {'Accuracy':12s} {'Loss':12s}")
print(f"{'-'*52}")
for name, acc, loss, size in sorted(results, key=lambda x: -x[1]):
    print(f"{name:20s} {size:3d}px    {acc:.4f}       {loss:.4f}")
print(f"{'='*60}")
