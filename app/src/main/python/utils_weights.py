import os
import json
import numpy as np
import tensorflow as tf


def serialize_model_weights(model_path):
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


def deserialize_and_load_model_weights(model_name, weights_json):
    """
    Load weights from a JSON string into a TensorFlow model.
    """
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    model = tf.keras.models.load_model(model_path)
    weights_list = json.loads(weights_json)
    weights = [np.array(w) for w in weights_list]
    model.set_weights(weights)
    model.save(model_path)  # Save the updated model at the same path
