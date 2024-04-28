# utils_model.py module
import numpy as np
import tensorflow as tf
from sklearn.metrics import classification_report, confusion_matrix, roc_curve, auc, \
    precision_score, recall_score, f1_score

import utils_config as cfg


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
    return {
        "classification_report": classification_report(test_labels, predicted_classes, output_dict=True),
        "confusion_matrix": confusion_matrix(test_labels, predicted_classes).tolist(),
        "roc_auc": auc(*roc_curve(test_labels, predictions)[:2])
    }

def save_model(model, path='tf_model_saved'):
    model.save(path, save_format='tf')

def load_model(model_path):
    """
    Load a TensorFlow model from the specified path.

    Parameters:
    - model_path: The path where the model is saved.
    """
    return tf.keras.models.load_model(model_path)

def detailed_evaluate_model(model, test_ds, epochs=5):
    """Evaluates the TensorFlow model with detailed metrics, averaged over a number of epochs."""
    aggregated_results = {'loss': [], 'accuracy': [], 'precision': [], 'recall': [], 'f1-score': [], 'roc_auc': []}

    for _ in range(epochs):
        results = model.evaluate(test_ds)
        test_labels, predictions, predicted_classes = generate_predictions(model, test_ds)
        metrics = generate_metrics(test_labels, predicted_classes, predictions)

        # Aggregate results
        aggregated_results['loss'].append(results[0])
        aggregated_results['accuracy'].append(results[1])
        aggregated_results['precision'].append(metrics['classification_report']['weighted avg']['precision'])
        aggregated_results['recall'].append(metrics['classification_report']['weighted avg']['recall'])
        aggregated_results['f1-score'].append(metrics['classification_report']['weighted avg']['f1-score'])
        aggregated_results['roc_auc'].append(metrics['roc_auc'])

    # Average results
    final_results = {k: sum(v) / len(v) for k, v in aggregated_results.items()}
    final_results['confusion_matrix'] = metrics['confusion_matrix']  # Assume last epoch matrix as representative
    return final_results