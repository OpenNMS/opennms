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

import java.io.InputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.codehaus.jackson.map.ObjectMapper;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.features.config.exception.ValidationException;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.events.api.EventProxy;
import org.opennms.web.api.Authentication;
import org.opennms.web.rest.v2.api.ThreshdConfigRestApi;
import org.opennms.web.rest.v2.mapper.ThreshdConfigMapper;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdPackageSummaryDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * REST service for the threshd daemon configuration, formerly {@code threshd-configuration.xml}.
 *
 * <p>Every mutation runs inside {@link WriteableThreshdDao#withWriteLock}, because the scheduled outages
 * service writes the same document to maintain package outage calendars.</p>
 */
@Service
public class ThreshdConfigRestService implements ThreshdConfigRestApi {

    private static final Logger LOG = LoggerFactory.getLogger(ThreshdConfigRestService.class);

    private static final String CONFIG_FILE_NAME = "threshd-configuration.xml";
    private static final String NO_CONFIG_MESSAGE = "Threshd configuration not found.";

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private WriteableThreshdDao threshdDao;

    @Autowired
    @Qualifier("eventProxy")
    private EventProxy eventProxy;

    // ------------------------------------------------------------------- reads

    @Override
    public Response getThreshdConfiguration(final SecurityContext securityContext) {
        try {
            final ThreshdConfiguration config = threshdDao.getWriteableConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build();
            }

            final ThreshdConfigDto dto = ThreshdConfigMapper.toDto(config);
            return Response.ok(dto).tag(ThresholdRestSupport.etagOf(objectMapper, dto)).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the threshd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve threshd configuration.").build();
        }
    }

    @Override
    public Response getThreshdPackages(final SecurityContext securityContext) {
        try {
            final ThreshdConfiguration config = threshdDao.getWriteableConfig();
            if (config == null) {
                return Response.ok(new ArrayList<ThreshdPackageSummaryDto>()).build();
            }

            final List<ThreshdPackageSummaryDto> summaries = config.getPackages().stream()
                    .sorted(Comparator.comparing(Package::getName, Comparator.nullsLast(String::compareTo)))
                    .map(ThreshdConfigMapper::toSummaryDto)
                    .collect(Collectors.toList());

            return Response.ok(summaries).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the threshd packages.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve threshd configuration.").build();
        }
    }

    @Override
    public Response getThreshdPackage(final String packageName, final SecurityContext securityContext) {
        try {
            final Optional<Package> pkg = findPackage(threshdDao.getWriteableConfig(), packageName);
            if (pkg.isEmpty()) {
                return Response.status(Status.NOT_FOUND).entity(unknownPackage(packageName)).build();
            }

            final ThreshdPackageDto dto = ThreshdConfigMapper.toDto(pkg.get());
            return Response.ok(dto).tag(ThresholdRestSupport.etagOf(objectMapper, dto)).build();
        } catch (final Exception e) {
            LOG.error("Failed to retrieve the threshd package {}.", packageName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to retrieve threshd configuration.").build();
        }
    }

    // ------------------------------------------------------------------ writes

    @Override
    public Response updateThreshdConfiguration(final ThreshdConfigDto configDto, final String ifMatch,
                                               final SecurityContext securityContext) {
        final String validationMessage = ThresholdConfigValidator.validateThreshdConfig(configDto);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            threshdDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final String currentEtag = ThresholdRestSupport.etagOf(objectMapper, ThreshdConfigMapper.toDto(config));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag)) {
                    failure.set(Response.status(Status.PRECONDITION_FAILED)
                            .entity("The threshd configuration has changed since it was read.").build());
                    return;
                }

                final ThreshdConfiguration replacement = ThreshdConfigMapper.toEntity(configDto);
                // Mutate the instance the DAO holds: saveConfig() serializes its own field, not an argument.
                config.setThreads(replacement.getThreads());
                config.setPackages(replacement.getPackages());
                config.setThresholder(replacement.getThresholder());

                threshdDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("The threshd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to update the threshd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist threshd configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendReload();
        return Response.noContent().build();
    }

    @Override
    public Response createThreshdPackage(final ThreshdPackageDto pkg, final SecurityContext securityContext) {
        final String validationMessage = ThresholdConfigValidator.validatePackage(pkg);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        final String packageName = pkg.getName().trim();
        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            threshdDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }
                if (findPackage(config, packageName).isPresent()) {
                    failure.set(Response.status(Status.CONFLICT)
                            .entity("Threshd package '" + packageName + "' already exists.").build());
                    return;
                }

                final List<Package> packages = new ArrayList<>(config.getPackages());
                packages.add(ThreshdConfigMapper.toEntity(pkg));
                config.setPackages(packages);

                threshdDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("Threshd package {} failed schema validation.", packageName, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to create the threshd package {}.", packageName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist threshd configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendReload();
        return Response.created(URI.create("threshd/packages/" + packageName)).build();
    }

    @Override
    public Response updateThreshdPackage(final String packageName, final ThreshdPackageDto pkg, final String ifMatch,
                                         final SecurityContext securityContext) {
        final String validationMessage = ThresholdConfigValidator.validatePackage(pkg);
        if (validationMessage != null) {
            return Response.status(Status.BAD_REQUEST).entity(validationMessage).build();
        }

        final String newName = pkg.getName().trim();
        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            threshdDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final Optional<Package> existing = findPackage(config, packageName);
                if (existing.isEmpty()) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(unknownPackage(packageName)).build());
                    return;
                }
                if (!newName.equals(packageName) && findPackage(config, newName).isPresent()) {
                    failure.set(Response.status(Status.CONFLICT)
                            .entity("Threshd package '" + newName + "' already exists.").build());
                    return;
                }

                final String currentEtag = ThresholdRestSupport.etagOf(objectMapper, ThreshdConfigMapper.toDto(existing.get()));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag)) {
                    failure.set(Response.status(Status.PRECONDITION_FAILED)
                            .entity("Threshd package '" + packageName + "' has changed since it was read.").build());
                    return;
                }

                final Package replacement = ThreshdConfigMapper.toEntity(pkg);
                final List<Package> packages = new ArrayList<>();
                for (final Package candidate : config.getPackages()) {
                    packages.add(packageName.equals(candidate.getName()) ? replacement : candidate);
                }
                config.setPackages(packages);

                threshdDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("Threshd package {} failed schema validation.", packageName, e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to update the threshd package {}.", packageName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist threshd configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendReload();
        return Response.noContent().build();
    }

    @Override
    public Response deleteThreshdPackage(final String packageName, final String ifMatch,
                                         final SecurityContext securityContext) {
        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            threshdDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }

                final Optional<Package> existing = findPackage(config, packageName);
                if (existing.isEmpty()) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(unknownPackage(packageName)).build());
                    return;
                }
                // thresholding.xsd declares package with minOccurs=1, so an empty list would not validate.
                if (config.getPackages().size() == 1) {
                    failure.set(Response.status(Status.BAD_REQUEST)
                            .entity("At least one threshd package is required; '" + packageName + "' cannot be deleted.").build());
                    return;
                }

                final String currentEtag = ThresholdRestSupport.etagOf(objectMapper, ThreshdConfigMapper.toDto(existing.get()));
                if (!ThresholdRestSupport.matchesIfMatch(ifMatch, currentEtag)) {
                    failure.set(Response.status(Status.PRECONDITION_FAILED)
                            .entity("Threshd package '" + packageName + "' has changed since it was read.").build());
                    return;
                }

                config.setPackages(config.getPackages().stream()
                        .filter(candidate -> !packageName.equals(candidate.getName()))
                        .collect(Collectors.toList()));

                threshdDao.saveConfig();
            });
        } catch (final Exception e) {
            LOG.error("Failed to delete the threshd package {}.", packageName, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist threshd configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendReload();
        return Response.noContent().build();
    }

    @Override
    public Response reloadThreshdConfiguration(final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to reload the threshd configuration.").build();
        }
        try {
            ThresholdRestSupport.sendReloadEvent(eventProxy, CONFIG_FILE_NAME);
            return Response.status(Status.ACCEPTED).build();
        } catch (final Exception e) {
            LOG.error("Could not send the reload event for {}.", CONFIG_FILE_NAME, e);
            return Response.status(Status.INTERNAL_SERVER_ERROR)
                    .entity("Could not send the reload event for " + CONFIG_FILE_NAME + ".").build();
        }
    }

    // ------------------------------------------------------- download / upload

    @Override
    public Response downloadThreshdConfiguration(final String format, final SecurityContext securityContext) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to download the threshd configuration.").build();
        }

        final boolean isXml = format != null && format.equalsIgnoreCase("xml");
        if (!isXml && format != null && !format.equalsIgnoreCase("json")) {
            return Response.status(Status.BAD_REQUEST).entity("Invalid format parameter. Supported values are 'json' and 'xml'.").build();
        }

        final byte[] body;
        try {
            final ThreshdConfiguration config = threshdDao.getWriteableConfig();
            if (config == null) {
                return Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build();
            }

            if (isXml) {
                body = JaxbUtils.marshal(config).getBytes(StandardCharsets.UTF_8);
            } else {
                body = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(ThreshdConfigMapper.toDto(config)).getBytes(StandardCharsets.UTF_8);
            }
        } catch (final Exception e) {
            LOG.error("Error serializing the threshd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Error retrieving the threshd configuration.").build();
        }

        final String fileName = isXml ? "threshd-configuration.xml" : "threshd-configuration.json";
        final String contentType = isXml ? "application/xml" : "application/json";

        return Response.ok().type(contentType + ";charset=" + StandardCharsets.UTF_8)
                .header("Content-Disposition", "attachment; filename=" + fileName)
                .header("Pragma", "public")
                .header("Cache-Control", "no-cache, must-revalidate")
                .entity(body).build();
    }

    @Override
    public Response uploadThreshdConfiguration(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, false);
    }

    @Override
    public Response uploadThreshdConfigurationXml(final Attachment attachment, final SecurityContext securityContext) {
        return uploadInternal(attachment, securityContext, true);
    }

    private Response uploadInternal(final Attachment attachment, final SecurityContext securityContext, final boolean isXml) {
        if (!securityContext.isUserInRole(Authentication.ROLE_ADMIN)) {
            return Response.status(Status.FORBIDDEN).entity("Admin role required to upload the threshd configuration.").build();
        }

        final String fileType = isXml ? "XML" : "JSON";
        if (attachment == null) {
            return Response.status(Status.BAD_REQUEST)
                    .entity("Missing uploaded file for threshd configuration " + fileType + " file upload.").build();
        }

        final ThreshdConfiguration replacement;

        try (InputStream inputStream = attachment.getObject(InputStream.class)) {
            if (isXml) {
                // The validating overload: the two-argument one parses without checking the schema.
                replacement = JaxbUtils.unmarshal(ThreshdConfiguration.class,
                        new String(inputStream.readAllBytes(), StandardCharsets.UTF_8), true);
            } else {
                final ThreshdConfigDto dto = objectMapper.readValue(inputStream, ThreshdConfigDto.class);
                final String message = ThresholdConfigValidator.validateThreshdConfig(dto);
                if (message != null) {
                    return Response.status(Status.BAD_REQUEST).entity(message).build();
                }
                replacement = ThreshdConfigMapper.toEntity(dto);
            }
        } catch (final Exception e) {
            LOG.warn("Failed to parse the uploaded threshd {} configuration.", fileType, e);
            return Response.status(Status.BAD_REQUEST).entity("Invalid threshd " + fileType + " configuration.").build();
        }

        final AtomicReference<Response> failure = new AtomicReference<>();

        try {
            threshdDao.withWriteLock(config -> {
                if (config == null) {
                    failure.set(Response.status(Status.NOT_FOUND).entity(NO_CONFIG_MESSAGE).build());
                    return;
                }
                config.setThreads(replacement.getThreads());
                config.setPackages(replacement.getPackages());
                config.setThresholder(replacement.getThresholder());
                threshdDao.saveConfig();
            });
        } catch (final ValidationException e) {
            LOG.warn("The uploaded threshd configuration failed schema validation.", e);
            return Response.status(Status.BAD_REQUEST).entity(e.getMessage()).build();
        } catch (final Exception e) {
            LOG.error("Failed to persist the uploaded threshd configuration.", e);
            return Response.status(Status.INTERNAL_SERVER_ERROR).entity("Failed to persist threshd configuration.").build();
        }

        if (failure.get() != null) {
            return failure.get();
        }

        sendReload();
        return Response.ok().build();
    }

    // ------------------------------------------------------------------ helpers

    private static Optional<Package> findPackage(final ThreshdConfiguration config, final String packageName) {
        if (config == null || packageName == null) {
            return Optional.empty();
        }
        return config.getPackages().stream()
                .filter(pkg -> packageName.equals(pkg.getName()))
                .findFirst();
    }

    private static String unknownPackage(final String packageName) {
        return "Threshd package '" + packageName + "' does not exist.";
    }

    private void sendReload() {
        try {
            ThresholdRestSupport.sendReloadEvent(eventProxy, CONFIG_FILE_NAME);
        } catch (final Exception e) {
            // The configuration is already stored at this point, so failing the request would be misleading.
            LOG.warn("Saved the threshd configuration but could not send the reload event for {}.", CONFIG_FILE_NAME, e);
        }
    }
}
