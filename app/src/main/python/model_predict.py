import numpy as np
import pandas as pd
import os
import tensorflow as tf
import utils_feature_extraction as ufe
import utils_data_preparation as udp

def predict_on_mbox(model_name, filename):

    model_path = os.path.join(os.environ["HOME"], 'models', model_name)

    # Load the trained model
    model = tf.keras.models.load_model(model_path)

#
    output_path = os.path.join(os.environ["HOME"], 'prediction_extracted')
    mbox_path = os.path.join(os.environ["HOME"], 'prediction_emails', filename)

    # Load and preprocess mbox file
    csv_filename = ufe.process_mbox_to_csv(mbox_path, "iso-8859-1", output_path, limit=200, is_phishy=None)

    processed_csv_path = os.path.join(output_path, csv_filename)


    # Load features from the processed CSV
    data = pd.read_csv(processed_csv_path)

    print(f"Number of emails processed: {data.shape[0]}")
    print("Sample of loaded data:")
    print(data.head())

    # Information on data types and columns
    print("\nColumns and their data types:")
    print(data.dtypes)

    # Drop 'is_phishy' column if it exists (it's not needed for prediction)
    if 'is_phishy' in data.columns:
        data.drop(columns=['is_phishy'], inplace=True)

    # Preprocess features for prediction
    preprocessed_df = udp.preprocess_features(data, is_for_prediction=True)

    # Print preprocessed data information
    print("\nColumns and their data types after preprocessing:")
    print(preprocessed_df.dtypes)
    print(preprocessed_df.head())

    # Convert DataFrame to the dictionary format for TensorFlow prediction
    feature_dict = {name: np.array(value) for name, value in preprocessed_df.items()}

    # Make predictions
    predictions = model.predict(feature_dict)
    return predictions
