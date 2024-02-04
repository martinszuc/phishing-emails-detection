import mailbox
import pandas as pd
import re
import csv
import os
import utils
import config
import joblib
import numpy
import tensorflow as tf
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
            if url.lower().startswith("mailto:") or (
                    emailPattern.search(url) and emailPattern.search(url).group()):
                continue
            atvalue = url.find("@")
            athexvalue = url.find("%40")
            atvalue = min(atvalue, athexvalue) if atvalue != -1 and athexvalue != -1 else max(
                atvalue, athexvalue)
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


def process_single_email(mbox_string):
    # Initialize finders...
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
               IFrameFinder(), HTMLContentFinder(), URLsFinder(),
               ExternalResourcesFinder(), JavascriptFinder(),
               CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

    email_data = {}

    # Convert mbox_string to a mailbox message...
    message = mailbox.mboxMessage(mbox_string)

    # Feature extraction...
    payload = utils.getpayload_dict(message)
    totalsize = sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload)

    if totalsize < 1:
        return None

    for finder in finders:
        email_data[finder.getFeatureTitle()] = finder.getFeature(message)

    # Preprocess features: rename columns, convert booleans
    email_data = {col.replace(' ', '_').replace('@', 'at'): email_data[col] for col in email_data}

    return email_data