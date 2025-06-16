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

import java.awt.Color;
import java.awt.Paint;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import org.jfree.chart.ChartFactory;
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
import org.opennms.netmgt.config.charts.ChartBackgroundColor;
import org.opennms.netmgt.config.charts.Green;
import org.opennms.netmgt.config.charts.ImageSize;
import org.opennms.netmgt.config.charts.PlotBackgroundColor;
import org.opennms.netmgt.config.charts.Red;
import org.opennms.netmgt.config.charts.Rgb;
import org.opennms.netmgt.config.charts.SeriesDef;
import org.opennms.netmgt.config.charts.SubTitle;
import org.opennms.netmgt.config.charts.Title;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class ChartUtils {
    private static final Logger LOG = LoggerFactory.getLogger(ChartUtils.class);
    
    private static final int DEFAULT_IMAGE_SIZE = 400;
    private static final int PNG_COMPRESSION_LEVEL = 6;
    private static final double RANGE_MARGIN = 0.1;
    
    /**
     * Use this it initialize required factories so that the WebUI doesn't
     * have to.  Can't wait for Spring.
     */
    static {
        try {
            ChartConfigFactory.init();
        } catch (@SuppressWarnings("java:S2139") IOException e) {
            LOG.error("static initializer: Error initializing chart configuration.", e);
            throw new IllegalStateException("Error initializing chart configuration.", e);
        } 
    }

    private ChartUtils() {}

    /**
     * This method will returns a JFreeChart bar chart constructed based on XML configuration.
     *
     * @param chartName Name specified in chart-configuration.xml
     * @return <code>JFreeChart</code> constructed from the chartName
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static JFreeChart getBarChart(String chartName) throws ChartException {

        BarChart chartConfig = null;
        chartConfig = getBarChartConfigByName(chartName);
        
        if (chartConfig == null) {
            throw new IllegalArgumentException("getBarChart: Invalid chart name.");
        }
        
        DefaultCategoryDataset baseDataSet = buildCategoryDataSet(chartConfig);        
        JFreeChart barChart = createBarChart(chartConfig, baseDataSet);
        addSubTitles(chartConfig, barChart);

        final var subLabelClassOptional = chartConfig.getSubLabelClass();
        if (subLabelClassOptional.isPresent()) {
            addSubLabels(barChart, subLabelClassOptional.get());
        }        

        customizeSeries(barChart, chartConfig);

        return barChart;
        
    }

    private static void addSubLabels(final JFreeChart barChart, final String subLabelClass) {
        ExtendedCategoryAxis subLabels;
        CategoryPlot plot = barChart.getCategoryPlot();
        try {
            final var clazz = Class.forName(subLabelClass);
            final var constructor = clazz.getDeclaredConstructor(String.class);
            subLabels = (ExtendedCategoryAxis) constructor.newInstance((String)null);
            List<?> cats = plot.getCategories();
            for(int i=0; i<cats.size(); i++) {
                subLabels.addSubLabel((Comparable<?>)cats.get(i), cats.get(i).toString());
            }
            plot.setDomainAxis(subLabels);
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            LOG.error("getBarChart: Couldn't instantiate configured CategorySubLabels class: {}", subLabelClass, e);
        }  
    }

    private static void customizeSeries(JFreeChart barChart, BarChart chartConfig) {
        
        /*
         * Set the series colors and labels
         */
        CategoryItemLabelGenerator itemLabelGenerator = new StandardCategoryItemLabelGenerator("{2}", new DecimalFormat("0"));
        SeriesDef[] seriesDefs = chartConfig.getSeriesDef();
        CustomSeriesColors seriesColors = null;
        
        final var seriesColorClassOpt = chartConfig.getSeriesColorClass();
        if (seriesColorClassOpt.isPresent()) {
            try {
                final var clazz = Class.forName(seriesColorClassOpt.get());
                final var constructor = clazz.getDeclaredConstructor();
                seriesColors = (CustomSeriesColors) constructor.newInstance();
            } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                LOG.error("getBarChart: Couldn't instantiate configured CustomSeriesColors class: {}", seriesColors, e);
            }  
        }

        for (int i = 0; i < seriesDefs.length; i++) {
            SeriesDef seriesDef = seriesDefs[i];
            final Paint paint;
            if (seriesColors != null) {
                Comparable<?> cat = (Comparable<?>)((BarRenderer)barChart.getCategoryPlot().getRenderer()).getPlot().getCategories().get(i);
                paint = seriesColors.getPaint(cat);
            } else {
                final var rgbOptional = seriesDef.getRgb();
                if (rgbOptional.isPresent()) {
                    final Rgb rgb = rgbOptional.get();
                    paint = new Color(rgb.getRed().getRgbColor(), rgb.getGreen().getRgbColor(), rgb.getBlue().getRgbColor());
                } else {
                    paint = Color.BLACK;
                }
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
            SubTitle subTitle = it.next();
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
        PlotOrientation po = ("horizontal".equals(chartConfig.getPlotOrientation().orElse(null))? PlotOrientation.HORIZONTAL : PlotOrientation.VERTICAL);        
        if ("3d".equalsIgnoreCase(chartConfig.getVariation().orElse(null))) {
            LOG.warn("3d charts no longer supported, using standard JFreeChart bar chart");
        }
        JFreeChart barChart = ChartFactory.createBarChart(chartConfig.getTitle().getValue(),
            chartConfig.getDomainAxisLabel(),
            chartConfig.getRangeAxisLabel(),
            baseDataSet,
            po,
            chartConfig.getShowLegend(),
            chartConfig.getShowToolTips(),
            chartConfig.getShowUrls());
        
        // Create a bit more headroom for value labels than is allowed for by the default 0.05 upper margin
        ValueAxis rangeAxis = barChart.getCategoryPlot().getRangeAxis();
        if (rangeAxis.getUpperMargin() < RANGE_MARGIN) {
            rangeAxis.setUpperMargin(RANGE_MARGIN);
        }
        
        return barChart;
    }

    /**
     * @param chartConfig
     * @param baseDataSet
     * @throws SQLException
     */
    private static DefaultCategoryDataset buildCategoryDataSet(BarChart chartConfig) throws ChartException {
        DefaultCategoryDataset baseDataSet = new DefaultCategoryDataset();
        /*
         * Configuration can contain more than one series.  This loop adds
         * single series data sets returned from sql query to a base data set
         * to be displayed in a the chart. 
         */
        try (
                final Connection conn = DataSourceFactory.getInstance().getConnection();
        ) {
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
        } catch (final SQLException e) {
            throw new ChartException("error while building category dataset", e);
        }
        return baseDataSet;
    }
    
    /**
     * Helper method that returns the JFreeChart to an output stream written in JPEG format.
     *
     * @param chartName a {@link java.lang.String} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static void getBarChart(final String chartName, final OutputStream out) throws ChartException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        if (chartConfig == null) {
            throw new IllegalStateException("unable to get chart config from name: " + chartName);
        }
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig == null? null : chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = DEFAULT_IMAGE_SIZE;
            vtPixels = DEFAULT_IMAGE_SIZE;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }
        
        try {
            org.jfree.chart.ChartUtils.writeChartAsJPEG(out, chart, hzPixels, vtPixels);
        } catch (final IOException e) {
            throw new ChartException("failed to generate JPEG", e);
        }
        
    }
    
    /**
     * Helper method that returns the JFreeChart to an output stream written in JPEG format.
     *
     * @param chartName a {@link java.lang.String} object.
     * @param out a {@link java.io.OutputStream} object.
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static void getBarChartPNG(String chartName, OutputStream out) throws ChartException {
        ChartFactory.setChartTheme(StandardChartTheme.createLegacyTheme());
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        if (chartConfig == null) {
            throw new ChartException("failed to determine chart config for " + chartName);
        }
        if(chartConfig.getChartBackgroundColor().isPresent()) {
            setChartBackgroundColor(chartConfig, chart);
        }
        if(chartConfig.getPlotBackgroundColor().isPresent()) {
            setPlotBackgroundColor(chartConfig, chart);
        }
        ImageSize imageSize = chartConfig == null? null : chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = DEFAULT_IMAGE_SIZE;
            vtPixels = DEFAULT_IMAGE_SIZE;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }
        
        try {
            org.jfree.chart.ChartUtils.writeChartAsPNG(out, chart, hzPixels, vtPixels, false, PNG_COMPRESSION_LEVEL);
        } catch (final IOException e) {
            throw new ChartException("failed to generate PNG", e);
        }
        
    }

    private static void setPlotBackgroundColor(BarChart chartConfig,
            JFreeChart chart) {
        final var backgroundColorOpt = chartConfig.getPlotBackgroundColor();
        if (backgroundColorOpt.isPresent()) {
            final PlotBackgroundColor bgColor = backgroundColorOpt.get();
            final Optional<Rgb> rgb = bgColor.getRgb();
            if (rgb.isPresent()) {
                final Red red = rgb.get().getRed();
                final Blue blue = rgb.get().getBlue();
                final Green green = rgb.get().getGreen();
                
                chart.getPlot().setBackgroundPaint(new Color(red.getRgbColor(), green.getRgbColor(), blue.getRgbColor()));
            }
        }
    }

    private static void setChartBackgroundColor(BarChart chartConfig,
            JFreeChart chart) {
        final var backgroundColorOpt = chartConfig.getChartBackgroundColor();
        if (backgroundColorOpt.isPresent()) {
            final ChartBackgroundColor bgColor = backgroundColorOpt.get();
            Red red = bgColor.getRgb().getRed();
            Blue blue = bgColor.getRgb().getBlue();
            Green green = bgColor.getRgb().getGreen();
            chart.setBackgroundPaint(new Color(red.getRgbColor(), green.getRgbColor(), blue.getRgbColor()));
        }
    }
    
    /**
     * Helper method that returns the JFreeChart as a PNG byte array.
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a byte array
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static byte[] getBarChartAsPNGByteArray(String chartName) throws ChartException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig == null? null : chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = DEFAULT_IMAGE_SIZE;
            vtPixels = DEFAULT_IMAGE_SIZE;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }
        try {
            return org.jfree.chart.ChartUtils.encodeAsPNG(chart.createBufferedImage(hzPixels, vtPixels));
        } catch (final IOException e) {
            throw new ChartException("failed to generate PNG", e);
        }
    }
    
    /**
     * Helper method used to return a JFreeChart as a buffered Image.
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a <code>BufferedImage</code>
     * @throws java.io.IOException if any.
     * @throws java.sql.SQLException if any.
     */
    public static BufferedImage getChartAsBufferedImage(String chartName) throws ChartException {
        BarChart chartConfig = getBarChartConfigByName(chartName);
        JFreeChart chart = getBarChart(chartName);
        ImageSize imageSize = chartConfig == null? null : chartConfig.getImageSize();
        int hzPixels;
        int vtPixels;
        
        if (imageSize == null) {
            hzPixels = DEFAULT_IMAGE_SIZE;
            vtPixels = DEFAULT_IMAGE_SIZE;
        } else {            
            hzPixels = imageSize.getHzSize().getPixels();
            vtPixels = imageSize.getVtSize().getPixels();
        }

        return chart.createBufferedImage(hzPixels, vtPixels);
        
    }
    
    /**
     * Helper method used to retrieve the XML defined BarChart
     *
     * @param chartName a {@link java.lang.String} object.
     * @return a BarChart
     * @throws java.io.IOException if any.
     */
    public static BarChart getBarChartConfigByName(String chartName) throws ChartException {
        Iterator<BarChart> it = getChartCollectionIterator();
        BarChart chart = null;
        while (it.hasNext()) {
            chart = it.next();
            if (chart.getName().equals(chartName)) {
                return chart;
            }
        }
        return null;
    }
    
    /**
     * Helper method used to fetch an Iterator for all defined Charts
     *
     * @return <code>BarChart</code> Iterator
     * @throws java.io.IOException if any.
     */
    public static Collection<BarChart> getChartCollection() throws IOException {
        return ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection();
    }

    /**
     * Helper method used to fetch an Iterator for all defined Charts
     *
     * @return <code>BarChart</code> Iterator
     * @throws java.io.IOException if any.
     */
    public static Iterator<BarChart> getChartCollectionIterator() throws ChartException {
        try {
            return ChartConfigFactory.getInstance().getConfiguration().getBarChartCollection().iterator();
        } catch (final IOException e) {
            throw new ChartException("unable to get chart instance configuration", e);
        }
    }
    
}
