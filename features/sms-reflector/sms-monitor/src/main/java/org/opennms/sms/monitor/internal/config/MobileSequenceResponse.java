package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlTransient;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.opennms.sms.monitor.MobileSequenceSession;
import org.opennms.sms.reflector.smsservice.MobileMsgRequest;
import org.opennms.sms.reflector.smsservice.MobileMsgResponse;

public abstract class MobileSequenceResponse extends MobileSequenceOperation {

    private List<SequenceResponseMatcher> m_matchers = Collections.synchronizedList(new ArrayList<SequenceResponseMatcher>());
	
	private MobileSequenceTransaction m_transaction;

	public MobileSequenceResponse() {
		super();
	}
	
	public MobileSequenceResponse(String label) {
		super(label);
	}

	public MobileSequenceResponse(String gatewayId, String label) {
		super(gatewayId, label);
	}
	
	@XmlElementRef
	public List<SequenceResponseMatcher> getMatchers() {
		return m_matchers;
	}
	
	public void setMatchers(List<SequenceResponseMatcher> matchers) {
		m_matchers.clear();
		m_matchers.addAll(matchers);
	}
	
	public void addMatcher(SequenceResponseMatcher matcher) {
		m_matchers.add(matcher);
	}
	
    public String getEffectiveLabel(MobileSequenceSession session) {
        return getLabel() != null ? session.substitute(getLabel()) : getTransaction().getResponseLabel(session, this); 
    }

    @XmlTransient
    public MobileSequenceTransaction getTransaction() {
        return m_transaction;
    }

    public void setTransaction(MobileSequenceTransaction transaction) {
        m_transaction = transaction;
    }

	public String toString() {
        return new ToStringBuilder(this)
            .append("gatewayId", getGatewayId())
            .append("label", getLabel())
            .append("matchers", getMatchers())
            .toString();
    }

	protected abstract boolean matchesResponseType(MobileMsgRequest request, MobileMsgResponse response);
    
    private boolean matchesCriteria(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {

        for (SequenceResponseMatcher m : getMatchers()) {
            if (!m.matches(session, request, response)) {
                return false;
            }
        }
        return true;
    }

    public boolean matches(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response) {
        return matchesResponseType(request, response) && matchesCriteria(session, request, response);
    }

    public abstract void processResponse(MobileSequenceSession session, MobileMsgRequest request, MobileMsgResponse response);

}
