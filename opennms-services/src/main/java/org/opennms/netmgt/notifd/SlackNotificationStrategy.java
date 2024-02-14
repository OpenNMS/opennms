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

/**
 * <p>SlackNotificationStrategy class.</p>
 *
 * @author <a href="mailto:jeffg@opennms.org">Jeff Gehlbach</a>
 */

public class SlackNotificationStrategy extends AbstractSlackCompatibleNotificationStrategy {
    private static final String SLACK_URL_PROPERTY = "org.opennms.netmgt.notifd.slack.webhookURL";
    private static final String SLACK_USERNAME_PROPERTY = "org.opennms.netmgt.notifd.slack.username";
    private static final String SLACK_ICONURL_PROPERTY = "org.opennms.netmgt.notifd.slack.iconURL";
    private static final String SLACK_ICONEMOJI_PROPERTY = "org.opennms.netmgt.notifd.slack.iconEmoji";
    private static final String SLACK_CHANNEL_PROPERTY = "org.opennms.netmgt.notifd.slack.channel";
    private static final String SLACK_USE_SYSTEM_PROXY = "org.opennms.netmgt.notifd.slack.useSystemProxy";

    @Override
    protected String decorateMessageSubject(String subject) {
    	if ("".equals(subject)) {
    		return "";
    	}
    	final StringBuilder bldr = new StringBuilder("*");
    	bldr.append(subject).append("*").append("\n");
    	return bldr.toString();
    }
    
    protected String getUrlPropertyName() {
    	return SLACK_URL_PROPERTY;
    }
    
    protected String getUsernamePropertyName() {
    	return SLACK_USERNAME_PROPERTY;
    }
    
    protected String getIconUrlPropertyName() {
    	return SLACK_ICONURL_PROPERTY;
    }
    
    protected String getIconEmojiPropertyName() {
    	return SLACK_ICONEMOJI_PROPERTY;
    }
    
    protected String getChannelPropertyName() {
    	return SLACK_CHANNEL_PROPERTY;
    }

    @Override
    protected String getUseSystemProxyPropertyName() {
        return SLACK_USE_SYSTEM_PROXY;
    }

	protected String decorateMessageBody(String body) {
		return body;
	}

    @Override
	protected String formatWebhookErrorResponse(int statusCode, String contents) {
    	final StringBuilder bldr = new StringBuilder("Response code: ");
    	bldr.append(statusCode).append("; ");
    	bldr.append(" Message: ").append(contents);
    	return bldr.toString();
    }
}
