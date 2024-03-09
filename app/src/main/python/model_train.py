import os
import tensorflow as tf
import utils_data_preparation as udp
import utils_model as um

def train_and_evaluate_model(resources_dir, safe_filename, phishing_filename, model_save_dir):
    """
    Train and evaluate the model using the provided dataset paths and save the model into the specified directory.

    Parameters:
    - resources_dir: Directory where the dataset files are located.
    - safe_filename: Filename of the dataset containing safe emails.
    - phishing_filename: Filename of the dataset containing phishing emails.
    - model_save_dir: Directory where the trained model should be saved.
    """
    print("Starting data preparation...")

    resources_path = os.path.join(os.environ["HOME"], resources_dir)
    model_save_path = os.path.join(os.environ["HOME"], 'models' , model_save_dir)


    train_ds, test_ds = udp.prepare_data_for_model(resources_path, safe_filename, phishing_filename)

    # Debug: Print a sample of one data point from the training dataset
    for features, label in train_ds.take(1):
        print("Sample data point from training set:", features, "Label:", label.numpy())

    # Build and train the model
    print("Building and training the model...")
    feature_columns = [tf.feature_column.numeric_column(key=key) for key, _ in features.items()]
    model = um.build_model(feature_columns)
    um.compile_and_train_model(model, train_ds, test_ds, epochs=10)  # Reduced epochs for quicker runs

    # Evaluate the model
    print("Evaluating the model...")
    um.evaluate_model(model, test_ds)

    # Generate and print metrics
    print("Generating metrics for the test dataset...")
    test_labels, predictions, predicted_classes = um.generate_predictions(model, test_ds)
    um.generate_metrics(test_labels, predicted_classes, predictions)

    # Save the model
    # model_save_path = os.path.join('models, 'model_save_dir, 'my_model')
    print("Saving the model to:", model_save_path)
    um.save_model(model, model_save_path)
    print("Model saved successfully.")
