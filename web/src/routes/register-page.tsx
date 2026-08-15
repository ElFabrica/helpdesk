import { zodResolver } from "@hookform/resolvers/zod";
import { useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Navigate } from "react-router-dom";
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

const registerSchema = z.object({
  name: z.string().trim().min(1, "Informe o nome."),
  email: z.string().trim().email("Informe um e-mail valido."),
  password: z.string().min(6, "A senha deve ter pelo menos 6 caracteres."),
});

type RegisterForm = z.infer<typeof registerSchema>;

export function RegisterPage() {
  const { isAuthenticated, register } = useAuth();
  const [error, setError] = useState("");
  const form = useForm<RegisterForm>({
    resolver: zodResolver(registerSchema),
    defaultValues: {
      name: "",
      email: "",
      password: "",
    },
  });

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  async function handleSubmit(values: RegisterForm) {
    setError("");

    try {
      await register(values);
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Nao foi possivel criar a conta.");
    }
  }

  return (
    <Card className="w-full max-w-md">
      <CardHeader>
        <CardTitle>Criar cadastro</CardTitle>
        <CardDescription>Cadastre-se como solicitante para abrir e acompanhar chamados.</CardDescription>
      </CardHeader>
      <CardContent>
        <form className="space-y-4" onSubmit={form.handleSubmit(handleSubmit)}>
          {error ? (
            <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
              {error}
            </Alert>
          ) : null}

          <div className="space-y-2">
            <Label htmlFor="name">Nome</Label>
            <Input
              id="name"
              autoComplete="name"
              {...form.register("name")}
            />
            <FieldError message={form.formState.errors.name?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="register-email">E-mail</Label>
            <Input
              id="register-email"
              type="email"
              autoComplete="email"
              {...form.register("email")}
            />
            <FieldError message={form.formState.errors.email?.message} />
          </div>

          <div className="space-y-2">
            <Label htmlFor="register-password">Senha</Label>
            <Input
              id="register-password"
              type="password"
              autoComplete="new-password"
              minLength={6}
              {...form.register("password")}
            />
            <FieldError message={form.formState.errors.password?.message} />
          </div>

          <Button className="w-full" type="submit" disabled={form.formState.isSubmitting}>
            {form.formState.isSubmitting ? "Criando..." : "Criar cadastro"}
          </Button>
        </form>

        <p className="mt-6 text-center text-sm text-muted-foreground">
          Ja tem conta?{" "}
          <Link className="font-medium text-primary hover:underline" to="/login">
            Entrar
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
