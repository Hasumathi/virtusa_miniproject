"use client";

import { useState, useEffect } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";
import LogoutButton from "@/components/auth/LogoutButton";
import FacultyActions from "@/components/dashboard/FacultyActions";
import SettingsModal from "@/components/dashboard/SettingsModal";
import { applyAppTheme } from "@/components/theme/ThemeInitializer";

type DashboardShellProps = {
  name: string;
  email: string;
  role: "FACULTY" | "STUDENT";
  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  data?: any;
  backendToken?: string;
};

export default function DashboardShell({
  name,
  email,
  role,
  data,
  backendToken,
}: DashboardShellProps) {
  const isFaculty = role === "FACULTY";

  // Resolve actual student name vs register/roll number
  const displayName = !isFaculty && data?.studentName ? data.studentName : name;
  const rollNumber = !isFaculty
    ? data?.registerNumber || (name !== displayName ? name : "")
    : "";

  // Active Tab state for interactive navigation
  const [facultyTab, setFacultyTab] = useState<"overview" | "students" | "attendance" | "grades">("overview");
  const [studentTab, setStudentTab] = useState<"overview" | "academics" | "announcements">("overview");
  const [isProfileMenuOpen, setIsProfileMenuOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);

  const router = useRouter();

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [liveRecentStudents, setLiveRecentStudents] = useState<any[]>(
    isFaculty && data?.recentlyAddedStudents
      ? data.recentlyAddedStudents.filter(
          // eslint-disable-next-line @typescript-eslint/no-explicit-any
          (s: any) => !s.isDeleted && s.accountStatus !== "INACTIVE"
        )
      : []
  );

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  const [liveRecentGrades, setLiveRecentGrades] = useState<any[]>(
    !isFaculty && data?.recentGrades ? data.recentGrades : []
  );
  const [liveAverageMarks, setLiveAverageMarks] = useState<number | undefined>(
    !isFaculty && data?.averageMarks !== undefined ? data.averageMarks : undefined
  );

  // Initialize theme from localStorage / system preference on mount
  useEffect(() => {
    applyAppTheme();
  }, []);

  // Click outside to close profile dropdown
  useEffect(() => {
    function handleClickOutside(e: MouseEvent) {
      const target = e.target as HTMLElement;
      if (!target.closest("#profile-dropdown-container")) {
        setIsProfileMenuOpen(false);
      }
    }
    if (isProfileMenuOpen) {
      document.addEventListener("click", handleClickOutside);
      return () => document.removeEventListener("click", handleClickOutside);
    }
  }, [isProfileMenuOpen]);

  // Read URL hash on initial mount (e.g., #attendance, #grades)
  useEffect(() => {
    const hash = window.location.hash.toLowerCase();
    if (hash.includes("attendance")) {
      setFacultyTab("attendance");
    } else if (hash.includes("grade")) {
      setFacultyTab("grades");
    } else if (hash.includes("student")) {
      setFacultyTab("students");
    }
  }, []);

  // Action-oriented navigation items
  const facultyNavItems = [
    { id: "overview", label: "Overview" },
    { id: "students", label: "Students" },
    { id: "attendance", label: "Mark Attendance" },
    { id: "grades", label: "Enter Grades" },
    { id: "profile", label: "Profile", href: "/profile" },
  ];

  const studentNavItems = [
    { id: "overview", label: "Overview" },
    { id: "academics", label: "My Courses" },
    { id: "announcements", label: "Notices & Guidelines" },
    { id: "profile", label: "Profile", href: "/profile" },
  ];

  const handleNavClick = (id: string, href?: string) => {
    if (href) {
      window.location.href = href;
      return;
    }

    if (isFaculty) {
      if (id === "overview") {
        setFacultyTab("overview");
        window.scrollTo({ top: 0, behavior: "smooth" });
      } else {
        setFacultyTab(id as "students" | "attendance" | "grades");
        // Scroll smoothly to faculty-actions console
        setTimeout(() => {
          const el = document.getElementById("faculty-actions");
          if (el) {
            el.scrollIntoView({ behavior: "smooth", block: "start" });
          }
        }, 50);
      }
    } else {
      setStudentTab(id as "overview" | "academics" | "announcements");
      setTimeout(() => {
        const el = document.getElementById(id);
        if (el) {
          el.scrollIntoView({ behavior: "smooth", block: "start" });
        }
      }, 50);
    }
  };

  const stats = isFaculty
    ? [
        {
          icon: "ST",
          title: "Total Students",
          value:
            data?.totalStudents !== undefined
              ? String(data.totalStudents)
              : "--",
          note: "Enrolled in department",
        },
        {
          icon: "PR",
          title: "Present Today",
          value:
            data?.presentToday !== undefined
              ? String(data.presentToday)
              : "--",
          note: `Absent today: ${data?.absentToday ?? 0}`,
        },
        {
          icon: "AT",
          title: "Attendance Rate",
          value:
            data?.overallAttendancePercentage !== undefined
              ? `${Number(data.overallAttendancePercentage).toFixed(1)}%`
              : "--",
          note: "Campus overall",
        },
        {
          icon: "AC",
          title: "Avg Performance",
          value:
            data?.averageAcademicPerformance !== undefined
              ? `${Number(data.averageAcademicPerformance).toFixed(1)}`
              : "--",
          note: "Out of 100",
        },
      ]
    : [
        {
          icon: "AT",
          title: "My Attendance",
          value:
            data?.overallAttendancePercentage !== undefined
              ? `${Number(data.overallAttendancePercentage).toFixed(1)}%`
              : "--",
          note: "Minimum threshold: 75%",
        },
        {
          icon: "CR",
          title: "Enrolled Courses",
          value:
            data?.totalEnrolledSubjects !== undefined
              ? String(data.totalEnrolledSubjects)
              : "--",
          note: `Semester ${data?.currentSemester ?? 5}`,
        },
        {
          icon: "GR",
          title: "Average Score",
          value:
            liveAverageMarks !== undefined
              ? `${Number(liveAverageMarks).toFixed(1)}`
              : data?.averageMarks !== undefined
              ? `${Number(data.averageMarks).toFixed(1)}`
              : "--",
          note: "Academic progress",
        },
      ];

  useEffect(() => {
    if (isFaculty && data?.recentlyAddedStudents) {
      setLiveRecentStudents(
        data.recentlyAddedStudents.filter(
          (s: any) => !s.isDeleted && s.accountStatus !== "INACTIVE"
        )
      );
    }
  }, [isFaculty, data?.recentlyAddedStudents]);

  useEffect(() => {
    if (!isFaculty && data) {
      if (data.recentGrades) {
        setLiveRecentGrades(data.recentGrades);
      }
      if (data.averageMarks !== undefined) {
        setLiveAverageMarks(data.averageMarks);
      }
    }
  }, [isFaculty, data]);

  // Client-side refresh for student grades on mount
  useEffect(() => {
    if (!isFaculty && backendToken) {
      fetch("/backend-api/dashboard/student", {
        headers: { Authorization: `Bearer ${backendToken}` },
      })
        .then((res) => (res.ok ? res.json() : null))
        .then((json) => {
          if (json?.data) {
            if (json.data.recentGrades && json.data.recentGrades.length > 0) {
              setLiveRecentGrades(json.data.recentGrades);
            }
            if (json.data.averageMarks !== undefined) {
              setLiveAverageMarks(json.data.averageMarks);
            }
          }
        })
        .catch((err) => console.warn("Could not fetch latest student grades:", err));
    }
  }, [isFaculty, backendToken]);

  const handleDataRefresh = async () => {
    router.refresh();
    if (backendToken) {
      try {
        if (isFaculty) {
          const res = await fetch("/backend-api/students?size=10", {
            headers: { Authorization: `Bearer ${backendToken}` },
          });
          if (res.ok) {
            const json = await res.json();
            if (json.data?.content) {
              setLiveRecentStudents(
                json.data.content.filter(
                  (s: any) => !s.isDeleted && s.accountStatus !== "INACTIVE"
                )
              );
            }
          }
        } else {
          const res = await fetch("/backend-api/dashboard/student", {
            headers: { Authorization: `Bearer ${backendToken}` },
          });
          if (res.ok) {
            const json = await res.json();
            if (json.data) {
              if (json.data.recentGrades) setLiveRecentGrades(json.data.recentGrades);
              if (json.data.averageMarks !== undefined) setLiveAverageMarks(json.data.averageMarks);
            }
          }
        }
      } catch (err) {
        console.warn("Could not refresh live dashboard:", err);
      }
    }
  };

  const recentGrades = liveRecentGrades;

  const navItems = isFaculty ? facultyNavItems : studentNavItems;

  return (
    <main className="min-h-screen bg-[#fbf8f1] text-[#17212b]">
      {/* TOP NAVIGATION */}
      <header className="sticky top-0 z-50 border-b border-[#e9e8df] bg-white/95 shadow-sm backdrop-blur">
        <div className="mx-auto flex w-full max-w-[1440px] flex-wrap items-center justify-between gap-4 px-5 py-3 lg:px-10">
          <Link
            href="/"
            className="flex shrink-0 items-center gap-2 text-2xl font-extrabold tracking-tight text-[#075b40]"
          >
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#edf5ee] text-xl">
              E
            </span>
            EduTrack
          </Link>

          {/* Interactive Nav Buttons with Active Indicators */}
          <nav className="order-3 flex w-full flex-wrap items-center gap-2 border-t border-[#e9e8df] pt-2 sm:order-none sm:w-auto sm:border-0 sm:pt-0">
            {navItems.map((item) => {
              const isActive = isFaculty
                ? facultyTab === item.id
                : studentTab === item.id;

              return (
                <button
                  key={item.label}
                  type="button"
                  onClick={() => handleNavClick(item.id, item.href)}
                  className={`rounded-lg px-3.5 py-1.5 text-sm font-semibold transition-all ${
                    isActive
                      ? "bg-[#edf5ee] text-[#075b40] font-bold shadow-xs"
                      : "text-[#68716d] hover:bg-[#f7f9f5] hover:text-[#075b40]"
                  }`}
                >
                  {item.label}
                </button>
              );
            })}
          </nav>

          {/* USER PROFILE & DROPDOWN WITH LOGOUT */}
          <div id="profile-dropdown-container" className="relative ml-auto flex shrink-0 items-center">
            <button
              type="button"
              onClick={() => setIsProfileMenuOpen((prev) => !prev)}
              className="flex items-center gap-3 rounded-2xl border border-transparent p-1.5 transition hover:border-[#e9e8df] hover:bg-[#f7f9f5]"
              title="Open profile menu"
            >
              <div className="hidden text-right sm:block">
                <p className="max-w-[160px] truncate text-sm font-bold text-[#17212b]">
                  {displayName}
                </p>
                <p className="text-xs text-[#68716d]">
                  {isFaculty
                    ? "Faculty Member"
                    : rollNumber
                    ? `Roll No: ${rollNumber}`
                    : "Student"}
                </p>
              </div>

              <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#dceee2] font-bold text-[#075b40]">
                {displayName.trim().charAt(0).toUpperCase()}
              </div>

              <span className="text-xs text-[#68716d]">▾</span>
            </button>

            {/* Profile Dropdown */}
            {isProfileMenuOpen && (
              <div className="absolute right-0 top-full mt-2 w-64 rounded-2xl border border-[#e9e8df] bg-white p-3 shadow-xl z-50 animate-in fade-in slide-in-from-top-2 duration-150">
                <div className="border-b border-[#f0efe8] px-3 pb-3">
                  <p className="truncate text-sm font-bold text-[#17212b]">{displayName}</p>
                  <p className="truncate text-xs text-[#68716d]">{email}</p>
                  {rollNumber && (
                    <p className="mt-1 text-xs font-semibold text-[#075b40]">
                      Roll No: {rollNumber}
                    </p>
                  )}
                  <span className="mt-1.5 inline-block rounded-md bg-[#edf5ee] px-2 py-0.5 text-[11px] font-semibold text-[#075b40]">
                    {isFaculty ? "Faculty Member" : "Student"}
                  </span>
                </div>

                <div className="py-2">
                  <Link
                    href="/profile"
                    onClick={() => setIsProfileMenuOpen(false)}
                    className="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-sm font-medium text-[#17212b] transition hover:bg-[#f7f9f5]"
                  >
                    <span>👤</span>
                    <span>My Profile & Photo</span>
                  </Link>

                  <button
                    type="button"
                    onClick={() => {
                      setIsProfileMenuOpen(false);
                      setIsSettingsOpen(true);
                    }}
                    className="flex w-full items-center gap-2.5 rounded-xl px-3 py-2 text-left text-sm font-medium text-[#17212b] transition hover:bg-[#f7f9f5]"
                  >
                    <span>⚙️</span>
                    <span>Settings (Theme & Alerts)</span>
                  </button>
                </div>

                <div className="border-t border-[#f0efe8] pt-2">
                  <LogoutButton
                    variant="danger"
                    className="flex w-full items-center gap-2 rounded-xl px-3 py-2 text-left text-sm font-semibold text-red-600 transition hover:bg-red-50"
                  >
                    <span>🚪</span>
                    <span>Logout</span>
                  </LogoutButton>
                </div>
              </div>
            )}
          </div>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <div className="mx-auto w-full max-w-[1440px] px-5 py-8 sm:py-10 lg:px-10">
        {/* WELCOME BANNER - CLEAN INSTITUTIONAL PROFILE */}
        <section
          id="overview"
          className="mb-9 flex flex-col justify-between gap-5 rounded-3xl bg-[#075b40] p-6 text-white shadow-lg sm:p-9 lg:flex-row lg:items-center"
        >
          <div className="min-w-0">
            <p className="mb-3 text-xs font-bold uppercase tracking-[2px] text-[#dceee2]">
              {isFaculty ? "Faculty Management Portal" : "Student Academic Portal"}
            </p>

            <h1 className="break-words text-3xl font-extrabold leading-tight sm:text-4xl">
              Welcome back, {displayName}!
            </h1>

            <p className="mt-3 max-w-2xl text-sm leading-6 text-white/80 sm:text-base">
              {isFaculty
                ? "Manage enrolled students, log session attendance, and enter semester grades."
                : "Stay updated with your courses, attendance percentages, and academic results."}
            </p>
          </div>

          <div className="shrink-0 rounded-2xl border border-white/20 bg-white/10 px-5 py-4 text-left sm:text-right">
            <p className="text-xs text-white/70">
              {isFaculty ? "Faculty Member" : "Student"}
            </p>
            <p className="mt-1 font-bold text-white">{displayName}</p>
            <p className="mt-0.5 text-xs text-white/80">{email}</p>
            {rollNumber && (
              <p className="mt-1 text-xs font-semibold text-emerald-200">
                Roll No: {rollNumber}
              </p>
            )}
            <p className="mt-1 text-xs font-medium text-[#dceee2]">
              {isFaculty
                ? "Department of Computer Science & Engineering"
                : (data?.departmentName || "Undergraduate Degree Program")}
            </p>
          </div>
        </section>

        {/* QUICK ACTION SHORTCUTS FOR FACULTY */}
        {isFaculty && (
          <section className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <button
              type="button"
              onClick={() => handleNavClick("students")}
              className={`flex items-center gap-4 rounded-2xl border p-5 text-left transition-all ${
                facultyTab === "students"
                  ? "border-[#075b40] bg-[#edf5ee] shadow-sm"
                  : "border-[#e9e8df] bg-white hover:border-[#075b40]/50 hover:shadow-xs"
              }`}
            >
              <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-[#075b40] text-xl font-bold text-white">
                👥
              </span>
              <div>
                <p className="text-base font-bold text-[#17212b]">
                  Student Roster
                </p>
                <p className="text-xs text-[#68716d]">
                  Enroll students & view profiles
                </p>
              </div>
            </button>

            <button
              type="button"
              onClick={() => handleNavClick("attendance")}
              className={`flex items-center gap-4 rounded-2xl border p-5 text-left transition-all ${
                facultyTab === "attendance"
                  ? "border-[#075b40] bg-[#edf5ee] shadow-sm"
                  : "border-[#e9e8df] bg-white hover:border-[#075b40]/50 hover:shadow-xs"
              }`}
            >
              <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-[#075b40] text-xl font-bold text-white">
                📋
              </span>
              <div>
                <p className="text-base font-bold text-[#17212b]">
                  Mark Attendance
                </p>
                <p className="text-xs text-[#68716d]">
                  Take daily class session attendance
                </p>
              </div>
            </button>

            <button
              type="button"
              onClick={() => handleNavClick("grades")}
              className={`flex items-center gap-4 rounded-2xl border p-5 text-left transition-all ${
                facultyTab === "grades"
                  ? "border-[#075b40] bg-[#edf5ee] shadow-sm"
                  : "border-[#e9e8df] bg-white hover:border-[#075b40]/50 hover:shadow-xs"
              }`}
            >
              <span className="flex h-12 w-12 shrink-0 items-center justify-center rounded-xl bg-[#075b40] text-xl font-bold text-white">
                🏅
              </span>
              <div>
                <p className="text-base font-bold text-[#17212b]">
                  Enter Grades
                </p>
                <p className="text-xs text-[#68716d]">
                  Continuous assessment & exams
                </p>
              </div>
            </button>
          </section>
        )}

        {/* STATISTICS */}
        <section className="mb-10">
          <div className="mb-5">
            <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
              Live Metrics
            </p>
            <h2 className="mt-2 text-2xl font-extrabold text-[#17212b]">
              Academic Overview
            </h2>
            <p className="mt-1 text-sm text-[#68716d]">
              Real-time academic records and performance indicators.
            </p>
          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-4">
            {stats.map((stat) => (
              <article
                key={stat.title}
                className="min-w-0 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm transition duration-200 hover:-translate-y-1 hover:shadow-md"
              >
                <div className="flex items-center justify-between gap-3">
                  <p className="text-sm font-semibold text-[#68716d]">
                    {stat.title}
                  </p>
                  <span className="flex h-11 w-11 shrink-0 items-center justify-center rounded-xl bg-[#edf5ee] text-sm font-extrabold text-[#075b40]">
                    {stat.icon}
                  </span>
                </div>

                <p className="mt-5 text-3xl font-extrabold text-[#075b40]">
                  {stat.value}
                </p>
                <p className="mt-2 text-xs text-[#68716d]">{stat.note}</p>
              </article>
            ))}
          </div>
        </section>

        {/* FACULTY ACTIONS CONSOLE (ENROLL STUDENT, MARK ATTENDANCE, ENTER GRADES) */}
        {isFaculty && (
          <div className="mb-10">
            <FacultyActions
              backendToken={backendToken}
              initialStudents={liveRecentStudents}
              activeTab={facultyTab === "overview" ? "students" : facultyTab}
              onTabChange={(tab) => setFacultyTab(tab)}
              onDataRefresh={handleDataRefresh}
            />
          </div>
        )}

        {/* ACADEMIC RECORDS */}
        <section id="academics" className="mb-10 scroll-mt-28">
          <div className="mb-5">
            <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
              Academic Records
            </p>
            <h2 className="mt-2 text-2xl font-extrabold text-[#17212b]">
              {isFaculty ? "Enrolled Student Roster" : "Course Records & Performance"}
            </h2>
            <p className="mt-1 text-sm text-[#68716d]">
              {isFaculty
                ? "Students actively registered in your departmental courses."
                : "Your enrolled subjects and academic performance."}
            </p>
          </div>

          {isFaculty && liveRecentStudents.length > 0 ? (
            <div className="overflow-x-auto rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-[#e9e8df] text-xs font-bold uppercase text-[#68716d]">
                    <th className="pb-3">Register No</th>
                    <th className="pb-3">Full Name</th>
                    <th className="pb-3">Email</th>
                    <th className="pb-3">Year / Sem</th>
                    <th className="pb-3">Status</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f0efe8]">
                  {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
                  {liveRecentStudents.map((s: any) => (
                    <tr key={s.id} className="hover:bg-[#fbfbf9]">
                      <td className="py-3 font-semibold text-[#075b40]">
                        {s.registerNumber}
                      </td>
                      <td className="py-3 font-medium">{s.fullName}</td>
                      <td className="py-3 text-[#68716d]">{s.email}</td>
                      <td className="py-3 text-[#68716d]">
                        Year {s.year}, Sem {s.semester}
                      </td>
                      <td className="py-3">
                        <span className="inline-flex rounded-full bg-emerald-50 px-2 py-0.5 text-xs font-semibold text-emerald-700">
                          {s.accountStatus || "ACTIVE"}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          ) : isFaculty ? (
            <div className="rounded-2xl border border-[#e9e8df] bg-white p-8 text-center text-sm text-[#68716d] shadow-sm">
              No active enrolled students found. Use the console above to enroll new students.
            </div>
          ) : !isFaculty && recentGrades.length > 0 ? (
            <div className="overflow-x-auto rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm">
              <div className="mb-4 flex flex-wrap items-center justify-between gap-2">
                <div>
                  <h3 className="text-base font-bold text-[#17212b]">
                    Evaluation & Continuous Assessment Scores
                  </h3>
                  <p className="text-xs text-[#68716d]">
                    Official semester evaluation breakdown recorded by faculty.
                  </p>
                </div>
                <span className="inline-flex items-center rounded-full bg-emerald-50 px-3 py-1 text-xs font-semibold text-emerald-800">
                  ✓ {recentGrades.length} Course Result{recentGrades.length > 1 ? "s" : ""} Published
                </span>
              </div>
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-[#e9e8df] text-xs font-bold uppercase text-[#68716d]">
                    <th className="pb-3">Subject</th>
                    <th className="pb-3 text-center">Internal (25)</th>
                    <th className="pb-3 text-center">Assign (15)</th>
                    <th className="pb-3 text-center">Lab (20)</th>
                    <th className="pb-3 text-center">Exam (40)</th>
                    <th className="pb-3 text-center">Total (100)</th>
                    <th className="pb-3 text-center">Grade</th>
                    <th className="pb-3 text-center">Result</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-[#f0efe8]">
                  {/* eslint-disable-next-line @typescript-eslint/no-explicit-any */}
                  {recentGrades.map((g: any, i: number) => {
                    const isPassed =
                      g.isPassed !== undefined
                        ? Boolean(g.isPassed)
                        : g.passed !== undefined
                        ? Boolean(g.passed)
                        : Number(g.percentage || g.totalMarks) >= 50;

                    return (
                      <tr key={i} className="hover:bg-[#fbfbf9]">
                        <td className="py-3 font-semibold text-[#075b40]">
                          <div>{g.subjectName || g.subjectCode}</div>
                          {g.subjectName && g.subjectCode && (
                            <span className="text-xs font-mono font-normal text-[#68716d]">
                              {g.subjectCode} • Sem {g.semester}
                            </span>
                          )}
                        </td>
                        <td className="py-3 text-center font-medium">{g.internalMarks ?? 0}</td>
                        <td className="py-3 text-center font-medium">{g.assignmentMarks ?? 0}</td>
                        <td className="py-3 text-center font-medium">{g.practicalMarks ?? 0}</td>
                        <td className="py-3 text-center font-medium">{g.examMarks ?? 0}</td>
                        <td className="py-3 text-center font-bold text-[#075b40]">
                          {g.totalMarks ?? 0}
                        </td>
                        <td className="py-3 text-center font-bold">{g.letterGrade || "A"}</td>
                        <td className="py-3 text-center">
                          <span
                            className={`inline-flex rounded-full px-2.5 py-0.5 text-xs font-semibold ${
                              isPassed
                                ? "bg-emerald-50 text-emerald-700"
                                : "bg-red-50 text-red-700"
                            }`}
                          >
                            {isPassed ? "PASSED" : "FAILED"}
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          ) : (
            <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
              <article className="min-w-0 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-7">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#edf5ee] font-extrabold text-[#075b40]">
                  {isFaculty ? "CL" : "CR"}
                </div>
                <h3 className="mt-5 text-lg font-bold">
                  {isFaculty ? "Course & Class Records" : "Enrolled Courses"}
                </h3>
                <p className="mt-2 text-sm leading-6 text-[#68716d]">
                  {isFaculty
                    ? "Manage assigned courses, syllabi, and student registrations through the console above."
                    : "Course outlines and study materials are accessible through your enrolled subjects."}
                </p>
              </article>

              <article className="min-w-0 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-7">
                <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#fbf0e6] font-extrabold text-[#b76528]">
                  AT
                </div>
                <h3 className="mt-5 text-lg font-bold">
                  {isFaculty ? "Attendance Logs" : "My Attendance Logs"}
                </h3>
                <p className="mt-2 text-sm leading-6 text-[#68716d]">
                  {isFaculty
                    ? "Log daily classroom attendance and generate compliance reports."
                    : "Review your daily lecture attendance records and semester percentage."}
                </p>
              </article>
            </div>
          )}
        </section>

        {/* ACADEMIC ANNOUNCEMENTS & GUIDELINES (STUDENT ONLY) */}
        {!isFaculty && (
          <section id="announcements" className="mb-10 scroll-mt-28">
            <div className="rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-8">
              <div className="flex flex-wrap items-center justify-between gap-4 border-b border-[#f0efe8] pb-4">
                <div>
                  <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
                    Campus Updates
                  </p>
                  <h2 className="mt-1 text-xl font-extrabold text-[#17212b]">
                    Academic Notices & Policies
                  </h2>
                </div>
                <span className="rounded-full bg-[#edf5ee] px-3 py-1 text-xs font-bold text-[#075b40]">
                  Session 2026-27
                </span>
              </div>

              <div className="mt-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
                <div className="rounded-xl border border-[#e9e8df] bg-[#fcfcfb] p-4">
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#075b40]">
                    <span className="h-2 w-2 rounded-full bg-[#075b40]"></span>
                    Attendance Rule
                  </div>
                  <p className="mt-2 text-sm font-semibold text-[#17212b]">
                    75% Mandatory Attendance
                  </p>
                  <p className="mt-1 text-xs leading-5 text-[#68716d]">
                    Students must maintain a minimum of 75% attendance across all registered courses to appear for semester exams.
                  </p>
                </div>

                <div className="rounded-xl border border-[#e9e8df] bg-[#fcfcfb] p-4">
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-[#b76528]">
                    <span className="h-2 w-2 rounded-full bg-[#b76528]"></span>
                    Grading Timeline
                  </div>
                  <p className="mt-2 text-sm font-semibold text-[#17212b]">
                    Continuous Assessment
                  </p>
                  <p className="mt-1 text-xs leading-5 text-[#68716d]">
                    Faculty can submit continuous internal assessment marks, assignments, and practicals via the console above.
                  </p>
                </div>

                <div className="rounded-xl border border-[#e9e8df] bg-[#fcfcfb] p-4">
                  <div className="flex items-center gap-2 text-xs font-bold uppercase tracking-wider text-emerald-700">
                    <span className="h-2 w-2 rounded-full bg-emerald-600"></span>
                    Student Accounts
                  </div>
                  <p className="mt-2 text-sm font-semibold text-[#17212b]">
                    Automated Student Credentials
                  </p>
                  <p className="mt-1 text-xs leading-5 text-[#68716d]">
                    When faculty enroll a student, their student portal login credentials are created and synchronized instantly.
                  </p>
                </div>
              </div>
            </div>
          </section>
        )}

        {/* FOOTER */}
        <footer className="flex flex-col items-center justify-between gap-4 border-t border-[#e9e8df] py-7 sm:flex-row">
          <div className="text-center sm:text-left">
            <p className="font-bold text-[#075b40]">EduTrack</p>
            <p className="mt-1 text-xs text-[#68716d]">
              © 2026 EduTrack. Smart Campus Portal.
            </p>
          </div>

          <p className="text-xs text-[#68716d]">
            Logged in as <span className="font-semibold text-[#17212b]">{displayName}</span>
            {rollNumber && ` (Roll No: ${rollNumber})`}
          </p>
        </footer>
      </div>

      {/* SETTINGS MODAL (THEME & NOTIFICATIONS) */}
      <SettingsModal
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
      />
    </main>
  );
}