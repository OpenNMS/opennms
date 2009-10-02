package org.opennms.sms.monitor.internal.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;

import org.apache.commons.lang.builder.ToStringBuilder;

public abstract class MobileSequenceResponse extends MobileSequenceOperation {

	private List<SequenceResponseMatcher> m_matchers = Collections.synchronizedList(new ArrayList<SequenceResponseMatcher>());

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
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("gatewayId", getGatewayId())
			.append("label", getLabel())
			.append("matchers", getMatchers())
			.toString();
	}
}
