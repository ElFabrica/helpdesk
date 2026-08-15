import {
  createContext,
  type PropsWithChildren,
  useCallback,
  useContext,
  useMemo,
  useState,
} from "react";
import {
  api,
  clearStoredToken,
  getApiErrorMessage,
  getStoredToken,
  storeToken,
} from "@/lib/api";

type LoginPayload = {
  email: string;
  password: string;
};

type RegisterPayload = {
  name: string;
  email: string;
  password: string;
};

type TokenResponseDTO = {
  token: string;
  type: string;
};

type AuthenticatedUser = {
  id: number;
  email: string;
  role: "ADMIN" | "SOLICITANTE";
};

type AuthContextValue = {
  token: string | null;
  user: AuthenticatedUser | null;
  isAuthenticated: boolean;
  isAdmin: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [token, setToken] = useState(() => getStoredToken());
  const user = useMemo(() => decodeToken(token), [token]);

  const login = useCallback(async (payload: LoginPayload) => {
    try {
      const response = await api.post<TokenResponseDTO>("/auth/login", payload);
      storeToken(response.data.token);
      setToken(response.data.token);
    } catch (error) {
      throw new Error(getApiErrorMessage(error, "Nao foi possivel entrar."));
    }
  }, []);

  const register = useCallback(async (payload: RegisterPayload) => {
    try {
      await api.post("/auth/register", payload);
      await login({ email: payload.email, password: payload.password });
    } catch (error) {
      throw new Error(getApiErrorMessage(error, "Nao foi possivel criar a conta."));
    }
  }, [login]);

  const logout = useCallback(() => {
    clearStoredToken();
    setToken(null);
  }, []);

  const value = useMemo(
    () => ({
      token,
      user,
      isAuthenticated: Boolean(token),
      isAdmin: user?.role === "ADMIN",
      login,
      register,
      logout,
    }),
    [login, logout, register, token, user]
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);
  if (!context) {
    throw new Error("useAuth deve ser usado dentro de AuthProvider");
  }

  return context;
}

function decodeToken(token: string | null): AuthenticatedUser | null {
  if (!token) {
    return null;
  }

  try {
    const [, payload] = token.split(".");
    const normalizedPayload = payload
      .replace(/-/g, "+")
      .replace(/_/g, "/")
      .padEnd(Math.ceil(payload.length / 4) * 4, "=");
    const decodedPayload = JSON.parse(window.atob(normalizedPayload)) as {
      sub?: unknown;
      userId?: unknown;
      role?: unknown;
    };

    if (
      typeof decodedPayload.userId !== "number" ||
      typeof decodedPayload.sub !== "string" ||
      (decodedPayload.role !== "ADMIN" && decodedPayload.role !== "SOLICITANTE")
    ) {
      return null;
    }

    return {
      id: decodedPayload.userId,
      email: decodedPayload.sub,
      role: decodedPayload.role,
    };
  } catch {
    return null;
  }
}
