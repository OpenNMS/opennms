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
package org.opennms.features.topology.shell;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;

@Command(scope="opennms", name="topo-show-history", description="Shows the history of a certain user")
@Service
public class ShowHistoryCommand implements Action {

    @Argument(required=false, name="user", description="The user to show the history for.")
    String user = "admin";

    @Reference
    public HistoryManager historyManager;

    @Override
    public Object execute() throws Exception {
        final SavedHistory savedHistory = historyManager.getHistoryByUserId(user);
        if (savedHistory == null) {
            System.out.println("No History for user '" + user + "' found.");
        } else {
            System.out.println("History for user '" + user + "':");
            JAXBContext jaxbContext = JAXBContext.newInstance(SavedHistory.class);
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            jaxbMarshaller.marshal(savedHistory, System.out);
        }
        return null;
    }
}
