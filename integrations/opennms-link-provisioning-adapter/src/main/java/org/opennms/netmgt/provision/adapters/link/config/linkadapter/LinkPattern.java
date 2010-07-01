package org.opennms.netmgt.provision.adapters.link.config.linkadapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/* Note that this root element is not referenced as link-pattern generally, it will be in a <for />
 * tag inside the {@link LinkAdapterConfiguration}, normally.
 */
/**
 * <p>LinkPattern class.</p>
 *
 * @author ranger
 * @version $Id: $
 */
@XmlRootElement(name="link-pattern")
public class LinkPattern {
    // premature optimization!  ;)
    private Pattern m_compiledPattern;

    private String m_pattern;
    private String m_template;

    /**
     * <p>Constructor for LinkPattern.</p>
     */
    public LinkPattern() {
    }

    /**
     * <p>Constructor for LinkPattern.</p>
     *
     * @param pattern a {@link java.lang.String} object.
     * @param template a {@link java.lang.String} object.
     */
    public LinkPattern(String pattern, String template) {
        setPattern(pattern);
        setTemplate(template);
    }

    /**
     * <p>getPattern</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlAttribute(name="match", required=true)
    public String getPattern() {
        return m_pattern;
    }
    
    /**
     * <p>setPattern</p>
     *
     * @param pattern a {@link java.lang.String} object.
     */
    public void setPattern(String pattern) {
        m_compiledPattern = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.DOTALL);
        m_pattern = pattern;
    }

    /**
     * <p>getTemplate</p>
     *
     * @return a {@link java.lang.String} object.
     */
    @XmlElement(name="link", required=true, nillable=false)
    public String getTemplate() {
        return m_template;
    }
    
    /**
     * <p>setTemplate</p>
     *
     * @param template a {@link java.lang.String} object.
     */
    public void setTemplate(String template) {
        m_template = template;
    }
    
    /**
     * <p>resolveTemplate</p>
     *
     * @param endPoint a {@link java.lang.String} object.
     * @return a {@link java.lang.String} object.
     */
    public String resolveTemplate(String endPoint) {
        Matcher m = m_compiledPattern.matcher(endPoint);

        if (m.matches()) {
            return m.replaceAll(m_template);
        }

        return null;
    }

    /**
     * <p>toString</p>
     *
     * @return a {@link java.lang.String} object.
     */
    public String toString() {
        return new ToStringBuilder(this)
            .append("pattern", m_pattern)
            .append("template", m_template)
            .toString();
    }
}
