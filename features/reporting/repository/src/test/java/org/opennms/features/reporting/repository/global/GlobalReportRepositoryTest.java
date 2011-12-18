package org.opennms.features.reporting.repository.global;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.reporting.model.basicreport.BasicReportDefinition;
import org.opennms.features.reporting.repository.ReportRepository;
import org.opennms.features.reporting.repository.local.LegacyLocalReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@Ignore
//TODO Tak: check this test and handel remote-repository connections
public class GlobalReportRepositoryTest {

	Logger logger = LoggerFactory.getLogger(GlobalReportRepositoryTest.class);

	private GlobalReportRepository globalRepo;
	private static final String OPENNMS_HOME = "src/test/resources";
	
	@BeforeClass
	public static void setup() {
		System.setProperty("opennms.home", OPENNMS_HOME);
		assertEquals(OPENNMS_HOME, System.getProperty("opennms.home"));
	}

	@Before
	public void init() {
		globalRepo = new DefaultGlobalReportRepository();
	}

	@Test
	public void addReportRepositoryTest() {
		ReportRepository newRepository = new LegacyLocalReportRepository();
		assertEquals("local", newRepository.getRepositoryId());
		assertEquals(2, globalRepo.getRepositoryList().size());
		globalRepo.addReportRepository(newRepository);
		assertEquals(3, globalRepo.getRepositoryList().size());

		List<ReportRepository> repositoryList = globalRepo.getRepositoryList();
		assertEquals("local", repositoryList.get(0).getRepositoryId());
		assertEquals("cioreporting", repositoryList.get(1).getRepositoryId());
		assertEquals("local", repositoryList.get(2).getRepositoryId());
	}

	@Test
	public void getAllOnlineReportsTest() {
		List<BasicReportDefinition> resultList = globalRepo.getAllOnlineReports();
		//TODO Tak: check this test it was set to 23...
        assertEquals(16, resultList.size());
		for (BasicReportDefinition report : resultList) {
			logger.debug("getAllOnlineReportsTest: report '{}' is online '{}'", report.getId(), report.getOnline());
			assertTrue(report.getId().contains("_"));
			assertTrue(report.getOnline());
		}
	}

	@Test
	public void getAllReportsTest() {
		List<BasicReportDefinition> resultList = globalRepo.getAllReports();
		//TODO Tak: check this test it was set to 27...
		assertEquals(18, resultList.size());
		for (BasicReportDefinition report : resultList) {
			assertTrue(report.getId().contains("_"));
			logger.debug("getAllReportsTest: report '{}' is online '{}'", report.getId(), report.getOnline());
		}
	}

	@Test
	public void getEngineTest() {
		BasicReportDefinition report = globalRepo.getAllOnlineReports().get(0);
		assertEquals("local_sample-report", report.getId());
		assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
		assertEquals("jdbc", globalRepo.getEngine("local_sample-report"));
	}

	@Test
	public void getOnlineReportsTest() {
		List<BasicReportDefinition> reportList = globalRepo.getOnlineReports("local");
		assertEquals(16, reportList.size());
	}

	@Test
	public void getRemplateStreamTest() throws IOException {
		BasicReportDefinition report = globalRepo.getAllOnlineReports().get(0);
		assertEquals("local_sample-report", report.getId());
		assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
		InputStream templateStream = globalRepo.getTemplateStream("local_sample-report");
		assertEquals(4822, templateStream.available());
		templateStream.close();
	}

	@Test
	public void getReportServiceTest() {
		BasicReportDefinition report = globalRepo.getAllOnlineReports().get(0);
		assertEquals("local_sample-report", report.getId());
		assertEquals("Kochwurst sample JasperReport", report.getDisplayName());
		assertEquals("jasperReportService", globalRepo.getReportService("local_sample-report"));
	}

	@Test
	public void getReportsTest() {
		List<BasicReportDefinition> reportList = globalRepo.getReports("local");
		assertEquals(18, reportList.size());
	}

	@Test
	public void getRepositoryByIdTest() {
		assertNotNull(globalRepo);
		List<ReportRepository> repositoryList = globalRepo.getRepositoryList();
		assertEquals(2, repositoryList.size());
		for (ReportRepository repository : repositoryList) {
			assertEquals(repository.getRepositoryId(), globalRepo.getRepositoryById(repository.getRepositoryId()).getRepositoryId());
		}
		assertFalse(globalRepo.getRepositoryById("") != null);

		logger.debug(globalRepo.getDisplayName("local_sample-report"));
		logger.debug("local repository : '{}'", globalRepo.getRepositoryById("cioreporting"));
		ReportRepository localRepo = globalRepo.getRepositoryById("local");
		logger.debug(localRepo.toString());
		BasicReportDefinition report = localRepo.getOnlineReports().get(0);
		logger.debug(report.toString());
		logger.debug(report.getId());
		report.setId("LOCAL_" + report.getId());
		assertEquals("LOCAL_local_sample-report", report.getId());
		assertFalse(("local_LOCAL_local_sample-report".equals(localRepo.getOnlineReports().get(0).getId())));
		assertEquals("local_sample-report", localRepo.getOnlineReports().get(0).getId());
	}

	@Test
	public void getRepositoryListTest() {
		List<ReportRepository> repositoryList = globalRepo.getRepositoryList();
		assertEquals(2, repositoryList.size());
		assertEquals("local", repositoryList.get(0).getRepositoryId());
		assertEquals("cioreporting", repositoryList.get(1).getRepositoryId());
	}
}