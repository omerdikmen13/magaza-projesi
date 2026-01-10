import React from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Image,
    RefreshControl,
    ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { Heart, ShoppingCart, Trash2 } from 'lucide-react-native';
import { MotiView } from 'moti';

import { favorilerApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function FavorilerScreen() {
    const { isAuthenticated } = useAuthStore();
    const queryClient = useQueryClient();

    const { data: favoriler = [], isLoading, refetch } = useQuery({
        queryKey: ['favoriler'],
        queryFn: favorilerApi.getAll,
        enabled: isAuthenticated,
    });

    const removeMutation = useMutation({
        mutationFn: (urunId: number) => favorilerApi.remove(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['favoriler'] });
            queryClient.invalidateQueries({ queryKey: ['favori-ids'] });
        },
    });

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-5">
                <Heart size={64} color="#cbd5e1" />
                <Text className="text-gray-900 text-xl font-bold mt-4">Favorileriniz</Text>
                <Text className="text-gray-600 text-center mt-2">
                    Favori ürünlerinizi görmek için giriş yapın
                </Text>
                <TouchableOpacity
                    onPress={() => router.push('/(auth)/login')}
                    className="mt-6"
                >
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="px-8 py-3 rounded-xl"
                    >
                        <Text className="text-white font-bold">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
            </View>
        );
    }

    if (isLoading) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <ActivityIndicator size="large" color="#667eea" />
            </View>
        );
    }

    return (
        <View className="flex-1 bg-gray-100">
            {/* Header */}
            <View className="bg-white pt-12 pb-4 px-5">
                <Text className="text-gray-900 text-2xl font-bold">Favorilerim</Text>
                <Text className="text-gray-600 mt-1">{favoriler.length} ürün</Text>
            </View>

            <ScrollView
                contentContainerStyle={{ paddingBottom: 100, paddingTop: 16 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
            >
                {favoriler.length === 0 ? (
                    <View className="items-center justify-center px-5 mt-20">
                        <Heart size={64} color="#cbd5e1" />
                        <Text className="text-gray-900 text-xl font-bold mt-4">Favori ürününüz yok</Text>
                        <Text className="text-gray-600 text-center mt-2">
                            Beğendiğiniz ürünleri favorilere ekleyin
                        </Text>
                    </View>
                ) : (
                    <View className="px-5">
                        {favoriler.map((item: any, index: number) => (
                            <MotiView
                                key={item.id}
                                from={{ opacity: 0, translateY: 20 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: index * 100 }}
                                className="mb-4"
                            >
                                <TouchableOpacity
                                    onPress={() => router.push(`/product/${item.urunId}`)}
                                    className="bg-white rounded-2xl overflow-hidden shadow-sm"
                                    style={{ elevation: 2 }}
                                >
                                    <View className="flex-row">
                                        {/* Image */}
                                        <Image
                                            source={{ uri: item.urunResimUrl || 'https://via.placeholder.com/150' }}
                                            className="w-28 h-28"
                                            resizeMode="cover"
                                        />

                                        {/* Content */}
                                        <View className="flex-1 p-4">
                                            <Text className="text-gray-900 font-bold text-base" numberOfLines={2}>
                                                {item.urunAd}
                                            </Text>
                                            <Text className="text-gray-600 text-sm mt-1">
                                                {item.magazaAd}
                                            </Text>
                                            <Text className="text-primary-500 font-bold text-lg mt-2">
                                                ₺{item.urunFiyat}
                                            </Text>
                                        </View>

                                        {/* Actions */}
                                        <View className="p-4 justify-between">
                                            <TouchableOpacity
                                                onPress={() => removeMutation.mutate(item.urunId)}
                                                disabled={removeMutation.isPending}
                                                className="w-10 h-10 rounded-full bg-red-50 items-center justify-center"
                                            >
                                                <Trash2 size={18} color="#ef4444" />
                                            </TouchableOpacity>

                                            <TouchableOpacity
                                                onPress={() => router.push(`/product/${item.urunId}`)}
                                                className="w-10 h-10 rounded-full bg-primary-50 items-center justify-center"
                                            >
                                                <ShoppingCart size={18} color="#667eea" />
                                            </TouchableOpacity>
                                        </View>
                                    </View>
                                </TouchableOpacity>
                            </MotiView>
                        ))}
                    </View>
                )}
            </ScrollView>
        </View>
    );
}
