/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2020 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2020 The OpenNMS Group, Inc.
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
