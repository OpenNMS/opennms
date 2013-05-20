/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.util.ilr;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


import static org.opennms.util.ilr.Filter.*;

public class Collector {

    
    public static final String SERVICE_TITLE_FORMAT = "%-40s%20s%15s%25s%15s%25s%15s%20s%25s%15s%15s\n";
    public static final String SERVICE_DATA_FORMAT = "%-40s%20s%15s%25s%15.1f%25s%15.1f%20s%25s%15s%15s\n";
    private String m_searchString = null;
    private static boolean s_durationsMs = false;


    public enum SortColumn {
        TOTALCOLLECTS,
        TOTALCOLLECTTIME,
        AVGCOLLECTTIME, 
        AVGTIMEBETWEENCOLLECTS,
        TOTALSUCCESSCOLLECTS,
        SUCCESSPERCENTAGE,
        AVGSUCCESSCOLLECTTIME,
        TOTALUNSUCCESSCOLLECTS,
        UNSUCCESSPERCENTAGE,
        AVGUNSUCCESSCOLLECTTIME,
        TOTALPERSISTTIME,
        AVERAGEPERSISTTIME
    }

    public enum SortOrder {
        ASCENDING, 
        DESCENDING
    }
    public void setSearchString(String searchString) {
        m_searchString = searchString;
    }
    SortOrder m_sortOrder = SortOrder.DESCENDING;
    SortColumn m_sortColumn = SortColumn.AVGCOLLECTTIME;

    public void setSortColumn(SortColumn sortFlag){
        this.m_sortColumn = sortFlag;
    }
    public void setSortOrder(SortOrder sortOrder) {
        this.m_sortOrder = sortOrder;   
    }
    public Collector.SortColumn getSortColumn() {
        return m_sortColumn;
    }
   public String getSearchString() {
       return m_searchString;
    }
    public static void setDurationsMs(boolean durationsMs) {
        s_durationsMs = durationsMs;   
    }
    public static boolean getDurationsMs() {
        return s_durationsMs;   
    }
    private Set<String> m_threads = new HashSet<String>();

    private LogMessage m_firstMessage;
    private LogMessage m_lastMessage;

    private Map<String, ServiceCollector> m_serviceCollectors = new HashMap<String, ServiceCollector>();

    public void addLog(String logMessage) {
        LogMessage msg = BaseLogMessage.create(logMessage);
        if (msg != null) {
            if (m_firstMessage == null && msg.isBeginMessage()) {
                m_firstMessage = msg;
            }
            if (msg.isEndMessage()) {
                m_lastMessage = msg;
            }
            getServiceCollector(msg.getServiceID()).addMessage(msg);
            m_threads.add(msg.getThread());
        }

    }
    public Date getStartTime() {
        LogMessage firstValidLine = getFirstValidLogMessage();
        return firstValidLine == null ? null : firstValidLine.getDate();	

    }
    public LogMessage getFirstValidLogMessage (){
        return m_firstMessage;
    }
    public LogMessage getLastValidLogMessage () {
        return m_lastMessage;
    }
    public Date getEndTime() {
        LogMessage logMessage = getLastValidLogMessage();
        return logMessage == null ? null : logMessage.getDate();
    }
    public long getDuration() {
        if (this.getEndTime() == null || this.getStartTime() == null) return 0L;
        return this.getEndTime().getTime()-this.getStartTime().getTime();
    }
    public String getFormattedDuration() {
        return formatDuration(getDuration());
    }
    public int getServiceCount() {
        return getServices().size();
    }
    public Map<String, ServiceCollector> getm_ServiceCollectors(){
        return m_serviceCollectors;
    }
    public int compareLongs(long l1, long l2) {
        Long a = Long.valueOf(l1);
        Long b = Long.valueOf(l2);
        return b.compareTo(a);
    }       
    public Set<String> getServices() {
        return m_serviceCollectors.keySet();
    }

    public List<ServiceCollector> getServiceCollectors() {
        ArrayList<ServiceCollector> collectors = new ArrayList<ServiceCollector>(m_serviceCollectors.values());
        Comparator<ServiceCollector> c = getColumnComparator();

        c = m_sortOrder == SortOrder.DESCENDING ? c: Collections.reverseOrder(c);
        Collections.sort(collectors, c);
        if(m_searchString != null) { 
            collectors = (ArrayList<ServiceCollector>) filter(collectors, byPartialServiceID(m_searchString));
        }
        return collectors;
    }


    private abstract static class LongComparator implements Comparator<ServiceCollector> {

        @Override
        public int compare(ServiceCollector o1, ServiceCollector o2) {
            Long a = Long.valueOf(getLong(o1));
            Long b = Long.valueOf(getLong(o2));
            return b.compareTo(a);
        }

        protected abstract long getLong(ServiceCollector sc);

    }
    private abstract static class DoubleComparator implements Comparator<ServiceCollector> {

        @Override
        public int compare(ServiceCollector o1, ServiceCollector o2) {
            Double a = Double.valueOf(getDouble(o1));
            Double b = Double.valueOf(getDouble(o2));
            return b.compareTo(a);
        }

        protected abstract double getDouble(ServiceCollector sc);

    }

    public Comparator<ServiceCollector> getColumnComparator() {
        Comparator<ServiceCollector> c = null;
        switch(m_sortColumn) {
        case TOTALCOLLECTS:
        {    
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getCollectionCount();
                }                
            };
        }   
        case TOTALCOLLECTTIME: 
        { 
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getTotalCollectionTime();
                }               


            };
        }   
        case AVGCOLLECTTIME: 
        { 
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getAverageCollectionTime();
                }                      
            };

        }   
        case AVGTIMEBETWEENCOLLECTS: 
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getAverageTimeBetweenCollections();
                }                  
            };

        }    
        case TOTALSUCCESSCOLLECTS: 
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getSuccessfulCollectionCount();
                }               
            };

        }   
        case SUCCESSPERCENTAGE:
        {
            return new DoubleComparator() {
                @Override
                protected double getDouble(ServiceCollector sc) {
                    return sc.getSuccessPercentage();
                }         
            };

        }

        case AVGSUCCESSCOLLECTTIME: 
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getAverageCollectionTime();
                }                
            };

        }

        case TOTALUNSUCCESSCOLLECTS: 
        {    
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getErrorCollectionCount();
                }                
            };

        }

        case UNSUCCESSPERCENTAGE:
        {    
            return new DoubleComparator() {
                @Override
                protected double getDouble(ServiceCollector sc) {
                    return sc.getErrorPercentage();
                }      
            };

        }

        case AVGUNSUCCESSCOLLECTTIME:
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getAverageErrorCollectionTime();
                }                
            };      

        }
        case TOTALPERSISTTIME:
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getTotalPersistTime();
                }               
            };
        }
        case AVERAGEPERSISTTIME:
        {
            return new LongComparator() {
                @Override
                protected long getLong(ServiceCollector sc) {
                    return sc.getAveragePersistTime();
                }               
            };
        }   
        }
        return c;
    }
    public int getThreadCount() {
        return m_threads.size();
    }
    public int getCollectionsPerService(String serviceID) {
        return getServiceCollector(serviceID).getCollectionCount();
    }

    public long getAverageCollectionTimePerService(String serviceID) {
        return getServiceCollector(serviceID).getAverageCollectionTime();
    }

    public long getTotalCollectionTimePerService(String serviceID) {
        return getServiceCollector(serviceID).getTotalCollectionTime();
    }
    private ServiceCollector getServiceCollector(String serviceID) {
        ServiceCollector serviceCollector = m_serviceCollectors.get(serviceID);
        if (serviceCollector == null) {
            serviceCollector = new ServiceCollector(serviceID);
            m_serviceCollectors.put(serviceID, serviceCollector);
        }
        return serviceCollector;
    }

    public void readLogMessagesFromFile(String fileName) throws IOException {
        File logFile = new File(fileName);
        BufferedReader r = new BufferedReader(new FileReader(logFile));	
        String logMessage = r.readLine();
        while(logMessage != null){
            this.addLog(logMessage);
            logMessage = r.readLine();
        }
        r.close();
    }
    public void printGlobalStats(PrintWriter out) {
        SimpleDateFormat f = new SimpleDateFormat ("yyyy-MM-dd HH:mm:ss,S");
        out.println("Start Time: " + f.format(this.getStartTime()));
        out.println("End Time: " + f.format(this.getEndTime()));
        out.println("Duration: "+ Collector.formatDuration(this.getDuration()));
        out.println("Total Services: "+this.getServiceCount());
        out.println("Threads Used: " + this.getThreadCount());
    }
    public static String formatDuration(long millis) {
        if (getDurationsMs()) {
            return new Long(millis).toString();
        }
        if (millis==0) {
            return "0s";
        }
        boolean force = false;
        StringBuilder buf = new StringBuilder();
        if (force || millis >= (1000*3600*24)) {
            long d = millis/(1000*3600*24);
            buf.append(d);
            buf.append("d");
            millis%=(1000*3600*24);
            force = millis!=0;
        }
        if (force || millis >= (1000*3600)) {
            long h = millis/(1000*3600);
            buf.append(h);
            buf.append("h");
            millis%=(1000*3600);
            force = millis!=0;
        }
        if (force || millis >= 60000) {
            long m = millis/60000;
            buf.append(m);
            buf.append("m");
            millis %= 60000;
            force=millis!=0;
        }
        if (millis!=0) {
            long s = millis/1000;
            buf.append(s);
            if(millis%1000 !=0) {
                buf.append(".");
                buf.append(String.format("%03d", millis%1000));
            }
            buf.append("s");
        }
        return buf.toString();

    }
    private void printServiceStats(ServiceCollector serviceCollector, PrintWriter out) {
        out.printf(SERVICE_DATA_FORMAT, serviceCollector.getServiceID(), 
                   Collector.formatDuration(serviceCollector.getAverageCollectionTime()), serviceCollector.getCollectionCount(), 
                   Collector.formatDuration(serviceCollector.getAverageSuccessfulCollectionTime()), serviceCollector.getSuccessPercentage(), 
                   Collector.formatDuration(serviceCollector.getAverageErrorCollectionTime()), serviceCollector.getErrorPercentage(),
                   Collector.formatDuration(serviceCollector.getAverageTimeBetweenCollections()),
                   Collector.formatDuration(serviceCollector.getTotalCollectionTime()),
                   Collector.formatDuration(serviceCollector.getAveragePersistTime()),
                   Collector.formatDuration(serviceCollector.getTotalPersistTime()));
    }
    //	Service               Avg Collect Time  Avg Persist Time  Avg Time between Collects # Collections Total Collection Time Total Persist Time
    //	19/172.10.1.21/SNMP       13.458s             .002s              5m27s                    3                 45.98s           .010s
    public void printServiceHeader(PrintWriter out) {
        out.printf(SERVICE_TITLE_FORMAT, "Service", "Avg Collect Time", "# Collections",  "Avg Success Time", "% Success", "Avg Error Time", "% Errors", "Avg Time Between", "Total Collection Time", "Avg Persist Time", "Total Persist Time");

    }
    public void printReport(PrintWriter out) {
        this.printGlobalStats(out);
        out.println();
        this.printServiceHeader(out);
        for(ServiceCollector serviceCollector : getServiceCollectors()) {
            this.printServiceStats(serviceCollector, out);
        }
    }
    public void printServiceStats(String serviceID, PrintWriter out) {
        ServiceCollector collector = getServiceCollector(serviceID);
        if (collector != null) {
            printServiceStats(collector, out);
        }

    }
}
