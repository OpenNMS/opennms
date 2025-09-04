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
package org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.core;

import org.opennms.core.fileutils.DotDUpdateWatcher;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.xml.config.IpfixElements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreInformationElementXmlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CoreInformationElementXmlProvider.class);
    final String OPENNMS_HOME = System.getProperty("opennms.home");

    @Autowired
    private TwinPublisher twinPublisher;
    private TwinPublisher.Session<IpfixDotD> twinSession;

    public CoreInformationElementXmlProvider() {
    }

    private Set<File> getFiles() throws IOException {
        final Path ipfixDotD = Paths.get(OPENNMS_HOME)
                .resolve("etc")
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
            return null;
        }

        for (final File file : files) {
            final IpfixElements ipfixElements;
            try {
                ipfixElements = JAXB.unmarshal(file, IpfixElements.class);
                ipfixDotD.getIpfixElements().add(ipfixElements);
            } catch (DataBindingException e) {
                LOG.error("Cannot load file {}", file.getAbsolutePath(), e);
                return null;
            }
        }
        return ipfixDotD;
    }

    public void init() {
        try {
            final DotDUpdateWatcher dotDUpdateWatcher = new DotDUpdateWatcher(OPENNMS_HOME + "/etc/ipfix.d", (dir, name) -> name.endsWith(".xml"), () -> {
                reloadConfig();
            });
        } catch (IOException e) {
            LOG.error("Error initializing DotDUpdateWatcher for directory {}", OPENNMS_HOME + "/etc/ipfix.d", e);
        }

        reloadConfig();
    }

    private void reloadConfig() {
        LOG.info("Loading information elements from XML files in {}", OPENNMS_HOME + "/etc/ipfix.d");
        final IpfixDotD ipfixDotD = loadIpfixDotDFiles();

        if (ipfixDotD == null) {
            return;
        }

        try {
            if (twinSession == null) {
                twinSession = this.twinPublisher.register(IpfixDotD.TWIN_KEY, IpfixDotD.class);
            }
            twinSession.publish(ipfixDotD);
        } catch (IOException e) {
            LOG.error("Error publishing ipfix.d configuration files");
        }
    }

    public void setTwinPublisher(TwinPublisher twinPublisher) {
        this.twinPublisher = twinPublisher;

        init();
    }
}
