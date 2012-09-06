/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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
