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
package org.opennms.netmgt.alarmd.drools;

import java.util.Date;

import org.opennms.netmgt.dao.api.AlarmDao;
import org.opennms.netmgt.dao.api.AlarmEntityNotifier;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.events.EventBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

public class DefaultAlarmTicketerService implements AlarmTicketerService {
    private static final Logger LOG = LoggerFactory.getLogger(DefaultAlarmTicketerService.class);

    private static final boolean ALARM_TROUBLE_TICKET_ENABLED = Boolean.getBoolean("opennms.alarmTroubleTicketEnabled");

    @Autowired
    private AlarmDao alarmDao;

    @Autowired
    private EventForwarder eventForwarder;

    @Autowired
    private AlarmEntityNotifier alarmEntityNotifier;

    @Override
    public boolean isTicketingEnabled() {
        return ALARM_TROUBLE_TICKET_ENABLED;
    }

    @Override
    @Transactional
    public void createTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="createTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/create" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
            </action-event>
        */

        // Send the create ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_CREATE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    @Override
    @Transactional
    public void updateTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="updateTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/update" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
              <assignment type="parameter" name="troubleTicket" value="${_tticketID}" />
            </action-event>
         */

        // Send the update ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_UPDATE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    @Override
    @Transactional
    public void closeTicket(OnmsAlarm alarm, Date now) {
        /*
            <action-event name="closeTicket" for-each-result="true" >
              <assignment type="field" name="uei" value="uei.opennms.org/troubleTicket/close" />
              <assignment type="parameter" name="alarmUei" value="${_eventuei}" />
              <assignment type="parameter" name="user" value="${_user}" />
              <assignment type="parameter" name="alarmId" value="${_alarmid}" />
              <assignment type="parameter" name="troubleTicket" value="${_tticketID}" />
            </action-event>
         */

        // Send the update ticket event
        eventForwarder.sendNow(new EventBuilder(EventConstants.TROUBLETICKET_CLOSE_UEI, DefaultAlarmTicketerService.class.getSimpleName())
                .addParam(EventConstants.PARM_ALARM_UEI, alarm.getUei())
                .addParam(EventConstants.PARM_USER, DefaultAlarmService.DEFAULT_USER)
                .addParam(EventConstants.PARM_ALARM_ID, alarm.getId())
                .addParam(EventConstants.PARM_TROUBLE_TICKET, alarm.getTTicketId())
                .getEvent());

        // Update the lastAutomationTime
        updateLastAutomationTime(alarm, now);
    }

    private void updateLastAutomationTime(OnmsAlarm alarm, Date now) {
        final OnmsAlarm alarmInTrans = alarmDao.get(alarm.getId());
        if (alarmInTrans == null) {
            LOG.warn("Alarm disappeared: {}. lastAutomationTime will not be updated.", alarm);
            return;
        }

        // Update the lastAutomationTime
        final Date previousLastAutomationTime = alarmInTrans.getLastAutomationTime();
        alarmInTrans.setLastAutomationTime(now);
        alarmEntityNotifier.didUpdateLastAutomationTime(alarmInTrans, previousLastAutomationTime);
    }
}
