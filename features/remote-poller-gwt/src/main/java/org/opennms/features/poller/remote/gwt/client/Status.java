
/**
 * <p>Status class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;

import com.google.gwt.user.client.rpc.IsSerializable;
public enum Status implements Serializable, IsSerializable {
    DOWN,
    DISCONNECTED,
    MARGINAL,
	UP,
	STOPPED,
	UNKNOWN;

	/**
	 * <p>getColor</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getColor() {
		String color;
		if (this.equals(Status.UP)){
			color = "#00ff00";
		} else if (this.equals(Status.MARGINAL)) {
			color = "#ffff00";
		} else if (this.equals(Status.DOWN)) {
			color = "#ff0000";
		} else if (this.equals(Status.DISCONNECTED)) {
			color = "#ff8800";
        } else if (this.equals(Status.STOPPED)) {
            color = "#aaaaaa";
		} else {
			color = "#dddddd";
		}
		return color;
	}

	/**
	 * <p>getStyle</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getStyle() {
		String cssClass;
		if (this.equals(Status.UP)) {
			cssClass = "statusUp";
		} else if (this.equals(Status.MARGINAL)) {
			cssClass = "statusMarginal";
		} else if (this.equals(Status.DOWN)) {
			cssClass = "statusDown";
		} else if (this.equals(Status.DISCONNECTED)){
			cssClass = "statusDisconnected";
        } else if (this.equals(Status.STOPPED)){
            cssClass = "statusStopped";
		} else {
			cssClass = "statusUninitialized";
		}
		return cssClass;
	}
}
