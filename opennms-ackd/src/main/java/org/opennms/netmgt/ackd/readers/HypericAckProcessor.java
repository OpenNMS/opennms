/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * Modifications:
 * 
 * Created: January 28, 2010
 *
 * Copyright (C) 2009 The OpenNMS Group, Inc.  All rights reserved.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
 *
 * For more information contact:
 *      OpenNMS Licensing       <license@opennms.org>
 *      http://www.opennms.org/
 *      http://www.opennms.com/
 */
package org.opennms.netmgt.ackd.readers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.hibernate.criterion.Restrictions;

/*
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.HostConfiguration;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.HttpVersion;
 */

import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.dao.AckdConfigurationDao;
import org.opennms.netmgt.dao.AlarmDao;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsCriteria;
import org.opennms.netmgt.xml.event.Parms;

public class HypericAckProcessor implements AckProcessor {

    private AckdConfigurationDao m_ackdDao;
    private AlarmDao m_alarmDao;

    private static Logger log() {
        return ThreadCategory.getInstance(HypericAckProcessor.class);
    }

    public void reloadConfigs() {
        log().debug("reloadConfigs: reloading configuration...");
        m_ackdDao.reloadConfiguration();
        log().debug("reloadConfigs: configuration reloaded");
    }

    public void run() {
        int count = 0;

        // Parse Hyperic alert states
        try {
            log().info("run: Processing Hyperic acknowledgments..." );

            OnmsCriteria criteria = new OnmsCriteria(OnmsAlarm.class, "alarm");
            criteria.add(Restrictions.isNull("alarmAckUser"));
            // Restrict to Hyperic alerts
            criteria.add(Restrictions.eq("uei", "uei.opennms.org/external/hyperic/alert"));
            // TODO Figure out how to query by parameters

            Map<String,List<OnmsAlarm>> organizedAlarms = new TreeMap<String,List<OnmsAlarm>>();

            // Query list of outstanding alerts with remote platform identifiers
            List<OnmsAlarm> unAckdAlarms = m_alarmDao.findMatching(criteria);

            // Organize the alarms according to the Hyperic system where they originated
            for (OnmsAlarm alarm : unAckdAlarms) {
                String hypericSystem = alarm.getEventParms();
                // Figure out how to parse the event parms into a list, get the platform.agent.address or platform.id
                String key = "blah";
                List<OnmsAlarm> targetList = organizedAlarms.get(key);
                if (targetList == null) {
                    targetList = new ArrayList<OnmsAlarm>();
                    organizedAlarms.put(key, targetList);
                }
                targetList.add(alarm);
            }

            // Connect to each Hyperic system and query for the status of corresponding alerts 
            for (Map.Entry<String, List<OnmsAlarm>> alarmList : organizedAlarms.entrySet()) {
                String hypericSystem = alarmList.getKey();
                for (OnmsAlarm alarmsForSystem : alarmList.getValue()) {
                    // Construct a sane query for the Hyperic system
                }
            }

            // Iterate and update any acknowledged or fixed alerts

            log().info("run: Finished processing Hyperic acknowledgments (" + count + " acks processed)" );
        } catch (Throwable e) {
            log().warn("run: threw exception: "+e);
        }
    }

    public synchronized void setAckdConfigDao(final AckdConfigurationDao configDao) {
        m_ackdDao = configDao;
    }

    public void afterPropertiesSet() throws Exception {
    }

    public synchronized void setAlertDao(final AlarmDao dao) {
        m_alarmDao = dao;
    }
}
