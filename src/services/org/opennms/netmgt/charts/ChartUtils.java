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

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.charts.BarChart;

/**
 * @author david
 *
 */
public class ChartUtils {

    public static JFreeChart getBarChart(String chartName) throws MarshalException, ValidationException, IOException, SQLException {

        BarChart chart = null;
        Connection conn = null;
        CategoryDataset dataSet = null;
        chart = getBarChartConfigByName(chartName);
        conn = DatabaseConnectionFactory.getInstance().getConnection();
        dataSet = new JDBCCategoryDataset(conn, chart.getJdbcDataSet().getSql());
        
        PlotOrientation po = (chart.getPlotOrientation() == "horizontal" ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        
        JFreeChart barChart = ChartFactory.createBarChart(chart.getTitle().getValue(),
                chart.getDomainAxisLabel(),
                chart.getRangeAxisLabel(),
                dataSet,
                po,
                chart.getShowLegend(),
                chart.getShowToolTips(),
                chart.getShowUrls());
        
        return barChart;
        
    }
    
    public static void getBarChart(String chartName, OutputStream out) throws MarshalException, ValidationException, IOException, SQLException {
        BarChart barChart = getBarChartConfigByName(chartName);
        ChartUtilities.writeChartAsPNG(out, getBarChart(chartName), barChart.getImageSize().getHzSize().getPixels(), barChart.getImageSize().getVtSize().getPixels());
    }
    
    public static BarChart getBarChartConfigByName(String chartName) throws MarshalException, ValidationException, IOException {
        Collection charts = ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection();
        Iterator it = charts.iterator();
        BarChart chart = null;
        while (it.hasNext()) {
            chart = (BarChart)it.next();
            if (chart.getName().equals(chartName))
                return chart;
        }
        return null;
    }
    
    public static void getChartAsStream(String chartName, OutputStream out) {
       
    }
}
