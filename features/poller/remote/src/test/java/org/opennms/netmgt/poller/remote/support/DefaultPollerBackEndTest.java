/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2006-2015 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2015 The OpenNMS Group, Inc.
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

package org.opennms.netmgt.poller.remote.support;

import static org.junit.Assert.assertEquals;

import java.util.Map;

import org.junit.Test;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.config.pagesequence.Page;
import org.opennms.netmgt.config.pagesequence.PageSequence;
import org.opennms.netmgt.config.poller.Parameter;
import org.opennms.netmgt.config.poller.Service;
import org.springframework.util.SerializationUtils;

import com.google.common.collect.Lists;

public class DefaultPollerBackEndTest {

    /**
     * Verifies that the parameter map generated for a given
     * service is serializable. 
     */
    @Test
    public void canConvertServiceParametersToSerializableMap() {
        Parameter paramWithStringValue = new Parameter();
        paramWithStringValue.setKey("a");
        paramWithStringValue.setValue("test");

        Parameter paramWithNullValue = new Parameter();
        paramWithNullValue.setKey("b");
        paramWithNullValue.setAnyObject(null);

        Parameter paramWithNonSerializableObject = new Parameter();
        paramWithNonSerializableObject.setKey("c");
        // This can be any object, provided that it does not implement the Serializable interface
        paramWithNonSerializableObject.setAnyObject(new DefaultPollerBackEndTest());

        Service svc = new Service();
        svc.setParameters(Lists.newArrayList(paramWithStringValue, paramWithNullValue, paramWithNonSerializableObject));

        // Expect no exception to be thrown
        SerializationUtils.serialize(DefaultPollerBackEnd.getParameterMap(svc));
    }

    /**
     * Verifies that {@link PageSequence} type parameters are marshalled to XML.
     */
    @Test
    public void getParameterMapMarshallsPageSequenceParameters() {
        final PageSequence ps = new PageSequence();

        Page page = new Page();
        page.setMethod("GET");
        page.setHttpVersion("1.1");
        page.setScheme("http");
        page.setHost("${ipaddr}");
        page.setDisableSslVerification("true");
        page.setPort(7080);
        page.setPath("/Login.do");
        page.setSuccessMatch("w00t");
        page.setResponseRange("100-399");
        ps.addPage(page);

        Parameter paramWithPageSequenceValue = new Parameter();
        paramWithPageSequenceValue.setKey("psm");
        paramWithPageSequenceValue.setAnyObject(ps);

        Service svc = new Service();
        svc.setParameters(Lists.newArrayList(paramWithPageSequenceValue));
        Map<String, Object> params = DefaultPollerBackEnd.getParameterMap(svc);

        PageSequence unmarshalledPs = JaxbUtils.unmarshal(PageSequence.class, (String)params.get("psm"));
        assertEquals(ps, unmarshalledPs);
    }
}
