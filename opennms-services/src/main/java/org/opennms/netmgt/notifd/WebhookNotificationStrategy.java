/*
 * Licensed to The OpenNMS Group, Inc (TOG) under one or more
 * contributor license agreements.  See the LICENSE.md file
 * distributed with this work for additional information
 * regarding copyright ownership.
 *
 * TOG licenses this file to You under the GNU Affero General
 * Public License Version 3 (the "License") or (at your option)
 * any later version.  You may not use this file except in
 * compliance with the License.  You may obtain a copy of the
 * License at:
 *
 *      https://www.gnu.org/licenses/agpl-3.0.txt
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied.  See the License for the specific
 * language governing permissions and limitations under the
 * License.
 */
package org.opennms.netmgt.notifd;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.StringEntity;
import org.apache.http.util.EntityUtils;
import org.opennms.core.web.HttpClientWrapper;
import org.opennms.netmgt.config.NotificationManager;
import org.opennms.netmgt.model.notifd.Argument;
import org.opennms.netmgt.model.notifd.NotificationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;

/**
 * Send a notification to an arbitrary HTTP webhook.
 *
 * The request method, content type, headers, and body are all supplied by the
 * notification command, so a single strategy can target Slack, Mattermost,
 * Microsoft Teams, Discord, and most other webhook receivers.
 *
 * Each setting is resolved from the notification parameter matching its switch,
 * then the switch's {@code <substitution>} literal, then a system property.
 */
public class WebhookNotificationStrategy implements NotificationStrategy {

    private static final Logger LOG = LoggerFactory.getLogger(WebhookNotificationStrategy.class);

    // Trailing tokens are rejected so that a template rendering to something like
    // {"text": "x"} oops is caught here instead of as a 400 from the receiver.
    private static final ObjectMapper JSON_MAPPER = JsonMapper.builder()
            .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
            .build();

    protected static final String SWITCH_NAME = "-name";
    protected static final String SWITCH_URL = "-url";
    protected static final String SWITCH_METHOD = "-method";
    protected static final String SWITCH_CONTENT_TYPE = "-content-type";
    protected static final String SWITCH_BODY = "-body";
    protected static final String SWITCH_SUCCESS_MATCH = "-success-match";
    protected static final String SWITCH_CONNECT_TIMEOUT = "-connect-timeout";
    protected static final String SWITCH_SOCKET_TIMEOUT = "-socket-timeout";
    protected static final String SWITCH_USE_SYSTEM_PROXY = "-useSystemProxy";
    protected static final String HEADER_SWITCH_PREFIX = "-header-";

    protected static final String PROPERTY_PREFIX = "org.opennms.netmgt.notifd.webhook.";

    private static final String DEFAULT_METHOD = "POST";
    private static final String DEFAULT_CONTENT_TYPE = "application/json";
    private static final int DEFAULT_TIMEOUT_MS = 3000;

    /**
     * Matches {@code ${name}} and {@code ${name|raw}}. The name deliberately excludes
     * colons so that a metadata token such as ${scv:webhook:token} is left untouched:
     * those are resolved by notifd before the strategy runs, but only when the value
     * came from a notification parameter rather than a substitution literal.
     */
    private static final Pattern TEMPLATE_TOKEN = Pattern.compile("\\$\\{([^{}:|]+?)(?:\\|([^{}|]*))?\\}");

    /** Friendlier names for the two cryptic message switches. */
    private static final Map<String, String> TEMPLATE_ALIASES = Map.of(
            "textMessage", NotificationManager.PARAM_TEXT_MSG,
            "numericMessage", NotificationManager.PARAM_NUM_MSG);

    private static final Map<String, String> SWITCH_PROPERTIES = Map.of(
            SWITCH_URL, "url",
            SWITCH_METHOD, "method",
            SWITCH_CONTENT_TYPE, "contentType",
            SWITCH_BODY, "body",
            SWITCH_SUCCESS_MATCH, "successMatch",
            SWITCH_CONNECT_TIMEOUT, "connectTimeout",
            SWITCH_SOCKET_TIMEOUT, "socketTimeout",
            SWITCH_USE_SYSTEM_PROXY, "useSystemProxy");

    private List<Argument> m_arguments = new ArrayList<>();

    /** {@inheritDoc} */
    @Override
    public int send(List<Argument> arguments) {
        setArguments(arguments);

        final String url = getValue(SWITCH_URL);
        if (url == null) {
            LOG.error("send: no webhook URL. Set the {} switch on the notification command or the {}url system property.",
                    SWITCH_URL, PROPERTY_PREFIX);
            return 1;
        }

        final String method = orDefault(getValue(SWITCH_METHOD), DEFAULT_METHOD).toUpperCase(Locale.ROOT);
        final String contentType = orDefault(getValue(SWITCH_CONTENT_TYPE), DEFAULT_CONTENT_TYPE);
        final boolean json = isJson(contentType);
        final String body = renderTemplate(getValue(SWITCH_BODY), json);

        if (json && body != null && !isWellFormedJson(body)) {
            LOG.error("send: the rendered {} is not valid JSON, so it was not sent. Check the template for unquoted "
                    + "substitutions or a stray comma. Rendered body: {}", SWITCH_BODY, body);
            return 1;
        }

        final HttpUriRequest request;
        try {
            request = buildRequest(method, url, body, contentType);
        } catch (IllegalArgumentException e) {
            LOG.error("send: cannot build the webhook request", e);
            return 1;
        }

        for (final Map.Entry<String, String> header : getHeaders().entrySet()) {
            request.setHeader(header.getKey(), header.getValue());
        }

        final HttpClientWrapper clientWrapper = HttpClientWrapper.create()
                .setConnectionTimeout(getTimeout(SWITCH_CONNECT_TIMEOUT))
                .setSocketTimeout(getTimeout(SWITCH_SOCKET_TIMEOUT));
        if (getUseSystemProxy()) {
            clientWrapper.useSystemProxySettings();
        }

        int statusCode;
        String contents;
        try {
            final CloseableHttpResponse response = clientWrapper.execute(request);
            statusCode = response.getStatusLine().getStatusCode();
            contents = response.getEntity() == null ? "" : EntityUtils.toString(response.getEntity());
            LOG.debug("send: {} {} returned {} with body: {}", method, redactUrl(url), statusCode, contents);
        } catch (IOException e) {
            LOG.error("send: I/O problem posting to webhook at {}", redactUrl(url), e);
            return 1;
        } finally {
            IOUtils.closeQuietly(clientWrapper);
        }

        return isSuccess(statusCode, contents) ? 0 : 1;
    }

    protected void setArguments(List<Argument> arguments) {
        m_arguments = arguments == null ? new ArrayList<>() : new ArrayList<>(arguments);
    }

    private HttpUriRequest buildRequest(final String method, final String url, final String body, final String contentType) {
        switch (method) {
            case "GET":
                warnIfBodyIgnored(method, body);
                return new HttpGet(url);
            case "DELETE":
                warnIfBodyIgnored(method, body);
                return new HttpDelete(url);
            case "POST":
                return withBody(new HttpPost(url), body, contentType);
            case "PUT":
                return withBody(new HttpPut(url), body, contentType);
            case "PATCH":
                return withBody(new HttpPatch(url), body, contentType);
            default:
                throw new IllegalArgumentException("Unsupported HTTP method '" + method + "'");
        }
    }

    private HttpUriRequest withBody(final HttpEntityEnclosingRequestBase request, final String body, final String contentType) {
        if (body == null) {
            LOG.warn("send: no {} specified for a {} request; sending an empty body", SWITCH_BODY, request.getMethod());
        } else {
            request.setEntity(new StringEntity(body, java.nio.charset.StandardCharsets.UTF_8));
        }
        // Set the header rather than using ContentType on the entity: some receivers
        // (Mattermost among them) reject a Content-Type that carries a charset.
        request.setHeader("Content-Type", contentType);
        return request;
    }

    private void warnIfBodyIgnored(final String method, final String body) {
        if (body != null) {
            LOG.warn("send: a {} was specified but {} requests do not carry one; ignoring it", SWITCH_BODY, method);
        }
    }

    /**
     * Substitutes {@code ${name}} tokens with the value of the matching notification
     * command switch, escaping each value when the payload is JSON unless the token
     * asked for {@code |raw}.
     */
    protected String renderTemplate(final String template, final boolean escapeJson) {
        if (template == null) {
            return null;
        }

        final Matcher matcher = TEMPLATE_TOKEN.matcher(template);
        final StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            final String name = matcher.group(1).trim();
            final String modifier = matcher.group(2);
            final boolean raw = "raw".equals(modifier);
            if (modifier != null && !raw) {
                LOG.warn("renderTemplate: ignoring unknown modifier '{}' on '{}'; the only modifier is 'raw'", modifier, name);
            }

            String value = lookupTemplateValue(name);
            if (value == null) {
                LOG.debug("renderTemplate: no value for '{}'; the notification command must declare a matching argument", name);
                value = "";
            }

            matcher.appendReplacement(out, Matcher.quoteReplacement(escapeJson && !raw ? escapeJson(value) : value));
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private String lookupTemplateValue(final String name) {
        final String alias = TEMPLATE_ALIASES.get(name);
        if (alias != null) {
            return getSwitchValue(alias);
        }
        // Most switches are dash-prefixed, but a few notifd parameters (noticeid,
        // eventID, eventUEI) are not, so try both spellings.
        final String dashed = getSwitchValue("-" + name);
        return dashed != null ? dashed : getSwitchValue(name);
    }

    /** Escapes a string for inclusion inside a JSON string literal. */
    protected static String escapeJson(final String value) {
        return new String(JsonStringEncoder.getInstance().quoteAsString(value));
    }

    /** Headers declared as -header-Name switches, in declaration order. */
    protected Map<String, String> getHeaders() {
        final Map<String, String> headers = new LinkedHashMap<>();
        for (final Argument arg : m_arguments) {
            final String argSwitch = arg.getSwitch();
            if (argSwitch == null || !argSwitch.startsWith(HEADER_SWITCH_PREFIX)) {
                continue;
            }
            final String name = argSwitch.substring(HEADER_SWITCH_PREFIX.length());
            if (name.isEmpty()) {
                LOG.warn("getHeaders: ignoring '{}' switch with no header name", argSwitch);
                continue;
            }
            final String value = getValue(argSwitch);
            if (value == null) {
                LOG.warn("getHeaders: ignoring header '{}' because it has no value", name);
                continue;
            }
            headers.put(name, value);
        }
        return headers;
    }

    /**
     * A 2xx status is required. When -success-match is set, the response body must
     * also match it, which covers receivers that report failure with a 200.
     */
    protected boolean isSuccess(final int statusCode, final String contents) {
        if (statusCode < 200 || statusCode > 299) {
            LOG.error("Webhook returned status {} with body: {}", statusCode, contents);
            return false;
        }

        final String successMatch = getValue(SWITCH_SUCCESS_MATCH);
        if (successMatch == null) {
            return true;
        }

        try {
            if (Pattern.compile(successMatch).matcher(orDefault(contents, "")).find()) {
                return true;
            }
            LOG.error("Webhook returned status {} but the body did not match '{}': {}", statusCode, successMatch, contents);
        } catch (java.util.regex.PatternSyntaxException e) {
            LOG.error("Invalid {} expression '{}'; treating the notification as failed", SWITCH_SUCCESS_MATCH, successMatch, e);
        }
        return false;
    }

    private int getTimeout(final String switchName) {
        final String value = getValue(switchName);
        if (value == null) {
            return DEFAULT_TIMEOUT_MS;
        }
        try {
            final int timeout = Integer.parseInt(value.trim());
            if (timeout < 0) {
                LOG.warn("Negative {} value '{}'; using {}ms", switchName, value, DEFAULT_TIMEOUT_MS);
                return DEFAULT_TIMEOUT_MS;
            }
            return timeout;
        } catch (NumberFormatException e) {
            LOG.warn("Invalid {} value '{}'; using {}ms", switchName, value, DEFAULT_TIMEOUT_MS);
            return DEFAULT_TIMEOUT_MS;
        }
    }

    protected boolean getUseSystemProxy() {
        return Boolean.parseBoolean(getValue(SWITCH_USE_SYSTEM_PROXY));
    }

    /**
     * Resolves a switch from the notification parameter, then the substitution
     * literal, then a system property. Returns null when none of them has a value.
     */
    protected String getValue(final String switchName) {
        final String fromSwitch = getSwitchValue(switchName);
        if (fromSwitch != null) {
            return fromSwitch;
        }
        return getSystemProperty(switchName);
    }

    private String getSystemProperty(final String switchName) {
        final String suffix = switchName.startsWith(HEADER_SWITCH_PREFIX)
                ? "header." + switchName.substring(HEADER_SWITCH_PREFIX.length())
                : SWITCH_PROPERTIES.get(switchName);
        if (suffix == null) {
            return null;
        }

        // An optional -name lets several webhook commands coexist with their own
        // properties, falling back to the unqualified property.
        final String instance = getSwitchValue(SWITCH_NAME);
        if (instance != null) {
            final String qualified = System.getProperty(PROPERTY_PREFIX + instance + "." + suffix);
            if (qualified != null) {
                return qualified;
            }
        }
        return System.getProperty(PROPERTY_PREFIX + suffix);
    }

    /** The argument's value, falling back to its substitution literal. Empty is treated as absent. */
    private String getSwitchValue(final String switchName) {
        for (final Argument arg : m_arguments) {
            if (!switchName.equals(arg.getSwitch())) {
                continue;
            }
            if (arg.getValue() != null && !arg.getValue().isEmpty()) {
                return arg.getValue();
            }
            if (arg.getSubstitution() != null && !arg.getSubstitution().isEmpty()) {
                return arg.getSubstitution();
            }
        }
        return null;
    }

    private static boolean isJson(final String contentType) {
        return contentType.toLowerCase(Locale.ROOT).contains("json");
    }

    /** Catches template mistakes here rather than as an opaque 400 from the receiver. */
    protected static boolean isWellFormedJson(final String body) {
        // readTree maps blank input to a MissingNode rather than failing.
        if (body == null || body.trim().isEmpty()) {
            return false;
        }
        try {
            JSON_MAPPER.readTree(body);
            return true;
        } catch (com.fasterxml.jackson.core.JacksonException e) {
            LOG.debug("isWellFormedJson: rendered body did not parse", e);
            return false;
        }
    }

    /**
     * Webhook URLs carry their own credential in the path or query, so only the
     * scheme and host are safe to log.
     */
    protected static String redactUrl(final String url) {
        try {
            final URI uri = new URI(url);
            if (uri.getHost() == null) {
                return "(unparseable URL)";
            }
            return uri.getScheme() + "://" + uri.getHost() + (uri.getPort() == -1 ? "" : ":" + uri.getPort());
        } catch (URISyntaxException e) {
            return "(unparseable URL)";
        }
    }

    private static String orDefault(final String value, final String fallback) {
        return value == null ? fallback : value;
    }
}
