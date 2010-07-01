package org.opennms.features.poller.remote.gwt.server;

/**
 * <p>InitializationHandler interface.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public interface InitializationHandler<T> {
	/**
	 * <p>start</p>
	 *
	 * @param size a int.
	 * @param <T> a T object.
	 */
	public void start(final int size);
	/**
	 * <p>handle</p>
	 *
	 * @param item a T object.
	 */
	public void handle(final T item);
	/**
	 * <p>finish</p>
	 */
	public void finish();
}
