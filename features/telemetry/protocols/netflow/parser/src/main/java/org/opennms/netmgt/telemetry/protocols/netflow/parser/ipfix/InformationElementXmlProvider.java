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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix;

import org.opennms.core.fileutils.DotDUpdateWatcher;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.distributed.core.api.Identity;
import org.opennms.distributed.core.api.SystemType;
import org.opennms.netmgt.snmp.TrapListenerConfig;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.xml.Element;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.xml.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.xml.IpfixElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;


public class InformationElementXmlProvider implements InformationElementDatabase.Provider {
    private static final Logger LOG = LoggerFactory.getLogger(InformationElementXmlProvider.class);
    final String OPENNMS_HOME = System.getProperty("opennms.home");

    private InformationElementDatabase database;
    private Identity identity;

    private TwinPublisher twinPublisher;
    private TwinSubscriber twinSubscriber;
    private TwinPublisher.Session<IpfixDotD> twinSession;
    private Closeable twinSubscription;

    public InformationElementXmlProvider(final Identity identity, final TwinPublisher twinPublisher, final TwinSubscriber twinSubscriber) {
        this.identity = identity;
        this.twinPublisher = twinPublisher;
        this.twinSubscriber = twinSubscriber;
    }

    @Override
    public InformationElementDatabase getDatabase() {
        return database;
    }

    @Override
    public void setDatabase(InformationElementDatabase database) {
        this.database = database;
    }

    private Set<File> getFiles() throws IOException {
        final Path ipfixDotD = Paths.get(System.getProperty("karaf.etc"))
                .resolve("ipfix.d");

        try (Stream<Path> stream = Files.list(ipfixDotD)) {
            return stream.filter(path -> !Files.isDirectory(path))
                    .filter(path -> path.getFileName().toString().endsWith(".xml"))
                    .map(Path::toFile)
                    .collect(Collectors.toSet());
        }
    }

    private IpfixDotD loadIpfixDotDFiles() {
        final IpfixDotD ipfixDotD = new IpfixDotD();

        final Set<File> files;

        try {
            files = getFiles();
        } catch (IOException e) {
            LOG.error("Error reading files in directory etc/ipfix.d", e);
            return ipfixDotD;
        }

        for (final File file : files) {
            final IpfixElements ipfixElements;

            try {
                ipfixElements = JAXB.unmarshal(file, IpfixElements.class);
                ipfixDotD.getIpfixElements().add(ipfixElements);
            } catch (DataBindingException e) {
                LOG.error("Cannot load file {}", file.getAbsolutePath(), e);
            }
        }
        return ipfixDotD;
    }

    @Override
    public void load(final InformationElementDatabase.Adder adder) {
        if (identity.getType().equals(SystemType.Minion.name())) {
            LOG.error("BMRHGA: TYPE MINION twinPublisher = " + (twinPublisher != null) + ", twinSubscriber = " + (twinSubscriber != null));
            // get config via twin mechanism
            twinSubscription = twinSubscriber.subscribe(TrapListenerConfig.TWIN_KEY, IpfixDotD.class, (config) -> {
                applyConfig(adder, config);
            });
        } else {
            LOG.error("BMRHGA: TYPE CORE twinPublisher = " + (twinPublisher != null) + ", twinSubscriber = " + (twinSubscriber != null));
            // Core or Sentinel
            reloadConfig(adder);

            // setup watcher
            try {
                final DotDUpdateWatcher dotDUpdateWatcher = new DotDUpdateWatcher(OPENNMS_HOME + "/etc/ipfix.d", (dir, name) -> name.endsWith(".xml"), () -> {
                    reloadConfig(adder);
                });
            } catch (Exception e) {
                LOG.error("Error initializing DotDUpdateWatcher for directory {}", OPENNMS_HOME + "/etc/ipfix.d", e);
            }
        }
    }

    private void reloadConfig(final InformationElementDatabase.Adder adder) {
        LOG.info("Loading information elements from XML files in {}", OPENNMS_HOME + "/etc/ipfix.d");
        final IpfixDotD ipfixDotD = loadIpfixDotDFiles();
        try {
            if (twinSession == null) {
                twinSession = this.twinPublisher.register("IpfixDotD", IpfixDotD.class);
            }
            twinSession.publish(ipfixDotD);
        } catch (IOException e) {
            LOG.error("Error publishing ipfix.d configuration files");
        }
        applyConfig(adder, ipfixDotD);
    }

    private void applyConfig(final InformationElementDatabase.Adder adder, final IpfixDotD ipfixDotD) {
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
}