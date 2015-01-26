package org.opennms.web.rest;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.transaction.annotation.Transactional;

import com.sun.jersey.spi.resource.PerRequest;

@PerRequest
@Scope("prototype")
@Path("info")
@Transactional
public class InfoRestService extends OnmsRestService {
    private static final Logger LOG = LoggerFactory.getLogger(InfoRestService.class);

    private static final String m_displayVersion = System.getProperty("version.display");
    private static final String m_version;
    static {
        final Pattern versionPattern = Pattern.compile("^(\\d+\\.\\d+\\.\\d+).*?$");
        final Matcher m = versionPattern.matcher(m_displayVersion);
        if (m.matches()) {
            m_version = m.group(1);
        } else {
            m_version = m_displayVersion;
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getInfo() throws ParseException {
        final Map<String,String> info = new HashMap<String,String>();
        info.put("displayVersion", m_displayVersion);
        info.put("version", m_version);

        final InputStream installerProperties = getClass().getResourceAsStream("/installer.properties");
        if (installerProperties != null) {
            final Properties props = new Properties();
            try {
                props.load(installerProperties);
                installerProperties.close();
                info.put("packageName", (String)props.get("install.package.name"));
                info.put("packageDescription", (String)props.get("install.package.description"));
            } catch (final IOException e) {
                LOG.debug("Unable to read from installer.properties in the classpath.", e);
            }
        }

        final JSONObject jo = new JSONObject(info);
        return Response.ok(jo.toString(), MediaType.APPLICATION_JSON).build();
    }
}
