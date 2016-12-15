package org.opennms.netmgt.syslogd.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.camel.Exchange;
import org.opennms.core.concurrent.ExecutorFactory;
import org.opennms.core.concurrent.ExecutorFactoryJavaImpl;
import org.opennms.netmgt.snmp.TrapNotification;
import org.opennms.netmgt.syslogd.SyslogDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerImpl {

		private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerImpl.class);

		public static final int KAFKA_PRODUCER_THREADS = Runtime.getRuntime().availableProcessors();
		
		private final ExecutorFactory m_executorFactory = new ExecutorFactoryJavaImpl();
		private final ExecutorService m_processorExecutor = m_executorFactory.newExecutor(KAFKA_PRODUCER_THREADS, 300, "Minion.Trapd.Kafka", "kafkaProcessors");

		private KafkaProducerFactory m_producerFactory;
		private String m_kafkaAddress;
		private String m_kafkaTopic;

		public void handleKafkaMessage(SyslogDTO syslogDto){//String kafkaAddress, String kafkaTopic, String message) {
			try {
				KafkaCustomProducer producer = m_producerFactory.getInstance();
				producer.setKafkaAddress(m_kafkaAddress);
				producer.setKafkaTopic(m_kafkaTopic);
				producer.setMessage("Test msg !");
				producer.doStart();
				// Call the producer asynchronously
				try {
					CompletableFuture.supplyAsync(producer::call, m_processorExecutor);
				} catch (Exception e) {
					e.printStackTrace();
				}
			} catch (Throwable e) {
				LOG.error("Task execution failed in {}", this.getClass().getSimpleName(), e);
			}
		}

		/**
		 * @return the m_producerFactory
		 */
		public KafkaProducerFactory getProducerFactory() {
			return m_producerFactory;
		}

		/**
		 * @param m_producerFactory the m_producerFactory to set
		 */
		public void setProducerFactory(KafkaProducerFactory m_producerFactory) {
			this.m_producerFactory = m_producerFactory;
		}	
		
		
		/**
		 * @return the m_kafkaAddress
		 */
		public String getKafkaAddress() {
			return m_kafkaAddress;
		}

		/**
		 * @param m_kafkaAddress the m_kafkaAddress to set
		 */
		public void setKafkaAddress(String m_kafkaAddress) {
			this.m_kafkaAddress = m_kafkaAddress;
		}	
				
		
		/**
		 * @return the m_kafkaTopic
		 */
		public String getKafkaTopic() {
			return m_kafkaTopic;
		}

		/**
		 * @param m_kafkaTopic the m_kafkaTopic to set
		 */
		public void setKafkaTopic(String m_kafkaTopic) {
			this.m_kafkaTopic = m_kafkaTopic;
		}		
	
}
