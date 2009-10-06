package org.opennms.sms.monitor.internal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="mobile-sequence")
public class MobileSequenceConfig implements Serializable, Comparable<MobileSequenceConfig> {
	private static final long serialVersionUID = 1L;
	private List<MobileSequenceTransaction> m_transactions = Collections.synchronizedList(new ArrayList<MobileSequenceTransaction>());
	private List<SequenceSessionVariable> m_sessionVariables;

	public void addSessionVariable(SequenceSessionVariable var) {
		getSessionVariables().add(var);
	}

	@XmlElement(name="session-variable")
	public List<SequenceSessionVariable> getSessionVariables() {
		if (m_sessionVariables == null) {
			m_sessionVariables = Collections.synchronizedList(new ArrayList<SequenceSessionVariable>());
		}
		return m_sessionVariables;
	}

	public void setSessionVariables(List<SequenceSessionVariable> sessionVariables) {
		m_sessionVariables = sessionVariables;
	}

	public void addTransaction(MobileSequenceTransaction transaction) {
		m_transactions.add(transaction);
	}

	@XmlElement(name="transaction")
	public List<MobileSequenceTransaction> getTransactions() {
		return m_transactions;
	}

	public synchronized void setTransactions(List<MobileSequenceTransaction> transactions) {
		m_transactions.clear();
		m_transactions.addAll(transactions);
	}

	public int compareTo(MobileSequenceConfig o) {
		return new CompareToBuilder()
			.append(this.getTransactions(), o.getTransactions())
			.toComparison();
	}
	
	public String toString() {
		return new ToStringBuilder(this)
			.append("transactions", getTransactions())
			.toString();
	}

}

/*
 * Sample XML (TODO):
 * 
 * <transactionTypes>
 *   <transactionType name="send-ussd" class="org.opennms.sms.monitor.internal.transactions.SendUssd" />
 *   <transactionType name="receive-ussd" class="org.opennms.sms.monitor.internal.transactions.ReceiveUssd" />
 *   <transactionType name="send-sms" class="org.opennms.sms.monitor.internal.transactions.SendSms" />
 *   <transactionType name="receive-sms" class="org.opennms.sms.monitor.internal.transactions.ReceiveSms" />
 * </transactionTypes>
 * 
 */

