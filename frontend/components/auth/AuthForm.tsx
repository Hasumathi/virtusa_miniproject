
"use client";

import { useState, type FormEvent } from "react";
import Link from "next/link";
import { useRouter } from "next/navigation";

type AuthFormProps = {
  role: "Faculty" | "Student";
  mode: "login" | "signup";
};

export default function AuthForm({ role, mode }: AuthFormProps) {
  const router = useRouter();

  const [showPassword, setShowPassword] = useState(false);
  const [message, setMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const isSignup = mode === "signup";

  async function handleSubmit(e: FormEvent<HTMLFormElement>) {
    e.preventDefault();
    setMessage("");

    const formElement = e.currentTarget;
    const form = new FormData(formElement);

    const password = form.get("password") as string;
    const confirmPassword = form.get("confirmPassword") as string;

    if (isSignup && password !== confirmPassword) {
      setMessage("Passwords do not match.");
      return;
    }

    const email = (form.get("email") as string || "").trim().toLowerCase();
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;

    if (!emailRegex.test(email)) {
      setMessage("Please enter a valid email address (e.g. name@campus.edu). Usernames and roll numbers are not accepted.");
      return;
    }

    const payload = isSignup
      ? {
          name: (form.get("name") as string).trim(),
          identity: (form.get("identity") as string).trim(),
          email,
          password,
          role: role.toUpperCase(),
        }
      : {
          email,
          password,
          role: role.toUpperCase(),
        };

    try {
      setLoading(true);

      const response = await fetch(
        isSignup ? "/api/auth/signup" : "/api/auth/login",
        {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify(payload),
        }
      );

      const result = await response.json();

      if (!response.ok) {
        setMessage(
          result.message ||
            (isSignup ? "Signup failed." : "Login failed.")
        );
        return;
      }

      if (isSignup) {
        setMessage("Account created successfully! You can now login.");
        formElement.reset();
      } else {
        setMessage("Login successful! Redirecting...");

        const dashboard =
          role === "Faculty"
            ? "/faculty-dashboard"
            : "/student-dashboard";

        router.replace(dashboard);
        router.refresh();
      }
    } catch (error) {
      console.error("Authentication error:", error);
      setMessage("Unable to connect to the server. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  const loginLink =
    role === "Faculty" ? "/faculty-login" : "/student-login";

  const signupLink =
    role === "Faculty" ? "/faculty-signup" : "/student-signup";

  return (
    <main className="auth-page">
      <Link href="/" className="auth-brand">
        🎓 EduTrack
      </Link>

      <div className="auth-shell">
        <section className="auth-illustration">
          <div className="auth-illustration-content">
            <span className="auth-tag">WELCOME TO EDUTRACK</span>

            <h1>
              Learn. Manage.
              <span> Grow.</span>
            </h1>

            <p>
              Your academic journey, beautifully organized.
              Everything you need in one connected campus.
            </p>

            <div className="auth-illustration-art">
              {role === "Faculty" ? "👩🏻‍🏫" : "🎓"}
            </div>

            <div className="auth-bottom-note">
              ✨ Empowering Education Through Technology
            </div>
          </div>
        </section>

        <section className="auth-form-side">
          <div className="auth-glass-card">
            <div className="auth-logo">🎓 EduTrack</div>

            <h2>
              {isSignup ? `${role} Signup` : `${role} Login`}
            </h2>

            <p className="auth-subtitle">
              {isSignup
                ? "Join our academic community!"
                : "Welcome back! 👋"}
            </p>

            <form onSubmit={handleSubmit}>
              {isSignup && (
                <>
                  <label>Full Name</label>
                  <input
                    name="name"
                    type="text"
                    placeholder="Enter your full name"
                    required
                    minLength={2}
                  />

                  <label>
                    {role === "Faculty" ? "Faculty ID" : "Register Number"}
                  </label>

                  <input
                    name="identity"
                    type="text"
                    placeholder={
                      role === "Faculty"
                        ? "Enter faculty ID"
                        : "Enter register number"
                    }
                    required
                  />
                </>
              )}

              <label>Email Address</label>

              <input
                name="email"
                type="email"
                placeholder={
                  role === "Faculty"
                    ? "e.g. smith@campus.edu"
                    : "e.g. student@campus.edu"
                }
                required
              />

              <label>Password</label>

              <div className="auth-password-field">
                <input
                  name="password"
                  type={showPassword ? "text" : "password"}
                  placeholder="Enter your password"
                  required
                  minLength={8}
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                >
                  {showPassword ? "Hide" : "Show"}
                </button>
              </div>

              {isSignup && (
                <>
                  <label>Confirm Password</label>

                  <input
                    name="confirmPassword"
                    type={showPassword ? "text" : "password"}
                    placeholder="Confirm your password"
                    required
                    minLength={8}
                  />
                </>
              )}

              {!isSignup && (
                <div className="auth-remember">
                  <label>
                    <input type="checkbox" name="remember" />
                    Remember me
                  </label>

                  <span>Forgot password?</span>
                </div>
              )}

              <button
                className="auth-submit"
                type="submit"
                disabled={loading}
              >
                {loading
                  ? "Please wait..."
                  : isSignup
                    ? "Sign Up →"
                    : "Login →"}
              </button>

              {message && (
                <p className="auth-message" role="status">
                  {message}
                </p>
              )}
            </form>

            <div className="auth-divider">OR</div>

            <p className="auth-switch">
              {isSignup
                ? "Already have an account?"
                : "Don't have an account?"}

              <Link href={isSignup ? loginLink : signupLink}>
                {isSignup ? " Login" : " Sign Up"}
              </Link>
            </p>

            <Link href="/" className="auth-home">
              ← Back to Home
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
}
