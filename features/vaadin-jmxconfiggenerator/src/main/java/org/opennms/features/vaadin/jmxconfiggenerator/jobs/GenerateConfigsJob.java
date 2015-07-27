package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import org.opennms.features.jmxconfiggenerator.graphs.GraphConfigGenerator;
import org.opennms.features.jmxconfiggenerator.graphs.JmxConfigReader;
import org.opennms.features.jmxconfiggenerator.graphs.Report;
import org.opennms.features.vaadin.jmxconfiggenerator.data.UiModel;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;

import java.io.IOException;
import java.util.Collection;

/**
 * Job to generate the configs needed.
 */
public class GenerateConfigsJob implements JobManager.Task {

    private final UiModel model;

    public GenerateConfigsJob(UiModel model) {
        this.model = model;
    }

    @Override
    public Void execute() throws JobManager.TaskRunException {
        try {
            // create snmp-graph.properties
            GraphConfigGenerator graphConfigGenerator = new GraphConfigGenerator();
            Collection<Report> reports = new JmxConfigReader().generateReportsByJmxDatacollectionConfig(model.getOutputConfig());
            model.setSnmpGraphProperties(graphConfigGenerator.generateSnmpGraph(reports));
            model.updateOutput();
            return null;
        } catch (IOException ex) {
            throw new JobManager.TaskRunException("SNMP Graph-Properties couldn't be created.", ex);
        }
    }

    @Override
    public void onSuccess(Object result) {
        UIHelper.updateView(UiState.ResultView);
    }

    @Override
    public void onError() {

    }
}
