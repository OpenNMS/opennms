package org.opennms.features.vaadin.utils;

import java.io.File;

public final class FileValidationUtils {
    public static  boolean isValidFileName(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return false;
        }

        // Block path traversal sequences
        if (fileName.contains("..") ||
                fileName.contains("/") ||
                fileName.contains("\\") ||
                fileName.contains("\0") || // null byte
                fileName.contains("%00") || // encoded null byte
                fileName.contains("%2e") || // encoded dot
                fileName.contains("%2f") || // encoded slash
                fileName.contains("%5c")) { // encoded backslash
            return false;
        }

        // Block other dangerous patterns
        if (fileName.matches(".*[<>:\"|?*].*")) {
            return false; // Windows reserved characters
        }

        // Extract just the filename part (in case any path components slipped through)
        String nameOnly = new File(fileName).getName();
        if (!nameOnly.equals(fileName)) {
            return false; // Path components detected
        }

        // Allow only safe characters for filenames
        if (!nameOnly.matches("^[a-zA-Z0-9][a-zA-Z0-9._\\-]+$")) {
            return false;
        }

        // Prevent reserved filenames (Windows)
        String nameWithoutExt = nameOnly.replaceFirst("\\.[^.]+$", "").toUpperCase();
        if (nameWithoutExt.matches("^(CON|PRN|AUX|NUL|COM[1-9]|LPT[1-9])$")) {
            return false;
        }
        return true;
    }

    public static boolean isFileNameTooLong(String fileName) {
        return fileName != null && fileName.length() > 255;
    }

}

