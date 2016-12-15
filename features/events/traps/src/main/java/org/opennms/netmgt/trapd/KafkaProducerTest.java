package org.opennms.netmgt.trapd;

import java.util.Properties;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;

public class KafkaProducerTest {

	public static void main(String[] argv)throws Exception {

        String topicName = "testsize";


        //Configure the Producer
        Properties configProperties = new Properties();
        configProperties.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"taspmoosskafka101.cernerasp.com:9092,taspmoosskafka102.cernerasp.com:9092,taspmoosskafka103.cernerasp.com:9092");
        configProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.ByteArraySerializer");
        configProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,"org.apache.kafka.common.serialization.StringSerializer");
        configProperties.put(ProducerConfig.BATCH_SIZE_CONFIG, 10000);
        configProperties.put(ProducerConfig.LINGER_MS_CONFIG, 100);
        configProperties.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);
        //configProperties.put(ProducerConfig.BLOCK_ON_BUFFER_FULL_CONFIG, true);
        configProperties.put(ProducerConfig.ACKS_CONFIG, "1");
        configProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "org.apache.kafka.clients.producer.internals.DefaultPartitioner");
        //configProperties.put(ProducerConfig.PARTITIONER_CLASS_CONFIG, "kafka.producer.ByteArrayPartitioner");


        org.apache.kafka.clients.producer.Producer<String, String> producer = new KafkaProducer<String, String>(configProperties);
        String msg = "Hello 123" ;
        for(int i=0 ; i<10000 ; i++){
            ProducerRecord<String, String> rec = new ProducerRecord<String, String>(topicName, msg);
            producer.send(rec);
            System.out.println("i is : "+i);
        }

        producer.close();
    }

	
}
