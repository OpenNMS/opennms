/*******************************************************************************
 * This file is part of OpenNMS(R).
 *
 * Copyright (C) 2016 The OpenNMS Group, Inc.
 * OpenNMS(R) is Copyright (C) 1999-2016 The OpenNMS Group, Inc.
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

package org.opennms.poller.remote;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

import org.apache.commons.io.IOUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.InetAddressUtils;
import org.opennms.core.xml.JaxbUtils;
import org.opennms.netmgt.poller.remote.support.GeodataResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class MetadataUtils {
    private static final Logger LOG = LoggerFactory.getLogger(MetadataUtils.class);

    public static ImageComponent getImageFromURL(final URL url) {
        try {
            return new ImageComponent(ImageIO.read(url));
        } catch (final Exception e) {
            LOG.warn("Unable to ready image: {}", url, e);
            return null;
        }
    }

    public static Map<String,String> fetchGeodata() {
        final Map<String,String> ret = new HashMap<>();
        final String url = "http://freegeoip.net/xml/";

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        final HttpGet get = new HttpGet(url);
        CloseableHttpResponse response = null;

        try {
            response = httpclient.execute(get);

            final HttpEntity entity = response.getEntity();
            final String xml = EntityUtils.toString(entity);
            System.err.println("xml = " + xml);
            final GeodataResponse geoResponse = JaxbUtils.unmarshal(GeodataResponse.class, xml);
            ret.put("external-ip-address", InetAddressUtils.str(geoResponse.getIp()));
            ret.put("country-code", geoResponse.getCountryCode());
            ret.put("region-code", geoResponse.getRegionCode());
            ret.put("city", geoResponse.getCity());
            ret.put("zip-code", geoResponse.getZipCode());
            ret.put("time-zone", geoResponse.getTimeZone());
            ret.put("latitude", geoResponse.getLatitude() == null? null : geoResponse.getLatitude().toString());
            ret.put("longitude", geoResponse.getLongitude() == null? null : geoResponse.getLongitude().toString());
            EntityUtils.consumeQuietly(entity);
        } catch (final Exception e) {
            LOG.debug("Failed to get GeoIP data from " + url, e);
        } finally {
            IOUtils.closeQuietly(response);
        }

        return ret;
    }

    public static class ImageComponent extends JPanel {
        private static final long serialVersionUID = 1L;
        private Image m_image;
        public ImageComponent(final Image image) {
            m_image = image;
            Dimension size = new Dimension(image.getWidth(null), image.getHeight(null));
            setPreferredSize(size);
            setMinimumSize(size);
            setMaximumSize(size);
            setSize(size);
            setLayout(null);
            setBackground(new Color(0,0,0,0));
        }

        @Override protected void paintComponent(final Graphics g) {
            super.paintComponent(g);
            g.drawImage(m_image, 0, 0, null);
        }
    }
}
