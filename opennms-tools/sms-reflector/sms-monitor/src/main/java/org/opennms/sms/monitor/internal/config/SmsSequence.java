package org.opennms.sms.monitor.internal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

@XmlRootElement(name="sms-sequence")
public class SmsSequence implements Serializable, Comparable<SmsSequence> {
	private static final long serialVersionUID = 1L;

	List<TransactionOperation> m_transactions;

	public void addTransaction(TransactionOperation transaction) {
		if (m_transactions == null) {
			m_transactions = Collections.synchronizedList(new ArrayList<TransactionOperation>());
		}
		m_transactions.add(transaction);
	}

	@XmlElementRef
	public List<TransactionOperation> getTransactions() {
		return m_transactions;
	}

	public void setTransactions(List<TransactionOperation> transactions) {
		m_transactions = transactions;
	}

	public int compareTo(SmsSequence o) {
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

