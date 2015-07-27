package org.opennms.features.vaadin.jmxconfiggenerator.jobs;

import org.opennms.features.jmxconfiggenerator.Starter;
import org.opennms.features.jmxconfiggenerator.jmxconfig.JmxDatacollectionConfiggenerator;
import org.opennms.features.vaadin.jmxconfiggenerator.data.ServiceConfig;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UIHelper;
import org.opennms.features.vaadin.jmxconfiggenerator.ui.UiState;
import org.opennms.xmlns.xsd.config.jmx_datacollection.JmxDatacollectionConfig;

import javax.management.remote.JMXConnector;
import javax.management.remote.JMXServiceURL;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 * Runnable to query the mbeanserver.
 */
public class DetectMBeansJob implements JobManager.Task<JmxDatacollectionConfig> {

    private final ServiceConfig config;

    public DetectMBeansJob(ServiceConfig config) {
        this.config = config;
    }


    @Override
	public JmxDatacollectionConfig execute() throws JobManager.TaskRunException {
        try {
            JmxDatacollectionConfiggenerator jmxConfigGenerator = new JmxDatacollectionConfiggenerator();
            JMXServiceURL jmxServiceURL = jmxConfigGenerator.getJmxServiceURL(config.isJmxmp(), config.getHost(), config.getPort());

            try (JMXConnector connector = jmxConfigGenerator.getJmxConnector(config.getUser(), config.getPassword(), jmxServiceURL)) {
                final JmxDatacollectionConfig generatedJmxConfigModel = jmxConfigGenerator.generateJmxConfigModel(connector.getMBeanServerConnection(), "anyservice", !config.isSkipDefaultVM(), config.isRunWritableMBeans(), Starter.loadInternalDictionary());
                return generatedJmxConfigModel;
            } catch (IOException e) {
                throw new JobManager.TaskRunException("Error while retrieving MBeans from server.", e);
            }
        } catch (MalformedURLException e) {
            throw new JobManager.TaskRunException(
                    String.format("Cannot create valid JMX Connection URL. Host = '%s', Port = '%s', use jmxmp = %s", config.getHost(), config.getPort(), config.isJmxmp()),
                    e);
        }
	}

    @Override
    public void onSuccess(JmxDatacollectionConfig generatedJmxConfigModel) {
        UIHelper.getCurrent().setRawModel(generatedJmxConfigModel);
        UIHelper.getCurrent().updateView(UiState.MbeansView);
    }

    @Override
    public void onError() {

    }
}
