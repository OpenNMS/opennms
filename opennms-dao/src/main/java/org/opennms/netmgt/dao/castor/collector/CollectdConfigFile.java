/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2013 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2013 The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is a registered trademark of The OpenNMS Group, Inc.
 *
 * OpenNMS(R) is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published
 * by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.
 *
 * OpenNMS(R) is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with OpenNMS(R).  If not, see:
 *      http://www.gnu.org/licenses/
 *
 * For more information contact:
 *     OpenNMS(R) Licensing <license@opennms.org>
 *     http://www.opennms.org/
 *     http://www.opennms.com/
 *******************************************************************************/

package org.opennms.netmgt.dao.castor.collector;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;

import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;
import org.opennms.core.xml.CastorUtils;
import org.opennms.netmgt.config.collectd.CollectdConfiguration;
import org.opennms.netmgt.config.collectd.Collector;
import org.springframework.core.io.FileSystemResource;

public class CollectdConfigFile {
    
    private static final Logger LOG = LoggerFactory.getLogger(CollectdConfigFile.class);
	
	File m_file;
	
	/**
	 * <p>Constructor for CollectdConfigFile.</p>
	 *
	 * @param file a {@link java.io.File} object.
	 */
	public CollectdConfigFile(File file) {
		m_file = file;
	}
	
	/**
	 * <p>visit</p>
	 *
	 * @param visitor a {@link org.opennms.netmgt.dao.castor.collector.CollectdConfigVisitor} object.
	 */
	public void visit(CollectdConfigVisitor visitor) {
        CollectdConfiguration collectdConfiguration = getCollectdConfiguration();
        visitor.visitCollectdConfiguration(collectdConfiguration);
        
        for (Iterator<Collector> it = collectdConfiguration.getCollectorCollection().iterator(); it.hasNext();) {
            Collector collector = it.next();
            doVisit(collector, visitor);
        }
        visitor.completeCollectdConfiguration(collectdConfiguration);
    }
	
	private void doVisit(Collector collector, CollectdConfigVisitor visitor) {
        visitor.visitCollectorCollection(collector);
        visitor.completeCollectorCollection(collector);
    }

    private CollectdConfiguration getCollectdConfiguration() {
		try {
			return CastorUtils.unmarshal(CollectdConfiguration.class, new FileSystemResource(m_file));
		} catch (MarshalException e) {
			throw runtimeException("Syntax error in "+m_file, e);
		} catch (ValidationException e) {
			throw runtimeException("invalid attribute in "+m_file, e);
        } catch (FileNotFoundException e) {
            throw runtimeException("Unable to find file "+m_file, e);
        } catch (IOException e) {
            throw runtimeException("Unable to find access "+m_file, e);
		}
	}

	private RuntimeException runtimeException(String msg, Exception e) {
		LOG.error(msg, e);
		return new RuntimeException(msg, e);
	}
}
