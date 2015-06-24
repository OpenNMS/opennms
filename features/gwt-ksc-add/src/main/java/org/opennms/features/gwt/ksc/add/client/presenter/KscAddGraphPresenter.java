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

package org.opennms.features.gwt.ksc.add.client.presenter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.opennms.features.gwt.ksc.add.client.GraphInfo;
import org.opennms.features.gwt.ksc.add.client.KscReport;
import org.opennms.features.gwt.ksc.add.client.rest.DefaultKscReportService;
import org.opennms.features.gwt.ksc.add.client.rest.KscReportService;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyUpEvent;
import com.google.gwt.event.logical.shared.ResizeEvent;
import com.google.gwt.event.logical.shared.ResizeHandler;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.Response;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PopupPanel;

public class KscAddGraphPresenter implements Presenter, KscAddGraphView.Presenter<KscReport> {

    private static final List<KscReport> EMPTY_KSCREPORT_LIST = Collections.unmodifiableList(new ArrayList<KscReport>());
    private KscAddGraphView<KscReport> m_view;
    private List<KscReport> m_KscReports;
    private PopupPanel m_mainPopup;
    private final Image m_addImage;
    private GraphInfo m_graphInfo;
    private final KscReportService m_reportService = new DefaultKscReportService();
    
    public KscAddGraphPresenter(final PopupPanel mainPopup, final KscAddGraphView<KscReport> addGraphView, final List<KscReport> kscReports, final GraphInfo graphInfo) {
        m_mainPopup = mainPopup;
        m_view = addGraphView;
        m_view.setPresenter(this);
        m_KscReports = kscReports;
        m_graphInfo = graphInfo;

        m_addImage = new Image("images/plus.gif");
        m_addImage.setAltText("Add this graph to a KSC report.");
        m_addImage.setTitle("Add this graph to a KSC report.");
    }

    private List<KscReport> filterResultsByName(final String searchText) {
        final List<KscReport> list = new ArrayList<KscReport>();
        for (final KscReport detail : m_KscReports) {
            if (detail.getLabel().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(detail);
            }
        }
        
        return list;
    }

    @Override
    public void onKeyCodeEvent(final KeyCodeEvent<?> event, final String searchText) {
        final int keyCode = event.getNativeEvent().getKeyCode();
        final boolean isKeyUp = event instanceof KeyUpEvent;
        final boolean isKeyDown = event instanceof KeyDownEvent;

        if (isKeyUp && keyCode == KeyCodes.KEY_ESCAPE) {
            GWT.log("escape, hiding results");
            m_view.hidePopup();
        } else if (isKeyUp && keyCode == KeyCodes.KEY_BACKSPACE && searchText.length() == 0) {
            m_view.hidePopup();
            m_view.setDataList(EMPTY_KSCREPORT_LIST);
            m_view.clearSelection();
        } else if (isKeyDown && keyCode == KeyCodes.KEY_ENTER && m_view.getSelectedReport() != null && m_view.getTitle() != null && !m_view.isPopupShowing()) {
            onAddButtonClicked();
        } else if (isKeyUp) {
            if (searchText.length() == 0) {
                GWT.log("search text is empty");
                m_view.setDataList(EMPTY_KSCREPORT_LIST);
            } else {
                GWT.log("search text is not empty");
                final List<KscReport> results = filterResultsByName(searchText);
                if (keyCode == KeyCodes.KEY_ENTER && results.size() == 1) {
                    m_view.hidePopup();
                    m_view.select(results.get(0));
                } else {
                    m_view.setDataList(results);
                    m_view.showPopup();
                    m_view.clearSelection();
                }
            }
        }
    }

    @Override
    public void onKscReportSelected() {
        GWT.log("selected report " + m_view.getSelectedReport().getId());
        m_view.hidePopup();
    }

    @Override
    public void onAddButtonClicked() {
        final String graphTitle    = m_view.getTitle();
        final int kscReportId      = m_view.getSelectedReport().getId();
        final String kscReportName = m_view.getSelectedReport().getLabel();

        final String resourceId = m_graphInfo.getResourceId();
        final String graphName  = m_graphInfo.getReportName();
        final String timeSpan   = m_graphInfo.getTimespan();

        final RequestCallback callback = new RequestCallback() {
            @Override
            public void onResponseReceived(final Request request, final Response response) {
                GWT.log("got response: " + response.getText() + " (" + response.getStatusCode() + ")");
            }
            @Override
            public void onError(final Request request, final Throwable t) {
                GWT.log("got error: " + t.getLocalizedMessage());
            }
        };

        GWT.log("adding resource '" + resourceId + "' from graph report '" + graphName + "' to KSC report '" + kscReportName + "' (" + kscReportId + ") with title '" + graphTitle + "' and timespan '" + timeSpan + "'");
        m_reportService.addGraphToReport(callback, kscReportId, graphTitle, graphName, resourceId, timeSpan);
        m_mainPopup.hide();
    }

    @Override
    public void go(final HasWidgets container) {
        m_addImage.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(final ClickEvent event) {
                GWT.log("clicked (showing = " + m_mainPopup.isShowing() + ", visible = " + m_mainPopup.isVisible() + ")");
                if (m_mainPopup.isShowing()) {
                    m_mainPopup.hide();
                    m_mainPopup.getElement().getStyle().setDisplay(Display.NONE);
                } else {
                    m_mainPopup.getElement().getStyle().setDisplay(Display.BLOCK);
                    m_mainPopup.showRelativeTo(m_addImage);
                }
            }
        });
        m_mainPopup.setVisible(false);
        m_mainPopup.getElement().getStyle().setDisplay(Display.NONE);
        m_mainPopup.addAutoHidePartner(m_addImage.getElement());

        Window.addResizeHandler(new ResizeHandler() {
            @Override
            public void onResize(final ResizeEvent event) {
                final int[] positions = calculateMainPopupPosition();
                m_mainPopup.setPopupPosition(positions[0], positions[1]);
            }
        });

        container.clear();
        container.add(m_addImage);
        container.add(m_mainPopup.asWidget());
    }
    
    public final native String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

    private int[] calculateMainPopupPosition() {
        final int[] positions = {0, 0};

        final int windowWidth = Window.getClientWidth();
        final int imageRightEdge = m_addImage.getAbsoluteLeft() + m_addImage.getWidth();

        if (imageRightEdge + 300 > windowWidth) {
            positions[0] = windowWidth - 320;
        } else {
            positions[0] = imageRightEdge - 3;
        }
        if (positions[0] < 0) positions[0] = 0;

        positions[1] = m_addImage.getAbsoluteTop() + m_addImage.getHeight() - 1;

        return positions;
    }

}
