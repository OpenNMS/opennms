package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>GWTPollResult class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTPollResult implements Serializable, IsSerializable, Comparable<GWTPollResult> {
	private static final long serialVersionUID = 1L;

	private String m_status;
	private Date m_timestamp;
	private String m_reason;
	private Double m_responseTime;

	/**
	 * <p>Constructor for GWTPollResult.</p>
	 */
	public GWTPollResult() {}

	/**
	 * <p>Constructor for GWTPollResult.</p>
	 *
	 * @param status a {@link java.lang.String} object.
	 * @param timestamp a {@link java.util.Date} object.
	 * @param reason a {@link java.lang.String} object.
	 * @param responseTime a {@link java.lang.Double} object.
	 */
	public GWTPollResult(final String status, final Date timestamp, final String reason, final Double responseTime) {
		m_status = status;
		m_timestamp = timestamp;
		m_reason = reason;
		m_responseTime = responseTime;
	}

	/**
	 * <p>getReason</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getReason() {
		return m_reason;
	}
	/**
	 * <p>setReason</p>
	 *
	 * @param reason a {@link java.lang.String} object.
	 */
	public void setReason(final String reason) {
		m_reason = reason;
	}

	/**
	 * <p>getResponseTime</p>
	 *
	 * @return a {@link java.lang.Double} object.
	 */
	public Double getResponseTime() {
		return m_responseTime;
	}
	/**
	 * <p>setResponseTime</p>
	 *
	 * @param responseTime a {@link java.lang.Double} object.
	 */
	public void setResponseTime(final Double responseTime) {
		m_responseTime = responseTime;
	}

	/**
	 * <p>getStatus</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStatus() {
		return m_status;
	}
	/**
	 * <p>setStatus</p>
	 *
	 * @param status a {@link java.lang.String} object.
	 */
	public void setStatus(final String status) {
		m_status = status;
	}

	/**
	 * <p>getTimestamp</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	public Date getTimestamp() {
		return m_timestamp;
	}
	/**
	 * <p>setTimestamp</p>
	 *
	 * @param timestamp a {@link java.util.Date} object.
	 */
	public void setTimestamp(final Date timestamp) {
		m_timestamp = timestamp;
	}
	
	/**
	 * <p>isDown</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDown() {
		return m_status.equalsIgnoreCase("down");
	}

	/**
	 * <p>available</p>
	 *
	 * @param responseTime a int.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public static GWTPollResult available(final int responseTime) {
		final GWTPollResult result = new GWTPollResult();
		result.setResponseTime((double)responseTime);
		result.setStatus("Up");
		result.setTimestamp(new Date());
		return result;
	}

	/**
	 * <p>down</p>
	 *
	 * @param reason a {@link java.lang.String} object.
	 * @return a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 */
	public static GWTPollResult down(final String reason) {
		final GWTPollResult result = new GWTPollResult();
		result.setStatus("Down");
		result.setReason(reason);
		result.setTimestamp(new Date());
		return result;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "GWTPollResult[status=" + m_status + ",timestamp=" + m_timestamp + ",responseTime=" + m_responseTime + ",reason=" + m_reason + "]";
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param that a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 * @return a int.
	 */
	public int compareTo(GWTPollResult that) {
		return new CompareToBuilder()
			.append(this.getTimestamp(), that.getTimestamp())
			.append(this.getStatus(), that.getStatus())
			.append(this.getResponseTime(), that.getResponseTime())
			.append(this.getReason(), that.getReason())
			.toComparison();
	}
}
