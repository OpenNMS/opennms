package org.opennms.features.mibcompiler.rest.model;

import java.io.File;
import java.util.Collections;
import java.util.List;

public class CompileMibResult {

    public enum Status {
        SUCCESS,
        NOT_FOUND,
        INVALID_REQUEST,
        VALIDATION_FAILED,
        MISSING_DEPENDENCIES,
        CONFLICT
    }

    private final Status status;
    private final String message;
    private final File pendingFile;
    private final File compiledFile;
    private final List<String> missingDependencies;
    private final String formattedErrors;

    private CompileMibResult(Status status,
                             String message,
                             File pendingFile,
                             File compiledFile,
                             List<String> missingDependencies,
                             String formattedErrors) {
        this.status = status;
        this.message = message;
        this.pendingFile = pendingFile;
        this.compiledFile = compiledFile;
        this.missingDependencies = missingDependencies == null ? Collections.<String>emptyList() : missingDependencies;
        this.formattedErrors = formattedErrors;
    }

    public static CompileMibResult success(File pendingFile, File compiledFile) {
        return new CompileMibResult(Status.SUCCESS, "Compiled successfully.", pendingFile, compiledFile,
                Collections.<String>emptyList(), null);
    }

    public static CompileMibResult notFound(String message) {
        return new CompileMibResult(Status.NOT_FOUND, message, null, null,
                Collections.<String>emptyList(), null);
    }

    public static CompileMibResult invalidRequest(String message) {
        return new CompileMibResult(Status.INVALID_REQUEST, message, null, null,
                Collections.<String>emptyList(), null);
    }

    public static CompileMibResult validationFailed(String message, String formattedErrors) {
        return new CompileMibResult(Status.VALIDATION_FAILED, message, null, null,
                Collections.<String>emptyList(), formattedErrors);
    }

    public static CompileMibResult missingDependencies(String message, List<String> missingDependencies) {
        return new CompileMibResult(Status.MISSING_DEPENDENCIES, message, null, null,
                missingDependencies, null);
    }

    public static CompileMibResult conflict(String message) {
        return new CompileMibResult(Status.CONFLICT, message, null, null,
                Collections.<String>emptyList(), null);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public File getPendingFile() {
        return pendingFile;
    }

    public File getCompiledFile() {
        return compiledFile;
    }

    public List<String> getMissingDependencies() {
        return missingDependencies;
    }

    public String getFormattedErrors() {
        return formattedErrors;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }
}
