import { create } from 'zustand';
import { persist, createJSONStorage } from 'zustand/middleware';
import AsyncStorage from '@react-native-async-storage/async-storage';

export type UserRole = 'MUSTERI' | 'MAGAZA_SAHIBI' | 'ADMIN';

export interface User {
    id: number;
    kullaniciAdi: string;
    email: string;
    ad: string;
    soyad: string;
    telefon?: string;
    adres?: string;
    rol: UserRole;
}

interface AuthState {
    user: User | null;
    token: string | null;
    isLoading: boolean;
    isAuthenticated: boolean;
    hasHydrated: boolean; // NEW: Track if store has loaded from AsyncStorage

    // Actions
    setUser: (user: User, token: string) => void;
    updateUser: (user: Partial<User>) => void;
    logout: () => void;
    setLoading: (loading: boolean) => void;
    setHasHydrated: (hydrated: boolean) => void; // NEW

    // Helpers
    isAdmin: () => boolean;
    isMagazaSahibi: () => boolean;
    isMusteri: () => boolean;
}

export const useAuthStore = create<AuthState>()(
    persist(
        (set, get) => ({
            user: null,
            token: null,
            isLoading: false,
            isAuthenticated: false,
            hasHydrated: false, // NEW

            setUser: (user: User, token: string) => {
                console.log('[Auth Store] setUser called:', { user, token });
                set({
                    user,
                    token,
                    isAuthenticated: true,
                    isLoading: false
                });
            },

            updateUser: (userData: Partial<User>) => {
                const currentUser = get().user;
                if (currentUser) {
                    set({ user: { ...currentUser, ...userData } });
                }
            },

            logout: () => {
                console.log('[Auth Store] logout called');
                set({
                    user: null,
                    token: null,
                    isAuthenticated: false,
                    isLoading: false
                });
            },

            setLoading: (loading: boolean) => {
                set({ isLoading: loading });
            },

            setHasHydrated: (hydrated: boolean) => {
                console.log('[Auth Store] setHasHydrated:', hydrated);
                set({ hasHydrated: hydrated });
            },

            isAdmin: () => get().user?.rol === 'ADMIN',
            isMagazaSahibi: () => get().user?.rol === 'MAGAZA_SAHIBI' || get().user?.rol === 'ADMIN',
            isMusteri: () => get().user?.rol === 'MUSTERI',
        }),
        {
            name: 'auth-storage',
            storage: createJSONStorage(() => AsyncStorage),
            partialize: (state) => ({
                user: state.user,
                token: state.token,
                isAuthenticated: state.isAuthenticated
            }),
            onRehydrateStorage: () => {
                return (state?: AuthState, error?: unknown) => {
                    if (error) {
                        console.log('[Auth Store] Rehydration error:', error);
                    } else {
                        console.log('[Auth Store] Rehydration complete:', {
                            user: state?.user,
                            token: state?.token ? `${state.token.substring(0, 20)}...` : null,
                            isAuthenticated: state?.isAuthenticated
                        });
                    }
                    // Always set hydrated, even if there was an error
                    useAuthStore.setState({ hasHydrated: true });
                };
            },
        }
    )
);

export default useAuthStore;
