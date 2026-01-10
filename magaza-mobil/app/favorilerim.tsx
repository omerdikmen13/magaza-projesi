import React from 'react';
import {
    View,
    Text,
    TouchableOpacity,
    FlatList,
    Image,
    StatusBar,
    ActivityIndicator,
    RefreshControl,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Heart, ShoppingBag, Store, Trash2 } from 'lucide-react-native';
import { MotiView } from 'moti';

import { favorilerApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';

export default function FavorilerimScreen() {
    const { isAuthenticated } = useAuthStore();
    const queryClient = useQueryClient();

    const { data, isLoading, refetch } = useQuery({
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

    const favoriler = data?.favoriler || [];

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <Heart size={80} color="#f43f5e" />
                <Text className="text-gray-900 text-2xl font-bold mt-6">Favorilerim</Text>
                <Text className="text-gray-500 text-center mt-2">
                    Favori ürünlerinizi görmek için giriş yapın
                </Text>
                <TouchableOpacity onPress={() => router.push('/(auth)/login')} className="mt-6">
                    <LinearGradient colors={['#f43f5e', '#ec4899']} className="px-8 py-4 rounded-2xl">
                        <Text className="text-white font-bold text-lg">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
            </View>
        );
    }

    const renderFavorite = ({ item, index }: { item: any; index: number }) => (
        <MotiView
            from={{ opacity: 0, translateX: -20 }}
            animate={{ opacity: 1, translateX: 0 }}
            transition={{ delay: index * 50 }}
        >
            <TouchableOpacity
                onPress={() => router.push(`/product/${item.urunId}`)}
                className="bg-white rounded-2xl mb-3 overflow-hidden flex-row"
                style={{ elevation: 2 }}
            >
                <Image
                    source={{ uri: item.resimUrl || 'https://via.placeholder.com/120' }}
                    className="w-28 h-28"
                    resizeMode="cover"
                />
                <View className="flex-1 p-3 justify-between">
                    <View>
                        <Text className="text-gray-900 font-bold text-base" numberOfLines={2}>
                            {item.urunAd}
                        </Text>
                        <View className="flex-row items-center mt-1">
                            <Store size={12} color="#6b7280" />
                            <Text className="text-gray-500 text-sm ml-1">{item.magazaAd}</Text>
                        </View>
                    </View>
                    <View className="flex-row justify-between items-center">
                        <Text className="text-rose-500 font-bold text-lg">₺{item.fiyat}</Text>
                        <TouchableOpacity
                            onPress={() => removeMutation.mutate(item.urunId)}
                            className="w-8 h-8 rounded-full bg-rose-100 items-center justify-center"
                        >
                            <Trash2 size={16} color="#f43f5e" />
                        </TouchableOpacity>
                    </View>
                </View>
            </TouchableOpacity>
        </MotiView>
    );

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header */}
            <View className="bg-white pt-12 pb-4 px-5 border-b border-gray-200">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="#4a5568" />
                    </TouchableOpacity>
                    <View className="flex-row items-center">
                        <Heart size={24} color="#f43f5e" fill="#f43f5e" />
                        <Text className="text-gray-900 text-xl font-bold ml-2">Favorilerim</Text>
                    </View>
                    {favoriler.length > 0 && (
                        <View className="ml-auto bg-rose-100 px-3 py-1 rounded-full">
                            <Text className="text-rose-500 font-bold">{favoriler.length}</Text>
                        </View>
                    )}
                </View>
            </View>

            {/* Content */}
            <View className="flex-1 px-4 pt-4">
                {isLoading ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator size="large" color="#f43f5e" />
                    </View>
                ) : favoriler.length === 0 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <Heart size={60} color="#d1d5db" />
                        <Text className="text-gray-900 font-bold text-xl mt-4 text-center">
                            Henüz favoriniz yok
                        </Text>
                        <Text className="text-gray-500 text-center mt-2">
                            Beğendiğiniz ürünleri favorilere ekleyerek daha sonra kolayca ulaşabilirsiniz
                        </Text>
                        <TouchableOpacity
                            onPress={() => router.push('/(tabs)/magazalar')}
                            className="mt-6"
                        >
                            <LinearGradient colors={['#f43f5e', '#ec4899']} className="px-6 py-3 rounded-xl flex-row items-center">
                                <ShoppingBag size={18} color="white" />
                                <Text className="text-white font-bold ml-2">Alışverişe Başla</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                ) : (
                    <FlatList
                        data={favoriler}
                        renderItem={renderFavorite}
                        keyExtractor={(item) => item.id.toString()}
                        showsVerticalScrollIndicator={false}
                        contentContainerStyle={{ paddingBottom: 100 }}
                        refreshControl={
                            <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#f43f5e" />
                        }
                    />
                )}
            </View>
        </View>
    );
}
