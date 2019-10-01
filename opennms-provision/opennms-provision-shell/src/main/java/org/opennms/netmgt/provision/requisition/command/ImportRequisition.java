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

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.http.client.utils.URIBuilder;
import org.apache.karaf.shell.api.action.Action;
import org.apache.karaf.shell.api.action.Argument;
import org.apache.karaf.shell.api.action.Command;
import org.apache.karaf.shell.api.action.Completion;
import org.apache.karaf.shell.api.action.Option;
import org.apache.karaf.shell.api.action.lifecycle.Reference;
import org.apache.karaf.shell.api.action.lifecycle.Service;
import org.opennms.netmgt.events.api.EventConstants;
import org.opennms.netmgt.events.api.EventForwarder;
import org.opennms.netmgt.model.events.EventBuilder;

import com.google.common.base.Strings;

@Command(scope = "provision", name = "import-requisition", description = "Import the requisition from given url")
@Service
public class ImportRequisition implements Action {

    public static final String EVENT_SOURCE = "karaf-shell";
    public static final String URI_SCHEME = "requisition";

    @Reference
    private EventForwarder eventForwarder;

    @Option(name = "-r", aliases = "--rescan", description = "Specify rescanExisting value, valid values : 'yes', 'no', 'dbonly'")
    private String rescanExisting;

    @Argument(index = 0, name = "type", description = "Type", required = true)
    @Completion(ProviderTypeNameCompleter.class)
    private String type;

    @Argument(index = 1, name = "parameters", description = "Provide parameters in key=value form", multiValued = true)
    private List<String> parameters = new LinkedList<>();

    @Override
    public Object execute() throws Exception {
        return sendImportRequisitionEvent(eventForwarder, type, parameters, rescanExisting);
    }

    public static Object sendImportRequisitionEvent(EventForwarder eventForwarder, String type, List<String> parameters, String rescanExisting) throws URISyntaxException {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, EVENT_SOURCE);
        URIBuilder builder = new URIBuilder().setScheme(URI_SCHEME).setHost(type);
        parse(parameters, builder);
        String url = builder.build().toString();
        eventBuilder.addParam(EventConstants.PARM_URL, url);
        if (!Strings.isNullOrEmpty(rescanExisting)) {
            List<String> validValues = Arrays.asList("yes", "dbonly", "no");
            if(validValues.contains(rescanExisting)) {
                eventBuilder.addParam(EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
            } else {
                System.out.printf("Not a valid rescanExisting value, valid values are: %s\n", validValues);
                return null;
            }
        }
        eventForwarder.sendNow(eventBuilder.getEvent());
        System.out.printf("Requisition import triggered asynchronously for URL:\n\t%s\n", url);
        return null;
    }

    private static void parse(List<String> attributeList, URIBuilder builder) {
        if (attributeList != null) {
            for (String keyValue : attributeList) {
                int splitAt = keyValue.indexOf("=");
                if (splitAt <= 0) {
                    throw new IllegalArgumentException("Invalid property " + keyValue);
                } else {
                    String key = keyValue.substring(0, splitAt);
                    String value = keyValue.substring(splitAt + 1, keyValue.length());
                    builder.addParameter(key, value);
                }
            }
        }
    }

}
