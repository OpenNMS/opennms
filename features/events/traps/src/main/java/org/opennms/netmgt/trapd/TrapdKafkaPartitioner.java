package org.opennms.netmgt.trapd;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import kafka.producer.Partitioner;

public class TrapdKafkaPartitioner implements Partitioner{
	private static Set<Integer> uniqueRandomSet=new HashSet<Integer>();
	@Override
	public int partition(Object arg0, int a_numPartitions) {
		
//		int partition = 0;
//		Random rnd = new Random();
////		if(arg0==null){
//			partition =  rnd.nextInt(a_numPartitions - 0 + 1) + 0;
////		}else{
////	        String stringKey = InetAddressUtils.toIpAddrString(sourceAddress);
////	        int offset = stringKey.lastIndexOf('.');
////	        if (offset > 0) {
////	           partition = Integer.parseInt( stringKey.substring(offset+1)) % a_numPartitions;
////	        }
////		}
		Random rand = null;
		int randomNum;
		rand = new Random();
		randomNum = rand.nextInt(300);
		return randomNum;
	}
	
	public static int UniqueRandomNumberGenration(Object arg0) {
		Random rand = null;
		int randomNum;
		rand = new Random();
		randomNum = rand.nextInt(300);
//		if (uniqueRandomSet.size() <= 300 && uniqueRandomSet.add(randomNum)) {
//			return randomNum;
//		} else if(uniqueRandomSet.size()>=300) {
//			uniqueRandomSet.clear();
//		}
		return randomNum;
}

	
}
