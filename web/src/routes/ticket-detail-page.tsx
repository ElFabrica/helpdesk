import { zodResolver } from "@hookform/resolvers/zod";
import { ArrowLeft, Ban, MessageSquarePlus, Save } from "lucide-react";
import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { Link, Navigate, useNavigate, useParams } from "react-router-dom";
import { toast } from "sonner";
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
import { useAuth } from "@/features/auth/auth-context";
import { formatDateTime } from "@/features/tickets/format";
import {
  categoryLabels,
  categoryOptions,
  priorityLabels,
  priorityOptions,
  statusOptions,
} from "@/features/tickets/ticket-options";
import {
  addTicketComment,
  cancelTicket,
  getTicket,
  listTicketComments,
  updateTicket,
} from "@/features/tickets/tickets-api";
import {
  ticketCategories,
  ticketPriorities,
  ticketStatuses,
  type TicketCommentResponseDTO,
  type TicketResponseDTO,
} from "@/features/tickets/types";
import { PriorityBadge, StatusBadge } from "@/routes/home-page";

const editTicketSchema = z.object({
  title: z.string().trim().min(1, "Informe o titulo."),
  description: z.string().trim().min(1, "Informe a descricao."),
  status: z.enum(ticketStatuses),
});

const adminTicketSchema = z.object({
  category: z.enum(ticketCategories),
  priority: z.enum(ticketPriorities),
});

const commentSchema = z.object({
  text: z.string().trim().min(1, "Informe o comentario."),
});

const invalidStatusTransitionMessage = "Transicao de status invalida";

type EditTicketForm = z.infer<typeof editTicketSchema>;
type AdminTicketForm = z.infer<typeof adminTicketSchema>;
type CommentForm = z.infer<typeof commentSchema>;

export function TicketDetailPage() {
  const { ticketId } = useParams();
  const navigate = useNavigate();
  const { isAdmin } = useAuth();
  const numericTicketId = Number(ticketId);
  const [ticket, setTicket] = useState<TicketResponseDTO | null>(null);
  const [comments, setComments] = useState<TicketCommentResponseDTO[]>([]);
  const [error, setError] = useState("");
  const [success, setSuccess] = useState("");
  const [isLoading, setIsLoading] = useState(true);
  const [isCancelling, setIsCancelling] = useState(false);

  const editForm = useForm<EditTicketForm>({
    resolver: zodResolver(editTicketSchema),
  });
  const adminForm = useForm<AdminTicketForm>({
    resolver: zodResolver(adminTicketSchema),
  });
  const commentForm = useForm<CommentForm>({
    resolver: zodResolver(commentSchema),
    defaultValues: {
      text: "",
    },
  });

  useEffect(() => {
    if (!Number.isInteger(numericTicketId)) {
      return;
    }

    let isCurrent = true;

    async function loadDetail() {
      setIsLoading(true);
      setError("");

      try {
        const [ticketResult, commentResult] = await Promise.all([
          getTicket(numericTicketId),
          listTicketComments(numericTicketId),
        ]);

        if (isCurrent) {
          setTicket(ticketResult);
          setComments(commentResult);
          editForm.reset({
            title: ticketResult.title,
            description: ticketResult.description,
            status: ticketResult.status,
          });
          adminForm.reset({
            category: ticketResult.category,
            priority: ticketResult.priority,
          });
        }
      } catch (caughtError) {
        if (isCurrent) {
          setError(caughtError instanceof Error ? caughtError.message : "Nao foi possivel carregar o chamado.");
        }
      } finally {
        if (isCurrent) {
          setIsLoading(false);
        }
      }
    }

    void loadDetail();

    return () => {
      isCurrent = false;
    };
  }, [adminForm, editForm, numericTicketId]);

  if (!Number.isInteger(numericTicketId)) {
    return <Navigate to="/" replace />;
  }

  async function refreshHistory(updatedTicket: TicketResponseDTO) {
    setTicket(updatedTicket);
    editForm.reset({
      title: updatedTicket.title,
      description: updatedTicket.description,
      status: updatedTicket.status,
    });
    adminForm.reset({
      category: updatedTicket.category,
      priority: updatedTicket.priority,
    });
    setComments(await listTicketComments(updatedTicket.id));
  }

  async function handleEditSubmit(values: EditTicketForm) {
    setError("");
    setSuccess("");

    try {
      const updatedTicket = await updateTicket(numericTicketId, values);
      await refreshHistory(updatedTicket);
      setSuccess("Chamado atualizado.");
    } catch (caughtError) {
      const message = getErrorMessage(caughtError, "Nao foi possivel atualizar o chamado.");
      notifyInvalidStatusTransition(message);
      setError(message);
    }
  }

  async function handleAdminSubmit(values: AdminTicketForm) {
    setError("");
    setSuccess("");

    try {
      const updatedTicket = await updateTicket(numericTicketId, values);
      await refreshHistory(updatedTicket);
      setSuccess("Classificacao atualizada.");
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Nao foi possivel atualizar a classificacao.");
    }
  }

  async function handleCommentSubmit(values: CommentForm) {
    setError("");
    setSuccess("");

    try {
      const createdComment = await addTicketComment(numericTicketId, values);
      setComments((currentComments) => [...currentComments, createdComment]);
      commentForm.reset();
      setSuccess("Comentario adicionado.");
    } catch (caughtError) {
      setError(caughtError instanceof Error ? caughtError.message : "Nao foi possivel adicionar o comentario.");
    }
  }

  async function handleCancel() {
    setError("");
    setSuccess("");
    setIsCancelling(true);

    try {
      await cancelTicket(numericTicketId);
      navigate("/");
    } catch (caughtError) {
      const message = getErrorMessage(caughtError, "Nao foi possivel cancelar o chamado.");
      notifyInvalidStatusTransition(message);
      setError(message);
    } finally {
      setIsCancelling(false);
    }
  }

  return (
    <main className="mx-auto max-w-6xl px-4 py-8">
      <div className="mb-5">
        <Button asChild variant="ghost" size="sm">
          <Link to="/">
            <ArrowLeft className="mr-2 h-4 w-4" />
            Voltar
          </Link>
        </Button>
      </div>

      {isLoading ? (
        <div className="rounded-md border bg-card px-4 py-8 text-center text-sm text-muted-foreground">
          Carregando chamado...
        </div>
      ) : ticket ? (
        <div className="grid gap-6 lg:grid-cols-[minmax(0,1fr)_360px]">
          <section className="min-w-0 space-y-5">
            <div className="rounded-md border bg-card p-5">
              <div className="flex flex-col gap-4 md:flex-row md:items-start md:justify-between">
                <div className="min-w-0">
                  <div className="flex flex-wrap items-center gap-2">
                    <span className="text-xs font-medium text-muted-foreground">#{ticket.id}</span>
                    <StatusBadge status={ticket.status} />
                    <PriorityBadge priority={ticket.priority} />
                  </div>
                  <h2 className="mt-3 break-words text-2xl font-semibold tracking-normal">
                    {ticket.title}
                  </h2>
                  <p className="mt-2 text-sm text-muted-foreground">
                    {categoryLabels[ticket.category]} · Classificacao {ticket.classificationOrigin}
                  </p>
                </div>
                <Button
                  type="button"
                  variant="outline"
                  onClick={handleCancel}
                  disabled={isCancelling || ticket.status === "FECHADO"}
                >
                  <Ban className="mr-2 h-4 w-4" />
                  {isCancelling ? "Cancelando..." : "Cancelar"}
                </Button>
              </div>

              <dl className="mt-5 grid gap-3 text-sm sm:grid-cols-2 lg:grid-cols-4">
                <InfoItem label="Solicitante" value={ticket.requesterName} />
                <InfoItem label="Responsavel" value={ticket.responsibleId ? `#${ticket.responsibleId}` : "Sem responsavel"} />
                <InfoItem label="Criado em" value={formatDateTime(ticket.createdAt)} />
                <InfoItem label="Atualizado em" value={formatDateTime(ticket.updatedAt)} />
              </dl>
            </div>

            {error ? (
              <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
                {error}
              </Alert>
            ) : null}
            {success ? (
              <Alert className="border-emerald-300 bg-emerald-50 text-emerald-800">
                {success}
              </Alert>
            ) : null}

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Dados do chamado</CardTitle>
                <CardDescription>Atualize descricao, titulo ou fluxo de status.</CardDescription>
              </CardHeader>
              <CardContent>
                <form className="space-y-4" onSubmit={editForm.handleSubmit(handleEditSubmit)}>
                  <div className="space-y-2">
                    <Label htmlFor="detail-title">Titulo</Label>
                    <Input id="detail-title" {...editForm.register("title")} />
                    <FieldError message={editForm.formState.errors.title?.message} />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="detail-description">Descricao</Label>
                    <Textarea id="detail-description" {...editForm.register("description")} />
                    <FieldError message={editForm.formState.errors.description?.message} />
                  </div>

                  <div className="space-y-2">
                    <Label htmlFor="detail-status">Status</Label>
                    <Select id="detail-status" {...editForm.register("status")}>
                      {statusOptions.map((option) => (
                        <option key={option.value} value={option.value}>
                          {option.label}
                        </option>
                      ))}
                    </Select>
                    <FieldError message={editForm.formState.errors.status?.message} />
                  </div>

                  <Button type="submit" disabled={editForm.formState.isSubmitting}>
                    <Save className="mr-2 h-4 w-4" />
                    {editForm.formState.isSubmitting ? "Salvando..." : "Salvar"}
                  </Button>
                </form>
              </CardContent>
            </Card>

            <Card>
              <CardHeader>
                <CardTitle className="text-lg">Historico e comentarios</CardTitle>
                <CardDescription>Interacoes aparecem em ordem cronologica.</CardDescription>
              </CardHeader>
              <CardContent className="space-y-5">
                <div className="space-y-3">
                  {comments.length ? (
                    comments.map((comment) => (
                      <article key={comment.id} className="rounded-md border bg-background p-3">
                        <div className="flex flex-wrap items-center justify-between gap-2 text-sm">
                          <span className="font-medium">{comment.authorName}</span>
                          <span className="text-muted-foreground">{formatDateTime(comment.createdAt)}</span>
                        </div>
                        <p className="mt-2 whitespace-pre-wrap text-sm leading-6">{comment.text}</p>
                      </article>
                    ))
                  ) : (
                    <p className="rounded-md border bg-muted/40 px-3 py-4 text-sm text-muted-foreground">
                      Nenhuma interacao registrada.
                    </p>
                  )}
                </div>

                <form className="space-y-3" onSubmit={commentForm.handleSubmit(handleCommentSubmit)}>
                  <div className="space-y-2">
                    <Label htmlFor="comment-text">Comentario</Label>
                    <Textarea id="comment-text" {...commentForm.register("text")} />
                    <FieldError message={commentForm.formState.errors.text?.message} />
                  </div>
                  <Button type="submit" disabled={commentForm.formState.isSubmitting}>
                    <MessageSquarePlus className="mr-2 h-4 w-4" />
                    {commentForm.formState.isSubmitting ? "Enviando..." : "Adicionar comentario"}
                  </Button>
                </form>
              </CardContent>
            </Card>
          </section>

          <aside className="space-y-5">
            {isAdmin ? (
              <Card>
                <CardHeader>
                  <CardTitle className="text-lg">Administracao</CardTitle>
                  <CardDescription>Corrija categoria e prioridade classificadas automaticamente.</CardDescription>
                </CardHeader>
                <CardContent>
                  <form className="space-y-4" onSubmit={adminForm.handleSubmit(handleAdminSubmit)}>
                    <div className="space-y-2">
                      <Label htmlFor="admin-category">Categoria</Label>
                      <Select id="admin-category" {...adminForm.register("category")}>
                        {categoryOptions.map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </Select>
                    </div>

                    <div className="space-y-2">
                      <Label htmlFor="admin-priority">Prioridade</Label>
                      <Select id="admin-priority" {...adminForm.register("priority")}>
                        {priorityOptions.map((option) => (
                          <option key={option.value} value={option.value}>
                            {option.label}
                          </option>
                        ))}
                      </Select>
                    </div>

                    <Button className="w-full" type="submit" disabled={adminForm.formState.isSubmitting}>
                      <Save className="mr-2 h-4 w-4" />
                      {adminForm.formState.isSubmitting ? "Salvando..." : "Salvar classificacao"}
                    </Button>
                  </form>
                </CardContent>
              </Card>
            ) : null}

            <div className="rounded-md border bg-card p-4 text-sm text-muted-foreground">
              <p className="font-medium text-foreground">Resumo</p>
              <p className="mt-2">Status: {ticket.status}</p>
              <p>Prioridade: {priorityLabels[ticket.priority]}</p>
              <p>Categoria: {categoryLabels[ticket.category]}</p>
            </div>
          </aside>
        </div>
      ) : (
        <Alert className="border-destructive/40 bg-destructive/10 text-destructive">
          {error || "Chamado nao encontrado."}
        </Alert>
      )}
    </main>
  );
}

function InfoItem({ label, value }: { label: string; value: string }) {
  return (
    <div className="rounded-md border bg-muted/30 px-3 py-2">
      <dt className="text-xs font-medium text-muted-foreground">{label}</dt>
      <dd className="mt-1 truncate font-medium">{value}</dd>
    </div>
  );
}

function FieldError({ message }: { message?: string }) {
  if (!message) {
    return null;
  }

  return <p className="text-sm text-destructive">{message}</p>;
}

function getErrorMessage(error: unknown, fallback: string) {
  return error instanceof Error ? error.message : fallback;
}

function notifyInvalidStatusTransition(message: string) {
  if (message !== invalidStatusTransitionMessage) {
    return;
  }

  toast.error("Transicao de status invalida", {
    description: "Escolha uma proxima etapa permitida para o chamado atual.",
  });
}
