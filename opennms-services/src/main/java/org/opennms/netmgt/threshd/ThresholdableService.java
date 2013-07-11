/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.threshd;

import java.net.InetAddress;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.opennms.core.utils.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;
import org.opennms.netmgt.poller.InetNetworkInterface;
import org.opennms.netmgt.scheduler.LegacyScheduler;
import org.opennms.netmgt.scheduler.ReadyRunnable;

/**
 * <P>
 * The ThresholdableService class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 *
 * @deprecated Thresholding now done in CollectableService (in collectd) 
 */
final class ThresholdableService extends InetNetworkInterface implements ThresholdNetworkInterface, ReadyRunnable {
    private static final Logger LOG = LoggerFactory.getLogger(ThresholdableService.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 2477161545461824755L;

    /**
     * Interface's parent node identifier
     */
    private int m_nodeId;

    /**
     * The package information for this interface/service pair
     */
    private Package m_package;

    /**
     * The service information for this interface/service pair
     */
    private final Service m_service;

    /**
     * Last known/current status
     */
    private int m_status;

    /**
     * The last time a threshold check occurred
     */
    private long m_lastThresholdCheckTime;

    /**
     * The last time this service was scheduled for threshold checking.
     */
    private long m_lastScheduledThresholdCheckTime;

    /**
     * The proxy used to send events.
     */
    private final EventProxy m_proxy;

    /**
     * The scheduler for threshd
     */
    private final LegacyScheduler m_scheduler;

    /**
     * Service updates
     */
    private ThresholderUpdates m_updates;

    /**
     * 
     */
    private static final boolean ABORT_THRESHOLD_CHECK = true;

    private ServiceThresholder m_thresholder;

    /**
     * The key used to lookup the service properties that are passed to the
     * thresholder.
     */
    private final String m_svcPropKey;

    /**
     * The map of service parameters. These parameters are mapped by the
     * composite key <em>(package name, service name)</em>.
     */
    private static Map<String,Map<?,?>> SVC_PROP_MAP = new ConcurrentSkipListMap<String,Map<?,?>>();

    private Threshd m_threshd;

    /**
     * Constructs a new instance of a ThresholdableService object.
     * 
     * @param dbNodeId
     *            The database identifier key for the interfaces' node
     * @param address
     *            InetAddress of the interface to collect from
     * @param svcName
     *            Service name
     * @param pkg
     *            The package containing parms for this collectable service.
     * 
     */
    ThresholdableService(Threshd threshd, int dbNodeId, InetAddress address, String svcName, org.opennms.netmgt.config.threshd.Package pkg) {
        super(address);
        m_nodeId = dbNodeId;
        m_package = pkg;
        m_status = ServiceThresholder.THRESHOLDING_SUCCEEDED;

        m_threshd = threshd;
        m_proxy = EventIpcManagerFactory.getIpcManager();
        m_scheduler = threshd.getScheduler();
        m_thresholder = Threshd.getServiceThresholder(svcName);
        m_updates = new ThresholderUpdates();

        // Initialize last scheduled threshold check and last threshold
        // check times to current time.
        m_lastScheduledThresholdCheckTime = System.currentTimeMillis();
        m_lastThresholdCheckTime = System.currentTimeMillis();

        // find the service matching the name
        //
        Service svc = null;
        for (final Service s : m_package.getServiceCollection()) {
            if (s.getName().equalsIgnoreCase(svcName)) {
                svc = s;
                break;
            }
        }
        if (svc == null)
            throw new RuntimeException("Service name not part of package!");

        // save reference to the service
        m_service = svc;

        // add property list for this service/package combination if
        // it doesn't already exist in the service property map
        //
        m_svcPropKey = m_package.getName() + "." + m_service.getName();
        synchronized (SVC_PROP_MAP) {
            if (!SVC_PROP_MAP.containsKey(m_svcPropKey)) {
                Map<String,String> m = new ConcurrentSkipListMap<String,String>();
                for (final Parameter p : m_service.getParameterCollection()) {
                    m.put(p.getKey(), p.getValue());
                }

                // Add configured service 'interval' attribute as
                // a property as well. Needed by the ServiceThresholder
                // check() method in order to generate the
                // correct rrdtool fetch command.
                m.put("interval", Integer.toString((int) m_service.getInterval()));

                SVC_PROP_MAP.put(m_svcPropKey, m);
            }
        }
    }

    /**
     * Returns node identifier
     *
     * @return a int.
     */
        @Override
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * Set node identifier
     *
     * @param nodeId a int.
     */
    public void setNodeId(int nodeId) {
        m_nodeId = nodeId;
    }

    /**
     * Returns the service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_service.getName();
    }

    /**
     * Returns the service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return m_package.getName();
    }

    /**
     * Uses the existing package name to try and re-obtain the package from the threshd config factory.
     * Should be called when the threshd config has been reloaded.
     */
    public void refreshPackage() {
        Package refreshedPackage=m_threshd.getPackage(getPackageName());
        if(refreshedPackage!=null) {
            this.m_package=refreshedPackage;
        }
    }

    /**
     * Returns updates object
     *
     * @return a {@link org.opennms.netmgt.threshd.ThresholderUpdates} object.
     */
    public ThresholderUpdates getThresholderUpdates() {
        return m_updates;
    }

    /**
     * This method is used to evaluate the status of this interface and service
     * pair. If it is time to run the threshold check again then a value of true
     * is returned. If the interface is not ready then a value of false is
     * returned.
     *
     * @return a boolean.
     */
        @Override
    public boolean isReady() {
        boolean ready = false;

        if (!m_threshd.isSchedulingCompleted())
            return false;

        if (m_service.getInterval() < 1) {
            ready = true;
        } else {
            ready = ((m_service.getInterval() - (System.currentTimeMillis() - m_lastScheduledThresholdCheckTime)) < 1);
        }

        return ready;
    }

    /**
     * Returns the service's configured thresholding interval.
     *
     * @return a long.
     */
    public long getInterval() {
        return m_service.getInterval();
    }

    /**
     * Generate event and send it to eventd via the event proxy.
     * 
     * uei Universal event identifier of event to generate.
     */
    private void sendEvent(String uei) {

        EventBuilder bldr = new EventBuilder(uei, "OpenNMS.Threshd");
        bldr.setNodeid(m_nodeId);
        bldr.setInterface(m_address);
        bldr.setService("SNMP");
        bldr.setHost(InetAddressUtils.getLocalHostName());

        // Send the event
        //
        try {
            m_proxy.send(bldr.getEvent());
        } catch (final Exception ex) {
            LOG.error("Failed to send the event {} for interface {}", uei, getHostAddress(), ex);
        }

        LOG.debug("sendEvent: Sent event {} for {}/{}/{}", uei, m_nodeId, getHostAddress(), m_service.getName());
    }

	private String getHostAddress() {
		return InetAddressUtils.str(m_address);
	}

    /**
     * This is the main method of the class. An instance is normally enqueued on
     * the scheduler which checks its <code>isReady</code> method to determine
     * execution. If the instance is ready for execution then it is started with
     * it's own thread context to execute the query. The last step in the method
     * before it exits is to reschedule the interface.
     */
        @Override
    public void run() {
        // Process any oustanding updates.
        if (processUpdates() == ABORT_THRESHOLD_CHECK)
            return;

        // Update last scheduled poll time
        m_lastScheduledThresholdCheckTime = System.currentTimeMillis();

        // Check scheduled outages to see if any apply indicating
        // that threshold checking should be skipped
        if (scheduledOutage()) {
            // Outage applied...reschedule the service and return
            m_scheduler.schedule(this, m_service.getInterval());
            return;
        }

        // Perform threshold checking
        LOG.debug("run: starting new threshold check for {}", getHostAddress());

        int status = ServiceThresholder.THRESHOLDING_FAILED;
        final Map<?,?> propertiesMap = SVC_PROP_MAP.get(m_svcPropKey);
        try {
            status = m_thresholder.check(this, m_proxy, propertiesMap);
        } catch (final Throwable t) {
            LOG.error("run: An undeclared throwable was caught during SNMP thresholding for interface {}", getHostAddress(), t);
        }

        // Update last threshold check time
        m_lastThresholdCheckTime = System.currentTimeMillis();

        // Any change in status?
        if (status != m_status) {
            // Generate transition events
            LOG.debug("run: change in thresholding status, generating event.");

            // Send the appropriate event
            switch (status) {
            case ServiceThresholder.THRESHOLDING_SUCCEEDED:
                sendEvent(EventConstants.THRESHOLDING_SUCCEEDED_EVENT_UEI);
                break;

            case ServiceThresholder.THRESHOLDING_FAILED:
                sendEvent(EventConstants.THRESHOLDING_FAILED_EVENT_UEI);
                break;

            default:
                break;
            }
        }

        // Set the new status
        m_status = status;

        // Reschedule ourselves
        //
        m_scheduler.schedule(this, this.getInterval());

        return;
    }

    Map<?,?> getPropertyMap() {
        return Collections.unmodifiableMap((Map<?,?>) SVC_PROP_MAP.get(m_svcPropKey));
    }

    /**
     * Checks the package information for the thresholdable service and
     * determines if any of the calendar outages associated with the package
     * apply to the current time and the service's interface. If an outage
     * applies true is returned...otherwise false is returned.
     * 
     * @return false if no outage found (indicating thresholding may be
     *         performed) or true if applicable outage is found (indicating
     *         thresholding should be skipped).
     */
    private boolean scheduledOutage() {
        boolean outageFound = false;

        PollOutagesConfigFactory outageFactory = PollOutagesConfigFactory.getInstance();

        // Iterate over the outage names defined in the interface's package.
        // For each outage...if the outage contains a calendar entry which
        // applies to the current time and the outage applies to this
        // interface then break and return true. Otherwise process the
        // next outage.
        // 
        for (final String outageName : m_package.getOutageCalendarCollection()) {
            // Does the outage apply to the current time?
            if (outageFactory.isCurTimeInOutage(outageName)) {
                // Does the outage apply to this interface?
                if ((outageFactory.isNodeIdInOutage((long) m_nodeId, outageName)) || (outageFactory.isInterfaceInOutage(getHostAddress(), outageName))) {
                    LOG.debug("scheduledOutage: configured outage '{}' applies, interface {} will not be thresholded for {}", outageName, getHostAddress(), m_service);
                    outageFound = true;
                    break;
                }
            }
        }

        return outageFound;
    }

    /**
     * Process any outstanding updates.
     * 
     * @return true if update indicates that threshold check should be aborted
     *         (for example due to deletion flag being set), false otherwise.
     */
    private boolean processUpdates() {
        // All update processing takes place within synchronized block
        // to ensure that no updates are missed.
        //
        synchronized (this) {
            if (!m_updates.hasUpdates())
                return !ABORT_THRESHOLD_CHECK;

            // Update: deletion flag
            //
            if (m_updates.isDeletionFlagSet()) {
                // Deletion flag is set, simply return without polling
                // or rescheduling this collector.
                //
                LOG.debug("Collector for  {} is marked for deletion...skipping thresholding, will not reschedule.", getHostAddress());

                return ABORT_THRESHOLD_CHECK;
            }

            // Update: reinitialization flag
            //
            if (m_updates.isReinitializationFlagSet()) {
                // Reinitialization flag is set, call initialize() to
                // reinit the collector for this interface
                //
                LOG.debug("ReinitializationFlag set for {}", getHostAddress());

                try {
                    m_thresholder.release(this);
                    m_thresholder.initialize(this, this.getPropertyMap());
                    LOG.debug("Completed reinitializing SNMP collector for {}", getHostAddress());
                } catch (final RuntimeException e) {
                    LOG.warn("Unable to reschedule {} for {} thresholding.", getHostAddress(), m_service.getName(), e);
                } catch (final Throwable t) {
                    LOG.error("Uncaught exception, failed to reschedule interface {} for {} thresholding.", getHostAddress(), m_service.getName(), t);
                }
            }

            // Update: reparenting flag
            //
            if (m_updates.isReparentingFlagSet()) {
                LOG.debug("ReparentingFlag set for {}", getHostAddress());

                // Convert new nodeId to integer value
                int newNodeId = -1;
                try {
                    newNodeId = Integer.parseInt(m_updates.getReparentNewNodeId());
                } catch (final NumberFormatException nfE) {
                    LOG.warn("Unable to convert new nodeId value to an int while processing reparenting update: {}", m_updates.getReparentNewNodeId(), nfE);
                }

                // Set this collector's nodeId to the value of the interface's
                // new parent nodeid.
                m_nodeId = newNodeId;

                // We must now reinitialize the thresholder for this interface,
                // in order to update the NodeInfo object to reflect changes
                // to the interface's parent node among other things.
                //
                try {
                    LOG.debug("Reinitializing SNMP thresholder for {}", getHostAddress());
                    m_thresholder.release(this);
                    m_thresholder.initialize(this, this.getPropertyMap());
                    LOG.debug("Completed reinitializing SNMP thresholder for {}", getHostAddress());
                } catch (final RuntimeException rE) {
                    LOG.warn("Unable to initialize {} for {} thresholding.", getHostAddress(), m_service.getName(), rE);
                } catch (final Throwable t) {
                    LOG.error("Uncaught exception, failed to initialize interface {} for {} thresholding.", getHostAddress(), m_service.getName(), t);
                }
            }

            // Updates have been applied. Reset ThresholderUpdates object.
            // .
            m_updates.reset();
        } // end synchronized

        return !ABORT_THRESHOLD_CHECK;
    }
}
