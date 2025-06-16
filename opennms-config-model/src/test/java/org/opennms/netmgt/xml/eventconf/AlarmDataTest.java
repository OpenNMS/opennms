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
package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AlarmDataTest extends XmlTestNoCastor<AlarmData> {

	public AlarmDataTest(final AlarmData sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		AlarmData alarmData0 = new AlarmData();
		alarmData0.setReductionKey("%uei%:%dpname%:%nodeid%");
		alarmData0.setAlarmType(3);
		AlarmData alarmData1 = new AlarmData();
		alarmData1.setReductionKey("%uei%:%dpname%:%nodeid%");
		alarmData1.setAlarmType(3);
		alarmData1.setAutoClean(true);
		alarmData1.setClearKey("uei.opennms.org/internal/importer/importFailed:%parm[importResource]%");
		AlarmData alarmData2 = new AlarmData();
		alarmData2.setReductionKey("%uei%:%dpname%:%nodeid%");
		alarmData2.setAlarmType(3);
		alarmData2.setAutoClean(false);
		ManagedObject managedObject = new ManagedObject();
		managedObject.setType("node");
		alarmData2.setManagedObject(managedObject);
		return Arrays.asList(new Object[][] {
				{alarmData0,
				"<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\"/>",
				"target/classes/xsds/eventconf.xsd" },
				{alarmData1,
				"<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\" auto-clean=\"true\" clear-key=\"uei.opennms.org/internal/importer/importFailed:%parm[importResource]%\"/>",
				"target/classes/xsds/eventconf.xsd" },
				{alarmData2,
				"<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\" auto-clean=\"false\"> <managed-object type=\"node\"/> </alarm-data>",
				"target/classes/xsds/eventconf.xsd"
				}
		});
	}

}
