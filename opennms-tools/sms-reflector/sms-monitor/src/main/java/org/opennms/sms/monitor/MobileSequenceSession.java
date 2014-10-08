/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

/**
 * <p>MobileSequenceSession class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
public class MobileSequenceSession {
	
	private static final int DEFAULT_RETRIES = 0;
	private static final long DEFAULT_TIMEOUT = 10000L;
	
    private List<SequenceSessionVariable> m_sessionVariables;
	private MobileMsgTracker m_tracker;
	

    /**
     * <p>Constructor for MobileSequenceSession.</p>
     *
     * @param tracker a {@link org.opennms.sms.reflector.smsservice.MobileMsgTracker} object.
     */
    public MobileSequenceSession(MobileMsgTracker tracker) {
        this(new HashMap<String, Object>(), Collections.<SequenceSessionVariable>emptyList(), tracker);
    }

    /**
     * <p>Constructor for MobileSequenceSession.</p>
     *
     * @param parameters a {@link java.util.Map} object.
     * @param sessionVariables a {@link java.util.List} object.
     * @param tracker a {@link org.opennms.sms.reflector.smsservice.MobileMsgTracker} object.
     */
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
	
	/**
	 * <p>getProperties</p>
	 *
	 * @return a {@link java.util.Properties} object.
	 */
	public Properties getProperties() {
		return m_properties;
	}
	
	/**
	 * <p>getGenerators</p>
	 *
	 * @return a {@link java.util.Map} object.
	 */
	public Map<String, SessionVariableGenerator> getGenerators() {
		return m_generators;
	}

	void setRecipient(String recipient) {
		getProperties().setProperty("recipient", recipient);
	}

	/**
	 * <p>getRetries</p>
	 *
	 * @return a int.
	 */
	public int getRetries() {
		return Integer.parseInt(getProperties().getProperty("retry", String.valueOf(DEFAULT_RETRIES)));
	}

	/**
	 * <p>getTimeout</p>
	 *
	 * @return a long.
	 */
	public long getTimeout() {
		return Long.parseLong(getProperties().getProperty("timeout", String.valueOf(DEFAULT_TIMEOUT)));
	}
	
    /**
     * <p>setTimeout</p>
     *
     * @param timeout a long.
     */
    public void setTimeout(long timeout) {
        getProperties().setProperty("timeout", String.valueOf(timeout));
    }

    /**
     * <p>setRetries</p>
     *
     * @param retries a int.
     */
    public void setRetries(int retries) {
        getProperties().setProperty("retry", String.valueOf(retries));
    }

    /**
     * <p>setVariable</p>
     *
     * @param varName a {@link java.lang.String} object.
     * @param value a {@link java.lang.String} object.
     */
    public void setVariable(String varName, String value) {
        getProperties().setProperty(varName, value);
    }

	/**
	 * <p>substitute</p>
	 *
	 * @param string a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
	 */
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

    /**
     * <p>eqOrMatches</p>
     *
     * @param expected a {@link java.lang.String} object.
     * @param actual a {@link java.lang.String} object.
     * @return a boolean.
     */
    public boolean eqOrMatches(String expected, String actual) {
		return MobileMsgResponseMatchers.isAMatch(substitute(expected), actual);
	}

	/**
	 * <p>matches</p>
	 *
	 * @param expected a {@link java.lang.String} object.
	 * @param actual a {@link java.lang.String} object.
	 * @return a boolean.
	 */
	public boolean matches(String expected, String actual) {
		return actual == null ? expected == null : actual.matches(substitute(expected));
	}

	/**
	 * <p>ussdStatusMatches</p>
	 *
	 * @param expected a {@link java.lang.String} object.
	 * @param actual a {@link org.smslib.USSDSessionStatus} object.
	 * @return a boolean.
	 */
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

    /**
     * <p>sendSms</p>
     *
     * @param gatewayId a {@link java.lang.String} object.
     * @param recipient a {@link java.lang.String} object.
     * @param text a {@link java.lang.String} object.
     * @param validityPeriodInHours a int.
     * @param responseHandler a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler} object.
     */
    public void sendSms(String gatewayId, String recipient, String text, int validityPeriodInHours, MobileMsgResponseHandler responseHandler) {
        MobileMsgRequest request = null;
        try {
            OutboundMessage msg = new OutboundMessage(substitute(recipient), substitute(text));
            msg.setGatewayId(substitute(gatewayId));
            msg.setValidityPeriod(validityPeriodInHours);
            request = m_tracker.sendSmsRequest(msg, getTimeout(), getRetries(), responseHandler, responseHandler);
        } catch (Throwable e) {
        	responseHandler.handleError(request, e);
        }
    }

    /**
     * <p>sendUssd</p>
     *
     * @param gatewayId a {@link java.lang.String} object.
     * @param text a {@link java.lang.String} object.
     * @param responseHandler a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler} object.
     */
    public void sendUssd(String gatewayId, String text, MobileMsgResponseHandler responseHandler) {
        MobileMsgRequest request = null;
        try {
            USSDRequest ussdRequest = new USSDRequest(substitute(text));
            ussdRequest.setGatewayId(substitute(gatewayId));
            request = m_tracker.sendUssdRequest(ussdRequest, getTimeout(), getRetries(), responseHandler, responseHandler);
        } catch (Throwable e) {
        	responseHandler.handleError(request, e);
        }
    }

}
