/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class ServiceCollector {

    private String m_serviceID;
    private int m_collectionCount = 0;
    private int m_errorCount = 0;
    private int m_betweenCount = 0;
    private int m_persistCount = 0;
    private long m_totalTime = 0;
    private long m_errorTime = 0;
    private long m_totalBetweenTime = 0;
    private long m_totalPersistTime;
    private long m_lastBegin = 0;
    private long m_lastErrorBegin = 0;
    private long m_lastEnd = 0;
    private long m_lastPersistBegin = 0;
    
    public ServiceCollector(String serviceID) {
        m_serviceID = serviceID;
    }

    public String getServiceID() {
        return m_serviceID;
    }

    private static final String SERVICE_ID_REGEX = "([^/]+)/(\\d+)/(\\d+.\\d+.\\d+.\\d+)/([\\w-_]+)"; // Should be consistent with BaseLogMessage.s_regexp
    private static final Pattern SERVICE_ID_PATTERN = Pattern.compile(SERVICE_ID_REGEX);	

    public void addMessage(LogMessage msg) {
        if (!m_serviceID.equals(msg.getServiceID())) {
            throw new IllegalArgumentException("ServiceID of log message does not match serviceID of ServiceCollector: " + m_serviceID);
        }
        if (msg.isCollectorBeginMessage()) {
            m_lastBegin = msg.getDate().getTime();
            // measure the time between collections
            if (m_lastEnd > 0) {
                m_totalBetweenTime += msg.getDate().getTime() - m_lastEnd;
                m_betweenCount++;
            }
            m_lastEnd = 0;
        }
        if (msg.isErrorMessage()) {
            m_lastErrorBegin = m_lastBegin;
        }
        if (msg.isCollectorEndMessage()) {
            long end = msg.getDate().getTime();
            m_lastEnd = msg.getDate().getTime();
            if (m_lastBegin > 0) {
                m_totalTime += end - m_lastBegin;
                m_collectionCount++;
            }
            m_lastBegin = 0;
            if (m_lastErrorBegin > 0) {
                m_errorTime += end - m_lastErrorBegin;
                m_errorCount++;
            }
            m_lastErrorBegin = 0;
        }
        if (msg.isPersistBeginMessage()) {
            m_lastPersistBegin = msg.getDate().getTime();
        }
        if (msg.isPersistEndMessage()) {
            long end  = msg.getDate().getTime();
            msg.getDate().getTime();
            if(m_lastPersistBegin > 0) {
                m_totalPersistTime += end - m_lastPersistBegin;
                m_persistCount++;
            }
            m_lastPersistBegin = 0;
        }
    }

    public String getParsedServiceID() {
        Matcher m = SERVICE_ID_PATTERN.matcher(getServiceID());
        if(m.matches()) {
            return new String(m.group(2));
        }else{
            return "Wrong ID";
        }
    }

    public int getCollectionCount() {
        return m_collectionCount;
    }

    public int getErrorCollectionCount() {
        return m_errorCount;
    }
    public int getPersistCount() {
        return m_persistCount;
    }

    public long getTotalCollectionTime() {
        return m_totalTime;
    }

    public Duration getTotalCollectionDuration() {
        return new Duration(getTotalCollectionTime());
    }

    public long getErrorCollectionTime() {
        return m_errorTime;
    }
    public Duration getErrorCollectionDuration() {
        return new Duration(getErrorCollectionTime());
    }

    public long getSuccessfulCollectionTime() {
        return m_totalTime - m_errorTime;
    }
    public Duration getSuccessfulCollectionDuration() {
        return new Duration(getSuccessfulCollectionTime());
    }
    public long getTotalPersistTime() {
        return m_totalPersistTime;
    }
    public Duration getTotalPersistDuration() {
        return new Duration(getTotalPersistTime());
    }
    public int getSuccessfulCollectionCount() {
        return m_collectionCount - m_errorCount;
    }

    public double getSuccessPercentage() {
        if(getCollectionCount() == 0) {
            return -1;
        } else {
            return getSuccessfulCollectionCount()*100.0/getCollectionCount();	
        }
    }

    public double getErrorPercentage() {
        if(getCollectionCount() == 0) {
            return -1;
        } else {
            return getErrorCollectionCount()*100.0/getCollectionCount();
        }
    }

    public long getAverageCollectionTime() {
        int count = getCollectionCount();
        if (count == 0) return 0;
        return getTotalCollectionTime()/count;
    }

    public Duration getAverageCollectionDuration() {
        return new Duration(getAverageCollectionTime());
    }

    public long getAveragePersistTime() {
        int count = getPersistCount();
        if(count == 0) return 0;
        return getTotalPersistTime()/count;
    }
    public Duration getAveragePersistDuration() {
        return new Duration(getAveragePersistTime());
    }
    public long getAverageErrorCollectionTime() {
        int count = getErrorCollectionCount();
        if (count == 0) return 0;
        return getErrorCollectionTime()/count;
    }
    public Duration getAverageErrorCollectionDuration() {
        return new Duration(getAverageErrorCollectionTime());
    }

    public long getAverageSuccessfulCollectionTime() {
        int count = getSuccessfulCollectionCount();
        if (count == 0) return 0;
        return getSuccessfulCollectionTime()/count;
    }
    public Duration getAverageSuccessfulCollectionDuration() {
        return new Duration(getAverageSuccessfulCollectionTime());
    }

    public long getAverageTimeBetweenCollections() {
        if (m_betweenCount == 0) return 0;
        return m_totalBetweenTime/m_betweenCount;
    }
    public Duration getAverageDurationBetweenCollections() {
        return new Duration(getAverageTimeBetweenCollections());
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ServiceCollector) {
            ServiceCollector c = (ServiceCollector)obj;
            return getServiceID().equals(c.getServiceID());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return getServiceID().hashCode();
    }




}
