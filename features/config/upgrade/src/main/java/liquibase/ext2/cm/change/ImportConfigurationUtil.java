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
package liquibase.ext2.cm.change;

import static liquibase.ext2.cm.change.ConfigFileUtil.asString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
import org.opennms.features.config.exception.ConfigConversionException;
import org.opennms.features.config.exception.ConfigRuntimeException;
import org.opennms.features.config.service.api.ConfigUpdateInfo;
import org.opennms.features.config.service.api.ConfigurationManagerService;
import org.opennms.features.config.service.api.JsonAsString;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import liquibase.ext2.cm.change.converter.PropertiesToJson;
import liquibase.ext2.cm.change.converter.XmlToJson;
import liquibase.util.file.FilenameUtils;

public class ImportConfigurationUtil {

    private static final Logger LOG = LoggerFactory.getLogger(RegisterSchema.class);

    public static void importConfig(
            final ConfigurationManagerService configurationManagerService,
            final Resource configResource,
            final ConfigUpdateInfo configurationIdentifier,
            final Path archivePath) {
        Objects.requireNonNull(configurationManagerService);
        Objects.requireNonNull(configResource);
        Objects.requireNonNull(configurationIdentifier);
        Objects.requireNonNull(archivePath);
        LOG.info("Importing configuration from {} for {}", configResource.getFilename(), configurationIdentifier);
        try {
            ConfigDefinition configDefinition = configurationManagerService
                    .getRegisteredConfigDefinition(configurationIdentifier.getConfigName())
                    .orElseThrow(() -> new ConfigRuntimeException("Cannot find configDefinition for " + configurationIdentifier.getConfigName()));
            if (!configDefinition.getAllowMultiple() && !ConfigDefinition.DEFAULT_CONFIG_ID.equals(configurationIdentifier.getConfigId())) {
                throw new IllegalArgumentException(String.format("For the '%s' only one configuration can be provided (configId='%s')",
                        configurationIdentifier.getConfigName(), ConfigDefinition.DEFAULT_CONFIG_ID));
            }
            String fileType = FilenameUtils.getExtension(configResource.getFilename());
            JsonAsString configObject;
            if("xml".equalsIgnoreCase(fileType)) {
                configObject = new XmlToJson(asString(configResource), configDefinition).getJson();
            } else if("cfg".equalsIgnoreCase(fileType)) {
                ConfigItem schema = OpenAPIBuilder.createBuilder(configurationIdentifier.getConfigName(), configurationIdentifier.getConfigName(), "", configDefinition.getSchema()).getRootConfig();
                configObject = new PropertiesToJson(configResource.getInputStream(), schema).getJson();
            } else {
                throw new ConfigConversionException(String.format("Unknown file type: '%s'", fileType));
            }
            configurationManagerService.registerConfiguration(configurationIdentifier, configObject);
            LOG.info("Configuration {} imported.", configurationIdentifier);
            if(configResource.getURL().getFile().endsWith("/etc/" + configResource.getFilename())) {
                Path etcFile = configResource.getFile().getAbsoluteFile().toPath();

                // we imported a user defined config file => move to archive
                // in order to prevent file exist exception, append data time sting at the end
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
                Path archiveFile = Path.of(archivePath.toString(), etcFile.getFileName() + "." + formatter.format(LocalDateTime.now()));
                Files.move(etcFile, archiveFile);
                LOG.info("Configuration file {} moved to {}", etcFile, archivePath);
            }
        } catch (Exception e) {
            throw new ConfigRuntimeException("Error while importing config.", e);
        }
    }
}
