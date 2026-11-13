"use client";

// Small success / error message that disappears by itself (Member 2).

import { useEffect } from "react";
import type { ToastMessage } from "./DeleteConfirmDialog";

type Props = {
  toast: ToastMessage;
  onDismiss: () => void;
};

export default function Toast({ toast, onDismiss }: Props) {
  useEffect(() => {
    const timer = setTimeout(onDismiss, 4500);
    return () => clearTimeout(timer);
  }, [toast, onDismiss]);

  return (
    <div className={`toast toast-${toast.type}`} role="status" aria-live="polite">
      <span>{toast.text}</span>
      <button type="button" aria-label="Dismiss message" onClick={onDismiss}>
        ✕
      </button>
    </div>
  );
}
