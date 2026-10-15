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

    # Base directories
    script_dir = os.path.dirname(os.path.abspath(__file__))
    models_dir = os.path.join(script_dir, "models")
    reports_dir = os.path.join(script_dir, "reports")
    os.makedirs(models_dir, exist_ok=True)
    os.makedirs(reports_dir, exist_ok=True)

    # 4. Generate Visual Analytics for Final-Year Project Report
    try:
        import matplotlib.pyplot as plt
        import seaborn as sns
        from sklearn.metrics import roc_curve, auc

        # A. Confusion Matrix Plot
        plt.figure(figsize=(6, 5))
        cm = confusion_matrix(y_test, rf_preds)
        sns.heatmap(cm, annot=True, fmt='d', cmap='Blues',
                    xticklabels=['Fraud (0)', 'Genuine (1)'],
                    yticklabels=['Fraud (0)', 'Genuine (1)'])
        plt.title('Confusion Matrix - Random Forest Biometric Classifier')
        plt.xlabel('Predicted Label')
        plt.ylabel('Actual Label')
        plt.tight_layout()
        cm_path = os.path.join(reports_dir, 'confusion_matrix.png')
        plt.savefig(cm_path, dpi=300)
        plt.close()
        print(f"📊 Confusion Matrix saved to: {cm_path}")

        # B. ROC Curve Plot
        rf_probs = rf_model.predict_proba(X_test_scaled)[:, 1]
        fpr, tpr, _ = roc_curve(y_test, rf_probs)
        roc_auc = auc(fpr, tpr)

        plt.figure(figsize=(7, 5))
        plt.plot(fpr, tpr, color='#2563eb', lw=2.5, label=f'Random Forest (AUC = {roc_auc:.3f})')
        plt.plot([0, 1], [0, 1], color='#9ca3af', lw=1.5, linestyle='--')
        plt.xlim([0.0, 1.0])
        plt.ylim([0.0, 1.05])
        plt.xlabel('False Positive Rate (1 - Specificity)')
        plt.ylabel('True Positive Rate (Sensitivity)')
        plt.title('Receiver Operating Characteristic (ROC) Curve')
        plt.legend(loc="lower right")
        plt.grid(True, alpha=0.3)
        plt.tight_layout()
        roc_path = os.path.join(reports_dir, 'roc_curve.png')
        plt.savefig(roc_path, dpi=300)
        plt.close()
        print(f"📈 ROC Curve saved to: {roc_path}")

        # C. Feature Importance Bar Chart
        plt.figure(figsize=(10, 6))
        sns.barplot(x='Importance', y='Feature', data=feature_ranking, palette='viridis')
        plt.title('Biometric Feature Importance Ranking')
        plt.xlabel('Relative Gini Importance')
        plt.ylabel('Biometric Feature')
        plt.tight_layout()
        fi_path = os.path.join(reports_dir, 'feature_importance.png')
        plt.savefig(fi_path, dpi=300)
        plt.close()
        print(f"📉 Feature Importance chart saved to: {fi_path}")

    except Exception as e:
        print(f"Warning generating evaluation plots: {e}")

    # Save best model and scaler
    model_file = os.path.join(models_dir, "behavior_model.pkl")
    scaler_file = os.path.join(models_dir, "scaler.pkl")
    joblib.dump(rf_model, model_file)
    joblib.dump(scaler, scaler_file)
    print(f"\n💾 Model successfully saved to {model_file}")
    print(f"💾 Scaler successfully saved to {scaler_file}")

if __name__ == "__main__":
    df = load_data()
    X, y, feature_cols = preprocess_data(df)
    train_and_evaluate(X, y, feature_cols)
