package com.virtusa.studentmanagement.util;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.exception.InvalidStudentDataException;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility helper class for CSV serialization and deserialization of Student entities.
 */
public class CsvHelper {

    public static final String TYPE = "text/csv";
    private static final String[] EXPORT_HEADERS = {"ID", "Name", "Email", "Department", "YearOfStudy", "PhoneNumber"};

    /**
     * Converts a list of Student entities into an RFC-4180 CSV byte stream.
     */
    public static ByteArrayInputStream studentsToCsv(List<Student> students) {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(EXPORT_HEADERS)
                .build();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CSVPrinter csvPrinter = new CSVPrinter(new PrintWriter(out, true, StandardCharsets.UTF_8), format)) {

            for (Student s : students) {
                csvPrinter.printRecord(
                        s.getId(),
                        s.getName(),
                        s.getEmail(),
                        s.getDepartment(),
                        s.getYearOfStudy(),
                        s.getPhoneNumber()
                );
            }

            csvPrinter.flush();
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export students data to CSV: " + e.getMessage(), e);
        }
    }

    /**
     * Parses an uploaded CSV input stream into a list of Student entities.
     * Dynamically auto-detects header names so uploads with or without ID column work seamlessly.
     */
    public static List<Student> csvToStudents(InputStream is) {
        try (BufferedReader fileReader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            CSVFormat format = CSVFormat.DEFAULT.builder()
                    .setHeader() // Auto-detect headers from first line
                    .setSkipHeaderRecord(true)
                    .setIgnoreHeaderCase(true)
                    .setTrim(true)
                    .build();

            try (CSVParser csvParser = new CSVParser(fileReader, format)) {
                List<Student> students = new ArrayList<>();

                for (CSVRecord record : csvParser) {
                    String name = getField(record, "Name", 0);
                    String email = getField(record, "Email", 1);
                    String department = getField(record, "Department", 2);
                    String yearStr = getField(record, "YearOfStudy", 3);
                    String phone = getField(record, "PhoneNumber", 4);

                    if (name == null || name.isBlank() || email == null || email.isBlank()) {
                        throw new InvalidStudentDataException("CSV row " + record.getRecordNumber() + ": Name and Email cannot be empty");
                    }

                    int year;
                    try {
                        year = Integer.parseInt(yearStr != null ? yearStr.trim() : "1");
                        if (year < 1 || year > 4) {
                            throw new InvalidStudentDataException("CSV row " + record.getRecordNumber() + ": Year of study must be between 1 and 4");
                        }
                    } catch (NumberFormatException e) {
                        throw new InvalidStudentDataException("CSV row " + record.getRecordNumber() + ": Invalid numeric format for year of study");
                    }

                    Student student = new Student(name, email, department, year, phone);
                    students.add(student);
                }

                return students;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse CSV file: " + e.getMessage(), e);
        }
    }

    private static String getField(CSVRecord record, String headerName, int fallbackIndex) {
        if (record.isMapped(headerName)) {
            return record.get(headerName);
        }
        if (record.size() > fallbackIndex) {
            return record.get(fallbackIndex);
        }
        return null;
    }
}
