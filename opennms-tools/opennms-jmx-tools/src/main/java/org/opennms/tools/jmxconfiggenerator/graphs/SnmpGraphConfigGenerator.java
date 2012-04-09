/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.tools.jmxconfiggenerator.graphs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXB;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.opennms.xmlns.xsd.config.jmx_datacollection.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Simon Walter <simon.walter@hp-factory.de>
 * @author Markus Neumann <markus@opennms.com>
 */

@Deprecated
public class SnmpGraphConfigGenerator {
	
	private static Logger logger = LoggerFactory.getLogger(SnmpGraphConfigGenerator.class);
	
	private static String graphList = "";
	private static String graphBodies = "";
	private static String output = "";
	private static String serviceName = "";
	
	private static List<String> tangoColors = new ArrayList<String>();
	private static int colorIndex = 0;

	static {
		tangoColors.add("edd400");
		tangoColors.add("f57900");
		tangoColors.add("c17d11");
		tangoColors.add("73d216");
		tangoColors.add("3465a4");
		tangoColors.add("75507b");
		tangoColors.add("cc0000");
		tangoColors.add("d3d7cf");
		tangoColors.add("555753");
		tangoColors.add("fce94f");
		tangoColors.add("fcaf3e");
		tangoColors.add("e9b96e");
		tangoColors.add("8ae234");
		tangoColors.add("729fcf");
		tangoColors.add("ad7fa8");
		tangoColors.add("ef2929");
		tangoColors.add("eeeeec");
		tangoColors.add("888a85");
		tangoColors.add("c4a000");
		tangoColors.add("ce5c00");
		tangoColors.add("8f5902");
		tangoColors.add("4e9a06");
		tangoColors.add("204a87");
		tangoColors.add("5c3566");
		tangoColors.add("a40000");
		tangoColors.add("babdb6");
		tangoColors.add("2e3436");
	}

	public static void generateGraphs(String serviceName, String inputFile, String outFile) throws IOException {
		JmxDatacollectionConfig inputConfig = JAXB.unmarshal(new File(inputFile), JmxDatacollectionConfig.class);
		
		for (JmxCollection jmxCollection : inputConfig.getJmxCollection()) {
			logger.debug("jmxCollection: '{}'", jmxCollection.getName());
			Mbeans mbeans = jmxCollection.getMbeans();
			int i = 0;
			for (Mbean mBean : mbeans.getMbean()) {
				logger.debug("\t" + "mBean: '{}'", mBean.getObjectname());
				String reportName = "jsr160" + "." + serviceName  + "." + "combo" + i;
				graphList = graphList + reportName + ", \\" + "\n";
				i++;
				graphBodies = graphBodies + "\n" + generateGraphCombo(jmxCollection, mBean, reportName);
				for (Attrib attrib :mBean.getAttrib()) {
					logger.debug("\t\t" + "attrib: '{}'", attrib.getAlias());
					graphList = graphList + "jsr160" + "." + serviceName + "."  + StringUtils.substring(attrib.getAlias(), 0 ,19) + ", \\" + "\n";
					graphBodies = graphBodies + "\n" + generateGraph(jmxCollection, mBean, attrib);
				}
			}
		}
		graphList = StringUtils.substringBeforeLast(graphList, ", \\\n") + "\n";
		output = graphList + "\n" + graphBodies;
		output = StringUtils.substringBeforeLast(output, " \\" + "\n") + "\n";
		FileUtils.writeStringToFile(new File(outFile), output, "UTF-8");
	}
	
	//TODO Tak: implement single graphs for composite data
	
	//TODO Tak: implement combo graphs for composite data
	private static String generateCompositeGraph(JmxCollection jmxCollection, Mbean mBean, String reportName) {
		return "";
	}
	
	private static String generateGraphCombo(JmxCollection jmxCollection, Mbean mBean, String reportName) {
		String report = "";

//		report.jvm.mempool.oldgen.name=JVM Memory Pool: OldGen Space
		report = report.concat("report." + reportName + ".name=" + reportName + "\n");
		
		List<String> attribNameList = new ArrayList<String>();
		String columnsNames = "";
		for (Attrib attrib : mBean.getAttrib()) {
			String attribAliasShort = StringUtils.substring(attrib.getAlias(), 0 , 19);
			attribNameList.add(attribAliasShort);
			columnsNames = columnsNames.concat(attribAliasShort + ", ");
		}
		columnsNames = StringUtils.substringBeforeLast(columnsNames, ", ");
		
//		report.jvm.mempool.oldgen.columns=OGenUsage.used, OGenUsage.max
		report = report.concat("report." + reportName + ".columns=" + columnsNames + "\n");
		
		
//		report.jvm.mempool.oldgen.type=interfaceSnmp
		report = report.concat("report." + reportName + ".type=interfaceSnmp" + "\n");
		
//		report.jvm.mempool.oldgen.command=--title="JVM Memory Pool: Old Gen Space" \
		report = report.concat("report." + reportName + ".command=--title=\"" + mBean.getName() + "\"" + " \\" + "\n");

//		 --vertical-label="COUNT Hits" \
		report = report.concat(" --vertical-label=\"COUNT\" " + "\\" + "\n");
		
		int i = 0;
		for (Attrib attrib : mBean.getAttrib()) {
			i++;
			String attribAliasShort = StringUtils.substring(attrib.getAlias(), 0 , 19);
//			 DEF:used={rrd1}:OGenUsage.used:AVERAGE \
//			 DEF:max={rrd2}:OGenUsage.max:AVERAGE \
			report = report.concat(" DEF:" + attribAliasShort + "={rrd"+i+"}:" + attribAliasShort + ":AVERAGE" + " \\" + "\n");	
		}

		i = 0;
		for (Attrib attrib : mBean.getAttrib()) {
			i++;
			String attribAliasShort = StringUtils.substring(attrib.getAlias(), 0 , 19);
//			 LINE2:used#73d216:"Bytes Used" \
			report = report.concat(" LINE2:" + attribAliasShort + "#" + getNextColor() + ":\"" + attribAliasShort + "\"" + " \\" + "\n");

//			 GPRINT:used:AVERAGE:" Avg \\: %5.2lf %s " \
			report = report.concat(" GPRINT:" + attribAliasShort + ":AVERAGE:\" Avg \\\\: %8.2lf %s\"" + " \\" + "\n");

//			 GPRINT:used:MIN:" Min \\: %5.2lf %s " \
			report = report.concat(" GPRINT:" + attribAliasShort + ":MIN:\" Min \\\\: %8.2lf %s\"" + " \\" + "\n");
			
//			 GPRINT:used:MAX:" Max \\: %5.2lf %s " \			
			report = report.concat(" GPRINT:" + attribAliasShort + ":MAX:\" Max \\\\: %8.2lf %s\"" + " \\" + "\n");
			
		}

		colorIndex = 0;
		report = StringUtils.substringBeforeLast(report, "\" \\\n") + "\\\\n\" \\\n";


//		 LINE2:max#ff0000:"Bytes Allocated" \
//		 GPRINT:max:AVERAGE:"Avg \\: %5.2lf %s " \
//		 GPRINT:max:MIN:"Min \\: %5.2lf %s " \
//		 GPRINT:max:MAX:"Max \\: %5.2lf %s\\n"
		
		return report;
	}

	private static String generateGraph(JmxCollection jmxCollection, Mbean mBean, Attrib attrib) {
		String report = "";
		String attibutAliasShort = StringUtils.substring(attrib.getAlias(), 0 ,19);
		
		//String reportDomainLikeName = "report." + jmxCollection.getName() + "." + attibutAliasShort;
		String reportDomainLikeName = "report." + "jsr160." + serviceName + "." + attibutAliasShort;
		
		
//		report.jsr160.cassandra.${attributAlias}.name=${reportDisplayName}
		
		//Alias passt nicht wirklich
//		report.${rrdReportTitle}.name=${reportDisplayName}
		report = report.concat(reportDomainLikeName + ".name=" + attibutAliasShort + "\n");
		
//		report.${jmx-collectionName}.columns=${attributAlias}
		report = report.concat(reportDomainLikeName + ".columns=" + attibutAliasShort + "\n");
		
//		report.${jmx-collectionName}.type=interfaceSnmp
		report = report.concat(reportDomainLikeName + ".type=interfaceSnmp" + "\n");
		
//		report.${jmx-collectionName}.command=--title=${"graphTitle"} \
		report = report.concat(reportDomainLikeName + ".command=--title=\"" + mBean.getName() + " " + attrib.getName() + "\" \\" + "\n");

//		--vertical-label=${"graphUnitName"} \
		report = report.concat(" --vertical-label=" +"\"" + "COUNT " + attrib.getName() +"\"" + " \\" + "\n");
		
//		DEF:${attributAlias}={rrd1}:${attributAlias}:AVERAGE \
		report = report.concat(" DEF:" + attibutAliasShort + "={rrd1}:" + attibutAliasShort + ":AVERAGE" + " \\" + "\n");
		
//		LINE2:${attributAlias}#00ff00:"In " \
		report = report.concat(" LINE2:" + attibutAliasShort + "#73d216" + ":\"" + attibutAliasShort + "\""  + " \\" + "\n");
		
		
//		GPRINT:${attributAlias}:AVERAGE:"Avg  \\: %8.2lf %s" \
		report = report.concat(" GPRINT:" + attibutAliasShort + ":AVERAGE:\"Avg \\\\: %8.2lf %s\"" + " \\" + "\n");
		
//		GPRINT:${attributAlias}:MIN:"Min  \\: %8.2lf %s" \
		report = report.concat(" GPRINT:" + attibutAliasShort + ":MIN:\"Min \\\\: %8.2lf %s\"" + " \\" + "\n");
		
//		GPRINT:${attributAlias}:MAX:"Max  \\: %8.2lf %s\\n" \
		report = report.concat(" GPRINT:" + attibutAliasShort + ":MAX:\"Max \\\\: %8.2lf %s\\\\n\"" + " \\" + "\n");
		
		return report;
	}

	private static String getNextColor() {
		String color = tangoColors.get(colorIndex);
		colorIndex = (colorIndex + 1)%tangoColors.size();	
		return color;
	}	
}
