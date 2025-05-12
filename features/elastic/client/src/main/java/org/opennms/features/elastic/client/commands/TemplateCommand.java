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
package org.opennms.features.elastic.client.commands;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.http.util.EntityUtils;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.opennms.features.elastic.client.DefaultElasticRestClient;
import org.opennms.features.elastic.client.ElasticRestClient;

@Command(scope = "opennms", name = "elastic-template", description = "Manage Elasticsearch templates")
@Service
public class TemplateCommand implements Action {

    @Reference
    private ElasticRestClient elasticRestClient;

    @Option(name = "-l", aliases = "--list", description = "List available templates")
    private boolean list;

    @Option(name = "-a", aliases = "--apply", description = "Apply a template")
    private boolean apply;

    @Option(name = "-s", aliases = "--show", description = "Show template content")
    private boolean show;

    @Option(name = "-d", aliases = "--directory", description = "Template directory")
    private String directory;

    @Option(name = "--apply-all", description = "Apply all templates from directory")
    private boolean applyAll;

    @Option(name = "--component", description = "Operate on component templates")
    private boolean componentTemplate;

    @Option(name = "--policy", description = "Operate on ILM policies")
    private boolean ilmPolicy;

    @Argument(index = 0, name = "template", description = "Template name", required = false)
    private String templateName;

    @Override
    public Object execute() throws Exception {
        try {
            if (!elasticRestClient.isConnected()) {
                System.out.println("Connecting to Elasticsearch...");
                elasticRestClient.connect();
            }


            if (applyAll && directory != null) {
                int count = ((DefaultElasticRestClient)elasticRestClient).applyAllTemplatesFromDirectory(directory);
                System.out.println("Applied " + count + " templates and policies");
                return null;
            } else if (list) {
                listResources();
                return null;
            } else if (show) {
                showResource();
                return null;
            } else if (apply) {
                applyResource();
                return null;
            }

            System.out.println("Please specify an operation: --list, --show, --apply, or --apply-all");
            return null;
        } catch (Exception e) {
            System.err.println("Error: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private void listResources() throws IOException {
        String endpoint;
        if (ilmPolicy) {
            endpoint = "/_ilm/policy";
        } else if (componentTemplate) {
            endpoint = "/_component_template";
        } else {
            endpoint = "/_index_template";
        }

        Request request = new Request("GET", endpoint);
        Response response = ((DefaultElasticRestClient)elasticRestClient).getRestClient().performRequest(request);

        String type = ilmPolicy ? "ILM Policies" : (componentTemplate ? "Component Templates" : "Index Templates");
        System.out.println(type + ":");

        String responseBody = EntityUtils.toString(response.getEntity());
        // Parse JSON and print names (simplified here)
        System.out.println(responseBody);
    }

    private void showResource() throws IOException {
        if (templateName == null) {
            System.err.println("Error: Name is required for --show operation");
            return;
        }

        String endpoint;
        if (ilmPolicy) {
            endpoint = "/_ilm/policy/" + templateName;
        } else if (componentTemplate) {
            endpoint = "/_component_template/" + templateName;
        } else {
            endpoint = "/_index_template/" + templateName;
        }

        Request request = new Request("GET", endpoint);

        try {
            Response response = ((DefaultElasticRestClient)elasticRestClient).getRestClient().performRequest(request);
            System.out.println(EntityUtils.toString(response.getEntity()));
        } catch (Exception e) {
            System.err.println("Error: Resource '" + templateName + "' not found");
        }
    }

    private void applyResource() throws IOException {
        if (templateName == null || directory == null) {
            System.err.println("Error: Name and directory are required for --apply operation");
            return;
        }

        String subdir;
        if (ilmPolicy) {
            subdir = "policies";
        } else if (componentTemplate) {
            subdir = "components";
        } else {
            subdir = "index-templates";
        }

        Path resourcePath = Paths.get(directory, subdir, templateName + ".json");

        if (!Files.exists(resourcePath)) {
            System.err.println("Error: File not found: " + resourcePath);
            return;
        }

        String content = new String(Files.readAllBytes(resourcePath));
        boolean success;

        if (ilmPolicy) {
            success = ((DefaultElasticRestClient)elasticRestClient).applyILMPolicy(templateName, content);
        } else if (componentTemplate) {
            success = ((DefaultElasticRestClient)elasticRestClient).applyComponentTemplate(templateName, content);
        } else {
            success = ((DefaultElasticRestClient)elasticRestClient).applyComposableIndexTemplate(templateName, content);
        }

        String resourceType = ilmPolicy ? "policy" : (componentTemplate ? "component template" : "index template");
        if (success) {
            System.out.println("Successfully applied " + resourceType + ": " + templateName);
        } else {
            System.err.println("Failed to apply " + resourceType + ": " + templateName);
        }
    }
}