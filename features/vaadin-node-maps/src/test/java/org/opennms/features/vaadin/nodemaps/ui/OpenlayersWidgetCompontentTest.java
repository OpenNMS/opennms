/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2013-2022 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2022 The OpenNMS Group, Inc.
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

package org.opennms.features.vaadin.nodemaps.ui;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.opennms.features.geocoder.GeocoderResult;
import org.opennms.features.geocoder.GeocoderService;
import org.opennms.features.vaadin.nodemaps.internal.NodeMapComponent;
import org.opennms.netmgt.dao.api.AssetRecordDao;
import org.opennms.netmgt.dao.api.NodeDao;
import org.opennms.netmgt.model.OnmsAssetRecord;
import org.opennms.netmgt.model.OnmsGeolocation;
import org.opennms.netmgt.model.OnmsNode;

import com.vaadin.server.PaintTarget;

public class OpenlayersWidgetCompontentTest {
    private NodeDao m_nodeDao;
    private AssetRecordDao m_assetDao;
    private GeocoderService m_geocoder;
    @SuppressWarnings("unused")
    private NodeMapComponent m_component;

    @Before
    public void setUp() {
        m_nodeDao = mock(NodeDao.class);
        m_assetDao = mock(AssetRecordDao.class);
        m_geocoder = mock(GeocoderService.class);
        m_component = new NodeMapComponent();
    }

    @After
    public void tearDown() throws Exception {
        verifyNoMoreInteractions(m_nodeDao);
        verifyNoMoreInteractions(m_assetDao);
        verifyNoMoreInteractions(m_geocoder);
    }

    @Test
    @Ignore
    public void testGeolocation() throws Exception {
        final OnmsNode node = new OnmsNode();
        final OnmsAssetRecord asset = new OnmsAssetRecord();
        final OnmsGeolocation geo = new OnmsGeolocation();
        
        node.setId(1);
        node.setAssetRecord(asset);
        asset.setGeolocation(geo);

        geo.setAddress1("220 Chatham Business Dr.");
        geo.setCity("Pittsboro");
        geo.setState("NC");
        geo.setZip("27312");
        
        assertEquals("220 Chatham Business Dr., Pittsboro, NC 27312", geo.asAddressString());

        when(m_geocoder.resolveAddress(geo.asAddressString())).thenReturn(GeocoderResult.success(geo.asAddressString(), -1.0f, 1.0f).build());
        final PaintTarget target = mock(PaintTarget.class);

        m_assetDao.saveOrUpdate(isA(OnmsAssetRecord.class));
        target.startTag(eq("1"));
        target.addAttribute(eq("longitude"), eq("-1.0"));
        target.addAttribute(eq("latitude"), eq("1.0"));
        target.endTag(eq("1"));

        verify(m_geocoder, times(1)).resolveAddress(geo.asAddressString());
        verify(target, times(1)).startTag("1");
        verify(target, times(1)).addAttribute("longitude", "-1.0");
        verify(target, times(1)).addAttribute("latitude", "1.0");
        verify(target, times(1)).endTag("1");
    }
}
