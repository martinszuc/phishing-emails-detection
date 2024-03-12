import mailbox
import pandas as pd
import os
import re
import csv
from feature_finders import HTMLFormFinder, AttachmentFinder, FlashFinder, IFrameFinder, HTMLContentFinder, URLsFinder, ExternalResourcesFinder, JavascriptFinder, CssFinder, IPsInURLs, AtInURLs, EncodingFinder
import utils_finders as utils

def process_mbox_to_csv(filepath, encoding, output_dir, is_phishy=True, limit=500):
    print(f"Processing file: {filepath}")
    print(f"Encoding: {encoding}, Output directory: {output_dir}, Is Phishy: {is_phishy}, Limit: {limit}")

    data, email_index = [], []
    try:
        mbox = mailbox.mbox(filepath)
        try:
            finders = [HTMLFormFinder(), AttachmentFinder(), FlashFinder(),
                       IFrameFinder(), HTMLContentFinder(), URLsFinder(),
                       ExternalResourcesFinder(), JavascriptFinder(),
                       CssFinder(), IPsInURLs(), AtInURLs(), EncodingFinder()]

            for i, message in enumerate(mbox, start=1):
                if i > limit:
                    print(f"Reached processing limit of {limit} emails.")
                    break
                payload = utils.getpayload_dict(message)
                if sum(len(re.sub(r'\s+', '', part["payload"])) for part in payload) < 1:
                    continue

                email_data = {finder.getFeatureTitle(): finder.getFeature(message) for finder in finders}
                email_data["is_phishy"] = is_phishy
                data.append(email_data)

                try:
                    email_raw = message.as_bytes().decode(encoding, errors='replace')
                    email_index.append({"id": i, "message": utils.getpayload(message), "raw": email_raw})
                except (UnicodeEncodeError, AttributeError):
                    print(f"Error decoding email ID {i}. Skipping.")
                    continue

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
            #print(f"Email index exported to {index_csv_path}")

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

