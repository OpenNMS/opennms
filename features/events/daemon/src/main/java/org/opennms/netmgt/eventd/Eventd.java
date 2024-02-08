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
package org.opennms.netmgt.eventd;

import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.dao.api.EventdServiceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

/**
 * <p>
 * Eventd listens for events from the discovery, capsd, trapd processes and
 * sends events to the Master Station when queried for.
 * </p>
 *
 * <p>
 * Eventd receives events sent in as XML, looks up the event.conf and adds
 * information to these events and stores them to the db. It also reconverts
 * them back to XML to be sent to other processes like 'actiond'
 * </p>
 *
 * <p>
 * Process like trapd, capsd etc. that are local to the distributed poller send
 * events to the eventd. Events can also be sent via TCP or UDP to eventd.
 * </p>
 *
 * <p>
 * Eventd listens for incoming events, loads info from the 'event.conf', adds
 * events to the database and sends the events added to the database to
 * subscribed listeners. It also maintains a servicename to serviceid mapping
 * from the services table so as to prevent a database lookup for each incoming
 * event
 * </P>
 *
 * <P>
 * The number of threads that processes events is configurable via the eventd
 * configuration xml
 * </P>
 *
 * @author <A HREF="mailto:sowmya@opennms.org">Sowmya Nataraj </A>
 * @author <A HREF="http://www.opennms.org">OpenNMS.org </A>
 */
public final class Eventd extends AbstractServiceDaemon {
    
    private static final Logger LOG = LoggerFactory.getLogger(Eventd.class);
    
    /**
     * The log4j category used to log debug messsages and statements.
     */
    public static final String LOG4J_CATEGORY = "eventd";

    /**
     * Reference to the event processor
     */
    private BroadcastEventProcessor m_receiver;

    /**
     * Class that handles mapping of service names to service IDs.
     */
    private EventdServiceManager m_eventdServiceManager;

    /**
     * Constuctor creates the localhost address(to be used eventually when
     * eventd originates events during correlation) and the broadcast queue
     */
    public Eventd() {
        super(LOG4J_CATEGORY);
    }

    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        Assert.state(m_eventdServiceManager != null, "property eventdServiceManager must be set");
        Assert.state(m_receiver != null, "property receiver must be set");
        
        m_eventdServiceManager.dataSourceSync();
    }

    /**
     * <p>onStart</p>
     */
    @Override
    protected void onStart() {
        LOG.debug("Eventd running");
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        if (m_receiver != null) {
            m_receiver.close();
        }
        LOG.debug("Eventd stopped");
    }

    /**
     * <p>getEventdServiceManager</p>
     *
     * @return a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
     */
    public EventdServiceManager getEventdServiceManager() {
        return m_eventdServiceManager;
    }

    /**
     * <p>setEventdServiceManager</p>
     *
     * @param eventdServiceManager a {@link org.opennms.netmgt.dao.api.EventdServiceManager} object.
     */
    public void setEventdServiceManager(EventdServiceManager eventdServiceManager) {
        m_eventdServiceManager = eventdServiceManager;
    }

    /**
     * <p>getReceiver</p>
     *
     * @return a {@link org.opennms.netmgt.eventd.BroadcastEventProcessor} object.
     */
    public BroadcastEventProcessor getReceiver() {
        return m_receiver;
    }

    /**
     * <p>setReceiver</p>
     *
     * @param receiver a {@link org.opennms.netmgt.eventd.BroadcastEventProcessor} object.
     */
    public void setReceiver(BroadcastEventProcessor receiver) {
        m_receiver = receiver;
    }

}
