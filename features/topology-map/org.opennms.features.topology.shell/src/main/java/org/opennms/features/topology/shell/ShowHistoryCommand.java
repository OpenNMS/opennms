/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.shell;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.karaf.shell.console.OsgiCommandSupport;
import org.opennms.features.topology.api.HistoryManager;
import org.opennms.features.topology.api.support.SavedHistory;
import org.apache.felix.gogo.commands.Command;
import org.apache.felix.gogo.commands.Argument;

@Command(scope="topo", name="show-history", description="Shows the history of a certain user")
public class ShowHistoryCommand extends OsgiCommandSupport {

    @Argument(required=false, name="user", description="The user to show the history for.")
    String user = "admin";

    private HistoryManager historyManager;

    @Override
    protected Object doExecute() throws Exception {
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

    public void setHistoryManager(HistoryManager historyManager) {
        this.historyManager = historyManager;
    }
}
