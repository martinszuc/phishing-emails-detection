import pandas as pd
import tensorflow as tf
from sklearn.model_selection import train_test_split

def preprocess_dataframe(combined_df, test_size=0.2, random_state=42, batch_size=32, for_training=True):
    print("Preprocessing DataFrame. For training: {}".format(for_training))

    # Explicitly label columns as boolean, numerical, and categorical
    boolean_columns = ['html_form', 'flash_content', 'html_iframe', 'html_content', 'ips_in_urls', 'at_in_urls', 'is_phishy']
    numerical_columns = ['attachments', 'urls', 'external_resources', 'javascript', 'css']
    categorical_columns = ['encoding']  # Assuming one-hot encoding is needed

    print("Converting boolean columns to integers.")
    combined_df[boolean_columns] = combined_df[boolean_columns].astype(int)

    print("Performing one-hot encoding on the 'encoding' column.")
    combined_df = pd.get_dummies(combined_df, columns=categorical_columns, drop_first=True)

    if for_training:
        print("Splitting data for training and testing.")
        train, test = train_test_split(combined_df, test_size=test_size, random_state=random_state)

        print("Preparing feature columns for TensorFlow.")
        feature_columns = [tf.feature_column.numeric_column(col) for col in combined_df.columns if col != 'is_phishy']

        print("Converting DataFrame to TensorFlow Dataset for training and testing.")
        train_ds = df_to_dataset(train, shuffle=True, batch_size=batch_size)
        test_ds = df_to_dataset(test, shuffle=False, batch_size=batch_size)

        print("Preprocessing completed for training.")
        return train_ds, test_ds, feature_columns
    else:
        print("Preparing dataset for prediction.")
        ds = df_to_dataset_no_label(combined_df, batch_size=batch_size)

        print("Preprocessing completed for prediction.")
        return ds

def df_to_dataset(dataframe, shuffle=True, batch_size=32):
    print("Converting DataFrame to TensorFlow Dataset with labels.")
    dataframe = dataframe.copy()
    labels = dataframe.pop('is_phishy')
    ds = tf.data.Dataset.from_tensor_slices((dict(dataframe), labels))
    if shuffle:
        ds = ds.shuffle(buffer_size=len(dataframe))
    ds = ds.batch(batch_size)
    print("Dataset conversion completed.")
    return ds

def df_to_dataset_no_label(dataframe, batch_size=32):
    print("Converting DataFrame to TensorFlow Dataset without labels (for prediction).")
    dataframe = dataframe.copy()
    ds = tf.data.Dataset.from_tensor_slices(dict(dataframe))
    ds = ds.batch(batch_size)
    print("Dataset conversion for prediction completed.")
    return ds

def adjust_input_features(features):
    # Cast numerical features to tf.int64
    for num_feature in ['attachments', 'urls', 'external_resources', 'javascript', 'css']:
        features[num_feature] = tf.cast(features[num_feature], tf.int64)

    # Ensure encoded features are boolean
    encoded_features = ['encoding_7bit', 'encoding_8bit', 'encoding_base64', 'encoding_binary', 'encoding_none', 'encoding_other']
    for enc_feature in encoded_features:
        # Assuming encoded features are already in 0/1 format, just need casting to boolean
        features[enc_feature] = tf.cast(features[enc_feature], tf.bool)

    return features

# Modify the dataset preparation to adjust features
def prepare_dataset_for_prediction(features):
    adjusted_features = adjust_input_features(features)
    ds = tf.data.Dataset.from_tensor_slices((adjusted_features))
    ds = ds.batch(1)  # Adjust batch size as needed
    return ds

def validate_input_data(input_data):
    required_features = ['at_in_urls', 'attachments', 'css', ...]  # Add all required features
    missing_features = [feature for feature in required_features if feature not in input_data]
    if missing_features:
        raise ValueError(f"Missing features: {missing_features}")
    # Additional checks for data types could be added here

