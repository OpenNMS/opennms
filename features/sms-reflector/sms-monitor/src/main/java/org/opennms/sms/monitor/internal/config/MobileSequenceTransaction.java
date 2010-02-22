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

	public MobileSequenceTransaction() {
	}

	public MobileSequenceTransaction(String label) {
		setLabel(label);
	}
	
	public MobileSequenceTransaction(String gatewayId, String label) {
		this(label);
		setGatewayId(gatewayId);
	}
	
	@XmlAttribute(name="label", required=true)
	public String getLabel() {
		return m_label;
	}
	
	public void setLabel(String label) {
		m_label = label;
	}

    @XmlAttribute(name="gatewayId", required=false)
    public String getGatewayId() {
        return m_gatewayId;
    }

	public void setGatewayId(String gatewayId) {
		m_gatewayId = gatewayId;
	}

	@XmlElementRef
	public MobileSequenceRequest getRequest() {
		return m_request;
	}
	
	public void setRequest(MobileSequenceRequest request) {

	    if (m_request != null) {
	        m_request.setTransaction(null);
	    }

	    m_request = request;

		if (request != null) {
		    request.setTransaction(this);
		}

	}
	
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

    public synchronized void setResponses(List<MobileSequenceResponse> responses) {
		m_responses.clear();
		m_responses.addAll(responses);
	}
	
	public void addResponse(MobileSequenceResponse response) {
		getResponses().add(response);
		
	}

	@XmlTransient
	public MobileSequenceConfig getSequenceConfig() {
	    return m_sequenceConfig;
	}

	public void setSequenceConfig(MobileSequenceConfig sequenceConfig) {
	    m_sequenceConfig = sequenceConfig;
	}
	
    @XmlTransient
    public String getDefaultGatewayId() {
        return m_defaultGatewayId;
    }
    
    public void setDefaultGatewayId(String gatewayId) {
        m_defaultGatewayId = gatewayId;
    }
    
    public String getLabel(MobileSequenceSession session) {
        return session.substitute(getRequest().getLabel(getLabel()));
    }

    public int compareTo(MobileSequenceTransaction o) {
        return new CompareToBuilder()
            .append(this.getRequest(), o.getRequest())
            .append(this.getResponses(), o.getResponses())
            .toComparison();
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("label", getLabel())
            .append("gatewayId", getGatewayId())
            .append("request", getRequest())
            .append("response(s)", getResponses())
            .toString();
    }

    public void sendRequest(MobileSequenceSession session, MobileMsgResponseHandler responseHandler) {
        getRequest().send(session, responseHandler);
    }

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
