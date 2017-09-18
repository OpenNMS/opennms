/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.ami;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTestNoCastor;

public class AmiConfigTest extends XmlTestNoCastor<AmiConfig> {

    public AmiConfigTest(final AmiConfig sampleObject,
            final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }

    @Parameters
    public static Collection<Object[]> data() throws ParseException {

        Range range = new Range("10.0.0.0", "10.255.255.255");
        List<Range> ranges = new ArrayList<>();
        ranges.add(range);

        List<String> specifics = new ArrayList<>();
        specifics.add("192.168.1.1");

        List<String> ipMatches = new ArrayList<>();
        ipMatches.add("172.23.*.*");

        Definition definition = new Definition(9999, true, 1000, 3, "user",
                                               "pass", ranges, specifics,
                                               ipMatches);
        List<Definition> definitions = new ArrayList<>();
        definitions.add(definition);

        AmiConfig amiConfig = new AmiConfig(9998, false, 3000, 1, "admin",
                                            "admin", definitions);

        return Arrays.asList(new Object[][] { {
                amiConfig,
                "<ami-config port=\"9998\" use-ssl=\"false\" timeout=\"3000\" "
                        + "retry=\"1\" username=\"admin\" password=\"admin\">"
                        + "<definition port=\"9999\" use-ssl=\"true\" timeout=\"1000\" "
                        + "retry=\"3\" username=\"user\" password=\"pass\">"
                        + "<range begin=\"10.0.0.0\" end=\"10.255.255.255\"/>"
                        + "<specific>192.168.1.1</specific>"
                        + "<ip-match>172.23.*.*</ip-match>"
                        + "</definition>"
                        + "</ami-config>",
                "target/classes/xsds/ami-config.xsd", }, });
    }
}
