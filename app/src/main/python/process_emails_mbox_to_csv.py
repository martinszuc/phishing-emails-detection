# process_emails_mbox_to_csv.py
import sys
import os
import utils_feature_extraction as ufe

def process_mbox_to_csv(resources_dir, mbox_filename, output_dir, encoding, limit, is_phishy):
    mbox_path = os.path.join(os.environ["HOME"], resources_dir, mbox_filename)
    output_path = os.path.join(os.environ["HOME"], output_dir)
    print(f"Processing {mbox_path} with encoding {encoding}")
    ufe.process_mbox_to_csv(mbox_path, encoding, output_path, limit=limit, is_phishy=is_phishy)

if __name__ == "__main__":
    if len(sys.argv) == 6:
        _, resources_dir, mbox_filename, output_dir, encoding, limit, is_phishy = sys.argv
        process_mbox_to_csv(resources_dir, mbox_filename, encoding, int(limit), is_phishy.lower() == 'true')
    else:
        print("Invalid number of arguments.")
