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
package org.opennms.features.amqp.alarmnorthbounder;

import org.opennms.features.amqp.alarmnorthbounder.internal.DefaultAlarmForwarder;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.alarmd.api.NorthbounderException;
import org.opennms.netmgt.alarmd.api.support.AbstractNorthbounder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class AlarmNorthbounder extends AbstractNorthbounder {

    private volatile DefaultAlarmForwarder alarmForwarder;

    private static final Logger LOG = LoggerFactory.getLogger(AlarmNorthbounder.class);

    public AlarmNorthbounder() {
        super("AMQPNorthbounder");
        LOG.debug("AMQPNorthbounder created");
    }

    @Override
    protected boolean accepts(NorthboundAlarm alarm) {
        return true; // Accept ANY alarm
    }

    @Override
    public void forwardAlarms(List<NorthboundAlarm> alarms) throws NorthbounderException {
        for(NorthboundAlarm alarm: alarms) {
            LOG.trace("AMQPNorthbounder Forwarding alarm: {}", alarm);
            alarmForwarder.sendNow(alarm);
        }
    }

    public DefaultAlarmForwarder getAlarmForwarder() {
        return alarmForwarder;
    }

    public void setAlarmForwarder(DefaultAlarmForwarder alarmForwarder) {
        this.alarmForwarder = alarmForwarder;
    }

    @Override
    public boolean isReady() {
        return true;
    }
}
