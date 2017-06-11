/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2007-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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
        String testKey = (String)parameters.get(CollectdIntegrationTest.TEST_KEY_PARM_NAME);
        if (testKey != null) {
            CollectdIntegrationTest.setServiceCollectorInTest(testKey, this);
        }
        s_collectCount++;
        return new CollectionSetBuilder(agent).build();
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
