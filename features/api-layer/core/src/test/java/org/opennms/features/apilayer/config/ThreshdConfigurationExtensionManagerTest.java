/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.features.apilayer.config;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.junit.Test;
import org.opennms.integration.api.v1.config.thresholding.ExcludeRange;
import org.opennms.integration.api.v1.config.thresholding.Filter;
import org.opennms.integration.api.v1.config.thresholding.IncludeRange;
import org.opennms.integration.api.v1.config.thresholding.PackageDefinition;
import org.opennms.integration.api.v1.config.thresholding.Parameter;
import org.opennms.integration.api.v1.config.thresholding.Service;
import org.opennms.integration.api.v1.config.thresholding.ServiceStatus;
import org.opennms.integration.api.v1.config.thresholding.ThreshdConfigurationExtension;
import org.opennms.netmgt.config.dao.thresholding.api.WriteableThreshdDao;
import org.opennms.netmgt.config.threshd.Package;
import org.opennms.netmgt.config.threshd.ThreshdConfiguration;
import org.opennms.netmgt.threshd.api.ThresholdingService;
import org.opennms.netmgt.threshd.api.ThresholdingSetPersister;

public class ThreshdConfigurationExtensionManagerTest {
    /**
     * Tests that mapping from API type to OpenNMS type works and multiple configurations provided by extensions are
     * merged together.
     */
    @Test
    public void canProvideMergedConfiguration() {
        WriteableThreshdDao threshdDao = mock(WriteableThreshdDao.class);
        ThresholdingService thresholdingService = mock(ThresholdingService.class);
        when(thresholdingService.getThresholdingSetPersister()).thenReturn(mock(ThresholdingSetPersister.class));
        ThreshdConfigurationExtensionManager manager = new ThreshdConfigurationExtensionManager(threshdDao,
                thresholdingService);

        // Shouldn't have config yet
        ThreshdConfiguration threshdConfiguration = manager.getObject();
        assertThat(threshdConfiguration.getPackages(), hasSize(0));

        // Expose an extension
        ThreshdConfigurationExtension ext1 = mock(ThreshdConfigurationExtension.class);

        PackageDefinition packageDefinition1 = mock(PackageDefinition.class);

        ExcludeRange excludeRange = mock(ExcludeRange.class);
        String excludeRangeBegin = "0.0.0.0";
        when(excludeRange.getBegin()).thenReturn(excludeRangeBegin);
        String excludeRangeEnd = "1.1.1.1";
        when(excludeRange.getEnd()).thenReturn(excludeRangeEnd);
        when(packageDefinition1.getExcludeRanges()).thenReturn(Collections.singletonList(excludeRange));

        IncludeRange includeRange = mock(IncludeRange.class);
        String includeRangeBegin = "2.2.2.2";
        when(includeRange.getBegin()).thenReturn(includeRangeBegin);
        String includeRangeEnd = "3.3.3.3";
        when(includeRange.getEnd()).thenReturn(includeRangeEnd);
        when(packageDefinition1.getIncludeRanges()).thenReturn(Collections.singletonList(includeRange));

        Filter filter1 = mock(Filter.class);
        String filter1Content = "filter 1 content";
        when(filter1.getContent()).thenReturn(Optional.of(filter1Content));
        when(packageDefinition1.getFilter()).thenReturn(filter1);

        String includeUrl = "include url";
        when(packageDefinition1.getIncludeUrls()).thenReturn(Collections.singletonList(includeUrl));

        String package1Name = "TestPackage1";
        when(packageDefinition1.getName()).thenReturn(package1Name);

        String outageCalendar = "outage calendar";
        when(packageDefinition1.getOutageCalendars()).thenReturn(Collections.singletonList(outageCalendar));

        Service service = mock(Service.class);
        long serviceInterval = 1000;
        when(service.getInterval()).thenReturn(serviceInterval);

        String serviceName = "service name";
        when(service.getName()).thenReturn(serviceName);

        Parameter parameter = mock(Parameter.class);
        String paramKey = "param key";
        when(parameter.getKey()).thenReturn(paramKey);
        String paramVal = "param value";
        when(parameter.getValue()).thenReturn(paramVal);
        when(service.getParameters()).thenReturn(Collections.singletonList(parameter));

        when(service.getStatus()).thenReturn(Optional.of(ServiceStatus.ON));
        when(service.getUserDefined()).thenReturn(true);

        when(packageDefinition1.getServices()).thenReturn(Collections.singletonList(service));

        String specific = "specific";
        when(packageDefinition1.getSpecifics()).thenReturn(Collections.singletonList(specific));

        when(ext1.getPackages()).thenReturn(Collections.singletonList(packageDefinition1));

        manager.onBind(ext1, Collections.emptyMap());

        // Should be configuration now
        threshdConfiguration = manager.getObject();
        assertThat(threshdConfiguration.getPackages(), hasSize(1));
        Package extPackage = threshdConfiguration.getPackages().get(0);

        assertThat(extPackage.getName(), equalTo(package1Name));
        assertThat(extPackage.getExcludeRanges().get(0).getBegin(), equalTo(excludeRangeBegin));
        assertThat(extPackage.getExcludeRanges().get(0).getEnd(), equalTo(excludeRangeEnd));
        assertThat(extPackage.getIncludeRanges().get(0).getBegin(), equalTo(includeRangeBegin));
        assertThat(extPackage.getIncludeRanges().get(0).getEnd(), equalTo(includeRangeEnd));
        assertThat(extPackage.getFilter().getContent().get(), equalTo(filter1Content));
        assertThat(extPackage.getOutageCalendars().get(0), equalTo(outageCalendar));
        assertThat(extPackage.getServices().get(0).getName(), equalTo(serviceName));
        assertThat(extPackage.getServices().get(0).getInterval(), equalTo(serviceInterval));
        assertThat(extPackage.getServices().get(0).getParameters().get(0).getKey(), equalTo(paramKey));
        assertThat(extPackage.getServices().get(0).getParameters().get(0).getValue(), equalTo(paramVal));
        assertThat(extPackage.getServices().get(0).getStatus().get(),
                equalTo(org.opennms.netmgt.config.threshd.ServiceStatus.ON));
        assertThat(extPackage.getServices().get(0).getUserDefined(), equalTo(true));
        assertThat(extPackage.getSpecifics().get(0), equalTo(specific));

        assertThat(extPackage.getIncludeUrls().get(0), equalTo(includeUrl));

        // Expose another extension
        ThreshdConfigurationExtension ext2 = mock(ThreshdConfigurationExtension.class);

        PackageDefinition packageDefinition2 = mock(PackageDefinition.class);
        when(packageDefinition2.getExcludeRanges()).thenReturn(Collections.emptyList());
        Filter filter2 = mock(Filter.class);
        when(filter2.getContent()).thenReturn(Optional.empty());
        when(packageDefinition2.getFilter()).thenReturn(filter2);
        when(packageDefinition2.getIncludeRanges()).thenReturn(Collections.emptyList());
        when(packageDefinition2.getIncludeUrls()).thenReturn(Collections.emptyList());
        String package2Name = "TestPackage2";
        when(packageDefinition2.getName()).thenReturn(package2Name);
        when(packageDefinition2.getOutageCalendars()).thenReturn(Collections.emptyList());
        when(packageDefinition2.getServices()).thenReturn(Collections.emptyList());
        when(packageDefinition2.getSpecifics()).thenReturn(Collections.emptyList());

        when(ext2.getPackages()).thenReturn(Collections.singletonList(packageDefinition2));
        manager.onBind(ext2, Collections.emptyMap());

        // Should see the merged results now
        threshdConfiguration = manager.getObject();
        List<String> packageNames = threshdConfiguration.getPackages()
                .stream()
                .map(Package::getName)
                .collect(Collectors.toList());
        assertThat(packageNames, hasItems(package1Name, package2Name));

        // Unbind ext2
        manager.onUnbind(ext2, Collections.emptyMap());
        threshdConfiguration = manager.getObject();
        assertThat(threshdConfiguration.getPackages(), hasSize(1));

        // Unbind ext1
        manager.onUnbind(ext1, Collections.emptyMap());
        threshdConfiguration = manager.getObject();
        assertThat(threshdConfiguration.getPackages(), hasSize(0));
    }
}
