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
package org.opennms.netmgt.measurements.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.dao.api.ResourceDao;
import org.opennms.netmgt.model.ResourceId;

@Command(scope = "opennms", name = "delete-measurement-resource", description = "Delete the measurements and meta-data for a given resource ID")
@Service
public class DeleteResource implements Action {

    @Reference
    ResourceDao resourceDao;

    @Argument(description = "Resource ID")
    String resourceId;

    @Override
    public Object execute() {
        System.out.printf("Deleting measurements and meta-data associated with resource ID '%s'...\n", resourceId);
        resourceDao.deleteResourceById(ResourceId.fromString(resourceId));
        System.out.printf("Done.\n");
        return null;
    }

}
