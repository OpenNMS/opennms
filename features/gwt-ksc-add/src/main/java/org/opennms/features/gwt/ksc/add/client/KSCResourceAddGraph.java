/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.features.gwt.ksc.add.client;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.rest.DefaultKscReportService;
import org.opennms.features.gwt.ksc.add.client.rest.KscReportService;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.RootPanel;

public class KSCResourceAddGraph implements EntryPoint {
    final static public boolean DEBUG = true;
    final static private String m_debugResponse =
        "{" +
            "\"@totalCount\":\"2\"," +
            "\"@count\":\"2\"," +
            "\"kscReport\":[" +
                "{\"@label\":\"Test\",\"@id\":\"0\"}," +
                "{\"@label\":\"Test 2\",\"@id\":\"1\"}" +
            "]" +
        "}";

    @Override
    public void onModuleLoad() {
        final KscReportService service = new DefaultKscReportService();
        final NodeList<Element> nodes = RootPanel.getBodyElement().getElementsByTagName("opennms-addKscReport");

        if (nodes.getLength() > 0) {
            service.getAllReports(new RequestCallback() {
                @Override
                public void onResponseReceived(final Request request, final Response response) {
                    final String responseText;
                    if (response.getStatusCode() == 200) {
                        responseText = response.getText();
                    } else {
                        if (DEBUG) {
                            responseText = m_debugResponse;
                        } else {
                            Window.alert("Error occurred retrieving list of KSC reports (response was " + response.getStatusCode() + ".");
                            responseText = null;
                        }
                    }

                    if (responseText != null) {
                        handleResponseText(nodes, responseText);
                    }
                }

                @Override
                public void onError(final Request request, final Throwable exception) {
                    if (DEBUG) {
                        handleResponseText(nodes, m_debugResponse);
                    } else {
                        Window.alert("Error occurred retrieving list of KSC reports: " + exception.getLocalizedMessage());
                    }
                }
                
                public void handleResponseText(final NodeList<Element> nodes, final String responseText) {
                    final List<KscReport> reports = KscReportRestResponseMapper.translate(responseText);
                    for (int i = 0; i < nodes.getLength(); i++) {
                        createView(nodes.getItem(i), reports);
                    }
                }
            });
        }

    }

    private void createView(final Element elem, final List<KscReport> kscReportList) {
        final GraphInfo info = new GraphInfo(elem);

        GWT.log("creating view for graph " + info);
        new AppController(kscReportList, info).go(RootPanel.get(elem.getId()));
    }
}
