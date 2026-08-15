import type { TicketPriority } from "@/features/tickets/types";
import { cn } from "@/lib/utils";

export function QuickMetric({ label, value, tone }: { label: string; value: number; tone?: TicketPriority }) {
  return (
    <div className="flex items-center justify-between gap-3 rounded-md border px-3 py-3">
      <span className="text-sm text-muted-foreground">{label}</span>
      <span
        className={cn(
          "text-lg font-semibold",
          tone === "ALTA" && "text-destructive",
          tone === "MEDIA" && "text-amber-700",
          tone === "BAIXA" && "text-emerald-700"
        )}
      >
        {value}
      </span>
    </div>
  );
}
