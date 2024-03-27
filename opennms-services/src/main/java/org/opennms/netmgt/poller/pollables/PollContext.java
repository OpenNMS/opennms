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
package org.opennms.netmgt.poller.pollables;

import java.net.InetAddress;
import java.util.Date;

import org.opennms.netmgt.poller.PollStatus;
import org.opennms.netmgt.poller.AsyncPollingEngine;
import org.opennms.netmgt.xml.event.Event;

/**
 * Represents a PollContext
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @version $Id: $
 */
public interface PollContext {
    
    /**
     * <p>getCriticalServiceName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getCriticalServiceName();

    /**
     * <p>isNodeProcessingEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isNodeProcessingEnabled();

    /**
     * <p>isPollingAllIfCritServiceUndefined</p>
     *
     * @return a boolean.
     */
    public boolean isPollingAllIfCritServiceUndefined();

    /**
     * <p>sendEvent</p>
     *
     * @param event the event to send
     * @return the same event
     */
    public PollEvent sendEvent(Event event);

    /**
     * <p>createEvent</p>
     *
     * @param uei a {@link java.lang.String} object.
     * @param nodeId a int.
     * @param address a {@link java.net.InetAddress} object.
     * @param svcName a {@link java.lang.String} object.
     * @param date a {@link java.util.Date} object.
     * @param reason a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.xml.event.Event} object.
     */
    public Event createEvent(String uei, int nodeId, InetAddress address, String svcName, Date date, String reason);

    /**
     * <p>openOutage</p>
     *
     * @param pSvc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param svcLostEvent a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public void openOutage(PollableService pSvc, PollEvent svcLostEvent);

    /**
     * <p>resolveOutage</p>
     *
     * @param pSvc a {@link org.opennms.netmgt.poller.pollables.PollableService} object.
     * @param svcRegainEvent a {@link org.opennms.netmgt.poller.pollables.PollEvent} object.
     */
    public void resolveOutage(PollableService pSvc, PollEvent svcRegainEvent);
    /**
     * <p>isServiceUnresponsiveEnabled</p>
     *
     * @return a boolean.
     */
    public boolean isServiceUnresponsiveEnabled();

    void trackPoll(PollableService service, PollStatus result);

    boolean isAsyncEngineEnabled();

    AsyncPollingEngine getAsyncPollingEngine();

}
