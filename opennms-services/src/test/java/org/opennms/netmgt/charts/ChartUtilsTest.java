//
// This file is part of the OpenNMS(R) Application.
//
// OpenNMS(R) is Copyright (C) 2005 The OpenNMS Group, Inc.  All rights reserved.
// OpenNMS(R) is a derivative work, containing both original code, included code and modified
// code that was published under the GNU General Public License. Copyrights for modified 
// and included code are below.
//
// OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
//
// Original code base Copyright (C) 1999-2001 Oculan Corp.  All rights reserved.
//
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
//
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.                                                            
//
// You should have received a copy of the GNU General Public License
// along with this program; if not, write to the Free Software
// Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
//    
// For more information contact: 
//   OpenNMS Licensing       <license@opennms.org>
//   http://www.opennms.org/
//   http://www.opennms.com/
//
// Tab Size = 8

package org.opennms.netmgt.charts;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.JFreeChart;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.charts.BarChart;
import org.opennms.netmgt.mock.OpenNMSTestCase;



public class ChartUtilsTest extends OpenNMSTestCase {
    
    private static final String CHART_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<tns:chart-configuration xmlns:tns=\"http://xmlns.opennms.org/xsd/config/charts\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/charts ../src/services/org/opennms/netmgt/config/chart-configuration.xsd \">\n" + 
            "\n" + 
            "  <tns:bar-chart name=\"sample-bar-chart\" \n" + 
            "   variation=\"2d\" \n" + 
            "   domain-axis-label=\"Severity\" \n" + 
            "   show-legend=\"true\" \n" + 
            "   plot-orientation=\"vertical\" \n" + 
            "   draw-bar-outline=\"true\" \n" + 
            "   range-axis-label=\"Count\" \n" + 
            "   show-urls=\"false\"\n" + 
            "    show-tool-tips=\"false\">\n" + 
            "      \n" + 
            "    <tns:title font=\"SansSerif\" style=\"\" value=\"Alarms\" pitch=\"12\" />\n" + 
            "    <tns:image-size>\n" + 
            "      <tns:hz-size>\n" + 
            "        <tns:pixels>300</tns:pixels>\n" + 
            "      </tns:hz-size>\n" + 
            "      <tns:vt-size>\n" + 
            "        <tns:pixels>300</tns:pixels>\n" + 
            "      </tns:vt-size>\n" + 
            "    </tns:image-size>\n" + 
            "    <tns:sub-title position=\"top\" horizontal-alignment=\"center\">\n" + 
            "           <tns:title font=\"SansSerif\" style=\"\" value=\"Severity Chart\" pitch=\"10\" />\n" + 
            "    </tns:sub-title>\n" + 
            "    <tns:grid-lines visible=\"true\">\n" + 
            "        <tns:rgb>\n" + 
            "            <tns:red>\n" + 
            "                <tns:rgb-color>255</tns:rgb-color>\n" + 
            "            </tns:red>\n" + 
            "            <tns:green>\n" + 
            "                <tns:rgb-color>255</tns:rgb-color>\n" + 
            "            </tns:green>\n" + 
            "            <tns:blue>\n" + 
            "                <tns:rgb-color>255</tns:rgb-color>\n" + 
            "            </tns:blue>\n" + 
            "        </tns:rgb>\n" + 
            "    </tns:grid-lines>\n" + 
            "    <tns:series-def number=\"1\" series-name=\"Events\" use-labels=\"true\" >\n" + 
            "     <tns:jdbc-data-set db-name=\"opennms\" sql=\"select eventseverity, count(*) from events where eventseverity &gt; 4 group by eventseverity\" />\n" + 
            "      <tns:rgb>\n" + 
            "        <tns:red>\n" + 
            "          <tns:rgb-color>255</tns:rgb-color>\n" + 
            "        </tns:red>\n" + 
            "        <tns:green>\n" + 
            "          <tns:rgb-color>255</tns:rgb-color>\n" + 
            "        </tns:green>\n" + 
            "        <tns:blue>\n" + 
            "          <tns:rgb-color>0</tns:rgb-color>\n" + 
            "        </tns:blue>\n" + 
            "      </tns:rgb>\n" + 
            "    </tns:series-def>\n" + 
            "    <tns:series-def number=\"1\" series-name=\"Alarms\" use-labels=\"true\" >\n" + 
            "     <tns:jdbc-data-set db-name=\"opennms\" sql=\"select severity, count(*) from alarms where severity &gt; 4 group by severity\" />\n" + 
            "      <tns:rgb>\n" + 
            "        <tns:red>\n" + 
            "          <tns:rgb-color>255</tns:rgb-color>\n" + 
            "        </tns:red>\n" + 
            "        <tns:green>\n" + 
            "          <tns:rgb-color>0</tns:rgb-color>\n" + 
            "        </tns:green>\n" + 
            "        <tns:blue>\n" + 
            "          <tns:rgb-color>0</tns:rgb-color>\n" + 
            "        </tns:blue>\n" + 
            "      </tns:rgb>\n" + 
            "    </tns:series-def>\n" + 
            "  </tns:bar-chart>\n" + 
            "</tns:chart-configuration>\n" + 
            "";
//    private ChartConfiguration m_config;

    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("java.awt.headless", "true");
        initalizeChartFactory();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testGetBarChartConfig() throws MarshalException, ValidationException, FileNotFoundException, IOException {
        
        assertNotNull(ChartUtils.getBarChartConfigByName("sample-bar-chart"));
        assertTrue(ChartUtils.getBarChartConfigByName("sample-bar-chart").getClass() == BarChart.class);
    }
    
    public void testGetBarChart() throws MarshalException, ValidationException, IOException, SQLException {
        JFreeChart barChart = ChartUtils.getBarChart("sample-bar-chart");
        assertNotNull(barChart);
        //SubTitle count includes "LegendTitle"
        assertEquals(2, barChart.getSubtitleCount());
    }

    public void testGetChartWithInvalidChartName() throws MarshalException, ValidationException, IOException, SQLException {
        
        JFreeChart chart = null;
        try {
            chart = ChartUtils.getBarChart("opennms-rules!");
        } catch (IllegalArgumentException e) {
            log().debug("testGetChartWithInvalidChartName: Good, this test is working.");
        }
        assertNull(chart);
    }

    public void testGetChartAsFileOutputStream() throws FileNotFoundException, IOException, SQLException, ValidationException, MarshalException {
        OutputStream stream = new FileOutputStream("//tmp//sample-bar-chart.png");
        ChartUtils.getBarChart("sample-bar-chart", stream);
        stream.close();
    }
    
    public void testGetChartAsBufferedImage() throws MarshalException, ValidationException, IOException, SQLException {
        BufferedImage bi = ChartUtils.getChartAsBufferedImage("sample-bar-chart");
        assertEquals(300, bi.getHeight());
    }

    @SuppressWarnings("deprecation")
    private void initalizeChartFactory() throws MarshalException, ValidationException, IOException {
        ChartConfigFactory.setInstance(new ChartConfigFactory());
        Reader rdr = new StringReader(CHART_CONFIG);
        ChartConfigFactory.parseXml(rdr);
        rdr.close();        
//        m_config = ChartConfigFactory.getInstance().getConfiguration();
    }

    private Category log() {
        return ThreadCategory.getInstance(getClass());
    }

}
