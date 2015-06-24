/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.jasper.jrobin;

import java.io.File;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRExporterParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRXhtmlExporter;

import org.junit.Before;
import org.junit.Test;

public class JRobinQueryExecuterTest {
    
    private JasperReport m_jasperReport;
    private JasperPrint m_jasperPrint;
    
    @Before
    public void setUp() {
        File reportDir = new File("target/reports");
        reportDir.mkdirs();
    }
    
    /**
     *
     */
    @Test
    public void test() throws JRException{
        compile();
        fill();
        pdf();
        xhtml();
    }
    
    @Test
    public void testNoDataReport() throws JRException{
    	compileNoDataReport();
    	fill();
    	pdf();
    }
    

    public void compile() throws JRException {
        // jrxml compiling process
        m_jasperReport = JasperCompileManager.compileReport("src/test/resources/RrdGraph.jrxml");
        
    }
    
    public void compileNoDataReport() throws JRException {
        // jrxml compiling process
        m_jasperReport = JasperCompileManager.compileReport("src/test/resources/NoDataReport.jrxml");
        
    }

    /**
     *
     */
    public void fill() throws JRException{
        long start = System.currentTimeMillis();
        Map<String,Object> params = new HashMap<String,Object>();
        params.put("rrdDir", "src/test/resources");
        params.put("startDate", new Date("Wed Oct 13 17:25:00 EDT 2010"));
        params.put("endDate", new Date("Wed Oct 13 21:16:30 EDT 2010"));
        m_jasperPrint = JasperFillManager.fillReport(m_jasperReport, params);
        System.err.println("Filling time : " + (System.currentTimeMillis() - start));
    }

    /**
     *
     */
    public void pdf() throws JRException
    {
        long start = System.currentTimeMillis();
        JasperExportManager.exportReportToPdfFile(m_jasperPrint, "target/reports/RrdGraph.pdf");
        System.err.println("PDF creation time : " + (System.currentTimeMillis() - start));
    }
    
    /**
     *
     */
    public void xhtml() throws JRException
    {
        long start = System.currentTimeMillis();

        File destFile = new File("target/reports/RrdGraph.x.html");
        
        JRXhtmlExporter exporter = new JRXhtmlExporter();
        
        exporter.setParameter(JRExporterParameter.JASPER_PRINT, m_jasperPrint);
        exporter.setParameter(JRExporterParameter.OUTPUT_FILE_NAME, destFile.toString());
        
        exporter.exportReport();

        System.err.println("XHTML creation time : " + (System.currentTimeMillis() - start));
    }


}