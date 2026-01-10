import React, { useState, useMemo } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    ShoppingCart,
    Clock,
    Check,
    Truck,
    X,
    Package,
    ChevronDown,
    Pencil,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

const DURUMLAR = [
    { value: '', label: 'Tümü', color: '#6b7280' },
    { value: 'BEKLEMEDE', label: 'Beklemede', color: '#f59e0b', icon: Clock },
    { value: 'ONAYLANDI', label: 'Onaylandı', color: '#22c55e', icon: Check },
    { value: 'HAZIRLANIYOR', label: 'Hazırlanıyor', color: '#3b82f6', icon: Package },
    { value: 'KARGODA', label: 'Kargoda', color: '#8b5cf6', icon: Truck },
    { value: 'TESLIM_EDILDI', label: 'Teslim Edildi', color: '#10b981', icon: Check },
    { value: 'IPTAL', label: 'İptal', color: '#ef4444', icon: X },
];

export default function AdminOrdersScreen() {
    const params = useLocalSearchParams<{ magazaId?: string; magazaAd?: string }>();
    const { isAdmin } = useAuthStore();
    const queryClient = useQueryClient();
    const [selectedDurum, setSelectedDurum] = useState('');
    const [expandedOrder, setExpandedOrder] = useState<number | null>(null);

    const { data: allSiparisler = [], isLoading, refetch } = useQuery({
        queryKey: ['admin-orders', selectedDurum],
        queryFn: () => adminApi.getSiparisler(selectedDurum || undefined),
        enabled: isAdmin(),
    });

    // Filter by magazaId if coming from ciro screen
    const siparisler = useMemo(() => {
        if (params.magazaId) {
            return allSiparisler.filter((s: any) => s.magazaId === Number(params.magazaId));
        }
        return allSiparisler;
    }, [allSiparisler, params.magazaId]);

    const updateStatusMutation = useMutation({
        mutationFn: ({ id, durum }: { id: number; durum: string }) => adminApi.updateSiparisDurum(id, durum),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
            Alert.alert('Başarılı', 'Sipariş durumu güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        },
    });

    const getDurumInfo = (durum: string) => {
        return DURUMLAR.find(d => d.value === durum) || DURUMLAR[0];
    };

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
                            <View>
                                <View className="flex-row items-center">
                                    <Text className="text-white font-bold text-lg">#{item.id}</Text>
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
                                <Text className="text-gray-400 text-sm">{item.kullaniciAd}</Text>
                                <Text className="text-gray-500 text-xs">{item.magazaAd}</Text>
                            </View>
                            <View className="items-end">
                                <Text className="text-primary-400 font-bold text-lg">₺{item.toplamTutar}</Text>
                                <Text className="text-gray-500 text-xs">
                                    {new Date(item.siparisTarihi).toLocaleDateString('tr-TR')}
                                </Text>
                            </View>
                        </View>

                        {/* Expand Indicator */}
                        <View className="items-center mt-2">
                            <ChevronDown
                                size={16}
                                color="#6b7280"
                                style={{ transform: [{ rotate: isExpanded ? '180deg' : '0deg' }] }}
                            />
                        </View>
                    </View>

                    {/* Expanded Actions */}
                    {isExpanded && (
                        <MotiView
                            from={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="px-4 pb-4 border-t border-white/10 pt-3"
                        >
                            <Text className="text-gray-400 text-sm mb-2">Teslimat Adresi:</Text>
                            <Text className="text-white text-sm mb-3">{item.teslimatAdresi || 'Belirtilmemiş'}</Text>

                            <Text className="text-gray-400 text-sm mb-2">Durumu Güncelle:</Text>
                            <View className="flex-row flex-wrap gap-2 mb-4">
                                {DURUMLAR.slice(1).map((d) => (
                                    <TouchableOpacity
                                        key={d.value}
                                        onPress={() => updateStatusMutation.mutate({ id: item.id, durum: d.value })}
                                        disabled={item.durum === d.value}
                                        className={`px-3 py-2 rounded-lg ${item.durum === d.value ? 'opacity-50' : ''
                                            }`}
                                        style={{ backgroundColor: `${d.color}20` }}
                                    >
                                        <Text style={{ color: d.color }} className="text-sm">{d.label}</Text>
                                    </TouchableOpacity>
                                ))}
                            </View>

                            {/* Sipariş Düzenle Butonu */}
                            <TouchableOpacity
                                onPress={() => router.push(`/admin/siparis-duzenle?id=${item.id}` as any)}
                                className="bg-blue-600/20 py-3 rounded-xl flex-row items-center justify-center"
                            >
                                <Pencil size={18} color="#3b82f6" />
                                <Text className="text-blue-400 font-semibold ml-2">İçeriği Düzenle</Text>
                            </TouchableOpacity>
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
                <View className="flex-row items-center mb-4">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <View>
                        <Text className="text-white text-2xl font-bold">
                            {params.magazaAd ? `${params.magazaAd} Siparişleri` : 'Sipariş Yönetimi'}
                        </Text>
                        {params.magazaAd && (
                            <Text className="text-gray-400 text-sm">Ciroya katkı sağlayan siparişler</Text>
                        )}
                    </View>
                </View>

                {/* Filters */}
                <FlatList
                    data={DURUMLAR}
                    horizontal
                    showsHorizontalScrollIndicator={false}
                    renderItem={({ item }) => (
                        <TouchableOpacity
                            onPress={() => setSelectedDurum(item.value)}
                            className={`mr-2 px-4 py-2 rounded-full ${selectedDurum === item.value ? 'bg-primary-600' : 'bg-white/10'
                                }`}
                        >
                            <Text className={selectedDurum === item.value ? 'text-white' : 'text-gray-300'}>
                                {item.label}
                            </Text>
                        </TouchableOpacity>
                    )}
                    keyExtractor={(item) => item.value}
                />
            </View>

            {/* Stats */}
            <View className="flex-row px-5 mb-4 gap-3">
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Toplam</Text>
                    <Text className="text-white text-xl font-bold">{siparisler.length}</Text>
                </View>
                <View className="flex-1 bg-yellow-500/10 rounded-xl p-3">
                    <Text className="text-yellow-400 text-sm">Bekleyen</Text>
                    <Text className="text-yellow-400 text-xl font-bold">
                        {siparisler.filter((s: any) => s.durum === 'BEKLEMEDE').length}
                    </Text>
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
                    <View className="items-center py-10">
                        <ShoppingCart size={48} color="#6b7280" />
                        <Text className="text-gray-400 mt-4">Sipariş bulunamadı</Text>
                    </View>
                }
            />
        </View>
    );
}
