/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.core.web.HttpClientWrapperConfigHelper;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

public abstract class AbstractSlackCompatibleNotificationStrategy implements NotificationStrategy {

	protected abstract String getChannelPropertyName();

	protected abstract String getIconEmojiPropertyName();

	protected abstract String getIconUrlPropertyName();

	protected abstract String getUsernamePropertyName();

	protected abstract String getUrlPropertyName();

	protected abstract String getUseSystemProxyPropertyName();

	protected abstract String decorateMessageBody(String body);

	protected abstract String decorateMessageSubject(String subject);

	protected abstract String formatWebhookErrorResponse(int statusCode, String contents);

	protected static final Logger LOG = LoggerFactory.getLogger(AbstractSlackCompatibleNotificationStrategy.class);
	private final List<Argument> m_arguments = Lists.newArrayList();

	protected void setArguments(List<Argument> arguments) {
		m_arguments.clear();
		m_arguments.addAll(arguments);
	}

	/** {@inheritDoc} */
	@SuppressWarnings("unchecked")
	public int send(List<Argument> arguments) {
		if (arguments != null) {
			setArguments(arguments);
		}

	    String url = getUrl();
	    if (url == null) {
	        LOG.error("send: url must not be null");
	        return 1;
	    }
	    String iconUrl = getIconUrl();
	    String iconEmoji = getIconEmoji();
	    String channel = getChannel();
	    String message = buildMessage(arguments);
	
	    final HttpClientWrapper clientWrapper = HttpClientWrapper.create()
	            .setConnectionTimeout(3000)
	            .setSocketTimeout(3000);
		if(getUseSystemProxy()) {
	        clientWrapper.useSystemProxySettings();
		}
	
	    HttpPost postMethod = new HttpPost(url);
	
	    JSONObject jsonData = new JSONObject();
	    jsonData.put("username", getUsername());
	    if (iconUrl != null) {
	    	jsonData.put("icon_url", iconUrl);
	    }
	    if (iconEmoji != null) {
	    	jsonData.put("icon_emoji", iconEmoji);
	    }
	    if (channel != null) {
	    	jsonData.put("channel", channel);
	    }
	    jsonData.put("text", message);
	    
	    if ( jsonData.containsKey("icon_url") && jsonData.containsKey("icon_emoji") ) {
	    	LOG.warn("Both URL and emoji specified for icon. Sending both; behavior is undefined.");
	    }
	
	    LOG.debug("Prepared JSON POST data for webhook is: {}", jsonData.toJSONString());
	    final HttpEntity entity = new StringEntity(jsonData.toJSONString(), ContentType.APPLICATION_JSON);
	    postMethod.setEntity(entity);
	    // Mattermost 1.1.0 does not like having charset specified alongside Content-Type
	    postMethod.setHeader("Content-Type", "application/json");
	
	    String contents = null;
	    int statusCode = -1;
	    try {
	        CloseableHttpResponse response = clientWrapper.getClient().execute(postMethod);
	        statusCode = response.getStatusLine().getStatusCode();
	        contents = EntityUtils.toString(response.getEntity());
	        LOG.debug("send: Contents is: {}", contents);
	    } catch (IOException e) {
	        LOG.error("send: I/O problem with webhook post/response", e);
	        throw new RuntimeException("Problem with webhook post: "+e.getMessage());
	    } finally {
	        IOUtils.closeQuietly(clientWrapper);
	    }
	    
	    if ("ok".equals(contents)) {
	    	LOG.debug("Got 'ok' back from webhook, indicating success.");
	    	statusCode = 0;
	    } else {
	    	LOG.info("Got a non-ok response from webhook, attempting to dissect response.");
	    	LOG.error("Webhook returned non-OK response to notification post: {}", formatWebhookErrorResponse(statusCode, contents));
	    	statusCode = 1;
	    }
	
	    return statusCode;
	}

	protected String getUrl() {
		String url = getValueFromSwitchOrProp("Webhook URL", "-url", getUrlPropertyName());
	
		if (url == null) {
			LOG.error("No webhook URL specified as a notification command switch or via system property {}. Cannot continue.", getUrlPropertyName());
		}
		return url;
	}

	protected String getUsername() {
		String username = getValueFromSwitchOrProp("Bot username", "-username", getUsernamePropertyName());
		
		if (username == null) {
			LOG.warn("No bot username specified as a notification command switch or via system property {}. Using default value opennms.", getUsernamePropertyName());
			return "opennms";
		}
		return username;
	}

	protected String getIconUrl() {
		String iconurl = getValueFromSwitchOrProp("Icon URL", "-iconurl", getIconUrlPropertyName());
		
		if (iconurl == null) {
			LOG.info("No icon URL specified as a notification command switch or via system property {}. Not setting one.", getIconUrlPropertyName());
		}
		return iconurl;
	}

	protected boolean getUseSystemProxy() {
		String useSystemProxy = getValueFromSwitchOrProp("Use System Proxy", HttpClientWrapperConfigHelper.PARAMETER_KEYS.useSystemProxy.name(), getUseSystemProxyPropertyName());

		if (useSystemProxy == null) {
			LOG.info("useSystemProxy is not specified as a notification command switch or via system property {}. Setting it to true (use system proxy settings).", getUseSystemProxyPropertyName());
            useSystemProxy="true"; // legacy behaviour
		}
		return Boolean.parseBoolean(useSystemProxy);
	}

	protected String getIconEmoji() {
		String iconemoji = getValueFromSwitchOrProp("Icon Emoji", "-iconemoji", getIconEmojiPropertyName());
		
		if (iconemoji == null) {
			LOG.info("No icon emoji specified as a notification command switch or via system property {}. Not setting one.", getIconEmojiPropertyName());
		}
		if (iconemoji != null && ! iconemoji.startsWith(":") && ! iconemoji.endsWith(":")) {
			LOG.warn("Specified icon emoji '{}' is not colon-enclosed. Did you mean to do this?", iconemoji);
		}
		return iconemoji;
	}

	protected String getChannel() {
		String channel = getValueFromSwitchOrProp("Channel name", "-channel", getChannelPropertyName());
		
		if (channel == null) {
			LOG.info("No channel specified as a notification command switch or via system property {}. Not setting one.", getChannelPropertyName());
		}
		return channel;
	}

	protected String getValueFromSwitchOrProp(String what, String switchName, String propName) {
		if (Strings.isNullOrEmpty(switchName)) {
			LOG.error("Switch name must not be null or empty");
			return null;
		}

		// Inform user about API changes, but do not enforce them
		if (!switchName.startsWith("-")) {
			LOG.warn("Specifying switch names (e.g. '{}') without a leading dash is deprecated. Please update your notification command definition to use '-{}' instead. See https://issues.opennms.org/browse/NMS-10552", switchName, switchName);
		}

		// Try getting the value for switchName
		LOG.debug("Trying to get {} from notification switch '{}'", what, switchName);
		if (hasSwitchName(switchName) && hasSwitchValue(switchName)) {
			final String val = getSwitchValue(switchName);
			LOG.info("Using {} value {} from notification switch {}", what, val, switchName);
			return val;
		}

		// switchName does not exist, try with alternate version
		final String alternateSwitchName = switchName.startsWith("-") ? switchName.substring(1) : "-" + switchName;
		LOG.debug("Trying to get {} from notification switch '{}'", what, alternateSwitchName);
		if (hasSwitchValue(alternateSwitchName) && hasSwitchValue(alternateSwitchName)) {
			final String val = getSwitchValue(alternateSwitchName);
			LOG.info("Using {} value {} from notification switch '{}'", what, val, switchName);
			return val;
		}

		if (propName != null) {
			LOG.debug("Trying to get {} from system property '{}'", what, propName);
			final String val = System.getProperty(propName);
			if (val != null) {
				LOG.info("Using {} value {} from system property '{}'", what, val, propName);
				return val;
			}
		}

		LOG.warn("Could not determine value for {} from notification command switch '{}' or system property '{}'", what, switchName, propName);
		return null;
	}

	/**
	 * Helper method to look into the Argument list and return the associated value.
	 * If the value is an empty String, this method returns null.
	 * @param argSwitch
	 * @return
	 */
	private String getSwitchValue(String argSwitch) {
	    String value = null;
	    for (Iterator<Argument> it = m_arguments.iterator(); it.hasNext();) {
	        Argument arg = it.next();
	        if (arg.getSwitch().equals(argSwitch)) {
	            if (! "".equals(arg.getValue())) {
	            	value = arg.getValue();
	            } else if (! "".equals(arg.getSubstitution())) {
	            	value = arg.getSubstitution();
	            }
	        }
	    }
	    if (value != null && value.equals(""))
	        value = null;
	
	    return value;
	}

	protected String buildMessage(List<Argument> args) {
		String subject = getValueFromSwitchOrProp("Subject", NotificationManager.PARAM_SUBJECT, null);
		String body = getValueFromSwitchOrProp("Message Body", NotificationManager.PARAM_TEXT_MSG, null);

		final StringBuilder bldr = new StringBuilder();
		if ("".equals(subject) || "RESOLVED: ".equals(subject)) {
			subject = null;
		} else {
			bldr.append(decorateMessageSubject(subject));
		}
		
		if ("".equals(body) || "RESOLVED: ".equals(body)) {
			body = null;
		} else {
			bldr.append(decorateMessageBody(body));
		}

		return bldr.toString();
	}

	protected String getValue(String switchName) {
		return getValueFromSwitchOrProp(switchName, switchName, null);
	}

	protected String getValue(String switchName, String systemPropertyName) {
		return getValueFromSwitchOrProp(switchName, switchName, systemPropertyName);
	}

	private boolean hasSwitchName(String theSwitchName) {
		return m_arguments.stream().anyMatch(a -> a.getSwitch().equals(theSwitchName));
	}

	private boolean hasSwitchValue(String theSwitchName) {
		final List<String> values = m_arguments.stream()
				.filter(a -> a.getSwitch().equals(theSwitchName))
				.flatMap(a -> Lists.newArrayList(a.getValue(), a.getSubstitution()).stream())
				.filter(value -> value != null && !"".equals(value))
				.collect(Collectors.toList());
		return !values.isEmpty();
	}
}
