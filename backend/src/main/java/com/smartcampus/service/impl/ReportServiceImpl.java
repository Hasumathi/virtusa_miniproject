package com.smartcampus.service.impl;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.smartcampus.dto.response.AttendanceResponse;
import com.smartcampus.dto.response.GradeResponse;
import com.smartcampus.dto.response.SemesterGradeSummaryResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse;
import com.smartcampus.dto.response.StudentAttendanceSummaryResponse.SubjectAttendanceDetail;
import com.smartcampus.entity.Student;
import com.smartcampus.entity.Subject;
import com.smartcampus.exception.ResourceNotFoundException;
import com.smartcampus.exception.StudentNotFoundException;
import com.smartcampus.repository.StudentRepository;
import com.smartcampus.repository.SubjectRepository;
import com.smartcampus.service.AttendanceService;
import com.smartcampus.service.GradeService;
import com.smartcampus.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final StudentRepository studentRepository;
    private final SubjectRepository subjectRepository;
    private final AttendanceService attendanceService;
    private final GradeService gradeService;

    @Autowired
    public ReportServiceImpl(
            StudentRepository studentRepository,
            SubjectRepository subjectRepository,
            AttendanceService attendanceService,
            GradeService gradeService) {
        this.studentRepository = studentRepository;
        this.subjectRepository = subjectRepository;
        this.attendanceService = attendanceService;
        this.gradeService = gradeService;
    }

    @Override
    public byte[] generateStudentAttendancePdf(Long studentId) {
        log.info("Generating PDF attendance report for student ID: {}", studentId);

        Student student = studentRepository.findByIdAndIsDeletedFalse(studentId)
                .orElseThrow(() -> new StudentNotFoundException(studentId));

        StudentAttendanceSummaryResponse summary = attendanceService.getStudentAttendanceSummary(studentId);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            // Document Header
            addReportHeader(document, "STUDENT ATTENDANCE REPORT", "Generated on: " + LocalDate.now());

            // Student Information Summary
            Paragraph studentInfo = new Paragraph();
            studentInfo.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
            studentInfo.add("Register Number: " + student.getRegisterNumber() + "\n");
            studentInfo.add("Student Name: " + student.getFullName() + "\n");
            studentInfo.add("Department: " + student.getDepartment().getName() + " (" + student.getDepartment().getCode() + ")\n");
            studentInfo.add("Year / Semester: Year " + student.getYear() + ", Sem " + student.getSemester() + "\n");
            studentInfo.add("Overall Attendance: " + summary.getOverallPercentage() + "% (" +
                    (summary.isBelowThreshold() ? "CRITICAL: BELOW 75%" : "GOOD") + ")\n\n");
            document.add(studentInfo);

            // Subject Table
            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 4f, 2f, 2f, 2f, 2f});

            addTableHeader(table, new String[]{"Code", "Subject Name", "Total", "Attended", "Absent", "Percentage"});

            for (SubjectAttendanceDetail s : summary.getSubjectDetails()) {
                table.addCell(createCell(s.getSubjectCode()));
                table.addCell(createCell(s.getSubjectName()));
                table.addCell(createCell(String.valueOf(s.getTotalClasses())));
                table.addCell(createCell(String.valueOf(s.getAttendedClasses())));
                table.addCell(createCell(String.valueOf(s.getAbsentClasses())));
                table.addCell(createCell(s.getPercentage() + "%"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception ex) {
            log.error("Failed to generate student attendance PDF: {}", ex.getMessage());
            throw new RuntimeException("Could not generate PDF attendance report", ex);
        }
    }

    @Override
    public byte[] generateClassAttendancePdf(Long subjectId, LocalDate date) {
        log.info("Generating PDF class attendance report for subject ID: {} on {}", subjectId, date);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<AttendanceResponse> records = attendanceService.getAttendanceBySubjectAndDate(subjectId, date);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "DAILY CLASS ATTENDANCE REPORT", "Date: " + date.format(DateTimeFormatter.ISO_DATE));

            Paragraph info = new Paragraph();
            info.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
            info.add("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectCode() + ")\n");
            info.add("Department: " + subject.getDepartment().getName() + "\n");
            info.add("Total Recorded Sessions: " + records.size() + "\n\n");
            document.add(info);

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 4f, 2f, 3.5f});

            addTableHeader(table, new String[]{"Register No", "Student Name", "Status", "Remarks"});

            for (AttendanceResponse r : records) {
                table.addCell(createCell(r.getStudentRegisterNumber()));
                table.addCell(createCell(r.getStudentName()));
                table.addCell(createCell(r.getStatus().name()));
                table.addCell(createCell(r.getRemarks() != null ? r.getRemarks() : "-"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception ex) {
            log.error("Failed to generate class attendance PDF: {}", ex.getMessage());
            throw new RuntimeException("Could not generate class attendance report", ex);
        }
    }

    @Override
    public byte[] generateSubjectGradePdf(Long subjectId, Integer semester, String academicYear) {
        log.info("Generating PDF grade report for subject ID: {}, semester: {}", subjectId, semester);

        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new ResourceNotFoundException("Subject", "id", subjectId));

        List<GradeResponse> grades = gradeService.getGradesBySubjectAndSemester(subjectId, semester);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "SUBJECT GRADE AND PERFORMANCE REPORT",
                    "Academic Year: " + academicYear + " | Semester: " + semester);

            Paragraph info = new Paragraph();
            info.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
            info.add("Subject: " + subject.getSubjectName() + " (" + subject.getSubjectCode() + ")\n");
            info.add("Credits: " + subject.getCredits() + " | Department: " + subject.getDepartment().getName() + "\n\n");
            document.add(info);

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2.5f, 4f, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f});

            addTableHeader(table, new String[]{"Register No", "Student Name", "Internal", "Assign", "Practical", "Exam", "Total"});

            for (GradeResponse g : grades) {
                table.addCell(createCell(g.getStudentRegisterNumber()));
                table.addCell(createCell(g.getStudentName()));
                table.addCell(createCell(String.valueOf(g.getInternalMarks() != null ? g.getInternalMarks() : 0.0)));
                table.addCell(createCell(String.valueOf(g.getAssignmentMarks() != null ? g.getAssignmentMarks() : 0.0)));
                table.addCell(createCell(String.valueOf(g.getPracticalMarks() != null ? g.getPracticalMarks() : 0.0)));
                table.addCell(createCell(String.valueOf(g.getExamMarks() != null ? g.getExamMarks() : 0.0)));
                table.addCell(createCell(g.getTotalMarks() + " (" + g.getLetterGrade() + ")"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception ex) {
            log.error("Failed to generate subject grade PDF: {}", ex.getMessage());
            throw new RuntimeException("Could not generate subject grade report", ex);
        }
    }

    @Override
    public byte[] generateStudentPerformancePdf(Long studentId, Integer semester) {
        log.info("Generating PDF semester performance report for student ID: {}, semester: {}", studentId, semester);

        SemesterGradeSummaryResponse summary = gradeService.getStudentSemesterGrades(studentId, semester);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4, 36, 36, 36, 36);
            PdfWriter.getInstance(document, out);
            document.open();

            addReportHeader(document, "SEMESTER ACADEMIC PERFORMANCE SUMMARY",
                    "Semester: " + semester + " | Academic Year: " + summary.getAcademicYear());

            Paragraph info = new Paragraph();
            info.setFont(FontFactory.getFont(FontFactory.HELVETICA, 10));
            info.add("Register Number: " + summary.getRegisterNumber() + "\n");
            info.add("Student Name: " + summary.getStudentName() + "\n");
            info.add("Total Subjects: " + summary.getTotalSubjects() + " | Passed: " + summary.getPassedSubjects() +
                    " | Failed: " + summary.getFailedSubjects() + "\n");
            info.add("Semester Percentage: " + summary.getOverallPercentage() + "% | Result: " + summary.getSemesterStatus() + "\n\n");
            document.add(info);

            PdfPTable table = new PdfPTable(6);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 4f, 1.5f, 1.5f, 1.5f, 1.5f});

            addTableHeader(table, new String[]{"Subject Code", "Subject Name", "Marks", "Percentage", "Grade", "Status"});

            for (GradeResponse g : summary.getGrades()) {
                table.addCell(createCell(g.getSubjectCode()));
                table.addCell(createCell(g.getSubjectName()));
                table.addCell(createCell(String.valueOf(g.getTotalMarks())));
                table.addCell(createCell(g.getPercentage() + "%"));
                table.addCell(createCell(g.getLetterGrade()));
                table.addCell(createCell(Boolean.TRUE.equals(g.getIsPassed()) ? "PASS" : "FAIL"));
            }

            document.add(table);
            document.close();
            return out.toByteArray();

        } catch (Exception ex) {
            log.error("Failed to generate student performance PDF: {}", ex.getMessage());
            throw new RuntimeException("Could not generate semester performance report", ex);
        }
    }

    @Override
    public List<StudentAttendanceSummaryResponse> getStudentsBelowThreshold(Long departmentId) {
        log.info("Finding students below attendance threshold for department ID: {}", departmentId);

        List<Student> students = departmentId != null
                ? studentRepository.findAll().stream()
                    .filter(s -> !s.getIsDeleted() && s.getDepartment().getId().equals(departmentId))
                    .toList()
                : studentRepository.findAll().stream()
                    .filter(s -> !s.getIsDeleted())
                    .toList();

        List<StudentAttendanceSummaryResponse> lowAttendanceStudents = new ArrayList<>();
        for (Student s : students) {
            var summary = attendanceService.getStudentAttendanceSummary(s.getId());
            if (summary.isBelowThreshold()) {
                lowAttendanceStudents.add(summary);
            }
        }

        return lowAttendanceStudents;
    }

    private void addReportHeader(Document document, String title, String subtitle) throws DocumentException {
        Paragraph titleParagraph = new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.DARK_GRAY));
        titleParagraph.setAlignment(Element.ALIGN_CENTER);
        document.add(titleParagraph);

        Paragraph subtitleParagraph = new Paragraph(subtitle, FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10, Color.GRAY));
        subtitleParagraph.setAlignment(Element.ALIGN_CENTER);
        subtitleParagraph.setSpacingAfter(15);
        document.add(subtitleParagraph);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(41, 128, 185)); // Smart Campus Blue
            cell.setPadding(6);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text) {
        PdfPCell cell = new PdfPCell(new Phrase(text, FontFactory.getFont(FontFactory.HELVETICA, 9)));
        cell.setPadding(5);
        return cell;
    }
}
