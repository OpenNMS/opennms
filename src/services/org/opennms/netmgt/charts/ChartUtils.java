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

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Iterator;

import org.apache.log4j.Category;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.opennms.core.utils.ThreadCategory;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.DatabaseConnectionFactory;
import org.opennms.netmgt.config.charts.BarChart;
import org.opennms.netmgt.config.charts.ImageSize;
import org.opennms.netmgt.config.charts.Rgb;
import org.opennms.netmgt.config.charts.SeriesDef;
import org.opennms.netmgt.config.charts.SubTitle;
import org.opennms.netmgt.config.charts.Title;

/**
 * @author david
 *
 */
public class ChartUtils {
    
    static {
        try {
            DatabaseConnectionFactory.init();
            ChartConfigFactory.init();
        } catch (MarshalException e) {
            log().error("static initializer: Error marshalling chart configuration. "+e);
        } catch (ValidationException e) {
            log().error("static initializer: Error validating chart configuration. "+e);
        } catch (FileNotFoundException e) {
            log().error("static initializer: Error finding chart configuration. "+e);
        } catch (IOException e) {
            log().error("static initializer: IO error while marshalling chart configuration file. "+e);
        } catch (ClassNotFoundException e) {
            log().error("static initializer: Error initializing database connection factory. "+e);
        }
    }

    private static Category log() {
        return ThreadCategory.getInstance(ChartUtils.class);
    }

    public static JFreeChart getBarChart(String chartName) throws MarshalException, ValidationException, IOException, SQLException {

        BarChart chartConfig = null;
        Connection conn = null;
        CategoryDataset dataSet = null;
        chartConfig = getBarChartConfigByName(chartName);
        
        if (chartConfig == null) {
            throw new IllegalArgumentException("getBarChart: Invalid chart name.");
        }
        
        conn = DatabaseConnectionFactory.getInstance().getConnection();
        dataSet = new JDBCCategoryDataset(conn, chartConfig.getJdbcDataSet().getSql());
        
        PlotOrientation po = (chartConfig.getPlotOrientation() == "horizontal" ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);
        
        JFreeChart barChart = ChartFactory.createBarChart(chartConfig.getTitle().getValue(),
                chartConfig.getDomainAxisLabel(),
                chartConfig.getRangeAxisLabel(),
                dataSet,
                po,
                chartConfig.getShowLegend(),
                chartConfig.getShowToolTips(),
                chartConfig.getShowUrls());
        
        for (Iterator it = chartConfig.getSubTitleCollection().iterator(); it.hasNext();) {
            SubTitle subTitle = (SubTitle) it.next();
            Title title = subTitle.getTitle();
            String value = title.getValue();
            barChart.addSubtitle(new TextTitle(value));
        }
        

        CategoryPlot plot = barChart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer)plot.getRenderer();
        

        SeriesDef[] seriesDefs = chartConfig.getSeriesDef();
        for (int i = 0; i < seriesDefs.length; i++) {
            SeriesDef seriesDef = seriesDefs[i];
            Rgb rgb = seriesDef.getRgb();
            Paint paint = new Color(rgb.getRed().getRgbColor(), rgb.getBlue().getRgbColor(), rgb.getGreen().getRgbColor());
            renderer.setSeriesPaint(i, paint);
        }

        return barChart;
        
    }
    
    public static void getBarChart(String chartName, OutputStream out) throws MarshalException, ValidationException, IOException, SQLException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = 400;
            vtPixels = 400;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }
        
        ChartUtilities.writeChartAsJPEG(out, chart, hzPixels, vtPixels);
        
    }
    
    public static byte[] getBarChartAsPNGByteArray(String chartName) throws MarshalException, ValidationException, IOException, SQLException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = 400;
            vtPixels = 400;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }
        return ChartUtilities.encodeAsPNG(chart.createBufferedImage(hzPixels, vtPixels));
    }
    
    public static BufferedImage getChartAsBufferedImage(String chartName) throws MarshalException, ValidationException, IOException, SQLException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = 400;
            vtPixels = 400;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }

        return chart.createBufferedImage(hzPixels, vtPixels);
        
    }
    
    public static BarChart getBarChartConfigByName(String chartName) throws MarshalException, ValidationException, IOException {
        Iterator it = getChartCollectionIterator();
        BarChart chart = null;
        while (it.hasNext()) {
            chart = (BarChart)it.next();
            if (chart.getName().equals(chartName))
                return chart;
        }
        return null;
    }
    
    public static Iterator getChartCollectionIterator() throws IOException, MarshalException, ValidationException {

        return ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection().iterator();
    }
    
}
