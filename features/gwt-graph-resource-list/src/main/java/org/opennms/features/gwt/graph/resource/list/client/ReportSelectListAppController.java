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

package org.opennms.features.gwt.graph.resource.list.client;

import java.util.ArrayList;
import java.util.List;

import org.opennms.features.gwt.graph.resource.list.client.presenter.Presenter;
import org.opennms.features.gwt.graph.resource.list.client.presenter.ReportSelectListPresenter;
import org.opennms.features.gwt.graph.resource.list.client.view.ReportSelectListViewImpl;
import org.opennms.features.gwt.graph.resource.list.client.view.ResourceListItem;
import org.opennms.features.gwt.graph.resource.list.client.view.SearchPopup;

import com.google.gwt.core.client.JsArray;
import com.google.gwt.user.client.ui.HasWidgets;

public class ReportSelectListAppController implements Presenter {

    
    private List<ResourceListItem> m_resourceList;
    private final String m_targetUrl;
    private String m_baseUrl;

    public ReportSelectListAppController(JsArray<ResourceListItem> resourceListData, String targetUrl, String baseUrl) {
        m_resourceList = convertJsArrayToList(resourceListData);
        m_targetUrl = targetUrl;
        m_baseUrl = baseUrl;
    }

    @Override
    public void go(HasWidgets container) {
        new ReportSelectListPresenter(new ReportSelectListViewImpl(m_resourceList), new SearchPopup(), m_targetUrl, m_baseUrl).go(container);
    }
    
    private List<ResourceListItem> convertJsArrayToList(JsArray<ResourceListItem> resourceList) {
        List<ResourceListItem> data = new ArrayList<ResourceListItem>();
        if (resourceList != null) {
            for(int i = 0; i < resourceList.length(); i++) {
                data.add(resourceList.get(i));
            }
        }
        return data;
    }

}
