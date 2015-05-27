package org.opennms.netmgt.jasper.chart;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.XYPlot;

import net.sf.jasperreports.engine.JRChart;
import net.sf.jasperreports.engine.JRChartCustomizer;

public class FormatNumberAxisWithBytesCustomizer implements JRChartCustomizer {
    public static class ByteFormat extends NumberFormat {
        private static final long serialVersionUID = 2292353515765577691L;

        private static enum Format {
            B,
            KB,
            MB,
            GB,
            TB,
            PB
        };

        private static DecimalFormat df = new DecimalFormat("#.####");

        public static String getBytes(double bytes) {
            Format largestFormat = Format.B;
            long denum = 0;
            for (Format fmt : Format.values()) {
                largestFormat = fmt;

                if (denum == 0) {
                    denum = 1;
                } else {
                    denum *= 1024;
                }

                if (((long)(bytes / (denum * 1024))) == 0) {
                    break;
                }
            }
            return df.format(bytes / denum) + largestFormat;
        }

        @Override
        public StringBuffer format(double number, StringBuffer toAppendTo,
                FieldPosition pos) {
            return toAppendTo.append(getBytes(number));
        }

        @Override
        public StringBuffer format(long number, StringBuffer toAppendTo,
                FieldPosition pos) {
            return toAppendTo.append(getBytes(number));
        }

        /**
         * Always returns <code>null</code>. Not used for parsing.
         */
        @Override
        public Number parse(String source, ParsePosition parsePosition) {
            return null;
        }
    }

    public void customize(JFreeChart chart, JRChart jasperChart){
        Plot plot = chart.getPlot();
        if (plot instanceof XYPlot) {
            XYPlot xyPlot = (XYPlot)plot;
            ValueAxis axis = xyPlot.getRangeAxis();
            if (axis instanceof NumberAxis) {
                NumberAxis numberAxis = (NumberAxis)axis;
                numberAxis.setNumberFormatOverride(new ByteFormat());
            }
        }
    }
}
