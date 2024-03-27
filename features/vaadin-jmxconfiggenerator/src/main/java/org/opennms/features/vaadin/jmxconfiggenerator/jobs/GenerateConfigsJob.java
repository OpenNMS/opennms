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

import java.util.Collection;
import java.util.Objects;

import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;
import org.opennms.features.jmxconfiggenerator.log.Slf4jLogAdapter;
import org.opennms.features.vaadin.jmxconfiggenerator.JmxConfigGeneratorUI;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;

/**
 * Job to generate the configs needed.
 */
public class GenerateConfigsJob implements Task {

    private final UiModel model;
    private final JmxConfigGeneratorUI ui;

    public GenerateConfigsJob(JmxConfigGeneratorUI ui, UiModel model) {
        this.ui = Objects.requireNonNull(ui);
        this.model = Objects.requireNonNull(model);
    }

    @Override
    public Void execute() throws TaskRunException {
        // create snmp-graph.properties
        GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator(new Slf4jLogAdapter(GraphConfigGenerator.class));
        Collection<Report> reports = new JmxConfigReader(new Slf4jLogAdapter(JmxConfigReader.class)).generateReportsByJmxDatacollectionConfig(model.getOutputConfig());
        model.setSnmpGraphProperties(graphConfigGenerator.generateSnmpGraph(reports));
        model.updateOutput();
        return null;
    }

    @Override
    public void onSuccess(Object result) {
        ui.updateView(UiState.ResultView);
    }

    @Override
    public void onError() {

    }

    @Override
    public JmxConfigGeneratorUI getUI() {
        if (!ui.isAttached()) {
            throw new IllegalStateException("UI " + ui.getUIId() + " is detached.");
        }
        return ui;
    }
}
