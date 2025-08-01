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
package org.opennms.netmgt.provision.requisition.command;

import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Future;
import java.util.function.BooleanSupplier;
import java.util.function.Function;
import java.util.function.Supplier;

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
import org.opennms.netmgt.events.api.EventListener;
import org.opennms.netmgt.events.api.EventSubscriptionService;
import org.opennms.netmgt.events.api.model.IEvent;
import org.opennms.netmgt.model.events.EventBuilder;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

@Command(scope = "opennms", name = "import-requisition", description = "Sends an 'uei.opennms.org/internal/importer/reloadImport' event to import the requisition from a given url parameter")
@Service
public class ImportRequisition implements Action {

    public static final String EVENT_SOURCE = "karaf-shell";
    public static final String URI_SCHEME = "requisition";

    @Reference
    private EventForwarder eventForwarder;

    @Reference
    private EventSubscriptionService eventSubscriptionService;

    @Option(name = "-w", aliases = "--wait", description = "Wait for completion event")
    private Boolean wait = false;

    @Option(name = "-r", aliases = "--rescan", description = "Specify rescanExisting value, valid values : 'true', 'false', 'dbonly'")
    private String rescanExisting;

    @Argument(index = 0, name = "type", description = "Type of import handler", required = true)
    @Completion(ProviderTypeNameCompleter.class)
    private String type;

    @Argument(index = 1, name = "parameters", description = "Provide parameters in key=value form", multiValued = true)
    private List<String> parameters = new LinkedList<>();

    static class ImportEventListener implements EventListener {
        public final static List<String> UEIS = Lists.newArrayList(EventConstants.IMPORT_SUCCESSFUL_UEI, EventConstants.IMPORT_FAILED_UEI);
        private String importResource;
        private String receivedUei;

        ImportEventListener(final String importResource) {
            this.importResource = stripCredentials(importResource);
        }

        static String stripCredentials(final String string) {
            if (string == null) {
                return null;
            } else {
                return string.replaceAll("(username=)[^;&]*(;&)?", "$1***$2")
                             .replaceAll("(password=)[^;&]*(;&)?", "$1***$2");
            }
        }

        @Override
        public String getName() {
            return "ImportRequisition-eventListener";
        }

        String getReceivedUei() {
            return this.receivedUei;
        }

        @Override
        public void onEvent(final IEvent e) {
            if (!e.getParm("importResource").getValue().getContent().contains(this.importResource)) {
                return;
            }

            if (e.getUei().equals(EventConstants.IMPORT_SUCCESSFUL_UEI) || e.getUei().equals(EventConstants.IMPORT_FAILED_UEI)) {
                this.receivedUei = e.getUei();
            }
        }

        boolean isDone() {
            return this.receivedUei != null;
        }
    }

    @Override
    public Object execute() throws Exception {
        return sendImportRequisitionEvent(eventForwarder, type, parameters, rescanExisting, wait, eventSubscriptionService);
    }

    public static Object sendImportRequisitionEvent(EventForwarder eventForwarder, String type, List<String> parameters, String rescanExisting, boolean wait, EventSubscriptionService eventSubscriptionService) throws URISyntaxException {
        EventBuilder eventBuilder = new EventBuilder(EventConstants.RELOAD_IMPORT_UEI, EVENT_SOURCE);
        URIBuilder builder = new URIBuilder().setScheme(URI_SCHEME).setHost(type);
        parse(parameters, builder);
        String url = builder.build().toString();
        eventBuilder.addParam(EventConstants.PARM_URL, url);
        if (!Strings.isNullOrEmpty(rescanExisting)) {
            List<String> validValues = Arrays.asList("true", "false", "dbonly");
            if(validValues.contains(rescanExisting)) {
                eventBuilder.addParam(EventConstants.PARM_IMPORT_RESCAN_EXISTING, rescanExisting);
            } else {
                System.out.printf("Not a valid rescanExisting value, valid values are: %s\n", validValues);
                return null;
            }
        }

        final ImportEventListener importEventListener = new ImportEventListener(url);
        if (wait) {
            eventSubscriptionService.addEventListener(importEventListener, ImportEventListener.UEIS);
        }

        eventForwarder.sendNow(eventBuilder.getEvent());
        System.out.printf("Requisition import triggered asynchronously for URL:\n\t%s\n", url);

        if (wait) {
            try {
                while (!importEventListener.isDone()) {
                    try {
                        Thread.sleep(1000);
                        System.out.print(".");
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

                if (EventConstants.IMPORT_SUCCESSFUL_UEI.equals(importEventListener.getReceivedUei())) {
                    System.out.printf("\nImport succeeded.\n");
                } else {
                    System.out.printf("\nImport failed.\n");
                }
            } finally {
                eventSubscriptionService.removeEventListener(importEventListener);
            }
        }

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
