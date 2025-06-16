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
package org.opennms.netmgt.ticketer.jira.commands;

import java.util.Properties;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.core.mate.api.EntityScopeProvider;
import org.opennms.netmgt.ticketer.jira.JiraTicketerPlugin;

@Command(scope = "opennms", name = "jira-show-config", description="Shows the current configuration for the Jira Ticketer Plugin")
@org.apache.karaf.shell.commands.Command(scope = "opennms", name = "jira-show-config", description="Shows the current configuration for the Jira Ticketer Plugin")
@Service
public class ShowConfigCommand extends OsgiCommandSupport implements Action {

    protected EntityScopeProvider entityScopeProvider;

    public void setEntityScopeProvider(EntityScopeProvider entityScopeProvider) {
        this.entityScopeProvider = entityScopeProvider;
    }

    @Override
    public Object execute() throws Exception {
        Properties properties = JiraTicketerPlugin.getConfig(entityScopeProvider).getProperties();
        for (String eachKey : properties.stringPropertyNames()) {
            System.out.println(eachKey + " = " + properties.getProperty(eachKey));
        }
        return null;
    }

    @Override
    @Deprecated
    protected final Object doExecute() throws Exception {
        return execute();
    }

}
