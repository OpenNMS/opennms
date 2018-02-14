/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.model;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.opennms.netmgt.xml.event.Event;
import org.opennms.netmgt.xml.event.Parm;

@XmlRootElement(name="ack")  //hmmm
@Entity
@Table(name = "acks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
/**
 * Persistable object used in acknowledgment activities
 *
 * @author <a href="mailto:david@opennms.org">David Hustace</a>
 * @version $Id: $
 */
public class OnmsAcknowledgment {

    private Integer m_id; 
    private Date m_ackTime;
    private String m_ackUser;
    private AckType m_ackType;
    private AckAction m_ackAction;
    private String m_log;
    private Integer m_refId;
    
    //main constructor
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param time a {@link java.util.Date} object.
     * @param user a {@link java.lang.String} object.
     */
    public OnmsAcknowledgment(Date time, String user) {
        m_ackTime = (time == null) ? new Date() : time;
        m_ackUser = (user == null) ? "admin" : user;
        m_ackType = AckType.UNSPECIFIED;
        m_ackAction = AckAction.ACKNOWLEDGE;  //probably should be the default, set as appropriate after instantiation
    }
    
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     */
    public OnmsAcknowledgment() {
        this(new Date(), "admin");
    }
    
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public OnmsAcknowledgment(String user) {
        this(new Date(), user);
    }
    
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param time a {@link java.util.Date} object.
     */
    public OnmsAcknowledgment(final Date time) {
        this(time, "admin");
    }

    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param e a {@link org.opennms.netmgt.xml.event.Event} object.
     * @throws java.text.ParseException if any.
     */
    public OnmsAcknowledgment(final Event e) throws ParseException {
        this(e.getTime(), "admin");
        Collection<Parm> parms = e.getParmCollection();
        
        if (parms.size() <= 2) {
            throw new IllegalArgumentException("Event:"+e.getUei()+" has invalid paramenter list, requires ackType and refId.");
        }
        
        for (Parm parm : parms) {
            final String parmValue = parm.getValue().getContent();
            
            if (!"ackType".equals(parm.getParmName()) && !"refId".equals(parm.getParmName()) && !"user".equals(parm.getParmName()) ) {
                throw new IllegalArgumentException("Event parm: "+parm.getParmName()+", is an invalid paramter");
            } else {
            
                if ("ackType".equals(parm.getParmName())) {

                    if ("ALARM".equalsIgnoreCase(parmValue) || "NOTIFICATION".equalsIgnoreCase(parmValue)) {
                        m_ackType = ("ALARM".equalsIgnoreCase(parmValue) ? AckType.ALARM : AckType.NOTIFICATION);
                    } else {
                        throw new IllegalArgumentException("Event parm: "+parm.getParmName()+", has invalid value, requires: \"Alarm\" or \"Notification\"." );
                    }
                    
                } else if ("refId".equals(parm.getParmName())){
                    m_refId = Integer.valueOf(parm.getValue().getContent());
                } else {
                    m_ackUser = parm.getValue().getContent();
                }
            }                
        }
    }

    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param a a {@link org.opennms.netmgt.model.Acknowledgeable} object.
     */
    public OnmsAcknowledgment(final Acknowledgeable a) {
        this(a, "admin", new Date());
        
        //not sure this is a valid use case but doing it for now
        if (a.getType() == AckType.ALARM) {
            if (a.getAckUser() != null) {
                m_ackUser = a.getAckUser();
                m_ackTime = a.getAckTime();
            }
        }
        
        
    }
    
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param a a {@link org.opennms.netmgt.model.Acknowledgeable} object.
     * @param user a {@link java.lang.String} object.
     */
    public OnmsAcknowledgment(final Acknowledgeable a, String user) {
        this(a, user, new Date());
    }
    
    /**
     * <p>Constructor for OnmsAcknowledgment.</p>
     *
     * @param a a {@link org.opennms.netmgt.model.Acknowledgeable} object.
     * @param user a {@link java.lang.String} object.
     * @param ackTime a {@link java.util.Date} object.
     */
    public OnmsAcknowledgment(final Acknowledgeable a, String user, Date ackTime) {
        this();
        if (a == null) {
            throw new IllegalArgumentException("Acknowledgable is null.");
        }
        
        m_ackUser = user;
        m_ackTime = ackTime;
        m_ackType = a.getType();
        m_refId = a.getAckId();
    }
    
    
    /**
     * <p>getId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Id
    @Column(nullable=false)
    @SequenceGenerator(name="opennmsSequence", sequenceName="opennmsNxtId")
    @GeneratedValue(generator="opennmsSequence")    
    public Integer getId() {
        return m_id;
    }
    
    /**
     * <p>setId</p>
     *
     * @param id a {@link java.lang.Integer} object.
     */
    public void setId(Integer id) {
        m_id = id;
    }

    /**
     * <p>getAckTime</p>
     *
     * @return a {@link java.util.Date} object.
     */
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="ackTime", nullable=false)
    public Date getAckTime() {
        return m_ackTime;
    }
    
    /**
     * <p>setAckTime</p>
     *
     * @param time a {@link java.util.Date} object.
     */
    public void setAckTime(Date time) {
        m_ackTime = time;
    }

    //TODO: make this right when Users are persisted to the DB
    /**
     * <p>getAckUser</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="ackUser", length=64, nullable=false)
    public String getAckUser() {
        return m_ackUser;
    }
    
    /**
     * <p>setAckUser</p>
     *
     * @param user a {@link java.lang.String} object.
     */
    public void setAckUser(String user) {
        m_ackUser = user;
    }

    /**
     * <p>getAckType</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckType} object.
     */
    @Column(name="ackType", nullable=false)
    public AckType getAckType() {
        return m_ackType;
    }

    /**
     * <p>setAckType</p>
     *
     * @param ackType a {@link org.opennms.netmgt.model.AckType} object.
     */
    public void setAckType(AckType ackType) {
        m_ackType = ackType;
    }

    /**
     * <p>getRefId</p>
     *
     * @return a {@link java.lang.Integer} object.
     */
    @Column(name="refId")
    public Integer getRefId() {
        return m_refId;
    }

    /**
     * <p>setRefId</p>
     *
     * @param refId a {@link java.lang.Integer} object.
     */
    public void setRefId(Integer refId) {
        m_refId = refId;
    }

    /**
     * <p>getAckAction</p>
     *
     * @return a {@link org.opennms.netmgt.model.AckAction} object.
     */
    @Column(name="ackAction", nullable=false)
    public AckAction getAckAction() {
        return m_ackAction;
    }

    /**
     * <p>setAckAction</p>
     *
     * @param ackAction a {@link org.opennms.netmgt.model.AckAction} object.
     */
    public void setAckAction(AckAction ackAction) {
        m_ackAction = ackAction;
    }
    
    /**
     * <p>getLog</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Column(name="log", nullable=true)
    public String getLog() {
        return m_log;
    }
    
    /**
     * <p>setLog</p>
     *
     * @param log a {@link java.lang.String} object.
     */
    public void setLog(String log) {
        m_log = log;
    }

    /** {@inheritDoc} */
    @Override
    public String toString() {
        final StringBuilder bldr = new StringBuilder("Acknowledgment ID:");
        bldr.append(m_id);
        bldr.append(" User:");
        bldr.append(m_ackUser);
        bldr.append(" Time:");
        bldr.append(m_ackTime);
        bldr.append(" AckType:");
        bldr.append(m_ackType);
        bldr.append(" AckAction:");
        bldr.append(m_ackAction);
        bldr.append(" Acknowledable ID:");
        bldr.append(m_refId);
        return bldr.toString();
    }

}
