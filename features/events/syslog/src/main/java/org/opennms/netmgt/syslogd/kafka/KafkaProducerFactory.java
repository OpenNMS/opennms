/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2012-2014 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2014 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.syslogd.kafka;

import org.springframework.beans.factory.InitializingBean;

/**
 * This factory constructs {@link KafkaCustomProducer} instances.
 */
public class KafkaProducerFactory implements InitializingBean {

    /**
     * The constructor
     */
    public KafkaProducerFactory() {
    }
    
	private String m_kafkaAddress;
	private String m_kafkaTopic;

    public KafkaCustomProducer getInstance() {
    	KafkaCustomProducer kafkaProducer = new KafkaCustomProducer();
        return kafkaProducer;
    }

	@Override
	public void afterPropertiesSet() throws Exception {
		// TODO Auto-generated method stub
		
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
