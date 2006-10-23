package org.opennms.web.graph;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.PatternSyntaxException;

import org.opennms.core.utils.BundleLists;

public class PropertiesGraphDao implements GraphDao {
    public static final String DEFAULT_GRAPH_LIST_KEY = "reports";
    
    private Map<String, PrefabGraphType> m_types =
        new HashMap<String, PrefabGraphType>();

    private HashMap<String, AdhocGraphType> m_adhocTypes =
        new HashMap<String, AdhocGraphType>();
    
    public PropertiesGraphDao(String prefabConfigs, String adhocConfigs)
            throws IOException {
        initPrefab(makeInitMap(prefabConfigs));
        initAdhoc(makeInitMap(adhocConfigs));
    }
    
    private void initPrefab(Map<String, File> configMap) throws IOException {
        for (Map.Entry<String, File> configEntry : configMap.entrySet()) {
            loadProperties(configEntry.getKey(), configEntry.getValue());
        }
    }
    
    private void initAdhoc(Map<String, File> configMap) throws IOException {
        for (Map.Entry<String, File> configEntry : configMap.entrySet()) {
            loadAdhocProperties(configEntry.getKey(), configEntry.getValue());
        }
    }

    private Map<String, File> makeInitMap(String configs) {
        Map<String, File> initMap = new HashMap<String, File>();
        try {
            String[] configEntries = configs.split(";");
            for (String configEntry: configEntries) {
                if ("".equals(configEntry)) {
                    continue;
                }
                String[] entry = configEntry.split("=");
                if (entry.length != 2) {
                    throw new IllegalArgumentException("Incorrect number of "
                                                       + "equals signs in "
                                                       + "config list: \""
                                                       + configs + "\"");
                }
                String type = entry[0];
                String configFile = entry[1];
                File file = new File(configFile);
                initMap.put(type, file);
            }
        } catch (PatternSyntaxException e) {
            throw new IllegalArgumentException("Could not parse config list: "
                                               + "\"" + configs + "\"");
        }
        
        return initMap;
    }


    public PrefabGraphType findByName(String name) {
        return m_types.get(name);
    }

    /*
    protected void loadProperties(String type, String homeDir, String fileName)
                throws IOException {
        if (homeDir == null) {
            throw new IllegalArgumentException("Cannot take null "
                                               + "parameters.");
        }
        loadProperties(type, homeDir + fileName);
    }
    */

    public void loadProperties(String type, File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        loadProperties(type, in);
        in.close();
    }
    
    public void loadProperties(String type, InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);

        PrefabGraphType t = new PrefabGraphType();
        t.setName(type);
        
        t.setRrdDirectory(new File(properties.getProperty("command.input.dir")));
        t.setCommandPrefix(properties.getProperty("command.prefix"));
        t.setOutputMimeType(properties.getProperty("output.mime"));

        if (properties.getProperty("default.report") != null) { 
            t.setDefaultReport(new String(properties.getProperty("default.report")));
        } else {
            t.setDefaultReport("none");
        }           

        t.setReportMap(getPrefabGraphDefinitions(properties));
        
        m_types.put(t.getName(), t);
    }

    
    private static String getReportProperty(Properties props, String key,
                String suffix, boolean required) {
        String propertyName = "report." + key + "." + suffix;
        
        String property = props.getProperty(propertyName);
        if (property == null && required == true) {
            throw new IllegalArgumentException("Properties parameter must "
                                               + "contain \"" + propertyName
                                               + "\" property");
        }
        
        return property;
    }
    
    public void loadAdhocProperties(String type, File file) throws IOException {
        FileInputStream in = new FileInputStream(file);
        loadAdhocProperties(type, in);
        in.close();
    }
    
    public void loadAdhocProperties(String type, InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);
        
        AdhocGraphType t = new AdhocGraphType();
        t.setName(type);
        
        t.setRrdDirectory(new File(properties.getProperty("command.input.dir")));
        t.setCommandPrefix(properties.getProperty("command.prefix"));
        t.setOutputMimeType(properties.getProperty("output.mime"));
        
        t.setTitleTemplate(properties.getProperty("adhoc.command.title"));
        t.setDataSourceTemplate(properties.getProperty("adhoc.command.ds"));
        t.setGraphLineTemplate(properties.getProperty("adhoc.command.graphline"));
        
        m_adhocTypes.put(t.getName(), t);
    }

    
    private static PrefabGraph makePrefabGraph(String key, Properties props, int order) {
        if (key == null) {
            throw new IllegalArgumentException("key cannot be null");
        }
        if (props == null) {
            throw new IllegalArgumentException("props cannot be null");
        }

        String title = getReportProperty(props, key, "name", true);

        String command = getReportProperty(props, key, "command", true);

        String columnString = getReportProperty(props, key, "columns", true);
        String[] columns = BundleLists.parseBundleList(columnString);

        String externalValuesString = getReportProperty(props, key,
                                                        "externalValues",
                                                        false);
        String[] externalValues;
        if (externalValuesString == null) {
            externalValues = new String[0];
        } else {
            externalValues = BundleLists.parseBundleList(externalValuesString);
        }

        String[] propertiesValues;
        String propertiesValuesString = getReportProperty(props, key,
                                                          "propertiesValues",
                
                                                          false);
        if( propertiesValuesString == null ) {
                propertiesValues = new String[0];
        } else {
                propertiesValues = BundleLists.parseBundleList(propertiesValuesString);
        }

        // can be null
        String type = getReportProperty(props, key, "type", false);

        // can be null
        String description = getReportProperty(props, key, "description", false);
        
        return new PrefabGraph(key, title, columns,
                command, externalValues,
                propertiesValues, order, type,
                description);

    }
    

    public static Map<String, PrefabGraph> getPrefabGraphDefinitions(Properties props) {
        return getPrefabGraphDefinitions(props, DEFAULT_GRAPH_LIST_KEY);
    }

    private static Map<String, PrefabGraph> getPrefabGraphDefinitions(Properties props, String listKey) {
        if (props == null) {
            throw new IllegalArgumentException("props cannot be null");
        }
        if (listKey == null) {
            throw new IllegalArgumentException("listKey cannot be null");
        }

        String listString = props.getProperty(listKey);
        if (listString == null) {
            throw new IllegalArgumentException("Properties parameter must contain \"" + listKey + "\" property");
        }

        String[] list = BundleLists.parseBundleList(listString);
        
        return getPrefabGraphDefinitions(props, list);
    }
    
    private static Map<String, PrefabGraph> getPrefabGraphDefinitions(Properties props, String[] list) {
        Map<String, PrefabGraph> map = new LinkedHashMap<String, PrefabGraph>();
        
        for (int i = 0; i < list.length; i++) {
            String key = list[i];
            PrefabGraph graph = makePrefabGraph(key, props, i);

            map.put(key, graph);
        }

        return map;
    }

    public AdhocGraphType findAdhocByName(String name) {
        return m_adhocTypes.get(name);
    }
}
