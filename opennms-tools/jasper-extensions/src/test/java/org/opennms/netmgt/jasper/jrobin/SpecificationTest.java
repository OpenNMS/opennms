package org.opennms.netmgt.jasper.jrobin;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.jrobin.core.RrdDb;
import org.jrobin.core.RrdDef;
import org.jrobin.core.RrdException;
import org.jrobin.core.Sample;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class SpecificationTest {

    private static final long MILLIS_PER_HOUR = 3600L * 1000L;
    private static final long MILLIS_PER_DAY = 24L * MILLIS_PER_HOUR;
    
    private JasperReport m_jasperReport;
    private JasperPrint m_jasperPrint;
    private Date m_startDate;
    private Date m_endDate;

    @Before
    public void setUp() throws RrdException, IOException {
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
        
        long now = System.currentTimeMillis();
        long end = now/MILLIS_PER_DAY*MILLIS_PER_DAY + (MILLIS_PER_HOUR * 4);
        long start = end - MILLIS_PER_DAY;
        m_startDate = new Date(start);
        System.out.println("startDate: " + m_startDate);
        m_endDate = new Date(end);
        System.out.println("endDate: " + m_endDate);
        
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
        
        int count = 0;
        for(long timestamp = start - 300000; timestamp<= end; timestamp += 300000){
            System.out.println("timestamp: " + new Date(timestamp));
            
            Sample sample = rrd1.createSample(timestamp/1000);
            sample.setValue("mo_call_attempts", 10 * count);
            sample.setValue("mo_call_completes", 8 * count);
            sample.setValue("mo_mins_carried", 32 * count);
            sample.setValue("mo_calls_active", 2);
            
            sample.update();
            
            Sample sample2 = rrd2.createSample(timestamp/1000);
            sample2.setValue("mt_call_attempts", 5 * count);
            sample2.setValue("mt_call_completes", 4 * count);
            sample2.setValue("mt_mins_carried", 16 * count);
            sample2.setValue("mt_calls_active", 1);
            
            sample2.update();
            
            count++;
        }
        
        rrd1.close();
        rrd2.close();
    }
    
    @After
    public void tearDown() {
        
    }
    
    @Test
    public void testRrdFilesExist() {
        File file = new File("target/rrd/mo_calls.jrb");
        assertTrue(file.exists());
        
        File file2 = new File("target/rrd/mt_calls.jrb");
        assertTrue(file2.exists());
    }
    
    @Test
    public void testSpecReport() throws JRException {
        compile();
        fill();
        pdf();
    }
    
    public void compile() throws JRException {
        // jrxml compiling process
        m_jasperReport = JasperCompileManager.compileReport("src/test/resources/AllChartsReport.jrxml");
        
    }
    
    public void fill() throws JRException{
        long start = System.currentTimeMillis();
        Map params = new HashMap();
        params.put("rrdDir", "target/rrd");
        params.put("startDate",m_startDate);
        params.put("endDate", m_endDate);
        m_jasperPrint = JasperFillManager.fillReport(m_jasperReport, params);
        System.err.println("Filling time : " + (System.currentTimeMillis() - start));
    }
    
    public void pdf() throws JRException{
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(m_jasperPrint, "target/reports/AllChartsReport.pdf");
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
    }
    
}
