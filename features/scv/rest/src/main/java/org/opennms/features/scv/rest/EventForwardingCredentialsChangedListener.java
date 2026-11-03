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
package org.opennms.features.scv.rest;

import java.util.Date;

import org.opennms.features.scv.api.CredentialsChangedListener;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.xml.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Forwards a nodeMetadataUpdated event when SCV credentials change, so daemons can
 * discard cached values interpolated from ${scv:...} expressions. The event carries
 * no node id since credentials are not tied to a single node.
 *
 * Registered as an OSGi service so the SCV shell commands can pick it up; this
 * bundle only ships on OpenNMS core, keeping the event API out of the shell bundle
 * which also boots on Minion and Sentinel.
 */
public class EventForwardingCredentialsChangedListener implements CredentialsChangedListener {

    private static final Logger LOG = LoggerFactory.getLogger(EventForwardingCredentialsChangedListener.class);

    private final EventForwarder eventForwarder;

    public EventForwardingCredentialsChangedListener(final EventForwarder eventForwarder) {
        this.eventForwarder = eventForwarder;
    }

    @Override
    public void credentialsChanged(final String source) {
        if (eventForwarder == null) {
            return;
        }
        try {
            final Event event = new Event();
            event.setUei(EventConstants.NODE_METADATA_UPDATED_EVENT_UEI);
            event.setSource(source);
            event.setTime(new Date());
            eventForwarder.sendNow(event);
        } catch (Exception e) {
            LOG.warn("Failed to send {} event", EventConstants.NODE_METADATA_UPDATED_EVENT_UEI, e);
        }
    }
}
