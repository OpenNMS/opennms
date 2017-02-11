package org.opennms.plugins.elasticsearch.rest;

import com.google.gson.Gson;

import io.searchbox.client.JestResult;

public class OnmsJestResult extends JestResult {

    public OnmsJestResult(final Gson gson) {
        super(gson);
    }

    public OnmsJestResult(final JestResult result) {
        super(result);
    }

    @Override
    public boolean isSucceeded() {
        final int response = this.getResponseCode();
        return (response >= 200 && response < 300);
    }
}
