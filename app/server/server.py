from flask import Flask, request, jsonify
from flask import send_from_directory  # Make sure to import this at the top
from flask_sqlalchemy import SQLAlchemy
from sqlalchemy import and_
import numpy as np
import json
import os
import gzip
import shutil
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(levelname)s - %(message)s')

# Setting the base directory relative to the current file's location
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
WEIGHTS_DIR = os.path.join(BASE_DIR, 'weights')

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///federated_learning.db'
app.config['SQLALCHEMY_TRACK_MODIFICATIONS'] = False
db = SQLAlchemy(app)

class Client(db.Model):
    id = db.Column(db.String(255), primary_key=True)
    last_update = db.Column(db.DateTime, nullable=True)

class Weight(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    client_id = db.Column(db.String(255), db.ForeignKey('client.id'), nullable=False)
    weights_file = db.Column(db.String(255), nullable=False)  # File path of weights

def initialize_database():
    with app.app_context():
        db.create_all()
    if not os.path.exists(WEIGHTS_DIR):
        os.makedirs(WEIGHTS_DIR)
    logging.info("Database and weights directory initialized.")

@app.route('/upload_weights', methods=['POST'])
def upload_weights():
    file = request.files.get('file')
    client_id = request.form.get('client_id')
    if file and client_id:
        filename = file.filename
        save_path = os.path.join(WEIGHTS_DIR, filename)
        file.save(save_path)
        logging.info(f"File {filename} saved successfully.")

        # Decompress the file
        decompressed_path = save_path.replace('.gz', '')
        with gzip.open(save_path, 'rb') as f_in, open(decompressed_path, 'wb') as f_out:
            shutil.copyfileobj(f_in, f_out)
        logging.info(f"File {filename} decompressed successfully.")

        store_weights(client_id, decompressed_path)
        return jsonify({"message": "Weights uploaded and decompressed successfully"}), 200
    else:
        logging.warning("Upload failed: No file provided.")
        return jsonify({"error": "No file provided"}), 400

def store_weights(client_id, weights_file_path):
    new_weight = Weight(client_id=client_id, weights_file=weights_file_path)
    db.session.add(new_weight)
    db.session.commit()
    logging.info(f"Weights stored for client {client_id} at {weights_file_path}")

@app.route('/get_weights', methods=['GET'])
def get_weights():
    all_weights = db.session.query(Weight).all()

    if not all_weights:
        logging.warning("No weights available to fetch.")
        return jsonify({"error": "No weights available"}), 404

    weights_by_layer = {}

    for weight in all_weights:
        try:
            with open(weight.weights_file, 'r') as f:
                weights_data = json.load(f)
            for layer_index, layer_weights in enumerate(weights_data):
                if layer_index not in weights_by_layer:
                    weights_by_layer[layer_index] = []
                weights_by_layer[layer_index].append(np.array(layer_weights, dtype=np.float32))
            logging.info(f"Loaded weights from {weight.weights_file}")
        except Exception as e:
            logging.error(f"Error loading weights from {weight.weights_file}: {str(e)}")
            continue

    if not weights_by_layer:
        logging.error("No valid weights matrices found after processing.")
        return jsonify({"error": "No valid weights data available"}), 404

    try:
        averaged_weights = [np.mean(np.stack(layer_weights), axis=0).tolist() for layer_index, layer_weights in weights_by_layer.items()]
        averaged_filename = 'averaged_weights.json.gz'
        averaged_filepath = os.path.join(WEIGHTS_DIR, averaged_filename)

        with gzip.open(averaged_filepath, 'wb') as f:
            f.write(json.dumps(averaged_weights).encode('utf-8'))
        logging.info(f"Averaged weights saved to {averaged_filepath}")

        return send_from_directory(directory=WEIGHTS_DIR, path=averaged_filename, as_attachment=True)
    except Exception as e:
        logging.error(f"Cannot average weights or send file: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/check', methods=['GET'])
def check_server():
    logging.info("Server status checked: Running.")
    return jsonify({"status": "Server is up and running!"}), 200

if __name__ == "__main__":
    initialize_database()
    app.run(debug=True, host='0.0.0.0', port=5000, ssl_context=('cert.pem', 'key.pem'))

