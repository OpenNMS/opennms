/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2011 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.config.datacollection;

import java.text.ParseException;
import java.util.Arrays;
import java.util.Collection;

import org.junit.runners.Parameterized.Parameters;
import org.opennms.core.test.xml.XmlTest;
import org.opennms.netmgt.config.datacollection.MibObj;

public class MibObjTest extends XmlTest<MibObj> {

    public MibObjTest(final MibObj sampleObject, final String sampleXml, final String schemaFile) {
        super(sampleObject, sampleXml, schemaFile);
    }
    
    @Parameters
    public static Collection<Object[]> data() throws ParseException {
        final MibObj obj = new MibObj();
        obj.setAlias("cyPMSerialPortNum");
        obj.setInstance("cyPMSerialPortNum");
        obj.setOid(".1.3.6.1.4.1.2925.4.5.2.1.1");
        obj.setType("string");
        obj.setMaxval("4294967295");
        obj.setMinval("0");

        return Arrays.asList(new Object[][] { {
                obj,
                "<mibObj oid=\".1.3.6.1.4.1.2925.4.5.2.1.1\" instance=\"cyPMSerialPortNum\" alias=\"cyPMSerialPortNum\" type=\"string\" maxval=\"4294967295\" minval=\"0\" />",
                "target/classes/xsds/datacollection-config.xsd" } });
    }


}
