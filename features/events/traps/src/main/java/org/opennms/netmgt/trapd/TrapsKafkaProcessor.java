package org.opennms.netmgt.trapd;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.opennms.netmgt.trapd.kafka.KafkaProducerImpl;

public class TrapsKafkaProcessor implements Processor{
	
	public static final String KAFKA_ADDRESS = "kafkaAddress";
	public static final String KAFKA_TOPIC = "kafkaTopic";

   // final org.apache.kafka.clients.producer.Producer<String, Object> producer = null;

	@Override
	public void process(Exchange exchange) throws Exception {
		final String message = exchange.getIn().getBody(String.class);
		String kafkaAddress = (String)exchange.getIn().getHeader(KAFKA_ADDRESS);
		String kafkaTopic = (String)exchange.getIn().getHeader(KAFKA_TOPIC);
		
		KafkaProducerImpl kafkaImpl = new KafkaProducerImpl();
		kafkaImpl.handleKafkaMessage(kafkaAddress, kafkaTopic, message);
		
//		//Configure the Producer
//        Properties configProperties = new Properties();
//        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
//        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
//        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
//        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 100);
//        configProperties.put(ProducerConfig.LINGER_MS_CONFIG, 1000);
//        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
//        configProperties.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, true);
//        configProperties.put(ProducerConfig.ACKS_CONFIG, "0");
//        configProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
//
//        final org.apache.kafka.clients.producer.Producer<String, Object> producer = new KafkaProducer<String, Object>(configProperties);
//        ProducerRecord<String, Object> rec = new ProducerRecord<String, Object>(kafkaTopic, trapDto);
//        producer.send(rec);
//        producer.close();
	}	
}
