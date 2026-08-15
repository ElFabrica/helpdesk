import { BarChart3, ListChecks, LogOut } from "lucide-react";
import type { ReactNode } from "react";
import { Link, Navigate, Outlet, useLocation, useNavigate } from "react-router-dom";
import { Button } from "@/components/ui/button";
import { useAuth } from "@/features/auth/auth-context";
import { cn } from "@/lib/utils";

export function AppLayout() {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const location = useLocation();
  const navigate = useNavigate();

  if (!isAuthenticated) {
    return <Navigate to="/login" replace />;
  }

  function handleLogout() {
    logout();
    navigate("/login", { replace: true });
  }

  return (
    <div className="min-h-screen bg-background">
      <header className="border-b bg-card">
        <div className="mx-auto flex min-h-16 max-w-6xl flex-col gap-3 px-4 py-3 md:flex-row md:items-center md:justify-between">
          <div>
            <p className="text-sm font-medium text-muted-foreground">Central de chamados</p>
            <h1 className="text-lg font-semibold">Helpdesk</h1>
          </div>
          <div className="flex flex-wrap items-center gap-2">
            <NavLink to="/" isActive={location.pathname === "/"}>
              <ListChecks className="mr-2 h-4 w-4" />
              Chamados
            </NavLink>
            {isAdmin ? (
              <NavLink to="/dashboard" isActive={location.pathname === "/dashboard"}>
                <BarChart3 className="mr-2 h-4 w-4" />
                Dashboard
              </NavLink>
            ) : null}
            <Button variant="outline" size="sm" onClick={handleLogout}>
              <LogOut className="mr-2 h-4 w-4" />
              Sair
            </Button>
          </div>
        </div>
      </header>
      <Outlet />
    </div>
  );
}

function NavLink({
  to,
  isActive,
  children,
}: {
  to: string;
  isActive: boolean;
  children: ReactNode;
}) {
  return (
    <Button asChild variant={isActive ? "default" : "outline"} size="sm">
      <Link to={to} className={cn(isActive && "pointer-events-none")}>
        {children}
      </Link>
    </Button>
  );
}
