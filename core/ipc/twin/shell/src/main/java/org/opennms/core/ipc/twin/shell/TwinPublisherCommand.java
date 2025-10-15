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

package org.opennms.core.ipc.twin.shell;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.core.ipc.twin.api.TwinPublisher;


@Command(scope = "opennms", name = "twin-publisher", description = "Validates whether Twin publisher is available")
@Service
public class TwinPublisherCommand implements Action {

    @Reference
    private TwinPublisher twinPublisher;

    @Override
    public Object execute() throws Exception {
        twinPublisher.register("test.publisher", String.class).publish("Testing publisher");
        System.out.println("Published test key, this is just to validate if publisher is available \n");
        return null;
    }
}
