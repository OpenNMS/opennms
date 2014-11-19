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

import org.opennms.features.gwt.graph.resource.list.client.presenter.KscCustomReportListPresenter.SelectionDisplay;

import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

public class KscCustomSelectionView implements SelectionDisplay {
    
    public static final String VIEW = "view";
    public static final String CUSTOMIZE = "customize";
    public static final String CREATE_NEW = "createNew";
    public static final String CREATE_NEW_FROM_EXISTING = "createNewExisting";
    public static final String DELETE = "delete";
    VerticalPanel m_vertPanel;
    Button m_submitButton;
    RadioButton m_viewRB;
    RadioButton m_customizeRB;
    RadioButton m_createNewRB;
    RadioButton m_createNewExistingRB;
    RadioButton m_deleteRB;
    
    public KscCustomSelectionView(boolean isReadOnly) {
        m_vertPanel = new VerticalPanel();
        m_vertPanel.setStyleName("onms-table-no-borders-margin");
        m_submitButton = new Button("Submit");
        m_viewRB = new RadioButton("group1", "View");
        m_customizeRB = new RadioButton("group1","Customize");
        m_createNewRB = new RadioButton("group1","Create New");
        m_createNewExistingRB = new RadioButton("group1","Create New from Existing");
        m_deleteRB = new RadioButton("group1","Delete");
        
        m_vertPanel.add(m_viewRB);
        m_viewRB.setValue(true);
        if(!isReadOnly){
            m_vertPanel.add(m_customizeRB);
            m_vertPanel.add(m_createNewRB);
            m_vertPanel.add(m_createNewExistingRB);
            m_vertPanel.add(m_deleteRB);
        }

        m_vertPanel.add(m_submitButton);
    }
    
    @Override
    public HasClickHandlers getSubmitButton() {
        return m_submitButton;
    }

    @Override
    public String getSelectAction() {
        if(m_viewRB.getValue()) {
            return VIEW;
        }else if(m_customizeRB.getValue()) {
            return CUSTOMIZE;
        }else if(m_createNewRB.getValue()) {
            return CREATE_NEW;
        }else if(m_createNewExistingRB.getValue()) {
            return CREATE_NEW_FROM_EXISTING;
        }else if(m_deleteRB.getValue()) {
            return DELETE;
        }
        return null;
    }

    @Override
    public Widget asWidget() {
        return m_vertPanel.asWidget();
    }

}
