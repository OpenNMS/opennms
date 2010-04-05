//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.util.Collection;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.EventConstants;
import org.opennms.netmgt.config.notifd.AutoAcknowledge;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.dao.castor.CastorUtils;
import org.opennms.netmgt.eventd.EventIpcManagerFactory;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Parms;
import org.opennms.netmgt.xml.event.Value;

/**
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class NotifdConfigManager {

    /**
     * 
     */
    protected NotifdConfiguration configuration;

    /**
     * @param reader
     * @throws MarshalException
     * @throws ValidationException
     * @throws IOException
     */
    @Deprecated
    public synchronized void parseXml(Reader reader) throws MarshalException, ValidationException, IOException {
        configuration = CastorUtils.unmarshal(NotifdConfiguration.class, reader);
    }

    public synchronized void parseXml(InputStream stream) throws MarshalException, ValidationException, IOException {
        configuration = CastorUtils.unmarshal(NotifdConfiguration.class, stream);
    }

    /**
     * @return
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public NotifdConfiguration getConfiguration() throws IOException, MarshalException, ValidationException {
        update();
    
        return configuration;
    }

    /**
     * @throws ValidationException
     * @throws MarshalException
     * @throws IOException
     * 
     */
    protected abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * @return
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public String getNotificationStatus() throws IOException, MarshalException, ValidationException {
        update();
        return configuration.getStatus();
    }

    /**
     * Turns the notifd service on
     */
    public void turnNotifdOn() throws MarshalException, ValidationException, IOException {
        sendEvent("uei.opennms.org/internal/notificationsTurnedOn");
        configuration.setStatus("on");

        saveCurrent();
    }

    /**
     * Turns the notifd service off
     */
    public void turnNotifdOff() throws MarshalException, ValidationException, IOException {
        sendEvent("uei.opennms.org/internal/notificationsTurnedOff");
        configuration.setStatus("off");

        saveCurrent();
    }

    /**
     * @return
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public boolean getNotificationMatch() throws IOException, MarshalException, ValidationException {
        update();
        return configuration.getMatchAll();
    }

    /**
     * 
     */
    public synchronized void saveCurrent() throws MarshalException, ValidationException, IOException {
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        StringWriter stringWriter = new StringWriter();
        Marshaller.marshal(configuration, stringWriter);
        String xml = stringWriter.toString();
        saveXml(xml);
        update();
    }

    /**
     * @param xml
     * @throws IOException
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * 
     */
    protected void sendEvent(String uei) {
        Event event = new Event();
        event.setUei(uei);
        event.setSource("NotifdConfigFactory");
    
        event.setTime(EventConstants.formatToString(new java.util.Date()));
    
        try {
            EventIpcManagerFactory.getIpcManager().sendNow(event);
        } catch (Throwable t) {
        }
    }

    /**
     * @return
     * @throws IOException
     * @throws MarshalException
     * @throws ValidationException
     */
    public String getNextNotifIdSql() throws IOException, MarshalException, ValidationException {
        return getConfiguration().getNextNotifId();
    }
    
    // TODO This change only works for one parameter, need to expand it to many.
    public boolean matchNotificationParameters(Event event, Notification notification) {
        Category log = ThreadCategory.getInstance(getClass());

        boolean parmmatch = false;
        Parms parms = event.getParms();
        if (parms != null && notification.getVarbind() != null && notification.getVarbind().getVbname() != null) {
            String parmName = null;
            Value parmValue = null;
            String parmContent = null;
            String notfValue = null;
            String notfName = notification.getVarbind().getVbname();

            if (notification.getVarbind().getVbvalue() != null) {
                notfValue = notification.getVarbind().getVbvalue();
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("BroadcastEventProcessor:matchNotificationParameters:  Null value for varbind, assuming true.");
                }
                parmmatch = true;
            }

            for (Parm parm : parms.getParmCollection()) {
                parmName = parm.getParmName();
                parmValue = parm.getValue();
                if (parmValue == null)
                    continue;
                else
                    parmContent = parmValue.getContent();

                if (parmName.equals(notfName) && parmContent.startsWith(notfValue)) {
                    parmmatch = true;
                }
            }
        } else if (notification.getVarbind() == null || notification.getVarbind().getVbname() == null) {
            parmmatch = true;
        }

        return parmmatch;
    }

    public String getNextUserNotifIdSql() throws IOException, MarshalException, ValidationException {
        return getConfiguration().getNextUserNotifId();
    }

    public Collection<AutoAcknowledge> getAutoAcknowledges() throws MarshalException, ValidationException, IOException {
        return getConfiguration().getAutoAcknowledgeCollection();
    }

    public Collection<String> getOutageCalendarNames() throws MarshalException, ValidationException, IOException {
        return getConfiguration().getOutageCalendarCollection();
    }
}
