/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.xml.eventconf;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class MaskTest extends XmlTestNoCastor<Mask> {

	public MaskTest(final Mask sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Mask mask0 = new Mask();
		Maskelement maskelement = new Maskelement();
		maskelement.setMename("specific");
		maskelement.addMevalue("3");
		mask0.addMaskelement(maskelement);
		Mask mask1 = new Mask();
		mask1.addMaskelement(maskelement);
		Varbind varbind = new Varbind();
		varbind.setVbnumber(5);
		varbind.addVbvalue("0");
		mask1.addVarbind(varbind);
		return Arrays.asList(new Object[][] {
				{mask0,
				"<mask><maskelement><mename>specific</mename><mevalue>3</mevalue></maskelement></mask>",
				"target/classes/xsds/eventconf.xsd" }, 
				{mask1,
					" <mask> " +
					"<maskelement>" +
					"<mename>specific</mename>" +
					"<mevalue>3</mevalue>" +
					"</maskelement>" +
					"<varbind>" +
					"<vbnumber>5</vbnumber>" +
					"<vbvalue>0</vbvalue>" +
					"</varbind>" +
					"</mask>",
					"target/classes/xsds/eventconf.xsd" } 
		});
	}

}
