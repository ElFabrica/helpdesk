import { api, getApiErrorMessage } from "@/lib/api";
import type {
  CreateTicketCommentRequestDTO,
  CreateTicketRequestDTO,
  TicketCommentResponseDTO,
  TicketFilters,
  TicketResponseDTO,
  TicketSummaryResponseDTO,
  UpdateTicketRequestDTO,
} from "@/features/tickets/types";

export async function listTickets(filters: TicketFilters) {
  try {
    const response = await api.get<TicketSummaryResponseDTO[]>("/tickets", {
      params: filters,
    });
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel carregar os chamados."));
  }
}

export async function createTicket(payload: CreateTicketRequestDTO) {
  try {
    const response = await api.post<TicketResponseDTO>("/tickets", payload);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel criar o chamado."));
  }
}

export async function getTicket(ticketId: number) {
  try {
    const response = await api.get<TicketResponseDTO>(`/tickets/${ticketId}`);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel carregar o chamado."));
  }
}

export async function listTicketComments(ticketId: number) {
  try {
    const response = await api.get<TicketCommentResponseDTO[]>(`/tickets/${ticketId}/comments`);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel carregar o historico."));
  }
}

export async function addTicketComment(ticketId: number, payload: CreateTicketCommentRequestDTO) {
  try {
    const response = await api.post<TicketCommentResponseDTO>(`/tickets/${ticketId}/comments`, payload);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel adicionar o comentario."));
  }
}

export async function updateTicket(ticketId: number, payload: UpdateTicketRequestDTO) {
  try {
    const response = await api.patch<TicketResponseDTO>(`/tickets/${ticketId}`, payload);
    return response.data;
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel atualizar o chamado."));
  }
}

export async function cancelTicket(ticketId: number) {
  try {
    await api.delete(`/tickets/${ticketId}`);
  } catch (error) {
    throw new Error(getApiErrorMessage(error, "Nao foi possivel cancelar o chamado."));
  }
}
