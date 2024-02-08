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
package org.opennms.netmgt.notifd.asterisk;

import java.util.List;

import org.opennms.core.spring.BeanUtils;
import org.opennms.netmgt.asterisk.agi.scripts.BaseOnmsAgiScript;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginator;
import org.opennms.netmgt.asterisk.utils.AsteriskOriginatorException;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>AsteriskOriginateNotificationStrategy class.</p>
 *
 * @author Jeff Gehlbach <A HREF="mailto:jeffg@opennms.org">jeffg</A>
 * @version $Id: $
 */
public class AsteriskOriginateNotificationStrategy implements NotificationStrategy {
    private static final Logger LOG = LoggerFactory.getLogger(AsteriskOriginateNotificationStrategy.class);

    /**
     * <p>Constructor for AsteriskOriginateNotificationStrategy.</p>
     */
    public AsteriskOriginateNotificationStrategy() {
    }

    /* (non-Javadoc)
     * @see org.opennms.netmgt.notifd.NotificationStrategy#send(java.util.List)
     */
    /** {@inheritDoc} */
    @Override
    public final int send(final List<Argument> arguments) {
        LOG.debug("In the {} class", getClass());

        try {
            buildOriginator(arguments).originateCall();
        } catch (final AsteriskOriginatorException aoe) {
            LOG.error("Error originating call for notification.", aoe);
            return 1;
        }
        return 0;
    }

    private AsteriskOriginator buildOriginator(final List<Argument> arguments) throws AsteriskOriginatorException {
        final AsteriskOriginator ao = new AsteriskOriginator();
        for (final Argument arg : arguments) {
            final String argSwitch = arg.getSwitch();
            final String argValue = arg.getValue();

            if (NotificationManager.PARAM_WORK_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_WORK_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_HOME_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_HOME_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_MOBILE_PHONE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_MOBILE_PHONE => {}", argValue);
                ao.setLegAExtension(argValue);
            } else if (NotificationManager.PARAM_SUBJECT.equals(argSwitch)) {
                LOG.debug("Found: PARAM_SUBJECT => {}", argValue);
                ao.setSubject(argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_SUBJECT, argValue);
            } else if (NotificationManager.PARAM_TEXT_MSG.equals(argSwitch)) {
                LOG.debug("Found: PARAM_TEXT_MSG => {}", argValue);
                ao.setMessageText(argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NOTIFY_BODY, argValue);
            } else if (NotificationManager.PARAM_TUI_PIN.equals(argSwitch)) {
                LOG.debug("Found: PARAM_TUI_PIN => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USER_PIN, argValue);
            } else if (NotificationManager.PARAM_DESTINATION.equals(argSwitch)) {
                LOG.debug("Found: PARAM_DESTINATION => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_USERNAME, argValue);
            } else if (NotificationManager.PARAM_NODE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_NODE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODEID, argValue);
                try {
                    final NodeDao nodeDao = BeanUtils.getBean("notifdContext", "nodeDao", NodeDao.class);
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, nodeDao.get(argValue).getLabel());
                } catch (final Throwable t) {
                    ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_NODELABEL, null);
                }
            } else if (NotificationManager.PARAM_INTERFACE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_INTERFACE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_INTERFACE, argValue);
            } else if (NotificationManager.PARAM_SERVICE.equals(argSwitch)) {
                LOG.debug("Found: PARAM_SERVICE => {}", argValue);
                ao.setChannelVariable(BaseOnmsAgiScript.VAR_OPENNMS_SERVICE, argValue);
            } else {
                LOG.debug("Unconsumed arg: {} => {}", String.valueOf(argSwitch), String.valueOf(argValue));
            }
        }
        return ao;
    }
}
