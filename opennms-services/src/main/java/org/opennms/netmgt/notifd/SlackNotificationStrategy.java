/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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
