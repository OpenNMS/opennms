//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2002-2003 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Modifications:
//
// 2006 Aug 15: Be explicit about method visibility. - dj@opennms.org
// 2003 Jan 31: Cleaned up some unused imports.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//       
// For more information contact: 
//      OpenNMS Licensing       <license@opennms.org>
//      http://www.opennms.org/
//      http://www.opennms.com/
//

package org.opennms.netmgt.collectd;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Map;

import org.apache.log4j.Category;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.collectd.Collectd.SchedulingCompletedFlag;
import org.opennms.netmgt.config.DataCollectionConfigFactory;
import org.opennms.netmgt.dao.CollectorConfigDao;
import org.opennms.netmgt.dao.IpInterfaceDao;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.model.OnmsIpInterface;
import org.opennms.netmgt.model.RrdRepository;
import org.opennms.netmgt.scheduler.ReadyRunnable;
import org.opennms.netmgt.scheduler.Scheduler;
import org.opennms.netmgt.threshd.ThresholdingVisitor;
import org.opennms.netmgt.xml.event.Event;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * <P>
 * The CollectableService class ...
 * </P>
 * 
 * @author <A HREF="mailto:mike@opennms.org">Mike Davidson </A>
 * @author <A HREF="http://www.opennms.org/">OpenNMS </A>
 * 
 */
final class CollectableService implements ReadyRunnable {
    /**
     * Interface's parent node identifier
     */
    private volatile int m_nodeId;

    /**
     * Last known/current status
     */
    private volatile int m_status;

    /**
     * The last time the collector was scheduled for collection.
     */
    private volatile long m_lastScheduledCollectionTime;

    /**
     * The scheduler for collectd
     */
    private final Scheduler m_scheduler;

    /**
     * Service updates
     */
    private final CollectorUpdates m_updates;

    /**
     * The thresholdvisitor for this collectable service; called 
     */
    private final ThresholdingVisitor m_thresholdVisitor;
    /**
     * 
     */
    private static final boolean ABORT_COLLECTION = true;

	private final CollectionSpecification m_spec;

	private final SchedulingCompletedFlag m_schedulingCompletedFlag;

	private volatile CollectionAgent m_agent;

	private final PlatformTransactionManager m_transMgr;

    private final IpInterfaceDao m_ifaceDao;

    private final ServiceParameters m_params;
    
    private final RrdRepository m_repository;
    /**
     * Constructs a new instance of a CollectableService object.
     *
     * @param iface The interface on which to collect data
     * @param spec
     *            The package containing parms for this collectable service.
     * @param ifaceDao a {@link org.opennms.netmgt.dao.IpInterfaceDao} object.
     * @param scheduler a {@link org.opennms.netmgt.scheduler.Scheduler} object.
     * @param schedulingCompletedFlag a {@link org.opennms.netmgt.collectd.Collectd.SchedulingCompletedFlag} object.
     * @param transMgr a {@link org.springframework.transaction.PlatformTransactionManager} object.
     */
    protected CollectableService(OnmsIpInterface iface, IpInterfaceDao ifaceDao, CollectionSpecification spec, Scheduler scheduler, SchedulingCompletedFlag schedulingCompletedFlag, PlatformTransactionManager transMgr) {
        m_agent = DefaultCollectionAgent.create(iface.getId(), ifaceDao, transMgr);
        m_spec = spec;
        m_scheduler = scheduler;
        m_schedulingCompletedFlag = schedulingCompletedFlag;
        m_ifaceDao = ifaceDao;
        m_transMgr = transMgr;

        m_nodeId = iface.getNode().getId().intValue();
        m_status = ServiceCollector.COLLECTION_SUCCEEDED;

        m_updates = new CollectorUpdates();

        m_lastScheduledCollectionTime = 0L;
        
        m_spec.initialize(m_agent);
        
        Map<String, String> roProps=m_spec.getReadOnlyPropertyMap();
        m_params=new ServiceParameters(roProps);
        m_repository=m_spec.getRrdRepository(m_params.getCollectionName());
        
        m_thresholdVisitor =  ThresholdingVisitor.createThresholdingVisitor(m_nodeId, getHostAddress(), m_spec.getServiceName(), m_repository,  roProps, m_spec.getInterval());

    }
    
    /**
     * <p>getAddress</p>
     *
     * @return a {@link java.lang.Object} object.
     */
    public Object getAddress() {
    	return m_agent.getAddress();
    }
    
    /**
     * <p>getSpecification</p>
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectionSpecification} object.
     */
    public CollectionSpecification getSpecification() {
    	return m_spec;
    }

    /**
     * Returns node identifier
     *
     * @return a int.
     */
    public int getNodeId() {
        return m_nodeId;
    }

    /**
     * Returns the service name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getServiceName() {
        return m_spec.getServiceName();
    }

    /**
     * Returns the package name
     *
     * @return a {@link java.lang.String} object.
     */
    public String getPackageName() {
        return m_spec.getPackageName();
    }

    /**
     * Returns updates object
     *
     * @return a {@link org.opennms.netmgt.collectd.CollectorUpdates} object.
     */
    public CollectorUpdates getCollectorUpdates() {
        return m_updates;
    }

	/**
	 * Uses the existing package name to try and re-obtain the package from the collectd config factory.
	 * Should be called when the collect config has been reloaded.
	 *
	 * @param collectorConfigDao a {@link org.opennms.netmgt.dao.CollectorConfigDao} object.
	 */
	public void refreshPackage(CollectorConfigDao collectorConfigDao) {
		m_spec.refresh(collectorConfigDao);
	}

    /** {@inheritDoc} */
    @Override
    public String toString() {
        return "CollectableService for service "+m_nodeId+':'+getAddress()+':'+getServiceName();
    }


    /**
     * This method is used to evaluate the status of this interface and service
     * pair. If it is time to run the collection again then a value of true is
     * returned. If the interface is not ready then a value of false is
     * returned.
     *
     * @return a boolean.
     */
    public boolean isReady() {
        boolean ready = false;

        if (!isSchedulingComplete())
            return false;

        if (m_spec.getInterval() < 1) {
            ready = true;
        } else {
            ready = ((m_spec.getInterval() - (System.currentTimeMillis() - m_lastScheduledCollectionTime)) < 1);
        }

        return ready;
    }

	private boolean isSchedulingComplete() {
		return m_schedulingCompletedFlag.isSchedulingCompleted();
	}

    /**
     * Generate event and send it to eventd via the event proxy.
     * 
     * uei Universal event identifier of event to generate.
     */
    private void sendEvent(String uei) {
        Event event = new Event();
        event.setUei(uei);
        event.setNodeid((long) m_nodeId);
        event.setInterface(getHostAddress());
        event.setService(m_spec.getServiceName());
        event.setSource("OpenNMS.Collectd");
        try {
            event.setHost(InetAddress.getLocalHost().getHostAddress());
        } catch (UnknownHostException ex) {
            event.setHost("unresolved.host");
        }

        event.setTime(EventConstants.formatToString(new java.util.Date()));

        // Send the event
        //
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(event);

            if (log().isDebugEnabled())
                log().debug("sendEvent: Sent event " + uei + " for " + m_nodeId + "/" + getHostAddress() + "/" + getServiceName());

        } catch (Exception ex) {
            log().error("Failed to send the event " + uei + " for interface " + getHostAddress(), ex);
        }

    }

	private String getHostAddress() {
		return m_agent.getHostAddress();
	}

    /**
     * This is the main method of the class. An instance is normally enqueued on
     * the scheduler which checks its <code>isReady</code> method to determine
     * execution. If the instance is ready for execution then it is started with
     * it's own thread context to execute the query. The last step in the method
     * before it exits is to reschedule the interface.
     */
    public void run() {
        // Process any oustanding updates.
        //
        if (processUpdates() == ABORT_COLLECTION) {
            log().debug("run: Aborting because processUpdates returned ABORT_COLLECTION (probably marked for deletion) for "+this);
            return;
        }

        // Update last scheduled poll time
        m_lastScheduledCollectionTime = System.currentTimeMillis();

        // Check scheduled outages to see if any apply indicating
        // that the collection should be skipped
        if (!m_spec.scheduledOutage(m_agent)) {

        	int status = doCollection();
        	updateStatus(status);

        }
    	// Reschedule the service
    	//
        m_scheduler.schedule(m_spec.getInterval(), getReadyRunnable());
    }

	private void updateStatus(int status) {
		// Any change in status?
		//
		if (status != m_status) {
			// Generate data collection transition events
			if (log().isDebugEnabled())
				log().debug("run: change in collection status, generating event.");

			// Send the appropriate event
			//
			switch (status) {
			case ServiceCollector.COLLECTION_SUCCEEDED:
				sendEvent(EventConstants.DATA_COLLECTION_SUCCEEDED_EVENT_UEI);
				break;

			case ServiceCollector.COLLECTION_FAILED:
				sendEvent(EventConstants.DATA_COLLECTION_FAILED_EVENT_UEI);
				break;

			default:
				break;
			}
		}

		// Set the new status
		m_status = status;
	}

        private BasePersister createPersister(ServiceParameters params, RrdRepository repository) {
            if (Boolean.getBoolean("org.opennms.rrd.storeByGroup")) {
                return new GroupPersister(params, repository);
            } else {
                return new OneToOnePersister(params, repository);
            }
        }
        
	private int doCollection() {
		// Perform data collection
		//
		log().info("run: starting new collection for " + getHostAddress() + "/" + m_spec.getServiceName());
		CollectionSet result=null;
		try {
		    result = m_spec.collect(m_agent);
		    if(result!=null) {

                        Collectd.instrumentation().beginPersistingServiceData(m_nodeId, getHostAddress(), m_spec.getServiceName());
                        try {
                            BasePersister persister = createPersister(m_params, m_repository);
                            persister.setIgnorePersist(result.ignorePersist());
                            result.visit(persister);
                        } finally {
                            Collectd.instrumentation().endPersistingServiceData(m_nodeId, getHostAddress(), m_spec.getServiceName());
                        }
                        //Do the thresholding; this could be made more generic (listeners begin passed the collectionset ), but frankly, why bother?
                        //The first person who actually needs to configure that sort of thing on the fly can code it up
                        if(m_thresholdVisitor!=null) {
                            result.visit(m_thresholdVisitor);
                        }
                       
		        return result.getStatus();
		    }
		} catch (Throwable t) {
		    log().error("run: An undeclared throwable was caught during data collection for interface " + getHostAddress() +"/"+ m_spec.getServiceName(), t);
		}
		//Fall-through case - something went wrong, we failed
		return ServiceCollector.COLLECTION_FAILED;
	}

	/**
     * Process any outstanding updates.
     * 
     * @return true if update indicates that collection should be aborted (for
     *         example due to deletion flag being set), false otherwise.
     */
    private boolean processUpdates() {
        // All update processing takes place within synchronized block
        // to ensure that no updates are missed.
        //
        synchronized (this) {
            if (!m_updates.hasUpdates())
                return !ABORT_COLLECTION;

            // Update: deletion flag
            //
            if (m_updates.isDeletionFlagSet()) {
                // Deletion flag is set, simply return without polling
                // or rescheduling this collector.
                //
                if (log().isDebugEnabled())
                    log().debug("Collector for  " + getHostAddress() + " is marked for deletion...skipping collection, will not reschedule.");

                return ABORT_COLLECTION;
            }

            OnmsIpInterface newIface = m_updates.isReinitializationNeeded();
			// Update: reinitialization flag
            //
            if (newIface != null) {
                // Reinitialization flag is set, call initialize() to
                // reinit the collector for this interface
                //
                if (log().isDebugEnabled())
                    log().debug("ReinitializationFlag set for " + getHostAddress());

                try {
                    reinitialize(newIface);
                    if (log().isDebugEnabled())
                        log().debug("Completed reinitializing "+this.getServiceName()+" collector for " + getHostAddress() +"/"+ m_spec.getServiceName());
                } catch (RuntimeException rE) {
                    log().warn("Unable to initialize " + getHostAddress() + " for " + m_spec.getServiceName() + " collection, reason: " + rE.getMessage());
                } catch (Throwable t) {
                    log().error("Uncaught exception, failed to intialize interface " + getHostAddress() + " for " + m_spec.getServiceName() + " data collection", t);
                }
            }

            // Update: reparenting flag
            //
            if (m_updates.isReparentingFlagSet()) {
                if (log().isDebugEnabled())
                    log().debug("ReparentingFlag set for " + getHostAddress());

                // The interface has been reparented under a different node
                // (with
                // a different nodeId).
                //
                // If the new directory doesn't already exist simply need to
                // rename the old
                // directory:
                // /opt/OpenNMS/share/rrd/snmp/<oldNodeId>
                // to the new directory:
                // /opt/OpenNMS/share/rrd/snmp/<newNodeId>
                //
                // Otherwise must iterate over each of the files/dirs in the
                // <oldNodeId>
                // directory and move/rename them under the <newNodeId>
                // directory.

                // Get path to RRD repository
                //
                String rrdPath = DataCollectionConfigFactory.getInstance().getRrdPath();

                // Does the <newNodeId> directory already exist?
                File newNodeDir = new File(rrdPath + File.separator + m_updates.getReparentNewNodeId());
                if (!newNodeDir.isDirectory()) {
                    // New directory does not exist yet so simply rename the old
                    // directory to
                    // the new directory.
                    //

                    // <oldNodeId> directory
                    File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());

                    try {
                        // Rename <oldNodeId> dir to <newNodeId> dir.
                        if (log().isDebugEnabled())
                            log().debug("Attempting to rename " + oldNodeDir + " to " + newNodeDir);
                        oldNodeDir.renameTo(newNodeDir);
                        if (log().isDebugEnabled())
                            log().debug("Rename successful!!");
                    } catch (SecurityException se) {
                        log().error("Insufficient authority to rename RRD directory.", se);
                    } catch (Throwable t) {
                        log().error("Unexpected exception while attempting to rename RRD directory.", t);
                    }
                } else {
                    // New node directory already exists so we must move/rename
                    // each of the
                    // old node directory contents under the new node directory.
                    //

                    // Get list of files to be renamed/moved
                    File oldNodeDir = new File(rrdPath + File.separator + m_updates.getReparentOldNodeId());
                    String[] filesToMove = oldNodeDir.list();

                    if (filesToMove != null) {
                        // Iterate over the file list and rename/move each one
                        for (int i = 0; i < filesToMove.length; i++) {
                            File srcFile = new File(oldNodeDir.toString() + File.separator + filesToMove[i]);
                            File destFile = new File(newNodeDir.toString() + File.separator + filesToMove[i]);
                            try {
                                if (log().isDebugEnabled())
                                    log().debug("Attempting to move " + srcFile + " to " + destFile);
                                srcFile.renameTo(destFile);
                            } catch (SecurityException se) {
                                log().error("Insufficient authority to move RRD files.", se);
                                break;
                            } catch (Throwable t) {
                                log().warn("Unexpected exception while attempting to move " + srcFile + " to " + destFile, t);
                            }
                        }
                    }
                }

                // Convert new nodeId to integer value
                int newNodeId = -1;
                try {
                    newNodeId = Integer.parseInt(m_updates.getReparentNewNodeId());
                } catch (NumberFormatException nfE) {
                    log().warn("Unable to convert new nodeId value to an int while processing reparenting update: " + m_updates.getReparentNewNodeId());
                }

                // Set this collector's nodeId to the value of the interface's
                // new parent nodeid.
                m_nodeId = newNodeId;

                // We must now reinitialize the collector for this interface,
                // in order to update the NodeInfo object to reflect changes
                // to the interface's parent node among other things.
                //
                try {
                    if (log().isDebugEnabled())
                        log().debug("Reinitializing collector for " + getHostAddress() +"/"+ m_spec.getServiceName());
                    reinitialize(m_updates.getUpdatedInterface());
                    if (log().isDebugEnabled())
                        log().debug("Completed reinitializing collector for " + getHostAddress() +"/"+ m_spec.getServiceName());
                } catch (RuntimeException rE) {
                    log().warn("Unable to initialize " + getHostAddress() + " for " + m_spec.getServiceName() + " collection, reason: " + rE.getMessage());
                } catch (Throwable t) {
                    log().error("Uncaught exception, failed to initialize interface " + getHostAddress() + " for " + m_spec.getServiceName() + " data collection", t);
                }
            }

            // Updates have been applied. Reset CollectorUpdates object.
            // .
            m_updates.reset();
        } // end synchronized

        return !ABORT_COLLECTION;
    }
    
    Category log() {
    	return ThreadCategory.getInstance(getClass());
    }

    private void reinitialize(OnmsIpInterface newIface) {
        m_spec.release(m_agent);
        m_agent = DefaultCollectionAgent.create(newIface.getId(), m_ifaceDao,
                                                m_transMgr);
        m_spec.initialize(m_agent);
    }

    /**
     * <p>reinitializeThresholding</p>
     */
    public void reinitializeThresholding() {
        if(m_thresholdVisitor!=null) {
            log().debug("reinitializeThresholding on "+this);
            m_thresholdVisitor.initThresholdState();
        }
    }
    
    /**
     * <p>getReadyRunnable</p>
     *
     * @return a {@link org.opennms.netmgt.scheduler.ReadyRunnable} object.
     */
    public ReadyRunnable getReadyRunnable() {
	return this;
    }

}
