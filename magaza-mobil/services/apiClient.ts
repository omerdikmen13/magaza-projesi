import axios, { AxiosError, InternalAxiosRequestConfig } from 'axios';
import Constants from 'expo-constants';
import { useAuthStore } from '../stores/authStore';

// Backend URL - Environment variable veya Expo config'den al
// Development: ngrok URL, Production: gerçek sunucu URL'si
const BASE_URL = Constants.expoConfig?.extra?.apiUrl
    || process.env.EXPO_PUBLIC_API_URL
    || 'http://localhost:8080';

export const apiClient = axios.create({
    baseURL: BASE_URL,
    headers: {
        'Content-Type': 'application/json',
        'ngrok-skip-browser-warning': 'true',
    },
    timeout: 15000, // 15 saniye timeout (AI yanıtları için artırıldı)
});

// Request interceptor - Token ekleme
apiClient.interceptors.request.use(
    (config: InternalAxiosRequestConfig) => {
        const token = useAuthStore.getState().token;
        console.log('[API Client] Request to:', config.url);
        console.log('[API Client] Token:', token ? `${token.substring(0, 20)}...` : 'NO TOKEN');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
            console.log('[API Client] Authorization header set');
        } else {
            console.log('[API Client] WARNING: No token found in auth store!');
        }
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

// Response interceptor - Hata yönetimi
apiClient.interceptors.response.use(
    (response) => response,
    (error: AxiosError) => {
        if (error.response?.status === 401) {
            console.log('[API Client] 401 Unauthorized:', error.config?.url);
            console.log('[API Client] NOT auto-logging out - user must manually logout');
            // REMOVED: Auto-logout on 401
            // Reason: Causes logout loop during store rehydration
            // User should manually logout or handle 401 in UI
        }
        return Promise.reject(error);
    }
);

// API Configuration helper
export const setBaseURL = (url: string) => {
    apiClient.defaults.baseURL = url;
};

export default apiClient;
