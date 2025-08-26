package org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.core;

import org.opennms.core.fileutils.DotDUpdateWatcher;
import org.opennms.core.ipc.twin.api.TwinPublisher;
import org.opennms.core.ipc.twin.api.TwinSubscriber;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.InformationElementDatabase;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixDotD;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ie.xml.IpfixElements;
import org.opennms.netmgt.telemetry.protocols.netflow.parser.ipfix.AbstractInformationElementXmlProvider;
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
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class CoreInformationElementXmlProvider extends AbstractInformationElementXmlProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CoreInformationElementXmlProvider.class);
    final String OPENNMS_HOME = System.getProperty("opennms.home");

    private final TwinPublisher twinPublisher;
    private InformationElementDatabase.Adder adder;

    public CoreInformationElementXmlProvider(final TwinPublisher twinPublisher, final TwinSubscriber twinSubscriber) {
        super(twinSubscriber);
        this.twinPublisher = twinPublisher;
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

    @Override
    public void load(InformationElementDatabase.Adder adder) {
        super.load(adder);

        if (this.adder == null) {
            this.adder = adder;

            try {
                final DotDUpdateWatcher dotDUpdateWatcher = new DotDUpdateWatcher(OPENNMS_HOME + "/etc/ipfix.d", (dir, name) -> name.endsWith(".xml"), () -> {
                    reloadConfig(adder);
                });
            } catch (Exception e) {
                LOG.error("Error initializing DotDUpdateWatcher for directory {}", OPENNMS_HOME + "/etc/ipfix.d", e);
            }

            reloadConfig(adder);
        }
    }

    private void reloadConfig(final InformationElementDatabase.Adder adder) {
        LOG.info("Loading information elements from XML files in {}", OPENNMS_HOME + "/etc/ipfix.d");
        final IpfixDotD ipfixDotD = loadIpfixDotDFiles();

        if (ipfixDotD == null) {
            return;
        }

        try {
            if (twinSession == null) {
                twinSession = this.twinPublisher.register(TWIN_KEY, IpfixDotD.class);
            }
            twinSession.publish(ipfixDotD);
        } catch (IOException e) {
            LOG.error("Error publishing ipfix.d configuration files");
        }
    }
}
