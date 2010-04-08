package org.opennms.features.poller.remote.gwt.client;

import com.google.gwt.user.client.rpc.IsSerializable;
import com.googlecode.gwtmapquest.transaction.MQAPoi;

public class MapQuestLocation extends BaseLocation implements IsSerializable {
	private static final long serialVersionUID = 1L;
	private MQAPoi m_marker;

	public MapQuestLocation() {
		super();
	}

	public MapQuestLocation(Location location) {
		super(location.getLocationInfo(), location.getLocationDetails());
	}

	@Override
	public String getImageURL() {
		return m_marker.getIcon().getImageURL();
	}

	public MQAPoi getMarker() {
		return m_marker;
	}

	public void setMarker(MQAPoi marker) {
		m_marker = marker;
	}
	
	@Override
	protected String getAttributeText() {
	    return super.getAttributeText() + ",imageUrl=" + getImageURL() + ",marker=" + getMarker();
	}
	
	public String toString() {
	    return "MapQuestLocation[" + getAttributeText() + "]";
	}
}
