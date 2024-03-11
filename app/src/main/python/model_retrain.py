import os
import tensorflow as tf
import utils_data_preparation as udp
import utils_model as um

def retrain_model(model, train_dataset, epochs=10):
    """
    Retrain the provided model with the given dataset.
    """
    model.fit(train_dataset, epochs=epochs)
    return model

def process_and_retrain(resources_dir, safe_filename, phishing_filename, model_name, batch_size=32, epochs=10):
    """
    Process the data files into a format suitable for retraining the model, retrain the model,
    and save the retrained model with the provided model name.
    """
    # Prepare data for retraining without splitting into training and test sets
    train_ds = udp.prepare_data_for_retraining(resources_dir, safe_filename, phishing_filename, batch_size=batch_size)

    model_path = os.path.join(os.environ["HOME"], 'models', model_name)  # Model path is now derived from model_name
    # Load the model to be retrained
    model = tf.keras.models.load_model(model_path)

    # Retrain the model with the new data
    print("Retraining the model...")
    retrained_model = retrain_model(model, train_ds, epochs=epochs)

    # Save the retrained model
    retrained_model_path = os.path.join(os.environ["HOME"], 'models', model_name + '_retrained')
    um.save_model(retrained_model, retrained_model_path)
    print(f"Model retrained and saved at {retrained_model_path}.")

def main(resources_dir, safe_filename, phishing_filename, model_name):
    """
    Main function to process arguments and retrain the model.
    """
    resources_path = os.path.join(os.environ["HOME"], resources_dir)


    process_and_retrain(resources_path, safe_filename, phishing_filename, model_name)

# This allows the script to be imported without executing the main function,
# while still being executable through Chaquopy from Android.
if __name__ == '__main__':
    import sys
    # Expects four arguments: resources_dir, safe_filename, phishing_filename, model_name
    if len(sys.argv) == 5:
        main(sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4])
    else:
        print("Invalid arguments. Expected resources_dir, safe_filename, phishing_filename, and model_name.")
