package org.opennms.netmgt.syslogd;

import java.net.InetAddress;
import java.util.Random;

import org.opennms.core.utils.InetAddressUtils;

import kafka.producer.Partitioner;

public class SyslogdKafkaPartitioner implements Partitioner{

	@Override
	public int partition(Object arg0, int a_numPartitions) {
		
		SyslogConnection syslogConn = (SyslogConnection)arg0;
		InetAddress sourceAddress = syslogConn.getSourceAddress();
        int partition = 0;
		Random rnd = new Random();
		if(sourceAddress==null){
			partition =  rnd.nextInt(255);
		}else{
	        String stringKey = InetAddressUtils.toIpAddrString(sourceAddress);
	        int offset = stringKey.lastIndexOf('.');
	        if (offset > 0) {
	           partition = Integer.parseInt( stringKey.substring(offset+1)) % a_numPartitions;
	        }
		}
		return partition;
	}
	
}
