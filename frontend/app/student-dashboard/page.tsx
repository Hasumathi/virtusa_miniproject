import { redirect } from "next/navigation";
import { getCurrentUser } from "@/lib/auth";
import DashboardShell from "@/components/dashboard/DashboardShell";

export const instant = false;

export default async function StudentDashboard() {
  const user = await getCurrentUser();

  if (!user || user.role !== "STUDENT") {
    redirect("/student-login");
  }

  // eslint-disable-next-line @typescript-eslint/no-explicit-any
  let dashboardData: any = null;
  if (user.backendToken) {
    try {
      const backendUrl =
        process.env.BACKEND_API_URL ||
        process.env.NEXT_PUBLIC_BACKEND_URL ||
        "http://localhost:8080";

      const res = await fetch(`${backendUrl}/api/dashboard/student`, {
        headers: {
          Authorization: `Bearer ${user.backendToken}`,
        },
        cache: "no-store",
      });

      if (res.ok) {
        const json = await res.json();
        dashboardData = json.data;
      }
    } catch (e) {
      console.warn("Could not fetch student dashboard data:", e);
    }
  }

  const displayName = dashboardData?.studentName || user.name;

  return (
    <DashboardShell
      name={displayName}
      email={user.email}
      role="STUDENT"
      data={dashboardData}
      backendToken={user.backendToken}
    />
  );
}
