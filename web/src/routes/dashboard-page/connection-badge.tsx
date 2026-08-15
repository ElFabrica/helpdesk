import { Signal, SignalLow, WifiOff } from "lucide-react";
import { cn } from "@/lib/utils";
import type { ConnectionStatus } from "@/routes/dashboard-page/use-dashboard-data";

export function ConnectionBadge({ status }: { status: ConnectionStatus }) {
  const details = {
    connecting: {
      label: "Conectando",
      icon: SignalLow,
      className: "border-amber-300 bg-amber-50 text-amber-900",
    },
    connected: {
      label: "Tempo real ativo",
      icon: Signal,
      className: "border-emerald-300 bg-emerald-50 text-emerald-800",
    },
    reconnecting: {
      label: "Reconectando",
      icon: SignalLow,
      className: "border-amber-300 bg-amber-50 text-amber-900",
    },
    offline: {
      label: "Offline",
      icon: WifiOff,
      className: "border-destructive/30 bg-destructive/10 text-destructive",
    },
  } satisfies Record<ConnectionStatus, { label: string; icon: typeof Signal; className: string }>;
  const Icon = details[status].icon;

  return (
    <span className={cn("inline-flex h-10 items-center rounded-md border px-3 text-sm font-medium", details[status].className)}>
      <Icon className="mr-2 h-4 w-4" />
      {details[status].label}
    </span>
  );
}
