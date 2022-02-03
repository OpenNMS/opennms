/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package liquibase.ext2.cm.change;

import static liquibase.ext2.cm.change.ConfigFileUtil.asString;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;

import org.opennms.features.config.dao.api.ConfigDefinition;
import org.opennms.features.config.dao.api.ConfigItem;
import org.opennms.features.config.dao.impl.util.OpenAPIBuilder;
import org.opennms.features.config.exception.ConfigConversionException;
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
            Optional<ConfigDefinition> configDefinition = configurationManagerService.getRegisteredConfigDefinition(configurationIdentifier.getConfigName());

            String fileType = FilenameUtils.getExtension(configResource.getFilename());
            JsonAsString configObject;
            if("xml".equalsIgnoreCase(fileType)) {
                configObject = new XmlToJson(asString(configResource), configDefinition.get()).getJson();
            } else if("cfg".equalsIgnoreCase(fileType)) {
                ConfigItem schema = OpenAPIBuilder.createBuilder(configurationIdentifier.getConfigName(), configurationIdentifier.getConfigName(), "", configDefinition.get().getSchema()).getRootConfig();
                configObject = new PropertiesToJson(configResource.getInputStream(), schema).getJson();
            } else {
                throw new ConfigConversionException(String.format("Unknown file type: '%s'", fileType));
            }
            configurationManagerService.registerConfiguration(configurationIdentifier, configObject);
            LOG.info("Configuration {} imported.", configurationIdentifier);
            if(configResource.getURL().getFile().endsWith("/etc/" + configResource.getFilename())) {
                Path etcFile = configResource.getFile().getAbsoluteFile().toPath();
                // we imported a user defined config file => move to archive
                Path archiveFile = Path.of(archivePath + "/" + etcFile.getFileName());
                Files.move(etcFile, archiveFile);
                LOG.info("Configuration file {} moved to {}", etcFile, archivePath);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
