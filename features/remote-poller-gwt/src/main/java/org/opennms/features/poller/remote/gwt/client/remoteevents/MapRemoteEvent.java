package org.opennms.features.poller.remote.gwt.client.remoteevents;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter;

import com.google.gwt.user.client.rpc.IsSerializable;

import de.novanic.eventservice.client.event.Event;

/**
 * <p>MapRemoteEvent interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface MapRemoteEvent extends Event, Serializable, IsSerializable {
	/**
	 * <p>dispatch</p>
	 *
	 * @param locationManager a {@link org.opennms.features.poller.remote.gwt.client.RemotePollerPresenter} object.
	 */
	public void dispatch(final RemotePollerPresenter locationManager);
}
