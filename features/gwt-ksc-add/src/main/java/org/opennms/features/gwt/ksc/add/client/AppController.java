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

package org.opennms.features.gwt.ksc.add.client;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.presenter.KscAddGraphPresenter;
import org.opennms.features.gwt.ksc.add.client.presenter.Presenter;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphView;
import org.opennms.features.gwt.ksc.add.client.view.KscAddGraphViewImpl;

import com.google.gwt.user.client.ui.HasWidgets;
import com.google.gwt.user.client.ui.PopupPanel;

public class AppController implements Presenter {
    private List<KscReport> m_reports;
    private PopupPanel m_popupPanel;
    private KscAddGraphView<KscReport> m_addGraphView;
    private GraphInfo m_graphInfo;

    public AppController(final List<KscReport> kscReports, final GraphInfo graphInfo) {
        m_reports = kscReports;
        m_graphInfo = graphInfo;
    }
    
    @Override
    public void go(final HasWidgets container) {
        if (m_addGraphView == null) {
            m_addGraphView = new KscAddGraphViewImpl();
            m_addGraphView.setTitle(m_graphInfo.getTitle() == null? "" : m_graphInfo.getTitle());
        }

        if (m_popupPanel == null) {
            m_popupPanel = new PopupPanel();
            m_popupPanel.setWidth("300px");
            m_popupPanel.setHeight("79px");
            m_popupPanel.add(m_addGraphView);
            m_popupPanel.setAutoHideEnabled(true);
            m_popupPanel.setAnimationEnabled(false);
            m_popupPanel.setModal(false);
            m_popupPanel.setVisible(false);
            m_popupPanel.hide();
        }

        new KscAddGraphPresenter(m_popupPanel, m_addGraphView, m_reports, m_graphInfo).go(container);
    }

}
