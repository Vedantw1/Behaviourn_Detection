import os
import sys
import json
import joblib
import pandas as pd
import numpy as np

def predict_session(feature_dict):
    """
    Predict whether a session is Genuine (1) or Fraud (0) using the trained ML model.
    """
    script_dir = os.path.dirname(os.path.abspath(__file__))
    model_path = os.path.join(script_dir, "models", "behavior_model.pkl")
    scaler_path = os.path.join(script_dir, "models", "scaler.pkl")

    if not os.path.exists(model_path):
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

    prediction = int(model.predict(scaled_sample)[0])
    probabilities = model.predict_proba(scaled_sample)[0]

    genuine_prob = float(probabilities[1]) * 100
    fraud_prob = float(probabilities[0]) * 100
    label = "Genuine" if prediction == 1 else "Fraud"
    confidence = float(probabilities[prediction]) * 100
    trust_score = round(genuine_prob, 2)

    return {
        "label": label,
        "is_genuine": bool(prediction == 1),
        "confidence": round(confidence, 2),
        "trust_score": trust_score,
        "fraud_score": round(fraud_prob, 2)
    }

if __name__ == "__main__":
    import argparse
    parser = argparse.ArgumentParser()
    parser.add_argument("--json", type=str, help="JSON string of feature dict")
    args = parser.parse_args()

    if args.json:
        try:
            data = json.loads(args.json)
            result = predict_session(data)
            print(json.dumps(result))
        except Exception as e:
            print(json.dumps({"error": str(e)}), file=sys.stderr)
            sys.exit(1)
    else:
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
        res = predict_session(sample_session)
        print(f"🎯 Prediction Result: {res['label']} ({res['confidence']:.2f}% confidence, Trust Score: {res['trust_score']}%)")
