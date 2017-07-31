/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.protocols.twilio.notifd;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>TwilioSmsNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class TwilioSmsNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(TwilioSmsNotificationStrategy.class);

    private static String ACCOUNT_SID_KEY = "org.opennms.protocols.twilio.accountSid";
    private static String AUTH_SID_KEY = "org.opennms.protocols.twilio.authSid";
    private static String AUTH_TOKEN_KEY = "org.opennms.protocols.twilio.authToken";
    private static String FROM_NUMBER_KEY = "org.opennms.protocols.twilio.fromNumber";
    private static String MESSAGING_SERVICE_KEY = "org.opennms.protocols.twilio.messagingService";
    private static String VALIDITY_PERIOD_KEY = "org.opennms.protocols.twilio.validityPeriod";
    private static Integer DEFAULT_VALIDITY_PERIOD = 14400;
    
	private static final Pattern s_e164Pattern = Pattern.compile("^\\+[1-9][0-9]+$");
	private static final Pattern s_nanpPattern = Pattern.compile("^[2-9][0-9]{2}[2-9][0-9]{6}$");

    private List<Argument> m_arguments;

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {

        m_arguments = arguments;

        String authSid = getAuthSid();
        String authToken = getAuthToken();
        String fromNumber = getFromNumber();
        String messagingService = getMessagingService();
        String toNumber = getToNumber();
        String body = getBody();
        
        if (authSid == null || authToken == null) {
        	LOG.error("send: authSid and/or authToken is null. TwilioSmsNotification requires both.");
        	return 1;
        }
        
        if (fromNumber == null && messagingService == null) {
        	LOG.error("send: fromNumber and messagingService are both null. TwilioSmsNotification requires at least one.");
        	return 1;
        }
        
        if (toNumber == null) {
        	LOG.error("send: no mobile number available. TwilioSmsNotification aborting.");
        	return 1;
        }
        
        if ("".equals(body)) {
        	LOG.error("send: Message body is blank. TwilioSmsNotification aborting.");
        	return 1;
        }
                
        LOG.debug("Creating Twilio message To={} From={} Body={}", toNumber, fromNumber, body);
        try {
			sendTwilioMessage(toNumber, body);
		} catch (MalformedURLException e) {
			LOG.error("MalformedURLException sending Twilio message: " + e.getMessage());
			return 1;
		} catch (UnsupportedEncodingException e) {
			LOG.error("UnsupportedEncodingException sending Twilio message: " + e.getMessage());
			return 1;
		} catch (JSONException e) {
			LOG.error("JSONException processing response from Twilio: " + e.getMessage());
			return 1;
		} catch (Exception e) {
			LOG.error("Unexpected " + e.getClass().getSimpleName() + ": " + e.getMessage());
			return 1;
		}
        
        return 0;
    }

    private String getAccountSid() {
    	String accountSid = System.getProperty(ACCOUNT_SID_KEY);
    	return accountSid;
    }
    private String getAuthSid() {
    	String authSid = System.getProperty(AUTH_SID_KEY);
    	return authSid;
    }
    
    private String getAuthToken() {
    	String authToken = System.getProperty(AUTH_TOKEN_KEY);
    	return authToken;
    }
    
    private String getFromNumber() {
    	String fromNumber = System.getProperty(FROM_NUMBER_KEY);
    	if (! numberLooksValid(fromNumber)) {
    		LOG.warn("From phone number '{}' does not look like a valid E.164 or NANP number. Proceeding, but Twilio may reject it.");
    	}
    	return fromNumber;
    }
    
    private Integer getValidityPeriod() {
    	Integer validityPeriod = null;
    	String validityStr = System.getProperty(VALIDITY_PERIOD_KEY);
    	try {
    		validityPeriod = Integer.valueOf(validityStr);
    	} catch (NumberFormatException nfe) {
    		LOG.warn("Invalid validity period {} specified. Using default value {}", validityStr, DEFAULT_VALIDITY_PERIOD);
    	}
    	return validityPeriod == null ? DEFAULT_VALIDITY_PERIOD : validityPeriod;
    }
    
    private String getMessagingService() {
    	String msgSvc = System.getProperty(MESSAGING_SERVICE_KEY);
    	return msgSvc;
    }
    
    private String getToNumber() {
    	String toNumber = getSwitchValue(NotificationManager.PARAM_MOBILE_PHONE);
    	if (toNumber == null) {
    		return null;
    	}
    	
    	if (! numberLooksValid(toNumber)) {
    		LOG.warn("To phone number '{}' does not look like a valid E.164 or NANP number. Proceeding, but Twilio may reject it.");
    	}
    	return toNumber;
    }
    
    private boolean numberLooksValid(String number) {
    	if (number == null) {
    		return false;
    	}
    	number.replaceAll("[. -]", "");
    	return (s_e164Pattern.matcher(number).matches() && s_nanpPattern.matcher(number).matches());
    }
    
    private String getBody() {
    	String body = getSwitchValue(NotificationManager.PARAM_TEXT_MSG);
    	return body;
    }
    /**
     * Helper method to look into the Argument list and return the associated value.
     * If the value is an empty String, this method returns null.
     * @param argSwitch
     * @return
     */
    private String getSwitchValue(String argSwitch) {
        String value = null;
        for (Argument arg : m_arguments) {
            if (arg.getSwitch().equals(argSwitch)) {
                value = arg.getValue();
            }
        }
        if (value != null && value.equals(""))
            value = null;

        return value;
    }
    
    private void sendTwilioMessage(String toNumber, String body) throws Exception {
    	final String messagesUrlStr = new StringBuilder("https://")
    			.append(getAuthSid())
    			.append(":")
    			.append(getAuthToken())
    			.append("@api.twilio.com/2010-04-01/Accounts/")
    			.append(getAccountSid())
    			.append("/Messages.json")
    			.toString();
    	
    	HttpClientWrapper clientWrapper = HttpClientWrapper.create()
    			.setConnectionTimeout(3000)
    			.setSocketTimeout(3000)
    			.useSystemProxySettings();
    	HttpPost postMethod = new HttpPost(messagesUrlStr);
    	
    	List<BasicNameValuePair> postData = new ArrayList<>();
    	postData.add(new BasicNameValuePair("To", toNumber));
    	if (getFromNumber() != null) {
    		LOG.debug("Using fromNumber '{}'", getFromNumber());
    		postData.add(new BasicNameValuePair("From", getFromNumber()));
    	}
    	if (getMessagingService() != null) {
    		LOG.debug("Using messagingServiceSid '{}'", getMessagingService());
    		postData.add(new BasicNameValuePair("MessagingServiceSid", getMessagingService()));
    	}
    	
    	LOG.debug("Using validityPeriod {}", getMessagingService());
    	postData.add(new BasicNameValuePair("ValidityPeriod", getValidityPeriod().toString()));
    	postData.add(new BasicNameValuePair("Body", body));
    	
    	postMethod.setEntity(new UrlEncodedFormEntity(postData));
    	
    	String contents = null;
    	int statusCode = -1;
    	try {
    		CloseableHttpResponse response = clientWrapper.getClient().execute(postMethod);
    		statusCode = response.getStatusLine().getStatusCode();
    		contents = EntityUtils.toString(response.getEntity());
    		LOG.debug("sendTwilioMessage: Response contents is: {}", contents);
    		
    	} catch (IOException e) {
    		LOG.error("sendTwilioMessage: I/O problem with Twilio API post/response: {}", e);
    		throw new RuntimeException("Problem with Twilio API transaction: "+e.getMessage());
    	} finally {
    		IOUtils.closeQuietly(clientWrapper);
    	}
    	
    	JSONObject jsonData = new JSONObject(contents);
    	if (statusCode != 201) {
    		LOG.error("sendTwilioMessage: Got unexpected {} response from Twilio API with contents: {}", statusCode, contents);
    		throw new RuntimeException("Unexpected HTTP response " + statusCode + " from Twilio API. Contents: " + contents);
    	}
    	
    	if ("accepted".equals(jsonData.get("status")) || ("queued".equals(jsonData.get("status")))) {
    		LOG.debug("Message submitted to Twilio API. Status={}, SID {}", jsonData.get("status"), jsonData.get("sid"));    		
    	} else if ("failed".equals(jsonData.get("status"))) {
    		throw new RuntimeException("Twilio API response indicates send failure, error code "  + jsonData.get("error_code") + "; Error message: " + jsonData.get("error_message"));
    	} else {
    		LOG.warn("Twilio API initial response has unexpected status '{}', expected 'accepted' or 'queued'. Proceeding, but this is weird.");
    	}
    }
}
