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

package org.opennms.features.gwt.ksc.add.client.view;

import java.util.List;

import org.opennms.features.gwt.ksc.add.client.KscReport;

import com.google.gwt.event.dom.client.KeyCodeEvent;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;

public interface KscAddGraphView<T> extends IsWidget {
    
    public interface Presenter<T> {
        void onAddButtonClicked();
        void onKeyCodeEvent(KeyCodeEvent<?> event, String searchText);
        void onKscReportSelected();
    }
    
    String getSearchText();
    void setPresenter(Presenter<T> presenter);
    void setDataList(List<T> dataList);
    @Override
    Widget asWidget();

    String getTitle();
    void setTitle(String defaultTitle);

    KscReport getSelectedReport();
    void select(KscReport report);
    void clearSelection();

    boolean isPopupShowing();
    void hidePopup();
    void showPopup();
}
