package org.opennms.features.poller.remote.gwt.client.remoteevents;


/**
 * <p>GeocodingUpdatingRemoteEvent class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GeocodingUpdatingRemoteEvent implements MapRemoteEvent {
	private static final long serialVersionUID = 1L;
	private int m_count = 0;
	private int m_size = 0;

	/**
	 * <p>Constructor for GeocodingUpdatingRemoteEvent.</p>
	 */
	public GeocodingUpdatingRemoteEvent() {}

	/**
	 * <p>Constructor for GeocodingUpdatingRemoteEvent.</p>
	 *
	 * @param count a int.
	 * @param size a int.
	 */
	public GeocodingUpdatingRemoteEvent(final int count, final int size) {
		m_count = count;
		m_size = size;
	}

	/** {@inheritDoc} */
	public void dispatch(final MapRemoteEventHandler presenter) {
//		Window.alert("updating geocoding: " + m_count + "/" + m_size);
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
	    return "GeocodingUpdatingRemoteEvent[count=" + m_count + ",size=" + m_size + "]";
	}
}
