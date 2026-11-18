"use client";

import { useEffect, useState, type FormEvent } from "react";
import Link from "next/link";

type Profile = {
  id: string;
  name: string;
  email: string;
  identity: string;
  role: "FACULTY" | "STUDENT";
  profileSettings: {
    phone: string | null;
    department: string | null;
    bio: string | null;
    profileImageUrl: string | null;
  } | null;
};

export default function ProfilePage() {
  const [profile, setProfile] = useState<Profile | null>(null);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [department, setDepartment] = useState("");
  const [bio, setBio] = useState("");
  const [profileImageUrl, setProfileImageUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    async function loadProfile() {
      try {
        const response = await fetch("/api/profile");
        const data = await response.json();

        if (!response.ok) {
          throw new Error(data.message || "Unable to load profile.");
        }

        const user: Profile = data.user;

        setProfile(user);
        setName(user.name || "");
        setPhone(user.profileSettings?.phone || "");
        setDepartment(user.profileSettings?.department || "");
        setBio(user.profileSettings?.bio || "");
        setProfileImageUrl(
          user.profileSettings?.profileImageUrl || ""
        );
      } catch (err) {
        setError(
          err instanceof Error ? err.message : "Unable to load profile."
        );
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, []);

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault();
    setSaving(true);
    setMessage("");
    setError("");

    try {
      const response = await fetch("/api/profile", {
        method: "PATCH",
        headers: {
          "Content-Type": "application/json",
        },
        body: JSON.stringify({
          name,
          phone: phone || null,
          department: department || null,
          bio: bio || null,
          profileImageUrl: profileImageUrl || null,
        }),
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Unable to update profile.");
      }

      setProfile(data.user);
      setMessage("Profile updated successfully.");
    } catch (err) {
      setError(
        err instanceof Error ? err.message : "Unable to update profile."
      );
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f7f5ec] text-[#075b40]">
        Loading profile...
      </main>
    );
  }

  if (error && !profile) {
    return (
      <main className="flex min-h-screen flex-col items-center justify-center gap-4 bg-[#f7f5ec] px-6 text-center">
        <p className="text-red-700">{error}</p>
        <Link
          href="/student-dashboard"
          className="font-semibold text-[#075b40] underline"
        >
          Return to dashboard
        </Link>
      </main>
    );
  }

  if (!profile) return null;

  const initials = profile.name
    .split(" ")
    .map((part) => part[0])
    .join("")
    .slice(0, 2)
    .toUpperCase();

  return (
    <main className="min-h-screen bg-[#f7f5ec] px-4 py-8 text-[#24352d] sm:px-8">
      <div className="mx-auto max-w-5xl">
        <header className="mb-8 flex flex-wrap items-center justify-between gap-4">
          <div>
            <p className="text-sm font-semibold uppercase tracking-[0.18em] text-[#648273]">
              EduTrack
            </p>
            <h1 className="mt-2 text-3xl font-bold text-[#075b40]">
              My Profile
            </h1>
            <p className="mt-2 text-sm text-[#718078]">
              View and manage your personal information.
            </p>
          </div>

          <Link
            href={
              profile.role === "FACULTY"
                ? "/faculty-dashboard"
                : "/student-dashboard"
            }
            className="rounded-xl border border-[#d9e2d9] bg-white px-5 py-3 text-sm font-semibold text-[#075b40] transition hover:bg-[#edf3ed]"
          >
            Back to Dashboard
          </Link>
        </header>

        <section className="overflow-hidden rounded-3xl border border-[#e3e7dc] bg-white shadow-sm">
          <div className="bg-[#075b40] px-6 py-8 text-white sm:px-9">
            <div className="flex flex-wrap items-center gap-5">
              <div className="flex h-20 w-20 items-center justify-center rounded-full border-4 border-white/30 bg-[#e9efe5] text-2xl font-bold text-[#075b40]">
                {initials || "U"}
              </div>

              <div>
                <h2 className="text-2xl font-bold">{profile.name}</h2>
                <p className="mt-1 text-sm text-white/75">
                  {profile.role === "FACULTY" ? "Faculty Account" : "Student Account"}
                </p>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-8 p-6 sm:p-9">
            <div>
              <h3 className="text-lg font-bold text-[#075b40]">
                Personal Information
              </h3>
              <p className="mt-1 text-sm text-[#829087]">
                Update your profile details below.
              </p>
            </div>

            {message && (
              <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800">
                {message}
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                {error}
              </div>
            )}

            <div className="grid gap-6 sm:grid-cols-2">
              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Full Name
                </label>
                <input
                  value={name}
                  onChange={(event) => setName(event.target.value)}
                  required
                  minLength={2}
                  className="w-full rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                  placeholder="Enter your name"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Email Address
                </label>
                <input
                  value={profile.email}
                  readOnly
                  className="w-full cursor-not-allowed rounded-xl border border-[#e4e8e0] bg-[#f2f3ed] px-4 py-3 text-[#829087]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Identity / Register Number
                </label>
                <input
                  value={profile.identity}
                  readOnly
                  className="w-full cursor-not-allowed rounded-xl border border-[#e4e8e0] bg-[#f2f3ed] px-4 py-3 text-[#829087]"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Phone Number
                </label>
                <input
                  type="tel"
                  value={phone}
                  onChange={(event) => setPhone(event.target.value)}
                  maxLength={20}
                  className="w-full rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                  placeholder="Enter phone number"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Department
                </label>
                <input
                  value={department}
                  onChange={(event) => setDepartment(event.target.value)}
                  maxLength={100}
                  className="w-full rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                  placeholder="Enter your department"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Profile Image URL
                </label>
                <input
                  type="url"
                  value={profileImageUrl}
                  onChange={(event) => setProfileImageUrl(event.target.value)}
                  maxLength={500}
                  className="w-full rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                  placeholder="https://example.com/photo.jpg"
                />
              </div>
            </div>

            <div>
              <label className="mb-2 block text-sm font-semibold">
                About Me
              </label>
              <textarea
                value={bio}
                onChange={(event) => setBio(event.target.value)}
                rows={4}
                className="w-full resize-y rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                placeholder="Write a short introduction about yourself..."
              />
            </div>

            <div className="flex flex-wrap justify-end gap-3 border-t border-[#edf0e9] pt-6">
              <Link
                href={
                  profile.role === "FACULTY"
                    ? "/faculty-dashboard"
                    : "/student-dashboard"
                }
                className="rounded-xl border border-[#dce4db] px-6 py-3 text-sm font-semibold text-[#53645a] transition hover:bg-[#f5f6f0]"
              >
                Cancel
              </Link>

              <button
                type="submit"
                disabled={saving}
                className="rounded-xl bg-[#075b40] px-7 py-3 text-sm font-semibold text-white shadow-sm transition hover:bg-[#06442f] disabled:cursor-not-allowed disabled:opacity-60"
              >
                {saving ? "Saving..." : "Save Changes"}
              </button>
            </div>
          </form>
        </section>
      </div>
    </main>
  );
}