/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2011-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.netmgt.model.OnmsArpInterface.StatusType;

@XmlRootElement(name = "ipRouteInterface")
@Entity
@Table(name="ipRouteInterface", uniqueConstraints = {@UniqueConstraint(columnNames={"nodeId", "routeDest"})})
public class OnmsIpRouteInterface {

    private Integer m_id;
    private OnmsNode m_node;
    private String m_routeDest;
    private String m_routeMask;
    private String m_routeNextHop;
    private Integer m_routeIfIndex;
    private Integer m_routeMetric1;
    private Integer m_routeMetric2;
    private Integer m_routeMetric3;
    private Integer m_routeMetric4;
    private Integer m_routeMetric5;
    private RouteType m_routeType;
    private Integer m_routeProto;
    private StatusType m_status = StatusType.UNKNOWN;
    private Date m_lastPollTime;

    @Embeddable
    public static class RouteType implements Comparable<RouteType>, Serializable {
        private static final long serialVersionUID = -4784344871599250528L;
        
        public static final int ROUTE_TYPE_OTHER = 1;
        public static final int ROUTE_TYPE_INVALID = 2;
        public static final int ROUTE_TYPE_DIRECT = 3;
        public static final int ROUTE_TYPE_INDIRECT = 4;

        private static final Integer[] s_order = {ROUTE_TYPE_OTHER, ROUTE_TYPE_INVALID, ROUTE_TYPE_DIRECT, ROUTE_TYPE_INDIRECT};

        private Integer m_routeType;

        private static final Map<Integer, String> routeTypeMap = new HashMap<Integer, String>();
        
        static {
            routeTypeMap.put(ROUTE_TYPE_OTHER, "Other" );
            routeTypeMap.put(ROUTE_TYPE_INVALID, "Invalid" );
            routeTypeMap.put(ROUTE_TYPE_DIRECT, "Direct" );
            routeTypeMap.put(ROUTE_TYPE_INDIRECT, "Indirect" );
        }
        @SuppressWarnings("unused")
        private RouteType() {
        }

        public RouteType(Integer routeType) {
            m_routeType = routeType;
        }

        @Column(name="routeType")
        public Integer getIntCode() {
            return m_routeType;
        }

        public void setIntCode(Integer routeType) {
            m_routeType = routeType;
        }

        public int compareTo(RouteType o) {
            return getIndex(m_routeType) - getIndex(o.m_routeType);
        }

        private static int getIndex(Integer code) {
            for (int i = 0; i < s_order.length; i++) {
                if (s_order[i] == code) {
                    return i;
                }
            }
            throw new IllegalArgumentException("illegal routeType code '"+code+"'");
        }

        public boolean equals(Object o) {
            if (o instanceof RouteType) {
                return m_routeType.intValue() == ((RouteType)o).m_routeType.intValue();
            }
            return false;
        }

        public int hashCode() {
            return toString().hashCode();
        }

        public String toString() {
            return String.valueOf(m_routeType);
        }

        public static RouteType get(Integer code) {
            if (code == null)
                return null;
            switch (code) {
            case ROUTE_TYPE_OTHER: return OTHER;
            case ROUTE_TYPE_INVALID: return INVALID;
            case ROUTE_TYPE_DIRECT: return DIRECT;
            case ROUTE_TYPE_INDIRECT: return INDIRECT;
            default:
                throw new IllegalArgumentException("Cannot create routeType from code "+code);
            }
        }

        /**
         * <p>getRouteTypeString</p>
         *
         * @return a {@link java.lang.String} object.
         */
        /**
         */
        public static String getRouteTypeString(Integer code) {
            if (routeTypeMap.containsKey(code))
                    return routeTypeMap.get( code);
            return null;
        }
        
        public static RouteType OTHER = new RouteType(ROUTE_TYPE_OTHER);
        public static RouteType INVALID = new RouteType(ROUTE_TYPE_INVALID);
        public static RouteType DIRECT = new RouteType(ROUTE_TYPE_DIRECT);
        public static RouteType INDIRECT = new RouteType(ROUTE_TYPE_INDIRECT);

    }
   @Id
    @Column(nullable=false)
    @XmlTransient
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    @XmlID
    @XmlAttribute(name="id")
    @Transient
    public String getInterfaceId() {
        return getId().toString();
    }

    public void setId(final Integer id) {
        m_id = id;
    }
    @ManyToOne(optional=false, fetch=FetchType.LAZY)
    @JoinColumn(name="nodeId")
    @XmlElement(name="nodeId")
    @XmlIDREF
    public OnmsNode getNode() {
        return m_node;
    }

    public void setNode(final OnmsNode node) {
        m_node = node;
    }

    @XmlElement
    @Column(nullable=false, length=16)
	public String getRouteDest() {
		return m_routeDest;
	}

	public void setRouteDest(final String routeDest) {
		m_routeDest = routeDest;
	}

    @XmlElement
    @Column(nullable=false, length=16)
	public String getRouteMask() {
		return m_routeMask;
	}

	public void setRouteMask(final String routeMask) {
		m_routeMask = routeMask;
	}

    @XmlElement
    @Column(nullable=false, length=16)
	public String getRouteNextHop() {
		return m_routeNextHop;
	}

	public void setRouteNextHop(final String routeNextHop) {
		m_routeNextHop = routeNextHop;
	}

    @XmlElement
    @Column(nullable=false)
	public Integer getRouteIfIndex() {
		return m_routeIfIndex;
	}

	public void setRouteIfIndex(final Integer routeIfIndex) {
		m_routeIfIndex = routeIfIndex;
	}

    @XmlElement
    @Column
	public Integer getRouteMetric1() {
		return m_routeMetric1;
	}

	public void setRouteMetric1(final Integer routeMetric1) {
		m_routeMetric1 = routeMetric1;
	}

    @XmlElement
    @Column
	public Integer getRouteMetric2() {
		return m_routeMetric2;
	}

	public void setRouteMetric2(final Integer routeMetric2) {
		m_routeMetric2 = routeMetric2;
	}

    @XmlElement
    @Column
	public Integer getRouteMetric3() {
		return m_routeMetric3;
	}

	public void setRouteMetric3(final Integer routeMetric3) {
		m_routeMetric3 = routeMetric3;
	}

    @XmlElement
    @Column
	public Integer getRouteMetric4() {
		return m_routeMetric4;
	}

	public void setRouteMetric4(final Integer routeMetric4) {
		m_routeMetric4 = routeMetric4;
	}

    @XmlElement
    @Column
	public Integer getRouteMetric5() {
		return m_routeMetric5;
	}

	public void setRouteMetric5(final Integer routeMetric5) {
		m_routeMetric5 = routeMetric5;
	}

    @XmlElement
    @Column
	public RouteType getRouteType() {
		return m_routeType;
	}

	public void setRouteType(final RouteType routeType) {
		m_routeType = routeType;
	}

    @XmlElement
    @Column
	public Integer getRouteProto() {
		return m_routeProto;
	}

	public void setRouteProto(final Integer routeProto) {
		m_routeProto = routeProto;
	}

    @XmlElement
    @Column(nullable=false)
	public StatusType getStatus() {
		return m_status;
	}

	public void setStatus(final StatusType status) {
		m_status = status;
	}

    @Temporal(TemporalType.TIMESTAMP)
    @Column(nullable=false)
    @XmlElement
	public Date getLastPollTime() {
		return m_lastPollTime;
	}

	public void setLastPollTime(final Date lastPollTime) {
		m_lastPollTime = lastPollTime;
	}

	public String toString() {
		    return new ToStringBuilder(this)
		        .append("id", m_id)
		        .append("node", m_node)
		        .append("routedest", m_routeDest)
		        .append("routemask", m_routeMask)
		        .append("routenexthop", m_routeNextHop)
		        .append("routeifindex", m_routeIfIndex)
		        .append("routetype", RouteType.getRouteTypeString(m_routeType.getIntCode()))
		        .append("routeprotocol", m_routeProto)
		        .append("routemetric1", m_routeMetric1)
		        .toString();
	}

}
