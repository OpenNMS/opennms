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

package org.opennms.features.gwt.snmpselect.list.client.view;

import java.util.List;

import org.opennms.features.gwt.snmpselect.list.client.view.handler.SnmpSelectTableCollectUpdateHandler;
import org.opennms.features.gwt.tableresources.client.OnmsSimplePagerResources;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.SimplePager.Resources;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.ListDataProvider;

public class SnmpSelectListViewImpl extends Composite implements SnmpSelectListView<SnmpCellListItem>{

    private static SnmpSelectListViewImplUiBinder uiBinder = GWT
            .create(SnmpSelectListViewImplUiBinder.class);

    interface SnmpSelectListViewImplUiBinder extends
            UiBinder<Widget, SnmpSelectListViewImpl> {
    }

    @UiField
    SnmpSelectTable m_snmpSelectTable;
    
    @UiField
    FlowPanel m_pagerContainer;
    
    private Presenter<SnmpCellListItem> m_presenter;
    private ListDataProvider<SnmpCellListItem> m_dataList;

    protected SnmpCellListItem m_updatedCell;

    public SnmpSelectListViewImpl() {
        initWidget(uiBinder.createAndBindUi(this));

        m_snmpSelectTable.setCollectUpdateHandler(new SnmpSelectTableCollectUpdateHandler() {
            
            @Override
            public void onSnmpInterfaceCollectUpdated(int ifIndex, String oldValue, String newValue) {
                m_presenter.onSnmpInterfaceCollectUpdated(ifIndex, oldValue, newValue);
            }
        });
        
        SimplePager simplePager = new SimplePager(TextLocation.CENTER, (Resources) GWT.create(OnmsSimplePagerResources.class), true, 1000, false);
        simplePager.setWidth("100%");
        simplePager.setDisplay(m_snmpSelectTable);
        m_pagerContainer.add(simplePager);
        
        m_dataList = new ListDataProvider<SnmpCellListItem>();
        m_dataList.addDataDisplay(m_snmpSelectTable);
    }

    @Override
    public void setPresenter(Presenter<SnmpCellListItem> presenter) {
        m_presenter = presenter;
    }

    @Override
    public void setDataList(List<SnmpCellListItem> dataList) {
        m_dataList.setList(dataList);
    }

    @Override
    public SnmpCellListItem getUpdatedCell() {
        return m_updatedCell;
    }

    @Override
    public void showError(String message) {
        Window.alert("Error: " + message);
    }
}
