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

package org.opennms.features.gwt.ksc.combobox.client.presenter;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.ksc.combobox.client.view.KscComboboxView;
import org.opennms.features.gwt.ksc.combobox.client.view.KscComboboxViewImpl;
import org.opennms.features.gwt.ksc.combobox.client.view.KscReportDetail;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.Window.Location;
import com.google.gwt.user.client.ui.HasWidgets;

public class KscComboboxPresenter implements Presenter, KscComboboxView.Presenter<KscReportDetail> {

    private KscComboboxView<KscReportDetail> m_view;
    private List<KscReportDetail> m_kscReportDetails;

    
    public KscComboboxPresenter(KscComboboxViewImpl view, JsArray<KscReportDetail> kscReportDetails) {
        m_view = view;
        m_view.setPresenter(this);
        m_kscReportDetails = convertJsArrayToList(kscReportDetails);
    }

    @Override
    public void onSearchButtonClicked() {
        m_view.setDataList(filterResultsByName(m_view.getSearchText()));
    }

    private List<KscReportDetail> filterResultsByName(String searchText) {
        List<KscReportDetail> list = new ArrayList<KscReportDetail>();
        for(KscReportDetail detail : m_kscReportDetails) {
            if(detail.getLabel().toLowerCase().contains(searchText.toLowerCase())) {
                list.add(detail);
            }
        }
        
        return list;
    }

    @Override
    public void onEnterKeyEvent() {
        m_view.setDataList(filterResultsByName(m_view.getSearchText()));
    }

    @Override
    public void onKscReportSelected() {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(getBaseHref() + "KSC/customView.htm");
        urlBuilder.append("?type=custom");
        urlBuilder.append("&report=" + m_view.getSelectedReport().getId());
        Location.assign(urlBuilder.toString());
    }

    @Override
    public void go(HasWidgets container) {
        container.clear();
        container.add(m_view.asWidget());
    }
    
    private List<KscReportDetail> convertJsArrayToList(JsArray<KscReportDetail> kscReportDetails) {
        List<KscReportDetail> m_list = new ArrayList<KscReportDetail>();
        if (kscReportDetails != null) {
            for(int i = 0; i < kscReportDetails.length(); i++) {
                m_list.add(kscReportDetails.get(i));
            }
        }
        return m_list;
    }
    
    public final native String getBaseHref() /*-{
        try{
            return $wnd.getBaseHref();
        }catch(err){
            return "";
        }
    }-*/;

}
