package org.opennms.systemreport;

public interface ResourceLocator {
    public String findBinary(final String name);
    public String slurpOutput(final String command, final boolean ignoreExitCode);
}
