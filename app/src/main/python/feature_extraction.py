import mailbox
import pandas as pd
import re
import csv
import os
import utils
import config
from bs4 import BeautifulSoup
from abc import ABC, abstractmethod
from io import StringIO


class FeatureFinder(ABC):

    @abstractmethod
    def getFeatureTitle(self):
        pass

    @abstractmethod
    def getFeature(self, message):
        pass

class HTMLFormFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "html_form"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        return re.compile(r'<\s?\/?\s?form\s?>', re.IGNORECASE).search(payload) is not None

class IFrameFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "html_iframe"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        return re.compile(r'<\s?\/?\s?iframe\s?>', re.IGNORECASE).search(payload) is not None

class FlashFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "flash_content"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        swflinks = re.compile(config.FLASH_LINKED_CONTENT, re.IGNORECASE).findall(payload)
        flashObject = re.compile(r'embed\s*src\s*=\s*\".*\.swf\"', re.IGNORECASE).search(payload)
        return (swflinks is not None and len(swflinks) > 0) or (flashObject is not None)

class AttachmentFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "attachments"

    def getFeature(self, message):
        return utils.getAttachmentCount(message)

class HTMLContentFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "html_content"

    def getFeature(self, message):
        return utils.ishtml(message)

class URLsFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "urls"

    def getFeature(self, message):
        return len(utils.geturls_payload(message))

class ExternalResourcesFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "external_resources"

    def getFeature(self, message):
        return len(utils.getexternalresources(message))

class JavascriptFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "javascript"

    def getFeature(self, message):
        return len(utils.getjavascriptusage(message))

class CssFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "css"

    def getFeature(self, message):
        return len(utils.getcssusage(message))

class IPsInURLs(FeatureFinder):
    def getFeatureTitle(self):
        return "ips_in_urls"

    def getFeature(self, message):
        return len(utils.getIPHrefs(message)) > 0

class AtInURLs(FeatureFinder):
    def getFeatureTitle(self):
        return "at_in_urls"

    def getFeature(self, message):
        emailPattern = re.compile(config.EMAILREGEX, re.IGNORECASE)
        for url in utils.geturls_payload(message):
            if url.lower().startswith("mailto:") or (emailPattern.search(url) and emailPattern.search(url).group()):
                continue
            atvalue = url.find("@")
            athexvalue = url.find("%40")
            atvalue = min(atvalue, athexvalue) if atvalue != -1 and athexvalue != -1 else max(atvalue, athexvalue)
            paramindex = url.find("?")
            if paramindex != -1 and atvalue != -1 and paramindex > atvalue:
                return True
            elif atvalue != -1:
                return True
        return False

class EncodingFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "encoding"

    def getFeature(self, message):
        encoding = message.get('content-transfer-encoding')
        common_encodings = ['7bit', '8bit', 'none', 'quoted_printable', 'base64', 'binary']

        if encoding:
            # Normalize the encoding string by making it lowercase and stripping white spaces and line breaks
            encoding = encoding.strip().lower().replace('\r', '').replace('\n', '')

            # Sanitize the encoding name first
            encoding = re.sub(r'\s+', ' ', encoding)  # Replace multiple spaces with a single space
            encoding = encoding.replace('/', '_').replace(';', '').replace(':', '').replace(' ', '_')

            # Normalize different variations of '7bit' and '8bit' to a standard form
            encoding = '7bit' if '7bit' in encoding else encoding
            encoding = '8bit' if '8bit' in encoding else encoding

            # Check if the encoding is one of the common encodings, else classify as 'other'
            encoding = encoding if encoding in common_encodings else 'other'

            return encoding
        return 'none'


class AlexaRankFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "alexa_rank"

    def getFeature(self, message):
        urls = utils.geturls_payload(message)
        ranks = []
        for url in urls:
            rank = utils.get_alexa_rank(url)
            if rank != -1:
                ranks.append(rank)
        return ranks if ranks else ['no_rank']

def processMboxFile(mbox_file_path, phishy=True, limit=500):
    """
    Processes an mbox file and extracts features for email classification.

    Parameters:
    - mbox_file_path: The path to the mbox file.
    - phishy: Flag indicating if the emails are considered phishing (True) or not (False).
    - limit: The maximum number of emails to process.

    Returns:
    A pandas DataFrame containing the extracted features.
    """
    data = []
    email_index = []
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
               IFrameFinder(), HTMLContentFinder(), URLsFinder(),
               ExternalResourcesFinder(), JavascriptFinder(),
               CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

    try:
        mbox = mailbox.mbox(mbox_file_path)
    except Exception as e:
        print(f"Error processing mbox file: {e}")
        return pd.DataFrame()  # Return an empty DataFrame on error

    for i, message in enumerate(mbox, start=1):
        if i > limit:
            break
        payload = utils.getpayload_dict(message)
        if sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload) < 1:
            continue

        email_data = {finder.getFeatureTitle(): finder.getFeature(message) for finder in finders}
        email_data["is_phishy"] = phishy
        data.append(email_data)

        try:
            email_raw = message.as_bytes().decode('utf-8', errors='replace')
            email_index.append({"id": i, "message": utils.getpayload_dict(message), "raw": email_raw})
        except (UnicodeEncodeError, AttributeError):
            continue

    # Create DataFrame from the extracted data
    df_data = pd.DataFrame(data)
    # df_email_index = pd.DataFrame(email_index)  # Optionally return or use email index information

    # Return the processed DataFrame
    return df_data