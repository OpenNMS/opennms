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
package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

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

/**
 * Runnable to query the MBean server.
 */
public class DetectMBeansJob implements Task<JmxDatacollectionConfig> {

    private final Map<Class<?>, Consumer<?>> filterMap = new HashMap<>();
    private final JmxConfigGeneratorUI ui;
    private final ServiceConfig config;

    public DetectMBeansJob(JmxConfigGeneratorUI ui, ServiceConfig config) {
        this.ui = Objects.requireNonNull(ui);
        this.config = Objects.requireNonNull(config);
        filterMap.put(Mbean.class, (Consumer<Mbean>) input -> {
            // The default PooledDataSource name is "com.mchange.v2.c3p0.PooledDataSource[1hge1gv9a1li8lwdjzwyop|290e7d09]".
            // We remove the weired part at the end
            if (input.getName().contains("com.mchange.v2.c3p0.PooledDataSource")) {
                input.setName("com.mchange.v2.c3p0.PooledDataSource");
            }
        });
        filterMap.put(Attrib.class, (Consumer<Attrib>) input -> {
            // The mbean "PooledDataSource" has already a "0numFailChecDfltUsr" alias. Therefore the
            // JmxConfiggenerator created a "NAME_CRASH"-alias. We manually overwrite the value here to a valid one
            if ("0numFailedCheckinsDfltUsr_NAME_CRASH_AS_19_CHAR_VALUE".equals(input.getAlias())
                    && "numFailedCheckinsDefaultUser".equals(input.getName())) {
                input.setAlias("1numFailChecDfltUsr");
            }
        });
    }

    @Override
    public JmxDatacollectionConfig execute() throws TaskRunException {
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
                throw new TaskRunException(String.format("Unknown host: %s", config.getConnection()), e);
            }
            if (e instanceof MalformedURLException || e.getCause() instanceof MalformedURLException) {
                throw new TaskRunException(
                        String.format("Cannot create valid JMX Connection URL. Connection: '%s'", config.getConnection()),
                        e);
            }
            throw new TaskRunException("Error while retrieving MBeans from server.", e);
        }
    }

    @Override
    public void onSuccess(JmxDatacollectionConfig generatedJmxConfigModel) {
        ui.setRawModel(generatedJmxConfigModel);
        ui.updateView(UiState.MbeansView);
    }

    @Override
    public void onError() {

    }

    @Override
    public JmxConfigGeneratorUI getUI() {
        if (ui.isAttached()) {
            return ui;
        }
        throw new IllegalStateException("UI " + ui.getUIId() + " is not attached");
    }

    private <T> void applyFilters(T input) {
        Consumer<T> filter = (Consumer<T>) filterMap.get(input.getClass());
        if (filter != null) {
            filter.accept(input);
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
