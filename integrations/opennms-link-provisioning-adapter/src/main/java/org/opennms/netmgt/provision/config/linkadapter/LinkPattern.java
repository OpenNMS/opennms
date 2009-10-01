package org.opennms.netmgt.provision.config.linkadapter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.lang.builder.ToStringBuilder;

/* Note that this root element is not referenced as link-pattern generally, it will be in a <for />
 * tag inside the {@link LinkAdapterConfiguration}, normally.
 */
@XmlRootElement(name="link-pattern")
public class LinkPattern {
    // premature optimization!  ;)
    private Pattern m_compiledPattern;

    private String m_pattern;
    private String m_template;

    public LinkPattern() {
    }

    public LinkPattern(String pattern, String template) {
        setPattern(pattern);
        setTemplate(template);
    }

    @XmlAttribute(name="match", required=true)
    public String getPattern() {
        return m_pattern;
    }
    
    public void setPattern(String pattern) {
        m_compiledPattern = Pattern.compile(pattern, Pattern.CANON_EQ | Pattern.DOTALL);
        m_pattern = pattern;
    }

    @XmlElement(name="link", required=true, nillable=false)
    public String getTemplate() {
        return m_template;
    }
    
    public void setTemplate(String template) {
        m_template = template;
    }
    
    public String resolveTemplate(String endPoint) {
        Matcher m = m_compiledPattern.matcher(endPoint);

        if (m.matches()) {
            return m.replaceAll(m_template);
        }

        return null;
    }

    public String toString() {
        return new ToStringBuilder(this)
            .append("pattern", m_pattern)
            .append("template", m_template)
            .toString();
    }
}
