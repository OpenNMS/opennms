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
package org.opennms.netmgt.threshd.shell;

import static org.opennms.netmgt.threshd.AbstractThresholdEvaluatorState.fst;

import java.util.Optional;

import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Service;

@Command(scope = "opennms", name = "threshold-details", description = "Prints the details of a specific " +
        "threshold state")
@Service
public class Details extends AbstractKeyOrIndexCommand {
    @Override
    public Object execute() {
        String key = getKey();
        Optional<byte[]> value = blobStore.get(key, THRESHOLDING_KV_CONTEXT);

        if (value.isPresent()) {
            System.out.println(fst.asObject(value.get()).toString());
        } else {
            System.out.printf("Could not find a state for key '%s'\n", key);
        }

        return null;
    }
}
