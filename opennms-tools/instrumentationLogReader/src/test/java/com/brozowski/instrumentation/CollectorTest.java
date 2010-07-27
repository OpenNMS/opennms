/*
 * This file is part of the OpenNMS(R) Application.
 *
 * OpenNMS(R) is Copyright (C) 2010 The OpenNMS Group, Inc.  All rights reserved.
 * OpenNMS(R) is a derivative work, containing both original code, included code and modified
 * code that was published under the GNU General Public License. Copyrights for modified
 * and included code are below.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
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
 * OpenNMS Licensing       <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 */
package com.brozowski.instrumentation;

import static org.junit.Assert.*;
import static org.hamcrest.CoreMatchers.*;

import java.util.Date;

import org.junit.Test;

public class CollectorTest {
    
    @Test
    public void testGlobalTime() {
       Collector c = new Collector();
       
       c.addLog("2010-05-26 12:12:41,040 DEBUG [CollectdScheduler-50 Pool-fiber18] Collectd: collector.collect: collectData: begin: 61/172.20.1.206/SNMP");
       c.addLog("2010-05-26 12:12:45,787 DEBUG [CollectdScheduler-50 Pool-fiber18] Collectd: collector.collect: collectData: end: 61/172.20.1.206/SNMP");
       c.addLog("2010-05-26 12:12:45,787 DEBUG [CollectdScheduler-50 Pool-fiber18] Collectd: collector.collect: persistDataQueueing: begin: 61/172.20.1.206/SNMP"); 
       c.addLog("2010-05-26 12:12:47,152 DEBUG [CollectdScheduler-50 Pool-fiber18] Collectd: collector.collect: persistDataQueueing: end: 61/172.20.1.206/SNMP");

       assertThat(c.getStartTime(), is(timestamp("2010-05-26 12:12:41,040")));
       assertThat(c.getEndTime(), is(timestamp("2010-05-26 12:12:47,152")));
       assertThat(c.getDuration(), is(new Duration(6112)));
    }
    
    Date timestamp(String dateString) {
        return LogMessage.parseTimestamp(dateString);
    }

}
