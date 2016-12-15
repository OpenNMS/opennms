package org.opennms.netmgt.syslogd.kafka;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

/**
 *
 */
public class KafkaCustomProducer implements Callable<Void>, InitializingBean{

	private static final Logger LOG = LoggerFactory.getLogger(KafkaCustomProducer.class);

    protected org.apache.kafka.clients.producer.Producer<String, String> producer = null;
	public static final String KAFKA_ADDRESS = "kafkaAddress";
	public static final String KAFKA_TOPIC = "kafkaTopic";
	
	private String kafkaAddress; 
	private String kafkaTopic;
	private String message;
	
	public KafkaCustomProducer(){
	}
	
    public KafkaCustomProducer(String kafkaAddress, String kafkaTopic, String message) {
    	this.kafkaAddress = kafkaAddress;
    	this.kafkaTopic = kafkaTopic;
    	this.message = message;
    	doStart();
    }

    protected void doStart() {
    	if(producer == null){
	        Properties configProperties = new Properties();
	        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaAddress);
	        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.ByteArraySerializer");
	        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringSerializer");
	        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 10000);
	        configProperties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
	        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
	        configProperties.put(ProducerConfig.MAX_BLOCK_MS_CONFIG, 10);
	        //configProperties.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, true);
	        configProperties.put(ProducerConfig.ACKS_CONFIG, "0");
	        configProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
	        producer = new KafkaProducer<String, String>(configProperties);
    	}
    }

//    public void process() {
//    	ProducerRecord<String, String> rec = new ProducerRecord<String, String>(kafkaTopic, message);
//        producer.send(rec);
//    }
    
    protected void doStop() throws Exception {
        if (producer != null) {
            producer.close();
        }
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Void call() {
    	try{
	    	ProducerRecord<String, String> rec = new ProducerRecord<String, String>(kafkaTopic, message);
	        producer.send(rec);
    	}catch (IllegalArgumentException e) {
	            LOG.info(e.getMessage());
	    } catch (Exception e) {
            LOG.info(e.getMessage());
	    }catch (Throwable e) {
	            LOG.error("Unexpected error processing messages: {}", e, e);
	    }
        return null;
	}
	
	public String getKafkaAddress() {
		return kafkaAddress;
	}

	public void setKafkaAddress(String kafkaAddress) {
		this.kafkaAddress = kafkaAddress;
	}

	public String getKafkaTopic() {
		return kafkaTopic;
	}

	public void setKafkaTopic(String kafkaTopic) {
		this.kafkaTopic = kafkaTopic;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}


}