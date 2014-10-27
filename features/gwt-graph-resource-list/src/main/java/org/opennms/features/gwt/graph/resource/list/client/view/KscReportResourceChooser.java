/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.gwt.graph.resource.list.client.view;

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscGraphResourceListPresenter.ViewChoiceDisplay;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KscReportResourceChooser implements ViewChoiceDisplay {
    
    VerticalPanel m_vertPanel;
    private Button m_chooseBtn;
    private Button m_viewBtn;
    
    public KscReportResourceChooser() {
        m_chooseBtn = new Button("Choose Child Resource");
        m_viewBtn = new Button("View Child Resource");
        
        m_vertPanel = new VerticalPanel();
        m_vertPanel.setStyleName("onms-table-no-borders-margin");
        m_vertPanel.add(m_viewBtn);
        m_vertPanel.add(m_chooseBtn);
    }
    
    @Override
    public HasClickHandlers getViewButton() {
        return m_viewBtn;
    }

    @Override
    public HasClickHandlers getChooseButton() {
        return m_chooseBtn;
    }

    @Override
    public Widget asWidget() {
        return m_vertPanel.asWidget();
    }

}
