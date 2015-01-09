/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.collectd.Package;
import org.opennms.netmgt.config.poller.outages.Outage;
import org.opennms.netmgt.config.poller.outages.Outages;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * <p>ScheduledOutagesRestService class.</p>
 * 
 * <ul>
 * <li><b>GET /sched-outages</b><br>to get a list of configured scheduled outages.</li>
 * <li><b>POST /sched-outages</b><br>to add a new outage (or update an existing one).</li>
 * <li><b>GET /sched-outages/{outageName}</b><br>to get the details of a specific outage.</li>
 * <li><b>DELETE /sched-outages/{outageName}</b><br>to delete a specific outage.</li>
 * <li><b>PUT /sched-outages/{outageName}/collectd/{package}</b><br>to add a specific outage to a collectd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/pollerd/{package}</b><br>to add a specific outage to a pollerd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/threshd/{package}</b><br>to add a specific outage to a threshd's package.</li>
 * <li><b>PUT /sched-outages/{outageName}/notifd</b><br>to add a specific outage to the notifications.</li>
 * <li><b>DELETE /sched-outages/{outageName}/collectd/{package}</b><br>to remove a specific outage from a collectd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/pollerd/{package}</b><br>to remove a specific outage from a pollerd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/threshd/{package}</b><br>to remove a specific outage from a threshd's package.</li>
 * <li><b>DELETE /sched-outages/{outageName}/notifd</b><br>to remove a specific outage from the notifications.</li>
 * </ul>
 * 
 * <p>Node and Interface status (the requests return true or false):</p>
 * <ul>
 * <li><b>GET /sched-outages/{outageName}/nodeInOutage/{nodeId}</b><br>to check if a node (with a specific nodeId) is currently on outage for a specific scheduled outage calendar.</li>
 * <li><b>GET /sched-outages/{outageName}/interfaceInOutage/{ipAddr}</b><br>to check if an interface (with a specific IP address) is currently on outage for a specific scheduled outage calendar.</li>
 * <li><b>GET /sched-outages/nodeInOutage/{nodeId}</b><br>to check if a node (with a specific nodeId) is currently in outage.</li>
 * <li><b>GET /sched-outages/interfaceInOutage/{ipAddr}</b><br>to check if an interface (with a specific IP address) is currently on outage.</li>
 * </ul>
 * 
 * @author Alejandro Galue <agalue@opennms.org>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("sched-outages")
public class ScheduledOutagesRestService extends OnmsRestService {
	
	private static final Logger LOG = LoggerFactory.getLogger(ScheduledOutagesRestService.class);


    private enum ConfigAction { ADD, REMOVE, REMOVE_FROM_ALL };
    
    @Context 
    UriInfo m_uriInfo;
    
    @Autowired
    protected PollOutagesConfigFactory m_pollOutagesConfigFactory;

    @Autowired
    protected CollectdConfigFactory m_collectdConfigFactory;

    @Autowired
    @Qualifier("eventProxy")
    protected EventProxy m_eventProxy;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Outages getOutages() {
        readLock();
        try {
            Outages outages = new Outages();
            outages.setOutage(m_pollOutagesConfigFactory.getOutages());
            return outages;
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON, MediaType.APPLICATION_ATOM_XML})
    public Outage getOutage(@PathParam("outageName") String outageName) throws IllegalArgumentException {
        readLock();
        try {
            Outage outage = m_pollOutagesConfigFactory.getOutage(outageName);
            if (outage == null) throw new IllegalArgumentException("Scheduled outage " + outageName + " does not exist.");
            return outage;
        } finally {
            readUnlock();
        }
    }

    @POST
    @Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveOrUpdateOutage(final Outage newOutage) {
        writeLock();
        try {
            if (newOutage == null) throw getException(Status.BAD_REQUEST, "Outage object can't be null");
            Outage oldOutage = m_pollOutagesConfigFactory.getOutage(newOutage.getName());
            if (oldOutage == null) {
                LOG.debug("saveOrUpdateOutage: adding outage {}", newOutage.getName());
                m_pollOutagesConfigFactory.addOutage(newOutage);
            } else {
                LOG.debug("saveOrUpdateOutage: updating outage {}", newOutage.getName());
                m_pollOutagesConfigFactory.replaceOutage(oldOutage, newOutage);
            }
            m_pollOutagesConfigFactory.saveCurrent();
            sendConfigChangedEvent();
            return Response.seeOther(getRedirectUri(m_uriInfo, newOutage.getName())).build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't save or update the scheduled outage " + newOutage.getName() + " because, " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}")
    public Response deleteOutage(@PathParam("outageName") String outageName) {
        writeLock();
        try {
            LOG.debug("deleteOutage: deleting outage {}", outageName);
            updateCollectd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updatePollerd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateThreshd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateNotifd(ConfigAction.REMOVE, outageName);
            m_pollOutagesConfigFactory.removeOutage(outageName);
            m_pollOutagesConfigFactory.saveCurrent();
            sendConfigChangedEvent();
            return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't delete the scheduled outage " + outageName + " because, " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/collectd/{packageName}")
    public Response addOutageToCollector(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateCollectd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't add scheduled outage " + outageName + " to collector package " + packageName + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/collectd/{packageName}")
    public Response removeOutageFromCollector(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateCollectd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't remove scheduled outage " + outageName + " from collector package " + packageName + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/pollerd/{packageName}")
    public Response addOutageToPoller(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updatePollerd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
            // return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't add scheduled outage " + outageName + " to poller package " + packageName  + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/pollerd/{packageName}")
    public Response removeOutageFromPoller(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updatePollerd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't remove scheduled outage " + outageName + " from poller package " + packageName + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/threshd/{packageName}")
    public Response addOutageToThresholder(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateThreshd(ConfigAction.ADD, outageName, packageName);
            sendConfigChangedEvent();
            return Response.seeOther(getRedirectUri(m_uriInfo)).build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't add scheduled outage " + outageName + " to threshold package " + packageName + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/threshd/{packageName}")
    public Response removeOutageFromThresholder(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        writeLock();
        try {
            updateThreshd(ConfigAction.REMOVE, outageName, packageName);
            sendConfigChangedEvent();
            return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't remove scheduled outage " + outageName + " from threshold package " + packageName + ", because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @PUT
    @Path("{outageName}/notifd")
    public Response addOutageToNotifications(@PathParam("outageName") String outageName) {
        writeLock();
        try {
            updateNotifd(ConfigAction.ADD, outageName);
            sendConfigChangedEvent();
            return Response.seeOther(getRedirectUri(m_uriInfo, outageName)).build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't add scheduled outage " + outageName + " to notifications because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @DELETE
    @Path("{outageName}/notifd")
    public Response removeOutageFromNotifications(@PathParam("outageName") String outageName) {
        writeLock();
        try {
            updateNotifd(ConfigAction.REMOVE, outageName);
            sendConfigChangedEvent();
            return Response.ok().build();
        } catch (Exception e) {
            throw getException(Status.BAD_REQUEST, "Can't remove scheduled outage " + outageName + " from notifications because: " + e.getMessage());
        } finally {
            writeUnlock();
        }
    }

    @GET
    @Path("{outageName}/nodeInOutage/{nodeId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isNodeInOutage(@PathParam("outageName") String outageName, @PathParam("nodeId") Integer nodeId) {
        readLock();
        try {
            Outage outage = getOutage(outageName);
            Boolean inOutage = m_pollOutagesConfigFactory.isNodeIdInOutage(nodeId, outage) && m_pollOutagesConfigFactory.isCurTimeInOutage(outage);
            return inOutage.toString();
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("nodeInOutage/{nodeId}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isNodeInOutage(@PathParam("nodeId") Integer nodeId) {
        readLock();
        try {
            for (Outage outage : m_pollOutagesConfigFactory.getOutages()) {
                if (m_pollOutagesConfigFactory.isNodeIdInOutage(nodeId, outage) && m_pollOutagesConfigFactory.isCurTimeInOutage(outage)) {
                    return Boolean.TRUE.toString();
                }
            }
            return Boolean.FALSE.toString();
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("{outageName}/interfaceInOutage/{ipAddr}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isInterfaceInOutage(@PathParam("outageName") String outageName, @PathParam("ipAddr") String ipAddr) {
        readLock();
        try {
            validateAddress(ipAddr);
            Outage outage = getOutage(outageName);
            Boolean inOutage = m_pollOutagesConfigFactory.isInterfaceInOutage(ipAddr, outage) && m_pollOutagesConfigFactory.isCurTimeInOutage(outage);
            return inOutage.toString();
        } finally {
            readUnlock();
        }
    }

    @GET
    @Path("interfaceInOutage/{ipAddr}")
    @Produces(MediaType.TEXT_PLAIN)
    public String isInterfaceInOutage(@PathParam("ipAddr") String ipAddr) {
        readLock();
        try {
            for (Outage outage : m_pollOutagesConfigFactory.getOutages()) {
                if (m_pollOutagesConfigFactory.isInterfaceInOutage(ipAddr, outage) && m_pollOutagesConfigFactory.isCurTimeInOutage(outage)) {
                    return Boolean.TRUE.toString();
                }
            }
            return Boolean.FALSE.toString();
        } finally {
            readUnlock();
        }
    }

    private void validateAddress(String ipAddress) {
        boolean valid = false;
        try {
            valid = InetAddressUtils.addr(ipAddress) != null;
        } catch (Exception e) {
            valid = false;
        }
        if (!valid) {
            throw getException(Status.BAD_REQUEST, "Malformed IP Address " + ipAddress);
        }
    }

    private void updateCollectd(ConfigAction action, String outageName, String packageName) throws Exception {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            Package pkg = getCollectdPackage(packageName);
            if (!pkg.getOutageCalendars().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            Package pkg = getCollectdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (Package pkg : m_collectdConfigFactory.getCollectdConfig().getPackages()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        m_collectdConfigFactory.saveCurrent();
    }

    private Package getCollectdPackage(String packageName) throws IllegalArgumentException {
        Package pkg = m_collectdConfigFactory.getPackage(packageName);
        if (pkg == null) throw new IllegalArgumentException("Collectd package " + packageName + " does not exist.");
        return pkg;
    }

    private void updatePollerd(ConfigAction action, String outageName, String packageName) throws Exception {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
            if (!pkg.getOutageCalendars().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (org.opennms.netmgt.config.poller.Package pkg : PollerConfigFactory.getInstance().getConfiguration().getPackages()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        PollerConfigFactory.getInstance().save();
    }

    private org.opennms.netmgt.config.poller.Package getPollerdPackage(String packageName) throws IllegalArgumentException {
        org.opennms.netmgt.config.poller.Package pkg = PollerConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) throw new IllegalArgumentException("Poller package " + packageName + " does not exist.");
        return pkg;
    }

    private void updateThreshd(ConfigAction action, String outageName, String packageName) throws Exception {
        getOutage(outageName); // Validate if outageName exists.
        if (action.equals(ConfigAction.ADD)) {
            org.opennms.netmgt.config.threshd.Package pkg = getThreshdPackage(packageName);
            if (!pkg.getOutageCalendarCollection().contains(outageName))
                pkg.addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE)) {
            org.opennms.netmgt.config.threshd.Package pkg = getThreshdPackage(packageName);
            pkg.removeOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            for (org.opennms.netmgt.config.threshd.Package pkg : ThreshdConfigFactory.getInstance().getConfiguration().getPackage()) {
                pkg.removeOutageCalendar(outageName);
            }
        }
        ThreshdConfigFactory.getInstance().saveCurrent();
    }

    private org.opennms.netmgt.config.threshd.Package getThreshdPackage(String packageName) throws IllegalArgumentException {
        org.opennms.netmgt.config.threshd.Package pkg = ThreshdConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) throw new IllegalArgumentException("Threshold package " + packageName + " does not exist.");
        return pkg;
    }

    private void updateNotifd(ConfigAction action, String outageName) throws Exception {
        getOutage(outageName); // Validate if outageName exists.
        NotifdConfigFactory factory = NotifdConfigFactory.getInstance();
        if (action.equals(ConfigAction.ADD)) {
            factory.getConfiguration().addOutageCalendar(outageName);
        }
        if (action.equals(ConfigAction.REMOVE) || action.equals(ConfigAction.REMOVE_FROM_ALL)) {
            factory.getConfiguration().removeOutageCalendar(outageName);
        }
        factory.saveCurrent();
    }

    private void sendConfigChangedEvent() {
        EventBuilder builder = new EventBuilder(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI, "Web UI");
        try {
            m_eventProxy.send(builder.getEvent());
        } catch (Throwable e) {
            throw getException(Status.BAD_REQUEST, "Could not send event " + builder.getEvent().getUei() + " because, " + e.getMessage());
        }
    }

}
