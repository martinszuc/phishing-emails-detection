import csv
import mailbox
import os
import pandas as pd
import re
import base64


import utils_finders as utils
from feature_finders import HTMLFormFinder, AttachmentFinder, FlashFinder, IFrameFinder, \
    HTMLContentFinder, URLsFinder, ExternalResourcesFinder, JavascriptFinder, CssFinder, IPsInURLs, \
    AtInURLs, EncodingFinder, SpamWordCountFinder, UrgencyPhraseFinder, MisspellingRatioFinder, \
    DKIMHeaderFinder, DMARCHeaderFinder, SPFHeaderFinder, ARCHeaderFinder, XHeaderSecurityFinder

finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
           IFrameFinder(), HTMLContentFinder(), URLsFinder(),
           ExternalResourcesFinder(), JavascriptFinder(),
           CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder(), SpamWordCountFinder(),
           UrgencyPhraseFinder(), MisspellingRatioFinder(), DKIMHeaderFinder(), DMARCHeaderFinder(),
           SPFHeaderFinder(), ARCHeaderFinder(), XHeaderSecurityFinder()]


def process_mbox_to_csv(filepath, encoding, output_dir, is_phishy=True, limit=500):
    print(f"Processing file: {filepath}")
    print(f"Encoding: {encoding}")
    print(f"Output directory: {output_dir}")
    print(f"Is Phishy: {is_phishy}")
    print(f"Limit: {limit}")

    common_encodings = ['utf-8', 'iso-8859-1', 'utf-16', 'ascii']

    data, email_index = [], []
    try:
        mbox = mailbox.mbox(filepath)
        try:
            for i, message in enumerate(mbox, start=1):
                if i > limit:
                    print(f"Reached processing limit of {limit} emails.")
                    break
                payload = utils.getpayload_dict(message)
                if sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload) < 1:
                    continue

                email_data = {finder.getFeatureTitle(): finder.getFeature(message) for finder in
                              finders}
                email_data["is_phishy"] = is_phishy
                data.append(email_data)
                # email_decoded = False
                # for encoding in common_encodings:
                #     try:
                #         email_raw = message.as_bytes().decode(encoding, errors='replace')
                #         email_index.append({"id": i, "message": utils.getpayload(message), "raw": email_raw})
                #         email_decoded = True
                #         break  # Break if successfully decoded
                #     except (UnicodeDecodeError, AttributeError, base64.binascii.Error) as e:
                #         print(f"Trying next encoding due to error with {encoding}: {e}")
                #
                # if not email_decoded:
                #     print(f"Failed to decode email ID {i} with common encodings. Skipping email.")
                #     continue

            # Construct the output file paths
            base_filename = os.path.splitext(os.path.basename(filepath))[0]
            data_csv_path = os.path.join(output_dir, f"{base_filename}-export.csv")
            index_csv_path = os.path.join(output_dir, f"{base_filename}-export-index.csv")

                 # Ensure the output directory exists
            if not os.path.exists(output_dir):
                os.makedirs(output_dir)

                    # Saving the processed data to CSV files
            pd.DataFrame(data).to_csv(data_csv_path, index=True, quoting=csv.QUOTE_ALL)
            # pd.DataFrame(email_index).to_csv(index_csv_path, index=False, quoting=csv.QUOTE_ALL) #
            print(f"Data exported to {data_csv_path}")
            # print(f"Email index exported to {index_csv_path}")

            saved_csv_filename = f"{base_filename}-export.csv"  # Assign the filename for return
        finally:
            mbox.close()  # Ensure the mbox file is closed properly
    except Exception as e:
        print(f"Error opening file {filepath}: {e}")

    return saved_csv_filename


def process_mbox_to_data(filepath, encoding='utf-8', is_phishy=None, limit=500):
    print(f"Processing file: {filepath}")
    data = []
    try:
        mbox = mailbox.mbox(filepath)
    except Exception as e:
        print(f"Error opening file {filepath}: {e}")
        return pd.DataFrame()

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
