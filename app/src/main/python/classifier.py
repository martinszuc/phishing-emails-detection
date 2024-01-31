import joblib
import pandas as pd
import feature_extraction
from os.path import dirname, join

def load_model():
    # The path to the model file in the assets folder
    model_path = join(dirname(__file__), "email_classifier_model.pkl")

    # Load and return the trained model
    return joblib.load(model_path)

def predict_email(mbox_string):
    # Load the model
    model = load_model()

    # Process the mbox message to extract features
    features = feature_extraction.process_mbox_message(mbox_string)

    if not features:
        return "Unable to process email or extract features."

    # Convert features to DataFrame
    features_df = pd.DataFrame([features])

    # Ensure the columns match the model's expected input
    expected_cols = ['Html Form', 'Attachments', 'Flash content', 'Html iFrame',
                     'HTML content', 'URLs', 'External Resources', 'Javascript',
                     'Css', 'IPs in URLs', '@ in URLs', 'Encoding']
    features_df = features_df.reindex(columns=expected_cols)

    # Predict and return the result
    prediction = model.predict(features_df)[0]
    return "Phishing" if prediction else "Safe"