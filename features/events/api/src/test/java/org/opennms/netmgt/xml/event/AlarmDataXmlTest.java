/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2018 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2018 The OpenNMS Group, Inc.
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
