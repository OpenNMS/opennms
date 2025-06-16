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
package org.opennms.features.apilayer.config;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.opennms.integration.api.v1.config.poller.PollerConfigurationExtension;
import org.opennms.netmgt.config.PollerConfig;
import org.opennms.netmgt.config.poller.Downtime;
import org.opennms.netmgt.config.poller.ExcludeRange;
import org.opennms.netmgt.config.poller.IncludeRange;
import org.opennms.netmgt.config.poller.Monitor;
import org.opennms.netmgt.config.poller.Package;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Rrd;
import org.opennms.netmgt.config.poller.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

public class PollerConfExtensionManager extends ConfigExtensionManager<PollerConfigurationExtension, PollerConfExtensionManager.PollerConfigurationPart> {

    public static class PollerConfigurationPart {
        private List<Package> packages = new ArrayList<>();
        private List<Monitor> monitors = new ArrayList<>();;

        public List<Package> getPackages() {
            return packages;
        }

        public List<Monitor> getMonitors() {
            return monitors;
        }
    }

    private final PollerConfig pollerConfig;

    private final EventForwarder eventForwarder;

    public PollerConfExtensionManager(final PollerConfig pollerConfig,
                                      final EventForwarder eventForwarder) {
        super(PollerConfigurationPart.class, new PollerConfigurationPart());
        this.pollerConfig = Objects.requireNonNull(pollerConfig);
        this.eventForwarder = Objects.requireNonNull(eventForwarder);
    }

    @Override
    protected PollerConfigurationPart getConfigForExtensions(Set<PollerConfigurationExtension> extensions) {
        PollerConfigurationPart pollerConfigurationPart = new PollerConfigurationPart();
        pollerConfigurationPart.packages = extensions.stream().flatMap(pce -> pce.getPackages().stream()).map(PollerConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        pollerConfigurationPart.monitors = extensions.stream().flatMap(pce -> pce.getMonitors().stream()).map(PollerConfExtensionManager::map).collect(Collectors.toUnmodifiableList());
        return pollerConfigurationPart;
    }

    @Override
    protected void triggerReload() {
        final PollerConfigurationPart object = getObject();
        this.pollerConfig.setExternalData(object.packages, object.monitors);

        this.eventForwarder.sendNow(new EventBuilder(EventConstants.RELOAD_DAEMON_CONFIG_UEI, "oia-plugins")
                                            .addParam(EventConstants.PARM_DAEMON_NAME, "pollerd")
                                            .getEvent());
    }

    public static Monitor map(org.opennms.integration.api.v1.config.poller.Monitor apiMonitor) {
        Monitor result = new Monitor();
        result.setClassName(apiMonitor.getClassName());
        result.setParameters(apiMonitor.getParameters().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setService(apiMonitor.getService());
        return result;
    }

    public static Parameter map(org.opennms.integration.api.v1.config.poller.Parameter apiParameter) {
        Parameter result = new Parameter();
        result.setKey(apiParameter.getKey());
        result.setValue(apiParameter.getValue());
        return result;
    }

    public static Package map(org.opennms.integration.api.v1.config.poller.Package apiPackage) {
        Package result = new Package();
        result.setDowntimes(apiPackage.getDowntimes().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setName(apiPackage.getName());
        result.setFilter(apiPackage.getFilter());
        result.setSpecifics(apiPackage.getSpecifics());
        result.setIncludeRanges(apiPackage.getIncludeRanges().stream().map(PollerConfExtensionManager::mapIncludeRanges).collect(Collectors.toList()));
        result.setExcludeRanges(apiPackage.getExcludeRanges().stream().map(PollerConfExtensionManager::mapExcludeRanges).collect(Collectors.toList()));
        result.setRrd(map(apiPackage.getRrd()));
        result.setServices(apiPackage.getServices().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setOutageCalendars(apiPackage.getOutageCalendars());
        return result;
    }

    private static Service map(org.opennms.integration.api.v1.config.poller.Service apiService) {
        Service result = new Service();
        result.setName(apiService.getName());
        result.setUserDefined(apiService.isUserDefined() ? "true" : "false");
        result.setStatus(apiService.isEnabled() ? "on" : "off");
        result.setPattern(apiService.getPattern().orElse(null));
        result.setParameters(apiService.getParameters().stream().map(PollerConfExtensionManager::map).collect(Collectors.toList()));
        result.setInterval(apiService.getInterval());
        return result;
    }


    private static IncludeRange mapIncludeRanges(org.opennms.integration.api.v1.config.poller.AddressRange addressRange) {
        final IncludeRange result = new IncludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

    private static ExcludeRange mapExcludeRanges(org.opennms.integration.api.v1.config.poller.AddressRange addressRange) {
        final ExcludeRange result = new ExcludeRange();
        result.setBegin(addressRange.getBegin());
        result.setEnd(addressRange.getEnd());
        return result;
    }

    public static Downtime map(org.opennms.integration.api.v1.config.poller.Downtime apiDowntime) {
        Downtime result = new Downtime();
        result.setBegin(apiDowntime.getBegin().getSeconds());
        result.setInterval(apiDowntime.getInterval().orElse(null));
        result.setEnd(apiDowntime.getEnd().map(Duration::getSeconds).orElse(null));
        if (apiDowntime.getDelete().isPresent()) {
            final org.opennms.integration.api.v1.config.poller.Downtime.DeletingMode deletingMode = apiDowntime.getDelete().get();
            switch (deletingMode) {
                case ALWAYS:
                    result.setDelete(Downtime.DELETE_ALWAYS);
                    break;
                case MANAGED:
                    result.setDelete(Downtime.DELETE_MANAGED);
                    break;
                case NEVER:
                    result.setDelete(Downtime.DELETE_NEVER);
                    break;
            }
        } else {
            result.setDelete(null);
        }

        return result;
    }

    public static Rrd map(org.opennms.integration.api.v1.config.poller.Rrd apiRrd) {
        Rrd result = new Rrd();
        result.setStep(apiRrd.getStep());
        result.setRras(apiRrd.getRras());
        return result;
    }
}
