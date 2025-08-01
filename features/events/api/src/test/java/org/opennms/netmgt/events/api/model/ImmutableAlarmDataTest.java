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
package org.opennms.netmgt.events.api.model;

import org.junit.Test;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.xml.event.*;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * A test class to verify mapping an immutability properties of '{@link ImmutableAlarmData}'.
 */
public class ImmutableAlarmDataTest {

    @Test
    public void test() {
        UpdateField updateField1 = new UpdateField();
        updateField1.setValueExpression("value-1");
        updateField1.setUpdateOnReduction(true);
        updateField1.setFieldName("field-1");

        List<UpdateField> updateFields = new ArrayList<>();
        updateFields.add(updateField1);

        ManagedObject managedObject = new ManagedObject();
        managedObject.setType("managed-object-type");

        AlarmData alarmData = new AlarmData();
        alarmData.setReductionKey("reduction-key");
        alarmData.setAlarmType(1);
        alarmData.setClearKey("clear-key");
        alarmData.setAutoClean(false);
        alarmData.setX733AlarmType("alarm-type");
        alarmData.setX733ProbableCause(0);
        alarmData.setUpdateField(updateFields);
        alarmData.setManagedObject(managedObject);

        // Mutable to Immutable
        IAlarmData immutableAlarmData = ImmutableMapper.fromMutableAlarmData(alarmData);

        // Attempt to add to immutable list.
        try {
            immutableAlarmData.getUpdateFieldList().add(
                    ImmutableUpdateField.newBuilder().build());
            fail();
        } catch (Exception e) {
            // Expected...
        }

        // Immutable to Mutable
        AlarmData convertedAlarmData = AlarmData.copyFrom(immutableAlarmData);

        String expectedXml = XmlTest.marshalToXmlWithJaxb(alarmData);
        String convertedXml = XmlTest.marshalToXmlWithJaxb(convertedAlarmData);
        XmlTest.assertXmlEquals(expectedXml, convertedXml);
    }
}
