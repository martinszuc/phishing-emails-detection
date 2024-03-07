import numpy as np
import pandas as pd
import tensorflow as tf
import utils_feature_extraction as ufe
import utils_data_preparation as udp
import utils_model as um

def retrain_model(model, train_dataset, epochs=10):
    """
    Retrain the provided model with the given dataset.
    """
    # Assuming the model has been compiled already, if not, you'll need to compile it before fitting.
    model.fit(train_dataset, epochs=epochs)
    return model

def process_and_retrain(resources_dir, safe_filename, phishing_filename, model_path='tf_model_saved', batch_size=32, epochs=10):
    """
    Process the raw mbox files into a format suitable for retraining the model, retrain the model, and save the retrained model.
    """
    # Prepare data for retraining without splitting into training and test sets
    train_ds = udp.prepare_data_for_retraining(resources_dir, safe_filename, phishing_filename, batch_size=batch_size)

    # Load the model to be retrained
    model = tf.keras.models.load_model(model_path)

    # Retrain the model with the new data
    print("Retraining the model...")
    retrained_model = retrain_model(model, train_ds, epochs=epochs)

    # Save the retrained model
    retrained_model_path = model_path + '_retrained'
    um.save_model(retrained_model, retrained_model_path)
    print(f"Model retrained and saved at {retrained_model_path}.")

def main():
    resources_dir = 'res_retrain'
    safe_filename = 'emails-samples-safe.mbox-export.csv'  # Adjusted for CSV format
    phishing_filename = 'emails-samples-phishing.mbox-export.csv'  # Adjusted for CSV format

    # Specify the path to the model to be retrained
    model_path = 'tf_model_saved'

    # Retrain the model and save the retrained version
    process_and_retrain(resources_dir, safe_filename, phishing_filename, model_path)

if __name__ == '__main__':
    main()
