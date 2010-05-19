package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

public class GWTPollResult implements Serializable, IsSerializable, Comparable<GWTPollResult> {
	private static final long serialVersionUID = 1L;

	private String m_status;
	private Date m_timestamp;
	private String m_reason;
	private Double m_responseTime;

	public GWTPollResult() {}

	public GWTPollResult(final String status, final Date timestamp, final String reason, final Double responseTime) {
		m_status = status;
		m_timestamp = timestamp;
		m_reason = reason;
		m_responseTime = responseTime;
	}

	public String getReason() {
		return m_reason;
	}
	public void setReason(final String reason) {
		m_reason = reason;
	}

	public Double getResponseTime() {
		return m_responseTime;
	}
	public void setResponseTime(final Double responseTime) {
		m_responseTime = responseTime;
	}

	public String getStatus() {
		return m_status;
	}
	public void setStatus(final String status) {
		m_status = status;
	}

	public Date getTimestamp() {
		return m_timestamp;
	}
	public void setTimestamp(final Date timestamp) {
		m_timestamp = timestamp;
	}
	
	public boolean isDown() {
		return m_status.equalsIgnoreCase("down");
	}

	public static GWTPollResult available(final int responseTime) {
		final GWTPollResult result = new GWTPollResult();
		result.setResponseTime((double)responseTime);
		result.setStatus("Up");
		result.setTimestamp(new Date());
		return result;
	}

	public static GWTPollResult down(final String reason) {
		final GWTPollResult result = new GWTPollResult();
		result.setStatus("Down");
		result.setReason(reason);
		result.setTimestamp(new Date());
		return result;
	}

	public String toString() {
		return "GWTPollResult[status=" + m_status + ",timestamp=" + m_timestamp + ",responseTime=" + m_responseTime + ",reason=" + m_reason + "]";
	}

	public int compareTo(GWTPollResult that) {
		return new CompareToBuilder()
			.append(this.getTimestamp(), that.getTimestamp())
			.append(this.getStatus(), that.getStatus())
			.append(this.getResponseTime(), that.getResponseTime())
			.append(this.getReason(), that.getReason())
			.toComparison();
	}
}
