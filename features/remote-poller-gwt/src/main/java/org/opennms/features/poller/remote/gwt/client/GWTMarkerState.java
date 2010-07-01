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
	private static final long serialVersionUID = 1L;

	private Status m_status;
    private String m_name;
    private GWTLatLng m_latLng;
    private Boolean m_selected = true;
	private Boolean m_visible = true;

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
    
    public boolean equals(final Object o) {
        if (o != null && o instanceof GWTMarkerState) {
            final GWTMarkerState that = (GWTMarkerState)o;
            return this.getName() == that.getName();
        }
        return false;
    }
}
