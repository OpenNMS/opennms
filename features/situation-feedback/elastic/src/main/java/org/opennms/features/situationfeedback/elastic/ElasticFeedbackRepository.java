package org.opennms.features.situationfeedback.elastic;

import java.util.Collection;

import org.opennms.features.situationfeedback.api.AlarmFeedback;
import org.opennms.features.situationfeedback.api.FeedbackException;
import org.opennms.features.situationfeedback.api.FeedbackRepository;

public class ElasticFeedbackRepository implements FeedbackRepository {

    @Override
    public void persist(Collection<AlarmFeedback> feedback) throws FeedbackException {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey, String situationFingerprint) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<AlarmFeedback> getFeedback(String situationKey, Collection<String> alarmKeys) {
        // TODO Auto-generated method stub
        return null;
    }

}
