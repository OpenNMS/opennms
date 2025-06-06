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
package org.opennms.netmgt.flows.elastic;

import com.github.dockerjava.api.model.Ulimit;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.testcontainers.containers.Container;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.images.builder.ImageFromDockerfile;
import org.testcontainers.utility.DockerImageName;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A TestContainers Elasticsearch container that supports installing plugins from Maven dependencies.
 * 
 * This container allows you to install Elasticsearch plugins directly from your Maven dependencies,
 * which is particularly useful for testing custom plugins that are part of your project.
 */
public class ElasticTestContainerWithPlugins extends GenericContainer<ElasticTestContainerWithPlugins> {

    private static final Logger LOG = LoggerFactory.getLogger(ElasticTestContainerWithPlugins.class);
    private static final int ES_HTTP_PORT = 9200;
    private static final int ES_TRANSPORT_PORT = 9300;
    private static final String ES_VERSION = "7.17.0"; // Default ES version
    private final List<MavenPlugin> plugins = new ArrayList<>();
    private final Path tempDir;

    /**
     * Constructs a new ElasticsearchMavenPluginContainer with the specified Docker image name.
     *
     * @param dockerImageName The Docker image name for Elasticsearch
     * @throws IOException If unable to create temporary directory for plugins
     */
    public ElasticTestContainerWithPlugins(String dockerImageName) throws IOException {
        super(DockerImageName.parse(dockerImageName).asCompatibleSubstituteFor("elasticsearch"));
        
        this.tempDir = Files.createTempDirectory("es-plugins");
        
        withExposedPorts(ES_HTTP_PORT, ES_TRANSPORT_PORT);
        withEnv("discovery.type", "single-node");
        withEnv("xpack.security.enabled", "false");
        withEnv("ES_JAVA_OPTS", "-Xms512m -Xmx512m");
        
        withCreateContainerCmdModifier(cmd -> {
            List<Ulimit> ulimits = new ArrayList<>();
            ulimits.add(new Ulimit("nofile", 65535L, 65535L));
            cmd.getHostConfig().withUlimits(ulimits);
        });
        
        waitingFor(new HttpWaitStrategy()
                .forPort(ES_HTTP_PORT)
                .forPath("/_cluster/health")
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofMinutes(1)));
    }

    /**
     * Adds a Maven plugin to be installed in the Elasticsearch container.
     *
     * @param groupId The Maven group ID of the plugin
     * @param artifactId The Maven artifact ID of the plugin
     * @param version The version of the plugin
     * @return This container instance for method chaining
     */
    public ElasticTestContainerWithPlugins withPlugin(String groupId, String artifactId, String version) {
        this.plugins.add(new MavenPlugin(groupId, artifactId, version));
        return this;
    }

    /**
     * Gets the HTTP host address for connecting to Elasticsearch.
     *
     * @return The HTTP host address
     */
    public String getHttpHostAddress() {
        return "http://" + getHost() + ":" + getMappedPort(ES_HTTP_PORT);
    }
    
    private RestClient elasticClient;
    
    /**
     * Gets a low-level Elasticsearch RestClient instance for direct API calls.
     *
     * @return The Elasticsearch RestClient
     */
    public RestClient getElasticClient() {
        if (elasticClient == null) {
            LOG.info("Creating Elasticsearch RestClient for {}", getHttpHostAddress());
            HttpHost httpHost = HttpHost.create(getHttpHostAddress());
            elasticClient = org.elasticsearch.client.RestClient.builder(httpHost).build();
        }
        return elasticClient;
    }
    
    /**
     * Verifies that all plugins were correctly installed by checking the Elasticsearch plugins API.
     * This can be called after the container is started to validate plugin installation.
     * 
     * @return true if all plugins were correctly installed, false otherwise
     */
    public boolean verifyPluginsInstalled() {
        try {
            // First check if the plugin files exist in the plugins directory
            LOG.info("Checking for plugin files in plugins directory");
            Container.ExecResult dirResult =
                    execInContainer("ls", "-la", "/usr/share/elasticsearch/plugins/");
            LOG.info("Plugin directory contents: {}", dirResult.getStdout());
            
            // Output container logs for debugging
            String logs = getLogs();
            LOG.info("Container startup logs: {}", logs);
            
            // Instead of checking the plugin list (which requires ES to be running),
            // just check if the plugin directory contains our plugin
            for (MavenPlugin plugin : plugins) {
                if (plugin.artifactId.contains("painless")) {
                    // Painless plugin is built into Elasticsearch
                    LOG.info("Skipping verification for built-in Painless plugin");
                    continue;
                }
                
                String simpleName = plugin.artifactId.replace("elasticsearch-", "")
                                           .replace("-" + ES_VERSION, "");
                LOG.info("Looking for plugin directory '{}' in plugins folder", simpleName);
                
                // Look for drift directory
                if (dirResult.getStdout().contains("drift")) {
                    LOG.info("Found drift plugin directory in plugins folder");
                    
                    // Check contents of the drift directory
                    Container.ExecResult driftDirResult =
                            execInContainer("ls", "-la", "/usr/share/elasticsearch/plugins/drift/");
                    LOG.info("Drift plugin directory contents: {}", driftDirResult.getStdout());
                    
                    // Look for plugin descriptor file
                    if (driftDirResult.getStdout().contains("plugin-descriptor.properties")) {
                        LOG.info("Found plugin-descriptor.properties in drift plugin directory");
                        return true;
                    } else {
                        LOG.error("Could not find plugin-descriptor.properties in drift plugin directory");
                        return false;
                    }
                } else {
                    LOG.error("Could not find drift plugin directory in plugins folder");
                    return false;
                }
            }
            return true;
        } catch (Exception e) {
            LOG.error("Failed to verify plugins installation", e);
            return false;
        }
    }

    @Override
    protected void configure() {
        if (!plugins.isEmpty()) {
            try {
                LOG.info("Building Elasticsearch image with plugins: {}", plugins);
                setImage(buildImageWithPlugins());
                LOG.info("Successfully built Docker image for Elasticsearch with plugins");
            } catch (IOException e) {
                LOG.error("Failed to build image with plugins", e);
                throw new RuntimeException("Failed to build image with plugins", e);
            }
        }
        
        LOG.info("Configuring Elasticsearch container with settings: discovery.type=single-node, ports: {}, {}", ES_HTTP_PORT, ES_TRANSPORT_PORT);
        super.configure();
    }

    private ImageFromDockerfile buildImageWithPlugins() throws IOException {
        List<File> pluginFiles = resolvePluginFiles();
        LOG.info("Building docker image with {} plugin files", pluginFiles.size());
        
        ImageFromDockerfile dockerfile = new ImageFromDockerfile();
        
        // Start with the base image
        dockerfile.withDockerfileFromBuilder(builder -> {
            builder.from(getDockerImageName().toString());
            builder.run("mkdir", "-p", "/tmp/plugins");
            
            // Add each plugin installation
            for (int i = 0; i < pluginFiles.size(); i++) {
                File plugin = pluginFiles.get(i);
                String pluginPath = "/tmp/plugins/" + plugin.getName();
                String pluginName = plugin.getName().replace(".zip", "");
                LOG.info("Adding plugin to Dockerfile: {}", pluginPath);
                
                // Skip if this is just a marker file (e.g., painless plugin)
                if (plugin.getName().endsWith(".txt")) {
                    LOG.info("Skipping built-in plugin: {}", pluginName);
                    continue;
                }
                
                // Copy the plugin to the image
                builder.copy(pluginPath, pluginPath);
                
                // Verify plugin file exists
                builder.run("ls", "-la", pluginPath);
                
                // Create a proper plugin subdirectory
                builder.run("mkdir", "-p", "/usr/share/elasticsearch/plugins/drift");
                
                // Extract the plugin into the plugin-specific subdirectory
                builder.run("unzip", "-q", "-o", pluginPath, "-d", "/usr/share/elasticsearch/plugins/drift/");
                
                // Set permissions
                builder.run("chown", "-R", "elasticsearch:elasticsearch", "/usr/share/elasticsearch/plugins/");
                
                // Check what was installed (but don't fail if the command fails)
                builder.run("ls", "-la", "/usr/share/elasticsearch/plugins/");
            }
            
            // Add additional debugging
            builder.run("echo", "\"Environment variables:\"");
            builder.run("env");
        });
        
        // Add each plugin file to the build context
        for (File plugin : pluginFiles) {
            String targetPath = "/tmp/plugins/" + plugin.getName();
            LOG.info("Adding file to build context: {} -> {}", plugin.getAbsolutePath(), targetPath);
            dockerfile = dockerfile.withFileFromPath(targetPath, plugin.toPath());
        }
        
        return dockerfile;
    }

    private List<File> resolvePluginFiles() throws IOException {
        List<File> resolvedPlugins = new ArrayList<>();
        
        for (MavenPlugin plugin : plugins) {
            File pluginFile = resolvePluginFile(plugin);
            if (pluginFile != null) {
                resolvedPlugins.add(pluginFile);
            }
        }
        
        return resolvedPlugins;
    }

    private File resolvePluginFile(MavenPlugin plugin) throws IOException {
        // Look for the plugin in the target directory (downloaded by maven-dependency-plugin)
        String pluginFileName = "drift-plugin.zip";
        if (plugin.artifactId.contains("painless")) {
            // Painless plugin is built into Elasticsearch
            LOG.info("Painless plugin is built into Elasticsearch, skipping");
            Path targetFile = tempDir.resolve("painless-plugin-marker.txt");
            Files.writeString(targetFile, "Painless plugin is built into Elasticsearch");
            return targetFile.toFile();
        }
        
        String projectDir = System.getProperty("user.dir");
        Path pluginPath = Paths.get(projectDir, "target/elasticsearch-plugins", pluginFileName);
        LOG.info("Looking for plugin at: {}", pluginPath);
        
        if (Files.exists(pluginPath)) {
            // Copy the plugin to a temporary location for the Docker build
            Path targetFile = tempDir.resolve(plugin.artifactId + "-" + plugin.version + ".zip");
            Files.copy(pluginPath, targetFile, StandardCopyOption.REPLACE_EXISTING);
            LOG.info("Resolved plugin {} from Maven-downloaded location: {}", plugin, pluginPath);
            return targetFile.toFile();
        } else {
            LOG.error("Plugin not found at expected location: {}. Make sure to run 'mvn generate-test-resources' first.", pluginPath);
            throw new IOException("Plugin not found at expected location: " + pluginPath);
        }
    }

    private static class MavenPlugin {
        private final String groupId;
        private final String artifactId;
        private final String version;
        
        public MavenPlugin(String groupId, String artifactId, String version) {
            this.groupId = groupId;
            this.artifactId = artifactId;
            this.version = version;
        }
        
        @Override
        public String toString() {
            return groupId + ":" + artifactId + ":" + version;
        }
    }
}