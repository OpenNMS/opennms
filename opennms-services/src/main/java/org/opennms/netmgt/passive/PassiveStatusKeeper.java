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
package org.opennms.netmgt.passive;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.opennms.core.utils.Querier;
import org.opennms.netmgt.daemon.AbstractServiceDaemon;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManager;
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.events.EventUtils;
import org.opennms.netmgt.poller.PollStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>PassiveStatusKeeper class.</p>
 *
 * @author <a href="mailto:brozow@opennms.org">Mathew Brozowski</a>
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 */
public class PassiveStatusKeeper extends AbstractServiceDaemon implements EventListener {
    
    private static final Logger LOG = LoggerFactory.getLogger(PassiveStatusKeeper.class);
    
    private static PassiveStatusKeeper s_instance = new PassiveStatusKeeper();
    
    private static final String PASSIVE_STATUS_UEI = "uei.opennms.org/services/passiveServiceStatus";

    private volatile Map<PassiveStatusKey, PollStatus> m_statusTable = null;
    private volatile EventIpcManager m_eventMgr;
    private volatile boolean m_initialized = false;

    private DataSource m_dataSource;

    
    /**
     * <p>Constructor for PassiveStatusKeeper.</p>
     */
    public PassiveStatusKeeper() {
    	super("passive");
    }
    
    /**
     * <p>Constructor for PassiveStatusKeeper.</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public PassiveStatusKeeper(EventIpcManager eventMgr) {
    	this();
        setEventManager(eventMgr);
    }
    
    /**
     * <p>setInstance</p>
     *
     * @param psk a {@link org.opennms.netmgt.passive.PassiveStatusKeeper} object.
     */
    public static synchronized void setInstance(PassiveStatusKeeper psk) {
        s_instance = psk;
    }
    
    /**
     * <p>getInstance</p>
     *
     * @return a {@link org.opennms.netmgt.passive.PassiveStatusKeeper} object.
     */
    public static synchronized PassiveStatusKeeper getInstance() {
        return s_instance;
    }

    
    /**
     * <p>onInit</p>
     */
    @Override
    protected void onInit() {
        if (m_initialized) return;
        
        checkPreRequisites();
        createMessageSelectorAndSubscribe();
        
        m_statusTable = new HashMap<PassiveStatusKey, PollStatus>();
        
        String sql = "select node.nodeLabel AS nodeLabel, ipInterface.ipAddr AS ipAddr, service.serviceName AS serviceName " +
                "FROM outages " +
                "JOIN ifServices ON outages.ifServiceId = ifServices.id " +
                "JOIN ipInterface ON ifServices.ipInterfaceId = ipInterface.id " +
                "JOIN node ON ipInterface.nodeId = node.nodeId " +
                "JOIN service ON ifServices.serviceId = service.serviceId " +
                "WHERE outages.ifRegainedService is NULL AND outages.perspective is NULL";
        
        Querier querier = new Querier(m_dataSource, sql) {
        
            @Override
            public void processRow(ResultSet rs) throws SQLException {
               
                PassiveStatusKey key = new PassiveStatusKey(rs.getString("nodeLabel"), rs.getString("ipAddr"), rs.getString("serviceName"));
                m_statusTable.put(key, PollStatus.down());
                
                
                
            }
        
        };
        querier.execute();
        
        
        
        m_initialized = true;
    }

    private void checkPreRequisites() {
        if (m_eventMgr == null)
            throw new IllegalStateException("eventManager has not been set");
        if (m_dataSource == null)
            throw new IllegalStateException("dataSource has not been set");
    }

    /**
     * <p>onStop</p>
     */
    @Override
    protected void onStop() {
        m_initialized = false;
        m_eventMgr = null;
        m_statusTable = null;
    }

    /**
     * <p>setStatus</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @param pollStatus a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void setStatus(String nodeLabel, String ipAddr, String svcName, PollStatus pollStatus) {
        checkInit();
        setStatus(new PassiveStatusKey(nodeLabel, ipAddr, svcName), pollStatus);
    }
    
    /**
     * <p>setStatus</p>
     *
     * @param key a {@link org.opennms.netmgt.passive.PassiveStatusKey} object.
     * @param pollStatus a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public void setStatus(PassiveStatusKey key, PollStatus pollStatus) {
        checkInit();
        m_statusTable.put(key, pollStatus);
    }

    private void checkInit() {
        if (!m_initialized)
            throw new IllegalStateException("the service has not been intialized");
    }

    /**
     * <p>getStatus</p>
     *
     * @param nodeLabel a {@link java.lang.String} object.
     * @param ipAddr a {@link java.lang.String} object.
     * @param svcName a {@link java.lang.String} object.
     * @return a {@link org.opennms.netmgt.poller.PollStatus} object.
     */
    public PollStatus getStatus(String nodeLabel, String ipAddr, String svcName) {
        //FIXME: Throw a log or exception here if this method is called and the this class hasn't been initialized
        PollStatus status = (m_statusTable == null ? PollStatus.unknown() : m_statusTable.get(new PassiveStatusKey(nodeLabel, ipAddr, svcName)));
        return (status == null ? PollStatus.up() : status);
    }

    private void createMessageSelectorAndSubscribe() {
        // Subscribe to eventd
        getEventManager().addEventListener(this, PASSIVE_STATUS_UEI);
    }

    /** {@inheritDoc} */
    @Override
    public void onEvent(IEvent e) {
        if (isPassiveStatusEvent(e)) {
            LOG.debug("onEvent: received valid registered passive status event: \n", e);
            PassiveStatusValue statusValue = getPassiveStatusValue(e);
            setStatus(statusValue.getKey(), statusValue.getStatus());
            LOG.debug("onEvent: passive status for: {} is: {}", statusValue.getKey(), m_statusTable.get(statusValue.getKey()));
        } 
        
        if (!isPassiveStatusEvent(e))
        {
            LOG.debug("onEvent: received Invalid registered passive status event: \n", e);
        }
    }

    private PassiveStatusValue getPassiveStatusValue(IEvent e) {
    		return new PassiveStatusValue(
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_NODE_LABEL),
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_IPADDR),
    				EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_NAME),
    				PollStatus.decodeWithTimestamp(EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_STATUS), EventUtils.getParm(e,EventConstants.PARM_PASSIVE_REASON_CODE), e.getTime())
    				);
    		
	}

	boolean isPassiveStatusEvent(IEvent e) {
		return PASSIVE_STATUS_UEI.equals(e.getUei()) &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_NODE_LABEL) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_IPADDR) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_NAME) != null &&
			EventUtils.getParm(e, EventConstants.PARM_PASSIVE_SERVICE_STATUS) != null;
	}

	/**
	 * <p>getEventManager</p>
	 *
	 * @return a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
	 */
	public EventIpcManager getEventManager() {
        return m_eventMgr;
    }

    /**
     * <p>setEventManager</p>
     *
     * @param eventMgr a {@link org.opennms.netmgt.events.api.EventIpcManager} object.
     */
    public void setEventManager(EventIpcManager eventMgr) {
        m_eventMgr = eventMgr;
    }
    
    /**
     * <p>getDbConnectoinFactory</p>
     *
     * @return a {@link javax.sql.DataSource} object.
     */
    public DataSource getDbConnectoinFactory() {
        return m_dataSource;
    }
    
    /**
     * <p>setDataSource</p>
     *
     * @param dataSource a {@link javax.sql.DataSource} object.
     */
    public void setDataSource(DataSource dataSource) {
        m_dataSource = dataSource;
    }
    
}
