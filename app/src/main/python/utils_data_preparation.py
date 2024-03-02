import pandas as pd
import os
import numpy as np
import tensorflow as tf
from sklearn.model_selection import train_test_split

def load_datasets(resources_dir, safe_filename, phishing_filename):
    """Load datasets from specified paths and combine them."""
    safe_path = os.path.join(resources_dir, safe_filename)
    phishing_path = os.path.join(resources_dir, phishing_filename)
    
    safe_df = pd.read_csv(safe_path, index_col=0)
    phishing_df = pd.read_csv(phishing_path, index_col=0)
    
    combined_df = pd.concat([safe_df, phishing_df], ignore_index=True)
    return combined_df

def preprocess_features(combined_df, is_for_prediction=False):
    """Preprocess features for training or prediction."""

    # Drop 'Unnamed' columns if they exist
    combined_df = combined_df.loc[:, ~combined_df.columns.str.contains('^Unnamed')]
    
    # Define column types
    boolean_columns = ['html_form', 'flash_content', 'html_iframe', 'html_content', 'ips_in_urls', 'at_in_urls']
    numerical_columns = ['attachments', 'urls', 'external_resources', 'javascript', 'css']
    categorical_columns = ['encoding']
    common_encodings = ['7bit', '8bit', 'none', 'base64', 'binary', 'other']
    
    # One-hot encode categorical columns
    combined_df = pd.get_dummies(combined_df, columns=categorical_columns, dtype=np.float32)

    # Ensure all encoding columns are present
    for encoding in common_encodings:
        column_name = f'encoding_{encoding}'
        combined_df[column_name] = combined_df.get(column_name, 0.0)

    # Convert data types
    combined_df[boolean_columns + numerical_columns + [f'encoding_{enc}' for enc in common_encodings]] = \
        combined_df[boolean_columns + numerical_columns + [f'encoding_{enc}' for enc in common_encodings]].astype(np.float32)
    
    if not is_for_prediction and 'is_phishy' in combined_df.columns:
        combined_df['is_phishy'] = combined_df['is_phishy'].astype(np.float32)

    return combined_df

def split_data(combined_df, test_size=0.2, random_state=42):
    """Split the data into training and testing datasets."""
    return train_test_split(combined_df, test_size=test_size, random_state=random_state)

def df_to_dataset(dataframe, shuffle=True, batch_size=32):
    """Convert a dataframe into a TensorFlow dataset."""
    labels = dataframe.pop('is_phishy')
    ds = tf.data.Dataset.from_tensor_slices((dict(dataframe), labels))
    if shuffle:
        ds = ds.shuffle(buffer_size=len(dataframe))
    return ds.batch(batch_size)

def prepare_data_for_model(resources_dir, safe_filename, phishing_filename, test_size=0.2, random_state=42, batch_size=32):
    """Load, preprocess, and prepare data for the model."""
    combined_df = load_datasets(resources_dir, safe_filename, phishing_filename)
    preprocessed_df = preprocess_features(combined_df)
    train_df, test_df = split_data(preprocessed_df, test_size=test_size, random_state=random_state)
    train_ds = df_to_dataset(train_df, shuffle=True, batch_size=batch_size)
    test_ds = df_to_dataset(test_df, shuffle=False, batch_size=batch_size)
    return train_ds, test_ds
