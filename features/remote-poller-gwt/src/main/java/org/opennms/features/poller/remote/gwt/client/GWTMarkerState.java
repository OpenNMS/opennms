package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;


public class GWTMarkerState implements IsSerializable, Serializable {
	private static final long serialVersionUID = 1L;

	private Status m_status;
    private String m_name;
    private GWTLatLng m_latLng;
    private Boolean m_selected = true;
	private Boolean m_visible = true;

	public GWTMarkerState() {}

    public GWTMarkerState(final String name, final GWTLatLng latLng, final Status status) {
    	setName(name);
    	setLatLng(latLng);
    	setStatus(status);
	}

    public String getName() {
        return m_name;
    }

    private void setName(String name) {
        m_name = name;
    }

    public GWTLatLng getLatLng() {
        return m_latLng;
    }

    private void setLatLng(GWTLatLng latLng) {
        m_latLng = latLng;
    }

    public Status getStatus() {
        return m_status;
    }

	public void setStatus(Status status) {
        m_status = status;
    }

	public boolean isVisible() {
		return m_visible;
	}

	public void setVisible(final boolean visible) {
		m_visible  = visible;
	}

	public boolean isSelected() {
		return m_selected;
	}

	public void setSelected(final boolean selected) {
		m_selected = selected;
	}

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
	
	public String toString() {
		return "GWTMarkerState[name=" + m_name
			+ ",latLng=" + m_latLng
			+ ",status=" + m_status
			+ ",visible=" + m_visible
			+ ",selected=" + m_selected
			+ "]";
	}
}
