"use client";

import { useState, useEffect } from "react";

type StudentItem = {
  id: number;
  registerNumber: string;
  fullName: string;
  email: string;
  year?: number;
  semester?: number;
  section?: string;
  accountStatus?: string;
};

type SubjectItem = {
  id: number;
  subjectCode: string;
  subjectName: string;
  semester: number;
  credits: number;
};

type FacultyActionsProps = {
  backendToken?: string;
  initialStudents?: StudentItem[];
  onDataRefresh?: () => void;
  activeTab?: "students" | "attendance" | "grades";
  onTabChange?: (tab: "students" | "attendance" | "grades") => void;
};

export default function FacultyActions({
  backendToken,
  initialStudents = [],
  activeTab: controlledTab,
  onTabChange,
}: FacultyActionsProps) {
  const [internalTab, setInternalTab] = useState<"students" | "attendance" | "grades">("students");

  const currentTab = controlledTab ?? internalTab;

  const setActiveTab = (tab: "students" | "attendance" | "grades") => {
    setInternalTab(tab);
    onTabChange?.(tab);
  };

  // Students state
  const [students, setStudents] = useState<StudentItem[]>(initialStudents);
  const [showAddStudentModal, setShowAddStudentModal] = useState(false);
  const [studentSearch, setStudentSearch] = useState("");

  // Student Form State
  const [regNo, setRegNo] = useState("");
  const [fullName, setFullName] = useState("");
  const [studentEmail, setStudentEmail] = useState("");
  const [studentPhone, setStudentPhone] = useState("");
  const [studentYear, setStudentYear] = useState(3);
  const [studentSem, setStudentSem] = useState(5);
  const [studentSection, setStudentSection] = useState("A");
  const [initialPassword, setInitialPassword] = useState("Password@2026");
  const [addingStudent, setAddingStudent] = useState(false);
  const [studentMsg, setStudentMsg] = useState("");
  const [studentErr, setStudentErr] = useState("");

  // Subjects State
  const [subjects, setSubjects] = useState<SubjectItem[]>([
    { id: 1, subjectCode: "CS101", subjectName: "Data Structures & Algorithms", semester: 3, credits: 4 },
    { id: 2, subjectCode: "CS102", subjectName: "Database Management Systems", semester: 3, credits: 4 },
    { id: 4, subjectCode: "CS104", subjectName: "Computer Networks", semester: 5, credits: 3 },
    { id: 5, subjectCode: "CS105", subjectName: "Web Technologies", semester: 5, credits: 4 },
  ]);

  // Attendance State
  const [attendanceSubjectId, setAttendanceSubjectId] = useState<number>(4);
  const [attendanceDate, setAttendanceDate] = useState<string>(new Date().toISOString().split("T")[0]);
  const [attendanceRecords, setAttendanceRecords] = useState<Record<number, { status: "PRESENT" | "ABSENT" | "ON_LEAVE"; remarks: string }>>({});
  const [submittingAttendance, setSubmittingAttendance] = useState(false);
  const [attendanceMsg, setAttendanceMsg] = useState("");
  const [attendanceErr, setAttendanceErr] = useState("");

  // Grades State
  const [gradeStudentId, setGradeStudentId] = useState<number>(students[0]?.id || 1);
  const [gradeSubjectId, setGradeSubjectId] = useState<number>(4);
  const [gradeSemester, setGradeSemester] = useState<number>(5);
  const [internalMarks, setInternalMarks] = useState<number>(20);
  const [assignmentMarks, setAssignmentMarks] = useState<number>(12);
  const [practicalMarks, setPracticalMarks] = useState<number>(18);
  const [examMarks, setExamMarks] = useState<number>(32);
  const [submittingGrade, setSubmittingGrade] = useState(false);
  const [gradeMsg, setGradeMsg] = useState("");
  const [gradeErr, setGradeErr] = useState("");

  // Load live students and subjects on mount
  useEffect(() => {
    function handleHash() {
      const hash = window.location.hash.toLowerCase();
      if (hash.includes("attendance")) {
        setActiveTab("attendance");
      } else if (hash.includes("grade")) {
        setActiveTab("grades");
      } else if (hash.includes("student") || hash.includes("faculty-actions")) {
        setActiveTab("students");
      }
    }

    handleHash();
    window.addEventListener("hashchange", handleHash);
    return () => window.removeEventListener("hashchange", handleHash);
  }, []);

  useEffect(() => {
    async function loadBackendData() {
      if (!backendToken) return;

      try {
        const studentRes = await fetch("/backend-api/students?size=50", {
          headers: { Authorization: `Bearer ${backendToken}` },
        });
        if (studentRes.ok) {
          const res = await studentRes.json();
          if (res.data?.content && res.data.content.length > 0) {
            setStudents(res.data.content);
            if (!gradeStudentId) setGradeStudentId(res.data.content[0].id);
          }
        }

        const subjectRes = await fetch("/backend-api/subjects", {
          headers: { Authorization: `Bearer ${backendToken}` },
        });
        if (subjectRes.ok) {
          const res = await subjectRes.json();
          if (res.data && res.data.length > 0) {
            setSubjects(res.data);
          }
        }
      } catch (err) {
        console.warn("Could not fetch initial faculty records:", err);
      }
    }

    loadBackendData();
  }, [backendToken, gradeStudentId]);

  // Initialize attendance records when students list changes
  useEffect(() => {
    const init: Record<number, { status: "PRESENT" | "ABSENT" | "ON_LEAVE"; remarks: string }> = {};
    students.forEach((s) => {
      init[s.id] = { status: "PRESENT", remarks: "Present in lecture" };
    });
    setAttendanceRecords(init);
  }, [students]);

  // Handler: Add Student
  async function handleAddStudent(e: React.FormEvent) {
    e.preventDefault();
    setAddingStudent(true);
    setStudentErr("");
    setStudentMsg("");

    try {
      const response = await fetch("/backend-api/students", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${backendToken}`,
        },
        body: JSON.stringify({
          registerNumber: regNo.trim().toUpperCase(),
          fullName: fullName.trim(),
          email: studentEmail.trim().toLowerCase(),
          phone: studentPhone || "9876543210",
          departmentId: 1, // Default CSE
          year: Number(studentYear),
          semester: Number(studentSem),
          section: studentSection.toUpperCase(),
          createAccount: true,
          initialPassword: initialPassword || `${regNo.trim().toUpperCase()}@Smart2026`,
        }),
      });

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to create student record.");
      }

      setStudentMsg(`Student ${fullName} (${regNo.toUpperCase()}) created successfully!`);
      setStudents((prev) => [data.data, ...prev]);
      setShowAddStudentModal(false);
      // Reset form
      setRegNo("");
      setFullName("");
      setStudentEmail("");
      setStudentPhone("");
    } catch (err) {
      setStudentErr(err instanceof Error ? err.message : "Error creating student.");
    } finally {
      setAddingStudent(false);
    }
  }

  // Handler: Mark Attendance
  async function handleMarkAttendance(e: React.FormEvent) {
    e.preventDefault();
    setSubmittingAttendance(true);
    setAttendanceMsg("");
    setAttendanceErr("");

    const records = Object.entries(attendanceRecords).map(([studentId, val]) => ({
      studentId: Number(studentId),
      status: val.status,
      remarks: val.remarks || (val.status === "PRESENT" ? "Attended" : "Unexcused"),
    }));

    if (records.length === 0) {
      setAttendanceErr("No students available to mark attendance.");
      setSubmittingAttendance(false);
      return;
    }

    try {
      const response = await fetch("/backend-api/attendance/batch", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${backendToken}`,
        },
        body: JSON.stringify({
          subjectId: Number(attendanceSubjectId),
          attendanceDate: attendanceDate,
          records: records,
        }),
      });

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to record batch attendance.");
      }

      setAttendanceMsg(`Attendance recorded successfully for ${records.length} students on ${attendanceDate}!`);
    } catch (err) {
      setAttendanceErr(err instanceof Error ? err.message : "Failed to record attendance.");
    } finally {
      setSubmittingAttendance(false);
    }
  }

  // Handler: Enter Grade
  async function handleEnterGrade(e: React.FormEvent) {
    e.preventDefault();
    setSubmittingGrade(true);
    setGradeMsg("");
    setGradeErr("");

    try {
      const response = await fetch("/backend-api/grades", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${backendToken}`,
        },
        body: JSON.stringify({
          studentId: Number(gradeStudentId),
          subjectId: Number(gradeSubjectId),
          semester: Number(gradeSemester),
          academicYear: "2026-2027",
          internalMarks: Number(internalMarks),
          assignmentMarks: Number(assignmentMarks),
          practicalMarks: Number(practicalMarks),
          examMarks: Number(examMarks),
        }),
      });

      const data = await response.json();

      if (!response.ok || !data.success) {
        throw new Error(data.message || "Failed to record grade.");
      }

      const total = (internalMarks + assignmentMarks + practicalMarks + examMarks).toFixed(1);
      setGradeMsg(`Grade recorded successfully! Total Score: ${total}/100 (Grade ${data.data?.letterGrade || "A"}).`);
    } catch (err) {
      setGradeErr(err instanceof Error ? err.message : "Failed to record student grade.");
    } finally {
      setSubmittingGrade(false);
    }
  }

  const filteredStudents = students.filter(
    (s) =>
      s.fullName?.toLowerCase().includes(studentSearch.toLowerCase()) ||
      s.registerNumber?.toLowerCase().includes(studentSearch.toLowerCase()) ||
      s.email?.toLowerCase().includes(studentSearch.toLowerCase())
  );

  const totalGradeScore = (
    Number(internalMarks || 0) +
    Number(assignmentMarks || 0) +
    Number(practicalMarks || 0) +
    Number(examMarks || 0)
  ).toFixed(1);

  return (
    <section className="mt-8 rounded-3xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-9" id="faculty-actions">
      {/* SECTION HEADER & TABS */}
      <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#e9e8df] pb-6">
        <div>
          <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
            Faculty Workspace
          </p>
          <h2 className="mt-1 text-2xl font-extrabold text-[#17212b]">
            Academic Management Console
          </h2>
          <p className="mt-1 text-sm text-[#68716d]">
            Enroll students, mark daily class attendance, and submit semester grades directly to the backend.
          </p>
        </div>

        {/* Tab Switcher */}
        <div className="flex items-center rounded-xl bg-[#f2f4ec] p-1.5 text-sm font-semibold">
          <button
            type="button"
            onClick={() => setActiveTab("students")}
            className={`rounded-lg px-4 py-2 transition ${
              currentTab === "students"
                ? "bg-white text-[#075b40] shadow-sm"
                : "text-[#68716d] hover:text-[#17212b]"
            }`}
          >
            👥 Students ({students.length})
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("attendance")}
            className={`rounded-lg px-4 py-2 transition ${
              currentTab === "attendance"
                ? "bg-white text-[#075b40] shadow-sm"
                : "text-[#68716d] hover:text-[#17212b]"
            }`}
          >
            📋 Mark Attendance
          </button>
          <button
            type="button"
            onClick={() => setActiveTab("grades")}
            className={`rounded-lg px-4 py-2 transition ${
              currentTab === "grades"
                ? "bg-white text-[#075b40] shadow-sm"
                : "text-[#68716d] hover:text-[#17212b]"
            }`}
          >
            🏅 Enter Grades
          </button>
        </div>
      </div>

      {/* =========================================================================
          TAB 1: STUDENTS MANAGEMENT
      ========================================================================= */}
      {currentTab === "students" && (
        <div className="pt-6">
          <div className="mb-6 flex flex-wrap items-center justify-between gap-4">
            <div className="relative min-w-[260px] flex-1 max-w-md">
              <input
                type="text"
                placeholder="Search students by name, reg no..."
                value={studentSearch}
                onChange={(e) => setStudentSearch(e.target.value)}
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
              />
            </div>

            <button
              type="button"
              onClick={() => setShowAddStudentModal(true)}
              className="flex items-center gap-2 rounded-xl bg-[#075b40] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#06442f]"
            >
              <span>+</span> Add New Student
            </button>
          </div>

          {studentMsg && (
            <div className="mb-4 rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              ✓ {studentMsg}
            </div>
          )}

          {studentErr && (
            <div className="mb-4 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
              ⚠️ {studentErr}
            </div>
          )}

          {/* Students Table */}
          <div className="overflow-x-auto rounded-2xl border border-[#e9e8df]">
            <table className="w-full text-left text-sm">
              <thead className="bg-[#fcfaf5] text-xs font-bold uppercase text-[#68716d]">
                <tr className="border-b border-[#e9e8df]">
                  <th className="px-5 py-3.5">Register No</th>
                  <th className="px-5 py-3.5">Full Name</th>
                  <th className="px-5 py-3.5">Email Address</th>
                  <th className="px-5 py-3.5">Academic Standing</th>
                  <th className="px-5 py-3.5">Status</th>
                  <th className="px-5 py-3.5 text-right">Quick Actions</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0efe8]">
                {filteredStudents.length > 0 ? (
                  filteredStudents.map((s) => (
                    <tr key={s.id} className="transition hover:bg-[#fafaf7]">
                      <td className="px-5 py-3.5 font-bold text-[#075b40]">
                        {s.registerNumber}
                      </td>
                      <td className="px-5 py-3.5 font-semibold text-[#17212b]">
                        {s.fullName}
                      </td>
                      <td className="px-5 py-3.5 text-[#68716d]">{s.email}</td>
                      <td className="px-5 py-3.5 text-[#68716d]">
                        Year {s.year || 3}, Sem {s.semester || 5} (Sec {s.section || "A"})
                      </td>
                      <td className="px-5 py-3.5">
                        <span className="inline-flex rounded-full bg-emerald-50 px-2.5 py-1 text-xs font-bold text-emerald-700">
                          {s.accountStatus || "ACTIVE"}
                        </span>
                      </td>
                      <td className="px-5 py-3.5 text-right">
                        <button
                          type="button"
                          onClick={() => {
                            setGradeStudentId(s.id);
                            setActiveTab("grades");
                          }}
                          className="mr-2 text-xs font-bold text-[#075b40] hover:underline"
                        >
                          Grade
                        </button>
                        <button
                          type="button"
                          onClick={() => {
                            setActiveTab("attendance");
                          }}
                          className="text-xs font-bold text-[#b76528] hover:underline"
                        >
                          Attendance
                        </button>
                      </td>
                    </tr>
                  ))
                ) : (
                  <tr>
                    <td colSpan={6} className="px-5 py-8 text-center text-[#68716d]">
                      No student records found. Click &quot;Add New Student&quot; to enroll a student.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* =========================================================================
          TAB 2: ATTENDANCE MANAGEMENT
      ========================================================================= */}
      {currentTab === "attendance" && (
        <form onSubmit={handleMarkAttendance} className="space-y-6 pt-6">
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <div>
              <label className="mb-2 block text-xs font-bold uppercase text-[#68716d]">
                Select Course / Subject
              </label>
              <select
                value={attendanceSubjectId}
                onChange={(e) => setAttendanceSubjectId(Number(e.target.value))}
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm font-semibold outline-none focus:border-[#075b40]"
              >
                {subjects.map((sub) => (
                  <option key={sub.id} value={sub.id}>
                    {sub.subjectCode} — {sub.subjectName} (Sem {sub.semester})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-xs font-bold uppercase text-[#68716d]">
                Session Date
              </label>
              <input
                type="date"
                value={attendanceDate}
                onChange={(e) => setAttendanceDate(e.target.value)}
                required
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm outline-none focus:border-[#075b40]"
              />
            </div>

            <div className="flex items-end">
              <button
                type="button"
                onClick={() => {
                  const updated: Record<number, { status: "PRESENT" | "ABSENT" | "ON_LEAVE"; remarks: string }> = {};
                  students.forEach((s) => {
                    updated[s.id] = { status: "PRESENT", remarks: "All present" };
                  });
                  setAttendanceRecords(updated);
                }}
                className="rounded-xl border border-[#cbe4d2] bg-[#edf5ee] px-4 py-2.5 text-xs font-bold text-[#075b40] transition hover:bg-[#dceee2]"
              >
                ✓ Mark All as Present
              </button>
            </div>
          </div>

          {attendanceMsg && (
            <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              ✓ {attendanceMsg}
            </div>
          )}

          {attendanceErr && (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
              ⚠️ {attendanceErr}
            </div>
          )}

          {/* Attendance Sheet Table */}
          <div className="overflow-x-auto rounded-2xl border border-[#e9e8df]">
            <table className="w-full text-left text-sm">
              <thead className="bg-[#fcfaf5] text-xs font-bold uppercase text-[#68716d]">
                <tr className="border-b border-[#e9e8df]">
                  <th className="px-5 py-3">Student</th>
                  <th className="px-5 py-3">Register No</th>
                  <th className="px-5 py-3">Attendance Status</th>
                  <th className="px-5 py-3">Faculty Remarks</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-[#f0efe8]">
                {students.map((s) => {
                  const record = attendanceRecords[s.id] || { status: "PRESENT", remarks: "" };
                  return (
                    <tr key={s.id} className="hover:bg-[#fafaf7]">
                      <td className="px-5 py-3.5 font-semibold text-[#17212b]">
                        {s.fullName}
                      </td>
                      <td className="px-5 py-3.5 text-[#075b40] font-mono font-bold">
                        {s.registerNumber}
                      </td>
                      <td className="px-5 py-3.5">
                        <div className="flex items-center gap-2">
                          <button
                            type="button"
                            onClick={() =>
                              setAttendanceRecords((prev) => ({
                                ...prev,
                                [s.id]: { ...record, status: "PRESENT" },
                              }))
                            }
                            className={`rounded-lg px-3 py-1 text-xs font-bold transition ${
                              record.status === "PRESENT"
                                ? "bg-emerald-600 text-white shadow-sm"
                                : "bg-emerald-50 text-emerald-700 hover:bg-emerald-100"
                            }`}
                          >
                            Present
                          </button>
                          <button
                            type="button"
                            onClick={() =>
                              setAttendanceRecords((prev) => ({
                                ...prev,
                                [s.id]: { ...record, status: "ABSENT" },
                              }))
                            }
                            className={`rounded-lg px-3 py-1 text-xs font-bold transition ${
                              record.status === "ABSENT"
                                ? "bg-red-600 text-white shadow-sm"
                                : "bg-red-50 text-red-700 hover:bg-red-100"
                            }`}
                          >
                            Absent
                          </button>
                          <button
                            type="button"
                            onClick={() =>
                              setAttendanceRecords((prev) => ({
                                ...prev,
                                [s.id]: { ...record, status: "ON_LEAVE" },
                              }))
                            }
                            className={`rounded-lg px-3 py-1 text-xs font-bold transition ${
                              record.status === "ON_LEAVE"
                                ? "bg-amber-600 text-white shadow-sm"
                                : "bg-amber-50 text-amber-700 hover:bg-amber-100"
                            }`}
                          >
                            Leave
                          </button>
                        </div>
                      </td>
                      <td className="px-5 py-3.5">
                        <input
                          type="text"
                          value={record.remarks}
                          onChange={(e) =>
                            setAttendanceRecords((prev) => ({
                              ...prev,
                              [s.id]: { ...record, remarks: e.target.value },
                            }))
                          }
                          placeholder="Optional remarks"
                          className="w-full max-w-xs rounded-lg border border-[#e9e8df] px-3 py-1.5 text-xs outline-none focus:border-[#075b40]"
                        />
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>

          <div className="flex justify-end pt-2">
            <button
              type="submit"
              disabled={submittingAttendance || students.length === 0}
              className="rounded-xl bg-[#075b40] px-8 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:opacity-50"
            >
              {submittingAttendance ? "Saving Attendance..." : "Save Class Attendance"}
            </button>
          </div>
        </form>
      )}

      {/* =========================================================================
          TAB 3: GRADES & MARKS ENTRY
      ========================================================================= */}
      {currentTab === "grades" && (
        <form onSubmit={handleEnterGrade} className="space-y-6 pt-6">
          <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-3">
            <div>
              <label className="mb-2 block text-xs font-bold uppercase text-[#68716d]">
                Student
              </label>
              <select
                value={gradeStudentId}
                onChange={(e) => setGradeStudentId(Number(e.target.value))}
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm font-semibold outline-none focus:border-[#075b40]"
              >
                {students.map((s) => (
                  <option key={s.id} value={s.id}>
                    {s.fullName} ({s.registerNumber})
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-xs font-bold uppercase text-[#68716d]">
                Course / Subject
              </label>
              <select
                value={gradeSubjectId}
                onChange={(e) => setGradeSubjectId(Number(e.target.value))}
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm font-semibold outline-none focus:border-[#075b40]"
              >
                {subjects.map((sub) => (
                  <option key={sub.id} value={sub.id}>
                    {sub.subjectCode} — {sub.subjectName}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="mb-2 block text-xs font-bold uppercase text-[#68716d]">
                Semester
              </label>
              <select
                value={gradeSemester}
                onChange={(e) => setGradeSemester(Number(e.target.value))}
                className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-4 py-2.5 text-sm font-semibold outline-none focus:border-[#075b40]"
              >
                {[1, 2, 3, 4, 5, 6, 7, 8].map((sem) => (
                  <option key={sem} value={sem}>
                    Semester {sem}
                  </option>
                ))}
              </select>
            </div>
          </div>

          {/* Component Marks Breakdown */}
          <div className="rounded-2xl border border-[#e9e8df] bg-[#fcfaf5] p-6">
            <h4 className="mb-4 text-sm font-extrabold uppercase tracking-wider text-[#075b40]">
              Continuous Assessment & Exam Score Breakdown
            </h4>

            <div className="grid gap-5 sm:grid-cols-2 lg:grid-cols-4">
              <div>
                <label className="mb-1 block text-xs font-semibold text-[#17212b]">
                  Internal Assessment (Max 25)
                </label>
                <input
                  type="number"
                  min="0"
                  max="25"
                  step="0.5"
                  value={internalMarks}
                  onChange={(e) => setInternalMarks(Number(e.target.value))}
                  required
                  className="w-full rounded-xl border border-[#e9e8df] bg-white px-4 py-2.5 text-sm font-bold outline-none focus:border-[#075b40]"
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#17212b]">
                  Assignments & Seminars (Max 15)
                </label>
                <input
                  type="number"
                  min="0"
                  max="15"
                  step="0.5"
                  value={assignmentMarks}
                  onChange={(e) => setAssignmentMarks(Number(e.target.value))}
                  required
                  className="w-full rounded-xl border border-[#e9e8df] bg-white px-4 py-2.5 text-sm font-bold outline-none focus:border-[#075b40]"
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#17212b]">
                  Practical / Lab Work (Max 20)
                </label>
                <input
                  type="number"
                  min="0"
                  max="20"
                  step="0.5"
                  value={practicalMarks}
                  onChange={(e) => setPracticalMarks(Number(e.target.value))}
                  required
                  className="w-full rounded-xl border border-[#e9e8df] bg-white px-4 py-2.5 text-sm font-bold outline-none focus:border-[#075b40]"
                />
              </div>

              <div>
                <label className="mb-1 block text-xs font-semibold text-[#17212b]">
                  End-Semester Exam (Max 40)
                </label>
                <input
                  type="number"
                  min="0"
                  max="40"
                  step="0.5"
                  value={examMarks}
                  onChange={(e) => setExamMarks(Number(e.target.value))}
                  required
                  className="w-full rounded-xl border border-[#e9e8df] bg-white px-4 py-2.5 text-sm font-bold outline-none focus:border-[#075b40]"
                />
              </div>
            </div>

            {/* Total Indicator */}
            <div className="mt-6 flex flex-wrap items-center justify-between border-t border-[#e9e8df] pt-4">
              <span className="text-sm font-bold text-[#68716d]">
                Computed Total Mark:
              </span>
              <span className="text-2xl font-black text-[#075b40]">
                {totalGradeScore} / 100
              </span>
            </div>
          </div>

          {gradeMsg && (
            <div className="rounded-xl border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              ✓ {gradeMsg}
            </div>
          )}

          {gradeErr && (
            <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-800">
              ⚠️ {gradeErr}
            </div>
          )}

          <div className="flex justify-end pt-2">
            <button
              type="submit"
              disabled={submittingGrade}
              className="rounded-xl bg-[#075b40] px-8 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:opacity-50"
            >
              {submittingGrade ? "Saving Grade..." : "Save & Publish Grade"}
            </button>
          </div>
        </form>
      )}

      {/* =========================================================================
          MODAL: ADD NEW STUDENT
      ========================================================================= */}
      {showAddStudentModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 p-4 backdrop-blur-sm">
          <div className="w-full max-w-lg rounded-3xl border border-[#e9e8df] bg-white p-7 shadow-2xl">
            <div className="flex items-center justify-between border-b border-[#e9e8df] pb-4">
              <div>
                <h3 className="text-xl font-extrabold text-[#17212b]">
                  Enroll New Student
                </h3>
                <p className="text-xs text-[#68716d]">
                  Create an official campus student record and active login credentials.
                </p>
              </div>
              <button
                type="button"
                onClick={() => setShowAddStudentModal(false)}
                className="text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>

            <form onSubmit={handleAddStudent} className="mt-5 space-y-4">
              <div className="grid gap-3 sm:grid-cols-2">
                <div>
                  <label className="mb-1 block text-xs font-bold text-[#17212b]">
                    Register Number *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. 2026CS102"
                    value={regNo}
                    onChange={(e) => setRegNo(e.target.value)}
                    className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                  />
                </div>

                <div>
                  <label className="mb-1 block text-xs font-bold text-[#17212b]">
                    Full Name *
                  </label>
                  <input
                    type="text"
                    required
                    placeholder="e.g. Deepak Kumar"
                    value={fullName}
                    onChange={(e) => setFullName(e.target.value)}
                    className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                  />
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-bold text-[#17212b]">
                  Email Address *
                </label>
                <input
                  type="email"
                  required
                  placeholder="e.g. deepak@campus.edu"
                  value={studentEmail}
                  onChange={(e) => setStudentEmail(e.target.value)}
                  className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                />
              </div>

              <div className="grid gap-3 sm:grid-cols-3">
                <div>
                  <label className="mb-1 block text-xs font-bold text-[#17212b]">
                    Year
                  </label>
                  <select
                    value={studentYear}
                    onChange={(e) => setStudentYear(Number(e.target.value))}
                    className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                  >
                    {[1, 2, 3, 4].map((y) => (
                      <option key={y} value={y}>
                        Year {y}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="mb-1 block text-xs font-bold text-[#17212b]">
                    Semester
                  </label>
                  <select
                    value={studentSem}
                    onChange={(e) => setStudentSem(Number(e.target.value))}
                    className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                  >
                    {[1, 2, 3, 4, 5, 6, 7, 8].map((s) => (
                      <option key={s} value={s}>
                        Sem {s}
                      </option>
                    ))}
                  </select>
                </div>

                <div>
                  <label className="mb-1 block text-xs font-bold text-[#17212b]">
                    Section
                  </label>
                  <select
                    value={studentSection}
                    onChange={(e) => setStudentSection(e.target.value)}
                    className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                  >
                    {["A", "B", "C"].map((sec) => (
                      <option key={sec} value={sec}>
                        Sec {sec}
                      </option>
                    ))}
                  </select>
                </div>
              </div>

              <div>
                <label className="mb-1 block text-xs font-bold text-[#17212b]">
                  Initial Account Password
                </label>
                <input
                  type="text"
                  value={initialPassword}
                  onChange={(e) => setInitialPassword(e.target.value)}
                  className="w-full rounded-xl border border-[#e9e8df] bg-[#fbfbf8] px-3.5 py-2 text-sm outline-none focus:border-[#075b40]"
                />
                <p className="mt-1 text-xs text-[#68716d]">
                  Student can log into Student Portal using this password.
                </p>
              </div>

              <div className="flex justify-end gap-3 pt-4 border-t border-[#e9e8df]">
                <button
                  type="button"
                  onClick={() => setShowAddStudentModal(false)}
                  className="rounded-xl border border-[#e9e8df] px-4 py-2 text-sm font-semibold text-[#68716d] hover:bg-gray-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  disabled={addingStudent}
                  className="rounded-xl bg-[#075b40] px-6 py-2 text-sm font-bold text-white shadow-sm hover:bg-[#06442f] disabled:opacity-50"
                >
                  {addingStudent ? "Enrolling..." : "Enroll Student"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </section>
  );
}
