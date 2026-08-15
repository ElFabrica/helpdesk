import { zodResolver } from "@hookform/resolvers/zod";
import { Filter, Plus, RotateCcw, Search } from "lucide-react";
import { useEffect, useMemo, useState } from "react";
import { type SubmitHandler, useForm } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
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
import { Select } from "@/components/ui/select";
import { Textarea } from "@/components/ui/textarea";
import { formatDateTime } from "@/features/tickets/format";
import {
  categoryLabels,
  categoryOptions,
  priorityLabels,
  priorityOptions,
  statusLabels,
  statusOptions,
} from "@/features/tickets/ticket-options";
import { createTicket, listTickets } from "@/features/tickets/tickets-api";
import type {
  TicketCategory,
  TicketFilters,
  TicketPriority,
  TicketStatus,
  TicketSummaryResponseDTO,
} from "@/features/tickets/types";
import { cn } from "@/lib/utils";

const createTicketSchema = z.object({
  title: z.string().trim().min(1, "Informe o titulo."),
  description: z.string().trim().min(1, "Informe a descricao."),
});

type CreateTicketForm = z.infer<typeof createTicketSchema>;

type FilterForm = {
  status: "" | TicketStatus;
  priority: "" | TicketPriority;
  category: "" | TicketCategory;
};

const emptyFilters: FilterForm = {
  status: "",
  priority: "",
  category: "",
};

export function HomePage() {
  const navigate = useNavigate();
  const [filters, setFilters] = useState<TicketFilters>({});
  const [tickets, setTickets] = useState<TicketSummaryResponseDTO[]>([]);
  const [listError, setListError] = useState("");
  const [createError, setCreateError] = useState("");
  const [isLoading, setIsLoading] = useState(true);

  const filterForm = useForm<FilterForm>({
    defaultValues: emptyFilters,
  });
  const createForm = useForm<CreateTicketForm>({
    resolver: zodResolver(createTicketSchema),
    defaultValues: {
      title: "",
      description: "",
    },
  });

  useEffect(() => {
    let isCurrent = true;

    async function loadTickets() {
      setIsLoading(true);
      setListError("");

      try {
        const result = await listTickets(filters);
        if (isCurrent) {
          setTickets(result);
        }
      } catch (error) {
        if (isCurrent) {
          setListError(error instanceof Error ? error.message : "Nao foi possivel carregar os chamados.");
        }
      } finally {
        if (isCurrent) {
          setIsLoading(false);
        }
      }
    }

    void loadTickets();

    return () => {
      isCurrent = false;
    };
  }, [filters]);

  const activeFilterCount = useMemo(
    () => Object.values(filters).filter(Boolean).length,
    [filters]
  );

  const handleFilterSubmit: SubmitHandler<FilterForm> = (values) => {
    setFilters({
      status: values.status || undefined,
      priority: values.priority || undefined,
      category: values.category || undefined,
    });
  };

  function handleClearFilters() {
    filterForm.reset(emptyFilters);
    setFilters({});
  }

  async function handleCreateSubmit(values: CreateTicketForm) {
    setCreateError("");

    try {
      const createdTicket = await createTicket(values);
      createForm.reset();
      setFilters({});
      filterForm.reset(emptyFilters);
      navigate(`/tickets/${createdTicket.id}`);
    } catch (error) {
      setCreateError(error instanceof Error ? error.message : "Nao foi possivel criar o chamado.");
    }
  }

  return (
    <main className="mx-auto grid max-w-6xl gap-6 px-4 py-8 lg:grid-cols-[minmax(0,1fr)_360px]">
      <section className="min-w-0 space-y-5">
        <div className="flex flex-col gap-3 sm:flex-row sm:items-end sm:justify-between">
          <div>
            <h2 className="text-2xl font-semibold tracking-normal">Chamados</h2>
            <p className="mt-1 text-sm text-muted-foreground">
              Acompanhe chamados abertos, em andamento, resolvidos e fechados.
            </p>
          </div>
          <div className="rounded-md border bg-card px-3 py-2 text-sm text-muted-foreground">
            {tickets.length} {tickets.length === 1 ? "chamado" : "chamados"}
            {activeFilterCount ? ` com ${activeFilterCount} filtros` : ""}
          </div>
        </div>

        <form
          className="grid gap-3 rounded-md border bg-card p-4 sm:grid-cols-2 lg:grid-cols-[1fr_1fr_1fr_auto_auto]"
          onSubmit={filterForm.handleSubmit(handleFilterSubmit)}
        >
          <div className="space-y-2">
            <Label htmlFor="status">Status</Label>
            <Select id="status" {...filterForm.register("status")}>
              <option value="">Todos</option>
              {statusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="priority">Prioridade</Label>
            <Select id="priority" {...filterForm.register("priority")}>
              <option value="">Todas</option>
              {priorityOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
          </div>

          <div className="space-y-2">
            <Label htmlFor="category">Categoria</Label>
            <Select id="category" {...filterForm.register("category")}>
              <option value="">Todas</option>
              {categoryOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </Select>
          </div>

          <Button className="self-end" type="submit">
            <Filter className="mr-2 h-4 w-4" />
            Filtrar
          </Button>
          <Button className="self-end" type="button" variant="outline" onClick={handleClearFilters}>
            <RotateCcw className="mr-2 h-4 w-4" />
            Limpar
          </Button>
        </form>

        {listError ? (
          <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
            {listError}
          </Alert>
        ) : null}

        <div className="overflow-hidden rounded-md border bg-card">
          {isLoading ? (
            <div className="px-4 py-8 text-center text-sm text-muted-foreground">
              Carregando chamados...
            </div>
          ) : tickets.length ? (
            <div className="divide-y">
              {tickets.map((ticket) => (
                <Link
                  key={ticket.id}
                  to={`/tickets/${ticket.id}`}
                  className="grid gap-3 px-4 py-4 transition-colors hover:bg-muted/50 md:grid-cols-[minmax(0,1fr)_auto]"
                >
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <span className="text-xs font-medium text-muted-foreground">#{ticket.id}</span>
                      <StatusBadge status={ticket.status} />
                      <PriorityBadge priority={ticket.priority} />
                    </div>
                    <h3 className="mt-2 truncate text-base font-semibold tracking-normal">
                      {ticket.title}
                    </h3>
                    <p className="mt-1 text-sm text-muted-foreground">
                      {categoryLabels[ticket.category]} · Solicitante: {ticket.requesterName}
                    </p>
                  </div>
                  <div className="text-sm text-muted-foreground md:text-right">
                    <p>{formatDateTime(ticket.createdAt)}</p>
                    <p className="mt-1">{ticket.responsibleId ? `Resp. #${ticket.responsibleId}` : "Sem responsavel"}</p>
                  </div>
                </Link>
              ))}
            </div>
          ) : (
            <div className="px-4 py-10 text-center">
              <Search className="mx-auto h-8 w-8 text-muted-foreground" />
              <p className="mt-3 text-sm font-medium">Nenhum chamado encontrado</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Ajuste os filtros ou abra um novo chamado.
              </p>
            </div>
          )}
        </div>
      </section>

      <Card className="h-fit">
        <CardHeader>
          <CardTitle className="text-lg">Abrir chamado</CardTitle>
          <CardDescription>
            Descreva o problema para que a API classifique categoria e prioridade automaticamente.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <form className="space-y-4" onSubmit={createForm.handleSubmit(handleCreateSubmit)}>
            {createError ? (
              <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
                {createError}
              </Alert>
            ) : null}

            <div className="space-y-2">
              <Label htmlFor="ticket-title">Titulo</Label>
              <Input id="ticket-title" {...createForm.register("title")} />
              <FieldError message={createForm.formState.errors.title?.message} />
            </div>

            <div className="space-y-2">
              <Label htmlFor="ticket-description">Descricao</Label>
              <Textarea id="ticket-description" {...createForm.register("description")} />
              <FieldError message={createForm.formState.errors.description?.message} />
            </div>

            <Button className="w-full" type="submit" disabled={createForm.formState.isSubmitting}>
              <Plus className="mr-2 h-4 w-4" />
              {createForm.formState.isSubmitting ? "Abrindo..." : "Abrir chamado"}
            </Button>
          </form>
        </CardContent>
      </Card>
    </main>
  );
}

function FieldError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <p className="text-sm text-destructive">{message}</p>;
}

export function StatusBadge({ status }: { status: TicketStatus }) {
  return (
    <span className="rounded-md border bg-muted/50 px-2 py-1 text-xs font-medium">
      {statusLabels[status]}
    </span>
  );
}

export function PriorityBadge({ priority }: { priority: TicketPriority }) {
  return (
    <span
      className={cn(
        "rounded-md border px-2 py-1 text-xs font-medium",
        priority === "ALTA" && "border-destructive/30 bg-destructive/10 text-destructive",
        priority === "MEDIA" && "border-amber-300 bg-amber-50 text-amber-800",
        priority === "BAIXA" && "border-emerald-300 bg-emerald-50 text-emerald-800"
      )}
    >
      {priorityLabels[priority]}
    </span>
  );
}
