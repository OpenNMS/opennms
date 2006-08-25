package org.opennms.web.performance;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.opennms.core.resource.Vault;
import org.opennms.core.utils.ThreadCategory;

public class PerformanceModelFactory {

    private static boolean s_loaded;
    private static PerformanceModelFactory s_singleton;

    private PerformanceModel m_config;

    private PerformanceModelFactory(String configFile) throws IOException {
        InputStream in = new FileInputStream(configFile);
        marshal(in);
        in.close();
    }

    public PerformanceModelFactory(InputStream in) throws IOException {
        marshal(in);
    }

    private void marshal(InputStream in) throws IOException {
        m_config = new PerformanceModel(in);
    }

    public static void setInstance(PerformanceModelFactory instance) {
        s_singleton = instance;
        s_loaded = true;
    }
    
    public static PerformanceModelFactory getInstance() {
        return s_singleton;
    }
    
    public PerformanceModel getConfig() {
        return m_config;
    }

    public static synchronized void init() throws IOException {
        if (s_loaded) {
            return;
        }

        File cfgFile = new File(Vault.getHomeDir()
                                + PerformanceModel.RRDTOOL_GRAPH_PROPERTIES_FILENAME);
                                                   

        ThreadCategory.getInstance(PerformanceModelFactory.class).debug("init: config file path: "
                                                                        + cfgFile.getPath());

        setInstance(new PerformanceModelFactory(cfgFile.getPath()));
    }

    public static synchronized void reload() throws IOException {
        s_singleton = null;
        s_loaded = false;

        init();
    }

}
