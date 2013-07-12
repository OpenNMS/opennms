/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.charts;

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.beans.PropertyVetoException;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Iterator;
import java.util.List;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.ExtendedCategoryAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.labels.CategoryItemLabelGenerator;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.jdbc.JDBCCategoryDataset;
import org.opennms.core.db.DataSourceFactory;
import org.opennms.netmgt.config.ChartConfigFactory;
import org.opennms.netmgt.config.charts.BarChart;
import org.opennms.netmgt.config.charts.Blue;
import org.opennms.netmgt.config.charts.Green;
import org.opennms.netmgt.config.charts.ImageSize;
import org.opennms.netmgt.config.charts.Red;
import org.opennms.netmgt.config.charts.Rgb;
import org.opennms.netmgt.config.charts.SeriesDef;
import org.opennms.netmgt.config.charts.SubTitle;
import org.opennms.netmgt.config.charts.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>ChartUtils class.</p>
 *
 * @author <a href="david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public abstract class ChartUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(ChartUtils.class);
    
    /**
     * Use this it initialize required factories so that the WebUI doesn't
     * have to.  Can't wait for Spring.
     */
    static {
        try {
            DataSourceFactory.init();
            ChartConfigFactory.init();
        } catch (MarshalException e) {
            LOG.error("static initializer: Error marshalling chart configuration", e);
        } catch (ValidationException e) {
            LOG.error("static initializer: Error validating chart configuration.", e);
        } catch (FileNotFoundException e) {
            LOG.error("static initializer: Error finding chart configuration.", e);
        } catch (IOException e) {
            LOG.error("static initializer: IO error while marshalling chart configuration file.", e);
        } catch (ClassNotFoundException e) {
            LOG.error("static initializer: Error initializing database connection factory.", e);
        } catch (PropertyVetoException e) {
            LOG.error("static initializer: Error initializing database connection factory.", e);
        } catch (SQLException e) {
            LOG.error("static initializer: Error initializing database connection factory.", e);
        }
        // XXX why don't we throw an exception here or something?
    }

    /**
     * This method will returns a JFreeChart bar chart constructed based on XML configuration.
     *
     * @param chartName Name specified in chart-configuration.xml
     * @return <code>JFreeChart</code> constructed from the chartName
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static JFreeChart getBarChart(String chartName) throws MarshalException, ValidationException, IOException, SQLException {

        //ChartConfigFactory.reload();
        
        BarChart chartConfig = null;
        chartConfig = getBarChartConfigByName(chartName);
        
        if (chartConfig == null) {
            throw new IllegalArgumentException("getBarChart: Invalid chart name.");
        }
        
        DefaultCategoryDataset baseDataSet = buildCategoryDataSet(chartConfig);        
        JFreeChart barChart = createBarChart(chartConfig, baseDataSet);
        addSubTitles(chartConfig, barChart);

        String subLabelClass = chartConfig.getSubLabelClass();
        if(subLabelClass != null) {
            addSubLabels(barChart, subLabelClass);
        }
        

        customizeSeries(barChart, chartConfig);

        return barChart;
        
    }

    /**
     * @param barChart TODO
     * @param subLabelClass
     */
    private static void addSubLabels(JFreeChart barChart, String subLabelClass) {
        ExtendedCategoryAxis subLabels;
        CategoryPlot plot = barChart.getCategoryPlot();
        try {
            subLabels = (ExtendedCategoryAxis) Class.forName(subLabelClass).newInstance();
            List<?> cats = plot.getCategories();
            for(int i=0; i<cats.size(); i++) {
                subLabels.addSubLabel((Comparable<?>)cats.get(i), cats.get(i).toString());
            }
            plot.setDomainAxis(subLabels);
        } catch (InstantiationException e) {
            LOG.error("getBarChart: Couldn't instantiate configured CategorySubLabels class: {}", subLabelClass, e);
        } catch (IllegalAccessException e) {
            LOG.error("getBarChart: Couldn't instantiate configured CategorySubLabels class: {}", subLabelClass, e);
        } catch (ClassNotFoundException e) {
            LOG.error("getBarChart: Couldn't instantiate configured CategorySubLabels class: {}", subLabelClass, e);
        }
    }

    /**
     * @param barChart TODO
     * @param chartConfig
     */
    private static void customizeSeries(JFreeChart barChart, BarChart chartConfig) {
        
        /*
         * Set the series colors and labels
         */
        CategoryItemLabelGenerator itemLabelGenerator = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0"));
        SeriesDef[] seriesDefs = chartConfig.getSeriesDef();
        CustomSeriesColors seriesColors = null;
        
        if (chartConfig.getSeriesColorClass() != null) {
            try {
                seriesColors = (CustomSeriesColors) Class.forName(chartConfig.getSeriesColorClass()).newInstance();
            } catch (InstantiationException e) {
                LOG.error("getBarChart: Couldn't instantiate configured CustomSeriesColors class: {}", seriesColors, e);
            } catch (IllegalAccessException e) {
                LOG.error("getBarChart: Couldn't instantiate configured CustomSeriesColors class: {}", seriesColors, e);
            } catch (ClassNotFoundException e) {
                LOG.error("getBarChart: Couldn't instantiate configured CustomSeriesColors class: {}", seriesColors, e);
            }
        }

        for (int i = 0; i < seriesDefs.length; i++) {
            SeriesDef seriesDef = seriesDefs[i];
            Paint paint = Color.BLACK;
            if (seriesColors != null) {
                Comparable<?> cat = (Comparable<?>)((BarRenderer)barChart.getCategoryPlot().getRenderer()).getPlot().getCategories().get(i);
                paint = seriesColors.getPaint(cat);
            } else {
                Rgb rgb = seriesDef.getRgb();
                paint = new Color(rgb.getRed().getRgbColor(), rgb.getGreen().getRgbColor(), rgb.getBlue().getRgbColor());
            }
            ((BarRenderer)barChart.getCategoryPlot().getRenderer()).setSeriesPaint(i, paint);
            ((BarRenderer)barChart.getCategoryPlot().getRenderer()).setSeriesItemLabelsVisible(i, seriesDef.getUseLabels());
            ((BarRenderer)barChart.getCategoryPlot().getRenderer()).setSeriesItemLabelGenerator(i, itemLabelGenerator);
        }
    }

    /**
     * @param chartConfig
     * @param barChart
     */
    private static void addSubTitles(BarChart chartConfig, JFreeChart barChart) {
        Iterator<SubTitle> it;
        /*
         * Add subtitles.
         */
        for (it = chartConfig.getSubTitleCollection().iterator(); it.hasNext();) {
            SubTitle subTitle = (SubTitle) it.next();
            Title title = subTitle.getTitle();
            String value = title.getValue();
            barChart.addSubtitle(new TextTitle(value));
        }
    }

    /**
     * @param chartConfig
     * @param baseDataSet
     * @return
     */
    private static JFreeChart createBarChart(BarChart chartConfig, DefaultCategoryDataset baseDataSet) {
        PlotOrientation po = (chartConfig.getPlotOrientation() == "horizontal" ? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);        
        JFreeChart barChart = null;
        if ("3d".equalsIgnoreCase(chartConfig.getVariation())) {
            barChart = ChartFactory.createBarChart3D(chartConfig.getTitle().getValue(),
                    chartConfig.getDomainAxisLabel(),
                    chartConfig.getRangeAxisLabel(),
                    baseDataSet,
                    po,
                    chartConfig.getShowLegend(),
                    chartConfig.getShowToolTips(),
                    chartConfig.getShowUrls());
        } else {
            barChart = ChartFactory.createBarChart(chartConfig.getTitle().getValue(),
                    chartConfig.getDomainAxisLabel(),
                    chartConfig.getRangeAxisLabel(),
                    baseDataSet,
                    po,
                    chartConfig.getShowLegend(),
                    chartConfig.getShowToolTips(),
                    chartConfig.getShowUrls());
        }
        
        // Create a bit more headroom for value labels than is allowed for by the default 0.05 upper margin
        ValueAxis rangeAxis = barChart.getCategoryPlot().getRangeAxis();
        if (rangeAxis.getUpperMargin() < 0.1) {
            rangeAxis.setUpperMargin(0.1);
        }
        
        return barChart;
    }

    /**
     * @param chartConfig
     * @param baseDataSet
     * @throws SQLException
     */
    private static DefaultCategoryDataset buildCategoryDataSet(BarChart chartConfig) throws SQLException {
        DefaultCategoryDataset baseDataSet = new DefaultCategoryDataset();
        /*
         * Configuration can contain more than one series.  This loop adds
         * single series data sets returned from sql query to a base data set
         * to be displayed in a the chart. 
         */
        Connection conn = null;
        try {
            conn = DataSourceFactory.getInstance().getConnection();
            Iterator<SeriesDef> it = chartConfig.getSeriesDefCollection().iterator();
            while (it.hasNext()) {
                SeriesDef def = it.next();
                JDBCCategoryDataset dataSet = new JDBCCategoryDataset(conn, def.getJdbcDataSet().getSql());
            
                for (int i = 0; i < dataSet.getRowCount(); i++) {
                    for (int j = 0; j < dataSet.getColumnCount(); j++) {
                        baseDataSet.addValue(dataSet.getValue(i, j), def.getSeriesName(), dataSet.getColumnKey(j));
                    }
                }
            }
        } finally {
            if (conn != null) {
                conn.close();
            }
        }
        return baseDataSet;
    }
    
    /**
     * Helper method that returns the JFreeChart to an output stream written in JPEG format.
     *
     * @param chartName a {@link java.lang.String} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
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
    
    /**
     * Helper method that returns the JFreeChart to an output stream written in JPEG format.
     *
     * @param chartName a {@link java.lang.String} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static void getBarChartPNG(String chartName, OutputStream out) throws MarshalException, ValidationException, IOException, SQLException {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        if(chartConfig.getChartBackgroundColor() != null) {
            setChartBackgroundColor(chartConfig, chart);
        }
        
        if(chartConfig.getPlotBackgroundColor() !=  null) {
            setPlotBackgroundColor(chartConfig, chart);
        }
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
        
        ChartUtilities.writeChartAsPNG(out, chart, hzPixels, vtPixels, false, 6);
        
    }

    private static void setPlotBackgroundColor(BarChart chartConfig,
            JFreeChart chart) {
        Red red = chartConfig.getPlotBackgroundColor().getRgb().getRed();
        Blue blue = chartConfig.getPlotBackgroundColor().getRgb().getBlue();
        Green green = chartConfig.getPlotBackgroundColor().getRgb().getGreen();
        
        chart.getPlot().setBackgroundPaint(new Color(red.getRgbColor(), green.getRgbColor(), blue.getRgbColor()));
    }

    private static void setChartBackgroundColor(BarChart chartConfig,
            JFreeChart chart) {
        Red red = chartConfig.getChartBackgroundColor().getRgb().getRed();
        Blue blue = chartConfig.getChartBackgroundColor().getRgb().getBlue();
        Green green = chartConfig.getChartBackgroundColor().getRgb().getGreen();
        chart.setBackgroundPaint(new Color(red.getRgbColor(), green.getRgbColor(), blue.getRgbColor()));
    }
    
    /**
     * Helper method that returns the JFreeChart as a PNG byte array.
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a byte array
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
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
    
    /**
     * Helper method used to return a JFreeChart as a buffered Image.
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a <code>BufferedImage</code>
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
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
    
    /**
     * Helper method used to retrieve the XML defined BarChart (castor class)
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a derived Castor class: BarChart
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     * @throws java.io.IOException if any.
     */
    public static BarChart getBarChartConfigByName(String chartName) throws MarshalException, ValidationException, IOException {
        Iterator<BarChart> it = getChartCollectionIterator();
        BarChart chart = null;
        while (it.hasNext()) {
            chart = (BarChart)it.next();
            if (chart.getName().equals(chartName))
                return chart;
        }
        return null;
    }
    
    /**
     * Helper method used to fetch an Iterator for all defined Charts
     *
     * @return <code>BarChart</code> Iterator
     * @throws java.io.IOException if any.
     * @throws org.exolab.castor.xml.MarshalException if any.
     * @throws org.exolab.castor.xml.ValidationException if any.
     */
    public static Iterator<BarChart> getChartCollectionIterator() throws IOException, MarshalException, ValidationException {
        return ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection().iterator();
    }
    
}
