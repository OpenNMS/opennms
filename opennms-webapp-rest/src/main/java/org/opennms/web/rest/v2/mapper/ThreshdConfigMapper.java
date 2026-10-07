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
package org.opennms.web.rest.v2.mapper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.opennms.netmgt.config.threshd.ExcludeRange;
import org.opennms.netmgt.config.threshd.Filter;
import org.opennms.netmgt.config.threshd.IncludeRange;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.Parameter;
import org.opennms.netmgt.config.threshd.Service;
import org.opennms.netmgt.config.threshd.ServiceStatus;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.web.rest.v2.model.IpRangeDto;
import org.opennms.web.rest.v2.model.ParameterDto;
import org.opennms.web.rest.v2.model.ThreshdConfigDto;
import org.opennms.web.rest.v2.model.ThreshdPackageDto;
import org.opennms.web.rest.v2.model.ThreshdPackageSummaryDto;
import org.opennms.web.rest.v2.model.ThreshdServiceDto;

/**
 * Converts between the JAXB threshd configuration model and the REST DTOs.
 */
public abstract class ThreshdConfigMapper {

    private ThreshdConfigMapper() {
    }

    // ------------------------------------------------------------------ to DTO

    public static ThreshdConfigDto toDto(final ThreshdConfiguration config) {
        final ThreshdConfigDto dto = new ThreshdConfigDto();
        dto.setPackages(config.getPackages().stream()
                .map(ThreshdConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static ThreshdPackageSummaryDto toSummaryDto(final Package pkg) {
        final ThreshdPackageSummaryDto dto = new ThreshdPackageSummaryDto();
        dto.setName(pkg.getName());
        dto.setFilter(filterContent(pkg.getFilter()));
        dto.setServiceCount(pkg.getServices().size());
        dto.setOutageCalendars(new ArrayList<>(pkg.getOutageCalendars()));
        return dto;
    }

    public static ThreshdPackageDto toDto(final Package pkg) {
        final ThreshdPackageDto dto = new ThreshdPackageDto();
        dto.setName(pkg.getName());
        dto.setFilter(filterContent(pkg.getFilter()));
        dto.setSpecifics(new ArrayList<>(pkg.getSpecifics()));
        dto.setIncludeRanges(pkg.getIncludeRanges().stream()
                .map(range -> toRangeDto(range.getBegin(), range.getEnd()))
                .collect(Collectors.toList()));
        dto.setExcludeRanges(pkg.getExcludeRanges().stream()
                .map(range -> toRangeDto(range.getBegin(), range.getEnd()))
                .collect(Collectors.toList()));
        dto.setIncludeUrls(new ArrayList<>(pkg.getIncludeUrls()));
        dto.setServices(pkg.getServices().stream()
                .map(ThreshdConfigMapper::toDto)
                .collect(Collectors.toList()));
        dto.setOutageCalendars(new ArrayList<>(pkg.getOutageCalendars()));
        return dto;
    }

    public static ThreshdServiceDto toDto(final Service service) {
        final ThreshdServiceDto dto = new ThreshdServiceDto();
        dto.setName(service.getName());
        dto.setInterval(service.getInterval());
        dto.setUserDefined(service.getUserDefined());
        dto.setStatus(service.getStatus().map(status -> status.name().toLowerCase(Locale.ROOT)).orElse(null));
        dto.setParameters(service.getParameters().stream()
                .map(ThreshdConfigMapper::toDto)
                .collect(Collectors.toList()));
        return dto;
    }

    public static ParameterDto toDto(final Parameter parameter) {
        final ParameterDto dto = new ParameterDto();
        dto.setKey(parameter.getKey());
        dto.setValue(parameter.getValue());
        return dto;
    }

    private static IpRangeDto toRangeDto(final String begin, final String end) {
        final IpRangeDto dto = new IpRangeDto();
        dto.setBegin(begin);
        dto.setEnd(end);
        return dto;
    }

    private static String filterContent(final Filter filter) {
        return filter == null ? null : filter.getContent().orElse(null);
    }

    // --------------------------------------------------------------- to entity

    public static ThreshdConfiguration toEntity(final ThreshdConfigDto dto) {
        final ThreshdConfiguration config = new ThreshdConfiguration();

        final List<Package> packages = new ArrayList<>();
        if (dto.getPackages() != null) {
            for (final ThreshdPackageDto packageDto : dto.getPackages()) {
                packages.add(toEntity(packageDto));
            }
        }
        config.setPackages(packages);

        return config;
    }

    public static Package toEntity(final ThreshdPackageDto dto) {
        final Package pkg = new Package();
        pkg.setName(trimToNull(dto.getName()));

        // The setter rejects null outright, so a blank filter leaves the field unset and is caught by
        // validation rather than by an IllegalArgumentException from deep inside the model.
        final String filter = trimToNull(dto.getFilter());
        if (filter != null) {
            pkg.setFilter(new Filter(filter));
        }

        pkg.setSpecifics(trimmedStrings(dto.getSpecifics()));

        final List<IncludeRange> includeRanges = new ArrayList<>();
        if (dto.getIncludeRanges() != null) {
            for (final IpRangeDto range : dto.getIncludeRanges()) {
                final IncludeRange includeRange = new IncludeRange();
                includeRange.setBegin(trimToNull(range.getBegin()));
                includeRange.setEnd(trimToNull(range.getEnd()));
                includeRanges.add(includeRange);
            }
        }
        pkg.setIncludeRanges(includeRanges);

        final List<ExcludeRange> excludeRanges = new ArrayList<>();
        if (dto.getExcludeRanges() != null) {
            for (final IpRangeDto range : dto.getExcludeRanges()) {
                final ExcludeRange excludeRange = new ExcludeRange();
                excludeRange.setBegin(trimToNull(range.getBegin()));
                excludeRange.setEnd(trimToNull(range.getEnd()));
                excludeRanges.add(excludeRange);
            }
        }
        pkg.setExcludeRanges(excludeRanges);

        pkg.setIncludeUrls(trimmedStrings(dto.getIncludeUrls()));

        final List<Service> services = new ArrayList<>();
        if (dto.getServices() != null) {
            for (final ThreshdServiceDto serviceDto : dto.getServices()) {
                services.add(toEntity(serviceDto));
            }
        }
        pkg.setServices(services);

        pkg.setOutageCalendars(trimmedStrings(dto.getOutageCalendars()));

        return pkg;
    }

    public static Service toEntity(final ThreshdServiceDto dto) {
        final Service service = new Service();
        service.setName(trimToNull(dto.getName()));
        service.setInterval(dto.getInterval());
        service.setUserDefined(dto.getUserDefined());
        service.setStatus(toServiceStatus(dto.getStatus()));
        service.setParameters(toParameterEntities(dto.getParameters()));
        return service;
    }

    public static ServiceStatus toServiceStatus(final String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        for (final ServiceStatus status : ServiceStatus.values()) {
            if (status.name().equalsIgnoreCase(value.trim())) {
                return status;
            }
        }
        return null;
    }

    private static List<Parameter> toParameterEntities(final List<ParameterDto> dtos) {
        final List<Parameter> parameters = new ArrayList<>();
        if (dtos != null) {
            for (final ParameterDto parameterDto : dtos) {
                final Parameter parameter = new Parameter();
                parameter.setKey(trimToNull(parameterDto.getKey()));
                parameter.setValue(parameterDto.getValue());
                parameters.add(parameter);
            }
        }
        return parameters;
    }

    private static List<String> trimmedStrings(final List<String> values) {
        final List<String> result = new ArrayList<>();
        if (values != null) {
            for (final String value : values) {
                final String trimmed = trimToNull(value);
                if (trimmed != null) {
                    result.add(trimmed);
                }
            }
        }
        return result;
    }

    private static String trimToNull(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
