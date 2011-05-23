package org.opennms.netmgt.invd;

import org.opennms.netmgt.model.events.EventListener;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.dao.InvdConfigDao;
import org.opennms.netmgt.capsd.InsufficientInformationException;
import org.opennms.netmgt.capsd.EventUtils;
import org.opennms.core.utils.ThreadCategory;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.transaction.TransactionStatus;

import java.util.ListIterator;
import java.net.InetAddress;

public class InvdEventListener implements EventListener {
    private final static String LOG4J_CATEGORY = "OpenNMS.Invd";

    private volatile TransactionTemplate m_transTemplate;

    //private Invd invd;

    private volatile InvdConfigDao m_inventoryConfigDao;

    private volatile InventoryScheduler m_inventoryScheduler;

    private volatile ScanableServices m_scanableServices;
    
    public String getName() {
        return LOG4J_CATEGORY;
    }

    private ThreadCategory log() {
        return ThreadCategory.getInstance(getClass());
    }

     /**
     * This method is invoked by the JMS topic session when a new event is
     * available for processing. Currently only text based messages are
     * processed by this callback. Each message is examined for its Universal
     * Event Identifier and the appropriate action is taking based on each
     * UEI.
     *
     * @param event
     *            The event message.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public void onEvent(final Event event) {

        m_transTemplate.execute(new TransactionCallback() {
            public Object doInTransaction(TransactionStatus status) {
                onEventInTransaction(event);
                return null;
            }

        });

    }

    private void onEventInTransaction(Event event) {
        // print out the uei
        //
        log().debug("received event, uei = " + event.getUei());

        try {
            if (event.getUei().equals(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI)) {
                //handleScheduledOutagesChanged(event);
            } else if (event.getUei().equals(EventConstants.CONFIGURE_SNMP_EVENT_UEI)) {
                //handleConfigureSNMP(event);
            } else if (event.getUei().equals(EventConstants.NODE_GAINED_SERVICE_EVENT_UEI)) {
                handleNodeGainedService(event);
            } else if (event.getUei().equals(EventConstants.PRIMARY_SNMP_INTERFACE_CHANGED_EVENT_UEI)) {
                //handlePrimarySnmpInterfaceChanged(event);
            } else if (event.getUei().equals(EventConstants.REINITIALIZE_PRIMARY_SNMP_INTERFACE_EVENT_UEI)) {
                //handleReinitializePrimarySnmpInterface(event);
            } else if (event.getUei().equals(EventConstants.INTERFACE_REPARENTED_EVENT_UEI)) {
                //handleInterfaceReparented(event);
            } else if (event.getUei().equals(EventConstants.NODE_DELETED_EVENT_UEI)) {
                //handleNodeDeleted(event);
            } else if (event.getUei().equals(EventConstants.DUP_NODE_DELETED_EVENT_UEI)) {
                //handleDupNodeDeleted(event);
            } else if (event.getUei().equals(EventConstants.INTERFACE_DELETED_EVENT_UEI)) {
                //handleInterfaceDeleted(event);
            } else if (event.getUei().equals(EventConstants.SERVICE_DELETED_EVENT_UEI)) {
                handleServiceDeleted(event);
            } else if (event.getUei().equals(EventConstants.THRESHOLDCONFIG_CHANGED_EVENT_UEI)) {
                //handleThresholdConfigurationChanged(event);
            }
        } catch (InsufficientInformationException e) {
            handleInsufficientInfo(e);
        }
    }

    protected void handleInsufficientInfo(InsufficientInformationException e) {
        log().info(e.getMessage());
    }

    /**
     * Process the event, construct a new CollectableService object
     * representing the node/interface combination, and schedule the interface
     * for collection. If any errors occur scheduling the interface no error
     * is returned.
     *
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     */
    private void handleNodeGainedService(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);
        // Schedule the interface
        //
        scheduleForCollection(event);
    }

    /**
     * This method is responsible for handling serviceDeleted events.
     *
     * @param event
     *            The event to process.
     * @throws InsufficientInformationException
     *
     */
    private void handleServiceDeleted(Event event)
            throws InsufficientInformationException {
        EventUtils.checkNodeId(event);
        EventUtils.checkInterface(event);
        EventUtils.checkService(event);

        ThreadCategory log = log();

        int nodeId = (int) event.getNodeid();
        String ipAddr = event.getInterface();
        String svcName = event.getService();

        // Iterate over the collectable services list and mark any entries
        // which match the nodeId/ipAddr of the deleted service
        // for deletion.
        synchronized (getScanableServices()) {
            ScanableService cSvc = null;
            ListIterator<ScanableService> liter = getScanableServices().getScanableServices().listIterator();
            while (liter.hasNext()) {
                cSvc = liter.next();

                // Only interested in entries with matching nodeId, IP address
                // and service
                InetAddress addr = (InetAddress) cSvc.getAddress();

                //WATCH the brackets; there userd to be an extra close bracket after the ipAddr comparision which borked this whole expression
                if (!(cSvc.getNodeId() == nodeId &&
                        addr.getHostName().equals(ipAddr) &&
                        cSvc.getServiceName().equals(svcName)))
                    continue;

                synchronized (cSvc) {
                    // Retrieve the CollectorUpdates object associated with
                    // this CollectableService if one exists.
                    //TODO enable updates
                    //CollectorUpdates updates = cSvc.getCollectorUpdates();

                    // Now set the update's deletion flag so the next
                    // time it is selected for execution by the scheduler
                    // the collection will be skipped and the service will not
                    // be rescheduled.
                    log().debug("Marking ScanableService for deletion because a service was deleted:  Service nodeid="+cSvc.getNodeId()+
                                ", deleted node:"+nodeId+
                                ", service address:"+addr.getHostName()+
                                ", deleted interface:"+ipAddr+
                                ", service servicename:"+cSvc.getServiceName()+
                                ", deleted service name:"+svcName+
                                ", event source "+event.getSource());
                    //TODO enable updates
                    //updates.markForDeletion();
                }

                // Now safe to remove the collectable service from
                // the collectable services list
                liter.remove();
            }
        }

        if (log.isDebugEnabled())
            log.debug("serviceDeletedHandler: processing of serviceDeleted event for "
                    + nodeId + "/" + ipAddr + "/" + svcName + " completed.");
    }

    private void scheduleForCollection(Event event) {
        // This moved to here from the scheduleInterface() for better behavior
        // during initialization

        getInventoryConfigDao().rebuildPackageIpListMap();

        getInventoryScheduler().scheduleInterface((int) event.getNodeid(), event.getInterface(),
                          event.getService(), false);
    }
    
    public void setTransactionTemplate(TransactionTemplate transTemplate) {
        m_transTemplate = transTemplate;
    }

//    public Invd getInvd() {
//        return invd;
//    }
//
//    public void setInvd(Invd invd) {
//        this.invd = invd;
//    }

    public InvdConfigDao getInventoryConfigDao() {
        return m_inventoryConfigDao;
    }

    public void setInventoryConfigDao(InvdConfigDao inventoryConfigDao) {
        this.m_inventoryConfigDao = inventoryConfigDao;
    }

    public InventoryScheduler getInventoryScheduler() {
        return m_inventoryScheduler;
    }

    public void setInventoryScheduler(InventoryScheduler inventoryScheduler) {
        this.m_inventoryScheduler = inventoryScheduler;
    }

    public ScanableServices getScanableServices() {
        return m_scanableServices;
    }

    public void setScanableServices(ScanableServices scanableServices) {
        this.m_scanableServices = scanableServices;
    }
}
