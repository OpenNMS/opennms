/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum Status implements Serializable, IsSerializable {
	UP,
	MARGINAL,
	DOWN,
	UNKNOWN,
	UNINITIALIZED;

	private String m_reason = "";

	public String getReason() {
		return m_reason;
	}
	
	public void setReason(String reason) {
		m_reason = reason;
	}

	public String getColor() {
		String color;
		if (this.equals(Status.UP)){
			color = "#00ff00";
		} else if (this.equals(Status.MARGINAL)) {
			color = "#ffff00";
		} else if (this.equals(Status.DOWN)) {
			color = "#ff0000";
		} else if (this.equals(Status.UNKNOWN)) {
			color = "#0000ff";
		} else {
			color = "#dddddd";
		}
		return color;
	}
	
	public String getStyle() {
		String cssClass;
		if (this.equals(Status.UP)) {
			cssClass = "statusUp";
		} else if (this.equals(Status.MARGINAL)) {
			cssClass = "statusMarginal";
		} else if (this.equals(Status.DOWN)) {
			cssClass = "statusDown";
		} else if (this.equals(Status.UNKNOWN)){
			cssClass = "statusUnknown";
		} else {
			cssClass = "statusUninitialized";
		}
		return cssClass;
	}

	public static Status up(final String reason) {
		final Status status = Status.UP;
		status.setReason(reason);
		return status;
	}

	public static Status marginal(final String reason) {
		final Status status = Status.MARGINAL;
		status.setReason(reason);
		return status;
	}
	
	public static Status down(final String reason) {
		final Status status = Status.DOWN;
		status.setReason(reason);
		return status;
	}

	public static Status unknown(final String reason) {
		final Status status = Status.UNKNOWN;
		status.setReason(reason);
		return status;
	}
}