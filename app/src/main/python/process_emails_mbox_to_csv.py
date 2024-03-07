# process_emails_mbox_to_csv.py file
import os
import utils_feature_extraction as ufe

# Usage
# Replace the resource dir, mbox file and is_phishy

def main():
    # Define the resources directory where your .mbox files are located
    resources_dir = 'res_retrain/' # Change here

    # Process emails-phishing-pot.mbox
    phishing_pot_path = os.path.join(resources_dir, "emails-samples-phishing.mbox")   # Change here, add phishing mbox file
    print(f"Processing {phishing_pot_path} with encoding iso-8859-1")
    ufe.process_mbox_to_csv(phishing_pot_path, "iso-8859-1", limit=2279, is_phishy=True)

    # Process emails-enron.mbox
    enron_path = os.path.join(resources_dir, "emails-samples-safe.mbox")   # Change here, add safe mbox file
    print(f"Processing {enron_path}")
    ufe.process_mbox_to_csv(enron_path, "ascii", limit=2257, is_phishy=False)

    print("Finished processing all files.")

if __name__ == "__main__":
    main()
