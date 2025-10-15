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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.minion;

import org.awaitility.Awaitility;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.memory.MemoryTwinPublisher;
import org.opennms.core.ipc.twin.memory.MemoryTwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixElements;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.Scope;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MinionInformationElementXmlProviderTest {
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

        final MemoryTwinPublisher publisher = new MemoryTwinPublisher();
        TwinPublisher.Session<IpfixDotD> session = publisher.register(MinionInformationElementXmlProvider.TWIN_KEY, IpfixDotD.class);
        final MemoryTwinSubscriber subscriber = new MemoryTwinSubscriber(publisher, "Default");

        publishElements(session, createElements(9999L, 901, 902));

        final InformationElementDatabase informationElementDatabase = new InformationElementDatabase(
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.InformationElementProvider(),
                new MinionInformationElementXmlProvider(subscriber),
                new org.opennms.netmgt.telemetry.protocols.netflow.parser.netflow9.InformationElementProvider());

        Awaitility.await().atMost(5, SECONDS).pollInterval(100, MILLISECONDS).
                until(() -> informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());

        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).isPresent());
        Assert.assertEquals("foo-octets-901", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 901).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).isPresent());
        Assert.assertEquals("foo-octets-902", informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9999L), 902).get().getName());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 903).isEmpty());
        Assert.assertTrue(informationElementDatabase.lookup(Protocol.IPFIX, Optional.of(9998L), 904).isEmpty());

        publishElements(session, createElements(9999L, 901, 902), createElements(9998L, 903, 904));

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

        publishElements(session, createElements(9999L, 905, 906), createElements(9998L, 903));

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

    private void publishElements(TwinPublisher.Session<IpfixDotD> publisher, IpfixElements ... elements) {
        final IpfixDotD ipfixDotD = new IpfixDotD();
        for(final IpfixElements element : elements) {
            ipfixDotD.getIpfixElements().add(element);
        }

        try {
            publisher.publish(ipfixDotD);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private IpfixElements createElements(final long pen, final int... numbers) {
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

        return ipfixElements;
    }
}
