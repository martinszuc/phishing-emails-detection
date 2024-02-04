import os
import pandas as pd
import tensorflow as tf
import feature_extraction  # Make sure this module is correctly imported

# Define the encoding vocabulary
encoding_vocabulary = ['7bit', 'quoted-printable', 'none', '8bit', 'base64']

def load_model():
    # Load the model
    model_path = os.path.join(os.environ["HOME"], "classifier_tf_model")
    model = tf.keras.models.load_model(model_path)
    print("Model loaded successfully.")
    return model

def preprocess_email_data(email_data, encoding_vocabulary):
    print("Preprocessing email data...")

    # Convert to DataFrame
    email_df = pd.DataFrame([email_data])
    bool_cols = ['Html_Form', 'Flash_content', 'Html_iFrame', 'HTML_content', 'IPs_in_URLs', 'at_in_URLs']

    # Rename columns and convert boolean columns to integers
    email_df.columns = [col.replace(' ', '_').replace('@', 'at') for col in email_df.columns]
    bool_cols = [col for col in bool_cols if col in email_df.columns]
    email_df[bool_cols] = email_df[bool_cols].astype(int)

    # One-hot encode the 'Encoding' column
    email_df['Encoding'] = pd.Categorical(email_df['Encoding'], categories=encoding_vocabulary)
    one_hot_encoding = pd.get_dummies(email_df['Encoding'], prefix='Encoding')
    email_df = pd.concat([email_df.drop('Encoding', axis=1), one_hot_encoding], axis=1)

    print("Email data after preprocessing:", email_df.head())
    return email_df

def df_to_dataset_no_label(dataframe, batch_size=1):
    print("Converting DataFrame to TensorFlow Dataset...")
    dataframe = dataframe.copy()
    ds = tf.data.Dataset.from_tensor_slices((dict(dataframe)))
    ds = ds.batch(batch_size)
    return ds

def predict_email(mbox_string):
    print("Predicting email...")

    model = load_model()

    # Process the single email
    email_data = feature_extraction.process_single_email(mbox_string)
    if email_data is None:
        return "Invalid Email"

    # Preprocess and encode email data
    email_df = preprocess_email_data(email_data, encoding_vocabulary)

    # Convert DataFrame to TensorFlow Dataset for prediction
    email_ds = df_to_dataset_no_label(email_df)

    # Generate prediction
    prediction = model.predict(email_ds)

    print("Prediction result:", prediction)
    return "Phishing" if prediction[0][0] > 0.45 else "Safe"
