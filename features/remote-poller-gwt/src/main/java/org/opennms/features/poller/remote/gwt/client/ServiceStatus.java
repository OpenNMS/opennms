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
}