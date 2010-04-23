package org.opennms.features.poller.remote.gwt.client;


public class GWTMarkerState {
    
    private Status m_status;
    private String m_name;
    private GWTLatLng m_latLng;
	private boolean m_visible = true;
    

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

	private void setStatus(Status status) {
        m_status = status;
    }

	public boolean isVisible() {
		return m_visible;
	}

	public void setVisible(final boolean visible) {
		m_visible  = visible;
	}

    public String getImageURL() {
        return "images/icon-" + getStatus() + ".png";
    }
}
