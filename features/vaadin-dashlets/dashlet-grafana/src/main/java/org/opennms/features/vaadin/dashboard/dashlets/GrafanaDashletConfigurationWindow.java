/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2017 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2017 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.dashboard.dashlets;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardConfigUI;
import org.opennms.features.vaadin.dashboard.config.ui.WallboardProvider;
import org.opennms.features.vaadin.dashboard.model.DashletConfigurationWindow;
import org.opennms.features.vaadin.dashboard.model.DashletSpec;

import com.google.common.base.Strings;
import com.vaadin.event.ShortcutAction;
import com.vaadin.shared.ui.label.ContentMode;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.NativeSelect;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;

/**
 * This class implements a configuration window for the  Grafana dashlet.
 *
 * @author Christian Pape
 */
public class GrafanaDashletConfigurationWindow extends DashletConfigurationWindow {

    private DashletSpec m_dashletSpec;

    /**
     * Exception for handling errors
     */
    public static class GrafanaDashletException extends Exception {
        public GrafanaDashletException(String message) {
            super(message);
        }
    }

    /**
     * Constructor for instantiating new objects of this class.
     *
     * @param dashletSpec the {@link DashletSpec} to be edited
     */
    public GrafanaDashletConfigurationWindow(DashletSpec dashletSpec) {
        /**
         * Setting the members
         */
        m_dashletSpec = dashletSpec;

        setWidth("60%");
        setHeight("55%");

        /**
         * Layout the components...
         */
        FormLayout topLayout = new FormLayout();
        topLayout.setSizeFull();
        topLayout.setWidth("100%");

        NativeSelect dashboardSelect = new NativeSelect();
        dashboardSelect.setCaption("Grafana Dashboard");
        dashboardSelect.addItem("Test");
        dashboardSelect.setDescription("Grafana Dashboard to be displayed");
        dashboardSelect.setImmediate(true);
        dashboardSelect.setNewItemsAllowed(false);
        dashboardSelect.setMultiSelect(false);
        dashboardSelect.setInvalidAllowed(false);
        dashboardSelect.setNullSelectionAllowed(false);
        dashboardSelect.setWidth("80%");

        GrafanaDashletQuickRangePicker grafanaDashletQuickRangePicker = new GrafanaDashletQuickRangePicker();
        grafanaDashletQuickRangePicker.setCaption("Quick ranges");
        grafanaDashletQuickRangePicker.setDescription("Quick ranges defined");
        grafanaDashletQuickRangePicker.setImmediate(true);
        grafanaDashletQuickRangePicker.setWidth("100%");

        TextField timeRangeFrom = new TextField();
        timeRangeFrom.setCaption("From");
        timeRangeFrom.setDescription("Start of time range to be displayed");
        timeRangeFrom.setImmediate(true);
        timeRangeFrom.setWidth("60%");
        timeRangeFrom.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);

        TextField timeRangeTo = new TextField();
        timeRangeTo.setCaption("To");
        timeRangeTo.setDescription("End of time range to be displayed");
        timeRangeTo.setImmediate(true);
        timeRangeTo.setWidth("60%");
        timeRangeTo.setTextChangeEventMode(AbstractTextField.TextChangeEventMode.EAGER);

        topLayout.addComponent(dashboardSelect);
        topLayout.addComponent(grafanaDashletQuickRangePicker);
        topLayout.addComponent(timeRangeFrom);
        topLayout.addComponent(timeRangeTo);

        /**
         * Implement listeners for value changes...
         */
        grafanaDashletQuickRangePicker.addQuickRangeListener(q -> {
            timeRangeFrom.setValue(q.getFrom());
            timeRangeTo.setValue(q.getTo());
        });

        timeRangeFrom.addTextChangeListener(e -> {
            grafanaDashletQuickRangePicker.selectQuickRange(e.getText(), timeRangeTo.getValue());
        });

        timeRangeTo.addTextChangeListener(e -> {
            grafanaDashletQuickRangePicker.selectQuickRange(timeRangeFrom.getValue(), e.getText());
        });

        /**
         * Now make the Grafana API call...
         */
        final Map<String, String> grafanaDashboards;

        try {
            grafanaDashboards = getGrafanaDashboards();

            String firstItem = null;
            for(Map.Entry<String, String> grafanaDashboard : grafanaDashboards.entrySet()) {
                dashboardSelect.addItem(grafanaDashboard.getKey());

                if (firstItem == null) {
                    firstItem = grafanaDashboard.getKey();
                }
            }

            dashboardSelect.select(firstItem);

            if (!Strings.isNullOrEmpty(m_dashletSpec.getParameters().get("title"))) {
                dashboardSelect.select(m_dashletSpec.getParameters().get("title"));
            }

            timeRangeFrom.setValue(m_dashletSpec.getParameters().get("from"));
            timeRangeTo.setValue(m_dashletSpec.getParameters().get("to"));

            grafanaDashletQuickRangePicker.selectQuickRange(timeRangeFrom.getValue(), timeRangeTo.getValue());
        } catch (GrafanaDashletException e) {
            /**
             * On failure create a simple layout with error description and message...
             */
            Label errorLabel = new Label("<b>Error retrieving Grafana Dashboards via HTTP API!</b><br/>" +
                    "Please verify that the following entries are correctly configured in your opennms.properties:" +
                    "<ul><li>org.opennms.grafanaBox.apiKey</li>" +
                    "<li>org.opennms.grafanaBox.protocol</li>" +
                    "<li>org.opennms.grafanaBox.hostname</li>" +
                    "<li>org.opennms.grafanaBox.port</li></ul>" +
                    "Error message:<br/>" +
                    "<pre>" + StringEscapeUtils.escapeHtml(e.getMessage()) + "</pre>");

            errorLabel.setContentMode(ContentMode.HTML);

            VerticalLayout errorLayout = new VerticalLayout();
            errorLayout.addComponent(errorLabel);

            VerticalLayout verticalLayout = new VerticalLayout();
            verticalLayout.setMargin(true);

            HorizontalLayout closeButtonLayout = new HorizontalLayout();

            closeButtonLayout.setMargin(true);
            closeButtonLayout.setSpacing(true);
            closeButtonLayout.setWidth("100%");

            /**
             * ...add only a "Close" button...
             */
            Button closeButton = new Button("Close", event -> { close(); });
            closeButton.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);

            closeButtonLayout.addComponent(closeButton);

            verticalLayout.addComponent(errorLayout);
            verticalLayout.addComponent(closeButtonLayout);
            verticalLayout.setExpandRatio(errorLayout, 0.85f);
            verticalLayout.setExpandRatio(closeButtonLayout, 0.15f);
            verticalLayout.setSizeFull();

            /**
             * ...and display it.
             */
            setContent(verticalLayout);
            return;
        }

        /**
         * Using an additional {@link com.vaadin.ui.HorizontalLayout} for layouting the buttons
         */
        HorizontalLayout buttonLayout = new HorizontalLayout();

        buttonLayout.setMargin(true);
        buttonLayout.setSpacing(true);
        buttonLayout.setWidth("100%");

        /**
         * Adding the cancel button...
         */
        Button cancel = new Button("Cancel");
        cancel.setDescription("Cancel editing");
        cancel.addClickListener(e->{
            close();
        });

        cancel.setClickShortcut(ShortcutAction.KeyCode.ESCAPE, null);
        buttonLayout.addComponent(cancel);
        buttonLayout.setExpandRatio(cancel, 1.0f);
        buttonLayout.setComponentAlignment(cancel, Alignment.TOP_RIGHT);

        /**
         * ...and the OK button
         */
        Button ok = new Button("Save");
        ok.setDescription("Save properties and close");

        ok.addClickListener(e->{
            /**
             * saving the data
             */
            m_dashletSpec.getParameters().put("uri", grafanaDashboards.get(dashboardSelect.getValue().toString()));
            m_dashletSpec.getParameters().put("title", dashboardSelect.getValue().toString());
            m_dashletSpec.getParameters().put("from", timeRangeFrom.getValue());
            m_dashletSpec.getParameters().put("to", timeRangeTo.getValue());

            WallboardProvider.getInstance().save();
            ((WallboardConfigUI) getUI()).notifyMessage("Data saved", "Properties");

            close();
        });

        ok.setClickShortcut(ShortcutAction.KeyCode.ENTER, null);
        buttonLayout.addComponent(ok);

        /**
         * Now mix all together...
         */
        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setMargin(true);

        verticalLayout.addComponent(topLayout);
        verticalLayout.addComponent(buttonLayout);
        verticalLayout.setExpandRatio(topLayout, 0.85f);
        verticalLayout.setExpandRatio(buttonLayout, 0.15f);
        verticalLayout.setSizeFull();

        setContent(verticalLayout);
    }

    private Map<String, String> getGrafanaDashboards() throws GrafanaDashletException {
        /**
         * Loading the required properties...
         */
        final String grafanaApiKey = System.getProperty("org.opennms.grafanaBox.apiKey", "");
        final String grafanaProtocol = System.getProperty("org.opennms.grafanaBox.protocol", "http");
        final String grafanaHostname = System.getProperty("org.opennms.grafanaBox.hostname", "localhost");
        final int grafanaPort = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.port", "3000"));
        final int grafanaConnectionTimeout = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.connectionTimeout", "500"));
        final int grafanaSoTimeout = Integer.parseInt(System.getProperty("org.opennms.grafanaBox.soTimeout", "500"));

        if (!"".equals(grafanaApiKey)
                && !"".equals(grafanaHostname)
                && !"".equals(grafanaProtocol)
                && ("http".equals(grafanaProtocol) || "https".equals(grafanaProtocol))) {
            try (CloseableHttpClient httpClient = HttpClients.createDefault();) {
                final RequestConfig requestConfig = RequestConfig.custom()
                        .setConnectTimeout(grafanaConnectionTimeout)
                        .setSocketTimeout(grafanaSoTimeout)
                        .build();
                final URI uri = new URIBuilder()
                        .setScheme(grafanaProtocol)
                        .setHost(grafanaHostname)
                        .setPort(grafanaPort)
                        .setPath("/api/search/")
                        .build();

                final HttpGet httpGet = new HttpGet(uri);
                httpGet.setConfig(requestConfig);

                /**
                 * Adding the API key...
                 */
                httpGet.setHeader("Authorization", "Bearer " + grafanaApiKey);

                final Map<String, String> resultSet = new TreeMap<>();

                try (CloseableHttpResponse httpResponse = httpClient.execute(httpGet);) {
                    HttpEntity httpEntity = httpResponse.getEntity();
                    if (httpEntity != null) {
                        /**
                         * Fill the result set...
                         */
                        final String responseString = IOUtils.toString(httpEntity.getContent(), StandardCharsets.UTF_8.name());
                        if (!Strings.isNullOrEmpty(responseString)) {
                            try {
                                final JSONArray arr = new JSONObject("{dashboards:" + responseString + "}").getJSONArray("dashboards");
                                for (int i = 0; i < arr.length(); i++) {
                                    resultSet.put(arr.getJSONObject(i).getString("title"), arr.getJSONObject(i).getString("uri"));
                                }
                            } catch (JSONException e) {
                                throw new GrafanaDashletException(e.getMessage());
                            }
                        }
                    }
                }
                return resultSet;
            } catch (Exception e) {
                throw new GrafanaDashletException(e.getMessage());
            }
        } else {
            throw new GrafanaDashletException("Invalid configuration");
        }
    }
}
