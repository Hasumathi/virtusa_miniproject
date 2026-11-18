
import Link from "next/link";
import LogoutButton from "@/components/auth/LogoutButton";

type DashboardShellProps = {
  name: string;
  email: string;
  role: "FACULTY" | "STUDENT";
};

export default function DashboardShell({
  name,
  email,
  role,
}: DashboardShellProps) {
  const isFaculty = role === "FACULTY";

  const navigation = [
    { label: "Overview", href: "#overview" },
    {
      label: isFaculty ? "My Classes" : "My Courses",
      href: "#academics",
    },
    { label: "Activity", href: "#activity" },
    { label: "Profile", href: "/profile" },
    { label: "Settings", href: "/settings" },
  ];

  const stats = isFaculty
    ? [
        {
          icon: "CL",
          title: "My Classes",
          value: "--",
          note: "Class information",
        },
        {
          icon: "ST",
          title: "Total Students",
          value: "--",
          note: "Student records",
        },
        {
          icon: "AT",
          title: "Attendance",
          value: "--",
          note: "Attendance summary",
        },
      ]
    : [
        {
          icon: "CR",
          title: "My Courses",
          value: "--",
          note: "Enrolled courses",
        },
        {
          icon: "AT",
          title: "Attendance",
          value: "--",
          note: "Attendance summary",
        },
        {
          icon: "AS",
          title: "Assignments",
          value: "--",
          note: "Assignment status",
        },
      ];

  return (
    <main className="min-h-screen bg-[#fbf8f1] text-[#17212b]">
      {/* TOP NAVIGATION */}
      <header className="sticky top-0 z-50 border-b border-[#e9e8df] bg-white/95 shadow-sm backdrop-blur">
        <div className="mx-auto flex w-full max-w-[1440px] flex-wrap items-center justify-between gap-4 px-5 py-4 lg:px-10">
          <Link
            href="/"
            className="flex shrink-0 items-center gap-2 text-2xl font-extrabold tracking-tight text-[#075b40]"
          >
            <span className="flex h-10 w-10 items-center justify-center rounded-xl bg-[#edf5ee] text-xl">
              E
            </span>
            EduTrack
          </Link>

          <nav className="order-3 flex w-full flex-wrap items-center gap-x-6 gap-y-3 border-t border-[#e9e8df] pt-3 sm:order-none sm:w-auto sm:border-0 sm:pt-0">
            {navigation.map((item, index) => (
              <Link
                key={item.label}
                href={item.href}
                className={`whitespace-nowrap text-sm font-semibold transition hover:text-[#075b40] ${
                  index === 0 ? "text-[#075b40]" : "text-[#68716d]"
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>

          <div className="ml-auto flex shrink-0 items-center gap-3">
            <div className="hidden text-right sm:block">
              <p className="max-w-[160px] truncate text-sm font-bold text-[#17212b]">
                {name}
              </p>
              <p className="text-xs text-[#68716d]">
                {isFaculty ? "Faculty" : "Student"}
              </p>
            </div>

            <div className="flex h-10 w-10 shrink-0 items-center justify-center rounded-full bg-[#dceee2] font-bold text-[#075b40]">
              {name.trim().charAt(0).toUpperCase()}
            </div>
          </div>
        </div>
      </header>

      {/* MAIN CONTENT */}
      <div className="mx-auto w-full max-w-[1440px] px-5 py-8 sm:py-10 lg:px-10">
        {/* WELCOME */}
        <section
          id="overview"
          className="mb-9 flex flex-col justify-between gap-5 rounded-3xl bg-[#075b40] p-6 text-white shadow-lg sm:p-9 lg:flex-row lg:items-center"
        >
          <div className="min-w-0">
            <p className="mb-3 text-xs font-bold uppercase tracking-[2px] text-[#dceee2]">
              {isFaculty ? "Faculty Portal" : "Student Portal"}
            </p>

            <h1 className="break-words text-3xl font-extrabold leading-tight sm:text-4xl">
              Welcome back, {name}!
            </h1>

            <p className="mt-3 max-w-2xl text-sm leading-6 text-white/80 sm:text-base">
              {isFaculty
                ? "Manage your teaching workspace and keep track of your academic activities."
                : "Stay updated with your courses, attendance and academic activities."}
            </p>
          </div>

          <div className="shrink-0 rounded-2xl border border-white/20 bg-white/10 px-5 py-4">
            <p className="text-xs text-white/70">Your account</p>
            <p className="mt-1 font-bold">
              {isFaculty ? "Faculty Account" : "Student Account"}
            </p>
            <p className="mt-1 max-w-[220px] truncate text-xs text-white/75">
              {email}
            </p>
          </div>
        </section>

        {/* STATISTICS */}
        <section className="mb-10">
          <div className="mb-5">
            <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
              At a glance
            </p>
            <h2 className="mt-2 text-2xl font-extrabold">
              Dashboard Overview
            </h2>
            <p className="mt-1 text-sm text-[#68716d]">
              Your academic summary in one place.
            </p>
          </div>

          <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 xl:grid-cols-3">
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
                <p className="mt-2 text-xs text-[#68716d]">
                  {stat.note}
                </p>
              </article>
            ))}
          </div>
        </section>

        {/* ACADEMIC WORKSPACE */}
        <section id="academics" className="mb-10 scroll-mt-28">
          <div className="mb-5">
            <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
              Academic workspace
            </p>
            <h2 className="mt-2 text-2xl font-extrabold">
              {isFaculty ? "Teaching Workspace" : "Learning Workspace"}
            </h2>
            <p className="mt-1 text-sm text-[#68716d]">
              {isFaculty
                ? "Your classes and teaching activities."
                : "Your courses and learning activities."}
            </p>
          </div>

          <div className="grid grid-cols-1 gap-5 lg:grid-cols-2">
            <article className="min-w-0 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-7">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#edf5ee] font-extrabold text-[#075b40]">
                {isFaculty ? "CL" : "CR"}
              </div>

              <h3 className="mt-5 text-lg font-bold">
                {isFaculty ? "My Classes" : "My Courses"}
              </h3>

              <p className="mt-2 text-sm leading-6 text-[#68716d]">
                {isFaculty
                  ? "Your assigned classes and student information will be displayed here."
                  : "Your enrolled courses and learning materials will be displayed here."}
              </p>

              <span className="mt-5 inline-flex rounded-full bg-[#edf5ee] px-3 py-2 text-xs font-bold text-[#075b40]">
                Academic module coming soon
              </span>
            </article>

            <article className="min-w-0 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-7">
              <div className="flex h-12 w-12 items-center justify-center rounded-xl bg-[#fbf0e6] font-extrabold text-[#b76528]">
                AT
              </div>

              <h3 className="mt-5 text-lg font-bold">
                {isFaculty ? "Attendance Management" : "My Attendance"}
              </h3>

              <p className="mt-2 text-sm leading-6 text-[#68716d]">
                {isFaculty
                  ? "Attendance records and class-wise summaries will be available here."
                  : "Your attendance records and academic participation will be available here."}
              </p>

              <span className="mt-5 inline-flex rounded-full bg-[#fbf0e6] px-3 py-2 text-xs font-bold text-[#b76528]">
                Attendance module coming soon
              </span>
            </article>
          </div>
        </section>

        {/* ACTIVITY */}
        <section id="activity" className="mb-10 scroll-mt-28">
          <div className="rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm sm:p-7">
            <p className="text-xs font-bold uppercase tracking-[1.5px] text-[#075b40]">
              Updates
            </p>
            <h2 className="mt-2 text-xl font-extrabold">
              Recent Activity
            </h2>
            <p className="mt-2 text-sm leading-6 text-[#68716d]">
              Your recent academic updates and notifications will appear here.
            </p>

            <div className="mt-5 rounded-xl bg-[#f5f7f1] p-4">
              <p className="text-sm font-semibold text-[#17212b]">
                No recent activity yet
              </p>
              <p className="mt-1 text-xs text-[#68716d]">
                New updates will be shown here when available.
              </p>
            </div>
          </div>
        </section>

        {/* PROFILE AND SETTINGS */}
        <section className="mb-10 grid grid-cols-1 gap-5 lg:grid-cols-2">
          <article
            id="profile"
            className="scroll-mt-28 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm"
          >
            <h2 className="text-lg font-bold">Profile</h2>
            <p className="mt-2 break-all text-sm text-[#68716d]">
              {name}
            </p>
            <p className="mt-1 break-all text-sm text-[#68716d]">
              {email}
            </p>
            <p className="mt-3 text-xs font-semibold text-[#075b40]">
              Profile management module coming soon
            </p>
          </article>

          <article
            id="settings"
            className="scroll-mt-28 rounded-2xl border border-[#e9e8df] bg-white p-6 shadow-sm"
          >
            <h2 className="text-lg font-bold">Settings</h2>
            <p className="mt-2 text-sm leading-6 text-[#68716d]">
              Manage your account preferences and notification settings.
            </p>
            <p className="mt-3 text-xs font-semibold text-[#075b40]">
              Settings module coming soon
            </p>
          </article>
        </section>

        {/* FOOTER */}
        <footer className="flex flex-col items-center justify-between gap-4 border-t border-[#e9e8df] py-7 sm:flex-row">
          <div className="text-center sm:text-left">
            <p className="font-bold text-[#075b40]">EduTrack</p>
            <p className="mt-1 text-xs text-[#68716d]">
              © 2026 EduTrack. Academic Management Portal.
            </p>
          </div>

          <LogoutButton />
        </footer>
      </div>
    </main>
  );
}
