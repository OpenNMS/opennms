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
package org.opennms.netmgt.collectd;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import org.opennms.netmgt.collection.api.CollectionAgent;
import org.opennms.netmgt.collection.api.CollectionInitializationException;
import org.opennms.netmgt.collection.api.CollectionSet;
import org.opennms.netmgt.collection.api.ServiceCollector;
import org.opennms.netmgt.collection.support.builder.CollectionSetBuilder;
import org.opennms.netmgt.rrd.RrdRepository;

public class MockServiceCollector implements ServiceCollector {

    private static ServiceCollector s_delegate = null;

    private static int s_collectCount = 0;
    
    public MockServiceCollector() {
        System.err.println("Created a MockServiceCollector");
    }

    @Override
    public CollectionSet collect(CollectionAgent agent, Map<String, Object> parameters) {
        String testKey = (String)parameters.get(CollectdMoreIT.TEST_KEY_PARM_NAME);
        if (testKey != null) {
            CollectdMoreIT.setServiceCollectorInTest(testKey, this);
        }
        s_collectCount++;

        if (s_delegate != null) {
            return s_delegate.collect(agent, parameters);
        } else {
            return new CollectionSetBuilder(agent).build();
        }
    }

    public int getCollectCount() {
        return s_collectCount;
    }
    
    public static void setDelegate(ServiceCollector delegate) {
        s_delegate = delegate;
    }

    @Override
    public RrdRepository getRrdRepository(String collectionName) {
        RrdRepository repo = new RrdRepository();
        repo.setRrdBaseDir(new File("target"));
        repo.setRraList(Collections.singletonList("RRA:AVERAGE:0.5:1:8928"));
        repo.setStep(300);
        repo.setHeartBeat(2 * 300);
        return repo;
    }

    @Override
    public void initialize() throws CollectionInitializationException {
        if (s_delegate != null) s_delegate.initialize();
    }

    @Override
    public void validateAgent(CollectionAgent agent, Map<String, Object> parameters)
            throws CollectionInitializationException {
        if (s_delegate != null) s_delegate.validateAgent(agent, parameters);
    }

    @Override
    public Map<String, Object> getRuntimeAttributes(CollectionAgent agent, Map<String, Object> parameters) {
        if (s_delegate != null) return s_delegate.getRuntimeAttributes(agent, parameters);
        return Collections.emptyMap();
    }

    @Override
    public String getEffectiveLocation(String location) {
        if (s_delegate != null) return s_delegate.getEffectiveLocation(location);
        return null;
    }

    @Override
    public Map<String, String> marshalParameters(Map<String, Object> parameters) {
        if (s_delegate != null) return s_delegate.marshalParameters(parameters);
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> unmarshalParameters(Map<String, String> parameters) {
        if (s_delegate != null) return s_delegate.unmarshalParameters(parameters);
        throw new UnsupportedOperationException();
    }
}
