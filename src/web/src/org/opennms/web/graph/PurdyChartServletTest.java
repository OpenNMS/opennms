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

package org.opennms.web.graph;

import java.io.File;
import java.io.IOException;

import junit.framework.TestCase;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
public class PurdyChartServletTest extends TestCase {

    protected void setUp() throws Exception {
        super.setUp();
    }

    protected void tearDown() throws Exception {
        super.tearDown();
    }

    public void testSampleSeverityChart() throws IOException {
        
        JFreeChart chart = PurdyChartServletTest.sampleSeverityChart();
        
        CategoryPlot cp = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer)cp.getRenderer();
        
/*        renderer.setSeriesPaint(0, Color.red);
        renderer.setSeriesPaint(1, Color.orange);
        renderer.setSeriesPaint(2, Color.yellow);
        renderer.setSeriesPaint(3, Color.cyan);
        renderer.setSeriesPaint(4, Color.green);
*/        
        
        ChartUtilities.saveChartAsPNG(new File("//tmp//chart.png"), chart, 400, 300);
        
    }

    public static JFreeChart sampleSeverityChart() {
        DefaultCategoryDataset dataSet = new DefaultCategoryDataset();
        dataSet.addValue(10.0, "Alarms", "Critical");
        dataSet.addValue(15.0, "Events", "Critical");
        dataSet.addValue(20.0, "Alarms", "Major");
        dataSet.addValue(25.0, "Events", "Major");
        dataSet.addValue(30.0, "Alarms", "Minor");
        dataSet.addValue(35.0, "Events", "Minor");
        dataSet.addValue(10.0, "Alarms", "Warning");
        dataSet.addValue(15.0, "Events", "Warning");
        dataSet.addValue(10.0, "Alarms", "Normal");
        dataSet.addValue(150.0, "Events", "Normal");
        
        JFreeChart chart = ChartFactory.createBarChart("Event/Alarm Status", "Severity", "Count", dataSet, PlotOrientation.VERTICAL, true, true, false);
        return chart;
    }

}
