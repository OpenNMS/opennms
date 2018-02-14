/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.topology.netutils.internal.ping;

import java.net.InetAddress;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.opennms.netmgt.icmp.proxy.LocationAwarePingClient;
import org.opennms.netmgt.icmp.proxy.PingStringUtils;
import org.opennms.netmgt.icmp.proxy.PingSummary;

import com.vaadin.data.fieldgroup.FieldGroup;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.Button;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Notification;
import com.vaadin.ui.ProgressBar;
import com.vaadin.ui.TextArea;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;
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
    public PingWindow(LocationAwarePingClient pingClient, List<String> locations, List<InetAddress> ipAddresses, String defaultLocation, InetAddress defaultIp, String caption) {
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
                final PingRequest pingRequest = pingForm.getPingRequest();

                resultArea.setValue(""); // Clear
                setRunning(true);
                getUI().setPollInterval(POLL_INTERVAL);

                pingFuture = pingClient.ping(pingRequest.getInetAddress())
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
