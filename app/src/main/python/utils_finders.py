import re
import logging
from bs4 import BeautifulSoup
import urllib.request as urllib
from functools import lru_cache
from utils_config import Config

def getpayload(msg):
    return __getpayload_rec__(msg, payloadresult="")

def __getpayload_rec__(msg, payloadresult):
    payload = msg.get_payload()

    if str(msg.get('content-transfer-encodin2279g')).lower() == "base64":
        payload = msg.get_payload(decode=True)

    if payload and msg.is_multipart():
        for subMsg in payload:
            payloadresult += __getpayload_rec__(subMsg, payloadresult)
    else:
        return msg.get_content_type() + "\t" + str(payload) + "\n"  # Added str() for payload
    return payloadresult

def getpayload_dict(msg):
    return __getpayload_dict_rec__(msg, [])

def __getpayload_dict_rec__(msg, payloadresult):
    if msg.is_multipart():
        for subMsg in msg.get_payload():
            __getpayload_dict_rec__(subMsg, payloadresult)
    else:
        content_type = msg.get_content_type()
        charset = msg.get_content_charset() or 'utf-8'  # Default to UTF-8 if charset is not specified

        # Clean up charset string
        if charset:
            charset = charset.replace('charset=', '').replace('"', '').strip()

        payload = msg.get_payload(decode=True)

        try:
            # Try decoding with the specified or default charset
            decoded_payload = payload.decode(charset, errors='replace')
        except (UnicodeDecodeError, LookupError, AttributeError):
            # Fallback to UTF-8 with replacement or raw bytes
            try:
                decoded_payload = payload.decode('utf-8', errors='replace')
            except UnicodeDecodeError:
                decoded_payload = payload

        payloadresult.append({"mimeType": content_type, "payload": decoded_payload})

    return payloadresult


def getAttachmentCount(msg):
    return __getAttachmentCountrec__(msg, count=0)

def __getAttachmentCountrec__(msg, count):
    payload = msg.get_payload()
    if msg.is_multipart():
        for subMsg in payload:
            count += __getAttachmentCountrec__(subMsg, count)
    else:
        if __hasAttachment__(msg):
            return 1
    return count

def __hasAttachment__(message):
    contentDisp = message.get("Content-Disposition")
    if contentDisp:
        # Ensure contentDisp is a string before calling .lower()
        contentDisp_str = str(contentDisp)
        return "attachment" in contentDisp_str.lower()
    return False

def getContentTypes(msg):
    return __getContentTypes_rec__(msg, [])

def __getContentTypes_rec__(msg, contenttypes):
    payload = msg.get_payload()
    if msg.is_multipart():
        for subMsg in payload:
            __getContentTypes_rec__(subMsg, contenttypes)
    else:
        contenttypes.append(msg.get_content_type())
    return contenttypes

def geturls_payload(message):
    return geturls_string(getpayload(message))

def getIPHrefs(message):
    urls = geturls_payload(message)
    iphref = re.compile(Config.IPREGEX, re.IGNORECASE)
    result = []
    for url in urls:
        if iphref.search(url) and iphref.search(url).group(1) is not None:
            result.append(iphref.search(url).group(1))
    return result

def getexternalresources(message):
    result = []

    for script in getjavascriptusage(message):
        if "src" in str(script) and "src" in script.attrs and isurl(script["src"]):
            result.append(script["src"])
    for css in getcssusage(message):
        if "href" in str(css) and "href" in css.attrs and isurl(css["href"]):
            result.append(css["href"])

    return result

def getjavascriptusage(message):
    result = []
    payload = getpayload_dict(message)
    for part in payload:
        if part["mimeType"].lower() == "text/html":
            htmlcontent = part["payload"]
            soup = BeautifulSoup(htmlcontent, 'html.parser')  # Updated parser
            scripts = soup.find_all("script")
            for script in scripts:
                result.append(script)
    return result

def getcssusage(message):
    result = []
    payload = getpayload_dict(message)
    for part in payload:
        if part["mimeType"].lower() == "text/html":
            htmlcontent = part["payload"]
            soup = BeautifulSoup(htmlcontent, 'html.parser')  # Updated parser
            csslinks = soup.find_all("link")
            for css in csslinks:
                result.append(css)
    return result

def geturls_string(string):
    result = []

    cleanPayload = re.sub(r'\s+', ' ', string)
    linkregex = re.compile(Config.HREFREGEX, re.IGNORECASE)
    links = linkregex.findall(cleanPayload)

    for link in links:
        if isurl(link):
            result.append(link)

    urlregex = re.compile(Config.URLREGEX_NOT_ALONE, re.IGNORECASE)
    links = urlregex.findall(cleanPayload)
    for link in links:
        if link not in result:
            result.append(link)
    return result

def isurl(link):
    return re.compile(Config.URLREGEX, re.IGNORECASE).search(link) is not None

def returnallmatches(string, regex):
    matches = re.finditer(regex, string, re.MULTILINE)
    result = []
    for matchNum, match in enumerate(matches):
        result.append(match.group())
    return result

def extract_registered_domain(url):
    return tldextract.extract(url).registered_domain

def get_whois_data(url): # doesnt work TBD
    domain = extract_registered_domain(url)
    try:
        w = whois.whois(domain)
        return w.text
    except Exception as e:
        return f"Error: {e}"

def ishtml(message):
    content_types = getContentTypes(message)
    if "text/html" not in content_types:
        return False

    payload = getpayload_dict(message)
    for part in payload:
        # We need to ensure that 'part["payload"]' is a string and contains HTML data
        if isinstance(part["payload"], str) and "<html" in part["payload"].lower():
            try:
                # Attempt to parse with BeautifulSoup to confirm it's HTML
                soup = BeautifulSoup(part["payload"], 'html.parser')
                if soup.find():  # Successfully found HTML tags
                    return True
            except Exception as e:
                # Log an error if BeautifulSoup fails to parse the content
                print(f"Error parsing HTML content: {e}")
    return False

def get_combined_text(msg):
    # Extract the subject
    subject = msg['subject'] or ""

    # Extract the body using existing utility methods
    payload_dict = getpayload_dict(msg)
    body_text = ""
    for part in payload_dict:
        if part['mimeType'] in ['text/plain', 'text/html']:
            body_text += part['payload']  # Concatenates all text/plain and text/html parts

    # Preprocess the text here if necessary (remove HTML, etc.)
    soup = BeautifulSoup(body_text, 'html.parser')
    body_text = soup.get_text(separator=' ', strip=True)

    # Combine subject and body
    combined_text = subject + " " + body_text
    return combined_text
