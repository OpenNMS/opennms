package org.opennms.netmgt.jmx;

import org.opennms.netmgt.config.JMXDataCollectionConfigDao;
import org.opennms.netmgt.config.collectd.jmx.JmxCollection;
import org.opennms.netmgt.config.collectd.jmx.JmxDatacollectionConfig;
import org.opennms.netmgt.jmx.connection.MBeanServerConnectionException;
import org.springframework.core.io.FileSystemResource;

import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

// TODO mvr remove this :)
public class Main {

    private static final String COLLECTION_NAME = "jsr160";

    public static void main(String[] args) throws URISyntaxException, InterruptedException, MBeanServerConnectionException {
        Main main = new Main();
        main.collect();
    }

    private final JMXDataCollectionConfigDao configDao;

    private final JmxDatacollectionConfig config;

    private Main() throws URISyntaxException {
        configDao = createDao();
        config = configDao.getConfig();
    }

    private void collect() throws InterruptedException, MBeanServerConnectionException {
        WiuJmxConfig config = new WiuJmxConfig();
        config.setAgentAddress("192.168.2.10");
        config.setRetries(3);
        config.setServiceProperties(createServiceProperties());
        config.setJmxCollection(getJmxCollection(COLLECTION_NAME));

        JmxSampleProcessor sampleProcessor = new JmxSampleProcessor() {
            @Override
            public void process(AttributeSample attributeSample) {
                System.out.println("Found attribute sample: " + attributeSample);
            }

            @Override
            public void process(CompositeSample compositeSample) {
                System.out.println("Found composite sample: " + compositeSample);
            }
        };

        WiuJmxCollector defaultJmxCollector = new WiuDefaultJmxCollector();
        defaultJmxCollector.collect(config, sampleProcessor);
    }

    private JMXDataCollectionConfigDao createDao() throws URISyntaxException {
        JMXDataCollectionConfigDao dao = new JMXDataCollectionConfigDao();
        Path configPath = Paths.get(Main.class.getResource("/jmx-datacollection-config.xml").toURI());
        dao.setConfigResource(new FileSystemResource(configPath.toString()));
        dao.afterPropertiesSet();
        return dao;
    }

    private JmxCollection getJmxCollection(String collectionName) {
        for (JmxCollection eachCollection : config.getJmxCollection()) {
            if (eachCollection != null && collectionName.equals(eachCollection.getName())) {
                return eachCollection;
            }
        }
        return null;
    }

    private Map<String, String> createServiceProperties() {
        Map<String, String> properties = new HashMap<>();

        properties.put("port", "18980");
        properties.put("retry", "2");
        properties.put("timeout", "3000");
        properties.put("protocol", "rmi");
        properties.put("urlPath", "/jmxrmi");
        properties.put("rrd-base-name", "java");
        properties.put("ds-name", "opennms-jvm");
        properties.put("friendly-name", "opennms-jvm");
        properties.put("collection", "jsr160");
        properties.put("thresholding-enabled", "true");

        return properties;

    }

}
