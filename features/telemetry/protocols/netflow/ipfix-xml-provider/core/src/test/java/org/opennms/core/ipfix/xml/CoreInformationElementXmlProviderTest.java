/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.core.ipfix.xml;

import org.opennms.core.ipc.twin.memory.*;
import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.core.CoreInformationElementXmlProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import javax.xml.bind.JAXB;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.IpfixElements;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.Scope;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementXmlProviderImpl;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
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

        var coreInformationElementXmlProvider = new CoreInformationElementXmlProvider();
        coreInformationElementXmlProvider.setTwinPublisher(publisher);

        final org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase informationElementDatabase = new InformationElementDatabase(
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
                new InformationElementXmlProviderImpl(subscriber),
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
