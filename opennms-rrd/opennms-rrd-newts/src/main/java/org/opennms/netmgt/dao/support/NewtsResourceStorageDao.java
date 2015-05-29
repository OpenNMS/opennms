package org.opennms.netmgt.dao.support;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.opennms.netmgt.dao.api.ResourceStorageDao;
import org.opennms.netmgt.model.OnmsAttribute;
import org.opennms.netmgt.model.ResourcePath;
import org.opennms.netmgt.model.RrdGraphAttribute;
import org.opennms.netmgt.model.StringPropertyAttribute;
import org.opennms.netmgt.rrd.newts.NewtsUtils;
import org.opennms.newts.api.search.BooleanQuery;
import org.opennms.newts.api.search.Operator;
import org.opennms.newts.api.search.SearchResults;
import org.opennms.newts.api.search.SearchResults.Result;
import org.opennms.newts.api.search.Searcher;
import org.opennms.newts.api.search.Term;
import org.opennms.newts.api.search.TermQuery;
import org.opennms.newts.cassandra.CassandraSession;
import org.opennms.newts.cassandra.search.CassandraSearcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

/**
 * Resource Storage Dao implementation for Newts that leverages
 * the Search API for walking the resource tree.
 *
 * In Newts, samples are associated with metrics, which are in
 * turn associated with resource.
 *
 * Here we split the resource id into two parts:
 *   bucket: last element of the resource id
 *   resource path: all the elements before the bucket
 * 
 * Relating this to .rrd file on disk, the bucket would
 * be the filename, and the resource path would be its folder.
 *
 * TODO:
 *  * Leverage the cache for calls to exist()
 *
 * @author jwhite
 */
public class NewtsResourceStorageDao implements ResourceStorageDao {

    private static final Logger LOG = LoggerFactory.getLogger(NewtsResourceStorageDao.class);

    private CassandraSession m_session = null;

    private CassandraSearcher m_searcher = null;

    private MetricRegistry m_registry = new MetricRegistry();

    private static final int INFINITE_DEPTH = -1;
    
    @Override
    public boolean exists(ResourcePath path) {
        return exists(path, INFINITE_DEPTH);
    }

    @Override
    public boolean exists(ResourcePath path, int depth) {
        return searchFor(path, depth).size() > 0;
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path) {
        return children(path, INFINITE_DEPTH);
    }

    @Override
    public Set<ResourcePath> children(ResourcePath path, int depth) {
        Set<ResourcePath> matches = Sets.newTreeSet();
        
        List<Result> results = searchFor(path, depth);

        for (Result res : results) {
            ResourcePath child = toChildResourcePath(path, res.getResource().getId());
            if (child == null) {
                continue;
            }
            matches.add(child);
        }

        return matches;
    }

    @Override
    public Set<OnmsAttribute> getAttributes(ResourcePath path) {
        Set<OnmsAttribute> attributes =  Sets.newHashSet();

        List<Result> results = searchFor(path, 0);

        for (Result result : results) {
            final String resourceId = result.getResource().getId();
            final ResourcePath resultPath = toResourcePath(resourceId);
            if (!path.equals(resultPath)) {
                continue;
            }

            for (String metric : result.getMetrics()) {
                // Use the metric name as the dsName
                // Store the resource id in the rrdFile field
                attributes.add(new RrdGraphAttribute(metric, "", resourceId));
            }

            final Map<String, String> resourceAttributes = result.getResource().getAttributes().orNull();
            if (resourceAttributes != null) {
                for (Entry<String, String> entry : resourceAttributes.entrySet()) {
                    attributes.add(new StringPropertyAttribute(entry.getKey(), entry.getValue()));
                }
            }
        }

        return attributes;
    }

    @Override
    public boolean delete(ResourcePath path) {
        return false;
    }

    @Override
    public Map<String, String> getMetaData(ResourcePath path) {
        return null;
    }

    private List<Result> searchFor(ResourcePath path, int depth) {
        BooleanQuery q = new BooleanQuery();
        for (final String entry : path) {
            if (q.getClauses().size() == 0) {
                // OR the first term
                q.add(new TermQuery(new Term(entry)), Operator.OR);
            } else {
                // AND all the others
                q.add(new TermQuery(new Term(entry)), Operator.AND);
            }
        }
        List<Result> matchingResults = Lists.newArrayList();

        LOG.trace("Searching for '{}'.", q);
        SearchResults results = getSearcher().search(q);
        LOG.trace("Found {} results.", results.size());
        for (final Result result : results) {
            Integer relativeDepth = getRelativeDepth(path, toResourcePath(result.getResource().getId()));
            if (relativeDepth == null) {
                continue;
            }
            if(depth == INFINITE_DEPTH || relativeDepth <= depth) {
                LOG.trace("Found match with {}.", result.getResource().getId());
                matchingResults.add(result);
            }
        }

        return matchingResults;
    }

    protected static String toResourceId(ResourcePath path) {
        StringBuilder sb = new StringBuilder();
        for (final String entry : path) {
            if (sb.length() != 0) {
                sb.append(":");
            }
            sb.append(entry);
        }
        return sb.toString();
    }

    protected static ResourcePath toChildResourcePath(ResourcePath parent, String resourceId) {
        final ResourcePath child = toResourcePath(resourceId);
        final String childEls[] = child.elements();
        final String parentEls[] = parent.elements();

        if (childEls.length <= parentEls.length) {
            return null;
        }

        String els[] = new String[parentEls.length + 1];
        for (int i = 0; i <= parentEls.length ; i++) {
            els[i] = childEls[i];
        }

        return ResourcePath.get(els);
    }

    protected static ResourcePath toResourcePath(String resourceId) {
        if (resourceId == null) {
            return null;
        }
        
        String els[] = resourceId.split(":");
        if (els.length == 0) {
            return null;
        }

        return ResourcePath.get(Arrays.copyOf(els, els.length-1));
    }

    protected static Integer getRelativeDepth(ResourcePath parent, ResourcePath child) {
        final String childEls[] = child.elements();
        final String parentEls[] = parent.elements();
        
        if (childEls.length < parentEls.length) {
            // Not a child
            return null;
        }

        // Verify the path elements up to the parents
        for (int i = 0; i < parentEls.length ; i++) {
            if (!parentEls[i].equals(childEls[i])) {
                return null;
            }
        }
        
        return childEls.length - parentEls.length; 
    }

    private synchronized Searcher getSearcher() {
        if (m_searcher == null) {
            if (m_session == null) {
                m_session = NewtsUtils.getCassrandraSession();
            }

            m_searcher = new CassandraSearcher(m_session, m_registry);
        }

        return m_searcher;
    }

    @VisibleForTesting
    protected void setSearcher(CassandraSearcher searcher) {
        m_searcher = searcher;
    }
}
