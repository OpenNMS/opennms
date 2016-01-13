package org.opennms.poller.remote;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class GeodataFetcher {
    private static final Logger LOG = LoggerFactory.getLogger(GeodataFetcher.class);

    public Map<String,String> fetchGeodata() {
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
        } catch (IOException e) {
            LOG.debug("Failed to get GeoIP data from " + url, e);
        } finally {
            IOUtils.closeQuietly(response);
        }

        return ret;
    }

}
