import { Stack, router } from 'expo-router';
import { useEffect } from 'react';
import { View, Text, ActivityIndicator } from 'react-native';
import { useAuthStore } from '../../stores/authStore';

export default function SahipLayout() {
    const { isMagazaSahibi, isAuthenticated, hasHydrated } = useAuthStore();

    useEffect(() => {
        if (hasHydrated) {
            console.log('[Sahip Layout] Store hydrated, checking auth');
            console.log('[Sahip Layout] isAuthenticated:', isAuthenticated);
            console.log('[Sahip Layout] isMagazaSahibi:', isMagazaSahibi());

            if (!isAuthenticated) {
                console.log('[Sahip Layout] Not authenticated, redirecting to login');
                router.replace('/(auth)/login');
            } else if (!isMagazaSahibi()) {
                console.log('[Sahip Layout] Not store owner, redirecting to home');
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

    if (!isAuthenticated || !isMagazaSahibi()) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center">
                <Text className="text-white">Yönlendiriliyor...</Text>
            </View>
        );
    }

    return (
        <Stack screenOptions={{ headerShown: false }}>
            <Stack.Screen name="index" />
            <Stack.Screen name="siparisler" />
            <Stack.Screen name="urunler" />
            <Stack.Screen name="urun-ekle" />
            <Stack.Screen name="ciro" />
        </Stack>
    );
}
