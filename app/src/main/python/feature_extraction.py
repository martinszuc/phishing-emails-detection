import mailbox
import pandas as pd
import re
import csv
import os
import utils
import config
from bs4 import BeautifulSoup
from abc import ABC, abstractmethod

class FeatureFinder(ABC):

    @abstractmethod
    def getFeatureTitle(self):
        pass

    @abstractmethod
    def getFeature(self, message):
        pass

class HTMLFormFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Html Form"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        return re.compile(r'<\s?\/?\s?form\s?>', re.IGNORECASE).search(payload) is not None

class IFrameFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Html iFrame"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        return re.compile(r'<\s?\/?\s?iframe\s?>', re.IGNORECASE).search(payload) is not None

class FlashFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Flash content"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        swflinks = re.compile(config.FLASH_LINKED_CONTENT, re.IGNORECASE).findall(payload)
        flashObject = re.compile(r'embed\s*src\s*=\s*\".*\.swf\"', re.IGNORECASE).search(payload)
        return (swflinks is not None and len(swflinks) > 0) or (flashObject is not None)

class AttachmentFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Attachments"

    def getFeature(self, message):
        return utils.getAttachmentCount(message)

class HTMLContentFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "HTML content"

    def getFeature(self, message):
        return utils.ishtml(message)

class URLsFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "URLs"

    def getFeature(self, message):
        return len(utils.geturls_payload(message))

class ExternalResourcesFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "External Resources"

    def getFeature(self, message):
        return len(utils.getexternalresources(message))

class JavascriptFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Javascript"

    def getFeature(self, message):
        return len(utils.getjavascriptusage(message))

class CssFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Css"

    def getFeature(self, message):
        return len(utils.getcssusage(message))

class IPsInURLs(FeatureFinder):
    def getFeatureTitle(self):
        return "IPs in URLs"

    def getFeature(self, message):
        return len(utils.getIPHrefs(message)) > 0

class AtInURLs(FeatureFinder):
    def getFeatureTitle(self):
        return "@ in URLs"

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
        return "Encoding"

    def getFeature(self, message):
        encoding = message.get('content-transfer-encoding')
        if encoding:
            # Normalize the encoding string by making it lowercase and stripping white spaces and line breaks
            encoding = encoding.strip().lower()
            encoding = encoding.replace('\r', '').replace('\n', '')

            # Normalize different variations of '7bit' and '8bit' to a standard form
            encoding = re.sub(r'7bit.*', '7bit', encoding)
            encoding = re.sub(r'8bit.*', '8bit', encoding)

            # Replace multiple spaces with a single space and strip leading/trailing whitespace
            encoding = re.sub(r'\s+', ' ', encoding).strip()

            return encoding
        return 'none'


class AlexaRankFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "Alexa Rank"

    def getFeature(self, message):
        urls = utils.geturls_payload(message)
        ranks = []
        for url in urls:
            rank = utils.get_alexa_rank(url)
            if rank != -1:
                ranks.append(rank)
        return ranks if ranks else ['No Rank']

def processFile(filepath, encoding, phishy=True, limit=500): # TODO this needs to be changed to work with the app, process one mbox string and return
    print(f"Processing file: {filepath}")                   # TODO      a list of dictionaries with the features and values? or save to file? and read later?
    try:
        mbox = mailbox.mbox(filepath)
    except Exception as e:
        print(f"Error opening file {filepath}: {e}")
        return

    data = []
    email_index = []
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
               IFrameFinder(), HTMLContentFinder(), URLsFinder(),
               ExternalResourcesFinder(), JavascriptFinder(),
               CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

    for i, message in enumerate(mbox, start=1):
        email_data = {}
        payload = utils.getpayload_dict(message)
        totalsize = sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload)

        if totalsize < 1:
            continue

        for finder in finders:
            email_data[finder.getFeatureTitle()] = finder.getFeature(message)

        email_data["Phishy"] = phishy
        data.append(email_data)

        try:
            email_raw = message.as_bytes().decode(encoding, errors='replace')
            email_fields = {"id": i, "message": utils.getpayload(message), "raw": email_raw}
            email_index.append(email_fields)
        except (UnicodeEncodeError, AttributeError):
            continue

        if limit and i >= limit:
            break

    df = pd.DataFrame(data)
    df.to_csv(filepath + "-export.csv", quoting=csv.QUOTE_ALL)

    emails_df = pd.DataFrame(email_index)
    emails_df.to_csv(filepath + "-export-index.csv", quoting=csv.QUOTE_ALL)

def process_mbox_message(mbox_string):
    # Parse the mbox string
    message = mailbox.mboxMessage(mbox_string)

    # Define your feature finders (assuming these are classes or functions you've defined)
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
               IFrameFinder(), HTMLContentFinder(), URLsFinder(),
               ExternalResourcesFinder(), JavascriptFinder(),
               CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

    email_data = {}
    payload = utils.getpayload_dict(message)
    totalsize = sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload)

    if totalsize < 1:
        return None

    for finder in finders:
        email_data[finder.getFeatureTitle()] = finder.getFeature(message)

    return email_data








def mboxtests():
    resources_dir = 'res'
    processFile(os.path.join(resources_dir, "emails-phishing.mbox"), "iso-8859-1", limit=2279)
    processFile(os.path.join(resources_dir, "emails-enron.mbox"), "ascii", limit=2257, phishy=False)

# mboxtests()