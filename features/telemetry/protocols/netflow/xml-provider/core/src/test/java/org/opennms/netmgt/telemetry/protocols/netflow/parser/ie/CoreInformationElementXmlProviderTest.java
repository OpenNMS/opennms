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

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.twin.memory.MemoryTwinPublisher;
import org.opennms.core.ipc.twin.memory.MemoryTwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixElements;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.Scope;

import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class CoreInformationElementXmlProviderTest {
    @Rule
    public TemporaryFolder tempFolder = new TemporaryFolder();

    @Test
    public void testConfigUpdates() throws IOException {
        final File etcFolder = tempFolder.newFolder("etc");
        final File ipFixD = new File(etcFolder, "ipfix.d");
        ipFixD.mkdirs();

        Assert.assertTrue(ipFixD.exists());
        Assert.assertTrue(ipFixD.isDirectory());

        System.setProperty("karaf.etc", etcFolder.getAbsolutePath());
        System.setProperty("opennms.home", tempFolder.getRoot().getAbsolutePath());

        final File file1 = new File(ipFixD, "custom1.xml");
        createFile(file1, 9999L, 901, 902);

        final MemoryTwinPublisher publisher = new MemoryTwinPublisher();
        final MemoryTwinSubscriber subscriber = new MemoryTwinSubscriber(publisher, "Default");

        final InformationElementDatabase informationElementDatabase = new InformationElementDatabase(
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
                new CoreInformationElementXmlProvider(publisher, subscriber),
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());
        Assert.assertEquals("foo-octets-901", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).isPresent());
        Assert.assertEquals("foo-octets-902", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).isEmpty());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 904).isEmpty());

        final File file2 = new File(ipFixD, "custom2.xml");
        createFile(file2, 9998L, 903, 904);

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).isPresent());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());
        Assert.assertEquals("foo-octets-901", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).isPresent());
        Assert.assertEquals("foo-octets-902", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).get().getName());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).isPresent());
        Assert.assertEquals("foo-octets-903", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 904).isPresent());
        Assert.assertEquals("foo-octets-904", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 904).get().getName());

        createFile(file1, 9999L, 905, 906);
        createFile(file2, 9998L, 903);

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 905).isPresent());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 905).isPresent());
        Assert.assertEquals("foo-octets-905", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 905).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 906).isPresent());
        Assert.assertEquals("foo-octets-906", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 906).get().getName());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).isPresent());
        Assert.assertEquals("foo-octets-903", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).get().getName());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isEmpty());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).isEmpty());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 904).isEmpty());
    }

    private void createFile(File file, final long pen, final int... numbers) {
        final IpfixElements ipfixElements = new IpfixElements();
        final Scope scope = new Scope();
        scope.setName("name-" + pen);
        scope.setPen(pen);
        ipfixElements.setScope(scope);
        for (final int number : numbers) {
            final Element element = new Element();
            element.setId(number);
            element.setName("foo-octets-" + number);
            element.setDataType("unsigned32");
            ipfixElements.getElements().add(element);
        }

        JAXB.marshal(ipfixElements, file);
    }
}
