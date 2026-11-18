"use client";

import { useEffect, useState } from "react";
import Link from "next/link";

type Settings = {
  emailNotifications: boolean;
  pushNotifications: boolean;
};

export default function SettingsPage() {
  const [settings, setSettings] = useState<Settings>({
    emailNotifications: true,
    pushNotifications: true,
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadSettings() {
      try {
        const response = await fetch("/api/settings");
        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.message || "Unable to load settings.");
        }

        setSettings(data.settings);
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Unable to load settings."
        );
      } finally {
        setLoading(false);
      }
    }

    loadSettings();
  }, []);

  function updateSetting(key: keyof Settings, value: boolean) {
    setSettings((previous) => ({
      ...previous,
      [key]: value,
    }));
    setMessage("");
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

      setSettings(data.settings);
      setMessage("Settings saved successfully.");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to save settings."
      );
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

  return (
    <main className="min-h-screen bg-[#f7f5ec] px-4 py-8 text-[#24352d] sm:px-8">
      <div className="mx-auto max-w-4xl">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[#648273]">
              EduTrack
            </p>
            <h1 className="mt-2 text-3xl font-bold text-[#075b40]">
              Settings
            </h1>
            <p className="mt-2 text-sm text-[#718078]">
              Manage your account preferences and notifications.
            </p>
          </div>

          <Link
            href="/student-dashboard"
            className="rounded-xl border border-[#d9e2d9] bg-white px-5 py-3 text-sm font-semibold text-[#075b40] transition hover:bg-[#edf3ed]"
          >
            Back to Dashboard
          </Link>
        </header>

        <section className="rounded-3xl border border-[#e3e7dc] bg-white p-6 shadow-sm sm:p-9">
          <div className="mb-8">
            <h2 className="text-xl font-bold text-[#075b40]">
              Notification Preferences
            </h2>
            <p className="mt-2 text-sm text-[#829087]">
              Choose which notifications you want to receive.
            </p>
          </div>

          {message && (
            <div className="mb-6 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800">
              {message}
            </div>
          )}

          {error && (
            <div className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {error}
            </div>
          )}

          <div className="space-y-5">
            <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-[#e7ebe2] bg-[#fcfcf8] p-5">
              <div className="max-w-md">
                <h3 className="font-semibold text-[#24352d]">
                  Email Notifications
                </h3>
                <p className="mt-1 text-sm text-[#829087]">
                  Receive updates and important information through email.
                </p>
              </div>

              <label className="flex cursor-pointer items-center gap-3">
                <span className="text-sm font-medium text-[#64736a]">
                  {settings.emailNotifications ? "Enabled" : "Disabled"}
                </span>
                <input
                  type="checkbox"
                  checked={settings.emailNotifications}
                  onChange={(event) =>
                    updateSetting("emailNotifications", event.target.checked)
                  }
                  className="h-5 w-5 cursor-pointer accent-[#075b40]"
                />
              </label>
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4 rounded-2xl border border-[#e7ebe2] bg-[#fcfcf8] p-5">
              <div className="max-w-md">
                <h3 className="font-semibold text-[#24352d]">
                  Push Notifications
                </h3>
                <p className="mt-1 text-sm text-[#829087]">
                  Enable in-app notification preferences for your account.
                </p>
              </div>

              <label className="flex cursor-pointer items-center gap-3">
                <span className="text-sm font-medium text-[#64736a]">
                  {settings.pushNotifications ? "Enabled" : "Disabled"}
                </span>
                <input
                  type="checkbox"
                  checked={settings.pushNotifications}
                  onChange={(event) =>
                    updateSetting("pushNotifications", event.target.checked)
                  }
                  className="h-5 w-5 cursor-pointer accent-[#075b40]"
                />
              </label>
            </div>
          </div>

          <div className="mt-8 flex justify-end border-t border-[#edf0e9] pt-6">
            <button
              type="button"
              onClick={saveSettings}
              disabled={saving}
              className="rounded-xl bg-[#075b40] px-7 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#06442f] disabled:cursor-not-allowed disabled:opacity-60"
            >
              {saving ? "Saving..." : "Save Settings"}
            </button>
          </div>
        </section>
      </div>
    </main>
  );
}