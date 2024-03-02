import os
import pandas as pd
import tensorflow as tf
from feature_extraction import processMboxFile
from data_preprocessing import preprocess_dataframe, df_to_dataset_no_label

model_path = os.path.join(os.environ["HOME"], "classifier_tf_model")

def load_model():
    print("Loading model...")
    model = tf.keras.models.load_model(model_path)
    print("Model loaded successfully.")
    return model

def predict_email(mbox_file_path):
    print(f"Starting email prediction for file: {mbox_file_path}")

    # Load the trained model
    model = load_model()

    # Process the mbox file to extract features into a DataFrame
    print("Extracting features from mbox file...")
    email_data = processMboxFile(mbox_file_path)

    if email_data.empty:
        print("No emails processed or DataFrame is empty.")
        return "Invalid Email"

    # Preprocess the extracted features for prediction
    print("Preprocessing email data...")
    email_ds = preprocess_dataframe(email_data, for_training=False)  # Make sure this returns a TensorFlow Dataset ready for prediction

    # Predict
    print("Making predictions...")
    predictions = model.predict(email_ds)
    prediction_results = ["Phishing" if pred > 0.5 else "Safe" for pred in predictions.flatten()]

    print("Prediction completed.")
    return prediction_results

def preprocess_dataframe(email_data, for_training=True):
    """
    Adjust this function based on your data_preprocessing.py script.
    This function should handle both training and prediction preprocessing steps.
    For prediction, it should prepare the dataset without labels and adjust the features as required by the trained model.
    """
    if for_training:
        # Perform operations for training data preprocessing
        pass
    else:
        # For prediction: Preprocess the data without splitting
        # Ensure the data is formatted as expected by the model
        email_ds = df_to_dataset_no_label(email_data, batch_size=1)  # Assuming batch size of 1 for prediction
        return email_ds

def df_to_dataset_no_label(dataframe, batch_size=32):
    """
    Converts the DataFrame to a TensorFlow Dataset without labels.
    Adjust as per your implementation in data_preprocessing.py.
    """
    print("Converting DataFrame to TensorFlow Dataset for prediction.")
    ds = tf.data.Dataset.from_tensor_slices((dict(dataframe)))
    ds = ds.batch(batch_size)
    return ds