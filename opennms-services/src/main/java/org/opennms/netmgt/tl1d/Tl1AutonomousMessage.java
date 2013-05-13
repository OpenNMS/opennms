/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2008-2012 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.tl1d;

import java.util.Date;

/**
 * This class is used to represent a TL1 Autonomous Message as defined below and scraped from
 * WikiPedia.
 *
 *<table border="1" cellspacing="0">
 *<tr>
 *<td colspan="8" align="center"><b>TL1 autonomous message</b></td>
 *</tr>
 *<tr>
 *<td colspan="3" align="center">Auto Header</td>
 *<td colspan="3" align="center">Auto Id</td>
 *<td colspan="1" align="center">Auto block</td>
 *<td colspan="1" align="center">Terminators</td>
 *</tr>
 *<tr>
 *<td>SID</td>
 *<td>Date</td>
 *<td>Time</td>
 *<td>Alarm code</td>
 *<td>ATAG</td>
 *<td>Verb</td>
 *<td></td>
 *<td></td>
 *</tr>
 *<tr>
 *<td><i>MyNE</i></td>
 *<td><i>04-08-14</i></td>
 *<td><i>09:12:04</i></td>
 *<td><i><center>A</center></i></td>
 *<td><i>101</i></td>
 *<td><i>REPT EVT SESSION</i></td>
 *<td></td>
 *<td></td>
 *</tr>
 *</table>
 *
 * @author <a href=mailto:david@opennms.org>David Hustace</a>
 * @version $Id: $
 */
public class Tl1AutonomousMessage extends Tl1Message {
    
    /** Constant <code>UEI="uei.opennms.org/api/tl1d/message/autono"{trunked}</code> */
    public static final String UEI = "uei.opennms.org/api/tl1d/message/autonomous";
    private AutoHeader m_autoHeader;
    private AutoId m_autoId;
    private AutoBlock m_autoBlock;
    private String m_terminator;
    
    /**
     * <p>Constructor for Tl1AutonomousMessage.</p>
     *
     * @param rawMessage a {@link java.lang.String} object.
     */
    public Tl1AutonomousMessage(String rawMessage) {
        super.setRawMessage(rawMessage);
        m_autoHeader = new AutoHeader();
        m_autoId = new AutoId();
        m_autoBlock = new AutoBlock();
        m_terminator = ";\n";
    }
    
    protected class AutoHeader {
        
        private String m_rawMessage;
        private String m_sid;
        private String m_date;
        private String m_time;
        private Date m_timestamp;
        
        public String getRawMessage() {
            return m_rawMessage;
        }
        public void setRawMessage(String rawMessage) {
            m_rawMessage = rawMessage;
        }
        public String getSid() {
            return m_sid;
        }
        public void setSid(String sid) {
            m_sid = sid;
        }
        public String getDate() {
            return m_date;
        }
        public void setDate(String date) {
            m_date = date;
        }
        public String getTime() {
            return m_time;
        }
        public void setTime(String time) {
            m_time = time;
        }
        public Date getTimestamp() {
            return m_timestamp;
        }
        public void setTimestamp(Date timestamp) {
            m_timestamp = timestamp;
        }
        
        @Override
        public String toString() {
            return m_rawMessage;
        }

    }
    
    protected class AutoId {
        
        private String m_rawMessage;
        private String m_alarmCode;
        private String m_alarmTag;
        private String m_verb;
        // private String m_verbModifier1;
        // private String m_verbModifier2;
        private String m_highestSeverity; //derived from alarmCode
       
        
        public String getRawMessage() {
            return m_rawMessage;
        }
        public void setRawMessage(String rawMessage) {
            m_rawMessage = rawMessage;
        }
        public String getAlarmCode() {
            return m_alarmCode;
        }
        public void setAlarmCode(String alarmCode) {
            m_alarmCode = alarmCode;   
            
            /* The highest alarm Severity is based on the AlarmCode. */        
            if(m_alarmCode.equals("*C"))
                m_highestSeverity = "Critical";
            else if(m_alarmCode.equals("**"))
                m_highestSeverity = "Major";
            else if(m_alarmCode.equals("*"))
                m_highestSeverity = "Minor";
            else if(m_alarmCode.equals("A"))
                m_highestSeverity = "Cleared";
          
        }
        
        public String getHighestSeverity() {
            return m_highestSeverity;
        }
       
        public String getAlarmTag() {
            return m_alarmTag;
        }
        public void setAlarmTag(String alarmTag) {
            m_alarmTag = alarmTag;
        }
        public String getVerb() {
            return m_verb;
        }
        public void setVerb(String verb) {
            m_verb = verb;
        }
        @Override
        public String toString() {
            return m_rawMessage;
        }
    }

    protected class AutoBlock {
        
        private String m_block;
        private String m_aid;
        private String m_ntfcncde;
        // private String m_severity;
        private String m_additionalParams;

        public String getBlock() {
            return m_block;
        }

        public void setBlock(String block) {
            m_block = block;
        }
        
       public void setAid(String aid) {
           m_aid = aid;
       }
       
       public String getAid() {
           return m_aid;
       }
       
       public void setNtfcncde(String ntfcncde) {
           m_ntfcncde = ntfcncde;
       }
       
       public String getNtfcncde() {
           return m_ntfcncde;
       }
       
       public void setAdditionalParams(String additionalParams){
           m_additionalParams = additionalParams;
       }
       
       public String getAdditionalParams(){
           return m_additionalParams;
       }
        
        @Override
        public String toString() {
            return m_block;
        }
    }

    /**
     * <p>getHeader</p>
     *
     * @return a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoHeader} object.
     */
    public AutoHeader getHeader() {
        return m_autoHeader;
    }

    /**
     * <p>setHeader</p>
     *
     * @param header a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoHeader} object.
     */
    public void setHeader(AutoHeader header) {
        m_autoHeader = header;
    }

    /**
     * <p>getId</p>
     *
     * @return a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoId} object.
     */
    public AutoId getId() {
        return m_autoId;
    }

    /**
     * <p>setId</p>
     *
     * @param id a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoId} object.
     */
    public void setId(AutoId id) {
        m_autoId = id;
    }

    /**
     * <p>getAutoBlock</p>
     *
     * @return a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoBlock} object.
     */
    public AutoBlock getAutoBlock() {
        return m_autoBlock;
    }

    /**
     * <p>setAutoBlock</p>
     *
     * @param block a {@link org.opennms.netmgt.tl1d.Tl1AutonomousMessage.AutoBlock} object.
     */
    public void setAutoBlock(AutoBlock block) {
        m_autoBlock = block;
    }

    /**
     * <p>getTerminator</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String getTerminator() {
        return m_terminator;
    }
    
    /**
     * <p>setTerminator</p>
     *
     * @param terminator a {@link java.lang.String} object.
     */
    public void setTerminator(String terminator) {
        m_terminator = terminator;
    }
}
