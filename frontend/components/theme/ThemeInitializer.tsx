"use client";

import { useEffect } from "react";

export type ThemeMode = "LIGHT" | "DARK" | "SYSTEM";

export function applyAppTheme(targetTheme?: ThemeMode) {
  if (typeof window === "undefined") return;

  try {
    const preference =
      targetTheme ||
      (localStorage.getItem("edutrack_theme") as ThemeMode | null) ||
      "SYSTEM";

    const systemIsDark = window.matchMedia(
      "(prefers-color-scheme: dark)"
    ).matches;

    const isDark =
      preference === "DARK" || (preference === "SYSTEM" && systemIsDark);

    const root = document.documentElement;

    if (isDark) {
      root.classList.add("dark");
      root.setAttribute("data-theme", "dark");
      root.style.colorScheme = "dark";
    } else {
      root.classList.remove("dark");
      root.setAttribute("data-theme", "light");
      root.style.colorScheme = "light";
    }

    if (targetTheme) {
      localStorage.setItem("edutrack_theme", targetTheme);
      window.dispatchEvent(
        new CustomEvent("edutrack-theme-change", {
          detail: { theme: targetTheme },
        })
      );
    }
  } catch {
    // Graceful fallback
  }
}

export default function ThemeInitializer() {
  useEffect(() => {
    // Apply saved theme immediately on mount
    applyAppTheme();

    // Listen to custom theme change event
    const handleCustomChange = () => {
      applyAppTheme();
    };
    window.addEventListener("edutrack-theme-change", handleCustomChange);

    // Listen to OS color scheme changes in real-time
    const mediaQuery = window.matchMedia("(prefers-color-scheme: dark)");
    const handleMediaChange = () => {
      const saved = localStorage.getItem("edutrack_theme");
      if (saved === "SYSTEM" || !saved) {
        applyAppTheme("SYSTEM");
      }
    };
    mediaQuery.addEventListener("change", handleMediaChange);

    // Listen to localStorage storage events across browser tabs
    const handleStorage = (e: StorageEvent) => {
      if (e.key === "edutrack_theme") {
        applyAppTheme((e.newValue as ThemeMode) || "SYSTEM");
      }
    };
    window.addEventListener("storage", handleStorage);

    return () => {
      window.removeEventListener("edutrack-theme-change", handleCustomChange);
      mediaQuery.removeEventListener("change", handleMediaChange);
      window.removeEventListener("storage", handleStorage);
    };
  }, []);

  return null;
}
