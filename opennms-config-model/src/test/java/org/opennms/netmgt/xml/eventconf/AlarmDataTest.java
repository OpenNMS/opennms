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
		return Arrays.asList(new Object[][] {
				{alarmData0,
				"<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\"/>",
				"target/classes/xsds/eventconf.xsd" },
				{alarmData1,
				"<alarm-data reduction-key=\"%uei%:%dpname%:%nodeid%\" alarm-type=\"3\" auto-clean=\"true\" clear-key=\"uei.opennms.org/internal/importer/importFailed:%parm[importResource]%\"/>",
				"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
