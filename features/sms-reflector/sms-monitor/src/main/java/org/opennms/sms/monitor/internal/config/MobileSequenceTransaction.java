/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2009-2012 The OpenNMS Group, Inc.
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

package org.opennms.sms.monitor.internal.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;
import org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler;

/**
 * <p>MobileSequenceTransaction class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="transaction")
@XmlType(propOrder={"request", "responses"})
public class MobileSequenceTransaction implements Comparable<MobileSequenceTransaction> {

    /* containing sequenceConfig */
    private MobileSequenceConfig m_sequenceConfig;

    /* attributes and sub elements */
    private String m_label;
	private String m_gatewayId;
	private MobileSequenceRequest m_request;
	private List<MobileSequenceResponse> m_responses;
	
    /* other data */
	private String m_defaultGatewayId;

	/**
	 * <p>Constructor for MobileSequenceTransaction.</p>
	 */
	public MobileSequenceTransaction() {
	}

	/**
	 * <p>Constructor for MobileSequenceTransaction.</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceTransaction(String label) {
		setLabel(label);
	}
	
	/**
	 * <p>Constructor for MobileSequenceTransaction.</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 * @param label a {@link java.lang.String} object.
	 */
	public MobileSequenceTransaction(String gatewayId, String label) {
		this(label);
		setGatewayId(gatewayId);
	}
	
	/**
	 * <p>getLabel</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	@XmlAttribute(name="label", required=true)
	public String getLabel() {
		return m_label;
	}
	
	/**
	 * <p>setLabel</p>
	 *
	 * @param label a {@link java.lang.String} object.
	 */
	public void setLabel(String label) {
		m_label = label;
	}

    /**
     * <p>getGatewayId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="gatewayId", required=false)
    public String getGatewayId() {
        return m_gatewayId;
    }

	/**
	 * <p>setGatewayId</p>
	 *
	 * @param gatewayId a {@link java.lang.String} object.
	 */
	public void setGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
	}

	/**
	 * <p>getRequest</p>
	 *
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceRequest} object.
	 */
	@XmlElementRef
	public MobileSequenceRequest getRequest() {
		return m_request;
	}
	
	/**
	 * <p>setRequest</p>
	 *
	 * @param request a {@link org.opennms.sms.monitor.internal.config.MobileSequenceRequest} object.
	 */
	public void setRequest(MobileSequenceRequest request) {

	    if (m_request != null) {
	        m_request.setTransaction(null);
	    }

	    m_request = request;

		if (request != null) {
		    request.setTransaction(this);
		}

	}
	
	/**
	 * <p>getResponses</p>
	 *
	 * @return a {@link java.util.List} object.
	 */
	@XmlElementRef
	public List<MobileSequenceResponse> getResponses() {
	    if (m_responses == null) {
	        m_responses = createResponsesList();
	    }
		return m_responses;
	}

	private List<MobileSequenceResponse> createResponsesList() {
	    return new TriggeredList<MobileSequenceResponse>() {

            @Override
            protected void onAdd(int index, MobileSequenceResponse element) {
                element.setTransaction(MobileSequenceTransaction.this);
            }

            @Override
            protected void onRemove(int index, MobileSequenceResponse element) {
                element.setTransaction(null);
            }
	        
	    };
    }

    /**
     * <p>setResponses</p>
     *
     * @param responses a {@link java.util.List} object.
     */
    public synchronized void setResponses(List<MobileSequenceResponse> responses) {
		if (m_responses == responses) return;
		m_responses.clear();
		m_responses.addAll(responses);
	}
	
	/**
	 * <p>addResponse</p>
	 *
	 * @param response a {@link org.opennms.sms.monitor.internal.config.MobileSequenceResponse} object.
	 */
	public void addResponse(MobileSequenceResponse response) {
		getResponses().add(response);
		
	}

	/**
	 * <p>getSequenceConfig</p>
	 *
	 * @return a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
	 */
	@XmlTransient
	public MobileSequenceConfig getSequenceConfig() {
	    return m_sequenceConfig;
	}

	/**
	 * <p>setSequenceConfig</p>
	 *
	 * @param sequenceConfig a {@link org.opennms.sms.monitor.internal.config.MobileSequenceConfig} object.
	 */
	public void setSequenceConfig(MobileSequenceConfig sequenceConfig) {
	    m_sequenceConfig = sequenceConfig;
	}
	
    /**
     * <p>getDefaultGatewayId</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlTransient
    public String getDefaultGatewayId() {
        return m_defaultGatewayId;
    }
    
    /**
     * <p>setDefaultGatewayId</p>
     *
     * @param gatewayId a {@link java.lang.String} object.
     */
    public void setDefaultGatewayId(String gatewayId) {
        m_defaultGatewayId = gatewayId;
    }
    
    /**
     * <p>getLabel</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @return a {@link java.lang.String} object.
     */
    public String getLabel(MobileSequenceSession session) {
        return session.substitute(getRequest().getLabel(getLabel()));
    }

    /**
     * <p>compareTo</p>
     *
     * @param o a {@link org.opennms.sms.monitor.internal.config.MobileSequenceTransaction} object.
     * @return a int.
     */
    @Override
    public int compareTo(MobileSequenceTransaction o) {
        return new CompareToBuilder()
            .append(this.getRequest(), o.getRequest())
            .append(this.getResponses(), o.getResponses())
            .toComparison();
    }
    
    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("label", getLabel())
            .append("gatewayId", getGatewayId())
            .append("request", getRequest())
            .append("response(s)", getResponses())
            .toString();
    }

    /**
     * <p>sendRequest</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param responseHandler a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponseHandler} object.
     */
    public void sendRequest(MobileSequenceSession session, MobileMsgResponseHandler responseHandler) {
        getRequest().send(session, responseHandler);
    }

    /**
     * <p>matchesResponse</p>
     *
     * @param session a {@link org.opennms.sms.monitor.MobileSequenceSession} object.
     * @param request a {@link org.opennms.sms.reflector.smsservice.MobileMsgRequest} object.
     * @param response a {@link org.opennms.sms.reflector.smsservice.MobileMsgResponse} object.
     * @return a boolean.
     */
    public boolean matchesResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        
        boolean match = false;
        
        for ( MobileSequenceResponse r : getResponses() ) {
            match = r.matches(session, request, response);
        }
        
        return match;
    }

    String getResponseLabel(MobileSequenceSession session, MobileSequenceResponse response) {
        return session.substitute(getLabel()+".response"+getResponseIndex(response));
    }

    private int getResponseIndex(MobileSequenceResponse response) {
        int index = 1;
        for(MobileSequenceResponse r : getResponses()) {
            if (r == response) {
                return index;
            }
            index++;
        }
        throw new IllegalArgumentException("response not found in transaction!");
    }

}
