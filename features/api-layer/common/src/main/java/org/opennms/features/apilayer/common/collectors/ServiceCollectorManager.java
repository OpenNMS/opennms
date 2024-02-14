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
package org.opennms.features.apilayer.common.collectors;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.opennms.features.apilayer.common.utils.InterfaceMapper;
import org.opennms.integration.api.v1.collectors.ServiceCollectorFactory;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.model.ResourceTypeUtils;
import org.opennms.netmgt.rrd.RrdRepository;
import org.osgi.framework.BundleContext;

public class ServiceCollectorManager extends InterfaceMapper<ServiceCollectorFactory, ServiceCollector> {
    private final int rrdStep;
    private final int rrdHeartBeat;
    private final String rrdRraStr;

    public ServiceCollectorManager(BundleContext bundleContext, int rrdStep, int rrdHeartBeat, String rrdRrsStr) {
        super(ServiceCollector.class, bundleContext);
        this.rrdStep = rrdStep;
        this.rrdHeartBeat = rrdHeartBeat;
        this.rrdRraStr = rrdRrsStr;
    }

    @Override
    public ServiceCollector map(ServiceCollectorFactory ext) {
        RrdRepository rrdRepository = new RrdRepository();
        rrdRepository.setStep(rrdStep);
        rrdRepository.setHeartBeat(rrdHeartBeat);
        rrdRepository.setRraList(Arrays.asList(rrdRraStr.split(",")));
        rrdRepository.setRrdBaseDir(new File(ResourceTypeUtils.DEFAULT_RRD_ROOT, ResourceTypeUtils.SNMP_DIRECTORY));
        return new ServiceCollectorImpl(ext, rrdRepository);
    }

    // override as registry needs collector class name in properties.
    @Override
    public Map<String, Object> getServiceProperties(ServiceCollectorFactory extension) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", extension.getCollectorClassName());
        return properties;
    }
}
