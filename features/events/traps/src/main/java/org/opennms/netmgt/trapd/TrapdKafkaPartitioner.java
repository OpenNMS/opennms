package org.opennms.netmgt.trapd;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import kafka.producer.Partitioner;

public class TrapdKafkaPartitioner implements Partitioner{
	private static Set<Integer> uniqueRandomSet=new HashSet<Integer>();
	@Override
	public int partition(Object arg0, int a_numPartitions) {
		
		Random rand = null;
		int randomNum;
		rand = new Random();
		randomNum = rand.nextInt();
		return randomNum;
	}
	
}
