package org.opennms.netmgt.dao.castor;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.util.List;

import org.junit.Test;
import org.opennms.netmgt.config.databaseReports.ReportParm;
import org.opennms.test.ConfigurationTestUtils;
import org.springframework.core.io.InputStreamResource;

public class LegacyDatabaseReportDaoTest {
    

    @Test
    public void testGetParmsForReport() throws Exception {
        LegacyDatabaseReportDao dao = new LegacyDatabaseReportDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("legacy-database-reports.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        
        List<ReportParm> parms =  dao.getParmsByName("Calendar Availability Report");
        assertEquals(2,parms.size());
    }

    @Test
    public void testGetReports() throws Exception {
        LegacyDatabaseReportDao dao = new LegacyDatabaseReportDao();
        
        InputStream in = ConfigurationTestUtils.getInputStreamForConfigFile("legacy-database-reports.xml");
        dao.setConfigResource(new InputStreamResource(in));
        dao.afterPropertiesSet();
        List<String> names = dao.getNames();
        assertEquals(2,names.size());
    }
}
