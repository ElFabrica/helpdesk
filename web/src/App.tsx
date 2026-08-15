import { Navigate, Route, Routes } from "react-router-dom";
import { AppLayout } from "@/routes/app-layout";
import { AuthLayout } from "@/routes/auth-layout";
import { HomePage } from "@/routes/home-page";
import { LoginPage } from "@/routes/login-page";
import { ProtectedRoute } from "@/routes/protected-route";
import { RegisterPage } from "@/routes/register-page";

export function App() {
  return (
    <Routes>
      <Route element={<AuthLayout />}>
        <Route path="/login" element={<LoginPage />} />
        <Route path="/cadastro" element={<RegisterPage />} />
      </Route>

      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/" element={<HomePage />} />
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}
