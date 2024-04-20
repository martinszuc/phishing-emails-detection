# utils_model.py module
import numpy as np
import utils_config as cfg
from sklearn.metrics import classification_report, confusion_matrix, roc_curve, auc, \
    precision_score, recall_score, f1_score
import tensorflow as tf

def build_model():
    config = cfg.Config()
    feature_columns = []
    for feature, categories in config.CATEGORICAL_FEATURES.items():
        for category in categories:
            feature_columns.append(tf.feature_column.numeric_column(f"{feature}_{category}"))

    feature_columns += [tf.feature_column.numeric_column(feat) for feat in config.NUMERICAL_FEATURES + config.BOOLEAN_FEATURES]

    # Create a DenseFeatures layer with specified feature columns
    feature_layer = tf.keras.layers.DenseFeatures(feature_columns)
    model = tf.keras.Sequential([
        feature_layer,
        tf.keras.layers.Dense(128, activation='relu'),
        tf.keras.layers.Dropout(0.3),
        tf.keras.layers.Dense(64, activation='relu'),
        tf.keras.layers.Dense(1, activation='sigmoid')
    ])
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    print("Compiled the model with Adam optimizer and binary_crossentropy loss")

    return model

def compile_and_train_model(model, train_ds, test_ds, epochs=100):
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    history = model.fit(train_ds, epochs=epochs, validation_data=test_ds)
    return history

def evaluate_model(model, test_ds):
    loss, accuracy = model.evaluate(test_ds)
    model.summary()
    print(f"Test Loss: {loss}, Test Accuracy: {accuracy}")
    return loss, accuracy

def generate_predictions(model, test_ds, threshold=0.45):
    test_labels = np.concatenate([y for x, y in test_ds], axis=0)
    predictions = model.predict(test_ds)
    predicted_classes = (predictions > threshold).astype(np.float32).flatten()
    return test_labels, predictions, predicted_classes

def generate_metrics(test_labels, predicted_classes, predictions):
    print("Classification Report:")
    print(classification_report(test_labels, predicted_classes))

    print("Confusion Matrix:")
    print(confusion_matrix(test_labels, predicted_classes))

    fpr, tpr, thresholds = roc_curve(test_labels, predictions)
    roc_auc = auc(fpr, tpr)
    print("ROC AUC:", roc_auc)

    precision = precision_score(test_labels, predicted_classes)
    recall = recall_score(test_labels, predicted_classes)
    f1 = f1_score(test_labels, predicted_classes)
    print("Precision:", precision)
    print("Recall:", recall)
    print("F1 Score:", f1)

def save_model(model, path='tf_model_saved'):
    model.save(path, save_format='tf')