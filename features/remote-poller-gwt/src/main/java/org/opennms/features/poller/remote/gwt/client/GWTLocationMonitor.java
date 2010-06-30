package org.opennms.features.poller.remote.gwt.client;

import java.io.Serializable;
import java.util.Date;

import org.opennms.features.poller.remote.gwt.client.utils.CompareToBuilder;
import org.opennms.features.poller.remote.gwt.client.utils.HashCodeBuilder;

import com.google.gwt.user.client.rpc.IsSerializable;

/**
 * <p>GWTLocationMonitor class.</p>
 *
 * @author ranger
 * @version $Id: $
 * @since 1.8.1
 */
public class GWTLocationMonitor implements Serializable, IsSerializable, Comparable<GWTLocationMonitor> {
	private static final long serialVersionUID = 1L;

	private Integer m_id;
	private String m_status;
	private String m_definitionName;
	private String m_name;
	private Date m_lastCheckInTime;

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
	 * @param string a {@link java.lang.String} object.
	 */
	public void setStatus(final String string) {
		m_status = string;
	}
	/**
	 * <p>getDefinitionName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getDefinitionName() {
		return m_definitionName;
	}
	/**
	 * <p>setDefinitionName</p>
	 *
	 * @param definitionName a {@link java.lang.String} object.
	 */
	public void setDefinitionName(final String definitionName) {
		m_definitionName = definitionName;
	}
	/**
	 * <p>getName</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return m_name;
	}
	/**
	 * <p>setName</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(final String name) {
		m_name = name;
	}
	/**
	 * <p>getLastCheckInTime</p>
	 *
	 * @return a {@link java.util.Date} object.
	 */
	public Date getLastCheckInTime() {
		return m_lastCheckInTime;
	}
	/**
	 * <p>setLastCheckInTime</p>
	 *
	 * @param lastCheckInTime a {@link java.util.Date} object.
	 */
	public void setLastCheckInTime(final Date lastCheckInTime) {
		m_lastCheckInTime = lastCheckInTime;
	}

	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return "GWTLocationMonitor[name=" + m_name + ",status=" + m_status + ",lastCheckInTime=" + m_lastCheckInTime + "]";
	}

	/** {@inheritDoc} */
	public boolean equals(Object o) {
		if (o == null) return false;
		if (!(o instanceof GWTLocationMonitor)) return false;
		GWTLocationMonitor that = (GWTLocationMonitor)o;
		if (this.getId().equals(that.getId())) return true;
		return false;
	}

	/**
	 * <p>hashCode</p>
	 *
	 * @return a int.
	 */
	public int hashCode() {
	    return this.getId();
	}

	/**
	 * <p>compareTo</p>
	 *
	 * @param that a {@link org.opennms.features.poller.remote.gwt.client.GWTLocationMonitor} object.
	 * @return a int.
	 */
	public int compareTo(GWTLocationMonitor that) {
		return new CompareToBuilder()
			.append(this.getDefinitionName(), that.getDefinitionName())
			.append(this.getName(), that.getName())
			.append(this.getStatus(), that.getStatus())
			.append(this.getLastCheckInTime(), that.getLastCheckInTime())
			.toComparison();
	}
}
