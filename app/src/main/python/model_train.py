import tensorflow as tf
import utils_data_preparation as udp
import utils_model as um

def train_and_evaluate_model(resources_dir, safe_filename, phishing_filename):
    """
    Train and evaluate the model using the provided dataset paths.

    Parameters:
    - resources_dir: Directory where the dataset files are located.
    - safe_filename: Filename of the dataset containing safe emails.
    - phishing_filename: Filename of the dataset containing phishing emails.
    """
    print("Starting data preparation...")
    train_ds, test_ds = udp.prepare_data_for_model(resources_dir, safe_filename, phishing_filename)

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
    print("Saving the model...")
    um.save_model(model, 'tf_model_saved')
    print("Model saved successfully.")

def main():
    resources_dir = 'res'
    safe_filename = 'emails-enron.mbox-export.csv'
    phishing_filename = 'emails-phishing-pot.mbox-export.csv'

    # Train and evaluate the model with the specified parameters
    train_and_evaluate_model(resources_dir, safe_filename, phishing_filename)

if __name__ == '__main__':
    main()