package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

public class StatusDetails implements Serializable, IsSerializable, Comparable<StatusDetails> {
	private static final long serialVersionUID = 1L;
	private Status m_status;
	private String m_reason;

	public StatusDetails() {
	}

	public StatusDetails(final Status status, final String reason) {
		m_status = status;
		m_reason = reason;
	}

	public Status getStatus() {
		if (m_status == null) {
			m_status = Status.UNINITIALIZED;
		}
		return m_status;
	}

	public String getReason() {
		return m_reason;
	}

	public String toString() {
		return "StatusDetails[status=" + m_status + ",reason=" + m_reason + "]";
	}

	public int compareTo(final StatusDetails that) {
		return new CompareToBuilder()
			.append(this.getStatus(), that.getStatus())
			.append(this.getReason(), that.getReason())
			.toComparison();
	}

	public static StatusDetails up() {
		return new StatusDetails(Status.UP, null);
	}

	public static StatusDetails marginal(final String reason) {
		return new StatusDetails(Status.MARGINAL, reason);
	}

	public static StatusDetails down(final String reason) {
		return new StatusDetails(Status.DOWN, reason);
	}

	public static StatusDetails unknown(final String reason) {
		return new StatusDetails(Status.UNKNOWN, reason);
	}

	public static StatusDetails uninitialized() {
		return new StatusDetails(Status.UNINITIALIZED, null);
	}
}
