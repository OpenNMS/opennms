/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collection;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.Marshaller;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.notifd.AutoAcknowledge;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;

/**
 * <p>Abstract NotifdConfigManager class.</p>
 *
 * @author david
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 * @version $Id: $
 */
public abstract class NotifdConfigManager {

    /**
     * 
     */
    protected NotifdConfiguration configuration;

    /**
     * <p>parseXml</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public synchronized void parseXml(InputStream stream) throws MarshalException, ValidationException, IOException {
        configuration = CastorUtils.unmarshal(NotifdConfiguration.class, stream);
    }

    /**
     * <p>Getter for the field <code>configuration</code>.</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @return a {@link org.opennms.netmgt.config.notifd.NotifdConfiguration} object.
     */
    public NotifdConfiguration getConfiguration() throws IOException, MarshalException, ValidationException {
        update();
    
        return configuration;
    }

    /**
     * <p>update</p>
     *
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws java.io.IOException if any.
     */
    protected abstract void update() throws IOException, MarshalException, ValidationException;

    /**
     * <p>getNotificationStatus</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @return a {@link java.lang.String} object.
     */
    public String getNotificationStatus() throws IOException, MarshalException, ValidationException {
        update();
        return configuration.getStatus();
    }

    /**
     * Turns the notifd service on
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public final void turnNotifdOn() throws MarshalException, ValidationException, IOException {
        configuration.setStatus("on");
        saveCurrent();
    }

    /**
     * Turns the notifd service off
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public final void turnNotifdOff() throws MarshalException, ValidationException, IOException {
        configuration.setStatus("off");
        saveCurrent();
    }

    /**
     * <p>getNotificationMatch</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @return a boolean.
     */
    public boolean getNotificationMatch() throws IOException, MarshalException, ValidationException {
        update();
        return configuration.getMatchAll();
    }

    /**
     * <p>saveCurrent</p>
     *
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
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
     * <p>saveXml</p>
     *
     * @param xml a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     */
    protected abstract void saveXml(String xml) throws IOException;

    /**
     * <p>getNextNotifIdSql</p>
     *
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @return a {@link java.lang.String} object.
     */
    public String getNextNotifIdSql() throws IOException, MarshalException, ValidationException {
        return getConfiguration().getNextNotifId();
    }
    
    // TODO This change only works for one parameter, need to expand it to many.
    /**
     * <p>matchNotificationParameters</p>
     *
     * @param event a {@link org.opennms.netmgt.xml.event.Event} object.
     * @param notification a {@link org.opennms.netmgt.config.notifications.Notification} object.
     * @return a boolean.
     */
    public boolean matchNotificationParameters(Event event, Notification notification) {
        ThreadCategory log = ThreadCategory.getInstance(getClass());

        boolean parmmatch = false;
        if (notification.getVarbind() != null && notification.getVarbind().getVbname() != null) {
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

            for (final Parm parm : event.getParmCollection()) {
                final String parmName = parm.getParmName();
                final Value parmValue = parm.getValue();
                final String parmContent;
                if (parmValue == null) {
                    continue;
                } else {
                    parmContent = parmValue.getContent();
                }

                if (parmName.equals(notfName) && parmContent.startsWith(notfValue)) {
                    parmmatch = true;
                }
            }
        } else if (notification.getVarbind() == null || notification.getVarbind().getVbname() == null) {
            parmmatch = true;
        }

        return parmmatch;
    }

    /**
     * <p>getNextUserNotifIdSql</p>
     *
     * @return a {@link java.lang.String} object.
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public String getNextUserNotifIdSql() throws IOException, MarshalException, ValidationException {
        return getConfiguration().getNextUserNotifId();
    }

    /**
     * <p>getAutoAcknowledges</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public Collection<AutoAcknowledge> getAutoAcknowledges() throws MarshalException, ValidationException, IOException {
        return getConfiguration().getAutoAcknowledgeCollection();
    }

    /**
     * <p>getOutageCalendarNames</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public Collection<String> getOutageCalendarNames() throws MarshalException, ValidationException, IOException {
        return getConfiguration().getOutageCalendarCollection();
    }
}
