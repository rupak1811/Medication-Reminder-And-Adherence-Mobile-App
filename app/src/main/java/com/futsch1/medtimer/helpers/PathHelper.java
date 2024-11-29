package com.futsch1.medtimer.helpers;

import com.futsch1.medtimer.exporters.CSVExport;
import com.futsch1.medtimer.exporters.PDFExporter;
import com.futsch1.medtimer.exporters.Exporter;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;

public class PathHelper {
    private static final String RESERVED_CHARS = "[\\[|?*<\":>+/'\\],]";

    private PathHelper() {
        // Intentionally empty
    }

    // Existing method for backup filenames
    public static String getBackupFilename() {
        String fileName = String.format("MedTimer Backup %s.json", ZonedDateTime.now().format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM)));
        return fileName.replaceAll(RESERVED_CHARS, "_");
    }

    // New method to handle export filenames based on the exporter type
    public static String getExportFilename(Exporter exporter) {
        String fileName = "exported_data";  // Default filename

        if (exporter instanceof CSVExport) {
            fileName = "exported_data.csv";  // For CSV exporter
        } else if (exporter instanceof PDFExporter) {
            fileName = "exported_data.pdf";  // For PDF exporter
        }

        return fileName.replaceAll(RESERVED_CHARS, "_");  // Replace any reserved characters
    }
}
