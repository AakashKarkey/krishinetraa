import os, sys
backend_dir = os.path.dirname(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
sys.path.insert(0, backend_dir)
os.chdir(backend_dir)

import tensorflow as tf
from tensorflow.keras import optimizers
from algorithms.efficientnet.model import build_efficientnet
from algorithms.efficientnet.utils import (
    BATCH_SIZE, EPOCHS, IMAGE_SIZE, CHANNELS, N_CLASSES,
    DIR_CLASS_NAMES, CLASS_WEIGHTS, get_callbacks, get_finetune_callbacks, get_data_augmentation
)

SAVE_DIR = os.path.join(backend_dir, "save_models", "efficientnet")
DATA_DIR = os.path.join(backend_dir, "train", "dataset", "plant")
os.makedirs(SAVE_DIR, exist_ok=True)

dataset = tf.keras.preprocessing.image_dataset_from_directory(
    DATA_DIR, shuffle=True, image_size=(IMAGE_SIZE, IMAGE_SIZE),
    batch_size=BATCH_SIZE, class_names=DIR_CLASS_NAMES, label_mode='categorical',
)

train_size = int(0.8 * len(dataset))
val_size = int(0.1 * len(dataset))
train_ds = dataset.take(train_size)
val_ds = dataset.skip(train_size).take(val_size)
test_ds = dataset.skip(train_size).skip(val_size)

AUTOTUNE = tf.data.AUTOTUNE
train_ds = train_ds.cache().shuffle(1000).prefetch(buffer_size=AUTOTUNE)
val_ds = val_ds.cache().prefetch(buffer_size=AUTOTUNE)
test_ds = test_ds.cache().prefetch(buffer_size=AUTOTUNE)

data_augmentation = get_data_augmentation()

model, base_model = build_efficientnet(input_shape=(IMAGE_SIZE, IMAGE_SIZE, CHANNELS), n_classes=N_CLASSES)
model.compile(optimizer=optimizers.Adam(learning_rate=1e-3), loss='categorical_crossentropy', metrics=['accuracy'])

history = model.fit(train_ds, validation_data=val_ds, epochs=EPOCHS, class_weight=CLASS_WEIGHTS, callbacks=get_callbacks(SAVE_DIR, "EfficientNetB3"), verbose=1)

base_model.trainable = True
model.compile(optimizer=optimizers.Adam(learning_rate=1e-5), loss='categorical_crossentropy', metrics=['accuracy'])

history_fine = model.fit(train_ds, validation_data=val_ds, epochs=EPOCHS, class_weight=CLASS_WEIGHTS, callbacks=get_finetune_callbacks(SAVE_DIR, "EfficientNetB3"), verbose=1)

test_loss, test_acc = model.evaluate(test_ds, verbose=0)
print(f"\nEfficientNetB3 Test Accuracy: {test_acc:.4f}")
model.save(os.path.join(SAVE_DIR, "EfficientNetB3_final.h5"))
print(f"EfficientNetB3 saved to {SAVE_DIR}")
