# utils_data_preparation.py
import numpy as np
import os
import pandas as pd
import tensorflow as tf
# noinspection PyUnresolvedReferences
from sklearn.model_selection import train_test_split

import utils_config as cfg


def load_datasets(resources_dir, safe_filename, phishing_filename):
    """Load datasets from specified paths, ensure equal size, and combine them."""
    safe_path = os.path.join(resources_dir, safe_filename)
    phishing_path = os.path.join(resources_dir, phishing_filename)

    safe_df = pd.read_csv(safe_path, index_col=0)
    phishing_df = pd.read_csv(phishing_path, index_col=0)

    # Determine the minimum size between both datasets
    min_size = min(len(safe_df), len(phishing_df))

    # Truncate both datasets to the minimum size
    safe_df = safe_df.sample(n=min_size, random_state=42).reset_index(drop=True)
    phishing_df = phishing_df.sample(n=min_size, random_state=42).reset_index(drop=True)

    # Combine the datasets
    combined_df = pd.concat([safe_df, phishing_df], ignore_index=True)
    return combined_df


def preprocess_features(combined_df, is_for_prediction=False):
    config = cfg.Config()

    # Drop 'Unnamed' columns if they exist
    combined_df = combined_df.loc[:, ~combined_df.columns.str.contains('^Unnamed')]

    # One-hot encode categorical columns
    for feature, categories in config.CATEGORICAL_FEATURES.items():
        for category in categories:
            column_name = f"{feature}_{category}"
            combined_df[column_name] = (combined_df[feature] == category).astype(int)

        # After processing each feature, drop the original column to avoid redundancy
        if feature in combined_df:
            combined_df.drop(columns=[feature], inplace=True)

    # Convert data types
    dtype_columns = config.NUMERICAL_FEATURES + config.BOOLEAN_FEATURES + [f"{feat}_{cat}" for
                                                                           feat, cats in
                                                                           config.CATEGORICAL_FEATURES.items()
                                                                           for cat in cats]
    combined_df[dtype_columns] = combined_df[dtype_columns].astype(np.float32)

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


def prepare_data_for_model(resources_dir, safe_filename, phishing_filename, test_size=0.2,
                           random_state=42, batch_size=32):
    """Load, preprocess, and prepare data for the model."""
    combined_df = load_datasets(resources_dir, safe_filename, phishing_filename)
    preprocessed_df = preprocess_features(combined_df)
    train_df, test_df = split_data(preprocessed_df, test_size=test_size, random_state=random_state)
    train_ds = df_to_dataset(train_df, shuffle=True, batch_size=batch_size)
    test_ds = df_to_dataset(test_df, shuffle=False, batch_size=batch_size)
    return train_ds, test_ds


def prepare_data_for_retraining(resources_dir, safe_filename, phishing_filename, batch_size=32):
    """Load, preprocess, and prepare data for retraining the model without splitting."""
    combined_df = load_datasets(resources_dir, safe_filename, phishing_filename)
    preprocessed_df = preprocess_features(combined_df)
    train_ds = df_to_dataset(preprocessed_df, shuffle=True, batch_size=batch_size)
    return train_ds
