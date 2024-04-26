# feature_finders.py
#  * Authored by matoszuc@gmail.com
import re
from abc import ABC, abstractmethod

import utils_finders as utils
from utils_config import Config


class FeatureFinder(ABC):

    @abstractmethod
    def getFeatureTitle(self):
        pass

    @abstractmethod
    def getFeature(self, message):
        pass

class XHeaderSecurityFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "xheader_security"

    def getFeature(self, message):
        headers = utils.get_email_headers(message)
        security_headers = ["X-Security", "X-Scanned", "X-Spam-Flag"]  # Example headers
        return any(header in headers for header in security_headers)


class ARCHeaderFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "arc_pass"

    def getFeature(self, message):
        headers = utils.get_email_headers(message)
        return "arc=pass" in headers.get("Authentication-Results", "").lower()

class DMARCHeaderFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "dmarc_pass"

    def getFeature(self, message):
        headers = utils.get_email_headers(message)
        return "dmarc=pass" in headers.get("Authentication-Results", "").lower()


class SPFHeaderFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "spf_pass"

    def getFeature(self, message):
        headers = utils.get_email_headers(message)
        return "spf=pass" in headers.get("Authentication-Results", "").lower()

class DKIMHeaderFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "dkim_pass"

    def getFeature(self, message):
        headers = utils.get_email_headers(message)
        return "dkim=pass" in headers.get("Authentication-Results", "").lower()

class MisspellingRatioFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "misspelling_ratio"

    def getFeature(self, message):
        payload = utils.getpayload(message)
        misspelling_ratio = utils.get_misspelling_ratio(payload)
        return misspelling_ratio

class UrgencyPhraseFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "urgency_phrase_count"

    def getFeature(self, message):
        payload = utils.getpayload(message)
        urgency_count = utils.get_urgency_phrase_count(payload)
        return urgency_count

class SpamWordCountFinder(FeatureFinder):
    def getFeatureTitle(self):
        return "spam_word_count"

    def getFeature(self, message):
        payload = utils.getpayload(message).lower()
        return utils.get_spam_word_count(payload)

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
        swflinks = re.compile(Config.FLASH_LINKED_CONTENT, re.IGNORECASE).findall(payload)
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
        emailPattern = re.compile(Config.EMAILREGEX, re.IGNORECASE)
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
        common_encodings = ['7bit', '8bit', 'none', 'quoted_printable', 'base64', 'binary'] # + 'other'
        
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