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

import java.util.List;

import com.google.gwt.user.client.ui.Widget;

public interface CustomReportSelectListView<T> {
    
    public interface Presenter<T>{
        void onSubmitButtonClick();
        void onClearSelectionButtonClick();
        void onSearchButtonClick();
    }
    
    ResourceListItem getSelectedReport();
    void setDataList(List<ResourceListItem> dataList);
    void setPresenter(Presenter<T> presenter);
    Widget asWidget();
    void clearAllSelections();
    void showWarning();
    List<ResourceListItem> getDataList();
    Widget searchPopupTarget();
    List<ResourceListItem> getAllReports();
}
