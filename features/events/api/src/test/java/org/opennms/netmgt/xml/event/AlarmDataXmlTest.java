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
package org.opennms.netmgt.xml.event;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AlarmDataXmlTest extends XmlTestNoCastor<AlarmData> {
    public AlarmDataXmlTest(AlarmData sampleObject, Object sampleXml, String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() throws ParseException {
        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("%uei%:%dpname%:%nodeid%");
        alarmData.setAlarmType(3);
        alarmData.setAutoClean(false);
        ManagedObject managedObject = new ManagedObject();
        managedObject.setType("node");
        alarmData.setManagedObject(managedObject);
        return Arrays.asList(new Object[][] {
                {alarmData,
                "<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\" auto-clean=\"false\"> <managed-object type=\"node\"/> </alarm-data>",
                null
                }
        });
    }
}
