import multiprocessing
import multiprocessing.dummy
import numpy as np
from sklearn.feature_extraction.text import TfidfVectorizer
import joblib
from os.path import dirname, join

# Use dummy multiprocessing for threading
multiprocessing = multiprocessing.dummy

file = join(dirname(__file__), "tfidf_vectorizer.pkl")


# Load the TfidfVectorizer from a .pkl file

def transform_text(text):
    """
    Transforms the input text to a tf-idf vector using the loaded vectorizer.
    The output is a flattened array.
    """
    return vectorizer.transform([text]).toarray().flatten()

# Initialize a new TfidfVectorizer
vectorizer = TfidfVectorizer()
vectorizer = joblib.load(file)

def fit_corpus(corpus):
    """
    Fits the TfidfVectorizer to the input corpus.
    """
    global vectorizer
    vectorizer.fit(corpus)

def transform_text(text):
    """
    Transforms the input text to a tf-idf vector using the fitted vectorizer.
    The output is a flattened array.
    """
    global vectorizer
    vector = vectorizer.transform([text])
    return vector.toarray().flatten()
