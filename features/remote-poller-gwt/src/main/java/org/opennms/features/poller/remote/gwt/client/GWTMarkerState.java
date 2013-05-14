/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
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

package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


/**
 * <p>GWTMarkerState class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTMarkerState implements IsSerializable, Serializable {

	private static final long serialVersionUID = 2603404660905704519L;

	private Status m_status;
    private String m_name;
    private GWTLatLng m_latLng;
    private boolean m_selected = true;
	private boolean m_visible = true;
	private boolean m_dirty = true;

	/**
	 * <p>Constructor for GWTMarkerState.</p>
	 */
	public GWTMarkerState() {}

    /**
     * <p>Constructor for GWTMarkerState.</p>
     *
     * @param name a {@link java.lang.String} object.
     * @param latLng a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     * @param status a {@link org.opennms.features.poller.remote.gwt.client.Status} object.
     */
    public GWTMarkerState(final String name, final GWTLatLng latLng, final Status status) {
    	setName(name);
    	setLatLng(latLng);
    	setStatus(status);
	}

    /**
     * <p>getName</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getName() {
        return m_name;
    }

    private void setName(String name) {
        if(!isEquals(m_name, name)) {
            m_dirty = true;
        }
        m_name = name;
    }

    /**
     * <p>getLatLng</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLatLng} object.
     */
    public GWTLatLng getLatLng() {
        return m_latLng;
    }

    private void setLatLng(GWTLatLng latLng) {
        if(!isEquals(m_latLng, latLng)) {
            m_dirty = true;
        }
        m_latLng = latLng;
    }

    /**
     * <p>getStatus</p>
     *
     * @return a {@link org.opennms.features.poller.remote.gwt.client.Status} object.
     */
    public Status getStatus() {
        return m_status;
    }

	/**
	 * <p>setStatus</p>
	 *
	 * @param status a {@link org.opennms.features.poller.remote.gwt.client.Status} object.
	 */
	public void setStatus(Status status) {
	    if(!isEquals(m_status , status)) {
	        m_dirty = true;
	    }
        m_status = status;
    }

	/**
	 * <p>isVisible</p>
	 *
	 * @return a boolean.
	 */
	public boolean isVisible() {
		return m_visible;
	}

	/**
	 * <p>setVisible</p>
	 *
	 * @param visible a boolean.
	 */
	public void setVisible(final boolean visible) {
	    if(m_visible != visible) {
	        m_dirty = true;
	    }
		m_visible  = visible;
	}

	/**
	 * <p>isSelected</p>
	 *
	 * @return a boolean.
	 */
	public boolean isSelected() {
		return m_selected;
	}

	/**
	 * <p>setSelected</p>
	 *
	 * @param selected a boolean.
	 */
	public void setSelected(final boolean selected) {
	    if(m_selected != selected ) {
	        m_dirty = true;
	    }
		m_selected = selected;
	}

	/**
	 * <p>getImageURL</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getImageURL() {
		final StringBuilder sb = new StringBuilder();
		sb.append("images/");
		if (isSelected()) {
			sb.append("selected");
		} else {
			sb.append("deselected");
		}
		sb.append("-");
		sb.append(getStatus().toString());
		sb.append(".png");
		return sb.toString();
    }
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
        @Override
	public String toString() {
		return "GWTMarkerState[name=" + m_name
			+ ",latLng=" + m_latLng
			+ ",status=" + m_status
			+ ",visible=" + m_visible
			+ ",selected=" + m_selected
			+ "]";
	}

    /**
     * <p>isWithinBounds</p>
     *
     * @param bounds a {@link org.opennms.features.poller.remote.gwt.client.GWTBounds} object.
     * @return a boolean.
     */
    public boolean isWithinBounds(GWTBounds bounds) {
        return bounds.contains(getLatLng());
    }
    
        @Override
    public boolean equals(final Object o) {
        if (o != null && o instanceof GWTMarkerState) {
            final GWTMarkerState that = (GWTMarkerState)o;
            return this.getName() == that.getName();
        }
        return false;
    }

    public void place(ApplicationView view) {
        if(m_dirty) {
            view.placeMarker(this);
            m_dirty = false;
        }
    }
    
    private boolean isEquals(Object a, Object b) {
        return a == null ? b == null : a.equals(b);
    }
}
