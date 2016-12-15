package org.opennms.netmgt.trapd.kafka;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;

import org.apache.camel.Exchange;
import org.opennms.core.concurrent.ExecutorFactory;
import org.opennms.core.concurrent.ExecutorFactoryJavaImpl;
import org.opennms.netmgt.snmp.TrapNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaProducerImpl {

		private static final Logger LOG = LoggerFactory.getLogger(KafkaProducerImpl.class);

		/**
		 * This is the number of threads that are used to process traps.
		 * 
		 * TODO: Make this configurable
		 */
		public static final int KAFKA_PROCESSOR_THREADS = Runtime.getRuntime().availableProcessors();
		
		private final ExecutorFactory m_executorFactory = new ExecutorFactoryJavaImpl();
		private final ExecutorService m_processorExecutor = m_executorFactory.newExecutor(KAFKA_PROCESSOR_THREADS, 300, "Minion.Trapd.Kafka", "kafkaProcessors");

		private KafkaProducerFactory m_producerFactory;

		public void handleKafkaMessage(String kafkaAddress, String kafkaTopic, String message) {
			try {
				KafkaCustomProducer producer = m_producerFactory.getInstance();
				producer.setKafkaAddress(kafkaAddress);
				producer.setKafkaTopic(kafkaTopic);
				producer.setMessage(message);
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
	
}
