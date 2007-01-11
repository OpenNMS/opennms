package org.opennms.web.svclayer.support;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.context.MessageSourceResolvable;
import org.springframework.validation.Errors;

public class LocationMonitorDetailsModel {
    private Errors m_errors;
    private MessageSourceResolvable m_title;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_mainDetails;
    private MessageSourceResolvable m_additionalDetailsTitle;
    private Map<MessageSourceResolvable, MessageSourceResolvable> m_additionalDetails;
    
    public LocationMonitorDetailsModel() {
    }

    public Map<MessageSourceResolvable, MessageSourceResolvable> getAdditionalDetails() {
        return m_additionalDetails;
    }

    public void setAdditionalDetails(Map<MessageSourceResolvable, MessageSourceResolvable> additionalDetails) {
        m_additionalDetails = additionalDetails;
    }
    
    public void addAdditionalDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_additionalDetails == null) {
            m_additionalDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_additionalDetails.put(key, value);
    }

    public MessageSourceResolvable getAdditionalDetailsTitle() {
        return m_additionalDetailsTitle;
    }

    public void setAdditionalDetailsTitle(MessageSourceResolvable additionalDetailsTitle) {
        m_additionalDetailsTitle = additionalDetailsTitle;
    }

    public Map<MessageSourceResolvable, MessageSourceResolvable> getMainDetails() {
        return m_mainDetails;
    }

    public void setMainDetails(Map<MessageSourceResolvable, MessageSourceResolvable> mainDetails) {
        m_mainDetails = mainDetails;
    }
    
    public void addMainDetail(MessageSourceResolvable key, MessageSourceResolvable value) {
        if (m_mainDetails == null) {
            m_mainDetails = new LinkedHashMap<MessageSourceResolvable, MessageSourceResolvable>();
        }
        m_mainDetails.put(key, value);
    }

    public MessageSourceResolvable getTitle() {
        return m_title;
    }

    public void setTitle(MessageSourceResolvable title) {
        m_title = title;
    }

    public Errors getErrors() {
        return m_errors;
    }

    public void setErrors(Errors errors) {
        m_errors = errors;
    }
}
