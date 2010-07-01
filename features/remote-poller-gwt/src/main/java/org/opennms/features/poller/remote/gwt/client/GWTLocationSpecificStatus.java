package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>GWTLocationSpecificStatus class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTLocationSpecificStatus implements Serializable, IsSerializable {
	private static final long serialVersionUID = 1L;

    private Integer m_id;
    private GWTLocationMonitor m_locationMonitor;
	private GWTPollResult m_pollResult;
	private GWTMonitoredService m_monitoredService;
	
	/**
	 * <p>Constructor for GWTLocationSpecificStatus.</p>
	 */
	public GWTLocationSpecificStatus() {}

    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    public Integer getId() {
		return m_id;
	}
	/**
	 * <p>setId</p>
	 *
	 * @param id a {@link java.lang.Integer} object.
	 */
	public void setId(final Integer id) {
		m_id = id;
	}
	/**
	 * <p>getLocationMonitor</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
	 */
	public GWTLocationMonitor getLocationMonitor() {
		return m_locationMonitor;
	}
	/**
	 * <p>setLocationMonitor</p>
	 *
	 * @param locationMonitor a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
	 */
	public void setLocationMonitor(final GWTLocationMonitor locationMonitor) {
		m_locationMonitor = locationMonitor;
	}
	/**
	 * <p>getMonitoredService</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
	 */
	public GWTMonitoredService getMonitoredService() {
		return m_monitoredService;
	}
	/**
	 * <p>setMonitoredService</p>
	 *
	 * @param monitoredService a {@link org.opennms.features.poller.remote.gwt.client.GWTMonitoredService} object.
	 */
	public void setMonitoredService(final GWTMonitoredService monitoredService) {
		m_monitoredService = monitoredService;
	}
	/**
	 * <p>getPollResult</p>
	 *
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public GWTPollResult getPollResult() {
		return m_pollResult;
	}
	/**
	 * <p>setPollResult</p>
	 *
	 * @param pollResult a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public void setPollResult(final GWTPollResult pollResult) {
		m_pollResult = pollResult;
	}
	/**
	 * <p>getPollTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	public Date getPollTime() {
		return m_pollResult == null? null : m_pollResult.getTimestamp();
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "GWTLocationSpecificStatus[id=" + m_id + ",locationMonitor=" + m_locationMonitor + ",monitoredService=" + m_monitoredService + ",pollResult=" + m_pollResult + "]";
	}
}
