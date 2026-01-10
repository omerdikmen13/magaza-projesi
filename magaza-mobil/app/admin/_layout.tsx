import { Stack, router } from 'expo-router';
import { useEffect } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../stores/authStore';

export default function AdminLayout() {
    const { isAdmin, isAuthenticated, hasHydrated } = useAuthStore();

    useEffect(() => {
        if (hasHydrated) {
            console.log('[Admin Layout] Store hydrated, checking auth');
            console.log('[Admin Layout] isAuthenticated:', isAuthenticated);
            console.log('[Admin Layout] isAdmin:', isAdmin());

            if (!isAuthenticated) {
                console.log('[Admin Layout] Not authenticated, redirecting to login');
                router.replace('/(auth)/login');
            } else if (!isAdmin()) {
                console.log('[Admin Layout] Not admin, redirecting to home');
                router.replace('/(tabs)');
            }
        }
    }, [hasHydrated]);

    if (!hasHydrated) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center">
                <ActivityIndicator size="large" color="#667eea" />
                <Text className="text-white mt-4">Yükleniyor...</Text>
            </View>
        );
    }

    if (!isAuthenticated || !isAdmin()) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center">
                <Text className="text-white">Yönlendiriliyor...</Text>
            </View>
        );
    }

    return (
        <Stack screenOptions={{ headerShown: false }}>
            <Stack.Screen name="index" />
            <Stack.Screen name="users" />
            <Stack.Screen name="stores" />
            <Stack.Screen name="orders" />
            <Stack.Screen name="ciro" />
            <Stack.Screen name="messages" />
        </Stack>
    );
}
