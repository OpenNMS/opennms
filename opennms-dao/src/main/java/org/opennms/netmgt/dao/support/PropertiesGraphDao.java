/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.dao.support;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.io.IOUtils;
import org.opennms.core.spring.FileReloadCallback;
import org.opennms.core.spring.FileReloadContainer;
import org.opennms.core.utils.BundleLists;
import org.opennms.netmgt.dao.api.GraphDao;
import org.opennms.netmgt.model.AdhocGraphType;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.OnmsResource;
import org.opennms.netmgt.model.PrefabGraph;
import org.opennms.netmgt.model.PrefabGraphType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.orm.ObjectRetrievalFailureException;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class PropertiesGraphDao implements GraphDao, InitializingBean {
    
    private static final Logger LOG = LoggerFactory.getLogger(PropertiesGraphDao.class);
    
    /** Constant <code>DEFAULT_GRAPH_LIST_KEY="reports"</code> */
    public static final String DEFAULT_GRAPH_LIST_KEY = "reports";

    private ConcurrentMap<String, Resource> m_prefabConfigs = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Resource> m_adhocConfigs  = new ConcurrentHashMap<>();

    private final ConcurrentMap<String, FileReloadContainer<PrefabGraphTypeDao>> m_types = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, FileReloadContainer<AdhocGraphType>> m_adhocTypes = new ConcurrentHashMap<>();

    private final PrefabGraphTypeCallback m_prefabCallback = new PrefabGraphTypeCallback();
    private final AdhocGraphTypeCallback m_adhocCallback = new AdhocGraphTypeCallback();

    /**
     * <p>
     * Constructor for PropertiesGraphDao.
     * </p>
     */
    public PropertiesGraphDao() {
    }

    private void initPrefab() throws IOException {
        for (Map.Entry<String, Resource> configEntry : getPrefabConfigs().entrySet()) {
            loadProperties(configEntry.getKey(), configEntry.getValue());
        }
    }

    private void initAdhoc() throws IOException {
        for (Map.Entry<String, Resource> configEntry : getAdhocConfigs().entrySet()) {
            loadAdhocProperties(configEntry.getKey(), configEntry.getValue());
        }
    }

    /** {@inheritDoc} */
    @Override
    public PrefabGraphType findPrefabGraphTypeByName(String name) {
        return findPrefabGraphTypeDaoByName(name);
    }

    /**
     * Same as findPrefabGraphTypeByName, but has the sub-class return type.
     * For use within the package, where access to the subclass methods is
     * required and we don't want to cast (ugly)
     * 
     * @param name
     * @return
     */
    PrefabGraphTypeDao findPrefabGraphTypeDaoByName(String name) {
        PrefabGraphTypeDao result = m_types.get(name).getObject();
        this.rescanIncludeDirectory(result);
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public AdhocGraphType findAdhocGraphTypeByName(String name) {
        return m_adhocTypes.get(name).getObject();
    }

    /**
     * <p>
     * loadProperties
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param resource
     *            a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException
     *             if any.
     */
    public void loadProperties(String typeName, Resource resource)
            throws IOException {
        PrefabGraphTypeDao type;
        type = createPrefabGraphType(typeName, resource);

        m_types.put(type.getName(),
                    new FileReloadContainer<PrefabGraphTypeDao>(type,
                                                                resource,
                                                                m_prefabCallback));
    }

    /**
     * <p>
     * loadProperties
     * </p>
     * Used exclusively by test code. Will ignore an "include.directory"
     * because we don't have a resource/path to do any useful "relative"
     * pathing to. Also anything loaded in this fashion will *not* have auto
     * reloading on changes, because there's no underlying Resource/File to
     * check against. Like, duh!
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param in
     *            a {@link java.io.InputStream} object.
     * @throws java.io.IOException
     *             if any.
     */
    public void loadProperties(String type, InputStream in)
            throws IOException {
        Resource resource = new InputStreamResource(in);
        // Not reloadable; we don't have a file to check for modifications
        PrefabGraphTypeDao t = createPrefabGraphType(type, resource, false);
        if (t != null) {
            m_types.put(t.getName(),
                        new FileReloadContainer<PrefabGraphTypeDao>(t));
        }
    }

    /**
     * Encapsulates the check of the rescan interval on a given 'type',
     * and if that has passed, initiates a rescan.
     *
     * Should be called on any accessor method which returns a graph or
     * a graph type object to external callers, to ensure they're
     * seeing the latest configuration
     * @param type
     */
    private void rescanIncludeDirectory(PrefabGraphTypeDao type) {
        try {
            //Always do this check; it'll only rescan modified previously munted files anyway
            this.recheckMalformedIncludedFiles(type);
            if (System.currentTimeMillis() > (type.getLastIncludeScan() + type.getIncludeDirectoryRescanTimeout())) {
                this.scanIncludeDirectory(type);
            }
        } catch (IOException e) {
            LOG.error("Unable to rescan the include directory '{}' of type {}", type.getIncludeDirectory(), type.getName(), e);
        }

    }
    
    private void loadIncludedFile(PrefabGraphTypeDao type, File file)
            throws FileNotFoundException, IOException {
        Properties props = new Properties();
        InputStream fileIn = new FileInputStream(file);
        try {
            props.load(fileIn);
        } finally {
            IOUtils.closeQuietly(fileIn);
        }
        //Clear any malformed setting; if everything goes ok, it'll remain cleared
        // If there's problems, it'll be re-added.
        type.removeMalformedFile(file);
        try {
            List<PrefabGraph> subGraphs = loadPrefabGraphDefinitions(type,
                                                                     props);
            for (PrefabGraph graph : subGraphs) {
                if(graph == null) {
                    //Indicates a multi-graph file that had a munted graph definition
                    type.addMalformedFile(file); //Record that the file was partly broken
                } else {
                    type.addPrefabGraph(new FileReloadContainer<PrefabGraph>(
                                                                         graph,
                                                                         new FileSystemResource(
                                                                                                file),
                                                                         type.getCallback()));
                }
            }

        } catch (DataAccessResourceFailureException e) {
            LOG.error("Problem while attempting to load {}", file, e);
            type.addMalformedFile(file); //Record that the file was completely broken
        }
    }
    
    /**
     * Checks for any files which were malformed last time the include directory for this type
     * was scanned.  If any of them have changed since then, attempt to read them again.
     * 
     * This is meant to ensure that if the administrator made a mistake when adding a new file
     * they don't need to wait for include.directory.rescan before the fix will be noticed 
     * 
     * It's necessary because if the file was broken, the broken graphs won't be stored in a
     * FileReloadContainer to be noticed.
     * @param type
     */
    private void recheckMalformedIncludedFiles(PrefabGraphTypeDao type) throws IOException {
        Map<File, Long> filesMap = type.getMalformedFiles();
        //Get an immutable set of files, for iterating over safely while potentially modifying
        // the set in loadIncludedFile
        File[] files = filesMap.keySet().toArray(new File[0]);
        for (File file : files) {
            Long lastKnownTimestamp = filesMap.get(file);
            if(file.lastModified() > lastKnownTimestamp) {
                //Try loading it again; it's been modified since we noted it was borked
                this.loadIncludedFile(type, file);
            }
        }
    }
    
    private void scanIncludeDirectory(PrefabGraphTypeDao type) throws IOException {
        Resource includeDirectoryResource = type.getIncludeDirectoryResource();

        if (includeDirectoryResource != null) {
            File includeDirectory = includeDirectoryResource.getFile();
            // Include all the files in the directory, knowing that the
            // format is slightly different (no report name required in
            // each property name, and report.id is expected)
            FilenameFilter propertyFilesFilter = new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".properties"));
                }
            };
            final File[] propertyFiles = includeDirectory.listFiles(propertyFilesFilter);
            Arrays.sort(propertyFiles);

            for (final File file : propertyFiles) {
                loadIncludedFile(type, file);
            }
        }
        type.setLastIncludeScan(System.currentTimeMillis());
    }

    /**
     * Wrapper around the createPrefabGraphType which takes a reloadable
     * argument, defaulting to reloadable = true For simplicity in the
     * default/expected case.
     * 
     * @param type
     * @param sourceResource
     * @return
     */
    private PrefabGraphTypeDao createPrefabGraphType(String type,
            Resource sourceResource) {
        // Default to adding a callback
        return this.createPrefabGraphType(type, sourceResource, true);
    }

    /**
     * Create a PrefabGraphTypeDao from the properties file in sourceResource
     * If reloadable is true, add the type's reload call back to each graph,
     * otherwise don't If sourceResource is not a file-based resource, then
     * reloadable should be false NB: I didn't want to get into checking for
     * what the implementation class of Resource is, because that could break
     * in future with new classes and types that do have a File underneath
     * them. This way, it's up to the caller, who *should* be able to make a
     * sensible choice as to whether the resource is reloadable or not.
     * 
     * @param type
     * @param sourceResource
     * @param reloadable
     * @return
     */
    private PrefabGraphTypeDao createPrefabGraphType(String type,
            Resource sourceResource, boolean reloadable) {
        InputStream in = null;
        try {
            in = sourceResource.getInputStream();
            Properties properties = new Properties();
            properties.load(in);
            PrefabGraphTypeDao t = new PrefabGraphTypeDao();
            t.setName(type);

            t.setCommandPrefix(getProperty(properties, "command.prefix"));
            t.setOutputMimeType(getProperty(properties, "output.mime"));

            t.setDefaultReport(properties.getProperty("default.report",
                                                      "none"));

            String includeDirectoryString = properties.getProperty("include.directory");
            t.setIncludeDirectory(includeDirectoryString);
            
            if (includeDirectoryString != null) {
                Resource includeDirectoryResource;

                File includeDirectoryFile = new File(includeDirectoryString);
                if (includeDirectoryFile.isAbsolute()) {
                    includeDirectoryResource = new FileSystemResource(
                                                                         includeDirectoryString);
                } else {
                    includeDirectoryResource = sourceResource.createRelative(includeDirectoryString);
                }
                
                File includeDirectory = includeDirectoryResource.getFile();
                
                if (includeDirectory.isDirectory()) {
                    t.setIncludeDirectoryResource(includeDirectoryResource);
                } else {
                    // Just warn; no need to throw a hissy fit or otherwise fail to load
                    LOG.warn("includeDirectory '{}' specified in '{}' is not a directory", includeDirectoryFile.getAbsolutePath(), sourceResource.getFilename());
                }
            }

            // Default to 5 minutes; it's up to users to specify a shorter
            // time if they don't mind OpenNMS spamming on that directory
            int interval;
            try {
                interval = Integer.parseInt(properties.getProperty("include.directory.rescan",
                                                                   "300000"));
            } catch (NumberFormatException e) {
                // Default value if one was specified but it wasn't an integer
                interval = 300000;
                LOG.warn("The property 'include.directory.rescan' in {} was not able to be parsed as an integer.  Defaulting to {}ms", sourceResource, interval, e);
            }

            t.setIncludeDirectoryRescanInterval(interval);

            List<PrefabGraph> graphs = loadPrefabGraphDefinitions(t,
                                                                  properties);

            for (PrefabGraph graph : graphs) {
                //The graphs list may contain nulls; see loadPrefabGraphDefinitions for reasons
                if(graph != null) {
                    FileReloadContainer<PrefabGraph> container;
                    if (reloadable) {
                        container = new FileReloadContainer<PrefabGraph>(
                                                                         graph,
                                                                         sourceResource,
                                                                         t.getCallback());
                    } else {
                        container = new FileReloadContainer<PrefabGraph>(graph);
                    }
    
                    t.addPrefabGraph(container);
                }
            }
            
            //This *must* come after loading the main graph file, to ensure overrides are correct
            this.scanIncludeDirectory(t);
            return t;

        } catch (IOException e) {
            LOG.error("Failed to load prefab graph configuration of type {} from {}", type, sourceResource, e);
            return null;
        } finally {
            IOUtils.closeQuietly(in);
        }

    }

    /**
     * <p>createPrefabGraphType
     * loadAdhocProperties
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param resource
     *            a {@link org.springframework.core.io.Resource} object.
     * @throws java.io.IOException
     *             if any.
     */
    public void loadAdhocProperties(String type, Resource resource)
            throws IOException {
        InputStream in = resource.getInputStream();
        AdhocGraphType t;
        try {
            t = createAdhocGraphType(type, in);
        } finally {
            IOUtils.closeQuietly(in);
        }

        m_adhocTypes.put(t.getName(),
                         new FileReloadContainer<AdhocGraphType>(t, resource,
                                                                 m_adhocCallback));
    }

    /**
     * <p>
     * loadAdhocProperties
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param in
     *            a {@link java.io.InputStream} object.
     * @throws java.io.IOException
     *             if any.
     */
    public void loadAdhocProperties(String type, InputStream in)
            throws IOException {
        AdhocGraphType t = createAdhocGraphType(type, in);
        m_adhocTypes.put(t.getName(),
                         new FileReloadContainer<AdhocGraphType>(t));
    }

    /**
     * <p>
     * createAdhocGraphType
     * </p>
     * 
     * @param type
     *            a {@link java.lang.String} object.
     * @param in
     *            a {@link java.io.InputStream} object.
     * @return a {@link org.opennms.netmgt.model.AdhocGraphType} object.
     * @throws java.io.IOException
     *             if any.
     */
    private AdhocGraphType createAdhocGraphType(String type, InputStream in)
            throws IOException {
        Properties properties = new Properties();
        properties.load(in);

        AdhocGraphType t = new AdhocGraphType();
        t.setName(type);

        t.setCommandPrefix(getProperty(properties, "command.prefix"));
        t.setOutputMimeType(getProperty(properties, "output.mime"));

        t.setTitleTemplate(getProperty(properties, "adhoc.command.title"));
        t.setDataSourceTemplate(getProperty(properties, "adhoc.command.ds"));
        t.setGraphLineTemplate(getProperty(properties,
                                           "adhoc.command.graphline"));

        return t;
    }

    /**
     * @param type
     *            - a PrefabGraphType in which graphs
     *            found in 'properties' will be stored
     * @param properties
     *            - a properties object, usually loaded from a File or
     *            InputStream, with graph definitions in it
     * @return A list of the graphs found.  THIS LIST MAY CONTAIN NULL ENTRIES, one for each
     * graph in a multi-graph file that failed to load (e.g. missing properties).  Other than
     * logging an error, this is the only way this method indicates a problem with just one graph
     * The only cause for a real exception is if there's neither a "reports" nor a "report.id" 
     * property, which cannot be recovered from   
     */
    private List<PrefabGraph> loadPrefabGraphDefinitions(
            PrefabGraphTypeDao type, Properties properties) {
        Assert.notNull(properties, "properties argument cannot be null");

        List<PrefabGraph> result = new ArrayList<>();

        String listString = properties.getProperty(DEFAULT_GRAPH_LIST_KEY); // Optional

        String[] list;

        if (listString != null) {
            list = BundleLists.parseBundleList(listString);
        } else {
            // A report-per-file properties file; just use the report.id
            // At this stage, if there was no "reports", then there *must* be
            // a report.id, otherwise we're pooched
            list = new String[1];
            try {
                list[0] = getProperty(properties, "report.id");
            } catch (DataAccessResourceFailureException e) {
                // Special case; if this exception is thrown, then report.id
                // was missing
                // But, we need to be more clear in the report (no report.id
                // *or* "reports" property).  However, we shouldn't throw
                // an exception, because that would break loading of all 
                // graphs if just one file was broken
                throw new DataAccessResourceFailureException("Properties must "
                                    + "contain a 'report.id' property "
                                    + "or a 'reports' property");
            }
        }

        for (String name : list) {
            try {
                PrefabGraph graph = makePrefabGraph(name, properties,
                                                    type.getNextOrdering());
                result.add(graph);
            } catch (DataAccessResourceFailureException e) {
                LOG.error("Failed to load report '{}'", name, e);
                result.add(null); //Add a null, indicating a broken graph
            }
        }
        return result;
    }

    private PrefabGraph makePrefabGraph(String name, Properties props,
            int order) {
        Assert.notNull(name, "name argument cannot be null");
        Assert.notNull(props, "props argument cannot be null");

        String key = name; // Default to the name, for

        if (props.getProperty("report.id") != null) {
            key = null; // A report-per-file properties file
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
        if (propertiesValuesString == null) {
            propertiesValues = new String[0];
        } else {
            propertiesValues = BundleLists.parseBundleList(propertiesValuesString);
        }

        // can be null
        String[] types;
        String typesString = getReportProperty(props, key, "type", false);
        if (typesString == null) {
            types = new String[0];
        } else {
            types = BundleLists.parseBundleList(typesString);
        }

        // can be null
        String description = getReportProperty(props, key, "description",
                                               false);

        /*
         * TODO: Right now a "width" and "height" property is required in
         * order to get zoom to work properly on non-standard sized graphs. A
         * more elegant solution would be to parse the command string and look
         * for --width and --height and set the following two variables
         * automagically, without having to rely on a configuration file.
         */
        Integer graphWidth = getIntegerReportProperty(props, key, "width",
                                                      false);
        Integer graphHeight = getIntegerReportProperty(props, key, "height",
                                                       false);

        String suppressString = getReportProperty(props, key, "suppress",
                                                  false);
        String[] suppress = (suppressString == null) ? new String[0]
                                                    : BundleLists.parseBundleList(suppressString);

        return new PrefabGraph(name, title, columns, command, externalValues,
                               propertiesValues, order, types, description,
                               graphWidth, graphHeight, suppress);

    }

    private String getProperty(Properties props, String name) {
        String property = props.getProperty(name);
        if (property == null) {
            throw new DataAccessResourceFailureException("Properties must "
                    + "contain \'" + name + "\' property");
        }

        return property;
    }

    // A null key means we are loading from a report-per-file properties file
    private String getReportProperty(Properties props, String key,
            String suffix, boolean required) {

        String propertyName;
        String graphName;
        if (key != null) {
            propertyName = "report." + key + "." + suffix;
            graphName = key;
        } else {
            propertyName = "report." + suffix;
            // It's lightly evil to know this from this method, but we can be
            // confident that report.id will exist
            graphName = props.getProperty("report.id");
        }

        String property = props.getProperty(propertyName);
        if (property == null && required == true) {
            throw new DataAccessResourceFailureException("Properties for "
                    + "report '" + graphName + "' must contain \'"
                    + propertyName + "\' property");
        }

        return property;
    }

    private Integer getIntegerReportProperty(Properties props, String key,
            String suffix, boolean required) {
        String value = getReportProperty(props, key, suffix, required);
        if (value == null) {
            return null;
        }

        try {
            return Integer.valueOf(value);
        } catch (NumberFormatException e) {
            throw new DataAccessResourceFailureException(
                                                         "Property value for '"
                                                                 + suffix
                                                                 + "' on report '"
                                                                 + key
                                                                 + "' must be an integer.  '"
                                                                 + value
                                                                 + "' is not a valid value");
        }
    }

    private class PrefabGraphTypeCallback implements
            FileReloadCallback<PrefabGraphTypeDao> {
        @Override
        public PrefabGraphTypeDao reload(PrefabGraphTypeDao object,
                Resource resource) {
            try {
                return createPrefabGraphType(object.getName(), resource);
            } catch (Throwable e) {
                LOG.error("Could not reload configuration '{}'", resource, e);
                return null;
            }
        }
    }

    private class PrefabGraphCallback implements
            FileReloadCallback<PrefabGraph> {
        private PrefabGraphTypeDao m_type;

        public PrefabGraphCallback(PrefabGraphTypeDao type) {
            m_type = type;
        }

        @Override
        public PrefabGraph reload(PrefabGraph graph, Resource resource) {
            try {
                String graphName = graph.getName();
                Properties props = new Properties();
                props.load(resource.getInputStream());
                List<PrefabGraph> reloadedGraphs = loadPrefabGraphDefinitions(m_type,
                                                                              props);
                PrefabGraph result = null;
                for (PrefabGraph reloadedGraph : reloadedGraphs) {
                    //The reloadedGraphs may contain nulls; see loadPrefabGraphDefinitions for reasons
                    if(reloadedGraph != null) {
                        if (reloadedGraph.getName().equals(graphName)) {
                            result = reloadedGraph;
                        }
                        m_type.addPrefabGraph(new FileReloadContainer<PrefabGraph>(
                                                                                   reloadedGraph,
                                                                                   resource,
                                                                                   this));
                    }
                }
                return result;
            } catch (Throwable e) {
                LOG.error("Could not reload configuration '{}'", resource, e);
                return null;
            }
        }
    }

    /**
     * <p>
     * getAllPrefabGraphs
     * </p>
     * 
     * @return a {@link java.util.List} object.
     */
    @Override
    public List<PrefabGraph> getAllPrefabGraphs() {
        final List<PrefabGraph> graphs = new ArrayList<>();
        for (final FileReloadContainer<PrefabGraphTypeDao> container : new ArrayList<>(m_types.values())) {
            final PrefabGraphTypeDao type = container.getObject();
            this.rescanIncludeDirectory(type);
            for (final FileReloadContainer<PrefabGraph> graphContainer : type.getReportMap().values()) {
                graphs.add(graphContainer.getObject());
            }
        }
        return graphs;
    }

    /** {@inheritDoc} */
    @Override
    public PrefabGraph getPrefabGraph(final String name) {
        for (final FileReloadContainer<PrefabGraphTypeDao> container : m_types.values()) {
            final PrefabGraphTypeDao type = container.getObject();
            this.rescanIncludeDirectory(type);
            final PrefabGraph graph = type.getQuery(name);
            if (graph != null) {
                return graph;
            }
        }
        throw new ObjectRetrievalFailureException(PrefabGraph.class, name,
                                                  "Could not find prefabricated graph report with name '"
                                                          + name + "'", null);
    }

    /** {@inheritDoc} */
    @Override
    public PrefabGraph[] getPrefabGraphsForResource(final OnmsResource resource) {
        if (resource == null) {
            LOG.warn("returning empty graph list for resource because it is null");
            return new PrefabGraph[0];
        }
        Set<OnmsAttribute> attributes = new LinkedHashSet<>(resource.getAttributes());
        // Check if there are no attributes
        if (attributes.size() == 0) {
            LOG.debug("returning empty graph list for resource {} because its attribute list is empty", resource);
            return new PrefabGraph[0];
        }

        Set<String> availableRrdAttributes = new LinkedHashSet<>(resource.getRrdGraphAttributes().keySet());
        Set<String> availableStringAttributes = new LinkedHashSet<>(resource.getStringPropertyAttributes().keySet());
        Set<String> availableExternalAttributes = new LinkedHashSet<>(resource.getExternalValueAttributes().keySet());

        // Check if there are no RRD attributes
        if (availableRrdAttributes.size() == 0) {
            LOG.debug("returning empty graph list for resource {} because it has no RRD attributes", resource);
            return new PrefabGraph[0];
        }

        String resourceType = resource.getResourceType().getName();

        Map<String, PrefabGraph> returnList = new LinkedHashMap<String, PrefabGraph>();
        for (PrefabGraph query : getAllPrefabGraphs()) {
            if (resourceType != null && !query.hasMatchingType(resourceType)) {
                LOG.debug("skipping {} because its types \"{}\" does not match resourceType \"{}\"", query.getName(), StringUtils.arrayToDelimitedString(query.getTypes(), ", "), resourceType);
                continue;
            }

            if (!verifyAttributesExist(query, "RRD",
                                       Arrays.asList(query.getColumns()),
                                       availableRrdAttributes)) {
                continue;
            }
            if (!verifyAttributesExist(query,
                                       "string property",
                                       Arrays.asList(query.getPropertiesValues()),
                                       availableStringAttributes)) {
                continue;
            }
            if (!verifyAttributesExist(query,
                                       "external value",
                                       Arrays.asList(query.getExternalValues()),
                                       availableExternalAttributes)) {
                continue;
            }

            LOG.debug("adding {} to query list", query.getName());

            returnList.put(query.getName(), query);
        }

        if (LOG.isDebugEnabled()) {
            ArrayList<String> nameList = new ArrayList<String>(
                                                               returnList.size());
            for (PrefabGraph graph : returnList.values()) {
                nameList.add(graph.getName());
            }
            LOG.debug("found {} prefabricated graphs for resource {}: {}", nameList.size(), resource, StringUtils.collectionToDelimitedString(nameList, ", "));
        }

        final Set<String> suppressReports = new HashSet<>();
        for (final Entry<String, PrefabGraph> entry : returnList.entrySet()) {
            suppressReports.addAll(Arrays.asList(entry.getValue().getSuppress()));
        }

        suppressReports.retainAll(returnList.keySet());
        if (suppressReports.size() > 0) {
            LOG.debug("suppressing {} prefabricated graphs for resource {}: {}", suppressReports.size(), resource, StringUtils.collectionToDelimitedString(suppressReports, ", "));
        }

        for (final String suppressReport : suppressReports) {
            returnList.remove(suppressReport);
        }

        return returnList.values().toArray(new PrefabGraph[returnList.size()]);
    }

    private boolean verifyAttributesExist(PrefabGraph query, String type, List<String> requiredList, Set<String> availableRrdAttributes) {
        if (availableRrdAttributes.containsAll(requiredList)) {
            return true;
        } else {
            LOG.debug("not adding {} to prefab graph list because the required list of {} attributes ({}) is not in the list of {} attributes on the resource ({})", query.getName(), type, StringUtils.collectionToDelimitedString(requiredList, ", "), type, StringUtils.collectionToDelimitedString(availableRrdAttributes, ", "));
            return false;
        }
    }

    private class AdhocGraphTypeCallback implements
            FileReloadCallback<AdhocGraphType> {
        @Override
        public AdhocGraphType reload(AdhocGraphType object, Resource resource) {
            InputStream in = null;
            try {
                in = resource.getInputStream();
                return createAdhocGraphType(object.getName(), in);
            } catch (Throwable e) {
                LOG.error("Could not reload configuration from '{}'", resource, e);
                return null;
            } finally {
                IOUtils.closeQuietly(in);
            }
        }

    }

    /**
     * <p>
     * afterPropertiesSet
     * </p>
     * 
     * @throws java.io.IOException
     *             if any.
     */
    @Override
    public void afterPropertiesSet() throws IOException {
        Assert.notNull(getPrefabConfigs(), "property prefabConfigs must be set to a non-null value");
        Assert.notNull(getAdhocConfigs(), "property adhocConfigs must be set to a non-null value");

        initPrefab();
        initAdhoc();
    }

    /**
     * <p>
     * getAdhocConfigs
     * </p>
     * 
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, Resource> getAdhocConfigs() {
        return m_adhocConfigs;
    }

    /**
     * <p>
     * setAdhocConfigs
     * </p>
     * 
     * @param adhocConfigs
     *            a {@link java.util.Map} object.
     */
    public void setAdhocConfigs(final Map<String, Resource> adhocConfigs) {
        m_adhocConfigs = new ConcurrentHashMap<>(adhocConfigs);
    }

    /**
     * <p>
     * getPrefabConfigs
     * </p>
     * 
     * @return a {@link java.util.Map} object.
     */
    protected Map<String, Resource> getPrefabConfigs() {
        return m_prefabConfigs;
    }

    /**
     * <p>
     * setPrefabConfigs
     * </p>
     * 
     * @param prefabConfigs
     *            a {@link java.util.Map} object.
     */
    public void setPrefabConfigs(final Map<String, Resource> prefabConfigs) {
        m_prefabConfigs = new ConcurrentHashMap<>(prefabConfigs);
    }

    /**
     * An package internal subclass of PrefabGraphType, intended for use only
     * within this Dao.  This object has knowledge of FileReloadContainers,
     * rescan intervals, and the original resources used to load the types.
     * (i.e. info that shouldn't be in the PrefabGraphType itself, which is 
     * a model object, in opennms-model)
     * It stores the actual graphs that belong to the PrefabGraphType, in said
     * FileReloadContainers; all access to graph objects is via this Dao anyway
     */
    class PrefabGraphTypeDao extends PrefabGraphType {
        private final ConcurrentMap<String, FileReloadContainer<PrefabGraph>> m_reportMap = new ConcurrentHashMap<>();
        
        //The ordering to use for the next graph added to this type, to ensure sortability
        private int m_ordering;
        
        //The time-stamp when the include directory was last scanned.  
        //Like FileReloadContainers. this doesn't belong in the model
        //The value is maintained by the PropertiesGraphDao itself, not this object
        private long m_lastIncludeScan;

         // A call-back object for this type, which will reload a file, and
        // re-add the
        // new graph to this PrefabGraphTypeDao instance
        private final PrefabGraphCallback m_callback;
        
        // A set of files that were malformed last time they were read by scanIncludeDirectory
        // and their previous time-stamps
        private final ConcurrentMap<File, Long> m_malformedFiles = new ConcurrentHashMap<>();
        
        /**
         * The resource that is the include directory
         * Stored because otherwise we'll have to recalculate it from the root resource
         * repeatedly (when rescanning).
         */
        private Resource m_includeDirectoryResource;

        public PrefabGraphTypeDao() {
            m_ordering = 0;
            m_callback = new PrefabGraphCallback(this);
        }

        public void setIncludeDirectoryResource(Resource includeDirectoryResource) {
            m_includeDirectoryResource = includeDirectoryResource;
        }

        public Resource getIncludeDirectoryResource() {
            return m_includeDirectoryResource;
        }

        public FileReloadCallback<PrefabGraph> getCallback() {
            return m_callback;
        }

        /**
         * Adds the specified graph to the internal map, replacing any
         * previous ones with the same name (name being the name from the
         * graph object itself)
         * 
         * @param graph
         */
        public void addPrefabGraph(FileReloadContainer<PrefabGraph> graph) {
            m_reportMap.put(graph.getObject().getName(), graph);
        }

        /**
         * <p>
         * getReportMap
         * </p>
         * A map of graphs, keyed by their name. The returned value is
         * readonly (unmodifiable)
         * 
         * @return a {@link java.util.Map} object.
         */
        public Map<String, FileReloadContainer<PrefabGraph>> getReportMap() {
            return Collections.unmodifiableMap(m_reportMap);
        }

        /**
         * <p>
         * getQuery
         * </p>
         * 
         * @param queryName
         *            a {@link java.lang.String} object.
         * @return a {@link org.opennms.netmgt.model.PrefabGraph} object.
         */
        public PrefabGraph getQuery(String queryName) {
            FileReloadContainer<PrefabGraph> container = m_reportMap.get(queryName);
            if (container == null) {
                return null;
            }
            //TODO check all columns against meta files and add metricIds to prefabgraph
            /*TODO maybe a later point in the process could be an option for the metricId matching, 
            /*if we have more infromation about the folder to look into.
             */
            return container.getObject();
        }

        public int getNextOrdering() {
            return m_ordering++;
        }

        public long getLastIncludeScan() {
            return m_lastIncludeScan;
        }

        public void setLastIncludeScan(long lastIncludeScan) {
            m_lastIncludeScan = lastIncludeScan;
        }

        /**
         * Returns the malformed files map.  
         * DO NOT EDIT THIS MAP DIRECTLY (well, it probably doesn't matter, but it's bad form old chap)
         * @return
         */
        public Map<File, Long> getMalformedFiles() {
            return m_malformedFiles;
        }

        /**
         * Add a malformed file to the list for this type; it's last modified time will be 
         * stored as the Long value in the map, allowing callers to later check if it's been modified
         * since being noted as malformed
         * @param malformedFile
         */
        public void addMalformedFile(File malformedFile) {
            m_malformedFiles.put(malformedFile, malformedFile.lastModified());
        }
        
        /**
         * Remove a malformed file from the malformed files list, presumably because it loaded correctly
         * and is no longer malformed
         * 
         * @param malformedFile
         */
        public void removeMalformedFile(File malformedFile) {
            m_malformedFiles.remove(malformedFile);
        }

    }
}
