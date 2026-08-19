package com.behavior.collector.feature;

import com.behavior.collector.model.BehavioralSession;
import com.behavior.collector.model.FeatureVector;

/**
 * Interface defining the behavioral feature extraction contract.
 */
public interface FeatureEngine {

    /**
     * Extracts the 12 engineered features from a completed behavioral session.
     *
     * @param session Completed behavioral recording session containing raw events
     * @return Immutable FeatureVector object
     */
    FeatureVector extractFeatures(BehavioralSession session);
}
