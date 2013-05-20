/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2010-2012 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2012 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

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

	private static final long serialVersionUID = 5776838083982296014L;

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
        @Override
	public String toString() {
		return "GWTPollResult[status=" + m_status + ",timestamp=" + m_timestamp + ",responseTime=" + m_responseTime + ",reason=" + m_reason + "]";
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param that a {@link org.opennms.features.poller.remote.gwt.client.GWTPollResult} object.
	 * @return a int.
	 */
        @Override
	public int compareTo(GWTPollResult that) {
		return new CompareToBuilder()
			.append(this.getTimestamp(), that.getTimestamp())
			.append(this.getStatus(), that.getStatus())
			.append(this.getResponseTime(), that.getResponseTime())
			.append(this.getReason(), that.getReason())
			.toComparison();
	}
}
