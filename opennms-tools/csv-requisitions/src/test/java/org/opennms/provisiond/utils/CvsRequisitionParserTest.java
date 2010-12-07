package org.opennms.provisiond.utils;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

public class CvsRequisitionParserTest {

	@Test
	@Ignore
	public void testParseCsv() throws IOException {
//		Resource r = new ClassPathResource(":classpath:opennms/requisition.csv");
		Resource r = new ClassPathResource("/Users/david/Documents/Business/Support/Towerstream/requisition2.csv");
		CsvRequisitionParser.parseCsv("/Users/david/Documents/Business/Support/Towerstream/requisition2.csv", "/tmp");
	}
	
	@Test
	public void testRegex() {
		Pattern p = Pattern.compile(".*[0-9]{4}+.*");
		String nodeLabel = "canopy-1467zz";
		Matcher m = p.matcher(nodeLabel);
		assertTrue(m.matches());
		
		p = Pattern.compile("(?!).*[0-9]{4}+.*");
		nodeLabel = "abc";
	}

	@Test
	@Ignore
	public void testCreateRequistionData() {
		fail("Not yet implemented");
	}

}
