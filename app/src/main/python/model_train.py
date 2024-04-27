import os

import utils_data_preparation as udp
import utils_model as um
#  * Authored by matoszuc@gmail.com


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
    model_save_path = os.path.join(os.environ["HOME"], 'models', model_save_dir)

    dataset = udp.prepare_data_for_model(resources_path, safe_filename, phishing_filename)

    print("Building and training the model...")
    model = um.build_model()  # Build the model using the updated build_model function

    print("Training the model...")
    model.fit(dataset, epochs=10)  # Assuming model.fit() can handle dataset directly

    print("Saving the model to:", model_save_path)
    um.save_model(model, model_save_path)
    print("Model saved successfully.")
