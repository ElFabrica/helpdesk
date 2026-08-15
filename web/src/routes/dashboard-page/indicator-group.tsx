import type { ReactNode } from "react";
import {
  Card,
  CardContent,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import type { TicketPriority } from "@/features/tickets/types";
import { cn } from "@/lib/utils";

type IndicatorItem = {
  key: string;
  label: string;
  value: number;
  tone?: TicketPriority;
};

export function IndicatorGroup({
  title,
  icon,
  items,
}: {
  title: string;
  icon: ReactNode;
  items: IndicatorItem[];
}) {
  return (
    <Card className="rounded-md">
      <CardHeader>
        <CardTitle className="flex items-center gap-2 text-lg">
          {icon}
          {title}
        </CardTitle>
      </CardHeader>
      <CardContent className="space-y-3">
        {items.map((item) => (
          <div key={item.key} className="flex items-center justify-between gap-4 rounded-md border px-3 py-3">
            <span className="text-sm font-medium text-muted-foreground">{item.label}</span>
            <span
              className={cn(
                "min-w-10 rounded-md border bg-muted/50 px-2 py-1 text-center text-sm font-semibold",
                item.tone === "ALTA" && "border-destructive/30 bg-destructive/10 text-destructive",
                item.tone === "MEDIA" && "border-amber-300 bg-amber-50 text-amber-800",
                item.tone === "BAIXA" && "border-emerald-300 bg-emerald-50 text-emerald-800"
              )}
            >
              {item.value}
            </span>
          </div>
        ))}
      </CardContent>
    </Card>
  );
}
