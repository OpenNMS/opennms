package org.opennms.features.poller.remote.gwt.server;

import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import org.opennms.core.utils.LogUtils;

import de.novanic.eventservice.service.EventExecutorService;
import de.novanic.eventservice.service.EventExecutorServiceFactory;

final class InitializeTask extends TimerTask {
    /** Constant <code>m_updateTaskScheduled</code> */
    public static AtomicBoolean m_updateTaskScheduled = new AtomicBoolean(false);

    private final EventExecutorService m_service;
    final LocationDataManager m_locationDataManager;
    private final Timer m_timer;

    static final int UPDATE_PERIOD = 1000 * 60; // 1 minute

    /**
     * <p>Constructor for InitializeTask.</p>
     *
     * @param service a {@link de.novanic.eventservice.service.EventExecutorService} object.
     * @param locationDataManager a {@link org.opennms.features.poller.remote.gwt.server.LocationDataManager} object.
     * @param timer a {@link java.util.Timer} object.
     */
    public InitializeTask(EventExecutorService service, LocationDataManager locationDataManager, Timer timer) {
        m_service = service;
        m_locationDataManager = locationDataManager;
        m_timer = timer;
    }

    /** {@inheritDoc} */
    @Override
    public void run() {
    	try {
            final Date startDate = new Date();
            m_locationDataManager.doInitialize(m_service);

            startUpdateTaskIfNecessary(startDate);
    	} catch (final Exception e) {
    		LogUtils.warnf(this, e, "An exception occurred pushing initial data.");
    	}
    }

    void startUpdateTaskIfNecessary(final Date lastUpdated) {
        if (! m_updateTaskScheduled.getAndSet(true)) {
            m_timer.schedule(new UpdateTask(EventExecutorServiceFactory.getInstance().getEventExecutorService((String)null), lastUpdated, m_locationDataManager), InitializeTask.UPDATE_PERIOD, InitializeTask.UPDATE_PERIOD);
        }
    }
}
