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

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;

import org.opennms.netmgt.telemetry.protocols.netflow.parser.Protocol;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.Semantics;
import org.opennms.netmgt.telemetry.api.xml.Element;
import org.opennms.netmgt.telemetry.api.xml.IpfixElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InformationElementXmlProvider implements InformationElementDatabase.Provider {

    private static final Logger LOG = LoggerFactory.getLogger(InformationElementXmlProvider.class);

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
    @Override
    public void load(final InformationElementDatabase.Adder adder) {
        final Set<File> files;

        try {
            files = getFiles();
        } catch (IOException e) {
            LOG.error("Error reading files in directory etc/ipfix.d", e);
            return;
        }

        for(final File file : files) {
            final IpfixElements ipfixElements;

            try {
                 ipfixElements = JAXB.unmarshal(file, IpfixElements.class);
            } catch (DataBindingException e) {
                LOG.error("Cannot load file {}", file.getAbsolutePath(), e);
                continue;
            }

            LOG.debug("Processing file {}, {} entries", file.getAbsolutePath(), ipfixElements.getElements().size());

            final long vendor = ipfixElements.getScope().getPen();

            for(final Element element : ipfixElements.getElements()) {
                final int id = element.getId();
                final String name = element.getName();
                final InformationElementDatabase.ValueParserFactory valueParserFactory = InformationElementProvider.TYPE_LOOKUP.get(element.getDataType());
                adder.add(Protocol.IPFIX, Optional.of(vendor), id, valueParserFactory, name, Optional.of(Semantics.DEFAULT));
            }
        }
    }
}
