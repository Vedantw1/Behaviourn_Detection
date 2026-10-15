import os
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field
from ml.predict import predict_session

app = FastAPI(
    title="Behavioral Biometrics Continuous Authentication API",
    description="Real-Time Machine Learning Fraud Detection & Biometric Trust Scoring Service",
    version="1.0.0"
)

class FeatureVectorRequest(BaseModel):
    AvgMouseSpeed: float = Field(..., description="Average mouse velocity in px/sec", example=230.5)
    MouseAcceleration: float = Field(..., description="Mouse acceleration in px/sec²", example=75000.0)
    ClickFrequency: float = Field(..., description="Clicks per second", example=0.45)
    ScrollSpeed: float = Field(..., description="Scroll events per second", example=0.8)
    AvgDwellTime: float = Field(..., description="Average keystroke hold duration in milliseconds", example=102.0)
    AvgFlightTime: float = Field(..., description="Average key release-to-press interval in milliseconds", example=235.0)
    TypingSpeed: float = Field(..., description="Keystrokes per second", example=3.8)
    BackspaceCount: int = Field(..., description="Number of backspaces/deletes", example=3)
    IdleTime: float = Field(..., description="Inactive duration in seconds", example=5.5)
    SessionDuration: float = Field(..., description="Total duration of session in seconds", example=30.0)
    WindowSwitchCount: int = Field(..., description="Window focus switch count", example=0)
    ActiveTimeRatio: float = Field(..., description="Ratio of active work [0.0 - 1.0]", example=0.8167)

class PredictionResponse(BaseModel):
    label: str = Field(..., description="Classification outcome: 'Genuine' or 'Fraud'")
    is_genuine: bool = Field(..., description="True if classified as genuine human user")
    confidence: float = Field(..., description="Model confidence percentage [0.0 - 100.0]")
    trust_score: float = Field(..., description="Biometric continuous trust score [0.0 - 100.0]")
    fraud_score: float = Field(..., description="Anomaly / fraud threat score [0.0 - 100.0]")

@app.get("/")
def root():
    return {
        "service": "Behavioral Biometrics Continuous Authentication API",
        "status": "online",
        "docs_url": "/docs"
    }

@app.get("/health")
def health():
    script_dir = os.path.dirname(os.path.abspath(__file__))
    model_exists = os.path.exists(os.path.join(script_dir, "models", "behavior_model.pkl"))
    return {
        "status": "healthy" if model_exists else "model_missing",
        "model_loaded": model_exists
    }

@app.post("/predict", response_model=PredictionResponse)
def predict(request: FeatureVectorRequest):
    try:
        feature_dict = request.dict()
        result = predict_session(feature_dict)
        return result
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Inference error: {str(e)}")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run("ml.api:app", host="0.0.0.0", port=8000, reload=True)
