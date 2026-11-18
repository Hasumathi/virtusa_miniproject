
const stats = [
  { icon: "♟", label: "Total Students", value: "1,248", note: "↗ 12% this year", tone: "green" },
  { icon: "♙", label: "Faculty Members", value: "86", note: "↗ 8% this year", tone: "blue" },
  { icon: "▤", label: "Departments", value: "12", note: "Active departments", tone: "orange" },
];

const menuItems = ["Overview", "Students", "Faculty", "Academics", "Settings"];

const chartBars = [
  { day: "Mon", height: "48%" },
  { day: "Tue", height: "68%" },
  { day: "Wed", height: "56%" },
  { day: "Thu", height: "85%" },
  { day: "Fri", height: "67%" },
  { day: "Sat", height: "94%" },
];

const features = [
  {
    icon: "▣",
    title: "Easy Management",
    description: "Manage student and faculty records effortlessly.",
  },
  {
    icon: "▥",
    title: "Real-Time Analytics",
    description: "Track academic performance and attendance.",
  },
  {
    icon: "◈",
    title: "Secure & Reliable",
    description: "Keep academic information protected.",
  },
];

function DashboardPreview() {
  return (
    <div className="dashboard-scene">
      <div className="dashboard-window">
        <div className="dashboard-topbar">
          <div className="dashboard-brand">
            <span className="brand-mark">◆</span>
            <strong>EduTrack</strong>
          </div>
          <div className="dashboard-tools">
            <span className="search-pill">⌕ &nbsp; Search...</span>
            <span className="notification">♧</span>
            <span className="admin-avatar">A</span>
          </div>
        </div>

        <div className="dashboard-main">
          <aside className="dashboard-sidebar">
            {menuItems.map((item, index) => (
              <div
                className={`dashboard-menu-item ${index === 0 ? "active" : ""}`}
                key={item}
              >
                <span>{["⌂", "♟", "♙", "▤", "⚙"][index]}</span>
                {item}
              </div>
            ))}
          </aside>

          <div className="dashboard-content">
            <div className="dashboard-heading">
              <div>
                <span className="eyebrow">OVERVIEW</span>
                <h3>Dashboard</h3>
                <p>Welcome back, Admin!</p>
              </div>
              <span className="academic-year">Academic Year 2026–27⌄</span>
            </div>

            <div className="stat-grid">
              {stats.map((stat) => (
                <div className="stat-card" key={stat.label}>
                  <span className={`stat-icon ${stat.tone}`}>{stat.icon}</span>
                  <span className="stat-label">{stat.label}</span>
                  <strong>{stat.value}</strong>
                  <small>{stat.note}</small>
                </div>
              ))}
            </div>

            <div className="dashboard-bottom">
              <div className="performance-card">
                <div className="chart-heading">
                  <strong>Academic Performance</strong>
                  <span>2026</span>
                </div>
                <div className="chart-area">
                  <div className="chart-scale">
                    <span>100</span>
                    <span>75</span>
                    <span>50</span>
                    <span>25</span>
                    <span>0</span>
                  </div>
                  <div className="chart-bars">
                    {chartBars.map((bar) => (
                      <div className="chart-column" key={bar.day}>
                        <div className="chart-track">
                          <div
                            className="chart-bar"
                            style={{ height: bar.height }}
                          />
                        </div>
                        <span>{bar.day}</span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>

              <div className="attendance-card">
                <strong>Attendance</strong>
                <div className="attendance-ring">
                  <div>
                    <b>92%</b>
                    <span>Present</span>
                  </div>
                </div>
                <small>Overall attendance</small>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="system-status">
        <span className="status-check">✓</span>
        <div>
          <strong>All systems active</strong>
          <small>Academic data is up to date</small>
        </div>
      </div>
    </div>
  );
}

export default function Home() {
  return (
    <main>
      <section className="hero-section" id="home">
        <header className="site-header">
          <a href="#home" className="logo">
            <span className="logo-icon">◆</span>
            <span>EduTrack</span>
          </a>

          <nav className="main-nav">
            <a className="nav-active" href="#home">Home</a>
            <a href="#about">About</a>
            <a href="#features">Features</a>
            <a href="#contact">Contact</a>
          </nav>

          <a className="login-button" href="#portals">
            Login <span>→</span>
          </a>
        </header>

        <div className="hero-content">
          <div className="hero-copy">
            <span className="hero-badge">✦ &nbsp; Smart Education Platform</span>
            <h1>
              Edu<span>Track</span>
            </h1>
            <h2>Track. Manage. Excel.</h2>
            <p>
              A modern academic management system
              <br className="desktop-break" />
              for students, faculty and administrators.
            </p>

            <div className="hero-actions">
              <a href="#portals" className="primary-button">
                Get Started <span>→</span>
              </a>
              <a href="#about" className="secondary-button">
                Learn More
              </a>
            </div>

             
            <div
              className="hero-trust"
              style={{
                display: "flex",
                alignItems: "center",
                gap: "12px",
                marginTop: "24px",
                position: "relative",
                zIndex: 10,
                width: "fit-content",
                padding: "8px 14px",
                borderRadius: "30px",
                background: "rgba(255, 255, 255, 0.88)",
                color: "#075B40",
                textShadow: "0 1px 2px rgba(0, 0, 0, 0.18)",
                fontWeight: 700,
              }}
            >
              <div className="trust-icons">
                <span>👨🏻‍🎓</span>
                <span>👩🏻‍🏫</span>
                <span>👨🏽‍🎓</span>
              </div>
              <span>Trusted by students and faculty</span>
            </div>

            <div
              className="hero-tagline"
              style={{
                display: "block",
                width: "fit-content",
                marginTop: "24px",
                position: "relative",
                zIndex: 10,
                transform: "none",
                fontStyle: "italic",
                color: "#A8C9A9",
                textShadow: "0 1px 3px rgba(0, 0, 0, 0.15)",
              }}
            >
              Learn. Manage. Grow.
            </div>
 
          </div>

          <DashboardPreview />
        </div>
      </section>

      <section className="portals-section" id="portals">
        <div className="section-heading">
          <span className="section-label">OUR PORTALS</span>
          <h2>Choose Your Portal</h2>
          <p>One platform. Two dedicated academic experiences.</p>
        </div>

        <div className="portal-grid">
          <article className="portal-card faculty-card">
            <div className="portal-illustration faculty-illustration">👨🏻‍🏫</div>
            <div className="portal-info">
              <span className="portal-label">FACULTY PORTAL</span>
              <h3>Empower Education.</h3>
              <p>
                Manage courses, track students,
                and access academic tools.
              </p>
              <a href="/faculty-login" className="portal-button">
                Faculty Login <span>→</span>
              </a>
            </div>
          </article>

          <article className="portal-card student-card">
            <div className="portal-illustration student-illustration">👩🏻‍🎓</div>
            <div className="portal-info">
              <span className="portal-label">STUDENT PORTAL</span>
              <h3>Shape Your Future.</h3>
              <p>
                View your courses, attendance,
                and academic progress.
              </p>
              <a href="/student-login" className="portal-button">
                Student Login <span>→</span>
              </a>
            </div>
          </article>
        </div>
      </section>

      <section className="features-section" id="features">
        <div className="features-intro">
          <span className="section-label">WHY CHOOSE US</span>
          <h2>Everything you need for better academic management.</h2>
          <p>
            Simple tools that bring your entire academic workspace together.
          </p>
        </div>

        <div className="feature-grid">
          {features.map((feature) => (
            <article className="feature-card" key={feature.title}>
              <div className="feature-icon">{feature.icon}</div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </article>
          ))}
        </div>
      </section>

      <footer className="site-footer" id="contact">
        <a href="#home" className="footer-logo">
          <span>◆</span> EduTrack
        </a>
        <p>Track. Manage. Excel.</p>
        <span>© 2026 EduTrack. All rights reserved.</span>
      </footer>
    </main>
  );
}
