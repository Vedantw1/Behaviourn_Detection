package com.behavior.collector.feature;

import com.behavior.collector.model.BiometricPrediction;
import com.behavior.collector.model.FeatureVector;
import com.behavior.collector.model.SessionLabel;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ModelInferenceServiceTest {

    @Test
    public void testInferenceReturnsPrediction() {
        ModelInferenceService service = new ModelInferenceService();

        // Sample genuine human feature vector
        FeatureVector genuineVector = new FeatureVector(
                "test_user",
                SessionLabel.GENUINE,
                240.5,
                65000.0,
                0.5,
                0.5,
                105.0,
                240.0,
                3.8,
                2,
                4.5,
                30.0,
                0,
                0.85
        );

        BiometricPrediction prediction = service.predict(genuineVector);
        assertNotNull(prediction, "Inference service should return a valid prediction");
        assertNotNull(prediction.label(), "Prediction should have a label");
        assertTrue(prediction.confidence() >= 0.0 && prediction.confidence() <= 100.0, "Confidence should be 0-100%");
        assertTrue(prediction.trustScore() >= 0.0 && prediction.trustScore() <= 100.0, "Trust score should be 0-100%");
    }
}
