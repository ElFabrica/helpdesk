import { AlertTriangle } from "lucide-react";
import { Link } from "react-router-dom";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { HighPriorityAlertDTO } from "@/features/dashboard/types";
import { priorityLabels } from "@/features/tickets/ticket-options";

export function HighPriorityAlertsCard({ alerts }: { alerts: HighPriorityAlertDTO[] }) {
  return (
    <Card className="rounded-md">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-lg">
          <AlertTriangle className="h-5 w-5 text-destructive" />
          Alertas de prioridade alta
        </CardTitle>
        <CardDescription>Chamados ALTA recebidos nesta sessao em tempo real.</CardDescription>
      </CardHeader>
      <CardContent>
        {alerts.length ? (
          <div className="divide-y rounded-md border">
            {alerts.map((alert) => (
              <Link
                key={`${alert.ticketId}-${alert.title}`}
                to={`/tickets/${alert.ticketId}`}
                className="flex items-center justify-between gap-4 px-4 py-3 transition-colors hover:bg-muted/50"
              >
                <div className="min-w-0">
                  <p className="truncate text-sm font-semibold">#{alert.ticketId} {alert.title}</p>
                  <p className="mt-1 text-xs text-muted-foreground">
                    Prioridade {priorityLabels[alert.priority]}
                  </p>
                </div>
                <span className="rounded-md border border-destructive/30 bg-destructive/10 px-2 py-1 text-xs font-medium text-destructive">
                  {priorityLabels[alert.priority]}
                </span>
              </Link>
            ))}
          </div>
        ) : (
          <div className="rounded-md border border-dashed px-4 py-8 text-center text-sm text-muted-foreground">
            Nenhum alerta de prioridade alta recebido nesta sessao.
          </div>
        )}
      </CardContent>
    </Card>
  );
}
