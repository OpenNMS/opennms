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
package org.opennms.netmgt.daemon;

import java.util.function.Consumer;

import org.apache.commons.lang.StringUtils;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventIpcManagerFactory;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.events.api.model.IParm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DaemonTools {

    public static final Logger LOG = LoggerFactory.getLogger(DaemonTools.class);

    public static void handleReloadEvent(IEvent e, String daemonName, Consumer<IEvent> handleConfigurationChanged) {
        final IParm daemonNameParm = e.getParm(EventConstants.PARM_DAEMON_NAME);
        if (daemonNameParm == null || daemonNameParm.getValue() == null) {
            LOG.warn("The {} parameter has no value. Ignoring.", EventConstants.PARM_DAEMON_NAME);
            return;
        }

        if (daemonName.equalsIgnoreCase(daemonNameParm.getValue().getContent())) {
            LOG.info("Reloading {}.", daemonName);

            EventBuilder ebldr = null;
            try {
                handleConfigurationChanged.accept(e);

                LOG.info("Reload successful.");

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_SUCCESSFUL_UEI, daemonName);

            } catch (Exception t) {
                LOG.error("Reload failed.", t);

                ebldr = new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_FAILED_UEI, daemonName);
                ebldr.addParam(EventConstants.PARM_REASON, StringUtils.abbreviate(t.getLocalizedMessage(), 128));
            }

            ebldr.addParam(EventConstants.PARM_DAEMON_NAME, daemonName);
            EventIpcManagerFactory.getIpcManager().sendNow(ebldr.getEvent());
        }
    }

}
