-- V2__auto_enroll_students.sql
-- Auto-enroll all existing students into subjects of their department
INSERT INTO enrollments (student_id, subject_id, academic_year, semester)
SELECT s.id, sub.id, '2026-2027', COALESCE(sub.semester, s.semester, 1)
FROM students s
JOIN subjects sub ON s.department_id = sub.department_id
WHERE NOT EXISTS (
    SELECT 1 FROM enrollments e 
    WHERE e.student_id = s.id AND e.subject_id = sub.id
)
ON CONFLICT DO NOTHING;
