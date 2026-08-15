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

type AuthContextValue = {
  token: string | null;
  isAuthenticated: boolean;
  login: (payload: LoginPayload) => Promise<void>;
  register: (payload: RegisterPayload) => Promise<void>;
  logout: () => void;
};

const AuthContext = createContext<AuthContextValue | null>(null);

export function AuthProvider({ children }: PropsWithChildren) {
  const [token, setToken] = useState(() => getStoredToken());

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
      isAuthenticated: Boolean(token),
      login,
      register,
      logout,
    }),
    [login, logout, register, token]
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
