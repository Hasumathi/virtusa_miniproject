package com.virtusa.studentmanagement.config;

import com.virtusa.studentmanagement.entity.Student;
import com.virtusa.studentmanagement.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.util.List;

/**
 * Database Initializer & Connectivity Test Component.
 * 
 * - Tests and verifies database connectivity on application startup.
 * - Implements initial database operations (Insert, Count, Query) using StudentRepository.
 * - Seeds initial student records if the database table is empty.
 */
@Component
public class DatabaseInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseInitializer.class);

    private final StudentRepository studentRepository;
    private final DataSource dataSource;

    public DatabaseInitializer(StudentRepository studentRepository, DataSource dataSource) {
        this.studentRepository = studentRepository;
        this.dataSource = dataSource;
    }

    @Override
    public void run(String... args) {
        logger.info("===================================================================");
        logger.info(">>> TESTING DATABASE CONNECTIVITY & OPERATIONS <<<");
        logger.info("===================================================================");

        // 1. Test raw database connectivity & metadata
        testDatabaseConnection();

        // 2. Perform database operations using StudentRepository
        performDatabaseOperations();

        logger.info("===================================================================");
        logger.info(">>> DATABASE INITIALIZATION & CONNECTIVITY TEST COMPLETE <<<");
        logger.info("===================================================================");
    }

    /**
     * Verifies MySQL connection and logs database metadata.
     */
    private void testDatabaseConnection() {
        try (Connection connection = dataSource.getConnection()) {
            DatabaseMetaData metaData = connection.getMetaData();
            logger.info("[SUCCESS] Database Connection SUCCESSFUL!");
            logger.info("   Database Product: {} {}", metaData.getDatabaseProductName(), metaData.getDatabaseProductVersion());
            logger.info("   Driver Name:      {}", metaData.getDriverName());
            logger.info("   JDBC URL:         {}", metaData.getURL());
            logger.info("   User:             {}", metaData.getUserName());
        } catch (Exception e) {
            logger.error("❌ Database Connection FAILED: {}", e.getMessage(), e);
        }
    }

    /**
     * Executes initial database operations (Count, Insert, Read) using JPA repository.
     */
    private void performDatabaseOperations() {
        try {
            long initialCount = studentRepository.count();
            logger.info("Current number of student records in database: {}", initialCount);

            // Seed initial records if empty
            if (initialCount == 0) {
                logger.info("Database table is empty. Seeding initial student records...");

                List<Student> sampleStudents = List.of(
                    new Student("Aarav Sharma", "aarav.sharma@example.com", "Computer Science", 3, "9876543210"),
                    new Student("Diya Patel", "diya.patel@example.com", "Information Technology", 2, "9123456780"),
                    new Student("Rohan Verma", "rohan.verma@example.com", "Electronics & Communication", 4, "9812345678"),
                    new Student("Ananya Iyer", "ananya.iyer@example.com", "Computer Science", 1, "9765432109")
                );

                List<Student> savedStudents = studentRepository.saveAll(sampleStudents);
                logger.info("[SUCCESS] Successfully inserted {} sample student records into database.", savedStudents.size());
            }

            // Verify retrieval
            List<Student> students = studentRepository.findAll();
            logger.info("Retrieved {} student(s) from database:", students.size());
            for (Student student : students) {
                logger.info("   - [ID: {}] {} ({}) - {}, Year {}, Phone: {}",
                        student.getId(),
                        student.getName(),
                        student.getEmail(),
                        student.getDepartment(),
                        student.getYearOfStudy(),
                        student.getPhoneNumber()
                );
            }

        } catch (Exception e) {
            logger.error("❌ Failed to perform repository database operations: {}", e.getMessage(), e);
        }
    }
}
