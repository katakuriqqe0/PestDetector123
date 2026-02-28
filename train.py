import tensorflow as tf
from tensorflow.keras import layers, models
import os

PATH = r'R:\code\datasetik\dataset'
IMG_SIZE = (224, 224)
BATCH_SIZE = 32

train_ds = tf.keras.utils.image_dataset_from_directory(
    PATH, validation_split=0.2, subset="training", seed=123,
    image_size=IMG_SIZE, batch_size=BATCH_SIZE
)

val_ds = tf.keras.utils.image_dataset_from_directory(
    PATH, validation_split=0.2, subset="validation", seed=123,
    image_size=IMG_SIZE, batch_size=BATCH_SIZE
)

# КРИТИЧНО: Порядок класів має бути зафіксований
class_names = train_ds.class_names
print("Твій порядок класів:", class_names)

# Посилена аугментація для боротьби з шумом монітора
data_augmentation = tf.keras.Sequential([
    layers.RandomFlip("horizontal_and_vertical"),
    layers.RandomRotation(0.2),
    layers.RandomZoom(0.2),
    layers.RandomContrast(0.2),
])

# Створення моделі з ПРАВИЛЬНОЮ нормалізацією
base_model = tf.keras.applications.MobileNetV2(
    input_shape=(224, 224, 3), 
    include_top=False, 
    weights='imagenet'
)
base_model.trainable = False 

model = models.Sequential([
    layers.Input(shape=(224, 224, 3)),
    # Цей шар перетворює 0..255 у формат, який розуміє MobileNetV2
    layers.Lambda(tf.keras.applications.mobilenet_v2.preprocess_input), 
    data_augmentation,
    base_model,
    layers.GlobalAveragePooling2D(),
    layers.Dense(128, activation='relu'),
    layers.Dropout(0.5), # Збільшено до 0.5, щоб модель не "зубрила"
    layers.Dense(len(class_names), activation='softmax')
])

# Низький Learning Rate, щоб модель вчилася акуратно
model.compile(
    optimizer=tf.keras.optimizers.Adam(learning_rate=0.0001),
    loss='sparse_categorical_crossentropy',
    metrics=['accuracy']
)

# Навчання на 30 епох (цього достатньо з вагами imagenet)
model.fit(train_ds, validation_data=val_ds, epochs=30)

# Збереження
converter = tf.lite.TFLiteConverter.from_keras_model(model)
tflite_model = converter.convert()
with open('pest_model.tflite', 'wb') as f:
    f.write(tflite_model)

with open('labels.txt', 'w') as f:
    for label in class_names:
        f.write(label + '\n')