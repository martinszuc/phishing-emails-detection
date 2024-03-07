import mailbox
import pandas as pd
import os
import re
import csv
from feature_finders import HTMLFormFinder, AttachmentFinder, FlashFinder, IFrameFinder, HTMLContentFinder, URLsFinder, ExternalResourcesFinder, JavascriptFinder, CssFinder, IPsInURLs, AtInURLs, EncodingFinder
import utils_finders as utils

def process_mbox_to_csv(filepath, encoding, is_phishy=True, limit=500):
    print(f"Processing file: {filepath}")
    print(f"Encoding: {encoding}, Is Phishy: {is_phishy}, Limit: {limit}")  # Debug statement

    data, email_index = [], []
    try:
        mbox = mailbox.mbox(filepath)
    except Exception as e:
        print(f"Error opening file {filepath}: {e}")
        return
    
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
               IFrameFinder(), HTMLContentFinder(), URLsFinder(),
               ExternalResourcesFinder(), JavascriptFinder(),
               CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

    for i, message in enumerate(mbox, start=1):
        if i > limit:
            print(f"Reached processing limit of {limit} emails.")  # Inform when the limit is reached
            break
        payload = utils.getpayload_dict(message)
        if sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload) < 1:
            continue  # Skip empty or nearly empty emails
        
        email_data = {finder.getFeatureTitle(): finder.getFeature(message) for finder in finders}
        email_data["is_phishy"] = is_phishy
        data.append(email_data)

        try:
            email_raw = message.as_bytes().decode(encoding, errors='replace')
            email_index.append({"id": i, "message": utils.getpayload(message), "raw": email_raw})
        except (UnicodeEncodeError, AttributeError):
            print(f"Error decoding email ID {i}. Skipping.")  # Error handling for decoding issues
            continue

    # Saving the processed data to CSV files
    data_csv_path = filepath + "-export.csv"
    index_csv_path = filepath + "-export-index.csv"
    pd.DataFrame(data).to_csv(data_csv_path, index=True, quoting=csv.QUOTE_ALL)
    pd.DataFrame(email_index).to_csv(index_csv_path, index=False, quoting=csv.QUOTE_ALL)
    print(f"Data exported to {data_csv_path}")
    print(f"Email index exported to {index_csv_path}")

def process_mbox_to_data(filepath, encoding='utf-8', is_phishy=None, limit=500):
    print(f"Processing file: {filepath}")
    data = []
    try:
        mbox = mailbox.mbox(filepath)
    except Exception as e:
        print(f"Error opening file {filepath}: {e}")
        return pd.DataFrame()

    # Assuming finders are defined and implemented elsewhere
    finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(), IFrameFinder(), HTMLContentFinder(), 
               URLsFinder(), ExternalResourcesFinder(), JavascriptFinder(), CssFinder(), IPsInURLs(), 
               AtInURLs(), EncodingFinder()]

    for i, message in enumerate(mbox):
        if i >= limit:
            break
        payload = utils.getpayload_dict(message)
        if sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload) < 1:
            continue
        
        email_data = {finder.getFeatureTitle(): finder.getFeature(message) for finder in finders}
        if is_phishy is not None:
            email_data["is_phishy"] = int(is_phishy)
        data.append(email_data)

    return pd.DataFrame(data)

