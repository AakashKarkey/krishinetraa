import os
from tensorflow.keras import layers, Sequential
from tensorflow.keras.callbacks import EarlyStopping, ReduceLROnPlateau, ModelCheckpoint

BATCH_SIZE = 32
EPOCHS = 15
IMAGE_SIZE = 256
CHANNELS = 3
N_CLASSES = 3
CLASS_NAMES = ["Early Blight", "Late Blight", "Healthy"]
DIR_CLASS_NAMES = ["Potato___Early_blight", "Potato___Late_blight", "Potato___healthy"]

counts = [1000, 1000, 152]
total = sum(counts)
CLASS_WEIGHTS = {i: total / (N_CLASSES * c) for i, c in enumerate(counts)}

def get_callbacks(save_path, prefix="model"):
    return [
        EarlyStopping(monitor='val_loss', patience=2, restore_best_weights=True),
        ReduceLROnPlateau(monitor='val_loss', factor=0.3, patience=1, min_lr=1e-7, verbose=1),
        ModelCheckpoint(os.path.join(save_path, f"{prefix}_best.h5"), monitor='val_accuracy', save_best_only=True, verbose=1),
    ]
