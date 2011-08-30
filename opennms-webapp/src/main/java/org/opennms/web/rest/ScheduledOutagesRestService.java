/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.web.rest;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.CollectdConfigFactory;
import org.opennms.netmgt.config.CollectdPackage;
import org.opennms.netmgt.config.NotifdConfigFactory;
import org.opennms.netmgt.config.PollOutagesConfigFactory;
import org.opennms.netmgt.config.PollerConfigFactory;
import org.opennms.netmgt.config.ThreshdConfigFactory;
import org.opennms.netmgt.config.poller.Outage;
import org.opennms.netmgt.config.poller.Outages;
import org.opennms.netmgt.model.events.EventBuilder;
import org.opennms.netmgt.model.events.EventProxy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.sun.jersey.spi.resource.PerRequest;

/**
 * <p>ScheduledOutagesRestService class.</p>
 *
 * @author Alejandro Galue <agalue@opennms.org>
 */
@Component
@PerRequest
@Scope("prototype")
@Path("sched-outages")
public class ScheduledOutagesRestService extends OnmsRestService {

    private enum ConfigAction { ADD, REMOVE, REMOVE_FROM_ALL };

    @Autowired
    protected PollOutagesConfigFactory m_configFactory;

    @Autowired
    protected EventProxy m_eventProxy;

    @GET
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Outages getOutages() {
        Outages outages = new Outages();
        outages.setOutage(m_configFactory.getOutages());
        return outages;
    }

    @GET
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Outage getOutage(@PathParam("outageName") String outageName) {
        Outage outage = m_configFactory.getOutage(outageName);
        if (outage == null) {
            throwException(Status.BAD_REQUEST, "Scheduled outage " + outageName + " does not exist.");
        }
        return outage;
    }

    @POST
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response saveOrUpdateOutage(Outage newOutage) {
        if (newOutage == null) {
            throwException(Status.BAD_REQUEST, "Outage object can't be null");
        }
        try {
            Outage oldOutage = m_configFactory.getOutage(newOutage.getName());
            if (oldOutage == null) {
                log().debug("saveOrUpdateOutage: adding outage " + newOutage.getName());
                m_configFactory.addOutage(newOutage);
            } else {
                log().debug("saveOrUpdateOutage: updating outage " + newOutage.getName());
                m_configFactory.replaceOutage(oldOutage, newOutage);
            }
            m_configFactory.saveCurrent();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't save or update the scheduled outage " + newOutage.getName() + " because, " + e.getMessage());
        }
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}")
    @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
    public Response deleteOutage(@PathParam("outageName") String outageName) {
        try {
            log().debug("deleteOutage: deleting outage " + outageName);
            m_configFactory.removeOutage(outageName);
            m_configFactory.saveCurrent();
            updateCollectd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updatePollerd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateThreshd(ConfigAction.REMOVE_FROM_ALL, outageName, null);
            updateNotifd(ConfigAction.REMOVE, outageName);
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't delete the scheduled outage " + outageName + " because, " + e.getMessage());
        }
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @PUT
    @Path("{outageName}/collectd/{packageName}")
    public Response addOutageToCollector(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updateCollectd(ConfigAction.ADD, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}/collectd/{packageName}")
    public Response removeOutageFromCollector(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updateCollectd(ConfigAction.REMOVE, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @PUT
    @Path("{outageName}/pollerd/{packageName}")
    public Response addOutageToPoller(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updatePollerd(ConfigAction.ADD, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}/pollerd/{packageName}")
    public Response removeOutageFromPoller(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updatePollerd(ConfigAction.REMOVE, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @PUT
    @Path("{outageName}/threshd/{packageName}")
    public Response addOutageToThresholder(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updateThreshd(ConfigAction.ADD, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}/threshd/{packageName}")
    public Response removeOutageFromThresholder(@PathParam("outageName") String outageName, @PathParam("packageName") String packageName) {
        updateThreshd(ConfigAction.REMOVE, outageName, packageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @PUT
    @Path("{outageName}/notifd")
    public Response addOutageToNotifications(@PathParam("outageName") String outageName) {
        updateNotifd(ConfigAction.ADD, outageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    @DELETE
    @Path("{outageName}/notifd")
    public Response removeOutageFromNotifications(@PathParam("outageName") String outageName) {
        updateNotifd(ConfigAction.REMOVE, outageName);
        sendConfigChangedEvent();
        return Response.ok().build();
    }

    private void updateCollectd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        try {
            if (action.equals(ConfigAction.ADD)) {
                CollectdPackage pkg = getCollectdPackage(packageName);
                if (!pkg.getPackage().getOutageCalendarCollection().contains(outageName))
                    pkg.getPackage().addOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE)) {
                CollectdPackage pkg = getCollectdPackage(packageName);
                pkg.getPackage().removeOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
                for (CollectdPackage pkg : CollectdConfigFactory.getInstance().getCollectdConfig().getPackages()) {
                    pkg.getPackage().removeOutageCalendar(outageName);
                }
            }
            CollectdConfigFactory.getInstance().saveCurrent();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't add/remove scheduled outage " + outageName + " for collector package " + packageName);
        }
    }

    private CollectdPackage getCollectdPackage(String packageName) {
        CollectdPackage pkg = CollectdConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) {
            throwException(Status.BAD_REQUEST, "Collectd package " + packageName + " does not exist.");
        }
        return pkg;
    }

    private void updatePollerd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        try {
            if (action.equals(ConfigAction.ADD)) {
                org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
                if (!pkg.getOutageCalendarCollection().contains(outageName))
                    pkg.addOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE)) {
                org.opennms.netmgt.config.poller.Package pkg = getPollerdPackage(packageName);
                pkg.removeOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE_FROM_ALL)) {
                for (org.opennms.netmgt.config.poller.Package pkg : PollerConfigFactory.getInstance().getConfiguration().getPackage()) {
                    pkg.removeOutageCalendar(outageName);
                }
            }
            PollerConfigFactory.getInstance().save();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't add/remove scheduled outage " + outageName + " for poller package " + packageName);
        }
    }

    private org.opennms.netmgt.config.poller.Package getPollerdPackage(String packageName) {
        org.opennms.netmgt.config.poller.Package pkg = PollerConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) {
            throwException(Status.BAD_REQUEST, "Poller package " + packageName + " does not exist.");
        }
        return pkg;
    }

    private void updateThreshd(ConfigAction action, String outageName, String packageName) {
        getOutage(outageName); // Validate if outageName exists.
        try {
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
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't add/remove scheduled outage " + outageName + " for threshold package " + packageName);
        }
    }

    private org.opennms.netmgt.config.threshd.Package getThreshdPackage(String packageName) {
        org.opennms.netmgt.config.threshd.Package pkg = ThreshdConfigFactory.getInstance().getPackage(packageName);
        if (pkg == null) {
            throwException(Status.BAD_REQUEST, "Threshold package " + packageName + " does not exist.");
        }
        return pkg;
    }

    private void updateNotifd(ConfigAction action, String outageName) {
        getOutage(outageName); // Validate if outageName exists.
        try {
            NotifdConfigFactory factory = NotifdConfigFactory.getInstance();
            if (action.equals(ConfigAction.ADD)) {
                factory.getConfiguration().addOutageCalendar(outageName);
            }
            if (action.equals(ConfigAction.REMOVE)) {
                factory.getConfiguration().removeOutageCalendar(outageName);
            }
            factory.saveCurrent();
        } catch (Exception e) {
            throwException(Status.BAD_REQUEST, "Can't add/remove scheduled outage " + outageName + " for notifications");
        }
    }

    private void sendConfigChangedEvent() {
        EventBuilder builder = new EventBuilder(EventConstants.SCHEDOUTAGES_CHANGED_EVENT_UEI, "Web UI");
        try {
            m_eventProxy.send(builder.getEvent());
        } catch (Throwable e) {
            throwException(Status.BAD_REQUEST, "Could not send event " + builder.getEvent().getUei() + " because, " + e.getMessage());
        }
    }

}
