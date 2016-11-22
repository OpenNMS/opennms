/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2005-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import static org.junit.Assert.assertNotNull;

import java.io.ByteArrayInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.test.MockLogAppender;
import org.opennms.netmgt.dao.support.PropertiesGraphDao.PrefabGraphTypeDao;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.test.FileAnticipator;
import org.springframework.core.io.Resource;

public class PropertiesGraphDaoITCase {

	protected static final Map<String, Resource> s_emptyMap = new HashMap<String, Resource>();
	protected static final String s_prefab = "command.prefix=foo\n"
	            + "output.mime=foo\n"
	            + "\n"
	            + "reports=mib2.HCbits, mib2.bits, mib2.discards\n"
	            + "\n"
	            + "report.mib2.HCbits.name=Bits In/Out\n"
	            + "report.mib2.HCbits.columns=ifHCInOctets,ifHCOutOctets\n"
	            + "report.mib2.HCbits.type=interface\n"
	            + "report.mib2.HCbits.externalValues=ifSpeed\n"
	            + "report.mib2.HCbits.suppress=mib2.bits\n"
	            + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\" \\\n"
	            + " DEF:octIn={rrd1}:ifHCInOctets:AVERAGE \\\n"
	            + " DEF:octOut={rrd2}:ifHCOutOctets:AVERAGE \\\n"
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
	protected static final String s_adhoc = "command.prefix=${install.rrdtool.bin} graph - --imgformat PNG --start {1} --end {2}\n"
	        + "output.mime=image/png\n"
	        + "adhoc.command.title=--title=\"{3}\"\n"
	        + "adhoc.command.ds=DEF:{4}={0}:{5}:{6}\n"
	        + "adhoc.command.graphline={7}:{4}#{8}:\"{9}\"\n";
	protected static final String s_responsePrefab = "command.prefix=foo\n"
	        + "output.mime=foo\n"
	        + "\n"
	        + "reports=icmp\n"
	        + "\n"
	        + "report.icmp.name=ICMP\n"
	        + "report.icmp.columns=icmp\n"
	        + "report.icmp.type=responseTime\n"
	        + "report.icmp.command=--title=\"ICMP Response Time\" \\\n"
	        + "  --vertical-label=\"Seconds\" \\\n"
	        + "  DEF:rtMicro={rrd1}:icmp:AVERAGE \\\n"
	        + "  CDEF:rt=rtMicro,1000000,/ \\\n"
	        + "  LINE1:rt#0000ff:\"Response Time\" \\\n"
	        + "  GPRINT:rt:AVERAGE:\" Avg  \\: %8.2lf %s\" \\\n"
	        + "  GPRINT:rt:MIN:\"Min  \\\\: %8.2lf %s\" \\\n"
	        + "  GPRINT:rt:MAX:\"Max  \\\\: %8.2lf %s\\\\n\"";
	protected static final String s_baseIncludePrefab = "command.prefix=foo\n" +
	        "output.mime=image/png\n" +
	        "reports=\n" + //Empty for a simple base prefab, with only graphs included from the sub directory
	        "include.directory=snmp-graph.properties.d\n" +
	        "include.directory.rescan=1000\n";
	protected static final String s_separateBitsGraph = "report.id=mib2.bits\n"
	        + "report.name=Bits In/Out\n"
	        + "report.columns=ifInOctets,ifOutOctets\n"
	        + "report.type=interface\n"
	        + "report.externalValues=ifSpeed\n"
	        + "report.command=--title=\"Bits In/Out\"\n";
	protected static final String s_separateHCBitsGraph = "report.id=mib2.HCbits\n"
	        + "report.name=Bits In/Out\n"
	        + "report.columns=ifHCInOctets,ifHCOutOctets\n"
	        + "report.type=interface\n"
	        + "report.externalValues=ifSpeed\n"
	        + "report.suppress=mib2.bits\n"
	        + "report.command=--title=\"Bits In/Out (High Speed)\"\n";
	protected static final String s_separateErrorsGraph = "report.id=mib2.errors\n"
	        +"report.name=Errors In/Out\n"
	        + "report.columns=ifIfErrors,ifOutErrors\n"
	        + "report.type=interface\n"
	        + "report.propertiesValues=ifSpeed\n"
	        + "report.command=--title=\"Erros In/Out\"\n";
	protected static final String s_includedMultiGraph1 = "reports=mib2.discards,mib2.errors\n"
	        +"report.mib2.discards.name=Discards In/Out\n"
	        + "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
	        + "report.mib2.discards.type=interface\n"
	        + "report.mib2.discards.propertiesValues=ifSpeed\n"
	        + "report.mib2.discards.command=--title=\"Discards In/Out\"\n"
	        + "\n"
	        + "report.mib2.errors.name=Errors In/Out\n"
	        + "report.mib2.errors.columns=ifInErrors,ifOutErrors\n"
	        + "report.mib2.errors.type=interface\n"
	        + "report.mib2.errors.propertiesValues=ifSpeed\n"
	        + "report.mib2.errors.command=--title=\"Discards In/Out\"\n";
	protected static final String s_includedMultiGraph2 = "reports=mib2.bits,mib2.HCbits\n"
	        + "report.mib2.bits.name=Bits In/Out\n"
	        + "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
	        + "report.mib2.bits.type=interface\n"
	        + "report.mib2.bits.externalValues=ifSpeed\n"
	        + "report.mib2.bits.command=--title=\"Bits In/Out\"\n"
	        + "\n"
	        + "report.mib2.HCbits.name=Bits In/Out\n"
	        + "report.mib2.HCbits.columns=ifHCInOctets,ifHCOutOctets\n"
	        + "report.mib2.HCbits.type=interface\n"
	        + "report.mib2.HCbits.externalValues=ifSpeed\n"
	        + "report.mib2.HCbits.suppress=mib2.bits\n"
	        + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\"\n";
	protected static final String s_mib2bitsBasePrefab = "command.prefix=foo\n"
	        + "output.mime=image/png\n" 
	        + "include.directory=snmp-graph.properties.d\n"
	        + "reports=mib2.bits\n"
	        + "report.mib2.bits.name=Wrong Name\n"
	        + "report.mib2.bits.columns=wrongColumn1,wrongColumn2\n"
	        + "report.mib2.bits.type=node\n"
	        + "report.mib2.bits.externalValues=fooBar\n"
	        + "report.mib2.bits.command=--title=\"Wrong Title\"\n";
	/**
	 * A prefab graphs config with just one of the reports broken in a subtle way (mib2.bits, with it's "name" property spelled "nmae"
	 * Used to test that the rest of the reports load as expected
	 * (Actual report details trimmed for space
	 */
	protected static final String s_partlyBorkedPrefab = "command.prefix=foo\n"
	        + "output.mime=foo\n"
	        + "\n"
	        + "reports=mib2.HCbits, mib2.bits, mib2.discards\n"
	        + "\n"
	        + "report.mib2.HCbits.name=Bits In/Out\n"
	        + "report.mib2.HCbits.columns=ifHCInOctets,ifHCOutOctets\n"
	        + "report.mib2.HCbits.type=interface\n"
	        + "report.mib2.HCbits.externalValues=ifSpeed\n"
	        + "report.mib2.HCbits.suppress=mib2.bits\n"
	        + "report.mib2.HCbits.command=--title=\"Bits In/Out (High Speed)\" \n"
	        + "\n"
	        + "report.mib2.bits.nmae=Bits In/Out\n"
	        + "report.mib2.bits.columns=ifInOctets,ifOutOctets\n"
	        + "report.mib2.bits.type=interface\n"
	        + "report.mib2.bits.externalValues=ifSpeed\n"
	        + "report.mib2.bits.command=--title=\"Bits In/Out\" \n"
	        + "\n"
	        + "report.mib2.discards.name=Discards In/Out\n"
	        + "report.mib2.discards.columns=ifInDiscards,ifOutDiscards\n"
	        + "report.mib2.discards.type=interface\n"
	        + "report.mib2.discards.propertiesValues=ifSpeed\n"
	        + "report.mib2.discards.command=--title=\"Discards In/Out\" \n";
	protected Map<String, FileReloadContainer<PrefabGraph>> m_graphs;
	protected PropertiesGraphDao m_dao;
	protected boolean m_testSpecificLoggingTest = false;
	protected FileAnticipator m_fileAnticipator = null;
	protected FileOutputStream m_outputStream = null;
	protected Writer m_writer = null;

	@Before
	public void setUp() throws Exception {
	    MockLogAppender.setupLogging(true);
	    
	    m_dao = createPropertiesGraphDao(s_emptyMap, s_emptyMap);
	    ByteArrayInputStream in = new ByteArrayInputStream(s_prefab.getBytes());
	    m_dao.loadProperties("performance", in);
	    
	    PrefabGraphTypeDao type = m_dao.findPrefabGraphTypeDaoByName("performance");
	    assertNotNull("could not get performance prefab graph type", type);
	
	    m_graphs = type.getReportMap();
	    assertNotNull("report map shouldn't be null", m_graphs);
	    
	    m_fileAnticipator = new FileAnticipator();
	}

	@After
	public void tearDown() throws Exception {
		IOUtils.closeQuietly(m_writer);
		IOUtils.closeQuietly(m_outputStream);
	
		// For Windows, see
		// http://stackoverflow.com/a/4213208/149820 for details
		m_writer = null;
		m_outputStream = null;
		System.gc();
	
	    //Allow an individual test to tell us to ignore the logging assertion
	    // e.g. if they're testing with assertLogAtLevel
	    if(!m_testSpecificLoggingTest) {
	        MockLogAppender.assertNoWarningsOrGreater();
	    }
	
		m_fileAnticipator.deleteExpected();
		m_fileAnticipator.tearDown();
	    MockLogAppender.resetState();
	}

	public PropertiesGraphDao createPropertiesGraphDao(Map<String, Resource> prefabConfigs, Map<String, Resource> adhocConfigs)
			throws IOException {
			    PropertiesGraphDao dao = new PropertiesGraphDao();
			    
			    dao.setPrefabConfigs(prefabConfigs);
			    dao.setAdhocConfigs(adhocConfigs);
			    dao.afterPropertiesSet();
			    
			    return dao;
			}

}
