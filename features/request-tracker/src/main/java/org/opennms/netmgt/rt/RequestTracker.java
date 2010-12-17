package org.opennms.netmgt.rt;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.opennms.core.utils.LogUtils;

public class RequestTracker {
    private final String m_baseURL;

    private String m_user;

    private String m_password;

    private int m_timeout;

    private int m_retries;

    private Pattern m_inTokensPattern = Pattern.compile("^(\\w+):\\s*(.*?)\\s*$", Pattern.MULTILINE);

    private Pattern m_ticketCreatedPattern = Pattern.compile("(?s) Ticket (\\d+) created");

    private Pattern m_ticketUpdatedPattern = Pattern.compile("(?s) Ticket (\\d+) updated");

    private DefaultHttpClient m_client;

    public RequestTracker(final String baseURL, final String username, final String password, int timeout, int retries) {
        m_baseURL = baseURL;
        m_user = username;
        m_password = password;
        m_timeout = timeout;
        m_retries = retries;
    }

    public Long createTicket(final RTTicket ticket) throws RequestTrackerException {
    	final HttpPost post = new HttpPost(m_baseURL + "/REST/1.0/edit");
        return postEdit(post, ticket.toContent(), m_ticketCreatedPattern);
    }

    public Long updateTicket(final Long id, final String content) throws RequestTrackerException {
    	HttpPost post = new HttpPost(m_baseURL + "/REST/1.0/ticket/" + id + "/edit");
        return postEdit(post, content, m_ticketUpdatedPattern);
    }

    public Long postEdit(final HttpPost post, final String content, final Pattern pattern) throws RequestTrackerException {
        String rtTicketNumber = null;

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("user", m_user));
        params.add(new BasicNameValuePair("pass", m_password));
        params.add(new BasicNameValuePair("content", content));

		try {
			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
			post.setEntity(entity);
		} catch (final UnsupportedEncodingException e) {
			// Should never happen
			LogUtils.warnf(this, e, "unsupported encoding exception for UTF-8 -- WTF?!");
		}

        try {
        	final HttpResponse response = getClient().execute(post);
            int responseCode = response.getStatusLine().getStatusCode();
			if (responseCode != HttpStatus.SC_OK) {
                throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
            } else {
                final String in = EntityUtils.toString(response.getEntity());
                final Matcher matcher = pattern.matcher(in);
                if (matcher.find()) {
                    rtTicketNumber = matcher.group(1);
                } else {
                    LogUtils.debugf(this, "did not get ticket ID from response when posting to %s", post.toString());
                }
            }
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "Failure attempting to update ticket.");
            throw new RequestTrackerException(e);
        }

        if (rtTicketNumber == null) {
            return null;
        }

        return Long.valueOf(rtTicketNumber);
    }

    public RTUser getUserInfo(final String username) {
        getSession();

        Map<String, String> attributes = Collections.emptyMap();

        final HttpGet get = new HttpGet(m_baseURL + "/REST/1.0/user/" + username);

        try {
        	final HttpResponse response = getClient().execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
            } else {
            	if (response.getEntity() != null) {
            		attributes = parseResponseStream(response.getEntity().getContent());
            	}
            }
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "An exception occurred while getting user info for " + username);
            return null;
        }

        final String id = attributes.get("id");
        final String realname = attributes.get("realname");
        final String email = attributes.get("emailaddress");
        
        if (id == null || "".equals(id)) {
            LogUtils.errorf(this, "Unable to retrieve ID from user info.");
            return null;
        }
        return new RTUser(Long.parseLong(id.replace("user/", "")), username, realname, email);
    }

    public RTTicket getTicket(final Long ticketId, boolean getTextAttachment) throws RequestTrackerException {
        getSession();

        Map<String, String> attributes = getTicketAttributes(ticketId.toString());

        RTTicket ticket = new RTTicket();
        if (attributes == null)
            throw new RequestTrackerException("received no ticket attributes back from RT");
        final String id = attributes.remove("id").replace("ticket/", "");
        if (id != null && id.length() > 0) {
            ticket.setId(Long.valueOf(id));
        }
        ticket.setQueue(attributes.remove("queue"));
        ticket.setCreated(attributes.remove("created"));
        ticket.setSubject(attributes.remove("subject"));
        ticket.setText(attributes.remove("text"));
        ticket.setStatus(attributes.remove("status"));

        if (attributes.containsKey("requestors")) {
            for (final String requestor : attributes.remove("requestors").split("\\s*,\\s*")) {
                ticket.addRequestor(requestor);
            }
        } else if (attributes.containsKey("requestor")) {
            ticket.setRequestor(attributes.remove("requestor"));
        }
        if (LogUtils.isTraceEnabled(this)) {
            if (attributes.size() > 0) {
                LogUtils.tracef(this, "unhandled RT ticket attributes: %s", attributes.keySet().toString());
            }
        }

        if (ticket.getText() == null || ticket.getText().equals("") && getTextAttachment) {
            attributes = getTicketAttributes(ticketId + "/attachments");
            if (attributes.containsKey("attachments")) {
                final Matcher matcher = m_inTokensPattern.matcher(attributes.get("attachments"));
                matcher.find();
                final String attachmentId = matcher.group(1);
                if (attachmentId != null && !"".equals(attachmentId)) {
                    attributes = getTicketAttributes(ticketId + "/attachments/" + attachmentId);
                    if (attributes.containsKey("content")) {
                        ticket.setText(attributes.remove("content"));
                    }
                }
                LogUtils.debugf(this, "attachment ID = %s", attachmentId);
            }
        }
        return ticket;
    }

    public List<RTTicket> getTicketsForQueue(final String queueName, long limit) {
    	getSession();

        final List<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair("query", "Queue='" + queueName + "' AND Status='open'"));
        params.add(new BasicNameValuePair("format", "i"));
        params.add(new BasicNameValuePair("orderby", "-id"));
        final HttpGet get = new HttpGet(m_baseURL + "/REST/1.0/search/ticket?" + URLEncodedUtils.format(params, "UTF-8"));

        final List<RTTicket> tickets = new ArrayList<RTTicket>();
        final List<Long> ticketIds = new ArrayList<Long>();

        try {
        	final HttpResponse response = getClient().execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
            } else {
                InputStreamReader isr = null;
                BufferedReader br = null;
                try {
                	if (response.getEntity() == null) return null;
                    isr = new InputStreamReader(response.getEntity().getContent());
                    br = new BufferedReader(isr);
                    String line = null;
                    do {
                        line = br.readLine();
                        if (line != null ) {
                        	if (line.contains("does not exist.")) {
                        		return null;
                        	}
	                        if (line.startsWith("ticket/")) {
	                            ticketIds.add(Long.parseLong(line.replace("ticket/", "")));
	                        }
                        }
                    } while (line != null);
                } catch (final Exception e) {
                    throw new RequestTrackerException("Unable to read ticket IDs from query.", e);
                } finally {
                    IOUtils.closeQuietly(br);
                    IOUtils.closeQuietly(isr);
                }
            }
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "An exception occurred while getting tickets for queue " + queueName);
            return null;
        }

        for (final Long id : ticketIds) {
            try {
                tickets.add(getTicket(id, false));
            } catch (final RequestTrackerException e) {
                LogUtils.warnf(this, e, "Unable to retrieve ticket.");
            }
        }

        return tickets;
    }

    public RTQueue getFirstPublicQueueForUser(final String username) throws RequestTrackerException {
        if (username == null) {
            LogUtils.errorf(this, "User name cannot be null.");
            throw new RequestTrackerException("User name cannot be null.");
        }

        for (final RTQueue queue : getQueuesForUser(username)) {
            if (queue.isAccessible() && !queue.getName().startsWith("___")) return queue;
        }
        
        return null;
    }

    public List<RTQueue> getQueuesForUser(final String username) throws RequestTrackerException {
        if (username == null) {
            LogUtils.errorf(this, "User name cannot be null.");
            throw new RequestTrackerException("User name cannot be null.");
        }

        getSession();

        final List<RTQueue> queues = new ArrayList<RTQueue>();

        long id = 1;
        RTQueue queue = null;

        while (true) {
            queue = getQueue(id);

            if (queue == null) {
                break;
            }
            if (queue.isAccessible() && queue.getName().startsWith("___")) {
                LogUtils.debugf(this, "found queue: %s (skipping)", queue);
            } else {
                LogUtils.debugf(this, "found queue: %s", queue);
                queues.add(queue);
            }
            id++;
        }

        return queues;
    }

    public RTQueue getQueue(long id) throws RequestTrackerException {
        getSession();

        Map<String, String> attributes = Collections.emptyMap();

        final HttpGet get = new HttpGet(m_baseURL + "/REST/1.0/queue/" + id);

        try {
        	final HttpResponse response = getClient().execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
            } else {
            	if (response.getEntity() == null) {
            		LogUtils.debugf(this, "no entity returned by HTTP client");
            	}
                attributes = parseResponseStream(response.getEntity().getContent());
            }
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "An exception occurred while getting queue #" + id);
            return null;
        }

        if (attributes.containsKey("id") && attributes.containsKey("name")) {
            final String queueId = attributes.get("id").replace("queue/", "");
            final long longId = Long.parseLong(queueId);

            final String name = attributes.get("name").trim();
            final String priority = attributes.get("finalpriority").trim();
            LogUtils.debugf(this, "name = %s, priority = %s", name, priority);
            if ("".equals(name) && "".equals(priority)) {
                LogUtils.debugf(this, "We got a response back, but it had no name or priority; assuming we have no access to this queue.");
                return new RTInaccessibleQueue(longId);
            }
            return new RTQueue(longId, attributes.get("name"));
        } else {
            LogUtils.debugf(this, "id or name missing (%d, %s)", attributes.get("id"), attributes.get("name"));
            return null;
        }
    }

    private Map<String, String> getTicketAttributes(final String ticketQuery) throws RequestTrackerException {
        // don't try to get ticket if it's marked as not available

        if (ticketQuery == null) {

            LogUtils.errorf(this, "No ticket query specified!");
            throw new RequestTrackerException("No ticket query specified!");

        }

        getSession();

        Map<String,String> ticketAttributes = Collections.emptyMap();
        final HttpGet get = new HttpGet(m_baseURL + "/REST/1.0/ticket/" + ticketQuery);

        try {
        	final HttpResponse response = getClient().execute(get);
            int responseCode = response.getStatusLine().getStatusCode();
            if (responseCode != HttpStatus.SC_OK) {
                throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
            } else {
            	if (response.getEntity() == null) {
            		LogUtils.debugf(this, "no entity returned by HTTP client");
            	}
                ticketAttributes = parseResponseStream(response.getEntity().getContent());
            }
        } catch (final Exception e) {
            LogUtils.errorf(this, e, "HTTP exception attempting to get ticket.");
        }

        if (ticketAttributes.size() == 0) {
            LogUtils.debugf(this, "matcher did not match %s", m_inTokensPattern.pattern());
            return null;
        }
        return ticketAttributes;
    }

    @SuppressWarnings("unchecked")
    protected Map<String,String> parseResponseStream(final InputStream responseStream) throws IOException {
        final Map<String,String> ticketAttributes = new HashMap<String,String>();
        
        LogUtils.debugf(this, "parsing response");
        String lastIndent = "";
        String lastKey = null;
        for (final String line : (List<String>)IOUtils.readLines(responseStream)) {
            LogUtils.tracef(this, "line = %s", line);
            if (line.contains("does not exist.")) {
                return ticketAttributes;
            } if (lastIndent.length() > 0 && line.startsWith(lastIndent)) {
                final String value = ticketAttributes.get(lastKey) + "\n" + line.replaceFirst("^" + lastIndent, "");
                ticketAttributes.put(lastKey, value);
            } else {
                final Matcher matcher = m_inTokensPattern.matcher(line);
                if (matcher.matches()) {
                    lastKey = matcher.group(1).toLowerCase();
                    lastIndent = lastKey.replaceAll(".", " ") + "  ";
                    ticketAttributes.put(lastKey, matcher.group(2));
                }
            }
            
        }
        return ticketAttributes;
    }

    private void getSession() {
        if (m_client == null) {
            // we need to log in at least once with a POST method before we can do any GETs so we get a session cookie
        	
            final HttpPost post = new HttpPost(m_baseURL + "/REST/1.0/user/" + m_user);
            final List<NameValuePair> params = new ArrayList<NameValuePair>();
            params.add(new BasicNameValuePair("user", m_user));
            params.add(new BasicNameValuePair("pass", m_password));

    		try {
    			UrlEncodedFormEntity entity = new UrlEncodedFormEntity(params, "UTF-8");
    			post.setEntity(entity);
    		} catch (final UnsupportedEncodingException e) {
    			// Should never happen
    			LogUtils.warnf(this, e, "unsupported encoding exception for UTF-8 -- WTF?!");
    		}

            try {
            	final HttpResponse response = getClient().execute(post);
                int responseCode = response.getStatusLine().getStatusCode();
                if (responseCode != HttpStatus.SC_OK) {
                    throw new RequestTrackerException("Received a non-200 response code from the server: " + responseCode);
                } else {
                	if (response.getEntity() != null) {
                		response.getEntity().consumeContent();
                	}
                    LogUtils.warnf(this, "got user session for username: %s", m_user);
                }
            } catch (final Exception e) {
                LogUtils.warnf(this, e, "Unable to get session (by requesting user details)");
            }
        }
    }

    public synchronized HttpClient getClient() {
		if (m_client == null) {
			m_client = new DefaultHttpClient();

			HttpParams clientParams = m_client.getParams();
			clientParams.setIntParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, m_timeout);
			clientParams.setIntParameter(CoreConnectionPNames.SO_TIMEOUT, m_timeout);
			clientParams.setParameter(ClientPNames.COOKIE_POLICY, CookiePolicy.BROWSER_COMPATIBILITY);
			m_client.setParams(clientParams);

			m_client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(m_retries, false));

		}

		return m_client;
    }

    public synchronized void setClient(final DefaultHttpClient client) {
        m_client = client;
    }

    public void setUser(final String user) {
        m_user = user;
    }

    public void setPassword(final String password) {
        m_password = password;
    }
    
    public String toString() {
        return new ToStringBuilder(this)
            .append("base-url", m_baseURL)
            .append("username", m_user)
            .append("password", m_password.replaceAll(".", "*"))
            .append("timeout", m_timeout)
            .append("retries", m_retries)
            .toString();
    }

    public String getUsername() {
        return m_user;
    }

}
