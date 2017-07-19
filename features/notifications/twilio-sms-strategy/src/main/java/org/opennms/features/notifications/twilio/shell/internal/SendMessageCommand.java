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
 * OpenNMS(R) Licensing <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.notifications.twilio.shell.internal;

import java.util.Objects;

import org.apache.felix.gogo.commands.Argument;
import org.apache.felix.gogo.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.notifications.twilio.internal.ConfigManager;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * @author Jeff Gehlbach <mailto:jeffg@opennms.com>
 *
 */

@Command(scope = "twilio", name = "send-message", description="Send a message via the Twilio messaging API")
public class SendMessageCommand extends OsgiCommandSupport {
	private ConfigManager m_configManager;
	
    @Argument(index = 0, name = "toNumber", description = "Destination phone number in E164 format", required = true, multiValued = false)
    String toNumber;
    
    @Argument(index = 1, name = "body", description = "Message body to send")
    String body;
    
	@Override
	protected Object doExecute() throws Exception {
		String sid = m_configManager.getAuthSid();
		String token = m_configManager.getAuthToken();
		String fromNumber = m_configManager.getFromNumber();

		System.out.println("Initializing Twilio API (SID=" + sid + "...");
		Twilio.init(m_configManager.getAuthSid(), m_configManager.getAuthToken());
		
		System.out.println("Sending message:\n\tFrom: " + fromNumber + "\n\tTo:   " + toNumber + "\n\tBody: " + body);
		Message message = Message.creator(
				new PhoneNumber(toNumber),
				new PhoneNumber(m_configManager.getFromNumber()),
				body
		).create();
		
		System.out.println("Request submitted. Message SID = " + message.getSid());
	}
	
    public void setConfigManager(ConfigManager configManager) {
        m_configManager = Objects.requireNonNull(configManager);
    }

}
