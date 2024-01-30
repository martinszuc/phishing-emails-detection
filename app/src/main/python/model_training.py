import pandas as pd
from sklearn.model_selection import train_test_split
from sklearn.linear_model import LogisticRegression
from sklearn.metrics import classification_report, confusion_matrix
from sklearn.preprocessing import StandardScaler, OneHotEncoder
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.feature_extraction.text import TfidfVectorizer
import os

# Define the directory containing the mbox files.
resources_dir = 'res'

# Merge Enron and phishing datasets into a single DataFrame
enron_df = pd.read_csv(os.path.join(resources_dir, 'emails-enron.mbox-export.csv'))
phishing_df = pd.read_csv(os.path.join(resources_dir, 'emails-phishing.mbox-export.csv'))
combined_df = pd.concat([enron_df, phishing_df], ignore_index=True)

# Convert boolean columns to integer values
bool_cols = ['Html Form', 'Flash content', 'Html iFrame', 'HTML content', 'IPs in URLs', '@ in URLs', 'Phishy']
for col in bool_cols:
    combined_df[col] = combined_df[col].astype(int)

# Define categorical and numerical columns for preprocessing
categorical_cols = ['Encoding']  # Columns to be one-hot encoded
numerical_cols = [col for col in combined_df.columns if col not in categorical_cols + ['Phishy', 'Unnamed: 0']]

# Construct preprocessing pipelines for both numerical and categorical data
numeric_transformer = StandardScaler()
categorical_transformer = OneHotEncoder(handle_unknown='ignore')
preprocessor = ColumnTransformer(
    transformers=[
        ('num', numeric_transformer, numerical_cols),
        ('cat', categorical_transformer, categorical_cols)
    ])

# Create a logistic regression pipeline with preprocessing steps
model = Pipeline(steps=[
    ('preprocessor', preprocessor),
    ('classifier', LogisticRegression())
])

# Split the dataset into features (X) and target (y)
X = combined_df.drop('Phishy', axis=1)
y = combined_df['Phishy']

# Split the dataset into training and testing sets
X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, stratify=y, random_state=9)

# Fit the model to the training data
model.fit(X_train, y_train)

# Get the probabilities of each class
y_pred_proba = model.predict_proba(X_test)

# Set threshold
threshold = 0.4

# Apply the threshold to the probabilities to get the final predictions
y_pred = (y_pred_proba[:, 1] >= threshold).astype(int)

# Print the classification report
print("Classification Report:")
print(classification_report(y_test, y_pred))

# Print the confusion matrix
print("Confusion Matrix:")
print(confusion_matrix(y_test, y_pred))