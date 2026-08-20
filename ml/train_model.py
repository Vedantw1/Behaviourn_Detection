import os
import pandas as pd
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import classification_report, confusion_matrix, accuracy_score, roc_auc_score
from xgboost import XGBClassifier
import joblib

def load_data():
    """Load dataset from local CSV or fetch from Supabase if CSV is missing."""
    csv_path = "../behavioral_data.csv"
    if not os.path.exists(csv_path):
        csv_path = "behavioral_data.csv"

    if os.path.exists(csv_path):
        print(f"📂 Loading dataset from local CSV: {csv_path}")
        df = pd.read_csv(csv_path)
    else:
        raise FileNotFoundError("Could not find behavioral_data.csv. Please collect data or export CSV from Supabase.")
    
    return df

def preprocess_data(df):
    """Clean and preprocess feature vectors and target labels."""
    print(f"📊 Dataset Shape: {df.shape[0]} rows, {df.shape[1]} columns")

    # Define the 12 engineered features
    feature_cols = [
        'AvgMouseSpeed', 'MouseAcceleration', 'ClickFrequency', 'ScrollSpeed',
        'AvgDwellTime', 'AvgFlightTime', 'TypingSpeed', 'BackspaceCount',
        'IdleTime', 'SessionDuration', 'WindowSwitchCount', 'ActiveTimeRatio'
    ]

    # Clean and encode label (Genuine -> 1, Fraud -> 0)
    df['LabelEncoded'] = df['Label'].apply(lambda x: 1 if str(x).strip().lower() == 'genuine' else 0)

    X = df[feature_cols]
    y = df['LabelEncoded']

    print(f"📈 Label Distribution:\n{df['Label'].value_counts()}\n")

    return X, y, feature_cols

def train_and_evaluate(X, y, feature_cols):
    """Train Random Forest and XGBoost classifiers and evaluate performance."""
    # Split data (80% train, 20% test)
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42, stratify=y)

    # Scale features
    scaler = StandardScaler()
    X_train_scaled = scaler.fit_transform(X_train)
    X_test_scaled = scaler.transform(X_test)

    # 1. Random Forest Classifier
    print("🌲 Training Random Forest Classifier...")
    rf_model = RandomForestClassifier(n_estimators=100, max_depth=10, random_state=42)
    rf_model.fit(X_train_scaled, y_train)

    rf_preds = rf_model.predict(X_test_scaled)
    rf_acc = accuracy_score(y_test, rf_preds)
    print(f"✅ Random Forest Accuracy: {rf_acc * 100:.2f}%")
    print("Classification Report:\n", classification_report(y_test, rf_preds))

    # 2. XGBoost Classifier
    print("⚡ Training XGBoost Classifier...")
    xgb_model = XGBClassifier(n_estimators=100, max_depth=6, learning_rate=0.1, random_state=42)
    xgb_model.fit(X_train_scaled, y_train)

    xgb_preds = xgb_model.predict(X_test_scaled)
    xgb_acc = accuracy_score(y_test, xgb_preds)
    print(f"✅ XGBoost Accuracy: {xgb_acc * 100:.2f}%")
    print("Classification Report:\n", classification_report(y_test, xgb_preds))

    # 3. Feature Importance Analysis
    print("\n🔍 Feature Importance Ranking (Random Forest):")
    importances = rf_model.feature_importances_
    feature_ranking = pd.DataFrame({
        'Feature': feature_cols,
        'Importance': importances
    }).sort_values(by='Importance', ascending=False)
    
    print(feature_ranking.to_string(index=False))

    # Save best model and scaler
    os.makedirs("models", exist_ok=True)
    joblib.dump(rf_model, "models/behavior_model.pkl")
    joblib.dump(scaler, "models/scaler.pkl")
    print("\n💾 Model saved to ml/models/behavior_model.pkl")
    print("💾 Scaler saved to ml/models/scaler.pkl")

if __name__ == "__main__":
    df = load_data()
    X, y, feature_cols = preprocess_data(df)
    train_and_evaluate(X, y, feature_cols)
