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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix;

import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementXmlProvider;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.IpfixElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.io.IOException;
import java.util.Optional;

public class InformationElementXmlProviderImpl implements InformationElementXmlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(InformationElementXmlProviderImpl.class);

    protected InformationElementDatabase database;
    protected TwinSubscriber twinSubscriber;
    protected TwinPublisher.Session<IpfixDotD> twinSession;
    protected Closeable twinSubscription;
    protected InformationElementDatabase.Adder adder;

    public InformationElementXmlProviderImpl(final TwinSubscriber twinSubscriber) {
        bind(twinSubscriber);
    }

    public InformationElementXmlProviderImpl() {
    }

    public void bind(final TwinSubscriber twinSubscriber) {
        this.twinSubscriber = twinSubscriber;
        subscribe();
    }

    public void unbind(final TwinSubscriber twinSubscriber) {
        if (this.twinSubscription != null) {
            try {
                this.twinSubscription.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        if (this.twinSubscriber != null) {
            try {
                this.twinSubscriber.close();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        this.twinSubscriber = null;
        this.twinSubscription = null;
    }

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }

    protected void applyConfig(final InformationElementDatabase.Adder adder, final IpfixDotD ipfixDotD) {
        if (ipfixDotD == null) {
            return;
        }

        LOG.info("Applying new config...");

        adder.clear(getClass().getName());

        for(final IpfixElements ipfixElements : ipfixDotD.getIpfixElements()) {
            final long vendor = ipfixElements.getScope().getPen();

            for (final Element element : ipfixElements.getElements()) {
                final int id = element.getId();
                final String name = element.getName();
                final InformationElementDatabase.ValueParserFactory valueParserFactory = InformationElementProvider.TYPE_LOOKUP.get(element.getDataType());
                adder.add(Protocol.IPFIX, Optional.of(vendor), id, valueParserFactory, name, Optional.of(Semantics.DEFAULT), this.database, getClass().getName());
            }
        }
    }

    @Override
    public void load(InformationElementDatabase.Adder adder) {
        if (this.adder == null) {
            this.adder = adder;
        }
        subscribe();
    }

    protected void subscribe() {
        if (twinSubscriber == null || adder == null) {
            return;
        }

        if (twinSubscription == null) {
            twinSubscription = twinSubscriber.subscribe(IpfixDotD.TWIN_KEY, IpfixDotD.class, (config) -> {
                applyConfig(adder, config);
            });
        }
    }
}
