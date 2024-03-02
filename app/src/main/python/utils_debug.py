def print_sample_data_point(features, label):
    # Convert features and label to numpy for easier reading
    features_np = {key: value.numpy() for key, value in features.items()}
    label_np = label.numpy()

    # Printing in a structured format
    print("Sample Data Point\n-----------------")
    for key, value in features_np.items():
        print(f"{key}:\n{value}\n")
    
    print("Label:\n", label_np)
