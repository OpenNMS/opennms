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
package org.opennms.netmgt.alarmd.northbounder.syslog;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.alarmd.api.NorthboundAlarm;
import org.opennms.netmgt.model.OnmsAlarm;
import org.opennms.netmgt.model.OnmsEvent;
import org.opennms.netmgt.model.OnmsEventParameter;

import com.google.common.collect.Lists;

/**
 * The Class SyslogFilterTest.
 * 
 * @author <a href="mailto:agalue@opennms.org">Alejandro Galue</a>
 */
public class SyslogFilterTest {

    /**
     * Test filter parsing.
     *
     * @throws Exception the exception
     */
    @Test
    public void testFilterParsing() throws Exception {
        OnmsEvent event = new OnmsEvent();
        event.setEventParameters(Lists.newArrayList(
                new OnmsEventParameter(event, "user", "agalue", "string"),
                new OnmsEventParameter(event, "passwd", "0nmsRules", "string")));

        OnmsAlarm onmsAlarm = new OnmsAlarm();
        onmsAlarm.setLastEvent(event);
        onmsAlarm.setUei("uei.opennms.org/junit/test");
        NorthboundAlarm alarm = new NorthboundAlarm(onmsAlarm);
        SyslogFilter filter = new SyslogFilter("test", "uei matches '^uei\\.opennms\\.org.*' and parameters['user'] == 'agalue'", "localhost");
        Assert.assertTrue(filter.passFilter(alarm));
    }

}
