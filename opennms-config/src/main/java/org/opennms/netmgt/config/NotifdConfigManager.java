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
package org.opennms.netmgt.config;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Collection;

import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.notifd.AutoAcknowledge;
import org.opennms.netmgt.config.notifd.NotifdConfiguration;
import org.opennms.netmgt.config.notifications.Notification;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;
import org.opennms.netmgt.xml.event.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final Logger LOG = LoggerFactory.getLogger(NotifdConfigManager.class);

    /**
     * 
     */
    protected NotifdConfiguration configuration;

    /**
     * <p>parseXml</p>
     *
     * @param stream a {@link java.io.InputStream} object.
     * @throws java.io.IOException if any.
     */
    public synchronized void parseXml(InputStream stream) throws IOException {
        try (final Reader reader = new InputStreamReader(stream)) {
            configuration = JaxbUtils.unmarshal(NotifdConfiguration.class, reader);
        }
    }

    /**
     * <p>Getter for the field <code>configuration</code>.</p>
     *
     * @throws java.io.IOException if any.
     * @return a {@link org.opennms.netmgt.config.notifd.NotifdConfiguration} object.
     */
    public NotifdConfiguration getConfiguration() throws IOException {
        update();
    
        return configuration;
    }

    /**
     * <p>update</p>
     *
     * @throws java.io.IOException if any.
     */
    protected abstract void update() throws IOException;

    /**
     * <p>getNotificationStatus</p>
     *
     * @throws java.io.IOException if any.
     * @return a {@link java.lang.String} object.
     */
    public String getNotificationStatus() throws IOException {
        update();
        return configuration.getStatus();
    }

    /**
     * Turns the notifd service on
     *
     * @throws java.io.IOException if any.
     */
    public final void turnNotifdOn() throws IOException {
        configuration.setStatus("on");
        saveCurrent();
    }

    /**
     * Turns the notifd service off
     *
     * @throws java.io.IOException if any.
     */
    public final void turnNotifdOff() throws IOException {
        configuration.setStatus("off");
        saveCurrent();
    }

    /**
     * <p>getNotificationMatch</p>
     *
     * @throws java.io.IOException if any.
     * @return a boolean.
     */
    public boolean getNotificationMatch() throws IOException {
        update();
        return configuration.getMatchAll();
    }

    /**
     * <p>saveCurrent</p>
     *
     * @throws java.io.IOException if any.
     */
    public synchronized void saveCurrent() throws IOException {
        // marshall to a string first, then write the string to the file. This
        // way the original config
        // isn't lost if the xml from the marshall is hosed.
        final String xml = JaxbUtils.marshal(configuration);
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
     * @return a {@link java.lang.String} object.
     */
    public String getNextNotifIdSql() throws IOException {
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

        boolean parmmatch = false;
        if (notification.getVarbind() != null && notification.getVarbind().getVbname() != null) {
            final var notfValue = notification.getVarbind().getVbvalue();

            if (notfValue == null) {
                LOG.debug("BroadcastEventProcessor:matchNotificationParameters:  Null value for varbind, assuming true.");
                return true;
            }

            final var notfName = notification.getVarbind().getVbname();
            for (final Parm parm : event.getParmCollection()) {
                if (parmmatch) break; // we've already matched, skip processing all this

                final String parmName = parm.getParmName();
                if (!parmName.equals(notfName)) {
                    continue;
                }

                final Value parmValue = parm.getValue();
                final String parmContent;
                if (parmValue == null) {
                    continue;
                } else {
                    parmContent = parmValue.getContent();
                }

                // regular expression should start with a '~'
                if (notfValue.charAt(0) == '~') {
                   if (parmContent.matches(notfValue.substring(1))) {
                       parmmatch = true;
                   }
                } else {
                    if (parmContent.startsWith(notfValue)) {
                       parmmatch = true;
                    }
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
     */
    public String getNextUserNotifIdSql() throws IOException {
        return getConfiguration().getNextUserNotifId();
    }

    /**
     * <p>getAutoAcknowledges</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     */
    public Collection<AutoAcknowledge> getAutoAcknowledges() throws IOException {
        return getConfiguration().getAutoAcknowledges();
    }

    /**
     * <p>getOutageCalendarNames</p>
     *
     * @return a {@link java.util.Collection} object.
     * @throws java.io.IOException if any.
     */
    public Collection<String> getOutageCalendarNames() throws IOException {
        return getConfiguration().getOutageCalendars();
    }
}
