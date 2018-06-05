/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;

import javax.management.JMException;

import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxHelper;
import org.opennms.features.jmxconfiggenerator.jmxconfig.query.MBeanServerQueryException;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ServiceConfig;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;
import org.opennms.netmgt.config.collectd.jmx.Attrib;
import org.opennms.netmgt.config.collectd.jmx.CompAttrib;
import org.opennms.netmgt.config.collectd.jmx.CompMember;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.config.collectd.jmx.Mbean;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfig;
import org.opennms.netmgt.jmx.connection.JmxConnectionConfigBuilder;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionException;
import org.opennms.netmgt.jmx.connection.JmxServerConnectionWrapper;
import org.opennms.netmgt.jmx.impl.connection.connectors.DefaultJmxConnector;
import org.opennms.netmgt.vaadin.core.UIHelper;

/**
 * Runnable to query the MBean server.
 */
public class DetectMBeansJob implements JobManager.Task<JmxDatacollectionConfig> {

    private interface Filter<X> {
        void apply(X input);
    }

    private final Map<Class<?>, Filter<?>> filterMap;

    private final ServiceConfig config;

    public DetectMBeansJob(ServiceConfig config) {
        this.config = config;
        this.filterMap = new HashMap<>();
        filterMap.put(Mbean.class, new Filter<Mbean>() {

            @Override
            public void apply(Mbean input) {
                // The default PooledDataSource name is "com.mchange.v2.c3p0.PooledDataSource[1hge1gv9a1li8lwdjzwyop|290e7d09]".
                // We remove the weired part at the end
                if (input.getName().contains("com.mchange.v2.c3p0.PooledDataSource")) {
                    input.setName("com.mchange.v2.c3p0.PooledDataSource");
                }
            }
        });

        filterMap.put(Attrib.class, new Filter<Attrib>() {
            @Override
            public void apply(Attrib input) {
                // The mbean "PooledDataSource" has already a "0numFailChecDfltUsr" alias. Therefore the
                // JmxConfiggenerator created a "NAME_CRASH"-alias. We manually overwrite the value here to a valid one
                if ("0numFailedCheckinsDfltUsr_NAME_CRASH_AS_19_CHAR_VALUE".equals(input.getAlias())
                        && "numFailedCheckinsDefaultUser".equals(input.getName())) {
                    input.setAlias("1numFailChecDfltUsr");
                }
            }
        });
    }

    @Override
    public JmxDatacollectionConfig execute() throws JobManager.TaskRunException {
        final JmxConnectionConfig connectionConfig = new JmxConnectionConfigBuilder()
                .withUrl(config.getConnection())
                .withUsername(config.getUser())
                .withPassword(config.getPassword())
                .build();


        try (JmxServerConnectionWrapper connector = new DefaultJmxConnector().createConnection(connectionConfig)) {
                final JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator(new Slf4jLogAdapter(JmxDatacollectionConfiggenerator.class));
                final JmxDatacollectionConfig generatedJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(
                        connector.getMBeanServerConnection(),
                        "anyservice",
                        !config.isSkipDefaultVM(),
                        config.isSkipNonNumber(),
                        JmxHelper.loadInternalDictionary());
                applyFilters(generatedJmxConfigModel);
                return generatedJmxConfigModel;
        } catch (IOException | MBeanServerQueryException | JMException | JmxServerConnectionException e) {
            if (e instanceof UnknownHostException || e.getCause() instanceof UnknownHostException) {
                throw new JobManager.TaskRunException(String.format("Unknown host: %s", config.getConnection()), e);
            }
            if (e instanceof MalformedURLException || e.getCause() instanceof MalformedURLException) {
                throw new JobManager.TaskRunException(
                        String.format("Cannot create valid JMX Connection URL. Connection: '%s'", config.getConnection()),
                        e);
            }
            throw new JobManager.TaskRunException("Error while retrieving MBeans from server.", e);
        }
    }

    @Override
    public void onSuccess(JmxDatacollectionConfig generatedJmxConfigModel) {
        UIHelper.getCurrent(JmxConfigGeneratorUI.class).setRawModel(generatedJmxConfigModel);
        UIHelper.getCurrent(JmxConfigGeneratorUI.class).updateView(UiState.MbeansView);
    }

    @Override
    public void onError() {

    }

    private <T> void applyFilters(T input) {
        Filter<T> filter = (Filter<T>) filterMap.get(input.getClass());
        if (filter != null) {
            filter.apply(input);
        }
    }

    /**
     * The generated values for the JMX datacollection may not be correct. In detail the name/alias fields
     * of MBeans/Attribs/CompMembers/CompAttribs may be too long or not unique in the whole JMX datacollection.
     * In order to keep the usability up, this method allows to filter values retrieved from the
     * {@link javax.management.MBeanServerConnection} to fix naming issues.
     *
     * @param config The {@link JmxDatacollectionConfig} retrieved from the {@link javax.management.MBeanServerConnection}.
     */
    private void applyFilters(JmxDatacollectionConfig config) {
        for (Mbean eachMbean : config.getJmxCollectionList().get(0).getMbeans()) {
            applyFilters(eachMbean);
            for (Attrib eachAttrib : eachMbean.getAttribList()) {
                applyFilters(eachAttrib);
            }
            for (CompAttrib eachCompAttrib : eachMbean.getCompAttribList()) {
                applyFilters(eachCompAttrib);
                for (CompMember eachCompMember : eachCompAttrib.getCompMemberList()) {
                    applyFilters(eachCompMember);
                }
            }
        }
    }
}
