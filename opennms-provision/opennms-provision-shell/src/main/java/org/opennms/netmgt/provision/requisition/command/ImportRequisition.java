/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2019 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2019 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.provision.requisition.command;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

import com.google.common.base.Strings;

@Command(scope = "provision", name = "import-requisition", description = "Load the requisition from given parameters")
@Service
public class ImportRequisition implements Action {

    private static final String EVENT_SOURCE = "karaf-shell";

    @Reference
    private EventForwarder eventForwarder;

    @Argument(index = 0, name = "type", description = "Type", required = true, multiValued = false)
    @Completion(ProviderTypeNameCompleter.class)
    String type;

    @Argument(index = 1, name = "requisition", description = "Provide requisition in key=value format", required = true, multiValued = false)
    String requisition;

    @Override
    public Object execute() throws Exception {

        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, EVENT_SOURCE);

        if((!Strings.isNullOrEmpty(requisition)) && (!Strings.isNullOrEmpty(type)) && requisition.contains("=")) {
            String url = String.format("requisition://%s?%s", type, requisition);
            eventBuilder.addParam("url", url);
            eventForwarder.sendNow(eventBuilder.getEvent());
            System.out.printf("Import Requisition Event sent with URL : %s \n ", url);
        } else {
            System.out.printf("No valid requisition specified, It should be specified in 'name=myServer' format \n");
        }
        return null;
    }

    private static Map<String, String> parse(List<String> params) {
        Map<String, String> properties = new HashMap<>();
        if (params != null) {
            for (String keyValue : params) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid param " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    properties.put(key, value);
                }
            }
        }
        return properties;
    }
}
