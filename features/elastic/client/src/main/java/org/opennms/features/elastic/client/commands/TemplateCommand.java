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

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

    @Option(name = "-l", aliases = "--list", description = "List available templates (only names)")
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


            if (applyAll && directory != null) {
                int count = elasticRestClient.applyAllTemplatesFromDirectory(directory);
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
            return null;
        }
    }


    private void listResources() throws IOException {
        String endpoint;
        String type;
        if (ilmPolicy) {
            endpoint = "/_ilm/policy";
            type = "ILM Policies";
        } else if (componentTemplate) {
            endpoint = "/_component_template";
            type = "Component Templates";
        } else {
            endpoint = "/_index_template";
            type = "Index Templates";
        }

        Request request = new Request("GET", endpoint);
        Response response = ((DefaultElasticRestClient)elasticRestClient).getRestClient().performRequest(request);
        String responseBody = EntityUtils.toString(response.getEntity());

        System.out.println(type + ":");

        Gson gson = new Gson();
        JsonElement jsonElement = gson.fromJson(responseBody, JsonElement.class);

        if (ilmPolicy) {
            JsonObject root = jsonElement.getAsJsonObject();
            for (String policyName : root.keySet()) {
                System.out.println("- " + policyName);
            }
        } else if (componentTemplate) {
            JsonArray templates = jsonElement.getAsJsonObject().getAsJsonArray("component_templates");
            for (JsonElement el : templates) {
                String name = el.getAsJsonObject().get("name").getAsString();
                System.out.println("- " + name);
            }
        } else {
            JsonArray templates = jsonElement.getAsJsonObject().getAsJsonArray("index_templates");
            for (JsonElement el : templates) {
                String name = el.getAsJsonObject().get("name").getAsString();
                System.out.println("- " + name);
            }
        }
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

        Path resourcePath = Paths.get(directory, templateName + ".json");

        if (!Files.exists(resourcePath)) {
            System.err.println("Error: File not found: " + resourcePath);
            return;
        }

        String content = new String(Files.readAllBytes(resourcePath));
        boolean success;

        if (ilmPolicy) {
            success = elasticRestClient.applyILMPolicy(templateName, content);
        } else if (componentTemplate) {
            success = elasticRestClient.applyComponentTemplate(templateName, content);
        } else {
            success = elasticRestClient.applyComposableIndexTemplate(templateName, content);
        }

        String resourceType = ilmPolicy ? "policy" : (componentTemplate ? "component template" : "index template");
        if (success) {
            System.out.println("Successfully applied " + resourceType + ": " + templateName);
        } else {
            System.err.println("Failed to apply " + resourceType + ": " + templateName);
        }
    }
}