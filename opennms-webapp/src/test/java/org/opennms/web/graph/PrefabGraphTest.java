package org.opennms.web.graph;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.opennms.web.graph.PrefabGraph;

public class PrefabGraphTest extends TestCase {

	final static String s_propertiesString =
			"reports=mib2.bits, mib2.discards\n"
			+ "\n"
			+ "report.mib2.bits.name=Bits In/Out\n"
			+ "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
			+ "report.mib2.bits.type=interface\n"
			+ "report.mib2.bits.externalValues=ifSpeed\n"
			+ "report.mib2.bits.command=--title=\"Bits In/Out\" \\\n"
			+ " DEF:octIn={rrd1}:ifInOctets:AVERAGE \\\n"
			+ " DEF:octOut={rrd2}:ifOutOctets:AVERAGE \\\n"
			+ " CDEF:bitsIn=octIn,8,* \\\n"
			+ " CDEF:bitsOut=octOut,8,* \\\n"
			+ " CDEF:totBits=octIn,octOut,+,8,* \\\n"
			+ " AREA:totBits#00ff00:\"Total\" \\\n"
			+ " GPRINT:totBits:AVERAGE:\" Avg  \\\\: %8.2lf %s\\\\n\" \\\n"
			+ " LINE2:bitsIn#0000ff:\"Bits In\" \\\n"
			+ " GPRINT:bitsIn:AVERAGE:\" Avg  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:bitsIn:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:bitsIn:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n"
			+ " LINE2:bitsOut#ff0000:\"Bits Out\" \\\n"
			+ " GPRINT:bitsOut:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:bitsOut:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:bitsOut:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"\n"
			+ "\n"
			+ "report.mib2.discards.name=Discards In/Out\n"
			+ "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
			+ "report.mib2.discards.type=interface\n"
			+ "report.mib2.discards.propertiesValues=ifSpeed\n"
			+ "report.mib2.discards.command=--title=\"Discards In/Out\" \\\n"
			+ " DEF:octIn={rrd1}:ifInDiscards:AVERAGE \\\n"
			+ " DEF:octOut={rrd2}:ifOutDiscards:AVERAGE \\\n"
			+ " LINE2:octIn#0000ff:\"Discards In\" \\\n"
			+ " GPRINT:octIn:AVERAGE:\" Avg  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:octIn:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:octIn:MAX:\"Max  \\\\: %8.2lf %s\\\\n\" \\\n"
			+ " LINE2:octOut#ff0000:\"Discards Out\" \\\n"
			+ " GPRINT:octOut:AVERAGE:\"Avg  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:octOut:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
			+ " GPRINT:octOut:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"\n";

	Properties m_properties;
	Map m_graphs;
	
	public void setUp() throws IOException {
		m_properties = new Properties();
		m_properties.load(new ByteArrayInputStream(s_propertiesString.getBytes()));
		m_graphs = PrefabGraph.getPrefabGraphDefinitions(m_properties);
	}
	
	public void testCompareTo() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		PrefabGraph discards = (PrefabGraph) m_graphs.get("mib2.discards");

		assertEquals("compareTo", -1, bits.compareTo(discards));
	}
	
	public void testGetName() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getName", "mib2.bits", bits.getName());
    }

    public void testGetTitle() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getTitle", "Bits In/Out", bits.getTitle());
    }

    public void testGetOrder() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getOrder", 0, bits.getOrder());
    }

    public void testGetColumns() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		String[] columns = bits.getColumns(); 
		assertEquals("getColumns().length", 2, columns.length);
		assertEquals("getColumns()[0]", "ifInOctets", columns[0]);
		assertEquals("getColumns()[1]", "ifOutOctets", columns[1]);
    }

    public void testGetCommand() {
		String expectedCommand =
			"--title=\"Bits In/Out\" "
			+ "DEF:octIn={rrd1}:ifInOctets:AVERAGE "
			+ "DEF:octOut={rrd2}:ifOutOctets:AVERAGE "
			+ "CDEF:bitsIn=octIn,8,* "
			+ "CDEF:bitsOut=octOut,8,* "
			+ "CDEF:totBits=octIn,octOut,+,8,* "
			+ "AREA:totBits#00ff00:\"Total\" "
			+ "GPRINT:totBits:AVERAGE:\" Avg  \\: %8.2lf %s\\n\" "
			+ "LINE2:bitsIn#0000ff:\"Bits In\" "
			+ "GPRINT:bitsIn:AVERAGE:\" Avg  \\: %8.2lf %s\" "
			+ "GPRINT:bitsIn:MIN:\"Min  \\: %8.2lf %s\" "
			+ "GPRINT:bitsIn:MAX:\"Max  \\: %8.2lf %s\\n\" "
			+ "LINE2:bitsOut#ff0000:\"Bits Out\" "
			+ "GPRINT:bitsOut:AVERAGE:\"Avg  \\: %8.2lf %s\" "
			+ "GPRINT:bitsOut:MIN:\"Min  \\: %8.2lf %s\" "
			+ "GPRINT:bitsOut:MAX:\"Max  \\: %8.2lf %s\\n\"";
		
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getCommand", expectedCommand, bits.getCommand());
    }

    public void testGetExternalValues() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		String[] values = bits.getExternalValues();
		assertEquals("getExternalValues().length", 1, values.length);
		assertEquals("getExternalValues()[0]", "ifSpeed", values[0]);
    }

	public void testGetExternalValuesEmpty() {
		PrefabGraph discards = (PrefabGraph) m_graphs.get("mib2.discards");
		assertEquals("getExternalValues().length", 0, discards.getExternalValues().length);
    }

    public void testGetPropertiesValues() {
		PrefabGraph discards = (PrefabGraph) m_graphs.get("mib2.discards");
		String[] values = discards.getPropertiesValues();
		assertEquals("getPropertiesValues().length", 1, values.length);
		assertEquals("getPropertiesValues()[0]", "ifSpeed", values[0]);
    }

    public void testGetPropertiesValuesEmpty() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getPropertiesValues().length", 0, bits.getPropertiesValues().length);
    }

    public void testGetType() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getGetType", "interface", bits.getType());
    }

	public void testGetDescription() {
		PrefabGraph bits = (PrefabGraph) m_graphs.get("mib2.bits");
		assertEquals("getDescription", null, bits.getDescription());
    }

}
