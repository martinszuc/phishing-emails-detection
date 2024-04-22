import logging
import os
import sys
import tensorflow as tf

import utils_data_preparation as udp
import utils_model as um
import utils_weights as uw

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

def load_and_initialize_model(model_name):
    """
    Load the original model, create a dummy input to initialize weights, and return the initialized model.
    """
    model_path = os.path.join(os.environ["HOME"], 'models', model_name)
    logging.info(f"Loading the original model from {model_path}")
    model = um.build_model()  # Rebuild the model

    # Create a dummy input to initialize the model
    dummy_input = uw.create_dummy_input()
    logging.info("Initializing model with dummy input to set the weights...")
    model.predict(dummy_input)  # Use predict to initialize the model

    # Load the original model weights
    original_model = tf.keras.models.load_model(model_path)
    model.set_weights(original_model.get_weights())
    logging.info("Weights loaded from the original model.")

    return model

def retrain_model(model, train_dataset, epochs=10):
    """
    Retrain the model with the specified training dataset.
    """
    logging.info("Starting to retrain the model.")
    model.fit(train_dataset, epochs=epochs)
    logging.info("Model retraining completed.")
    return model

def process_and_retrain(resources_dir, safe_filename, phishing_filename, model_name, batch_size=32, epochs=10):
    logging.info("Preparing data for retraining...")
    train_ds = udp.prepare_data_for_retraining(resources_dir, safe_filename, phishing_filename, batch_size=batch_size)

    logging.info("Initializing and loading weights to the new model...")
    model = load_and_initialize_model(model_name)

    logging.info("Retraining the new model with new data...")
    retrained_model = retrain_model(model, train_ds, epochs=epochs)

    retrained_model_path = os.path.join(os.environ["HOME"], 'models', model_name + '_retrained')
    logging.info(f"Saving the retrained model at {retrained_model_path}...")
    retrained_model.save(retrained_model_path)
    logging.info("New model retrained with original weights and saved successfully.")

def main(resources_dir, safe_filename, phishing_filename, model_name):
    resources_path = os.path.join(os.environ["HOME"], resources_dir)
    logging.info("Starting the retraining process...")
    process_and_retrain(resources_path, safe_filename, phishing_filename, model_name)

if __name__ == '__main__':
    if len(sys.argv) == 5:
        main(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
    else:
        logging.error("Invalid arguments. Expected resources_dir, safe_filename, phishing_filename, and model_name.")
