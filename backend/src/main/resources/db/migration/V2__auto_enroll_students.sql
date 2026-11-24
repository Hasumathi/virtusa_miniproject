-- V2__auto_enroll_students.sql
-- 1. Insert comprehensive 4-year CSE curriculum subjects
INSERT INTO subjects (subject_code, subject_name, department_id, semester, credits)
VALUES 
    -- Year 1 (Sem 1 & 2)
    ('CS1101', 'Problem Solving & C Programming', 1, 1, 4),
    ('MA1101', 'Engineering Mathematics I', 1, 1, 4),
    ('CS1201', 'Object-Oriented Programming with Java', 1, 2, 4),
    ('EC1201', 'Digital Logic & Computer Organization', 1, 2, 3),
    -- Year 2 (Sem 3 & 4)
    ('CS101', 'Data Structures & Algorithms', 1, 3, 4),
    ('CS102', 'Database Management Systems', 1, 3, 4),
    ('CS103', 'Operating Systems', 1, 4, 3),
    -- Year 3 (Sem 5 & 6)
    ('CS104', 'Computer Networks', 1, 5, 3),
    ('CS105', 'Web Technologies', 1, 5, 4),
    ('CS106', 'Software Engineering & DevOps', 1, 6, 3),
    ('CS107', 'Artificial Intelligence & Machine Learning', 1, 6, 4),
    -- Year 4 (Sem 7 & 8)
    ('CS108', 'Cloud Computing & Distributed Systems', 1, 7, 3),
    ('CS109', 'Cyber Security & Cryptography', 1, 7, 3),
    ('CS110', 'Capstone Project & Industrial Internship', 1, 8, 6)
ON CONFLICT (subject_code) DO NOTHING;

-- 2. Auto-enroll each student in subjects matching their department and semester
INSERT INTO enrollments (student_id, subject_id, academic_year, semester)
SELECT s.id, sub.id, '2026-2027', sub.semester
FROM students s
JOIN subjects sub ON s.department_id = sub.department_id AND s.semester = sub.semester
WHERE NOT EXISTS (
    SELECT 1 FROM enrollments e 
    WHERE e.student_id = s.id AND e.subject_id = sub.id
)
ON CONFLICT DO NOTHING;
