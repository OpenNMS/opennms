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
import org.opennms.core.utils.LldpUtils.LldpPortIdSubType;
import org.opennms.core.utils.LldpUtils.LldpChassisIdSubType;
import org.opennms.netmgt.model.FilterManager;
import org.opennms.netmgt.model.OnmsNode;

@Entity
@Table(name="lldpLink")
@Filter(name=FilterManager.AUTH_FILTER_NAME, condition="exists (select distinct x.nodeid from node x join category_node cn on x.nodeid = cn.nodeid join category_group cg on cn.categoryId = cg.categoryId where x.nodeid = nodeid and cg.groupId in (:userGroups))")
public class LldpLink implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3813247749765614567L;



    private Integer m_id;	
	private OnmsNode m_node;
	
	private LldpPortIdSubType m_lldpPortIdSubType;
	private Integer m_lldpRemLocalPortNum;
	private Integer m_lldpRemIndex;
	private Integer m_lldpPortIfindex;
	private String m_lldpPortId;
	private String m_lldpPortDescr;
    
	private String m_lldpRemChassisId;
	private String m_lldpRemSysname;
        private org.opennms.core.utils.LldpUtils.LldpChassisIdSubType m_lldpRemChassisIdSubType;
	private LldpPortIdSubType m_lldpRemPortIdSubType;
	private String m_lldpRemPortId;
	private String m_lldpRemPortDescr;
    private Date m_lldpLinkCreateTime = new Date();
    private Date m_lldpLinkLastPollTime;
	
	public LldpLink() {
	}

    public LldpLink(OnmsNode node, Integer lldpRemIndex, Integer lldpRemLocalPortNum, Integer portIfIndex, String portId,
                    String portDescr, LldpPortIdSubType portIdSubType, String remChassisId, String remSysname, LldpChassisIdSubType remChassisIdSubType,
                    String remPortId, LldpPortIdSubType remPortIdSubType, String remPortDescr) {
        setNode(node);
        setLldpRemIndex(lldpRemIndex);
		setLldpRemLocalPortNum(lldpRemLocalPortNum);
        setLldpPortIfindex(portIfIndex);
        setLldpPortId(portId);
        setLldpPortDescr(portDescr);
        setLldpPortIdSubType(portIdSubType);
        setLldpRemChassisId(remChassisId);
        setLldpRemSysname(remSysname);
        setLldpRemChassisIdSubType(remChassisIdSubType);
        setLldpRemPortId(remPortId);
        setLldpRemPortIdSubType(remPortIdSubType);
        setLldpRemPortDescr(remPortDescr);
    }


    @Id
    @Column(nullable = false)
    @SequenceGenerator(name = "opennmsSequence", sequenceName = "opennmsNxtId", allocationSize = 1)
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

    @Column(name="lldpRemLocalPortNum", nullable = false)
	public Integer getLldpRemLocalPortNum() {
		return m_lldpRemLocalPortNum;
	}

	public void setLldpRemLocalPortNum(Integer lldpRemLocalPortNum) {
		m_lldpRemLocalPortNum = lldpRemLocalPortNum;
	}

	@Column(name="lldpRemIndex", nullable = false)
	public Integer getLldpRemIndex() {
		return m_lldpRemIndex;
	}

	public void setLldpRemIndex(Integer lldpRemIndex) {
		m_lldpRemIndex = lldpRemIndex;
	}

    @Column(name="lldpPortIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.LldpPortIdSubTypeUserType")
	public LldpPortIdSubType getLldpPortIdSubType() {
		return m_lldpPortIdSubType;
	}


	public void setLldpPortIdSubType(LldpPortIdSubType lldpPortIdSubType) {
		m_lldpPortIdSubType = lldpPortIdSubType;
	}

	
    @Column(name="lldpPortId" , length=256, nullable = false)
	public String getLldpPortId() {
		return m_lldpPortId;
	}


	public void setLldpPortId(String lldpPortId) {
		m_lldpPortId = lldpPortId;
	}

    @Column(name="lldpPortDescr" , length=256, nullable = false)
	public String getLldpPortDescr() {
		return m_lldpPortDescr;
	}

	public void setLldpPortDescr(String lldpPortDescr) {
		m_lldpPortDescr = lldpPortDescr;
	}

    @Column(name = "lldpPortIfindex")
	public Integer getLldpPortIfindex() {
		return m_lldpPortIfindex;
	}


	public void setLldpPortIfindex(Integer lldpPortIfindex) {
		m_lldpPortIfindex = lldpPortIfindex;
	}


    @Column(name="lldpRemChassisId" , length=256, nullable = false)
	public String getLldpRemChassisId() {
		return m_lldpRemChassisId;
	}


	public void setLldpRemChassisId(String lldpRemChassisId) {
		m_lldpRemChassisId = lldpRemChassisId;
	}


    @Column(name="lldpRemSysname" , length=256, nullable = false)
	public String getLldpRemSysname() {
		return m_lldpRemSysname;
	}


	public void setLldpRemSysname(String lldpRemSysname) {
		m_lldpRemSysname = lldpRemSysname;
	}


    @Column(name="lldpRemChassisIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.LldpChassisIdSubTypeUserType")
	public LldpChassisIdSubType getLldpRemChassisIdSubType() {
		return m_lldpRemChassisIdSubType;
	}


	public void setLldpRemChassisIdSubType(
			LldpChassisIdSubType lldpRemChassisIdSubType) {
		m_lldpRemChassisIdSubType = lldpRemChassisIdSubType;
	}


    @Column(name="lldpRemPortIdSubType", nullable = false)
    @Type(type="org.opennms.netmgt.enlinkd.model.LldpPortIdSubTypeUserType")
	public LldpPortIdSubType getLldpRemPortIdSubType() {
		return m_lldpRemPortIdSubType;
	}


	public void setLldpRemPortIdSubType(LldpPortIdSubType lldpRemPortIdSubType) {
		m_lldpRemPortIdSubType = lldpRemPortIdSubType;
	}

    @Column(name="lldpRemPortId" , length=256, nullable = false)
	public String getLldpRemPortId() {
		return m_lldpRemPortId;
	}


	public void setLldpRemPortId(String lldpRemPortId) {
		m_lldpRemPortId = lldpRemPortId;
	}


    @Column(name="lldpRemPortDescr" , length=256, nullable = false)
	public String getLldpRemPortDescr() {
		return m_lldpRemPortDescr;
	}


	public void setLldpRemPortDescr(String lldpRemPortDescr) {
		m_lldpRemPortDescr = lldpRemPortDescr;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpLinkCreateTime", nullable=false)
	public Date getLldpLinkCreateTime() {
		return m_lldpLinkCreateTime;
	}


	public void setLldpLinkCreateTime(Date lldpLinkCreateTime) {
		m_lldpLinkCreateTime = lldpLinkCreateTime;
	}


    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="lldpLinkLastPollTime", nullable=false)
	public Date getLldpLinkLastPollTime() {
		return m_lldpLinkLastPollTime;
	}


	public void setLldpLinkLastPollTime(Date lldpLinkLastPollTime) {
		m_lldpLinkLastPollTime = lldpLinkLastPollTime;
	}


	public void merge(LldpLink link) {

		setLldpPortId(link.getLldpPortId());
		setLldpPortIdSubType(link.getLldpPortIdSubType());
		setLldpPortDescr(link.getLldpPortDescr());
		setLldpPortIfindex(link.getLldpPortIfindex());
		
		setLldpRemChassisId(link.getLldpRemChassisId());
		setLldpRemChassisIdSubType(link.getLldpRemChassisIdSubType());
		setLldpRemSysname(link.getLldpRemSysname());
		
		setLldpRemPortId(link.getLldpRemPortId());
		setLldpRemPortIdSubType(link.getLldpRemPortIdSubType());
		setLldpRemPortDescr(link.getLldpRemPortDescr());

		setLldpLinkLastPollTime(link.getLldpLinkCreateTime());
	}
	
	/**
	 * <p>toString</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {

		return "lldplink: nodeid:[" +
				getNode().getId() +
				"]. remLocalPortNum:[ " +
				getLldpRemLocalPortNum() +
				"]. remIndex:[ " +
				getLldpRemIndex() +
				"], ifindex:[" +
				getLldpPortIfindex() +
				"], port type/id:[" +
				LldpPortIdSubType.getTypeString(getLldpPortIdSubType().getValue()) +
				"/" +
				getLldpPortId() +
				"], port descr:[" +
				getLldpPortDescr() +
				"], rem chassis type/id:[" +
				LldpChassisIdSubType.getTypeString(getLldpRemChassisIdSubType().getValue()) +
				"/" +
				getLldpRemChassisId() +
				"], rem sysname:[" +
				getLldpRemSysname() +
				"], rem port type/id:[" +
				LldpPortIdSubType.getTypeString(getLldpRemPortIdSubType().getValue()) +
				"/" +
				getLldpRemPortId() +
				"], rem port descr: [" +
				getLldpRemPortDescr() +
				"]";
	}

}
