/**
 * 
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;

public enum ServiceStatus implements Serializable, IsSerializable {
	UP,
	MARGINAL,
	DOWN,
	UNKNOWN;

	private String m_reason = "";

	public String getReason() {
		return m_reason;
	}
	
	public void setReason(String reason) {
		m_reason = reason;
	}

	public String getColor() {
		String color;
		if (this.equals(ServiceStatus.UP)){
			color = "#00ff00";
		} else if (this.equals(ServiceStatus.MARGINAL)) {
			color = "#ffff00";
		} else if (this.equals(ServiceStatus.DOWN)) {
			color = "#ff0000";
		} else {
			color = "#0000ff";
		}
		return color;
	}
	
	public String getStyle() {
		String cssClass;
		if (this.equals(ServiceStatus.UP)) {
			cssClass = "statusUp";
		} else if (this.equals(ServiceStatus.MARGINAL)) {
			cssClass = "statusMarginal";
		} else if (this.equals(ServiceStatus.DOWN)) {
			cssClass = "statusDown";
		} else {
			cssClass = "statusUnknown";
		}
		return cssClass;
	}

	public static ServiceStatus up(final String reason) {
		final ServiceStatus status = ServiceStatus.UP;
		status.setReason(reason);
		return status;
	}

	public static ServiceStatus marginal(final String reason) {
		final ServiceStatus status = ServiceStatus.MARGINAL;
		status.setReason(reason);
		return status;
	}
	
	public static ServiceStatus down(final String reason) {
		final ServiceStatus status = ServiceStatus.DOWN;
		status.setReason(reason);
		return status;
	}

	public static ServiceStatus unknown(final String reason) {
		final ServiceStatus status = ServiceStatus.UNKNOWN;
		status.setReason(reason);
		return status;
	}
}