import { redirect } from "next/navigation";
import { getCurrentUser } from "@/lib/auth";
import DashboardShell from "@/components/dashboard/DashboardShell";

export const instant = false;

export default async function StudentDashboard() {
  const user = await getCurrentUser();

  if (!user || user.role !== "STUDENT") {
    redirect("/student-login");
  }

  return (
    <DashboardShell
      name={user.name}
      email={user.email}
      role="STUDENT"
    />
  );
}