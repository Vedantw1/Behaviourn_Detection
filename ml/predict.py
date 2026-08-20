import joblib
import pandas as pd
import numpy as np

def predict_session(feature_dict):
    """
    Predict whether a session is Genuine (1) or Fraud (0) using the trained ML model.
    """
    model_path = "models/behavior_model.pkl"
    scaler_path = "models/scaler.pkl"

    model = joblib.load(model_path)
    scaler = joblib.load(scaler_path)

    feature_cols = [
        'AvgMouseSpeed', 'MouseAcceleration', 'ClickFrequency', 'ScrollSpeed',
        'AvgDwellTime', 'AvgFlightTime', 'TypingSpeed', 'BackspaceCount',
        'IdleTime', 'SessionDuration', 'WindowSwitchCount', 'ActiveTimeRatio'
    ]

    df_sample = pd.DataFrame([feature_dict])[feature_cols]
    scaled_sample = scaler.transform(df_sample)

    prediction = model.predict(scaled_sample)[0]
    probabilities = model.predict_proba(scaled_sample)[0]

    label = "Genuine" if prediction == 1 else "Fraud"
    confidence = probabilities[prediction] * 100

    print(f"🎯 Prediction Result: {label} ({confidence:.2f}% confidence)")
    return label, confidence

if __name__ == "__main__":
    sample_session = {
        'AvgMouseSpeed': 105.5,
        'MouseAcceleration': 35000.0,
        'ClickFrequency': 0.033,
        'ScrollSpeed': 0.0,
        'AvgDwellTime': 95.0,
        'AvgFlightTime': 230.0,
        'TypingSpeed': 2.5,
        'BackspaceCount': 3,
        'IdleTime': 10.0,
        'SessionDuration': 30.0,
        'WindowSwitchCount': 1,
        'ActiveTimeRatio': 0.66
    }
    
    predict_session(sample_session)
