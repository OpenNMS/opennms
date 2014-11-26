/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2014 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.util.List;

import org.opennms.features.poller.remote.gwt.client.events.LocationPanelSelectEvent;
import org.opennms.features.poller.remote.gwt.client.location.LocationInfo;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.ui.HTMLTable.Cell;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;


/**
 * <p>PageableLocationList class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class PageableLocationList extends PageableList {
    
    private List<? extends LocationInfo> m_locations;

    private class LocationInfoDisplay extends Widget{
        
        Image m_icon = new Image();
        Label m_nameLabel = new Label();
        Label m_areaLabel = new Label();
        Label m_statusLabel = new Label();
        
        @Override
        @SuppressWarnings("deprecation")
        protected void doAttachChildren() {
            super.doAttachChildren();
            DOM.appendChild(this.getElement(), m_icon.getElement());
            DOM.appendChild(this.getElement(), m_nameLabel.getElement());
            DOM.appendChild(this.getElement(), m_areaLabel.getElement());
            DOM.appendChild(this.getElement(), m_statusLabel.getElement());
        }
        
        @Override
        protected void onLoad() {
            resizeToFit();
        }
        
        protected void resizeToFit() {
            int calculatedHeight = m_nameLabel.getOffsetHeight() + m_statusLabel.getOffsetHeight();
            int newHeight = calculatedHeight > 60 ? calculatedHeight : 60;
            setHeight(Integer.toString(newHeight + 2));
        }

        @SuppressWarnings("deprecation")
        public LocationInfoDisplay(final LocationInfo locationInfo) {
            setElement(DOM.createDiv());

            setStyles();

            m_icon.setUrl(locationInfo.getMarkerImageURL());
            m_nameLabel.setText(locationInfo.getName());
            m_areaLabel.setText(locationInfo.getArea());
            m_statusLabel.setText(locationInfo.getStatusDetails().getReason());
        }

        private void setStyles() {
            setStyleName(locationDetailStyle.detailContainerStyle());
            m_icon.addStyleName(locationDetailStyle.iconStyle());
            m_nameLabel.addStyleName(locationDetailStyle.nameStyle());
            m_areaLabel.addStyleName(locationDetailStyle.areaStyle());
            m_statusLabel.addStyleName(locationDetailStyle.statusStyle());
            
        }
        
    }

    /** {@inheritDoc} */
    @Override
    protected Widget getListItemWidget(final int rowIndex) {
        return new LocationInfoDisplay(getLocations().get(rowIndex));
    }

    /**
     * TODO: Maybe enhance this so that it only adds/updates/deletes individual items
     * TODO: Don't skip to the front page on every update
     *
     * @param locations a {@link java.util.ArrayList} object.
     */
    public void updateList(final List<? extends LocationInfo> locations) {
        setLocations(locations);
        setCurrentPageIndex(getCurrentPageIndex());
        //refresh();
    }

    private List<? extends LocationInfo> getLocations() {
        return m_locations;
    }

    private void setLocations(final List<? extends LocationInfo> locations) {
        m_locations = locations;
    }

    /** {@inheritDoc} */
    @Override
    protected int getListSize() {
        if (getLocations() == null) return 0;
        return getLocations().size();
    }

    /** {@inheritDoc} */
    @Override
    public void onItemClickHandler(final ClickEvent event) {
      final Cell cell = getCellForEvent(event);
      LocationInfo location = m_locations.get(cell.getRowIndex() + (getCurrentPageIndex() * getTotalListItemsPerPage()));

      fireEvent(new LocationPanelSelectEvent(location.getName()));
    }

    /**
     * <p>refreshLocationListResize</p>
     */
    public void refreshLocationListResize() {
        for(int i = 0; i < getDataList().getRowCount(); i++) {
            LocationInfoDisplay locInfo = (LocationInfoDisplay) getDataList().getWidget(i, 0);
            locInfo.resizeToFit();
        }
        refresh();
    }

}
