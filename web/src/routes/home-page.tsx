import { ShieldCheck } from "lucide-react";
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card";

export function HomePage() {
  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-8 md:grid-cols-[1.2fr_0.8fr]">
      <section>
        <h2 className="text-2xl font-semibold tracking-normal">Area autenticada</h2>
        <p className="mt-2 max-w-2xl text-sm leading-6 text-muted-foreground">
          O token JWT foi salvo localmente e sera enviado automaticamente nas proximas chamadas autenticadas da API.
        </p>
      </section>

      <Card>
        <CardHeader>
          <div className="mb-2 flex h-10 w-10 items-center justify-center rounded-md bg-primary/10 text-primary">
            <ShieldCheck className="h-5 w-5" />
          </div>
          <CardTitle className="text-lg">Sessao ativa</CardTitle>
          <CardDescription>
            Use o botao de sair para remover o token e bloquear rotas privadas.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="rounded-md border bg-muted/40 px-3 py-2 text-sm text-muted-foreground">
            Autenticacao pronta para as proximas telas de chamados.
          </div>
        </CardContent>
      </Card>
    </main>
  );
}
