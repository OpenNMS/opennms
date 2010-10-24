package org.opennms.netmgt.jasper.jrobin;

class ColonSplitter {
    private String cmd;
    private static final String COLON = "@#@";

    ColonSplitter(String command) {
        this.cmd = command;
    }

    String[] split() {
        String[] tokens = cmd.replaceAll("\\\\:", COLON).split(":");
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].replaceAll(COLON, ":");
        }
        return tokens;
    }
}