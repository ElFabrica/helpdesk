import { Activity, Gauge, RefreshCw, ShieldAlert } from "lucide-react";
import { Alert } from "@/components/ui/alert";
import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { useAuth } from "@/features/auth/auth-context";
import {
  priorityLabels,
  priorityOptions,
  statusLabels,
  statusOptions,
} from "@/features/tickets/ticket-options";
import { ConnectionBadge } from "@/routes/dashboard-page/connection-badge";
import { HighPriorityAlertsCard } from "@/routes/dashboard-page/high-priority-alerts-card";
import { IndicatorGroup } from "@/routes/dashboard-page/indicator-group";
import { QuickMetric } from "@/routes/dashboard-page/quick-metric";
import {
  getPriorityCount,
  getStatusCount,
  useDashboardData,
} from "@/routes/dashboard-page/use-dashboard-data";

export function DashboardPage() {
  const { isAdmin } = useAuth();
  const {
    alerts,
    connectionStatus,
    error,
    indicators,
    isLoading,
    lastUpdatedLabel,
    loadIndicators,
  } = useDashboardData(isAdmin);

  if (!isAdmin) {
    return (
      <main className="mx-auto max-w-6xl px-4 py-8">
        <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
          Dashboard disponivel apenas para administradores.
        </Alert>
      </main>
    );
  }

  return (
    <main className="mx-auto max-w-6xl space-y-6 px-4 py-8">
      <section className="flex flex-col gap-4 md:flex-row md:items-end md:justify-between">
        <div>
          <h2 className="text-2xl font-semibold tracking-normal">Dashboard</h2>
          <p className="mt-1 text-sm text-muted-foreground">
            Indicadores globais de chamados atualizados por eventos em tempo real.
          </p>
        </div>
        <div className="flex flex-col gap-2 sm:flex-row sm:items-center">
          <ConnectionBadge status={connectionStatus} />
          <Button type="button" variant="outline" onClick={() => void loadIndicators()}>
            <RefreshCw className="mr-2 h-4 w-4" />
            Atualizar
          </Button>
        </div>
      </section>

      {error ? (
        <Alert className="border-amber-300 bg-amber-50 text-amber-900">
          {error} Fallback ativo: os indicadores sao recarregados por HTTP durante a reconexao.
        </Alert>
      ) : null}

      <section className="grid gap-4 md:grid-cols-[1fr_2fr]">
        <Card className="rounded-md">
          <CardHeader>
            <CardTitle className="flex items-center gap-2 text-lg">
              <Gauge className="h-5 w-5 text-primary" />
              Total de chamados
            </CardTitle>
            <CardDescription>Ultima atualizacao: {lastUpdatedLabel}</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="text-5xl font-semibold tracking-normal">
              {isLoading ? "..." : indicators.total}
            </div>
            <p className="mt-2 text-sm text-muted-foreground">
              Soma de todos os chamados cadastrados.
            </p>
          </CardContent>
        </Card>

        <div className="grid gap-4 sm:grid-cols-2">
          <IndicatorGroup
            title="Por status"
            icon={<Activity className="h-5 w-5 text-primary" />}
            items={statusOptions.map((option) => ({
              key: option.value,
              label: option.label,
              value: getStatusCount(indicators, option.value),
            }))}
          />
          <IndicatorGroup
            title="Por prioridade"
            icon={<ShieldAlert className="h-5 w-5 text-primary" />}
            items={priorityOptions.map((option) => ({
              key: option.value,
              label: option.label,
              value: getPriorityCount(indicators, option.value),
              tone: option.value,
            }))}
          />
        </div>
      </section>

      <section className="grid gap-4 lg:grid-cols-[2fr_1fr]">
        <HighPriorityAlertsCard alerts={alerts} />

        <Card className="rounded-md">
          <CardHeader>
            <CardTitle className="text-lg">Leitura rapida</CardTitle>
            <CardDescription>Distribuicao atual para triagem.</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <QuickMetric label={statusLabels.ABERTO} value={getStatusCount(indicators, "ABERTO")} />
            <QuickMetric label={statusLabels.EM_ANDAMENTO} value={getStatusCount(indicators, "EM_ANDAMENTO")} />
            <QuickMetric label={priorityLabels.ALTA} value={getPriorityCount(indicators, "ALTA")} tone="ALTA" />
          </CardContent>
        </Card>
      </section>
    </main>
  );
}
