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

package org.opennms.features.subscriptionchecker.internal;

import org.opennms.features.subscriptionchecker.web.internal.ModalInjector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Maintains state loaded from cfg file
 */
public class Model {
	private static final Logger LOG = LoggerFactory.getLogger(Model.class);


	private boolean enabled;
	private String subscriptionArtifactId;
	private String subscriptionGroupId;
	private String subscriptionVersion;
	private String adminMessage;
	private String userMessage;
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	public String getSubscriptionArtifactId() {
		return subscriptionArtifactId;
	}
	
	public void setSubscriptionArtifactId(String subscriptionArtifactId) {
		this.subscriptionArtifactId = subscriptionArtifactId;
	}
	
	public String getSubscriptionGroupId() {
		return subscriptionGroupId;
	}
	
	public void setSubscriptionGroupId(String subscriptionGroupId) {
		this.subscriptionGroupId = subscriptionGroupId;
	}
	
	public String getSubscriptionVersion() {
		return subscriptionVersion;
	}
	
	public void setSubscriptionVersion(String subscriptionVersion) {
		this.subscriptionVersion = subscriptionVersion;
	}
	
	public String getAdminMessage() {
		return adminMessage;
	}
	
	public void setAdminMessage(String adminMessage) {
		this.adminMessage = adminMessage;
	}
	
	public String getUserMessage() {
		return userMessage;
	}
	
	public void setUserMessage(String userMessage) {
		this.userMessage = userMessage;
	}
	
	public void init(){
		if(LOG.isDebugEnabled()) {
			LOG.debug("subscription model initialised with values: "+toString());
		}
		
	}

	@Override
	public String toString() {
		return "Model [enabled=" + enabled + ", subscriptionArtifactId="
				+ subscriptionArtifactId + ", subscriptionGroupId="
				+ subscriptionGroupId + ", subscriptionVersion="
				+ subscriptionVersion + ", adminMessage=" + adminMessage
				+ ", userMessage=" + userMessage + "]";
	}

	
}
