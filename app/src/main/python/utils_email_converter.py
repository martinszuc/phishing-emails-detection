import os
import shutil
import email
import mailbox

def clear_directory(directory):
    """Removes all files and folders in the specified directory."""
    for item in os.listdir(directory):
        item_path = os.path.join(directory, item)
        if os.path.isfile(item_path) or os.path.islink(item_path):
            os.unlink(item_path)
        elif os.path.isdir(item_path):
            shutil.rmtree(item_path)
    print(f"Cleaned up {directory}.")

def eml_to_mbox(input_directory, output_mbox_file):
    """Converts all .eml files in a directory to a single .mbox file."""
    mbox = mailbox.mbox(output_mbox_file, create=True)
    eml_files = [f for f in os.listdir(input_directory) if f.endswith(".eml")]
    total_files = len(eml_files)
    
    print(f"Found {total_files} .eml files in {input_directory} to process.")
    
    for i, filename in enumerate(eml_files, start=1):
        file_path = os.path.join(input_directory, filename)
        print(f"Processing file {i} of {total_files}: {filename}")
        
        with open(file_path, 'rb') as file:
            eml_content = file.read()
        
        msg = email.message_from_bytes(eml_content)
        mbox.add(msg)
    
    mbox.close()
    print(f"All .eml files from {input_directory} have been added to {output_mbox_file}.")
    print("Conversion completed successfully.")
