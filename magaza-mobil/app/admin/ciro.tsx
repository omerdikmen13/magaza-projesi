import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, TrendingUp, DollarSign, Store, ChevronRight, Package } from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';

const DONEMLER = [
    { value: 'HAFTALIK', label: 'Bu Hafta' },
    { value: 'AYLIK', label: 'Bu Ay' },
    { value: 'YILLIK', label: 'Bu Yıl' },
    { value: 'TUMU', label: 'Tümü' },
];

export default function AdminCiroScreen() {
    const [donem, setDonem] = useState('TUMU');

    const { data: ciro, isLoading, refetch } = useQuery({
        queryKey: ['admin-ciro', donem],
        queryFn: () => adminApi.getCiro(donem),
    });

    // Mağaza detayına git (siparişlerini göster)
    const handleMagazaPress = (magazaId: number, magazaAd: string) => {
        // Sipariş yönetimine git ve mağaza filtresi uygula
        router.push({
            pathname: '/admin/orders',
            params: { magazaId, magazaAd }
        } as any);
    };

    // Mağaza ürünlerini göster
    const handleMagazaUrunler = (magazaId: number) => {
        router.push(`/admin/stores/${magazaId}/products` as any);
    };

    return (
        <View className="flex-1 bg-dark-300">
            <StatusBar barStyle="light-content" />
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            <View className="pt-12 pb-4 px-5">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <Text className="text-white text-xl font-bold">📊 Ciro Raporu</Text>
                </View>
            </View>

            <ScrollView
                contentContainerStyle={{ paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
            >
                {/* Dönem Seçici */}
                <View className="px-5 pt-2">
                    <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                        <View className="flex-row gap-2">
                            {DONEMLER.map((d) => (
                                <TouchableOpacity
                                    key={d.value}
                                    onPress={() => setDonem(d.value)}
                                >
                                    {donem === d.value ? (
                                        <LinearGradient
                                            colors={['#d946ef', '#9333ea']}
                                            className="px-5 py-2 rounded-full"
                                        >
                                            <Text className="text-white font-medium">{d.label}</Text>
                                        </LinearGradient>
                                    ) : (
                                        <View className="px-5 py-2 rounded-full bg-white/10">
                                            <Text className="text-gray-300">{d.label}</Text>
                                        </View>
                                    )}
                                </TouchableOpacity>
                            ))}
                        </View>
                    </ScrollView>
                </View>

                {/* Toplam Ciro */}
                <View className="px-5 pt-6">
                    <MotiView from={{ opacity: 0, scale: 0.95 }} animate={{ opacity: 1, scale: 1 }}>
                        <LinearGradient
                            colors={['#d946ef', '#9333ea']}
                            className="rounded-3xl p-6 mb-6"
                        >
                            <View className="flex-row items-center mb-4">
                                <TrendingUp size={24} color="white" />
                                <Text className="text-white/80 ml-2">Toplam Ciro</Text>
                            </View>
                            <Text className="text-white text-4xl font-extrabold">
                                ₺{ciro?.toplamCiro?.toFixed(2) || '0.00'}
                            </Text>
                            <View className="flex-row mt-3 gap-4">
                                <View>
                                    <Text className="text-white/60 text-xs">Tamamlanan</Text>
                                    <Text className="text-green-300 font-bold">
                                        ₺{ciro?.tamamlananCiro?.toFixed(2) || '0.00'}
                                    </Text>
                                </View>
                                <View>
                                    <Text className="text-white/60 text-xs">Bekleyen</Text>
                                    <Text className="text-yellow-300 font-bold">
                                        ₺{ciro?.bekleyenCiro?.toFixed(2) || '0.00'}
                                    </Text>
                                </View>
                            </View>
                        </LinearGradient>
                    </MotiView>

                    {/* Stats */}
                    <View className="flex-row gap-3 mb-6">
                        <View className="flex-1 bg-white/5 rounded-2xl p-4">
                            <DollarSign size={24} color="#d946ef" />
                            <Text className="text-gray-400 text-sm mt-2">Ortalama</Text>
                            <Text className="text-white font-bold text-xl">
                                ₺{ciro?.magazaRaporlari?.length > 0
                                    ? (ciro?.toplamCiro / ciro?.magazaRaporlari?.length).toFixed(2)
                                    : '0.00'}
                            </Text>
                        </View>
                        <View className="flex-1 bg-white/5 rounded-2xl p-4">
                            <Store size={24} color="#10b981" />
                            <Text className="text-gray-400 text-sm mt-2">Mağaza</Text>
                            <Text className="text-white font-bold text-xl">
                                {ciro?.magazaRaporlari?.length || 0}
                            </Text>
                        </View>
                    </View>

                    {/* Mağaza bazlı */}
                    <Text className="text-white font-bold text-lg mb-4">🏪 Mağaza Bazlı Ciro</Text>
                    <Text className="text-gray-500 text-sm mb-3">Mağazaya tıklayarak siparişleri ve ürünleri görüntüleyebilirsiniz</Text>

                    {ciro?.magazaRaporlari?.map((m: any, index: number) => (
                        <MotiView
                            key={m.magazaId}
                            from={{ opacity: 0, translateX: -20 }}
                            animate={{ opacity: 1, translateX: 0 }}
                            transition={{ delay: index * 100 }}
                            className="bg-white/5 rounded-2xl mb-3 overflow-hidden"
                        >
                            {/* Mağaza Bilgisi */}
                            <TouchableOpacity
                                onPress={() => handleMagazaPress(m.magazaId, m.magazaAd)}
                                className="p-4"
                            >
                                <View className="flex-row items-center justify-between">
                                    <View className="flex-1">
                                        <Text className="text-white font-bold text-lg">{m.magazaAd}</Text>
                                        <Text className="text-gray-400 text-sm">{m.siparisSayisi} sipariş</Text>
                                    </View>
                                    <View className="items-end">
                                        <Text className="text-primary-400 font-bold text-xl">₺{m.ciro?.toFixed(2)}</Text>
                                        <View className="flex-row items-center">
                                            <Text className="text-gray-500 text-xs mr-1">Siparişleri Gör</Text>
                                            <ChevronRight size={14} color="#6b7280" />
                                        </View>
                                    </View>
                                </View>
                            </TouchableOpacity>

                            {/* Alt Aksiyonlar */}
                            <View className="flex-row border-t border-white/10">
                                <TouchableOpacity
                                    onPress={() => handleMagazaPress(m.magazaId, m.magazaAd)}
                                    className="flex-1 py-3 flex-row items-center justify-center"
                                >
                                    <Package size={16} color="#10b981" />
                                    <Text className="text-green-400 ml-2 text-sm">Siparişleri Gör ({m.siparisSayisi})</Text>
                                </TouchableOpacity>
                            </View>
                        </MotiView>
                    ))}

                    {(!ciro?.magazaRaporlari || ciro.magazaRaporlari.length === 0) && (
                        <View className="items-center py-10">
                            <Store size={48} color="#6b7280" />
                            <Text className="text-gray-400 mt-4">Veri bulunamadı</Text>
                        </View>
                    )}
                </View>
            </ScrollView>
        </View>
    );
}
