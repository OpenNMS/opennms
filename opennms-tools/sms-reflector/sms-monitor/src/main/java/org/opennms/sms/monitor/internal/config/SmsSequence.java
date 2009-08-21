package org.opennms.sms.monitor.internal.config;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.CompareToBuilder;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name="sms-sequence")
public class SmsSequence implements Serializable, Comparable<SmsSequence> {
	private static final long serialVersionUID = 1L;

	@XmlElementRef
	List<SequenceTransaction> m_transactions = Collections.synchronizedList(new ArrayList<SequenceTransaction>());

	public void addTransaction(SequenceTransaction transaction) {
		m_transactions.add(transaction);
	}

	public List<SequenceTransaction> getTransactions() {
		return m_transactions;
	}

	public void setTransactions(List<SequenceTransaction> transactions) {
		m_transactions = transactions;
	}

	public int compareTo(SmsSequence o) {
		return new CompareToBuilder()
			.append(this.getTransactions(), o.getTransactions())
			.toComparison();
	}
}

/*
 * Sample XML:
 * 
 * <transactionTypes>
 *   <transactionType name="send-ussd" class="org.opennms.sms.monitor.internal.transactions.SendUssd" />
 *   <transactionType name="receive-ussd" class="org.opennms.sms.monitor.internal.transactions.ReceiveUssd" />
 *   <transactionType name="send-sms" class="org.opennms.sms.monitor.internal.transactions.SendSms" />
 *   <transactionType name="receive-sms" class="org.opennms.sms.monitor.internal.transactions.ReceiveSms" />
 * </transactionTypes>
 * 
 * <sms-sequence>
 *   <transaction label="USSD balance">
 *     <!-- UniqueNumber conforms to TransactionSessionVariable interface, checkOut(), and checkIn(value) -->
 *     <session-variable name="amount" class="org.opennms.sms.monitor.internal.config.UniqueNumber">
 *       <parameter key="min" value="1" />
 *       <parameter key="max" value="15" />
 *     </session-variable>
 *     <operation type="send-ussd" label="originator sends balance request">
 *       <value>*327*${session.target}*${session.amount}#</value>
 *     </operation>
 *     <operation type="receive-ussd" label="network asks for balance confirmation">
 *       <value>~Transfiere L ${session.amount} al ${session.target}</value>
 *       <parameters>
 *         <!-- CUSD -->
 *         <parameter key="session-status" value="1" />
 *       </parameters>
 *     </operation>
 *     <operation type="send-ussd" label="send 1 to confirm balance request">1</operation>
 *     <transaction type="asynchronous">
 *       <operation type="receive-ussd" label="transaction is processing">
 *         <value>~Su transaccion se esta procesando</value>
 *         <parameters>
 *           <parameter key="session-status" value="0" />
 *         </parameters>
 *       </operation>
 *       <operation type="receive-sms" label="receive balance amount">
 *         <value>~le ha transferido L ${session.amount}</value>
 *         <parameters>
 *           <parameter key="validate-source" value="+3746" />
 *         </parameters>
 *       </operation>
 *     </transaction>
 *   </transaction>
 * </sms-sequence>
 *
 * <sms-sequence>
 *   <transaction label="SMS gift">
 *     <operation type="send-sms" label="sending ping">
 *       <value>'tis better to give...</value>
 *       <parameters>
 *         <parameter key="destination" value="*327${session.target}" />
 *     </operation>
 *     <transaction type="asynchronous">
 *       <transaction>
 *         <operation type="receive-sms" label="receive ping">
 *           <value>'tis better to give... [Responda gratis]</value>
 *           <!-- store the source phone number to the session variable "source" -->
 *           <parameter key="store-source" value="source" />
 *         </operation>
 *         <operation type="send-sms" label="send pong">
 *           <value>...than to receive.</value>
 *           <parameters>
 *             <parameter key="destination" value="${session.source}" />
 *           </parameters>
 *         </operation>
 *       </transaction>
 *       <operation> type="receive-sms" label="receive pong">
 *         <value>~than to receive</value>
 *         <parameters>
 *           <parameter key="validate-source" value="+327${session.target}" />
 *         </parameter>
 *       </operation>
 *     </transaction>
 *   </transaction>
 * </sms-sequence>
 * 
 */

