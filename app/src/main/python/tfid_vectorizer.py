from sklearn.feature_extraction.text import TfidfVectorizer
import numpy as np

# Initialize a TfidfVectorizer
vectorizer = TfidfVectorizer()

def fit_corpus(corpus):
    # Learn the vocabulary and store TfidfVectorizer object
    global vectorizer
    vectorizer.fit(corpus)

def transform_text(text):
    # Transform text to tf-idf vector
    global vectorizer
    vector = vectorizer.transform([text])
    # Convert sparse matrix to dense array and flatten it
    return vector.toarray().flatten()