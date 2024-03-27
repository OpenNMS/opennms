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
package org.opennms.web.charts;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Objects;

import javax.sql.DataSource;

import org.jfree.chart.JFreeChart;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.charts.BarChart;

public class ChartUtilsIT {
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

    @Before
    public void setUp() throws Exception {
        System.setProperty("java.awt.headless", "true");

        final DataSource ds = createMockDataSource();

        DataSourceFactory.setInstance(ds);
        initializeChartFactory(CHART_CONFIG);
    }

    private DataSource createMockDataSource() throws SQLException {
        final DataSource ds = Mockito.mock(DataSource.class);
        final Connection conn = Mockito.mock(Connection.class);
        final Statement stmt = Mockito.mock(Statement.class);
        final ResultSet rs = Mockito.mock(ResultSet.class);
        final ResultSetMetaData rsmd = Mockito.mock(ResultSetMetaData.class);
        when(ds.getConnection()).thenReturn(conn);
        when(conn.createStatement()).thenReturn(stmt);
        when(stmt.executeQuery(any())).thenReturn(rs);
        when(rs.getMetaData()).thenReturn(rsmd);
        when(rsmd.getColumnCount()).thenReturn(5);
        return ds;
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testGetBarChartConfig() throws Exception {
        assertNotNull("sample bar chart should not be null", ChartUtils.getBarChartConfigByName("sample-bar-chart"));
        assertEquals("sample bar chart config should be a BarChart class", ChartUtils.getBarChartConfigByName("sample-bar-chart").getClass(), BarChart.class);
    }

    @Test
    public void testGetBarChartAsPNGByteArray() throws Exception {
        final byte[] png = ChartUtils.getBarChartAsPNGByteArray("sample-bar-chart");
        assertNotNull("sample bar chart png should not be null", png);
        assertTrue("sample bar chart png should not be empty", png.length > 0);
    }

    @Test
    public void testGetBarChart() throws Exception {
        JFreeChart barChart = ChartUtils.getBarChart("sample-bar-chart");
        assertNotNull("sample bar chart should not be null", barChart);
        //SubTitle count includes "LegendTitle"
        assertEquals("sample chart should have the right number of sub-titles", 2, barChart.getSubtitleCount());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetChartWithInvalidChartName() throws Exception {
        ChartUtils.getBarChart("opennms-rules!");
    }

    @Test
    public void testGetChartAsFileOutputStream() throws Exception {
        final File tempFile = File.createTempFile("sample-bar-chart", "png");
        OutputStream stream = new FileOutputStream(tempFile);
        ChartUtils.getBarChart("sample-bar-chart", stream);
        stream.close();
        assertTrue("file should not be empty", tempFile.length() > 0);
    }

    @Test
    public void testGetChartAsBufferedImage() throws Exception {
        BufferedImage bi = ChartUtils.getChartAsBufferedImage("sample-bar-chart");
        assertEquals("sample chart should be the correct height", 300, bi.getHeight());
    }

    @Test
    public void testShippedConfig() throws Exception {
        final String chartText = Files.readString(Paths.get("..", "..", "opennms-base-assembly", "src", "main", "filtered", "etc", "chart-configuration.xml"));
        initializeChartFactory(chartText);

        byte[] png = ChartUtils.getBarChartAsPNGByteArray("sample-bar-chart");
        assertNotNull("bar chart png should not be null", png);
        assertTrue("bar chart png should not be empty", png.length > 0);

        png = ChartUtils.getBarChartAsPNGByteArray("sample-bar-chart2");
        assertNotNull("bar chart png should not be null", png);
        assertTrue("bar chart png should not be empty", png.length > 0);

        png = ChartUtils.getBarChartAsPNGByteArray("sample-bar-chart3");
        assertNotNull("bar chart png should not be null", png);
        assertTrue("bar chart png should not be empty", png.length > 0);
    }

    private static void initializeChartFactory(final String chartText) throws IOException {
        final String config = Objects.requireNonNull(chartText);
        ChartConfigFactory.setInstance(new ChartConfigFactory());
        ByteArrayInputStream rdr = new ByteArrayInputStream(config.getBytes(StandardCharsets.UTF_8));
        ChartConfigFactory.parseXml(rdr);
        rdr.close();        
        //        m_config = ChartConfigFactory.getInstance().getConfiguration();
    }

}
