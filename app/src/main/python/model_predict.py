import numpy as np
import os
import pandas as pd
import tensorflow as tf
import logging

import utils_data_preparation as udp
import utils_feature_extraction as ufe

# Set up logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def predict_on_mbox(model_name, filename):
    logging.info(f"Starting prediction process for model: {model_name} and mailbox: {filename}")

    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    logging.info(f"Model path resolved to: {model_path}")

    # Load the trained model
    logging.info("Loading the trained model...")
    model = tf.keras.models.load_model(model_path)
    logging.info("Model loaded successfully.")

    output_path = os.path.join(os.environ["HOME"], 'prediction_extracted')
    mbox_path = os.path.join(os.environ["HOME"], 'prediction_emails', filename)
    logging.info(f"Mbox file path: {mbox_path}")

    # Load and preprocess mbox file
    logging.info("Processing mbox file to CSV...")
    csv_filename = ufe.process_mbox_to_csv(mbox_path, "iso-8859-1", output_path, limit=200, is_phishy=None)
    processed_csv_path = os.path.join(output_path, csv_filename)
    logging.info(f"CSV file created at: {processed_csv_path}")

    # Load features from the processed CSV
    logging.info("Loading data from processed CSV...")
    data = pd.read_csv(processed_csv_path)
    logging.info(f"Data loaded with {data.shape[0]} records.")

    # Information on data types and columns
    logging.info("Data types and column information before preprocessing:")
    logging.info(data.dtypes)

    # Drop 'is_phishy' column if it exists (it's not needed for prediction)
    if 'is_phishy' in data.columns:
        data.drop(columns=['is_phishy'], inplace=True)
        logging.info("Dropped 'is_phishy' column as it's not needed for predictions.")

    # Preprocess features for prediction
    logging.info("Preprocessing features for prediction...")
    preprocessed_df = udp.preprocess_features(data, is_for_prediction=True)
    logging.info("Features preprocessed.")

    # Print preprocessed data information
    logging.info("Data types and column information after preprocessing:")
    logging.info(preprocessed_df.dtypes)

    # Convert DataFrame to the dictionary format for TensorFlow prediction
    logging.info("Converting DataFrame to dictionary format for TensorFlow prediction...")
    feature_dict = {name: np.array(value) for name, value in preprocessed_df.items()}
    logging.info("Conversion to dictionary format completed.")

    # Make predictions
    logging.info("Making predictions...")
    predictions = model.predict(feature_dict)
    logging.info("Predictions completed.")

    return predictions
