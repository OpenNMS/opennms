package org.opennms.features.poller.remote.gwt.server;

public interface InitializationHandler<T> {
	public void start(final int size);
	public void handle(final T item);
	public void finish();
}
