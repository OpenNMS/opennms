package org.opennms.netmgt.trapd;

import java.util.Random;

import kafka.producer.Partitioner;

public class TrapdKafkaPartitioner implements Partitioner{

	@Override
	public int partition(Object arg0, int a_numPartitions) {
		
		int partition = 0;
		Random rnd = new Random();
//		if(arg0==null){
			partition =  rnd.nextInt(a_numPartitions);
//		}else{
//	        String stringKey = InetAddressUtils.toIpAddrString(sourceAddress);
//	        int offset = stringKey.lastIndexOf('.');
//	        if (offset > 0) {
//	           partition = Integer.parseInt( stringKey.substring(offset+1)) % a_numPartitions;
//	        }
//		}
		return partition;
	}
	
}
