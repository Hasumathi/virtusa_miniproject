"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";

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

const featureList = [
  {
    icon: "🎓",
    title: "Student Academic Tracking",
    description: "Monitor real-time semester progress, course enrollments, CGPA calculations, and performance records.",
    tag: "Academics",
  },
  {
    icon: "📋",
    title: "Attendance Management",
    description: "Instant class session attendance marking, student percentage analytics, and automated low-attendance warnings below 75%.",
    tag: "Compliance",
  },
  {
    icon: "👩🏻‍🏫",
    title: "Faculty Workspace",
    description: "Streamlined tools for grading, student roster management, course syllabus publication, and departmental coordination.",
    tag: "Administration",
  },
  {
    icon: "📄",
    title: "Instant PDF Reports",
    description: "Export certified PDF transcripts, official student attendance reports, and semester summaries with one click.",
    tag: "Reporting",
  },
  {
    icon: "📢",
    title: "Announcements & Alerts",
    description: "Broadcast campus-wide notices, department circulars, and instant student notifications in real-time.",
    tag: "Communication",
  },
  {
    icon: "🔒",
    title: "Enterprise Security",
    description: "Powered by Spring Security with stateless JWT authorization, BCrypt hashing, and comprehensive database audit logs.",
    tag: "Security",
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
  const [activeTab, setActiveTab] = useState("home");
  const [contactSubmitted, setContactSubmitted] = useState(false);
  const [contactLoading, setContactLoading] = useState(false);

  function handleContactSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setContactLoading(true);
    setTimeout(() => {
      setContactLoading(false);
      setContactSubmitted(true);
      (e.target as HTMLFormElement).reset();
    }, 600);
  }

  return (
    <main>
      {/* HERO SECTION */}
      <section className="hero-section" id="home">
        <header className="site-header">
          <a href="#home" className="logo" onClick={() => setActiveTab("home")}>
            <span className="logo-icon">◆</span>
            <span>EduTrack</span>
          </a>

          <nav className="main-nav">
            <a
              className={activeTab === "home" ? "nav-active" : ""}
              href="#home"
              onClick={() => setActiveTab("home")}
            >
              Home
            </a>
            <a
              className={activeTab === "about" ? "nav-active" : ""}
              href="#about"
              onClick={() => setActiveTab("about")}
            >
              About
            </a>
            <a
              className={activeTab === "features" ? "nav-active" : ""}
              href="#features"
              onClick={() => setActiveTab("features")}
            >
              Features
            </a>
            <a
              className={activeTab === "contact" ? "nav-active" : ""}
              href="#contact"
              onClick={() => setActiveTab("contact")}
            >
              Contact
            </a>
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
              <a
                href="#about"
                className="secondary-button"
                onClick={() => setActiveTab("about")}
              >
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

      {/* PORTALS SECTION */}
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
                Manage courses, track students, publish grades,
                and mark session attendance seamlessly.
              </p>
              <Link href="/faculty-login" className="portal-button">
                Faculty Login <span>→</span>
              </Link>
            </div>
          </article>

          <article className="portal-card student-card">
            <div className="portal-illustration student-illustration">👩🏻‍🎓</div>
            <div className="portal-info">
              <span className="portal-label">STUDENT PORTAL</span>
              <h3>Shape Your Future.</h3>
              <p>
                View your courses, track attendance percentages,
                and download official performance transcripts.
              </p>
              <Link href="/student-login" className="portal-button">
                Student Login <span>→</span>
              </Link>
            </div>
          </article>
        </div>
      </section>

      {/* ABOUT US SECTION */}
      <section className="about-section" id="about">
        <div className="about-container">
          <div className="section-heading">
            <span className="section-label">ABOUT EDUTRACK</span>
            <h2>Connected Campus. Unified Intelligence.</h2>
            <p>
              EduTrack bridges the gap between students, educators, and campus
              administration through real-time data and automated workflows.
            </p>
          </div>

          <div className="about-grid">
            <div className="about-card">
              <div className="about-icon">🎯</div>
              <h3>Our Mission</h3>
              <p>
                To replace fragmented spreadsheets and legacy portals with a
                unified, enterprise-grade academic operating system built on
                Spring Boot and modern Next.js architecture.
              </p>
            </div>

            <div className="about-card">
              <div className="about-icon">⚡</div>
              <h3>Real-Time Performance</h3>
              <p>
                From marking lecture attendance to computing semester GPAs, every
                record is validated instantly against relational PostgreSQL
                constraints with sub-millisecond response times.
              </p>
            </div>

            <div className="about-card">
              <div className="about-icon">🛡️</div>
              <h3>Integrity & Compliance</h3>
              <p>
                Automated 75% attendance threshold monitoring flags students
                at risk before examinations, ensuring academic guidelines are
                transparently upheld.
              </p>
            </div>
          </div>

          <div className="about-stats-banner">
            <div className="about-stat-item">
              <strong>12+</strong>
              <span>Academic Departments</span>
            </div>
            <div className="about-stat-divider"></div>
            <div className="about-stat-item">
              <strong>24/7</strong>
              <span>Student & Faculty Access</span>
            </div>
          </div>
        </div>
      </section>

      {/* FEATURES SECTION */}
      <section className="features-section" id="features">
        <div className="features-intro">
          <span className="section-label">POWERFUL CAPABILITIES</span>
          <h2>Everything you need for seamless campus management.</h2>
          <p>
            Purpose-built modules that bring your entire academic lifecycle
            together in one secure ecosystem.
          </p>
        </div>

        <div className="feature-grid">
          {featureList.map((feature) => (
            <article className="feature-card" key={feature.title}>
              <div className="feature-header">
                <div className="feature-icon">{feature.icon}</div>
                <span className="feature-tag">{feature.tag}</span>
              </div>
              <h3>{feature.title}</h3>
              <p>{feature.description}</p>
            </article>
          ))}
        </div>
      </section>

      {/* CONTACT US SECTION */}
      <section className="contact-section" id="contact">
        <div className="contact-container">
          <div className="section-heading">
            <span className="section-label">GET IN TOUCH</span>
            <h2>Contact Campus Support</h2>
            <p>
              Have questions about EduTrack or need assistance with your portal
              account? We are here to help.
            </p>
          </div>

          <div className="contact-grid">
            {/* Contact Info Side */}
            <div className="contact-info-card">
              <h3>Academic Help Desk</h3>
              <p>
                Reach out to our campus IT and administration team for technical
                inquiries, account authorizations, or general portal support.
              </p>

              <div className="contact-details">
                <div className="contact-item">
                  <span className="contact-item-icon">📍</span>
                  <div>
                    <strong>Campus Location</strong>
                    <p>Department of Computer Science & Engineering, Academic Block 3</p>
                  </div>
                </div>

                <div className="contact-item">
                  <span className="contact-item-icon">📧</span>
                  <div>
                    <strong>Support Email</strong>
                    <p>support@smartcampus.edu</p>
                  </div>
                </div>

                <div className="contact-item">
                  <span className="contact-item-icon">📞</span>
                  <div>
                    <strong>Help Desk Helpline</strong>
                    <p>+91 (044) 2834-5678 / Ext 402</p>
                  </div>
                </div>

                <div className="contact-item">
                  <span className="contact-item-icon">🕒</span>
                  <div>
                    <strong>Operational Hours</strong>
                    <p>Monday – Friday: 8:30 AM – 5:30 PM IST</p>
                  </div>
                </div>
              </div>

              <div className="contact-status-box">
                <span className="status-dot"></span>
                <span>All backend services and database clusters operational</span>
              </div>
            </div>

            {/* Contact Form Side */}
            <div className="contact-form-card">
              {contactSubmitted ? (
                <div className="contact-success-state">
                  <div className="success-icon">✓</div>
                  <h3>Message Sent Successfully!</h3>
                  <p>
                    Thank you for reaching out. Our academic support desk will
                    review your inquiry and respond within 24 business hours.
                  </p>
                  <button
                    type="button"
                    className="primary-button"
                    onClick={() => setContactSubmitted(false)}
                  >
                    Send Another Message
                  </button>
                </div>
              ) : (
                <form onSubmit={handleContactSubmit}>
                  <h3>Send an Inquiry</h3>
                  <p>Fill out the form below and we will get back to you shortly.</p>

                  <div className="form-group">
                    <label>Full Name</label>
                    <input
                      name="name"
                      type="text"
                      placeholder="e.g. Alex Johnson"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Campus Email</label>
                    <input
                      name="email"
                      type="email"
                      placeholder="e.g. student@campus.edu"
                      required
                    />
                  </div>

                  <div className="form-group">
                    <label>Inquiry Topic</label>
                    <select name="topic" defaultValue="general" required>
                      <option value="general">General Inquiry</option>
                      <option value="login">Portal Login or Password Issue</option>
                      <option value="faculty">Faculty Registration Key</option>
                      <option value="attendance">Attendance / Grade Discrepancy</option>
                      <option value="admin">Administrative Support</option>
                    </select>
                  </div>

                  <div className="form-group">
                    <label>Message</label>
                    <textarea
                      name="message"
                      rows={4}
                      placeholder="Describe your inquiry or the issue you are experiencing..."
                      required
                    ></textarea>
                  </div>

                  <button
                    type="submit"
                    className="primary-button contact-submit-btn"
                    disabled={contactLoading}
                  >
                    {contactLoading ? "Sending..." : "Submit Inquiry →"}
                  </button>
                </form>
              )}
            </div>
          </div>
        </div>
      </section>

      {/* FOOTER */}
      <footer className="site-footer">
        <a href="#home" className="footer-logo">
          <span>◆</span> EduTrack
        </a>
        <p>Track. Manage. Excel. Connected Academic Portal.</p>
        <span>© 2026 EduTrack. All rights reserved.</span>
      </footer>
    </main>
  );
}
