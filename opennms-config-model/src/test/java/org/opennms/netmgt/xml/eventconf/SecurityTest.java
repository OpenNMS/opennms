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

public class SecurityTest extends XmlTestNoCastor<Security> {

	public SecurityTest(final Security sampleObject, final String sampleXml, final String schemaFile) {
		super(sampleObject, sampleXml, schemaFile);
	}

	@Parameters
	public static Collection<Object[]> data() throws ParseException {
		Security security0 = new Security();
		security0.addDoNotOverride("I'm very important, don't mess with me!");
		Security security1 = new Security();
		security1.addDoNotOverride("I'm very important, don't mess with me!");
		security1.addDoNotOverride("Also important");
		return Arrays.asList(new Object[][] {
				{security0,
				"<security>" +
				"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
				"</security>",
				"target/classes/xsds/eventconf.xsd" }, 
				{security1,
					"<security>" +
					"<doNotOverride>I'm very important, don't mess with me!</doNotOverride>" +
					"<doNotOverride>Also important</doNotOverride>" +
					"</security>",
					"target/classes/xsds/eventconf.xsd" }, 		});
	}

}
