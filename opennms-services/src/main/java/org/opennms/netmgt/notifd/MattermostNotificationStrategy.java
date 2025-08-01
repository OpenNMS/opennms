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

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>MattermostNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 * @version $Id: $
 */
public class MattermostNotificationStrategy extends AbstractSlackCompatibleNotificationStrategy {
	protected static final Logger LOG = LoggerFactory.getLogger(MattermostNotificationStrategy.class);
	
    private static final String MM_URL_PROPERTY = "org.opennms.netmgt.notifd.mattermost.webhookURL";
    private static final String MM_USERNAME_PROPERTY = "org.opennms.netmgt.notifd.mattermost.username";
    private static final String MM_ICONURL_PROPERTY = "org.opennms.netmgt.notifd.mattermost.iconURL";
    private static final String MM_ICONEMOJI_PROPERTY = "org.opennms.netmgt.notifd.mattermost.iconEmoji";
    private static final String MM_CHANNEL_PROPERTY = "org.opennms.netmgt.notifd.mattermost.channel";
    private static final String MM_USE_SYSTEM_PROXY = "org.opennms.netmgt.notifd.mattermost.useSystemProxy";
    
    @Override
    protected String formatWebhookErrorResponse(int statusCode, String contents) {
    	final StringBuilder bldr = new StringBuilder("Response code: ");
    	bldr.append(statusCode);
    	
    	JSONObject errorJson = new JSONObject();
    	JSONParser jp = new JSONParser();
    	try {
			Object parsedError = jp.parse(contents);
			if (parsedError instanceof JSONObject) {
				LOG.debug("Got back some JSON. Parsing for dissection.");
				errorJson = (JSONObject)parsedError;
			}
		} catch (ParseException e) {
			LOG.warn("Got some non-JSON error.", e);
			bldr.append(" Contents:").append(contents);
			return bldr.toString();
		}
    	
    	bldr.append("; Message: ").append(errorJson.get("message"));
    	bldr.append("; Detailed error: ").append(errorJson.get("detailed_error"));
    	bldr.append("; Request ID: ").append(errorJson.get("request_id"));
    	bldr.append("; Status code: ").append(errorJson.get("status_code"));
    	bldr.append("; Is OAUTH?: ").append(errorJson.get("is_oauth"));
    	return bldr.toString();
    }

    @Override
	protected String decorateMessageSubject(String subject) {
    	if ("".equals(subject)) {
    		return "";
    	}
    	final StringBuilder bldr = new StringBuilder("**");
    	bldr.append(subject).append("**").append("\n");
    	return bldr.toString();
    }
    
    @Override
	protected String decorateMessageBody(String body) {
    	return body;
    }
    
    @Override
	protected String getUrlPropertyName() {
    	return MM_URL_PROPERTY;
    }

    @Override
    protected String getUseSystemProxyPropertyName() {
        return MM_USE_SYSTEM_PROXY;
    }

    @Override
	protected String getUsernamePropertyName() {
    	return MM_USERNAME_PROPERTY;
    }
    
    @Override
	protected String getIconUrlPropertyName() {
    	return MM_ICONURL_PROPERTY;
    }
    
    @Override
	protected String getIconEmojiPropertyName() {
    	LOG.warn("Icon emoji may not work with all versions of Mattermost.");
    	return MM_ICONEMOJI_PROPERTY;
    }
    
    @Override
	protected String getChannelPropertyName() {
    	return MM_CHANNEL_PROPERTY;
    }
}
