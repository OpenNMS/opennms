/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.enlinkd.model;

import java.io.Serializable;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.persistence.Column;
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

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.Type;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="cdpLink")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class CdpLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3428640531131834328L;

	/**
	 * 
	 */

	/*
    CiscoNetworkProtocol ::= TEXTUAL-CONVENTION
                STATUS          current
                DESCRIPTION
                    "Represents the different types of network layer protocols."
                SYNTAX          INTEGER  {
                                    ip(1),
                                    decnet(2),
                                    pup(3),
                                    chaos(4),
                                    xns(5),
                                    x121(6),
                                    appletalk(7),
                                    clns(8),
                                    lat(9),
                                    vines(10),
                                    cons(11),
                                    apollo(12),
                                    stun(13),
                                    novell(14),
                                    qllc(15),
                                    snapshot(16),
                                    atmIlmi(17),
                                    bstun(18),
                                    x25pvc(19),
                                    ipv6(20),
                                    cdm(21),
                                    nbf(22),
                                    bpxIgx(23),
                                    clnsPfx(24),
                                    http(25),
                                    unknown(65535)
                                } */
	public enum CiscoNetworkProtocolType{
		ip(1),
        decnet(2),
        pup(3),
        chaos(4),
        xns(5),
        x121(6),
        appletalk(7),
        clns(8),
        lat(9),
        vines(10),
        cons(11),
        apollo(12),
        stun(13),
        novell(14),
        qllc(15),
        snapshot(16),
        atmIlmi(17),
        bstun(18),
        x25pvc(19),
        ipv6(20),
        cdm(21),
        nbf(22),
        bpxIgx(23),
        clnsPfx(24),
        http(25),
        unknown(65535);
		private final int m_type;
  
		CiscoNetworkProtocolType(Integer chassisIdsubtype) {
	    	m_type = chassisIdsubtype;
	    }

	    protected static final Map<Integer, String> s_typeMap = new HashMap<>();

	    static {
	    	s_typeMap.put(1,"ip");
	    	s_typeMap.put(2,"decnet");
	    	s_typeMap.put(3,"pup");
	    	s_typeMap.put(4,"chaos");
	    	s_typeMap.put(5,"xns");
	    	s_typeMap.put(6,"x121");
	    	s_typeMap.put(7,"appletalk");
	    	s_typeMap.put(8,"clns");
	    	s_typeMap.put(9,"lat");
	    	s_typeMap.put(10,"vines");
	    	s_typeMap.put(11,"cons");
	    	s_typeMap.put(12,"apollo");
	    	s_typeMap.put(13,"stun");
	    	s_typeMap.put(14,"novell");
	    	s_typeMap.put(15,"qllc");
	    	s_typeMap.put(16,"snapshot");
	    	s_typeMap.put(17,"atmIlmi");
	    	s_typeMap.put(18,"bstun");
	    	s_typeMap.put(19,"x25pvc");
	    	s_typeMap.put(20,"ipv6");
	    	s_typeMap.put(21,"cdm");
	    	s_typeMap.put(22,"nbf");
	    	s_typeMap.put(23,"bpxIgx");
	    	s_typeMap.put(24,"clnsPfx");
	    	s_typeMap.put(25,"http");
	    	s_typeMap.put(65535,"unknown");        }

	    /**
	     * <p>ElementIdentifierTypeString</p>
	     *
	     * @return a {@link java.lang.String} object.
	     */
	    public static String getTypeString(Integer code) {
	        if (s_typeMap.containsKey(code))
	                return s_typeMap.get( code);
	        return null;
	    }

        public static CiscoNetworkProtocolType get(Integer code) {
            if (code == null)
                throw new IllegalArgumentException("Cannot create CiscoNetworkProtocolType from null code");
            switch (code) {
            case 1:       return ip;
            case 2:       return decnet;
            case 3:       return pup;
            case 4:       return chaos;
            case 5:       return xns;
            case 6:       return x121;
            case 7:       return appletalk;
            case 8:       return clns;
            case 9:       return lat;
            case 10:      return vines;
            case 11:      return cons;
            case 12:      return apollo;
            case 13:      return stun;
            case 14:      return novell;
            case 15:      return qllc;
            case 16:      return snapshot;
            case 17:      return atmIlmi;
            case 18:      return bstun;
            case 19:      return x25pvc;
            case 20:      return ipv6;
            case 21:      return cdm;
            case 22:      return nbf;
            case 23:      return bpxIgx;
            case 24:      return clnsPfx;
            case 25:      return http;
            case 65535:   return unknown;            default:
                throw new IllegalArgumentException("Cannot create CiscoNetworkProtocolType from code "+code);
            }
        }
        
        public Integer getValue() {
        	return m_type;
        }
        
    }

    private Integer m_id;	
    private OnmsNode m_node;
	
    private Integer m_cdpCacheIfIndex;
    private Integer m_cdpCacheDeviceIndex;
    private String m_cdpInterfaceName;
    
    private CiscoNetworkProtocolType m_cdpCacheAddressType;
    private String m_cdpCacheAddress;
    private String m_cdpCacheVersion;
    private String m_cdpCacheDeviceId;
    private String m_cdpCacheDevicePort;
    private String m_cdpCacheDevicePlatform;
    private Date m_cdpLinkCreateTime = new Date();
    private Date m_cdpLinkLastPollTime;
	
	public CdpLink() {
	}

    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId")
    @GeneratedValue(generator = "opennmsSequence")
    public Integer getId() {
        return m_id;
    }


    public void setId(Integer id) {
        m_id = id;
    }


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nodeId")
    public OnmsNode getNode() {
        return m_node;
    }


    public void setNode(OnmsNode node) {
        m_node = node;
    }

    @Column(name="cdpCacheIfIndex", nullable = false)
    public Integer getCdpCacheIfIndex() {
        return m_cdpCacheIfIndex;
    }

    public void setCdpCacheIfIndex(Integer cdpCacheIfIndex) {
        m_cdpCacheIfIndex = cdpCacheIfIndex;
    }
    
    @Column(name="cdpCacheDeviceIndex", nullable = false)
    public Integer getCdpCacheDeviceIndex() {
        return m_cdpCacheDeviceIndex;
    }

    public void setCdpCacheDeviceIndex(Integer cdpCacheDeviceIndex) {
        m_cdpCacheDeviceIndex = cdpCacheDeviceIndex;
    }

    @Column(name="cdpInterfaceName" , length=96)
    public String getCdpInterfaceName() {
        return m_cdpInterfaceName;
    }

    public void setCdpInterfaceName(String cdpInterfaceName) {
        m_cdpInterfaceName = cdpInterfaceName;
    }

    @Column(name="cdpCacheAddressType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.CiscoNetworkProtocolTypeUserType")
    public CiscoNetworkProtocolType getCdpCacheAddressType() {
        return m_cdpCacheAddressType;
    }

    public void setCdpCacheAddressType(
            CiscoNetworkProtocolType cdpCacheAddressType) {
        m_cdpCacheAddressType = cdpCacheAddressType;
    }

    @Column(name="cdpCacheAddress" , length=64, nullable = false)
    public String getCdpCacheAddress() {
        return m_cdpCacheAddress;
    }

    public void setCdpCacheAddress(String cdpCacheAddress) {
        m_cdpCacheAddress = cdpCacheAddress;
    }

    @Column(name="cdpCacheVersion" , length=256, nullable = false)
    public String getCdpCacheVersion() {
	return m_cdpCacheVersion;
    }

    public void setCdpCacheVersion(String cdpCacheVersion) {
        m_cdpCacheVersion = cdpCacheVersion;
    }

    @Column(name="cdpCacheDeviceId" , length=64, nullable = false)
    public String getCdpCacheDeviceId() {
        return m_cdpCacheDeviceId;
    }

    public void setCdpCacheDeviceId(String cdpCacheDeviceId) {
        m_cdpCacheDeviceId = cdpCacheDeviceId;
    }

    @Column(name="cdpCacheDevicePort" , length=96, nullable = false)
    public String getCdpCacheDevicePort() {
        return m_cdpCacheDevicePort;
    }

    public void setCdpCacheDevicePort(String cdpCacheDevicePort) {
        m_cdpCacheDevicePort = cdpCacheDevicePort;
    }

    @Column(name="cdpCacheDevicePlatform" , length=96, nullable = false)
    public String getCdpCacheDevicePlatform() {
	return m_cdpCacheDevicePlatform;
    }

    public void setCdpCacheDevicePlatform(String cdpCacheDevicePlatform) {
	m_cdpCacheDevicePlatform = cdpCacheDevicePlatform;
    }
	
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cdpLinkCreateTime", nullable=false)
    public Date getCdpLinkCreateTime() {
        return m_cdpLinkCreateTime;
    }

    public void setCdpLinkCreateTime(Date cdpLinkCreateTime) {
        m_cdpLinkCreateTime = cdpLinkCreateTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="cdpLinkLastPollTime", nullable=false)
    public Date getCdpLinkLastPollTime() {
        return m_cdpLinkLastPollTime;
    }

    public void setCdpLinkLastPollTime(Date cdpLinkLastPollTime) {
	m_cdpLinkLastPollTime = cdpLinkLastPollTime;
    }

    public void merge(CdpLink link) {
        if (link == null) return;
        setCdpInterfaceName(link.getCdpInterfaceName());
        setCdpCacheAddressType(link.getCdpCacheAddressType());
        setCdpCacheAddress(link.getCdpCacheAddress());
        setCdpCacheVersion(link.getCdpCacheVersion());
        setCdpCacheDeviceId(link.getCdpCacheDeviceId());
        setCdpCacheDevicePort(link.getCdpCacheDevicePort());
        setCdpCacheDevicePlatform(link.getCdpCacheDevicePlatform());
        setCdpLinkLastPollTime(link.getCdpLinkCreateTime());
    }
	
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {

        return "cdplink: nodeid:[" +
                getNode().getId() +
                "]. ifindex:[ " +
                getCdpCacheIfIndex() +
                "], deviceindex:[" +
                getCdpCacheDeviceIndex() +
                "], interfacename:[" +
                getCdpInterfaceName() +
                "], address/type:[" +
                getCdpCacheAddress() +
                "/" +
                CiscoNetworkProtocolType.getTypeString(getCdpCacheAddressType().getValue()) +
                "], deviceid:[" +
                getCdpCacheDeviceId() +
                "], deviceport:[" +
                getCdpCacheDevicePort() +
                "]";
    }

}
