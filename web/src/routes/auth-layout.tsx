import { Outlet } from "react-router-dom";

export function AuthLayout() {
  return (
    <main className="flex min-h-screen items-center justify-center bg-[radial-gradient(circle_at_top_left,hsl(188_70%_90%),transparent_32%),linear-gradient(135deg,hsl(210_20%_98%),hsl(210_18%_92%))] px-4 py-10">
      <Outlet />
    </main>
  );
}
