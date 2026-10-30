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
    public void testMinMaxLastCollectionTime() {
        ServiceCollector svcCollector = setupServiceCollector();

        assertEquals(0, svcCollector.getMinCollectionTime());
        assertEquals(0, svcCollector.getMaxCollectionTime());
        assertEquals(0, svcCollector.getLastCollectionTime());

        addCollection(svcCollector, "2010-05-26 12:12:18,027", "2010-05-26 12:12:23,027");
        addCollection(svcCollector, "2010-05-26 12:17:18,027", "2010-05-26 12:17:38,027");
        addCollection(svcCollector, "2010-05-26 12:22:18,027", "2010-05-26 12:22:20,027");
        addCollection(svcCollector, "2010-05-26 12:27:18,027", "2010-05-26 12:27:28,027");

        assertEquals(4, svcCollector.getCollectionCount());
        assertEquals(2000L, svcCollector.getMinCollectionTime());
        assertEquals(20000L, svcCollector.getMaxCollectionTime());
        assertEquals(10000L, svcCollector.getLastCollectionTime());
        assertEquals(new Duration(2000L).toString(), svcCollector.getMinCollectionDuration().toString());
        assertEquals(new Duration(20000L).toString(), svcCollector.getMaxCollectionDuration().toString());
        assertEquals(new Duration(10000L).toString(), svcCollector.getLastCollectionDuration().toString());
    }

    @Test
    public void testMinMaxLastIgnoreUnmatchedEnd() {
        ServiceCollector svcCollector = setupServiceCollector();

        svcCollector.addMessage(BaseLogMessage.create("2010-05-26 12:12:18,027 INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: end:example1/24/216.216.217.254/SNMP"));
        assertEquals(0, svcCollector.getCollectionCount());
        assertEquals(0, svcCollector.getMinCollectionTime());
        assertEquals(0, svcCollector.getMaxCollectionTime());
        assertEquals(0, svcCollector.getLastCollectionTime());

        addCollection(svcCollector, "2010-05-26 12:17:18,027", "2010-05-26 12:17:21,027");
        assertEquals(3000L, svcCollector.getMinCollectionTime());
        assertEquals(3000L, svcCollector.getMaxCollectionTime());
        assertEquals(3000L, svcCollector.getLastCollectionTime());
    }

    private void addCollection(ServiceCollector svcCollector, String begin, String end) {
        svcCollector.addMessage(BaseLogMessage.create(begin + " INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: begin:example1/24/216.216.217.254/SNMP"));
        svcCollector.addMessage(BaseLogMessage.create(end + " INFO [CollectdScheduler-50 Pool-fiber11] collector.collect: end:example1/24/216.216.217.254/SNMP"));
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
