/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

import static org.junit.Assert.*;

import org.junit.Test;
import org.opennms.util.ilr.LogMessage;


public class ServiceCollectorTest { 
    
    @Test(expected=IllegalArgumentException.class)
    public void testServiceIDCheck() {
        
        String serviceID = "1/1.1.1.1/SVC";
        ServiceCollector svcCollector = new ServiceCollector(serviceID);
        
        LogMessage startMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect:" +
        		" begin:example1/24/216.216.217.254/SNMP");
        svcCollector.addMessage(startMessage);
        
    }
    
    @Test 
    public void testCollectAddMessage() {
        
        ServiceCollector svcCollector = setupServiceCollector();
        
        assertEquals("example1/24/216.216.217.254/SNMP", svcCollector.getServiceID());
        assertEquals(0, svcCollector.getCollectionCount());
        assertEquals(0, svcCollector.getAverageCollectionTime());
        
        setupCollectionMessages(svcCollector);
        
        assertEquals(1, svcCollector.getCollectionCount());
        assertEquals(20000L, svcCollector.getAverageCollectionTime());
        assertEquals(100,svcCollector.getSuccessPercentage(),0);
    }
    
    @Test
    public void testErrorAddMessage() {
        
        ServiceCollector svcCollector = setupServiceCollector();
        
        assertEquals("example1/24/216.216.217.254/SNMP", svcCollector.getServiceID());
        assertEquals(0, svcCollector.getErrorCollectionTime());
        assertEquals(0,svcCollector.getErrorCollectionCount());
        
        setupErrorMessages(svcCollector);
        
        assertEquals(1,svcCollector.getErrorCollectionCount());
        assertEquals(100.00,svcCollector.getErrorPercentage(),0);
        assertEquals(20000L,svcCollector.getAverageErrorCollectionTime());
    }

    @Test
    public void testPersistAddMessage() {
       
        ServiceCollector svcCollector = setupServiceCollector();
        
        assertEquals("example1/24/216.216.217.254/SNMP",svcCollector.getServiceID());
        assertEquals(0,svcCollector.getTotalPersistTime());
        assertEquals(0,svcCollector.getPersistCount());
        
        setupPersistMessages(svcCollector);
        
        assertEquals(1, svcCollector.getPersistCount());
        assertEquals(20000L, svcCollector.getAveragePersistTime());

    }
    
    @Test
    public void testGetCollectionTimeWithDifferentDates() {
        
        ServiceCollector svcCollector = setupServiceCollector();
        
        assertEquals(0,svcCollector.getPersistCount());
        assertEquals(0, svcCollector.getErrorCollectionCount());
        assertEquals(0, svcCollector.getCollectionCount());
        
        LogMessage startOneDayApartCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: " +
        "begin:example1/24/216.216.217.254/SNMP");
        LogMessage endOneDayApartCollectionMessage = BaseLogMessage.create("2010-05-27 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect" +
        ": end:example1/24/216.216.217.254/SNMP");
        
        svcCollector.addMessage(startOneDayApartCollectionMessage);
        svcCollector.addMessage(endOneDayApartCollectionMessage);
        
        assertEquals(1, svcCollector.getCollectionCount());
        assertEquals(86400000L, svcCollector.getAverageCollectionTime()); //test milliseconds in a day vs a one day long SUCCESSFUL collection
    }   
    
    @Test
    public void testGetErrorTimeWithDifferentDates() {
        
        ServiceCollector svcCollector = setupServiceCollector();
        
        LogMessage startOneDayApartErrorCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: " +
        "begin:example1/24/216.216.217.254/SNMP");
        LogMessage oneDayApartErrorMessage = BaseLogMessage.create("2010-05-27 12:00:00,884 INFO [CollectdScheduler-200 Pool-fiber86] collector.collect: error:" +
                        " example1/24/216.216.217.254/SNMP: org.opennms.netmgt.collectd.CollectionWarning: collect: collection failed for 172.30.248.86");
        LogMessage endOneDayApartErrorCollectionMessage = BaseLogMessage.create("2010-05-27 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect" +
        ": end:example1/24/216.216.217.254/SNMP"); 
        
        svcCollector.addMessage(startOneDayApartErrorCollectionMessage);
        svcCollector.addMessage(oneDayApartErrorMessage);
        svcCollector.addMessage(endOneDayApartErrorCollectionMessage);
        
        assertEquals(1, svcCollector.getErrorCollectionCount());
        assertEquals(86400000L, svcCollector.getErrorCollectionTime()); //test milliseconds in a day vs a one day long ERROR collection)
    }
    @Test
    public void testGetPersistTimeWithDifferentDates() {
        
        ServiceCollector svcCollector = setupServiceCollector();
        
        LogMessage startOneDayApartPersistMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-200 Pool-fiber81] " +
        "collector.collect: persistDataQueueing: begin: example1/24/216.216.217.254/SNMP");
        LogMessage endOneDayApartPersistMessage = BaseLogMessage.create("2010-05-27 12:12:18,027 INFO [CollectdScheduler-200 Pool-fiber22] " +
        "collector.collect: persistDataQueueing: end: example1/24/216.216.217.254/SNMP");
        
        svcCollector.addMessage(startOneDayApartPersistMessage);
        svcCollector.addMessage(endOneDayApartPersistMessage);
        
        assertEquals(1, svcCollector.getPersistCount());
        assertEquals(86400000L, svcCollector.getTotalPersistTime()); //test milliseconds in a day vs a one day long PERSIST)
    }

    @Test
    public void testGetParsedServiceID() {
       ServiceCollector svcCollector = setupServiceCollector();
       
       assertEquals("example1/24/216.216.217.254/SNMP", svcCollector.getServiceID());
       assertEquals("24", svcCollector.getParsedServiceID());
       
       String wrongFormatServiceID = "example1/153.5.8/32/SVC";
       ServiceCollector expectFailureCollector = new ServiceCollector(wrongFormatServiceID);
       
       assertEquals(wrongFormatServiceID, expectFailureCollector.getServiceID());
       assertEquals("Wrong ID", expectFailureCollector.getParsedServiceID());
       
       
    }
    
    public void setupCollectionMessages(ServiceCollector svcCollector){
   
        LogMessage startNormalCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: " +
        "begin:example1/24/216.216.217.254/SNMP");
        LogMessage endNormalCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:38,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect" +
        ": end:example1/24/216.216.217.254/SNMP");
        
        svcCollector.addMessage(startNormalCollectionMessage);
        svcCollector.addMessage(endNormalCollectionMessage);
    }    
    public void setupErrorMessages(ServiceCollector svcCollector){

        LogMessage startErrorCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: " +
        "begin:example1/24/216.216.217.254/SNMP");
        LogMessage ErrorMessage = BaseLogMessage.create("2010-05-26 12:56:23,884 INFO [CollectdScheduler-200 Pool-fiber86] collector.collect: error:" +
                        " example1/24/216.216.217.254/SNMP: org.opennms.netmgt.collectd.CollectionWarning: collect: collection failed for 172.30.248.86");
        LogMessage endErrorCollectionMessage = BaseLogMessage.create("2010-05-26 12:12:38,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect" +
        ": end:example1/24/216.216.217.254/SNMP");
        
        svcCollector.addMessage(startErrorCollectionMessage);
        svcCollector.addMessage(ErrorMessage);
        svcCollector.addMessage(endErrorCollectionMessage);
    }
    public void setupPersistMessages(ServiceCollector svcCollector) {
        
        LogMessage startPersistMessage = BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-200 Pool-fiber81] " +
        "collector.collect: persistDataQueueing: begin: example1/24/216.216.217.254/SNMP");
        LogMessage endPersistMessage = BaseLogMessage.create("2010-05-26 12:12:38,027 INFO [CollectdScheduler-200 Pool-fiber22] " +
        "collector.collect: persistDataQueueing: end: example1/24/216.216.217.254/SNMP");
        
        svcCollector.addMessage(startPersistMessage);
        svcCollector.addMessage(endPersistMessage);
    }
    public ServiceCollector setupServiceCollector() {
        String serviceID = "example1/24/216.216.217.254/SNMP";
        ServiceCollector svcCollector = new ServiceCollector(serviceID);
        return svcCollector;
    }

}
