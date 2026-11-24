"use client";

import { useEffect, useState } from "react";
import { applyAppTheme, type ThemeMode } from "@/components/theme/ThemeInitializer";

type SettingsModalProps = {
  isOpen: boolean;
  onClose: () => void;
};

export default function SettingsModal({ isOpen, onClose }: SettingsModalProps) {
  const [theme, setTheme] = useState<ThemeMode>("SYSTEM");
  const [emailNotifs, setEmailNotifs] = useState(true);
  const [pushNotifs, setPushNotifs] = useState(true);
  const [loading, setLoading] = useState(false);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  // Load saved settings on modal open
  useEffect(() => {
    if (!isOpen) return;

    // Immediately reflect saved local theme without waiting for network
    try {
      const localTheme = (localStorage.getItem("edutrack_theme") as ThemeMode) || "SYSTEM";
      setTheme(localTheme);
    } catch {
      // Safe fallback
    }

    async function loadSettings() {
      setMessage("");
      setError("");

      try {
        const res = await fetch("/api/settings");
        if (res.ok) {
          const data = await res.json();
          const s = data.settings || {};
          setEmailNotifs(s.emailNotifications ?? true);
          setPushNotifs(s.pushNotifications ?? true);

          // If backend has a theme preference and local storage is empty, sync it
          if (s.themePreference && !localStorage.getItem("edutrack_theme")) {
            const loadedTheme = s.themePreference as ThemeMode;
            setTheme(loadedTheme);
            applyAppTheme(loadedTheme);
          }
        }
      } catch (err) {
        console.warn("Could not load user settings:", err);
      }
    }

    loadSettings();
  }, [isOpen]);

  // Handle immediate theme change with auto-save
  async function handleThemeChange(newTheme: ThemeMode) {
    setTheme(newTheme);
    applyAppTheme(newTheme);
    setMessage("");

    // Auto-save in background to backend
    try {
      fetch("/api/settings", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          themePreference: newTheme,
          emailNotifications: emailNotifs,
          pushNotifications: pushNotifs,
        }),
      }).catch(() => {});
    } catch {
      // safe fallback
    }
  }

  async function handleSave() {
    setSaving(true);
    setMessage("");
    setError("");

    try {
      const res = await fetch("/api/settings", {
        method: "PATCH",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          themePreference: theme,
          emailNotifications: emailNotifs,
          pushNotifications: pushNotifs,
        }),
      });

      const data = await res.json();

      if (!res.ok) {
        throw new Error(data.message || "Failed to update preferences.");
      }

      applyAppTheme(theme);
      setMessage("Preferences saved successfully!");
      setTimeout(() => {
        setMessage("");
      }, 3000);
    } catch (err) {
      setError(err instanceof Error ? err.message : "Error saving preferences.");
    } finally {
      setSaving(false);
    }
  }

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4 backdrop-blur-sm animate-in fade-in duration-150">
      <div className="w-full max-w-lg overflow-hidden rounded-3xl border border-[#e9e8df] bg-white p-6 shadow-2xl sm:p-8 dark:border-[#334155] dark:bg-[#16202c]">
        {/* MODAL HEADER */}
        <div className="flex items-center justify-between border-b border-[#f0efe8] pb-4 dark:border-[#233143]">
          <div>
            <h3 className="text-xl font-extrabold text-[#17212b] dark:text-white">
              ⚙️ Account Settings
            </h3>
            <p className="mt-1 text-xs text-[#68716d] dark:text-[#94a3b8]">
              Personalize your portal theme and notification preferences.
            </p>
          </div>
          <button
            type="button"
            onClick={onClose}
            className="flex h-9 w-9 items-center justify-center rounded-xl bg-[#f2f4ec] text-lg font-bold text-[#68716d] transition hover:bg-[#e4e8dc] hover:text-[#17212b] dark:bg-[#233143] dark:text-[#94a3b8] dark:hover:bg-[#2d3f54] dark:hover:text-white"
          >
            ✕
          </button>
        </div>

        {loading ? (
          <div className="py-12 text-center text-sm font-semibold text-[#075b40] dark:text-[#22c55e]">
            Loading settings...
          </div>
        ) : (
          <div className="mt-6 space-y-6">
            {message && (
              <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-2.5 text-xs font-semibold text-green-800 dark:border-green-800 dark:bg-green-950/40 dark:text-green-300">
                ✓ {message}
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-2.5 text-xs font-semibold text-red-800 dark:border-red-800 dark:bg-red-950/40 dark:text-red-300">
                ⚠️ {error}
              </div>
            )}

            {/* THEME PREFERENCE */}
            <div>
              <label className="mb-2 block text-xs font-bold uppercase tracking-wider text-[#075b40] dark:text-[#22c55e]">
                🎨 Theme Preference
              </label>
              <div className="grid grid-cols-3 gap-3">
                <button
                  type="button"
                  onClick={() => handleThemeChange("LIGHT")}
                  className={`flex flex-col items-center justify-center rounded-2xl border p-4 text-center transition-all ${
                    theme === "LIGHT"
                      ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                      : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                  }`}
                >
                  <span className="text-2xl">☀️</span>
                  <span className="mt-2 text-xs font-bold">Light</span>
                </button>

                <button
                  type="button"
                  onClick={() => handleThemeChange("DARK")}
                  className={`flex flex-col items-center justify-center rounded-2xl border p-4 text-center transition-all ${
                    theme === "DARK"
                      ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                      : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                  }`}
                >
                  <span className="text-2xl">🌙</span>
                  <span className="mt-2 text-xs font-bold">Dark</span>
                </button>

                <button
                  type="button"
                  onClick={() => handleThemeChange("SYSTEM")}
                  className={`flex flex-col items-center justify-center rounded-2xl border p-4 text-center transition-all ${
                    theme === "SYSTEM"
                      ? "border-[#075b40] bg-[#edf5ee] font-bold text-[#075b40] shadow-sm dark:border-[#22c55e] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
                      : "border-[#e9e8df] bg-[#fcfcfb] text-[#68716d] hover:border-[#075b40]/40 dark:border-[#334155] dark:bg-[#1f2b3b] dark:text-[#94a3b8]"
                  }`}
                >
                  <span className="text-2xl">💻</span>
                  <span className="mt-2 text-xs font-bold">System</span>
                </button>
              </div>
            </div>

            {/* NOTIFICATION PREFERENCES */}
            <div>
              <label className="mb-2 block text-xs font-bold uppercase tracking-wider text-[#075b40] dark:text-[#22c55e]">
                🔔 Notification Preferences
              </label>

              <div className="space-y-3">
                {/* Email notifications */}
                <div className="flex items-center justify-between rounded-2xl border border-[#e9e8df] bg-[#fcfcfb] p-4 dark:border-[#334155] dark:bg-[#1f2b3b]">
                  <div className="pr-3">
                    <p className="text-sm font-bold text-[#17212b] dark:text-white">
                      ✉️ Email Notifications
                    </p>
                    <p className="text-xs text-[#68716d] dark:text-[#94a3b8]">
                      Receive important updates, attendance warnings, and results via email.
                    </p>
                  </div>
                  <label className="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      checked={emailNotifs}
                      onChange={(e) => setEmailNotifs(e.target.checked)}
                      className="sr-only peer"
                    />
                    <div className="h-6 w-11 rounded-full bg-gray-200 peer-checked:bg-[#075b40] dark:bg-gray-700 dark:peer-checked:bg-[#22c55e] after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
                  </label>
                </div>

                {/* Push notifications */}
                <div className="flex items-center justify-between rounded-2xl border border-[#e9e8df] bg-[#fcfcfb] p-4 dark:border-[#334155] dark:bg-[#1f2b3b]">
                  <div className="pr-3">
                    <p className="text-sm font-bold text-[#17212b] dark:text-white">
                      🔔 In-App Push Alerts
                    </p>
                    <p className="text-xs text-[#68716d] dark:text-[#94a3b8]">
                      Real-time alerts when new grades are published or attendance is logged.
                    </p>
                  </div>
                  <label className="relative inline-flex cursor-pointer items-center">
                    <input
                      type="checkbox"
                      checked={pushNotifs}
                      onChange={(e) => setPushNotifs(e.target.checked)}
                      className="sr-only peer"
                    />
                    <div className="h-6 w-11 rounded-full bg-gray-200 peer-checked:bg-[#075b40] dark:bg-gray-700 dark:peer-checked:bg-[#22c55e] after:content-[''] after:absolute after:top-[2px] after:left-[2px] after:bg-white after:rounded-full after:h-5 after:w-5 after:transition-all peer-checked:after:translate-x-full"></div>
                  </label>
                </div>
              </div>
            </div>

            {/* ACTION BUTTONS */}
            <div className="flex items-center justify-end gap-3 border-t border-[#f0efe8] pt-5 dark:border-[#233143]">
              <button
                type="button"
                onClick={onClose}
                className="rounded-xl border border-[#dce4db] px-5 py-2.5 text-xs font-bold text-[#68716d] transition hover:bg-[#edf3ed] dark:border-[#334155] dark:text-[#94a3b8] dark:hover:bg-[#233143]"
              >
                Cancel
              </button>
              <button
                type="button"
                onClick={handleSave}
                disabled={saving}
                className="rounded-xl bg-[#075b40] px-6 py-2.5 text-xs font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:opacity-50 dark:bg-[#22c55e] dark:hover:bg-[#16a34a]"
              >
                {saving ? "Saving Preferences..." : "Save Preferences"}
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
