from tensorflow.keras.models import Sequential
from tensorflow.keras.layers import Dense, Dropout
from tensorflow.keras.layers import DenseFeatures
from sklearn.metrics import classification_report, confusion_matrix, roc_curve, auc, precision_score, recall_score, f1_score
import numpy as np
import tensorflow as tf

def build_model(feature_columns):
    feature_layer = DenseFeatures(feature_columns)
    model = Sequential([
        feature_layer,
        Dense(128, activation='relu'),
        Dropout(0.3),  # Added dropout for regularization
        Dense(64, activation='relu'),
        Dense(1, activation='sigmoid')
    ])
    return model

def compile_and_train_model(model, train_ds, test_ds, epochs=100):
    model.compile(optimizer='adam', loss='binary_crossentropy', metrics=['accuracy'])
    history = model.fit(train_ds, epochs=epochs, validation_data=test_ds)
    return history

def evaluate_model(model, test_ds):
    loss, accuracy = model.evaluate(test_ds)
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