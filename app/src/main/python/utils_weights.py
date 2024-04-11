import json
import numpy as np
import os
import tensorflow as tf


def serialize_model_weights(model_name):
    """
    Load a TensorFlow model from the specified path, extract its weights,
    and serialize the weights to a JSON-compatible format, saving the result to a file.

    Parameters:
    - model_name: The name of the model.

    Returns:
    - The filename of the file containing the serialized model weights.
    """
    try:
        model_path = os.path.join(os.environ["HOME"], 'models', model_name)
        weights_path = os.path.join(os.environ["HOME"], 'weights')
        os.makedirs(weights_path, exist_ok=True)  # Ensure the weights directory exists

        model = tf.keras.models.load_model(model_path)  # Load the model

        # Extract weights and convert numpy arrays to lists for JSON serialization
        weights_list = [w.tolist() for w in model.get_weights()]
        weights_json = json.dumps(weights_list)

        # Define the filename for the weights file
        weights_filename = f"{model_name}.json"
        # Define the full path for the weights file
        weights_file_path = os.path.join(weights_path, weights_filename)

        # Write the JSON string to the file
        with open(weights_file_path, 'w') as weights_file:
            weights_file.write(weights_json)

        # Return only the filename
        return weights_filename
    except Exception as e:
        print(f"Error serializing model weights to file: {str(e)}")
        return None


def deserialize_and_load_model_weights(model_name, weights_file):
    """
    Load weights from a JSON file into a TensorFlow model.

    Parameters:
    - model_name: The name of the model.
    - weights_file: Relative file path from the user's home directory to the JSON file containing the weights.
    """
    try:
        model_path = os.path.join(os.environ["HOME"], 'models', model_name)
        model = tf.keras.models.load_model(model_path)  # Load the model

        weights_file_path = os.path.join(os.environ["HOME"], weights_file)
        with open(weights_file_path, 'r') as file:  # Proper file handling
            weights_json = file.read()

        # Deserialize and convert the list back into numpy arrays
        weights_list = json.loads(weights_json)
        weights = [np.array(w) for w in weights_list]
        model.set_weights(weights)  # Set the model's weights

        model.save(model_path)  # Save the updated model

        # Consider if you really want to delete the weights file. If the app crashes,
        # you might want to keep it for debugging.
        # os.remove(weights_file_path)  # Clean up
    except Exception as e:
        print(f"Error loading model weights: {str(e)}")

