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
package org.opennms.web.rest.v2;

import org.opennms.core.utils.ConfigFileConstants;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.mibcompiler.api.MibParser;
import org.opennms.features.mibcompiler.services.JsmiMibParser;
import org.opennms.features.mibcompiler.services.PrefabGraphDumper;
import org.opennms.netmgt.config.datacollection.DatacollectionGroup;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.xml.eventconf.Events;
import org.opennms.web.rest.v2.model.mibcompiler.MibCompileResultDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibDataCollectionPreviewDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibEventsPreviewDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibFileDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibGraphTemplatesDto;
import org.opennms.web.rest.v2.model.mibcompiler.MibParseResultDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Filesystem and parser operations behind the MIB compiler REST API.
 *
 * MIB files live on the local filesystem under $OPENNMS_HOME/share/mibs/{pending,compiled},
 * the same layout the Vaadin MIB compiler uses. A fresh {@link JsmiMibParser} is created per
 * operation because the parser is stateful; the blueprint-published singleton must not be shared.
 */
@Component
public class MibFileService {

    private static final Logger LOG = LoggerFactory.getLogger(MibFileService.class);

    public static final String PENDING = "pending";
    public static final String COMPILED = "compiled";

    private static final String MIB_FILE_EXTENSION = ".mib";
    private static final long MAX_MIB_FILE_SIZE = 10L * 1024 * 1024;

    private File mibsRootDir = new File(ConfigFileConstants.getHome(), "share" + File.separatorChar + "mibs");
    private File graphTemplatesDir = new File(ConfigFileConstants.getHome(), "etc" + File.separatorChar + "snmp-graph.properties.d");

    /** Thrown when a compiled MIB with the same name already exists and overwrite was not requested. */
    public static class MibExistsException extends RuntimeException {
        private final String mibName;
        private final String targetFile;

        public MibExistsException(String mibName, String targetFile) {
            super("A compiled MIB named '" + targetFile + "' already exists");
            this.mibName = mibName;
            this.targetFile = targetFile;
        }

        public String getMibName() {
            return mibName;
        }

        public String getTargetFile() {
            return targetFile;
        }
    }

    public List<MibFileDto> listMibFiles(String dir) {
        final File directory = directoryFor(dir);
        final File[] files = directory.listFiles(File::isFile);
        final List<MibFileDto> result = new ArrayList<>();
        if (files != null) {
            for (File file : files) {
                result.add(new MibFileDto(file.getName(), file.length(), file.lastModified()));
            }
        }
        result.sort(Comparator.comparing(MibFileDto::getName, String.CASE_INSENSITIVE_ORDER));
        return result;
    }

    public void saveUpload(String fileName, InputStream stream) throws IOException {
        final File pendingFile = resolve(PENDING, fileName);
        final File compiledFile = resolve(COMPILED, fileName);
        if (pendingFile.exists()) {
            throw new IllegalArgumentException("A MIB file named '" + fileName + "' already exists in the pending directory");
        }
        if (compiledFile.exists()) {
            throw new IllegalArgumentException("A MIB file named '" + fileName + "' already exists in the compiled directory");
        }
        Files.write(pendingFile.toPath(), readCapped(stream));
    }

    /** Reads the stream into memory, rejecting it as soon as it exceeds {@link #MAX_MIB_FILE_SIZE}. */
    private static byte[] readCapped(InputStream stream) throws IOException {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        final byte[] chunk = new byte[8192];
        int read;
        while ((read = stream.read(chunk)) != -1) {
            if (buffer.size() + read > MAX_MIB_FILE_SIZE) {
                throw new IllegalArgumentException("File exceeds the maximum allowed size of " + MAX_MIB_FILE_SIZE + " bytes");
            }
            buffer.write(chunk, 0, read);
        }
        return buffer.toByteArray();
    }

    public String readMibFile(String dir, String name) throws IOException {
        final File file = existingFile(dir, name);
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    public void updatePendingMibFile(String name, String content) throws IOException {
        if (content == null || content.getBytes(StandardCharsets.UTF_8).length > MAX_MIB_FILE_SIZE) {
            throw new IllegalArgumentException("Content is missing or exceeds the maximum allowed size of " + MAX_MIB_FILE_SIZE + " bytes");
        }
        final File file = existingFile(PENDING, name);
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    public void deleteMibFile(String dir, String name) throws IOException {
        final File file = existingFile(dir, name);
        Files.delete(file.toPath());
    }

    public MibCompileResultDto compile(String name, boolean overwrite) throws IOException {
        final File pendingFile = existingFile(PENDING, name);
        final MibParser parser = createParser();
        final MibCompileResultDto result = new MibCompileResultDto();
        if (!parser.parseMib(pendingFile)) {
            populateFailure(result, parser);
            return result;
        }
        final String mibName = parser.getMibName();
        final String targetName = mibName + MIB_FILE_EXTENSION;
        final File targetFile = resolve(COMPILED, targetName);
        if (targetFile.exists() && !overwrite) {
            throw new MibExistsException(mibName, targetName);
        }
        Files.move(pendingFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        result.setSuccess(true);
        result.setMibName(mibName);
        result.setTargetFile(targetName);
        LOG.info("Compiled MIB {} into {}", name, targetFile);
        return result;
    }

    public MibEventsPreviewDto generateEvents(String name, String ueiBase) throws IOException {
        final File mibFile = existingFile(COMPILED, name);
        final MibParser parser = createParser();
        final MibEventsPreviewDto result = new MibEventsPreviewDto();
        if (!parser.parseMib(mibFile)) {
            populateFailure(result, parser);
            return result;
        }
        final String mibName = parser.getMibName();
        final String effectiveUeiBase = ueiBase == null || ueiBase.isBlank()
                ? "uei.opennms.org/traps/" + mibName
                : ueiBase.trim();
        final Events events = parser.getEvents(effectiveUeiBase);
        if (events == null) {
            populateFailure(result, parser);
            return result;
        }
        result.setSuccess(true);
        result.setMibName(mibName);
        result.setUeiBase(effectiveUeiBase);
        result.setEventCount(events.getEvents().size());
        result.setSuggestedFileName(mibName + ".events.xml");
        result.setEventsXml(JaxbUtils.marshal(events));
        return result;
    }

    public MibDataCollectionPreviewDto generateDataCollection(String name) throws IOException {
        final File mibFile = existingFile(COMPILED, name);
        final MibParser parser = createParser();
        final MibDataCollectionPreviewDto result = new MibDataCollectionPreviewDto();
        if (!parser.parseMib(mibFile)) {
            populateFailure(result, parser);
            return result;
        }
        final DatacollectionGroup group = parser.getDataCollection();
        if (group == null) {
            populateFailure(result, parser);
            return result;
        }
        final String mibName = parser.getMibName();
        result.setSuccess(true);
        result.setMibName(mibName);
        result.setGroupCount(group.getGroups().size());
        result.setSuggestedFileName(mibName.replaceAll(" ", "_") + ".xml");
        result.setDataCollectionXml(JaxbUtils.marshal(group));
        return result;
    }

    public MibGraphTemplatesDto generateGraphTemplates(String name, boolean dryRun) throws IOException {
        final File mibFile = existingFile(COMPILED, name);
        final MibParser parser = createParser();
        final MibGraphTemplatesDto result = new MibGraphTemplatesDto();
        if (!parser.parseMib(mibFile)) {
            populateFailure(result, parser);
            return result;
        }
        final List<PrefabGraph> graphs = parser.getPrefabGraphs();
        if (graphs == null) {
            populateFailure(result, parser);
            return result;
        }
        final String mibName = parser.getMibName();
        final StringWriter content = new StringWriter();
        new PrefabGraphDumper().dump(graphs, content);
        final String fileName = mibName.replaceAll(" ", "_") + "-graph.properties";
        result.setSuccess(true);
        result.setMibName(mibName);
        result.setGraphCount(graphs.size());
        result.setFileName(fileName);
        result.setContent(content.toString());
        if (!dryRun) {
            if (!graphTemplatesDir.exists() && !graphTemplatesDir.mkdirs()) {
                throw new IOException("Unable to create directory " + graphTemplatesDir);
            }
            final File target = new File(graphTemplatesDir, fileName);
            try (FileWriter writer = new FileWriter(target, StandardCharsets.UTF_8)) {
                writer.write(content.toString());
            }
            result.setWritten(true);
            LOG.info("Graph templates for MIB {} written to {}", mibName, target);
        }
        return result;
    }

    private MibParser createParser() {
        final MibParser parser = new JsmiMibParser();
        parser.setMibDirectory(directoryFor(COMPILED));
        return parser;
    }

    private void populateFailure(MibParseResultDto result, MibParser parser) {
        result.setSuccess(false);
        result.setErrors(parser.getFormattedErrors());
        result.setMissingDependencies(parser.getMissingDependencies());
    }

    private File directoryFor(String dir) {
        if (!PENDING.equals(dir) && !COMPILED.equals(dir)) {
            throw new IllegalArgumentException("Invalid MIB directory '" + dir + "'; must be '" + PENDING + "' or '" + COMPILED + "'");
        }
        final File directory = new File(mibsRootDir, dir);
        if (!directory.exists() && !directory.mkdirs()) {
            throw new IllegalStateException("Unable to create MIB directory " + directory);
        }
        return directory;
    }

    private File existingFile(String dir, String name) throws FileNotFoundException {
        final File file = resolve(dir, name);
        if (!file.isFile()) {
            throw new FileNotFoundException("MIB file '" + name + "' not found in the " + dir + " directory");
        }
        return file;
    }

    private File resolve(String dir, String name) {
        if (name == null || name.isBlank() || name.startsWith(".") || name.contains("/") || name.contains("\\")) {
            throw new IllegalArgumentException("Invalid MIB file name: " + name);
        }
        final File directory = directoryFor(dir);
        final Path directoryNormalized = directory.toPath().normalize();
        final Path fileNormalized = directoryNormalized.resolve(name).normalize();
        if (!(fileNormalized.getNameCount() > directoryNormalized.getNameCount() && fileNormalized.startsWith(directoryNormalized))) {
            throw new IllegalArgumentException("Cannot access files outside of the MIB directories! File name given: " + name);
        }
        return fileNormalized.toFile();
    }

    public void setMibsRootDir(File mibsRootDir) {
        this.mibsRootDir = mibsRootDir;
    }

    public void setGraphTemplatesDir(File graphTemplatesDir) {
        this.graphTemplatesDir = graphTemplatesDir;
    }
}
