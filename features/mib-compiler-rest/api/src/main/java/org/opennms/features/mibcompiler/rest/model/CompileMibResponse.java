package org.opennms.features.mibcompiler.rest.model;

import java.util.List;

public class CompileMibResponse {

    private boolean success;
    private String message;

    private String name;

    private String compiledFile;

    private List<String> missingDependencies;

    private String formattedErrors;

    private CompileMibResponse() {}

    public CompileMibResponse(boolean success,
                              String message,
                              String name,
                              String compiledFile,
                              List<String> missingDependencies,
                              String formattedErrors) {
        this.success = success;
        this.message = message;
        this.name = name;
        this.compiledFile = compiledFile;
        this.missingDependencies = missingDependencies;
        this.formattedErrors = formattedErrors;
    }

    public String getCompiledFile() {
        return compiledFile;
    }

    public void setCompiledFile(String compiledFile) {
        this.compiledFile = compiledFile;
    }

    public String getFormattedErrors() {
        return formattedErrors;
    }

    public void setFormattedErrors(String formattedErrors) {
        this.formattedErrors = formattedErrors;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public void setMissingDependencies(List<String> missingDependencies) {
        this.missingDependencies = missingDependencies;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

}
