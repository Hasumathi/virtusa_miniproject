"use client";

import { useEffect, useState } from "react";
import Link from "next/link";
import { applyAppTheme, type ThemeMode } from "@/components/theme/ThemeInitializer";

type Settings = {
  themePreference: ThemeMode;
  emailNotifications: boolean;
  pushNotifications: boolean;
};

export default function SettingsPage() {
  const [settings, setSettings] = useState<Settings>({
    themePreference: "SYSTEM",
    emailNotifications: true,
    pushNotifications: true,
  });

  const [role, setRole] = useState<string>("STUDENT");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    // Read local theme immediately
    try {
      const local = (localStorage.getItem("edutrack_theme") as ThemeMode) || "SYSTEM";
      setSettings((prev) => ({ ...prev, themePreference: local }));
    } catch {
      // safe fallback
    }

    async function loadSettings() {
      try {
        const [settingsRes, profileRes] = await Promise.all([
          fetch("/api/settings"),
          fetch("/api/profile"),
        ]);

        if (settingsRes.ok) {
          const data = await settingsRes.json();
          const s = data.settings || {};
          const loadedTheme = (s.themePreference as ThemeMode) || "SYSTEM";
          setSettings((prev) => ({
            ...prev,
            emailNotifications: s.emailNotifications ?? true,
            pushNotifications: s.pushNotifications ?? true,
            themePreference: localStorage.getItem("edutrack_theme")
              ? prev.themePreference
              : loadedTheme,
          }));
        }

        if (profileRes.ok) {
          const p = await profileRes.json();
          if (p.user?.role) setRole(p.user.role);
        }
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to load settings.");
      } finally {
        setLoading(false);
      }
    }

    loadSettings();
  }, []);

  function handleThemeChange(newTheme: ThemeMode) {
    setSettings((prev) => ({ ...prev, themePreference: newTheme }));
    applyAppTheme(newTheme);
    setMessage("");

    // Auto-save in background
    try {
      fetch("/api/settings", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          ...settings,
          themePreference: newTheme,
        }),
      }).catch(() => {});
    } catch {
      // safe fallback
    }
  }

  async function saveSettings() {
    setSaving(true);
    setMessage("");
    setError("");

    try {
      const response = await fetch("/api/settings", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify(settings),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to save settings.");
      }

      applyAppTheme(settings.themePreference);
      setMessage("Preferences and settings saved successfully!");
      setTimeout(() => setMessage(""), 4000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to save settings.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f7f5ec] text-[#075b40]">
        Loading settings...
      </main>
    );
  }

  const dashboardHref =
    role === "FACULTY" ? "/faculty-dashboard" : "/student-dashboard";

  return (
    <main className="min-h-screen bg-[#f7f5ec] px-4 py-8 text-[#24352d] sm:px-8 dark:bg-[#0b131e] dark:text-[#f1f5f9]">
      <div className="mx-auto max-w-4xl">
        <header className="mb-8 rounded-3xl border border-[#e3e7dc] bg-white p-6 shadow-sm sm:p-8 dark:border-[#233143] dark:bg-[#16202c]">
          <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#648273] dark:text-[#22c55e]">
                EduTrack
              </p>
              <h1 className="mt-1 text-2xl font-bold text-[#075b40] sm:text-3xl dark:text-[#22c55e]">
                Settings & Preferences
              </h1>
              <p className="mt-1 text-sm text-[#718078] dark:text-[#94a3b8]">
                Manage portal appearance, theme, and notification preferences.
              </p>
            </div>

            <Link
              href={dashboardHref}
              className="inline-flex items-center gap-2 rounded-xl bg-[#edf5ee] px-5 py-2.5 text-sm font-bold text-[#075b40] transition hover:bg-[#dceee2] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
            >
              ← Back to Dashboard
            </Link>
          </div>
        </header>

        <section className="rounded-3xl border border-[#e3e7dc] bg-white p-6 shadow-sm sm:p-9 dark:border-[#233143] dark:bg-[#16202c]">
          {message && (
            <div className="mb-6 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm font-semibold text-green-800 dark:border-green-800 dark:bg-green-950/40 dark:text-green-300">
              ✓ {message}
            </div>
          )}

          {error && (
            <div className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm font-semibold text-red-700 dark:border-red-800 dark:bg-red-950/40 dark:text-red-300">
              ⚠️ {error}
            </div>
          )}

          {/* THEME SECTION */}
          <div className="mb-8">
            <h2 className="text-xl font-bold text-[#075b40] dark:text-[#22c55e]">
              🎨 Appearance & Theme
            </h2>
            <p className="mt-1 text-sm text-[#829087] dark:text-[#94a3b8]">
              Select your preferred visual style for the EduTrack portal.
            </p>

            <div className="mt-4 grid grid-cols-1 gap-4 sm:grid-cols-3">
              <button
                type="button"
                onClick={() => handleThemeChange("LIGHT")}
                className={`flex flex-col items-center justify-center rounded-2xl border p-5 text-center transition-all ${
                  settings.themePreference === "LIGHT"
                    ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                    : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                }`}
              >
                <span className="text-3xl">☀️</span>
                <span className="mt-3 text-sm font-bold">Light Theme</span>
                <span className="mt-1 text-xs text-[#829087] dark:text-[#94a3b8]">Forest Green & Ivory</span>
              </button>

              <button
                type="button"
                onClick={() => handleThemeChange("DARK")}
                className={`flex flex-col items-center justify-center rounded-2xl border p-5 text-center transition-all ${
                  settings.themePreference === "DARK"
                    ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                    : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                }`}
              >
                <span className="text-3xl">🌙</span>
                <span className="mt-3 text-sm font-bold">Dark Theme</span>
                <span className="mt-1 text-xs text-[#829087] dark:text-[#94a3b8]">Deep Charcoal & Neon</span>
              </button>

              <button
                type="button"
                onClick={() => handleThemeChange("SYSTEM")}
                className={`flex flex-col items-center justify-center rounded-2xl border p-5 text-center transition-all ${
                  settings.themePreference === "SYSTEM"
                    ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                    : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                }`}
              >
                <span className="text-3xl">💻</span>
                <span className="mt-3 text-sm font-bold">System Default</span>
                <span className="mt-1 text-xs text-[#829087] dark:text-[#94a3b8]">Sync with your OS</span>
              </button>
            </div>
          </div>

          {/* NOTIFICATION PREFERENCES */}
          <div className="mb-8 border-t border-[#edf0e9] pt-8 dark:border-[#233143]">
            <h2 className="text-xl font-bold text-[#075b40] dark:text-[#22c55e]">
              🔔 Notification Preferences
            </h2>
            <p className="mt-1 text-sm text-[#829087] dark:text-[#94a3b8]">
              Choose how you wish to receive updates regarding attendance, grades, and announcements.
            </p>

            <div className="mt-5 space-y-4">
              <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-[#e7ebe2] bg-[#fcfcf8] p-5 dark:border-[#334155] dark:bg-[#1f2b3b]">
                <div className="max-w-md">
                  <h3 className="font-semibold text-[#24352d] dark:text-white">
                    ✉️ Email Notifications
                  </h3>
                  <p className="mt-1 text-sm text-[#829087] dark:text-[#94a3b8]">
                    Receive academic alerts, exam notifications, and campus updates in your mailbox.
                  </p>
                </div>

                <label className="flex cursor-pointer items-center gap-3">
                  <span className="text-sm font-medium text-[#64736a] dark:text-[#94a3b8]">
                    {settings.emailNotifications ? "Enabled" : "Disabled"}
                  </span>
                  <input
                    type="checkbox"
                    checked={settings.emailNotifications}
                    onChange={(e) =>
                      setSettings((prev) => ({
                        ...prev,
                        emailNotifications: e.target.checked,
                      }))
                    }
                    className="h-5 w-5 cursor-pointer accent-[#075b40]"
                  />
                </label>
              </div>

              <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-[#e7ebe2] bg-[#fcfcf8] p-5 dark:border-[#334155] dark:bg-[#1f2b3b]">
                <div className="max-w-md">
                  <h3 className="font-semibold text-[#24352d] dark:text-white">
                    🔔 In-App Push Alerts
                  </h3>
                  <p className="mt-1 text-sm text-[#829087] dark:text-[#94a3b8]">
                    Receive instant notifications in the portal when attendance is marked or grades are recorded.
                  </p>
                </div>

                <label className="flex cursor-pointer items-center gap-3">
                  <span className="text-sm font-medium text-[#64736a] dark:text-[#94a3b8]">
                    {settings.pushNotifications ? "Enabled" : "Disabled"}
                  </span>
                  <input
                    type="checkbox"
                    checked={settings.pushNotifications}
                    onChange={(e) =>
                      setSettings((prev) => ({
                        ...prev,
                        pushNotifications: e.target.checked,
                      }))
                    }
                    className="h-5 w-5 cursor-pointer accent-[#075b40]"
                  />
                </label>
              </div>
            </div>
          </div>

          {/* SAVE BUTTON */}
          <div className="flex justify-end border-t border-[#edf0e9] pt-6 dark:border-[#233143]">
            <button
              type="button"
              onClick={saveSettings}
              disabled={saving}
              className="rounded-xl bg-[#075b40] px-8 py-3 text-sm font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:cursor-not-allowed disabled:opacity-60 dark:bg-[#22c55e] dark:hover:bg-[#16a34a]"
            >
              {saving ? "Saving Changes..." : "Save Preferences"}
            </button>
          </div>
        </section>
      </div>
    </main>
  );
}
