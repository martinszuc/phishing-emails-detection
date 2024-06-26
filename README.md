# Phishing Emails Detection

This project aims to detect phishing emails using federated learning for OS Android. The application processes emails for feature extraction and uses those features in a machine learning process as a dynamicly created datasets for phishing email classification. It also allows training and retraining of the model on new data, evaluating models, and includes a federated server for model`s weight management.

![fl](https://github.com/martinszuc/phishing-emails-detection/assets/100486753/7965ae5e-9ff2-4d95-a1f4-9390d5733c72)

## Table of Contents

- [Installation](#installation)
  - [Android App](#android-app)
  - [Federated Server](#federated-server)
- [Usage](#usage)
  - [Email Processing](#email-processing)
  - [Machine Learning](#machine-learning)
  - [Phishing Detection](#phishing-detection)
  - [Federated Server Usage](#federated-server-usage)
- [Features](#features)
- [App Architecture](#architecture)
- [Python Component](#python-component)
  - [Feature Finders and Detection Strategy](#feature-finders-and-detection-strategy)
- [Contributing](#contributing)
- [License](#license)


## Installation
### Android App

To install and set up the Android application, follow these steps:

1. **Clone the repository:**
   ```bash
    git clone https://github.com/your-username/phishing-emails-detection.git
    ```
2. Install the app through Android Studio:

3. Open the cloned project in Android Studio.
4. Set up debug key:
  - Open `File` -> `Project Structure`.
  - Navigate to `SDK Location` -> `Debug keystore`.
  - Set the path to the `debug.keystore` file in the root directory.
5. Build and run the app:
  - Click `Run` -> `Run 'app'`.
  - Choose your device or an emulat
Note: This app is currently in development mode and limited to test users.

For test access, contact [matoszuc@gmail.com](mailto:matoszuc@gmail.com).

### Federated Server

To set up the federated server, follow these steps:

#### Prerequisites

- Python 3.8
- pip3

1. Create and activate a Python virtual environment:
   ```sh
     cd server
     python3.8 -m venv env_server
     source ./env_server/bin/activate
   ```
2. Install dependencies and run the server:
   ```
     pip install -r requirements.txt
     python server.py
   ```
## Usage

### Email Processing

The app can import emails from various sources and process them for feature extraction.

#### Import Emails

- **Gmail Import**: Users can use their Google account to import emails directly from Gmail.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/a835b65f-7025-4869-b095-ab002a7e3677" alt="Gmail Import" height="600">

- **EML Import**: Users can import individual `.eml` files.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/29489c64-02ca-4279-ae59-07021992cda1" alt="EML Import" height="600">

- **MBOX Import**: Users can import `.mbox` files containing multiple emails.

When importing, users are asked to label the emails as `phishing` or `safe`.

#### Email Packaging

- **Email Packaging**: Users can combine multiple emails into packages for processing.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/48a18d59-4438-4699-9ffc-7e2f25dd291c" alt="Email Packaging" height="600">

### Machine Learning

The app provides several features for machine learning, including feature extraction, training, and retraining.

#### Feature Extraction

- **Feature Extraction**: Users can extract features from emails using Python integration.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/b2e7e604-f226-450d-9d92-cb0418f086a2" alt="Feature Extraction" height="600">

#### Training

- **Training**: Users can train the model on the extracted features.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/4e182d17-d32d-4b26-93bb-267ec4c5cbac" alt="Training" height="600">

#### Retraining

- **Retraining**: Users can retrain the model with new data.
 <img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/368f8a85-4a48-4bcd-91e3-ca00dc5c19cc" alt="Retraining" height="600">

#### Model Evaluation

- **Model Evaluation**: Users can evaluate the performance of the trained model.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/ac0560df-922f-4ee4-bc8f-f7e50f0cce14" alt="Model Evaluation" height="600">

### Phishing Detection

- **Phishing Detection**: Users can use the selected model to classify a single email as phishing or safe using logistic regression.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/8a2c7223-3099-47f4-8f63-b5680b044a8b" alt="Phishing Detection" height="600">

### Federated Server Usage

The federated server handles weight management for federated learning.

#### Endpoints

- **Upload Weights**: Users can upload the local model weights.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/2178f6cf-712e-42d1-969c-0fbf4ee116e5" alt="Weights Upload" height="600">

- **Download Global Weights**: Users can download the globally averaged weights.

- **Check Server Status**: Users can ping the server to check its status.
<img src="https://github.com/martinszuc/phishing-emails-detection/assets/100486753/3a83af5c-910c-4bc8-ad5d-675a95baf7fe" alt="Server Status" height="600">

## Features
- **Google Login**: Users can log in using their Google account.
- **Logout**: Users can log out from their account.
- **Integration with Gmail API**: Seamless integration with Gmail API for importing emails.
- **Email Import**: Users can import emails from Gmail, `.eml`, and `.mbox` files.
- **Email Labeling**: Users can label imported emails as `phishing` or `safe`.
- **Email Packaging**: Combine multiple emails into packages for processing.
- **Feature Extraction**: Extract features from emails using integrated Python scripts.
- **Machine Learning**:
  - **Training**: Train the model on extracted features.
  - **Retraining**: Retrain the model with new data.
  - **Model Evaluation**: Evaluate the performance of trained models.
- **Phishing Detection**: Classify individual emails as phishing or safe using logistic regression.
- **Federated Learning**:
  - **Upload Weights**: Upload local model weights to the federated server.
  - **Download Weights**: Download globally averaged weights from the server.
  - **Server Status**: Check the operational status of the federated server.
  - **Set Federated Server IP**: Dynamically set the IP address of the federated server.

## Architecture

The project is structured to separate concerns and ensure modularity. Below is an overview of the main directories and their purposes:

### Key Components:

- **Data**: Contains data-related classes, repositories, and entities for handling email data.
    - **Local**: Local data sources and caches.
    - **Remote**: Manages remote data sources, such as API calls.
    - **Repositories**: Interfaces for data access and management.
    - **Auth**: Handles user authentication.
    - **DB**: Database configurations and access.
      - **Entity**: Entity classes representing different data models such as `EmailFull`, `EmailMinimal`, `EmailPackageMetadata`, etc.

- **Python**: Contains Python scripts and modules for machine learning and data processing.
  - **DataProcessing**: Scripts for processing email data.
  - **EvaluateModel**: Scripts for evaluating models.
  - **Prediction**: Scripts for making predictions.
  - **Retraining**: Scripts for retraining models.
  - **Training**: Scripts for training models.
  - **WeightManager**: Manages model weights.
  - **PythonSingleton**: Singleton class for Python which starts and holds Python interpreter.

- **DI**: Dependency injection modules.
  - **AppModule**: Provides application-wide dependencies.
  - **DatabaseModule**: Provides database-related dependencies.
  - **NetworkModule**: Provides network-related dependencies.

- **UI**: User interface components.
  - **Base**: Base classes for UI components.
  - **component**: Specific UI components for authentication, email detection, machine learning, and settings.
  - **App**: Main application class.
  - **MainActivity**: Main activity of the application.
  - **Utils**: Utility classes and functions.
 
## Python Component
### Feature Finders and Detection Strategy

Our phishing detection uses several feature finders, each responsible for extracting specific elements from emails that are commonly used by phishing attempts:

- **HTMLFormFinder**: Identifies HTML forms within emails, a common phishing vector to solicit user information.
- **IFrameFinder**: Detects the use of IFrames, potentially embedding malicious content invisibly.
- **FlashFinder**: Searches for Flash content links, which could execute harmful scripts.
- **AttachmentFinder**: Counts email attachments, which may contain malicious payloads.
- **HTMLContentFinder**: Looks for specific HTML content indicative of phishing.
- **URLsFinder**: Extracts and evaluates URLs found within emails for malicious links.
- **ExternalResourcesFinder**: Identifies external resources linked within emails that could be harmful.
- **JavascriptFinder**: Detects JavaScript, which can be used in phishing for malicious activities.
- **CssFinder**: Searches for custom CSS that might be used to disguise phishing attempts.
- **IPsInURLs**: Checks for IP addresses in URLs, a technique used to bypass domain name suspicion.
- **AtInURLs**: Identifies '@' symbols in URLs, which can be a sign of deceptive links.
- **EncodingFinder**: Analyzes the content encoding for signs of obfuscation or unusual patterns.

### Acknowledgments and References

This project builds upon and extends the work found at [MachineLearningPhishing](https://github.com/diegoocampoh/MachineLearningPhishing) by Diego Ocampo.

### Data Sources

The data used for training the phishing detection model were sourced from two main repositories, which provided a rich dataset of phishing emails:

- [Phishing Pot Dataset](https://github.com/rf-peixoto/phishing_pot) by rf-peixoto (converted .eml to mbox using scripts in this repo)
- [Phishing Dataset](https://monkey.org/~jose/phishing/) by jose at monkey.org (downloaded mbox files)

## Contributing

If you want to contribute to this project, please follow these guidelines:

1. Fork the repository.
2. Create a new branch.
3. Make your changes and commit them.
4. Push your changes to your fork.
5. Create a pull request.

## License

This project is licensed under the MIT License. See the LICENSE file for details.
