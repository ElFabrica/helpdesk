import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Navigate, useLocation, useNavigate } from "react-router-dom";
import { z } from "zod";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { useAuth } from "@/features/auth/auth-context";

type LocationState = {
  from?: {
    pathname?: string;
  };
};

const loginSchema = z.object({
  email: z.string().trim().email("Informe um e-mail valido."),
  password: z.string().min(1, "Informe a senha."),
});

type LoginForm = z.infer<typeof loginSchema>;

export function LoginPage() {
  const { isAuthenticated, login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [error, setError] = useState("");
  const form = useForm<LoginForm>({
    resolver: zodResolver(loginSchema),
    defaultValues: {
      email: "",
      password: "",
    },
  });

  const state = location.state as LocationState | null;
  const redirectTo = state?.from?.pathname ?? "/";

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(values: LoginForm) {
    setError("");

    try {
      await login(values);
      navigate(redirectTo, { replace: true });
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Nao foi possivel entrar.");
    }
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Entrar</CardTitle>
        <CardDescription>Acesse a central de chamados com seu e-mail e senha.</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={form.handleSubmit(handleSubmit)}>
          {error ? (
            <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
              {error}
            </Alert>
          ) : null}

          <div className="space-y-2">
            <Label htmlFor="email">E-mail</Label>
            <Input
              id="email"
              type="email"
              autoComplete="email"
              {...form.register("email")}
            />
            <FieldError message={form.formState.errors.email?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="password">Senha</Label>
            <Input
              id="password"
              type="password"
              autoComplete="current-password"
              {...form.register("password")}
            />
            <FieldError message={form.formState.errors.password?.message} />
          </div>

          <Button className="w-full" type="submit" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Entrando..." : "Entrar"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          Ainda nao tem conta?{" "}
          <Link className="font-medium text-primary hover:underline" to="/cadastro">
            Criar cadastro
          </Link>
        </p>
      </CardContent>
    </Card>
  );
}

function FieldError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <p className="text-sm text-destructive">{message}</p>;
}
