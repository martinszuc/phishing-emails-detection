import utils_email_converter as uec
import os

def main():
    input_directory = 'samples/safe'
    output_mbox_file = 'res_retrain/emails-samples-safe.mbox'

    # Remove the old output mbox file if it exists
    if os.path.exists(output_mbox_file):
        os.remove(output_mbox_file)

    # Make the output directory first
    output_directory = os.path.dirname(output_mbox_file)
    if not os.path.exists(output_directory):
        os.makedirs(output_directory)

    uec.eml_to_mbox(input_directory, output_mbox_file)

if __name__ == '__main__':
    main()