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
package org.opennms.features.topology.netutils.internal.ping;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingStringUtils;
import org.opennms.netmgt.icmp.proxy.PingSummary;

import com.vaadin.v7.data.fieldgroup.FieldGroup;
import com.vaadin.v7.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.v7.ui.HorizontalLayout;
import com.vaadin.v7.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.v7.ui.ProgressBar;
import com.vaadin.v7.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.v7.ui.VerticalLayout;
import com.vaadin.ui.Window;

/**
 * The PingWindow class creates a Vaadin Sub-window with a form and results section
 * for the Ping functionality of a Vertex.
 *
 * @author Leonardo Bell
 * @author Philip Grenon
 * @author Markus von Rüden
 * @version 2.0
 */
public class PingWindow extends Window {

    /**
     * As soon as we start the ping command, we must poll for state changes, as the ping is executed asynchronously.
     * The POLL_INTERVAL determines the poll interval in milliseconds.
     */
    public static final int POLL_INTERVAL = 1000;

    /**
     * If a ping is started, it is indicated.
     */
    private final ProgressBar progressIndicator;

    /**
     * Cancels the current ping
     */
    private final Button cancelButton;

    /**
     * Starts a ping
     */
    private final Button pingButton;

    /**
     * The form to configure the Ping command.
     */
    private final PingForm pingForm;

    private CompletableFuture<PingSummary> pingFuture;

    /**
     * Creates the PingWindow to make ping requests.
     *
     * @param locations All available locations to ping from. Must not be null.
     * @param defaultLocation The location to pre-select from the locations. Ensure <code>defaultLocation</code> is also available in the <code>locations</code> list.
     * @param ipAddresses All available ipAddresses. Must not be null or empty.
     * @param defaultIp The default ip to pre-select from the ip addresses. Ensure <code>defaultIp</code> is also available in the <code>ipAddresses</code> list.
     * @param pingClient The LocationAwarePingClient to ping. Must not be null.
     */
    public PingWindow(LocationAwarePingClient pingClient, List<String> locations, List<String> ipAddresses, String defaultLocation, String defaultIp, String caption) {
        Objects.requireNonNull(pingClient);
        Objects.requireNonNull(ipAddresses);
        Objects.requireNonNull(defaultIp);
        Objects.requireNonNull(locations);
        Objects.requireNonNull(defaultLocation);
        Objects.requireNonNull(caption);

        // Remember initial poll interval, as we poll as soon as we start pinging
        final int initialPollInterval = UI.getCurrent().getPollInterval();

        // Ping Form
        pingForm = new PingForm(locations, defaultLocation, ipAddresses, defaultIp);

        // Result
        final TextArea resultArea = new TextArea();
        resultArea.setRows(15);
        resultArea.setSizeFull();

        // Progress Indicator
        progressIndicator = new ProgressBar();
        progressIndicator.setIndeterminate(true);

        // Buttons
        cancelButton = new Button("Cancel");
        cancelButton.addClickListener((event) -> {
            cancel(pingFuture);
            resultArea.setValue(resultArea.getValue() + "\n" + "Ping cancelled by user");
            getUI().setPollInterval(initialPollInterval);
            setRunning(false);
        } );
        pingButton = new Button("Ping");
        pingButton.addClickListener((event) -> {
            try {
                resultArea.setValue(""); // Clear
                getUI().setPollInterval(POLL_INTERVAL);

                final PingRequest pingRequest = pingForm.getPingRequest();
                final InetAddress inetAddress = convertToInetAddress(pingRequest.getIpAddress());
                if (inetAddress == null) {
                    final String error = "'" + pingRequest.getIpAddress() + "' is not a valid IP Address";
                    Notification.show(error);
                    resultArea.setValue(error);
                    return;
                }
                setRunning(true);
                pingFuture = pingClient.ping(inetAddress)
                        .withRetries(pingRequest.getRetries())
                        .withPacketSize(pingRequest.getPacketSize())
                        .withTimeout(pingRequest.getTimeout(), TimeUnit.MILLISECONDS)
                        .withLocation(pingRequest.getLocation())
                        .withNumberOfRequests(pingRequest.getNumberRequests())
                        .withProgressCallback((newSequence, summary) -> {
                                getUI().accessSynchronously(() -> {
                                    if (pingFuture != null && !pingFuture.isCancelled()) {
                                    setRunning(!summary.isComplete());
                                    resultArea.setValue(PingStringUtils.renderAll(summary));
                                    if (summary.isComplete()) {
                                        getUI().setPollInterval(initialPollInterval);
                                    }
                                }
                            });
                        })
                        .execute();
            } catch (FieldGroup.CommitException e) {
                Notification.show("Validation errors", "Please correct them. Make sure all required fields are set.", Notification.Type.ERROR_MESSAGE);
            }
        });

        // Button Layout
        final HorizontalLayout buttonLayout = new HorizontalLayout();
        buttonLayout.setSpacing(true);
        buttonLayout.addComponent(pingButton);
        buttonLayout.addComponent(cancelButton);
        buttonLayout.addComponent(progressIndicator);

        // Root Layout
        final VerticalLayout rootLayout = new VerticalLayout();
        rootLayout.setSpacing(true);
        rootLayout.setMargin(true);
        rootLayout.addComponent(pingForm);
        rootLayout.addComponent(buttonLayout);
        rootLayout.addComponent(new Label("<b>Results</b>", ContentMode.HTML));
        rootLayout.addComponent(resultArea);
        rootLayout.setExpandRatio(resultArea, 1.0f);

        // Window Config
        setCaption(caption);
        setResizable(false);
        setModal(true);
        setWidth(800, Unit.PIXELS);
        setHeight(600, Unit.PIXELS);
        setContent(rootLayout);
        center();
        setRunning(false);

        // Set back to default, when closing
        addCloseListener((CloseListener) e -> {
            cancel(pingFuture);
            UI.getCurrent().setPollInterval(initialPollInterval);
        });
    }

    private InetAddress convertToInetAddress(String ipAddress) {
        try {
            return InetAddressUtils.getInetAddress(ipAddress);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private static void cancel(CompletableFuture pingFuture) {
        if (pingFuture != null) {
            pingFuture.cancel(true);
        }
    }

    private void setRunning(boolean running) {
        cancelButton.setEnabled(running);
        progressIndicator.setVisible(running);
        pingButton.setEnabled(!running);
        pingForm.setEnabled(!running);
    }

    public void open() {
        UI.getCurrent().addWindow(this);
    }
}
