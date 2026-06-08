import tensorflow as tf
from tensorflow.keras import layers, models

def build_cnn(input_shape=(256, 256, 3), n_classes=3):
    resize_and_rescale = tf.keras.Sequential([
        layers.Resizing(256, 256),
        layers.Rescaling(1./255),
    ])

    data_augmentation = tf.keras.Sequential([
        layers.RandomFlip("horizontal_and_vertical"),
        layers.RandomRotation(0.2),
    ])

    model = models.Sequential([
        layers.Input(shape=input_shape),
        resize_and_rescale,
        data_augmentation,
        layers.Conv2D(32, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Conv2D(64, kernel_size=(3,3), activation='relu'),
        layers.MaxPooling2D((2, 2)),
        layers.Flatten(),
        layers.Dense(64, activation='relu'),
        layers.Dense(n_classes, activation='softmax'),
    ], name="CustomCNN")

    return model
