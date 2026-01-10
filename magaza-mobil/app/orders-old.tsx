import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
    ArrowLeft,
    ShoppingCart,
    Clock,
    Check,
    Truck,
    X,
    Package,
    ChevronRight,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { siparislerApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';

const DURUMLAR = [
    { value: 'BEKLEMEDE', label: 'Beklemede', color: '#f59e0b', icon: Clock },
    { value: 'HAZIRLANIYOR', label: 'Hazırlanıyor', color: '#3b82f6', icon: Package },
    { value: 'KARGODA', label: 'Kargoda', color: '#8b5cf6', icon: Truck },
    { value: 'TESLIM_EDILDI', label: 'Teslim Edildi', color: '#10b981', icon: Check },
    { value: 'IPTAL', label: 'İptal', color: '#ef4444', icon: X },
];

export default function MyOrdersScreen() {
    const { isAuthenticated } = useAuthStore();
    const [expandedOrder, setExpandedOrder] = useState<number | null>(null);

    const { data: siparisler = [], isLoading, refetch } = useQuery({
        queryKey: ['my-orders'],
        queryFn: siparislerApi.getAll,
        enabled: isAuthenticated,
    });

    const getDurumInfo = (durum: string) => {
        return DURUMLAR.find(d => d.value === durum) || DURUMLAR[0];
    };

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center px-6">
                <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />
                <ShoppingCart size={60} color="#6b7280" />
                <Text className="text-white text-xl font-bold mt-4">Siparişlerim</Text>
                <Text className="text-gray-400 text-center mt-2">
                    Siparişlerinizi görmek için giriş yapın
                </Text>
                <TouchableOpacity
                    onPress={() => router.push('/(auth)/login')}
                    className="mt-6 bg-primary-600 px-8 py-3 rounded-xl"
                >
                    <Text className="text-white font-semibold">Giriş Yap</Text>
                </TouchableOpacity>
            </View>
        );
    }

    const renderOrder = ({ item, index }: any) => {
        const durumInfo = getDurumInfo(item.durum);
        const DurumIcon = durumInfo.icon || ShoppingCart;
        const isExpanded = expandedOrder === item.id;

        return (
            <MotiView
                from={{ opacity: 0, translateY: 20 }}
                animate={{ opacity: 1, translateY: 0 }}
                transition={{ delay: index * 50 }}
                className="mb-3"
            >
                <TouchableOpacity
                    onPress={() => setExpandedOrder(isExpanded ? null : item.id)}
                    className="bg-white/5 rounded-xl overflow-hidden"
                >
                    <View className="p-4">
                        <View className="flex-row items-center justify-between">
                            <View className="flex-1">
                                <View className="flex-row items-center">
                                    <Text className="text-white font-bold">Sipariş #{item.id}</Text>
                                    <View
                                        className="ml-2 flex-row items-center px-2 py-1 rounded-lg"
                                        style={{ backgroundColor: `${durumInfo.color}20` }}
                                    >
                                        <DurumIcon size={12} color={durumInfo.color} />
                                        <Text className="ml-1 text-xs" style={{ color: durumInfo.color }}>
                                            {durumInfo.label}
                                        </Text>
                                    </View>
                                </View>
                                <Text className="text-gray-400 text-sm mt-1">{item.magazaAd}</Text>
                                <Text className="text-gray-500 text-xs">
                                    {new Date(item.siparisTarihi).toLocaleDateString('tr-TR', {
                                        day: 'numeric',
                                        month: 'long',
                                        year: 'numeric',
                                        hour: '2-digit',
                                        minute: '2-digit'
                                    })}
                                </Text>
                            </View>
                            <View className="items-end">
                                <Text className="text-primary-400 font-bold text-lg">₺{item.toplamTutar}</Text>
                                <ChevronRight
                                    size={18}
                                    color="#6b7280"
                                    style={{ transform: [{ rotate: isExpanded ? '90deg' : '0deg' }] }}
                                />
                            </View>
                        </View>
                    </View>

                    {/* Expanded Details */}
                    {isExpanded && (
                        <MotiView
                            from={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="px-4 pb-4 border-t border-white/10 pt-3"
                        >
                            {/* Products */}
                            <View className="bg-white/5 rounded-lg p-3 mb-3">
                                {item.detaylar?.map((d: any, i: number) => (
                                    <View key={i} className="flex-row justify-between py-2 border-b border-white/5 last:border-b-0">
                                        <View className="flex-1">
                                            <Text className="text-white">{d.urunAd}</Text>
                                            <Text className="text-gray-400 text-sm">Beden: {d.bedenAd} | Adet: {d.adet}</Text>
                                        </View>
                                        <Text className="text-primary-400">₺{d.toplamFiyat}</Text>
                                    </View>
                                ))}
                            </View>

                            {/* Delivery Address */}
                            <View className="bg-white/5 rounded-lg p-3">
                                <Text className="text-gray-400 text-xs mb-1">Teslimat Adresi:</Text>
                                <Text className="text-white">{item.teslimatAdresi || 'Belirtilmemiş'}</Text>
                            </View>
                        </MotiView>
                    )}
                </TouchableOpacity>
            </MotiView>
        );
    };

    return (
        <View className="flex-1 bg-dark-300">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <Text className="text-white text-2xl font-bold">Siparişlerim</Text>
                </View>
            </View>

            {/* List */}
            <FlatList
                data={siparisler}
                renderItem={renderOrder}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
                ListEmptyComponent={
                    <View className="items-center py-20">
                        <ShoppingCart size={60} color="#6b7280" />
                        <Text className="text-gray-400 mt-4">Henüz siparişiniz yok</Text>
                        <TouchableOpacity
                            onPress={() => router.push('/(tabs)')}
                            className="mt-4 bg-primary-600 px-6 py-3 rounded-xl"
                        >
                            <Text className="text-white font-semibold">Alışverişe Başla</Text>
                        </TouchableOpacity>
                    </View>
                }
            />
        </View>
    );
}
