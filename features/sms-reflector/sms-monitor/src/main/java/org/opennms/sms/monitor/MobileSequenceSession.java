package org.opennms.sms.monitor;


import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.opennms.core.utils.PropertiesUtils;
import org.opennms.sms.monitor.internal.config.SequenceSessionVariable;
import org.opennms.sms.monitor.session.SessionVariableGenerator;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseMatchers;
import org.opennms.sms.reflector.smsservice.MobileMsgTracker;
import org.smslib.OutboundMessage;
import org.smslib.USSDRequest;
import org.smslib.USSDSessionStatus;

public class MobileSequenceSession {
	
	private static final int DEFAULT_RETRIES = 0;
	private static final long DEFAULT_TIMEOUT = 10000L;
	
    private List<SequenceSessionVariable> m_sessionVariables;
	private MobileMsgTracker m_tracker;
	

    public MobileSequenceSession(MobileMsgTracker tracker) {
        this(new HashMap<String, Object>(), Collections.<SequenceSessionVariable>emptyList(), tracker);
    }

    public MobileSequenceSession(Map<String, Object> parameters, List<SequenceSessionVariable> sessionVariables, MobileMsgTracker tracker) {
        
        m_sessionVariables = sessionVariables; 
        m_tracker = tracker;
        
        if (parameters.get("retry") == null) {
            parameters.put("retry", String.valueOf(DEFAULT_RETRIES));
        }
        if (parameters.get("timeout") == null) {
            parameters.put("timeout", String.valueOf(DEFAULT_TIMEOUT));
        }
        // first, transfer anything from the parameters to the session
        for (Map.Entry<String,Object> entry : parameters.entrySet()) {
            if (entry.getKey() != null && entry.getValue() != null) {
                getProperties().put(entry.getKey(), entry.getValue());
            }
        }
    }


	
	private Properties m_properties = new Properties();
	private Map<String, SessionVariableGenerator> m_generators = new HashMap<String,SessionVariableGenerator>();
	
	public Properties getProperties() {
		return m_properties;
	}
	
	public Map<String, SessionVariableGenerator> getGenerators() {
		return m_generators;
	}

	void setRecipient(String recipient) {
		getProperties().setProperty("recipient", recipient);
	}

	public int getRetries() {
		return Integer.parseInt(getProperties().getProperty("retry", String.valueOf(DEFAULT_RETRIES)));
	}

	public long getTimeout() {
		return Long.parseLong(getProperties().getProperty("timeout", String.valueOf(DEFAULT_TIMEOUT)));
	}
	
    public void setTimeout(long timeout) {
        getProperties().setProperty("timeout", String.valueOf(timeout));
    }

    public void setRetries(int retries) {
        getProperties().setProperty("retry", String.valueOf(retries));
    }

    public void setVariable(String varName, String value) {
        getProperties().setProperty(varName, value);
    }

	public String substitute(String string) {
		return PropertiesUtils.substitute(string, getProperties());
	}

	void checkinVariables() {
	    
	    for (SequenceSessionVariable var : m_sessionVariables) {
	        var.checkIn(getProperties());
	    }

	}

    void checkoutVariables() throws ClassNotFoundException, InstantiationException, IllegalAccessException {
    
	    for (SequenceSessionVariable var : m_sessionVariables) {
			var.checkOut(getProperties());
		}
	}

    public boolean eqOrMatches(String expected, String actual) {
		return MobileMsgResponseMatchers.isAMatch(substitute(expected), actual);
	}

	public boolean matches(String expected, String actual) {
		return actual == null ? expected == null : actual.matches(substitute(expected));
	}

	public boolean ussdStatusMatches(String expected, USSDSessionStatus actual) {
		USSDSessionStatus status;
	
		try {
			int statusVal = Integer.parseInt(substitute(expected));
			status = USSDSessionStatus.getByNumeric(statusVal);
		} catch (NumberFormatException e) {
			status = USSDSessionStatus.valueOf(substitute(expected));
		}
		
		return status.equals(actual);
	}

    public void sendSms(String gatewayId, String recipient, String text, int validityPeriodInHours, MobileMsgResponseHandler responseHandler) {
        MobileMsgRequest request = null;
        try {
            OutboundMessage msg = new OutboundMessage(substitute(recipient), substitute(text));
            msg.setGatewayId(substitute(gatewayId));
            msg.setValidityPeriod(validityPeriodInHours);
            request = m_tracker.sendSmsRequest(msg, getTimeout(), getRetries(), responseHandler, responseHandler);
        } catch (Exception e) {
        	responseHandler.handleError(request, e);
        }
    }

    public void sendUssd(String gatewayId, String text, MobileMsgResponseHandler responseHandler) {
        MobileMsgRequest request = null;
        try {
            USSDRequest ussdRequest = new USSDRequest(substitute(text));
            ussdRequest.setGatewayId(substitute(gatewayId));
            request = m_tracker.sendUssdRequest(ussdRequest, getTimeout(), getRetries(), responseHandler, responseHandler);
        } catch (Exception e) {
        	responseHandler.handleError(request, e);
        }
    }

}
