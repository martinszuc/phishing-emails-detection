# utils_string.py
import re
from bs4 import BeautifulSoup

from utils_config import Config


def clean_text(html_content):
    soup = BeautifulSoup(html_content, "html.parser")
    text = soup.get_text(separator=" ").lower()  # Convert to lower case
    text = re.sub(r'[^\w\s]', '', text)  # Remove punctuation
    return text

# You might also consider removing common stop words to focus on more meaningful words
def remove_stop_words(text):
    return ' '.join([word for word in text.split() if word not in Config.stop_words])