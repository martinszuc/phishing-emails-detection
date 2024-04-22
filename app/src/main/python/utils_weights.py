import json
import numpy as np
import pandas as pd
import os
import tensorflow as tf
import utils_model as um
import utils_config as cfg
import logging

# Configure logging
logging.basicConfig(level=logging.DEBUG, format='%(asctime)s - %(levelname)s - %(message)s')

def serialize_model_weights(model_name):
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    weights_path = os.path.join(os.environ["HOME"], 'weights')
    os.makedirs(weights_path, exist_ok=True)

    model = tf.keras.models.load_model(model_path)
    weights = model.get_weights()

    # Log the shapes of weights for debugging
    for i, w in enumerate(weights):
        logging.debug(f"Layer {i} weight shape: {w.shape}")

    weights_list = [w.tolist() for w in weights]
    weights_json = json.dumps(weights_list)

    weights_filename = f"{model_name}.json"
    weights_file_path = os.path.join(weights_path, weights_filename)

    with open(weights_file_path, 'w') as weights_file:
        weights_file.write(weights_json)

    return weights_filename


def deserialize_and_load_model_weights(model_name, weights_file):
    # Construct the path to the model directory
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    logging.debug(f"Model path: {model_path}")

    # Build the model based on the defined architecture in utils_model
    model = um.build_model()
    logging.info("Model built successfully.")

    # Generate dummy input to initialize the model
    dummy_input = create_dummy_input()
    logging.debug("Dummy input created.")

    # Use the dummy input to initialize model weights
    model.predict(dummy_input)
    logging.info("Model initialized with dummy input.")

    # Path where the weights JSON file is stored
    weights_file_path = os.path.join(os.environ["HOME"], weights_file)
    logging.debug(f"Weights file path: {weights_file_path}")

    # Read the weights from the JSON file
    try:
        with open(weights_file_path, 'r') as file:
            weights_json = file.read()
        logging.debug("Weights file read successfully.")
    except IOError as e:
        logging.error(f"Error reading weights file: {e}")
        return

    # Deserialize the JSON string to Python objects
    weights_list = json.loads(weights_json)
    logging.debug("Weights JSON deserialized.")

    # Convert list to numpy arrays and check shapes
    weights = [np.array(w) for w in weights_list]
    for i, w in enumerate(weights):
        logging.debug(f"Loaded Layer {i} weight shape: {w.shape}, expected shape: {model.get_weights()[i].shape}")

    # Set weights to the model if they match expected shapes
    if all(w.shape == model.get_weights()[i].shape for i, w in enumerate(weights)):
        model.set_weights(weights)
        logging.info("Weights set successfully.")
        model.save(model_path)
        logging.info(f"Model saved successfully at {model_path}")
    else:
        logging.error("Mismatch in weight shapes; cannot set weights.")

def create_dummy_input():
    config = cfg.Config()  # Assuming your configuration settings are accessible like this

    logging.debug("Creating dummy input for categorical features.")
    dummy_data = {}

    # For categorical features, assign a unique value to each category
    for feature, categories in config.CATEGORICAL_FEATURES.items():
        for i, category in enumerate(categories):
            dummy_data[f"{feature}_{category}"] = [i + 1]  # Assign a unique value to each category
            logging.debug(f"Feature '{feature}': Assigned value {i + 1} to category '{category}'.")


    logging.debug("Creating dummy input for numerical and boolean features.")
    # For numerical and boolean features, use zeros or a small number
    for feature in config.NUMERICAL_FEATURES + config.BOOLEAN_FEATURES:
        dummy_data[feature] = [0.0]  # Use float zeros
        logging.debug(f"Feature '{feature}': Set to zero.")

    # Convert dictionary to DataFrame to handle input to the model
    # Check the lengths of arrays in dummy_data
    array_lengths = {key: len(value) for key, value in dummy_data.items()}
    logging.debug("Lengths of arrays in dummy_data: {}".format(array_lengths))

    # Create the DataFrame if all arrays have the same length
    if len(set(array_lengths.values())) == 1:
        dummy_df = pd.DataFrame(dummy_data)
        logging.debug("Dummy DataFrame created with columns: {}".format(dummy_df.columns.tolist()))
    else:
        logging.error("All arrays in dummy_data must be of the same length.")

    # Return the dummy data formatted as model input with float32
    formatted_dummy_data = {key: np.array(value, dtype=np.float32) for key, value in dummy_df.items()}
    logging.debug("Formatted dummy data for model input.")
    return formatted_dummy_data
