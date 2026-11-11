
"use client";

import { useEffect, useMemo, useState } from "react";

const API_URL = "http://localhost:8080/api/students";

type Student = {
  id: number;
  name: string;
  email: string;
  department: string;
  yearOfStudy: number;
  phoneNumber: string;
};

function formatStudentId(id: number) {
  return `STU${String(id).padStart(3, "0")}`;
}

function formatYear(year: number) {
  const suffix =
    year === 1 ? "st" : year === 2 ? "nd" : year === 3 ? "rd" : "th";

  return `${year}${suffix} Year`;
}

export default function Home() {
  const [students, setStudents] = useState<Student[]>([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedDepartment, setSelectedDepartment] = useState("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // GET API: Fetch all students
  useEffect(() => {
    const controller = new AbortController();

    async function fetchStudents() {
      try {
        setLoading(true);
        setError("");

        const response = await fetch(API_URL, {
          method: "GET",
          headers: {
            Accept: "application/json",
          },
          signal: controller.signal,
        });

        if (!response.ok) {
          throw new Error(`Server error: ${response.status}`);
        }

        const data: Student[] = await response.json();

        if (!Array.isArray(data)) {
          throw new Error("Invalid student data received from server.");
        }

        setStudents(data);
      } catch (err) {
        if (err instanceof Error && err.name === "AbortError") {
          return;
        }

        setError(
          err instanceof Error
            ? err.message
            : "Unable to connect to the backend."
        );
      } finally {
        if (!controller.signal.aborted) {
          setLoading(false);
        }
      }
    }

    fetchStudents();

    return () => controller.abort();
  }, []);

  // Get departments dynamically from API records
  const departments = useMemo(
    () => [...new Set(students.map((student) => student.department))].sort(),
    [students]
  );

  // Search and department filtering
  const filteredStudents = useMemo(() => {
    const search = searchTerm.toLowerCase().trim();

    return students.filter((student) => {
      const studentId = formatStudentId(student.id).toLowerCase();

      const matchesSearch =
        studentId.includes(search) ||
        student.name.toLowerCase().includes(search) ||
        student.email.toLowerCase().includes(search) ||
        student.department.toLowerCase().includes(search) ||
        student.phoneNumber.includes(search);

      const matchesDepartment =
        selectedDepartment === "" ||
        student.department === selectedDepartment;

      return matchesSearch && matchesDepartment;
    });
  }, [students, searchTerm, selectedDepartment]);

  const clearFilters = () => {
    setSearchTerm("");
    setSelectedDepartment("");
  };

  return (
    <div className="dashboard">
      {/* SIDEBAR */}
      <aside className="sidebar">
        <div className="brand">
          <div className="brand-icon">S</div>
          <span>
            Student<span className="brand-light">Hub</span>
          </span>
        </div>

        <div className="sidebar-menu">
          <p className="menu-label">WORKSPACE</p>

          <nav className="navigation">
            <a className="nav-link active" href="#">
              <span className="nav-icon">▦</span>
              Dashboard
            </a>

            <a className="nav-link" href="#students">
              <span className="nav-icon">♙</span>
              Students
            </a>

            <a className="nav-link" href="#students">
              <span className="nav-icon">▤</span>
              Departments
            </a>
          </nav>
        </div>

        <div className="sidebar-bottom">
          <div className="sidebar-help">
            <span className="help-icon">✦</span>
            <div>
              <strong>StudentHub</strong>
              <small>Manage. Monitor. Grow.</small>
            </div>
          </div>

          <div className="profile">
            <div className="avatar">R</div>
            <div className="profile-info">
              <strong>Team 8</strong>
              <small>Administrator</small>
            </div>
            <span className="profile-menu">•••</span>
          </div>
        </div>
      </aside>

      {/* MAIN CONTENT */}
      <main className="main-content">
        <header className="topbar">
          <div className="breadcrumb">
            <span>Workspace</span>
            <span className="breadcrumb-separator">/</span>
            <strong>Dashboard</strong>
          </div>

          <div className="topbar-right">
            <div className="system-status">
              <span
                className="status-dot"
                style={{ backgroundColor: error ? "#ef4444" : "#22c55e" }}
              ></span>
              {error ? "Backend Offline" : loading ? "Connecting..." : "System Online"}
            </div>

            <div className="topbar-avatar">T</div>
          </div>
        </header>

        <section className="page-content">
          {/* WELCOME SECTION */}
          <div className="welcome-section">
            <div>
              <div className="welcome-tag">
                <span>✦</span> STUDENT MANAGEMENT PORTAL
              </div>

              <h1>
                Welcome back, <span>Team 8</span>
              </h1>

              <p>
                Here is what is happening with your students today.
              </p>
            </div>

            <div className="academic-card">
              <div className="academic-icon">▣</div>
              <div>
                <small>ACADEMIC SESSION</small>
                <strong>2026 – 2027</strong>
              </div>
            </div>
          </div>

          {/* STATISTICS */}
          <section className="stats-grid">
            <div className="stat-card stat-blue">
              <div className="stat-top">
                <div className="stat-icon">♙</div>
                <span className="stat-symbol">↗</span>
              </div>
              <p>Total Students</p>
              <h2>{loading ? "..." : error ? "—" : students.length}</h2>
              <small>Registered students</small>
            </div>

            <div className="stat-card stat-purple">
              <div className="stat-top">
                <div className="stat-icon">▦</div>
                <span className="stat-symbol">✦</span>
              </div>
              <p>Departments</p>
              <h2>{loading ? "..." : error ? "—" : departments.length}</h2>
              <small>Registered departments</small>
            </div>

            <div className="stat-card stat-green">
              <div className="stat-top">
                <div className="stat-icon">✓</div>
                <span className="stat-symbol">↗</span>
              </div>
              <p>Year 1 Students</p>
              <h2>
                {loading
                  ? "..."
                  : error
                    ? "—"
                    : students.filter((s) => s.yearOfStudy === 1).length}
              </h2>
              <small>First year students</small>
            </div>

            <div className="stat-card stat-orange">
              <div className="stat-top">
                <div className="stat-icon">＋</div>
                <span className="stat-symbol">✦</span>
              </div>
              <p>Year 2 Students</p>
              <h2>
                {loading
                  ? "..."
                  : error
                    ? "—"
                    : students.filter((s) => s.yearOfStudy === 2).length}
              </h2>
              <small>Second year students</small>
            </div>
          </section>

          {/* STUDENT RECORDS */}
          <section className="students-section" id="students">
            <div className="section-heading">
              <div>
                <div className="section-title-row">
                  <h2>Student Directory</h2>
                  <span className="record-count">
                    {loading ? "Loading..." : `${filteredStudents.length} records`}
                  </span>
                </div>
                <p>View and manage your registered student information.</p>
              </div>

              <button className="add-button" type="button">
                <span>＋</span> Add Student
              </button>
            </div>

            {/* SEARCH AND FILTER */}
            <div className="table-toolbar">
              <div className="search-box">
                <span className="search-icon">⌕</span>
                <input
                  type="search"
                  placeholder="Search by name, ID, email or phone..."
                  aria-label="Search students"
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  disabled={loading || !!error}
                />
              </div>

              <select
                aria-label="Filter by department"
                value={selectedDepartment}
                onChange={(e) => setSelectedDepartment(e.target.value)}
                disabled={loading || !!error}
              >
                <option value="">All Departments</option>
                {departments.map((department) => (
                  <option key={department} value={department}>
                    {department}
                  </option>
                ))}
              </select>
            </div>

            {/* TABLE */}
            <div className="table-container">
              <table>
                <thead>
                  <tr>
                    <th>STUDENT ID</th>
                    <th>STUDENT NAME</th>
                    <th>EMAIL ADDRESS</th>
                    <th>DEPARTMENT</th>
                    <th>YEAR</th>
                    <th>PHONE NUMBER</th>
                  </tr>
                </thead>

                <tbody>
                  {loading ? (
                    <tr>
                      <td colSpan={6}>
                        <div className="empty-state">
                          <strong>Loading students...</strong>
                          <p>Connecting to the StudentHub backend.</p>
                        </div>
                      </td>
                    </tr>
                  ) : error ? (
                    <tr>
                      <td colSpan={6}>
                        <div className="empty-state">
                          <div className="empty-icon">!</div>
                          <strong>Unable to load students</strong>
                          <p>{error}</p>
                          <p>Make sure the Spring Boot backend is running on port 8080.</p>
                          <button
                            type="button"
                            onClick={() => window.location.reload()}
                          >
                            Try Again
                          </button>
                        </div>
                      </td>
                    </tr>
                  ) : filteredStudents.length > 0 ? (
                    filteredStudents.map((student) => (
                      <tr key={student.id}>
                        <td>
                          <span className="student-id">
                            {formatStudentId(student.id)}
                          </span>
                        </td>

                        <td>
                          <div className="student-cell">
                            <div className="student-avatar">
                              {student.name.charAt(0).toUpperCase()}
                            </div>
                            <span className="student-name">
                              {student.name}
                            </span>
                          </div>
                        </td>

                        <td className="email-cell">{student.email}</td>

                        <td>
                          <span className="department-tag">
                            {student.department}
                          </span>
                        </td>

                        <td>{formatYear(student.yearOfStudy)}</td>
                        <td>{student.phoneNumber}</td>
                      </tr>
                    ))
                  ) : (
                    <tr>
                      <td colSpan={6}>
                        <div className="empty-state">
                          <div className="empty-icon">⌕</div>
                          <strong>No students found</strong>
                          <p>
                            Try changing your search or department filter.
                          </p>
                          <button type="button" onClick={clearFilters}>
                            Clear filters
                          </button>
                        </div>
                      </td>
                    </tr>
                  )}
                </tbody>
              </table>
            </div>

            <div className="table-footer">
              <span>
                Showing <strong>{filteredStudents.length}</strong> of{" "}
                <strong>{students.length}</strong> student records
              </span>

              <span className="footer-note">
                StudentHub Management Portal
              </span>
            </div>
          </section>

          <footer className="page-footer">
            <span>© 2026 StudentHub</span>
            <span>Built for smarter student management ✦</span>
          </footer>
        </section>
      </main>
    </div>
  );
}
