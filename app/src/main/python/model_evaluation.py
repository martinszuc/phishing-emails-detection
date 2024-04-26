# model_evaluation.py
#  * Authored by matoszuc@gmail.com
#
import os
import utils_model as um
import utils_data_preparation as udp
import logging
import pandas as pd


# Set up logging
logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)
def evaluate_model(model_dir_name, phishing_filename, safe_filename):
    """
    Load the model from the specified directory and evaluate it using the provided testing datasets.

    Parameters:
    - model_dir_name: Name of the directory where the model is saved, which is also the model's name.
    - resources_dir: Directory where the testing dataset files are located.
    - phishing_filename: Filename of the phishing dataset.
    - safe_filename: Filename of the safe dataset.
    """
    print("Loading model...")
    model_path = os.path.join(os.environ["HOME"], 'models', model_dir_name)

    model = um.load_model(model_path)  # Ensure you have a load_model function in utils_model

    print("Preparing testing datasets...")
    phishing_dataset_path = os.path.join(os.environ["HOME"], 'testing_datasets', phishing_filename)
    safe_dataset_path = os.path.join(os.environ["HOME"], 'testing_datasets', safe_filename)

    phishing_df = pd.read_csv(phishing_dataset_path, index_col=0)
    safe_df = pd.read_csv(safe_dataset_path, index_col=0)

    # Preprocess features for both datasets
    phishing_df = udp.preprocess_features(phishing_df)
    safe_df = udp.preprocess_features(safe_df)

    # Combine the datasets
    combined_df = pd.concat([phishing_df, safe_df], ignore_index=True)

    # Convert combined DataFrame to TensorFlow dataset
    test_ds = udp.df_to_dataset(combined_df, shuffle=False)  # No need to shuffle for evaluation

    print("Evaluating the model...")
    test_results = um.detailed_evaluate_model(model, test_ds)

    print("Test Results:", test_results)
    return test_results
