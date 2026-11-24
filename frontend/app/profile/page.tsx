"use client";

import { useEffect, useState, useRef, type FormEvent, type ChangeEvent } from "react";
import Link from "next/link";
import Image from "next/image";
import LogoutButton from "@/components/auth/LogoutButton";

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
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  const [profile, setProfile] = useState<Profile | null>(null);
  const [name, setName] = useState("");
  const [phone, setPhone] = useState("");
  const [department, setDepartment] = useState("");
  const [bio, setBio] = useState("");
  const [profileImageUrl, setProfileImageUrl] = useState("");
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [uploadingImage, setUploadingImage] = useState(false);
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
        setProfileImageUrl(user.profileSettings?.profileImageUrl || "");
      } catch (err) {
        setError(err instanceof Error ? err.message : "Unable to load profile.");
      } finally {
        setLoading(false);
      }
    }

    loadProfile();
  }, []);

  async function handleFileChange(event: ChangeEvent<HTMLInputElement>) {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!file.type.startsWith("image/")) {
      setError("Please select a valid image file (JPEG, PNG, WEBP).");
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setError("Image size must be less than 5MB.");
      return;
    }

    setError("");
    setMessage("");
    setUploadingImage(true);

    try {
      const formData = new FormData();
      formData.append("file", file);

      const response = await fetch("/api/upload", {
        method: "POST",
        body: formData,
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Failed to upload image.");
      }

      setProfileImageUrl(data.imageUrl);
      setMessage("Profile picture uploaded successfully!");
    } catch (err) {
      setError(err instanceof Error ? err.message : "Unable to upload picture.");
    } finally {
      setUploadingImage(false);
    }
  }

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
      setError(err instanceof Error ? err.message : "Unable to update profile.");
    } finally {
      setSaving(false);
    }
  }

  if (loading) {
    return (
      <main className="flex min-h-screen items-center justify-center bg-[#f7f5ec] px-4">
        <div className="rounded-2xl border border-[#e3e7dc] bg-white p-8 text-center shadow-sm">
          <div className="mx-auto mb-4 h-10 w-10 animate-spin rounded-full border-4 border-[#075b40] border-t-transparent"></div>
          <p className="font-semibold text-[#075b40]">Loading profile...</p>
        </div>
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
      <div className="mx-auto max-w-4xl">
        <header className="mb-8 rounded-3xl border border-[#e3e7dc] bg-white p-6 shadow-sm sm:p-8 dark:border-[#233143] dark:bg-[#16202c]">
          <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <p className="text-xs font-semibold uppercase tracking-[0.18em] text-[#648273] dark:text-[#22c55e]">
                EduTrack
              </p>
              <h1 className="mt-1 text-2xl font-bold text-[#075b40] sm:text-3xl dark:text-[#22c55e]">
                My Profile
              </h1>
              <p className="mt-1 text-sm text-[#718078] dark:text-[#94a3b8]">
                Manage your personal information and profile picture.
              </p>
            </div>

            <div className="flex flex-wrap items-center gap-3">
              <Link
                href={
                  profile.role === "FACULTY"
                    ? "/faculty-dashboard"
                    : "/student-dashboard"
                }
                className="inline-flex items-center gap-2 rounded-xl bg-[#edf5ee] px-5 py-2.5 text-sm font-bold text-[#075b40] transition hover:bg-[#dceee2] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
              >
                ← Back to Dashboard
              </Link>

              <Link
                href="/settings"
                className="inline-flex items-center gap-2 rounded-xl bg-[#edf5ee] px-5 py-2.5 text-sm font-bold text-[#075b40] transition hover:bg-[#dceee2] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
              >
                ⚙️ Settings
              </Link>

              <LogoutButton
                className="inline-flex items-center gap-2 rounded-xl bg-red-50 px-5 py-2.5 text-sm font-bold text-red-700 transition hover:bg-red-100 dark:bg-red-950/40 dark:text-red-400"
              >
                🚪 Logout
              </LogoutButton>
            </div>
          </div>
        </header>

        <section className="overflow-hidden rounded-3xl border border-[#e3e7dc] bg-white shadow-sm">
          {/* BANNER WITH AVATAR & UPLOAD */}
          <div className="bg-[#075b40] px-6 py-8 text-white sm:px-9">
            <div className="flex flex-wrap items-center gap-6">
              {/* Profile Image / Avatar */}
              <div className="relative group">
                <div className="relative flex h-24 w-24 shrink-0 overflow-hidden rounded-full border-4 border-white/40 bg-[#dceee2] text-3xl font-bold text-[#075b40] shadow-md">
                  {profileImageUrl ? (
                    <Image
                      src={profileImageUrl}
                      alt={profile.name}
                      width={96}
                      height={96}
                      unoptimized
                      className="h-full w-full object-cover"
                    />
                  ) : (
                    <div className="flex h-full w-full items-center justify-center">
                      {initials || "U"}
                    </div>
                  )}
                </div>

                {/* Upload Trigger Overlay */}
                <button
                  type="button"
                  onClick={() => fileInputRef.current?.click()}
                  className="absolute bottom-0 right-0 flex h-8 w-8 items-center justify-center rounded-full bg-white text-[#075b40] shadow-lg transition hover:scale-110"
                  title="Upload picture from computer"
                >
                  📷
                </button>
              </div>

              <div>
                <h2 className="text-2xl font-bold">{profile.name}</h2>
                <p className="mt-1 text-sm text-white/80">
                  {profile.role === "FACULTY" ? "Faculty Member" : "Enrolled Student"} • {profile.identity}
                </p>

                <div className="mt-3 flex items-center gap-3">
                  <input
                    ref={fileInputRef}
                    type="file"
                    accept="image/png, image/jpeg, image/jpg, image/webp"
                    className="hidden"
                    onChange={handleFileChange}
                  />

                  <button
                    type="button"
                    onClick={() => fileInputRef.current?.click()}
                    disabled={uploadingImage}
                    className="rounded-lg bg-white/20 px-3.5 py-1.5 text-xs font-semibold text-white backdrop-blur transition hover:bg-white/30"
                  >
                    {uploadingImage ? "Uploading Photo..." : "📁 Upload from Computer"}
                  </button>

                  {profileImageUrl && (
                    <button
                      type="button"
                      onClick={() => setProfileImageUrl("")}
                      className="text-xs text-white/70 hover:text-white hover:underline"
                    >
                      Remove
                    </button>
                  )}
                </div>
              </div>
            </div>
          </div>

          <form onSubmit={handleSubmit} className="space-y-8 p-6 sm:p-9">
            <div>
              <h3 className="text-lg font-bold text-[#075b40]">
                Personal & Academic Details
              </h3>
              <p className="mt-1 text-sm text-[#829087]">
                Update your contact information and departmental preferences below.
              </p>
            </div>

            {message && (
              <div className="rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800">
                ✓ {message}
              </div>
            )}

            {error && (
              <div className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                ⚠️ {error}
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
                  {profile.role === "FACULTY" ? "Faculty ID / Employee ID" : "Register Number"}
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
                  placeholder="e.g. +91 9876543210"
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
                  placeholder="e.g. Computer Science & Engineering"
                />
              </div>

              <div>
                <label className="mb-2 block text-sm font-semibold">
                  Designation / Role
                </label>
                <input
                  value={bio}
                  onChange={(event) => setBio(event.target.value)}
                  maxLength={100}
                  className="w-full rounded-xl border border-[#dce4db] bg-[#fcfcf8] px-4 py-3 outline-none transition focus:border-[#075b40] focus:ring-2 focus:ring-[#075b40]/10"
                  placeholder={profile.role === "FACULTY" ? "e.g. Associate Professor" : "e.g. Undergraduate Student"}
                />
              </div>
            </div>

            <div className="flex flex-wrap items-center justify-between gap-4 border-t border-[#f0efe8] pt-6 dark:border-[#233143]">
              <LogoutButton
                className="flex items-center gap-2 rounded-full border-2 border-red-200 bg-red-50 px-6 py-2.5 text-sm font-bold text-red-700 transition hover:bg-red-100 disabled:opacity-50 dark:border-red-900/60 dark:bg-red-950/40 dark:text-red-300"
              >
                🚪 Sign Out
              </LogoutButton>

              <button
                type="submit"
                disabled={saving}
                className="rounded-full bg-[#075b40] px-8 py-3.5 font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:opacity-50 dark:bg-[#10b981] dark:text-[#091b14] dark:hover:bg-[#059669]"
              >
                {saving ? "Saving Changes..." : "Save Profile"}
              </button>
            </div>
          </form>
        </section>

        {/* PORTAL SETTINGS & PREFERENCES CARD */}
        <section className="mt-8 rounded-3xl border border-[#e3e7dc] bg-white p-6 shadow-sm sm:p-8 dark:border-[#233143] dark:bg-[#16202c]">
          <div className="flex flex-col items-start justify-between gap-4 sm:flex-row sm:items-center">
            <div>
              <h2 className="text-xl font-bold text-[#075b40] dark:text-[#22c55e]">
                ⚙️ Portal Theme & Notification Settings
              </h2>
              <p className="mt-1 text-sm text-[#829087] dark:text-[#94a3b8]">
                Switch between Light, Dark, or System mode and customize your notification preferences.
              </p>
            </div>

            <Link
              href="/settings"
              className="inline-flex items-center gap-2 rounded-xl bg-[#edf5ee] px-5 py-2.5 text-sm font-bold text-[#075b40] transition hover:bg-[#dceee2] dark:bg-[#14532d]/40 dark:text-[#22c55e]"
            >
              <span>Manage Settings</span>
              <span>→</span>
            </Link>
          </div>
        </section>
      </div>
    </main>
  );
}
