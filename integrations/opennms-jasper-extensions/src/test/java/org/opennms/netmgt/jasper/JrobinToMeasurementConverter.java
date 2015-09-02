package org.opennms.netmgt.jasper;

import com.google.common.collect.RowSortedTable;
import org.jrobin.cmd.RrdCommander;
import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.opennms.netmgt.measurements.model.QueryResponse;
import org.opennms.netmgt.rrd.model.RrdXport;
import org.opennms.netmgt.rrd.model.XMeta;
import org.opennms.netmgt.rrd.model.XRow;

import javax.xml.bind.JAXB;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mvrueden on 27/08/15.
 */
// TODO MVR delete me
public class JrobinToMeasurementConverter {

    public interface Function {
        double evaluate(long timestamp);
    }

    private static class Sin implements Function {

        long m_startTime;
        double m_offset;
        double m_amplitude;
        double m_period;
        double m_factor;

        public Sin(long startTime, double offset, double amplitude, double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;
            m_factor = 2 * Math.PI / period;
        }

        @Override
        public double evaluate(long timestamp) {
            long x = timestamp - m_startTime;
            double ret = (m_amplitude * Math.sin(m_factor * x)) + m_offset;
            System.out.println("Sin("+ x + ") = " + ret);
            return ret;
        }
    }

    private static class Cos implements Function {

        long m_startTime;
        double m_offset;
        double m_amplitude;
        double m_period;

        double m_factor;

        private Cos(long startTime, double offset, double amplitude, double period) {
            m_startTime = startTime;
            m_offset = offset;
            m_amplitude = amplitude;
            m_period = period;

            m_factor = 2 * Math.PI / period;
        }

        @Override
        public double evaluate(long timestamp) {
            long x = timestamp - m_startTime;
            double ret = (m_amplitude * Math.cos(m_factor * x)) + m_offset;
            System.out.println("Cos("+ x + ") = " + ret);
            return ret;
        }
    }

    private static class Times implements Function {
        Function m_a;
        Function m_b;

        public Times(Function a, Function b) {
            m_a = a;
            m_b = b;
        }

        @Override
        public double evaluate(long timestamp) {
            return m_a.evaluate(timestamp)*m_b.evaluate(timestamp);
        }
    }

    private static class Counter implements Function {
        double m_prevValue;
        Function m_function;

        public Counter(double initialValue, Function function) {
            m_prevValue = initialValue;
            m_function = function;
        }

        @Override
        public double evaluate(long timestamp) {
            double m_diff = m_function.evaluate(timestamp);
            m_prevValue += m_diff;
            return m_prevValue;
        }

    }

    private static final long MILLIS_PER_HOUR = 3600L * 1000L;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;

    private static void initAllChartsReport(Date startDate, Date endDate) throws RrdException, IOException {
        File file = new File("target/rrd/mo_calls.jrb");
        if(file.exists()) {
            file.delete();
        }

        File file2 = new File("target/rrd/mt_calls.jrb");
        if(file2.exists()) {
            file2.delete();
        }

        new File("target/rrd").mkdirs();
        new File("target/reports").mkdirs();


        final long start = startDate.getTime();
        final long end = endDate.getTime();

        RrdDef rrdDef = new RrdDef("target/rrd/mo_calls.jrb", (start/1000) - 600000, 300);
        rrdDef.addDatasource("DS:mo_call_attempts:COUNTER:600:0:U");
        rrdDef.addDatasource("DS:mo_call_completes:COUNTER:600:0:U");
        rrdDef.addDatasource("DS:mo_mins_carried:COUNTER:600:0:U");
        rrdDef.addDatasource("DS:mo_calls_active:GAUGE:600:0:U");
        rrdDef.addArchive("RRA:AVERAGE:0.5:1:288");

        RrdDef rrdDef2 = new RrdDef("target/rrd/mt_calls.jrb", (start/1000) - 600000 , 300);
        rrdDef2.addDatasource("DS:mt_call_attempts:COUNTER:600:0:U");
        rrdDef2.addDatasource("DS:mt_call_completes:COUNTER:600:0:U");
        rrdDef2.addDatasource("DS:mt_mins_carried:COUNTER:600:0:U");
        rrdDef2.addDatasource("DS:mt_calls_active:GAUGE:600:0:U");
        rrdDef2.addArchive("RRA:AVERAGE:0.5:1:288");

        RrdDb rrd1 = new RrdDb(rrdDef);
        RrdDb rrd2 = new RrdDb(rrdDef2);

        Function bigSine = new Sin(start, 15, -10, MILLIS_PER_DAY);
        Function smallSine = new Sin(start, 7, 5, MILLIS_PER_DAY);
        Function moSuccessRate = new Cos(start, .5, .3, MILLIS_PER_DAY);
        Function mtSuccessRate = new Cos(start, .5, -.2, 2*MILLIS_PER_DAY);

        Function moAttempts = new Counter(0, bigSine);
        Function moCompletes = new Counter(0, new Times(moSuccessRate, bigSine));

        Function mtAttempts = new Counter(0, smallSine);
        Function mtCompletes = new Counter(0, new Times(mtSuccessRate, smallSine));

        int count = 0;
        for(long timestamp = start - 300000; timestamp<= end; timestamp += 300000){
            //System.out.println("timestamp: " + new Date(timestamp));


            Sample sample = rrd1.createSample(timestamp/1000);
            double attemptsVal = moAttempts.evaluate(timestamp);
            double completesVal = moCompletes.evaluate(timestamp);

            //System.out.println("Attempts: " + attemptsVal + " Completes " + completesVal);
            sample.setValue("mo_call_attempts", attemptsVal);
            sample.setValue("mo_call_completes", completesVal);
            sample.setValue("mo_mins_carried", 32 * count);
            sample.setValue("mo_calls_active", 2);

            sample.update();

            Sample sample2 = rrd2.createSample(timestamp/1000);
            sample2.setValue("mt_call_attempts", mtAttempts.evaluate(timestamp));
            sample2.setValue("mt_call_completes", mtCompletes.evaluate(timestamp));
            sample2.setValue("mt_mins_carried", 16 * count);
            sample2.setValue("mt_calls_active", 1);

            sample2.update();

            count++;
        }

        rrd1.close();
        rrd2.close();
    }

    public static void main(String[] args) throws Exception {
      /*  final DateFormat formatter = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");

        // All Charts Report jrobin query
        long now = System.currentTimeMillis();
        long end = now/MILLIS_PER_DAY*MILLIS_PER_DAY + (MILLIS_PER_HOUR * 4);
        long start = end - (MILLIS_PER_DAY*7);
        initAllChartsReport(new Date(start), new Date(end));
        transformResult("all-charts-1", "xport --start $P{startDate} --end $P{endDate} --step 3600\n" +
                "        DEF:mo=$P{rrdDir}/mo_calls.jrb:mo_call_attempts:AVERAGE\n" +
                "        DEF:mt=$P{rrdDir}/mt_calls.jrb:mt_call_attempts:AVERAGE\n" +
                "        CDEF:moTotal=mo,3600,*\n" +
                "        CDEF:mtTotal=mt,3600,*\n" +
                "        XPORT:moTotal:moCallAttempts\n" +
                "        XPORT:mtTotal:mtCallAttempts", "target/rrd", new Date(start), new Date(end));

        transformResult("all-charts-2", "xport --start $P{startDate} --end $P{endDate} --step 3600\n" +
                "        DEF:moAttempts=$P{rrdDir}/mo_calls.jrb:mo_call_attempts:AVERAGE\n" +
                "        DEF:moCompletes=$P{rrdDir}/mo_calls.jrb:mo_call_completes:AVERAGE\n" +
                "        DEF:mtAttempts=$P{rrdDir}/mt_calls.jrb:mt_call_attempts:AVERAGE\n" +
                "        DEF:mtCompletes=$P{rrdDir}/mt_calls.jrb:mt_call_completes:AVERAGE\n" +
                "        CDEF:moSuccessRate=moCompletes,100,*,moAttempts,/\n" +
                "        CDEF:mtSuccessRate=mtCompletes,100,*,mtAttempts,/\n" +
                "        XPORT:moSuccessRate:moSuccessRate\n" +
                "        XPORT:mtSuccessRate:mtSuccessRate", "target/rrd", new Date(start), new Date(end));

        //RrdGraph jrobin query
        transformResult("rrd-graph", "xport --start $P{startDate} --end $P{endDate}\n" +
                        "        DEF:xx=$P{rrdDir}/http-8980.jrb:http-8980:AVERAGE\n" +
                        "        DEF:zz=$P{rrdDir}/ssh.jrb:ssh:AVERAGE\n" +
                        "        XPORT:xx:HttpLatency\n" +
                        "        XPORT:zz:SshLatency", "src/test/resources",
                formatter.parse("Wed Oct 13 17:25:00 EDT 2010"), formatter.parse("Wed Oct 13 21:16:30 EDT 2010"));

        //Forecast jrobin query
        transformResult("rrd-graph", "--start $P{startDate} --end $P{endDate}\n" +
                        "        DEF:xx=$P{rrdDir}/ifInOctets.jrb:ifInOctets:AVERAGE ANALYTICS:HoltWinters=HW:Values:1:86400 XPORT:xx:Values",
                "src/test/resources/forecasting",
                new Date(1414602000), new Date(1417046400));
*/

        final String command = getCommand("--start $P{startDate} --end $P{endDate}\n" +
                "        DEF:xx=$P{rrdDir}/ifInOctets.jrb:ifInOctets:AVERAGE ANALYTICS:HoltWinters=HW:Values:1:86400 XPORT:xx:Values",
                "src/test/resources/forecasting",
                1414602000, 1417046400);

        final String name = "dummy";
        System.out.println(command);

//        RrdDataSourceFilter dsFilter = new RrdDataSourceFilter(command);
//        JRRewindableDataSource ds = new RrdXportCmd().executeCommand(dsFilter.getRrdQueryString());
//        RowSortedTable<Integer, String, Double> table = DataSourceUtils.fromDs(ds, dsFilter.getFieldNames());
//        dsFilter.filter(table);
//        RrdXport result = fromTable(table, 1414602000, 1417046400);

//        JAXB.marshal(result, new FileOutputStream("/Users/mvrueden/Desktop/" + name + "-Xport-Response.xml"));
//        QueryResponse queryResponse = convertToQueryResponse(result);
//        JAXB.marshal(queryResponse, new FileOutputStream("/Users/mvrueden/Desktop/" + name + "-Measurement-Response.xml"));
    }

    private static RrdXport fromTable(RowSortedTable<Integer, String, Double> input, long start, long end) {
        RrdXport xport = new RrdXport();

        XMeta xmeta = new XMeta();
        xmeta.setStart(start);
        xmeta.setEnd(end);
        xmeta.setRows((long) input.rowKeySet().size());
        xmeta.setLegends(new ArrayList<String>(input.columnKeySet()));
        xmeta.setStep(input.get(0, "step").longValue());
        xmeta.getLegends().remove("timestamp");
        xmeta.getLegends().remove("step");
        xmeta.setColumns((long) xmeta.getLegends().size());

        xport.setMeta(xmeta);

        for (int rowIndex : input.rowKeySet()) {
            XRow row = new XRow();
            row.setTimestamp(input.get(rowIndex, "timestamp").longValue());
            row.setValues(new ArrayList<Double>());
            for (String eachColumnName : xport.getMeta().getLegends()) {
                Double value = input.get(rowIndex, eachColumnName);
                row.getValues().add(value == null ? Double.NaN : value);
            }
            xport.getRows().add(row);
        }

        return xport;
    }

//    private static RowSortedTable<Integer, String, Double> fromRrdXport(RrdXport input) {
//        RowSortedTable<Integer, String, Double> table = TreeBasedTable.create();
//        // Build the table, row by row
//        for (int rowIndex = 0; rowIndex < input.getMeta().getRows(); rowIndex++) {
//            for (int colIndex = 0; colIndex < input.getMeta().getColumns(); colIndex++) {
//                table.put(rowIndex, input.getMeta().getLegends().get(colIndex), input.getRows().get(rowIndex).getValues().get(colIndex));
//            }
//            table.put(rowIndex, "timestamp", (double) input.getRows().get(rowIndex).getTimestamp());
//            table.put(rowIndex, "step", (double) input.getMeta().getStep());
//            table.put(rowIndex, "start", (double) input.getMeta().getStart());
//            table.put(rowIndex, "end", (double) input.getMeta().getEnd());
//        }
//
//        return table;
//    }

    private static void transformResult(final String name, final String cmd, final String rrdDir, final Date startDate, final Date endDate) throws RrdException, IOException {
        final String command = getCommand(cmd, rrdDir, startDate.getTime() / 1000, endDate.getTime() / 1000);

        System.out.println(command);
        String resultString = (String) RrdCommander.execute(command);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(resultString.getBytes());
        RrdXport result = JAXB.unmarshal(inputStream, RrdXport.class);
        JAXB.marshal(result, new FileOutputStream("/Users/mvrueden/Desktop/" + name + "-Xport-Response.xml"));
        QueryResponse queryResponse = convertToQueryResponse(result);
        JAXB.marshal(queryResponse, new FileOutputStream("/Users/mvrueden/Desktop/" + name + "-Measurement-Response.xml"));
    }

    private static QueryResponse convertToQueryResponse(RrdXport rrdXport) {
        QueryResponse queryResponse = new QueryResponse();
        queryResponse.setStart(rrdXport.getMeta().getStart() * 1000);
        queryResponse.setEnd(rrdXport.getMeta().getEnd() * 1000);
        queryResponse.setStep(rrdXport.getMeta().getStep());

        Map<String, double[]> columns = new HashMap<String, double[]>();
        for (int i=0; i<rrdXport.getMeta().getColumns(); i++) {
            final String label = rrdXport.getMeta().getLegends().get(i);
            final double[] values = new double[Long.valueOf(rrdXport.getMeta().getRows()).intValue()];
            for (int a=0; a<rrdXport.getMeta().getRows(); a++) {
                values[a] = rrdXport.getRows().get(a).getValues().get(i);
            }
            columns.put(label, values);
        }
        queryResponse.setColumns(columns);

        final long[] timestamps = new long[Long.valueOf(rrdXport.getMeta().getRows()).intValue()];
        for (int i=0; i<rrdXport.getMeta().getRows(); i++) {
            timestamps[i] = rrdXport.getRows().get(i).getTimestamp() * 1000;
        }
        queryResponse.setTimestamps(timestamps);
        return queryResponse;
    }

    private static String getCommand(String input, String rrdDir, long startDateTime, long endDateTime) {
       input = input.replaceAll("\\$P\\{startDate\\}", String.valueOf(startDateTime))
                    .replaceAll("\\$P\\{endDate\\}", String.valueOf(endDateTime))
                    .replaceAll("\\$P\\{rrdDir\\}", rrdDir);
       return input;
    }
}
