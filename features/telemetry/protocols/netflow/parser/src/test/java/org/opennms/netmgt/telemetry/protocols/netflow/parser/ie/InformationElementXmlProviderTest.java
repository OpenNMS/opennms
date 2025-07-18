/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2024 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2024 The OpenNMS Group, Inc.
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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie;

import java.util.Optional;

import org.junit.Assert;
import org.junit.Test;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;

public class InformationElementXmlProviderTest {

    @Test
    public void testXml() {
        System.setProperty("karaf.etc", "src/test/resources");

        final InformationElementDatabase informationElementDatabase = new InformationElementDatabase(
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementXmlProvider(null, null, null),
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());
        Assert.assertEquals("foo-octets", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).isPresent());
        Assert.assertEquals("bar-octets", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 903).isEmpty());
    }
}
