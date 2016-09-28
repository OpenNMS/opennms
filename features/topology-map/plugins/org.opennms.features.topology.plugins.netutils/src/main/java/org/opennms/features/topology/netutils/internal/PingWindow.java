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

package org.opennms.features.topology.netutils.internal;

import java.util.Objects;

import org.opennms.core.utils.InetAddressUtils;
import org.opennms.features.topology.api.topo.Vertex;
import org.opennms.features.topology.netutils.internal.service.PingRequest;
import org.opennms.features.topology.netutils.internal.service.PingService;

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

    /**
     * Creates the PingWindow to make ping requests.
     *
     * @param vertex The vertex which IP Address is pinged.
     *               It is expected that the IP Address os not null and parseable.
     * @param pingService The {@link PingService} to actually make the ping request.
     */
    public PingWindow(Vertex vertex, PingService pingService) {
        Objects.requireNonNull(vertex);
        Objects.requireNonNull(pingService);

        // Remember initial poll interval, as we poll as soon as we start pinging
        final int initialPollInterval = UI.getCurrent().getPollInterval();

        // Ping Form
        pingForm = new PingForm(InetAddressUtils.getInetAddress(vertex.getIpAddress()));

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
            pingService.cancel();
            resultArea.setValue(resultArea.getValue() + "\n" + "Ping cancelled by user");
            getUI().setPollInterval(initialPollInterval);
            setRunning(false);
        } );
        pingButton = new Button("Ping");
        pingButton.addClickListener((event) -> {
                try {
                    final PingRequest pingRequest = pingForm.getPingRequest();
                    setRunning(true);
                    getUI().setPollInterval(POLL_INTERVAL);
                    resultArea.setValue(""); // Clear
                    pingService.ping(pingRequest, (result) -> {
                        setRunning(!result.isComplete());
                        resultArea.setValue(result.toDetailString());
                        if (result.isComplete()) {
                            getUI().setPollInterval(initialPollInterval);
                        }
                    });
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
        setCaption(String.format("Ping - %s (%s)", vertex.getLabel(), vertex.getIpAddress()));
        setResizable(false);
        setModal(true);
        setWidth(800, Unit.PIXELS);
        setHeight(550, Unit.PIXELS);
        setContent(rootLayout);
        center();
        setRunning(false);

        // Set back to default, when closing
        addCloseListener((CloseListener) e -> {
            pingService.cancel();
            getUI().setPollInterval(initialPollInterval);
        });
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
