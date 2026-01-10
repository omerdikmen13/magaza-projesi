import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
    ArrowLeft,
    Package,
    Clock,
    Check,
    Truck,
    X,
    ChevronRight,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { siparislerApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';

const DURUMLAR = [
    { value: 'BEKLEMEDE', label: 'Beklemede', color: '#d69e2e', icon: Clock },
    { value: 'HAZIRLANIYOR', label: 'Hazırlanıyor', color: '#3182ce', icon: Package },
    { value: 'KARGODA', label: 'Kargoda', color: '#805ad5', icon: Truck },
    { value: 'TESLIM_EDILDI', label: 'Teslim Edildi', color: '#38a169', icon: Check },
    { value: 'IPTAL', label: 'İptal Edildi', color: '#e53e3e', icon: X },
];

export default function SiparislerimScreen() {
    const { isAuthenticated } = useAuthStore();
    const [expandedOrder, setExpandedOrder] = useState<number | null>(null);

    const { data: siparisler = [], isLoading, refetch } = useQuery({
        queryKey: ['siparislerim'],
        queryFn: siparislerApi.getAll,
        enabled: isAuthenticated,
    });

    const getDurumInfo = (durum: string) => {
        return DURUMLAR.find(d => d.value === durum) || DURUMLAR[0];
    };

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <Package size={80} color="#a0aec0" />
                <Text className="text-gray-900 text-2xl font-bold mt-6">Siparişlerim</Text>
                <Text className="text-gray-500 text-center mt-2">
                    Siparişlerinizi görmek için giriş yapın
                </Text>
                <TouchableOpacity
                    onPress={() => router.push('/(auth)/login')}
                    className="mt-6"
                >
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="px-8 py-4 rounded-2xl"
                    >
                        <Text className="text-white font-bold text-lg">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
            </View>
        );
    }

    const renderOrder = ({ item, index }: any) => {
        const durumInfo = getDurumInfo(item.durum);
        const DurumIcon = durumInfo.icon || Package;
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
                    className="bg-white rounded-2xl shadow-sm overflow-hidden"
                    style={{ elevation: 2 }}
                >
                    <View className="p-4">
                        <View className="flex-row items-center justify-between">
                            <View className="flex-1">
                                <View className="flex-row items-center">
                                    <Text className="text-gray-900 font-bold">Sipariş #{item.id}</Text>
                                    <View
                                        className="ml-2 flex-row items-center px-2 py-1 rounded-lg"
                                        style={{ backgroundColor: `${durumInfo.color}20` }}
                                    >
                                        <DurumIcon size={12} color={durumInfo.color} />
                                        <Text className="ml-1 text-xs font-medium" style={{ color: durumInfo.color }}>
                                            {durumInfo.label}
                                        </Text>
                                    </View>
                                </View>
                                <Text className="text-gray-500 text-sm mt-1">{item.magazaAd}</Text>
                                <Text className="text-gray-400 text-xs">
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
                                <Text className="text-primary-500 font-bold text-lg">₺{item.toplamTutar}</Text>
                                <ChevronRight
                                    size={18}
                                    color="#a0aec0"
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
                            className="px-4 pb-4 border-t border-gray-100 pt-3"
                        >
                            {/* Products */}
                            <View className="bg-gray-50 rounded-xl p-3 mb-3">
                                {item.detaylar?.map((d: any, i: number) => (
                                    <View key={i} className="flex-row justify-between py-2 border-b border-gray-100 last:border-b-0">
                                        <View className="flex-1">
                                            <Text className="text-gray-900">{d.urunAd}</Text>
                                            <Text className="text-gray-500 text-sm">Beden: {d.bedenAd} | Adet: {d.adet}</Text>
                                        </View>
                                        <Text className="text-primary-500 font-medium">₺{d.toplamFiyat}</Text>
                                    </View>
                                ))}
                            </View>

                            {/* Delivery Address */}
                            <View className="bg-gray-50 rounded-xl p-3">
                                <Text className="text-gray-400 text-xs mb-1">Teslimat Adresi:</Text>
                                <Text className="text-gray-900">{item.teslimatAdresi || 'Belirtilmemiş'}</Text>
                            </View>
                        </MotiView>
                    )}
                </TouchableOpacity>
            </MotiView>
        );
    };

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header */}
            <View className="bg-white pt-12 pb-4 px-5">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="#4a5568" />
                    </TouchableOpacity>
                    <Text className="text-gray-900 text-2xl font-bold">📦 Siparişlerim</Text>
                </View>
            </View>

            {/* List */}
            <FlatList
                data={siparisler}
                renderItem={renderOrder}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingTop: 16, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
                ListEmptyComponent={
                    <View className="items-center py-20">
                        <Package size={60} color="#a0aec0" />
                        <Text className="text-gray-900 font-bold text-xl mt-4">Henüz siparişiniz yok</Text>
                        <TouchableOpacity
                            onPress={() => router.push('/(tabs)/magazalar')}
                            className="mt-4"
                        >
                            <LinearGradient
                                colors={['#667eea', '#764ba2']}
                                className="px-6 py-3 rounded-xl"
                            >
                                <Text className="text-white font-semibold">Alışverişe Başla</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                }
            />
        </View>
    );
}
