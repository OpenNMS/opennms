package org.opennms.features.poller.remote.gwt.client.remoteevents;


/**
 * <p>GeocodingFinishedRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GeocodingFinishedRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private int m_size = 0;

	/**
	 * <p>Constructor for GeocodingFinishedRemoteEvent.</p>
	 */
	public GeocodingFinishedRemoteEvent() {}

	/**
	 * <p>Constructor for GeocodingFinishedRemoteEvent.</p>
	 *
	 * @param size a int.
	 */
	public GeocodingFinishedRemoteEvent(final int size) {
		m_size = size;
	}

	/** {@inheritDoc} */
	public void dispatch(final MapRemoteEventHandler presenter) {
//		Window.alert("Updated geocoding on " + m_size + " location definitions.");
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return "GeocodingFinishedRemoteEvent[size=" + m_size + "]";
	}
}
