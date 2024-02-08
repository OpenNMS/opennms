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
package org.opennms.netmgt.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.opennms.netmgt.xml.event.AlarmData;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.UpdateField;

import static org.junit.Assert.*;

public class EventTranslatorConfigFactoryTest {

    private static String SEVERITY = "severity";
    private static String LOG_MSG = "logmsg";
    
    private Event event;
    
    @Before
    public void setUpTestCloneEvent() {
        event = new Event();
        AlarmData alarmData = new AlarmData();
       
        List<UpdateField> updateFields = new ArrayList<>();
       
        UpdateField field1 = new UpdateField();
        field1.setFieldName(SEVERITY);
        
        UpdateField field2 = new UpdateField();
        field2.setFieldName(LOG_MSG);
        
        updateFields.add(field1);
        updateFields.add(field2);
        
        alarmData.setUpdateFieldCollection(updateFields);
        
        event.setAlarmData(alarmData);
    }

    @Test
    public void testCloneEvent() {
        assertTrue(event instanceof Serializable);
        
        Event copy = EventTranslatorConfigFactory.cloneEvent(event);
        
        assertNotNull(copy);
                
    }

}
