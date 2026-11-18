import { redirect } from "next/navigation";
import { getCurrentUser } from "@/lib/auth";
import DashboardShell from "@/components/dashboard/DashboardShell";

export const instant = false;

export default async function FacultyDashboard() {
  const user = await getCurrentUser();

  if (!user || user.role !== "FACULTY") {
    redirect("/faculty-login");
  }

  return (
    <DashboardShell
      name={user.name}
      email={user.email}
      role="FACULTY"
    />
  );
}