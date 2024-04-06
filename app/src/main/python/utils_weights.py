import os
import json
import numpy as np
import tensorflow as tf


def serialize_model_weights(model_name):
    """
    Load a TensorFlow model from the specified path, extract its weights,
    and serialize the weights to a JSON-compatible format.

    Parameters:
    - model_path: Relative path to the model from the user's home directory.

    Returns:
    - A JSON string representing the model's weights.
    """
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    model = tf.keras.models.load_model(model_path)   # Load the model
    # Extract weights
    weights = model.get_weights()
    # Convert numpy arrays to lists for JSON serialization
    weights_list = [w.tolist() for w in weights]
    # Serialize to JSON
    weights_json = json.dumps(weights_list)

    return weights_json


def deserialize_and_load_model_weights(model_name, weights_file):
    """
    Load weights from a JSON file into a TensorFlow model.

    Parameters:
    - model_name: The name of the model directory.
    - weights_file: The file path to the temporary JSON file containing the weights.
    """
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)     # Construct the full path to the model directory
    model = tf.keras.models.load_model(model_path)     # Load the model
    weights_file_path = os.path.join(os.environ["HOME"], weights_file)     # Construct the full path to the weights file
    with open(weights_file_path, 'r') as file:          # Open and read the weights JSON file
        weights_json = file.read()     # Deserialize the JSON string into Python objects
    weights_list = json.loads(weights_json)    # Convert the lists back into numpy arrays
    weights = [np.array(w) for w in weights_list]
    model.set_weights(weights)     # Set the model's weights,
    model.save(model_path) # Save the updated model
    os.remove(weights_file_path) # Optionally, delete the temporary weights file to clean up