/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.jasper.analytics;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.JRCsvExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleWriterExporterOutput;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.google.common.collect.Table;
import com.google.common.collect.TreeBasedTable;

public class HWForecastReportTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();
    
    private JasperReport m_jasperReport;
    private JasperPrint m_jasperPrint;
    
    private File m_jrbFile;
    private String m_dsName;
    private long m_startTime;
    private long m_endTime;

    private File m_csvFile;

    @Test
    public void canForecastValues() throws Exception {
        useJrb();
        compile();
        fill();
        csv();
        verify();
    }

    private void useJrb() {
        m_jrbFile = new File("src/test/resources/forecasting/ifInOctets.jrb");
        assertTrue(m_jrbFile.canRead());
        m_dsName = "ifInOctets";
        m_startTime = 1414602000;
        m_endTime = 1417046400;
    }

    /*
    private void generateJrb() throws Exception {
        m_jrbFile = tempFolder.newFile();
        m_dsName = "test";
        
        final int secondsInYear = 60 * 60 * 24 * 365;
        final int numSlots = secondsInYear / 300;

        RrdDef def = new RrdDef(m_jrbFile.getAbsolutePath());
        def.setStep(300);
        def.addDatasource(m_dsName, "GAUGE", 300L, Double.NaN, Double.NaN);
        def.addArchive("RRA:AVERAGE:0.5:1:" + numSlots);
        RrdDb db = new RrdDb(def);

        // Fill in the values
        m_startTime = System.currentTimeMillis() / 1000;
        for (int i = 0; i < numSlots; i++) {
            long t = m_startTime + (i * 300);
            Sample sample = db.createSample();
            sample.setAndUpdate(t + ":" + 42);
        }
        m_endTime = m_startTime + ((numSlots - 1) * 300);

        db.close();
    }
    */

    private void compile() throws JRException {
        m_jasperReport = JasperCompileManager.compileReport("src/test/resources/forecasting/Forecast.jrxml");
    }
    
    private void fill() throws JRException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put(JRParameter.IS_IGNORE_PAGINATION, true);

        params.put("jrbFile", m_jrbFile.getAbsolutePath());
        params.put("dsName", m_dsName);
        params.put("startDate", "" + m_startTime);
        params.put("endDate", "" + m_endTime);

        m_jasperPrint = JasperFillManager.fillReport(m_jasperReport, params);
    }

    private void csv() throws Exception {
        // Input
        SimpleExporterInput sei = new SimpleExporterInput(m_jasperPrint);

        // Output
        m_csvFile = tempFolder.newFile();
        SimpleWriterExporterOutput sweo = new SimpleWriterExporterOutput(m_csvFile);

        // Export
        JRCsvExporter exporter = new JRCsvExporter();
        exporter.setExporterInput(sei);
        exporter.setExporterOutput(sweo);
        exporter.exportReport();
    }

    private void verify() throws Exception {
        Table<Integer, String, Double> forecasts = TreeBasedTable.create();

        try (
                FileReader reader = new FileReader(m_csvFile);
                CSVParser parser = new CSVParser(reader, CSVFormat.RFC4180.withHeader());    
        ) {
            int k = 0;
            for (CSVRecord record : parser) {
                try {
                    Double fit = Double.parseDouble(record.get("HWFit"));
                    Double lwr = Double.parseDouble(record.get("HWLwr"));
                    Double upr = Double.parseDouble(record.get("HWUpr"));

                    if(Double.isNaN(fit)) {
                        continue;
                    }

                    forecasts.put(k, "fit", fit);
                    forecasts.put(k, "lwr", lwr);
                    forecasts.put(k, "upr", upr);

                    k++;
                } catch (NumberFormatException e) {
                    // pass
                }
            }
        }

        assertEquals(340, forecasts.rowKeySet().size());
        // First fitted value
        assertEquals(432.526086422424, forecasts.get(0, "fit"), 0.00001);
        // Last fitted value for which there is a known datapoint
        assertEquals(24079.4692522087, forecasts.get(327, "fit"), 0.00001);
        // First forecasted value
        assertEquals(22245.5417010936, forecasts.get(328, "fit"), 0.00001);
    }
}
