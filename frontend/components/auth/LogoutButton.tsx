"use client";

import { useState } from "react";
import { useRouter } from "next/navigation";

type LogoutButtonProps = {
  className?: string;
  variant?: "danger" | "outline" | "default";
  children?: React.ReactNode;
};

export default function LogoutButton({
  className,
  variant = "default",
  children,
}: LogoutButtonProps) {
  const router = useRouter();
  const [loading, setLoading] = useState(false);

  async function handleLogout() {
    try {
      setLoading(true);

      const response = await fetch("/api/auth/logout", {
        method: "POST",
      });

      if (!response.ok) {
        throw new Error("Logout failed");
      }

      router.replace("/");
      router.refresh();
    } catch (error) {
      console.error("Logout error:", error);
      alert("Unable to logout. Please try again.");
    } finally {
      setLoading(false);
    }
  }

  const defaultClasses =
    variant === "danger"
      ? "flex items-center gap-2 rounded-xl border border-red-200 bg-red-50 px-4 py-2 text-sm font-bold text-red-700 transition hover:bg-red-100 disabled:opacity-50"
      : variant === "outline"
      ? "flex items-center gap-2 rounded-xl border border-[#dce4db] bg-white px-4 py-2 text-sm font-semibold text-[#075b40] transition hover:bg-[#edf3ed] disabled:opacity-50"
      : "rounded-xl bg-[#075b40] px-5 py-2.5 text-sm font-bold text-white shadow-sm transition hover:bg-[#06442f] disabled:opacity-50";

  return (
    <button
      type="button"
      onClick={handleLogout}
      disabled={loading}
      className={className || defaultClasses}
    >
      {loading ? "Logging out..." : children || "Logout"}
    </button>
  );
}