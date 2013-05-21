/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

import static org.junit.Assert.*;
import static org.opennms.util.ilr.Filter.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.Test;
import org.opennms.util.ilr.Filter.Predicate;

public class FilterTest {
    Filter filter = new Filter();
    Collector c = new Collector();
    ServiceCollector svcCollector = new ServiceCollector(null);

    public void setup() {
        c.addLog("2010-03-13 02:21:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:21:40,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/1.1.1.1/SNMP");
        c.addLog("2010-03-13 02:22:20,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:22:50,976 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/2.2.2.2/SNMP");
        c.addLog("2010-03-13 02:23:30,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: begin:0/3.3.3.3/SNMP");
        c.addLog("2010-03-13 02:23:50,000 DEBUG [CollectdScheduler-400 Pool-fiber51] Collectd: collector.collect: end:0/3.3.3.3/SNMP");
    }

    @Test
    public void testCreatePredicate() {
        Predicate<Integer> predicate = new Predicate<Integer>() {
            @Override
            public boolean apply(Integer i) {
                if(i == 1){
                    return true;
                }else{
                    return false;   
                }        
            }
        }; 
        boolean expectedTrue = predicate.apply(1);
        boolean expectedFalse = predicate.apply(0);

        assertEquals(true, expectedTrue);
        assertEquals(false, expectedFalse);
    }

    @Test
    public void testCreateIntegerBasedFilter() {
        Predicate<Integer> predicate = filter.createIntegerBasedPredicate(3);
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        Collection<Integer> actual = filter(list, predicate);
        List<Integer> expected = Arrays.asList(new Integer[] {3});
        assertEquals(expected, actual);
    }
    @Test
    public void testCreateStringBasedFilter() {
        Predicate<String> predicate = filter.createStringBasedPredicate("This is a String");
        List<String> list = Arrays.asList(new String[] {"just", "to","let","you","know","This is a String"});
        Collection<String> actual = filter(list, predicate);
        List<String> expected = Arrays.asList(new String[] {"This is a String"});
        assertEquals(expected, actual);
    }
    @Test
    public void testFilterEvenNumbersOut() {
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        Collection<Integer> oddNumbers = filter(list, new Predicate<Integer>() {
            @Override
            public boolean apply(Integer i) {
                if (i % 2 != 0) {
                    return true;
                }
                return false;
            }
        });
        List<Integer> expected = Arrays.asList(new Integer[] {1, 3, 5, 7, 9});
        assertEquals(expected, oddNumbers);
    }
    
    @Test
    public void testFilterServiceCollectorsByServiceID () {
        setup();
        
        final String serviceID = "0/3.3.3.3/SNMP";
        
        Collection<ServiceCollector> filtered = filter(c.getServiceCollectors(), and(eq(serviceID(), serviceID), lessThan(collectionCount(), 2)));
        assertEquals(1, filtered.size());
        assertEquals(serviceID, filtered.iterator().next().getServiceID());
        
    }

    @Test
    public void testCompoundFilters() {
        setup();
        final String serviceID1 = "0/1.1.1.1/SNMP";
        final String serviceID2 = "0/2.2.2.2/SNMP";
        final String serviceID3 = "0/3.3.3.3/SNMP";
        
        Collection<ServiceCollector> compoundOrFilter = filter(c.getServiceCollectors(), or(byServiceID(serviceID2), byServiceID(serviceID3)));
        assertEquals(2, compoundOrFilter.size());
        assertEquals(serviceID2, compoundOrFilter.iterator().next().getServiceID());
        
        
        Collection<ServiceCollector> compoundAndFilter = filter(c.getServiceCollectors(), and(byServiceID(serviceID1), byServiceID(serviceID2)));
        assertEquals(0, compoundAndFilter.size());
        assertFalse(compoundOrFilter.contains(serviceID1));
        assertFalse(compoundOrFilter.contains(serviceID2));
        assertFalse(compoundOrFilter.contains(serviceID3));
        
    }


    @Test
    public void testFilterOddNumbersOut() {
        List<Integer> list = Arrays.asList(new Integer[] { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 });
        Collection<Integer> evenNumbers = filter(list, new Predicate<Integer>() {
            @Override
            public boolean apply(Integer i) {
                if (i % 2 == 0) {
                    return true;
                }
                return false;
            }
        });
        List<Integer> expected = Arrays.asList(new Integer[] {2, 4, 6, 8, 10});
        assertEquals(expected, evenNumbers);
    }
}
