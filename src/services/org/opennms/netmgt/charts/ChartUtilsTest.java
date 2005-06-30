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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.sql.SQLException;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.JFreeChart;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.charts.BarChart;
import org.opennms.netmgt.config.charts.ChartConfiguration;
import org.opennms.netmgt.mock.OpenNMSTestCase;



public class ChartUtilsTest extends OpenNMSTestCase {
    
    private static final String CHART_CONFIG = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + 
            "<tns:chart-configuration xmlns:tns=\"http://xmlns.opennms.org/xsd/config/charts\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://xmlns.opennms.org/xsd/config/charts ../src/services/org/opennms/netmgt/config/chart-configuration.xsd \">\n" + 
            "\n" + 
            "  <tns:bar-chart name=\"sample-bar-chart\" variation=\"2d\" domain-axis-label=\"domain label\" show-legend=\"true\" plot-orientation=\"vertical\" draw-bar-outline=\"true\" range-axis-label=\"range label\" show-urls=\"false\"\n" + 
            "      show-tool-tips=\"false\">\n" + 
            "      \n" + 
            "      <tns:jdbc-data-set db-name=\"opennms\" sql=\"select severity, count(*) as count from alarms group by severity order by count desc\" />\n" + 
            "      \n" + 
            "      <tns:title font=\"SansSerif\" style=\"\" value=\"Sample Bar Chart\" pitch=\"12\" />\n" + 
            "      \n" + 
            "      <tns:sub-title position=\"top\" horizontal-alignment=\"center\">\n" + 
            "          <tns:title font=\"SansSerif\" style=\"\" value=\"Sample SubTitle\" pitch=\"10\" />\n" + 
            "      </tns:sub-title>\n" + 
            "      \n" + 
            "      <tns:grid-lines visible=\"true\">\n" + 
            "          <tns:rgb>\n" + 
            "              <tns:red>\n" + 
            "                  <tns:rgb-color>255</tns:rgb-color>\n" + 
            "              </tns:red>\n" + 
            "              <tns:green>\n" + 
            "                  <tns:rgb-color>255</tns:rgb-color>\n" + 
            "              </tns:green>\n" + 
            "              <tns:blue>\n" + 
            "                  <tns:rgb-color>255</tns:rgb-color>\n" + 
            "              </tns:blue>\n" + 
            "          </tns:rgb>\n" + 
            "      </tns:grid-lines>\n" + 
            "      \n" + 
            "  </tns:bar-chart>\n" + 
            "</tns:chart-configuration>\n" + 
            "";
    private ChartConfiguration m_config;

    protected void setUp() throws Exception {
        super.setUp();
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
        assertNotNull(ChartUtils.getBarChart("sample-bar-chart"));
        assertTrue(ChartUtils.getBarChart("sample-bar-chart").getSubtitleCount() == 1);
        
    }

    private void initalizeChartFactory() throws MarshalException, ValidationException, IOException {
        ChartConfigFactory.setInstance(new ChartConfigFactory());
        Reader rdr = new StringReader(CHART_CONFIG);
        ChartConfigFactory.parseXml(rdr);
        rdr.close();        
        m_config = ChartConfigFactory.getInstance().getConfiguration();
    }
    
    public void testGetChart() {
        
        JFreeChart chart = null;
        try {
            chart = ChartUtils.getBarChart("bar-chart-test");
        } catch (MarshalException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (ValidationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (SQLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        assertNotNull(chart);
    }

    public void testGetChartAsStream() {
    }

}
