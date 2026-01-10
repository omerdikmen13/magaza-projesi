import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    StatusBar,
    Alert,
    Modal,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Package,
    Clock,
    Check,
    Truck,
    X,
    ChevronDown,
    ChevronUp,
    User,
    MapPin,
    CheckCircle,
    PackageCheck,
    XCircle,
    AlertCircle,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi } from '../../services/api';

// Web'deki tüm sipariş durumları
const DURUMLAR = [
    { value: 'BEKLEMEDE', label: 'Beklemede', color: '#d69e2e', bgColor: '#faf089', icon: Clock },
    { value: 'ONAYLANDI', label: 'Onaylandı', color: '#3182ce', bgColor: '#bee3f8', icon: CheckCircle },
    { value: 'HAZIRLANIYOR', label: 'Hazırlanıyor', color: '#805ad5', bgColor: '#e9d8fd', icon: Package },
    { value: 'KARGODA', label: 'Kargoda', color: '#dd6b20', bgColor: '#feebc8', icon: Truck },
    { value: 'TESLIM_EDILDI', label: 'Teslim Edildi', color: '#38a169', bgColor: '#c6f6d5', icon: PackageCheck },
    { value: 'IPTAL', label: 'İptal Edildi', color: '#e53e3e', bgColor: '#fed7d7', icon: XCircle },
];

export default function SahipSiparislerScreen() {
    const params = useLocalSearchParams<{ magazaId?: string }>();
    const magazaId = params.magazaId ? Number(params.magazaId) : 1;
    const queryClient = useQueryClient();
    const [expandedOrder, setExpandedOrder] = useState<number | null>(null);
    const [selectedFilter, setSelectedFilter] = useState<string | null>(null);
    const [durumModalVisible, setDurumModalVisible] = useState(false);
    const [selectedSiparis, setSelectedSiparis] = useState<{ id: number; durum: string } | null>(null);

    const { data: siparisler = [], isLoading, refetch } = useQuery({
        queryKey: ['sahip-siparisler', magazaId],
        queryFn: () => magazaSahibiApi.getMagazaSiparisler(magazaId),
    });

    const updateDurumMutation = useMutation({
        mutationFn: ({ siparisId, durum }: { siparisId: number; durum: string }) =>
            magazaSahibiApi.updateSiparisDurum(siparisId, durum),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-siparisler'] });
            setDurumModalVisible(false);
            Alert.alert('✅ Başarılı', 'Sipariş durumu güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Durum güncellenemedi');
        },
    });

    const handleDurumChange = (siparisId: number, currentDurum: string) => {
        setSelectedSiparis({ id: siparisId, durum: currentDurum });
        setDurumModalVisible(true);
    };

    const selectDurum = (durum: string) => {
        if (selectedSiparis) {
            updateDurumMutation.mutate({ siparisId: selectedSiparis.id, durum });
        }
    };

    const getDurumInfo = (durum: string) => DURUMLAR.find(d => d.value === durum) || DURUMLAR[0];

    // Filtrelenmiş siparişler
    const filteredSiparisler = selectedFilter
        ? siparisler.filter((s: any) => s.durum === selectedFilter)
        : siparisler;

    const renderOrder = ({ item, index }: any) => {
        const durumInfo = getDurumInfo(item.durum);
        const isExpanded = expandedOrder === item.id;
        const DurumIcon = durumInfo.icon;

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
                    {/* Header */}
                    <View className="p-4">
                        <View className="flex-row items-center justify-between mb-3">
                            <View className="flex-row items-center">
                                <View
                                    className="w-10 h-10 rounded-full items-center justify-center mr-3"
                                    style={{ backgroundColor: durumInfo.bgColor }}
                                >
                                    <DurumIcon size={20} color={durumInfo.color} />
                                </View>
                                <View>
                                    <Text className="text-gray-900 font-bold">Sipariş #{item.id}</Text>
                                    <Text className="text-gray-500 text-sm">
                                        {new Date(item.siparisTarihi).toLocaleDateString('tr-TR')}
                                    </Text>
                                </View>
                            </View>
                            <View className="items-end">
                                <Text className="text-primary-500 font-bold text-lg">₺{item.toplamTutar}</Text>
                                {isExpanded ? (
                                    <ChevronUp size={18} color="#667eea" />
                                ) : (
                                    <ChevronDown size={18} color="#a0aec0" />
                                )}
                            </View>
                        </View>

                        {/* Durum Badge */}
                        <View
                            className="self-start px-3 py-1.5 rounded-full"
                            style={{ backgroundColor: durumInfo.bgColor }}
                        >
                            <Text style={{ color: durumInfo.color }} className="font-semibold text-sm">
                                {durumInfo.label}
                            </Text>
                        </View>
                    </View>

                    {/* Expanded Content */}
                    {isExpanded && (
                        <View className="px-4 pb-4 border-t border-gray-100 pt-4">
                            {/* Müşteri Bilgileri */}
                            <Text className="text-gray-900 font-bold mb-3">👤 Müşteri Bilgileri</Text>
                            <View className="bg-gray-50 rounded-xl p-3 mb-4">
                                <View className="flex-row items-center mb-2">
                                    <User size={16} color="#667eea" />
                                    <Text className="text-gray-900 ml-2">{item.musteriAd || 'Müşteri'}</Text>
                                </View>
                                {item.teslimatAdresi && (
                                    <View className="flex-row items-start">
                                        <MapPin size={16} color="#667eea" />
                                        <Text className="text-gray-500 ml-2 flex-1">{item.teslimatAdresi}</Text>
                                    </View>
                                )}
                            </View>

                            {/* Ürünler */}
                            {item.detaylar && item.detaylar.length > 0 && (
                                <>
                                    <Text className="text-gray-900 font-bold mb-3">📦 Sipariş Detayları</Text>
                                    <View className="bg-gray-50 rounded-xl p-3 mb-4">
                                        {item.detaylar.map((detay: any, i: number) => (
                                            <View
                                                key={i}
                                                className={`flex-row justify-between ${i > 0 ? 'pt-2 mt-2 border-t border-gray-200' : ''}`}
                                            >
                                                <View className="flex-1">
                                                    <Text className="text-gray-900">{detay.urunAd}</Text>
                                                    <Text className="text-gray-500 text-sm">
                                                        Beden: {detay.bedenAd} • Adet: {detay.adet}
                                                    </Text>
                                                </View>
                                                <Text className="text-gray-900 font-bold">₺{detay.toplamFiyat}</Text>
                                            </View>
                                        ))}
                                    </View>
                                </>
                            )}

                            {/* Düzenle Butonları */}
                            <View className="flex-row mt-3 space-x-3">
                                <TouchableOpacity
                                    onPress={() => router.push(`/sahip/siparis-duzenle?id=${item.id}&magazaId=${magazaId}` as any)}
                                    className="flex-1 bg-gray-100 py-3 rounded-xl flex-row items-center justify-center border border-gray-200"
                                >
                                    <View className="mr-2"><Package size={18} color="#4a5568" /></View>
                                    <Text className="text-gray-700 font-bold">İçeriği Düzenle</Text>
                                </TouchableOpacity>

                                <TouchableOpacity
                                    onPress={() => handleDurumChange(item.id, item.durum)}
                                    className="flex-1"
                                >
                                    <LinearGradient
                                        colors={['#667eea', '#764ba2']}
                                        className="py-3 rounded-xl flex-row items-center justify-center"
                                    >
                                        <View className="mr-2"><CheckCircle size={18} color="white" /></View>
                                        <Text className="text-white font-bold">Durum Değiştir</Text>
                                    </LinearGradient>
                                </TouchableOpacity>
                            </View>
                        </View>
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
                    <View className="flex-1">
                        <Text className="text-gray-900 text-xl font-bold">📦 Siparişler</Text>
                        <Text className="text-gray-500 text-sm">{siparisler.length} sipariş</Text>
                    </View>
                </View>

                {/* Filter Chips */}
                <FlatList
                    horizontal
                    data={[{ value: null, label: 'Tümü' }, ...DURUMLAR]}
                    keyExtractor={(item) => item.value || 'all'}
                    showsHorizontalScrollIndicator={false}
                    className="mt-4 -mx-1"
                    renderItem={({ item }) => (
                        <TouchableOpacity
                            onPress={() => setSelectedFilter(item.value)}
                            className={`px-4 py-2 rounded-full mx-1 ${selectedFilter === item.value
                                ? 'bg-primary-500'
                                : 'bg-gray-100'
                                }`}
                        >
                            <Text
                                className={`font-medium ${selectedFilter === item.value
                                    ? 'text-white'
                                    : 'text-gray-600'
                                    }`}
                            >
                                {item.label}
                            </Text>
                        </TouchableOpacity>
                    )}
                />
            </View>

            {/* Orders List */}
            <FlatList
                data={filteredSiparisler}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ padding: 20 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
                renderItem={renderOrder}
                ListEmptyComponent={
                    <View className="items-center py-16">
                        <Package size={64} color="#a0aec0" />
                        <Text className="text-gray-900 font-bold text-xl mt-4">Sipariş Yok</Text>
                        <Text className="text-gray-500 text-center mt-2 px-8">
                            {selectedFilter
                                ? `${getDurumInfo(selectedFilter).label} durumunda sipariş bulunmuyor`
                                : 'Henüz sipariş almadınız'}
                        </Text>
                    </View>
                }
            />

            {/* Durum Değiştirme Modal */}
            <Modal
                visible={durumModalVisible}
                transparent
                animationType="slide"
                onRequestClose={() => setDurumModalVisible(false)}
            >
                <View className="flex-1 bg-black/50 justify-end">
                    <View className="bg-white rounded-t-3xl p-6">
                        <View className="flex-row items-center justify-between mb-6">
                            <Text className="text-gray-900 text-xl font-bold">📦 Durum Değiştir</Text>
                            <TouchableOpacity onPress={() => setDurumModalVisible(false)}>
                                <X size={24} color="#4a5568" />
                            </TouchableOpacity>
                        </View>

                        <Text className="text-gray-500 mb-4">Yeni sipariş durumunu seçin:</Text>

                        {/* Tüm Durumlar */}
                        {DURUMLAR.map((durum) => {
                            const DurumIcon = durum.icon;
                            const isSelected = selectedSiparis?.durum === durum.value;
                            const isCurrent = selectedSiparis?.durum === durum.value;

                            return (
                                <TouchableOpacity
                                    key={durum.value}
                                    onPress={() => !isCurrent && selectDurum(durum.value)}
                                    disabled={isCurrent || updateDurumMutation.isPending}
                                    className={`flex-row items-center p-4 rounded-xl mb-2 ${isCurrent ? 'bg-gray-100' : 'bg-white border border-gray-200'}`}
                                >
                                    <View
                                        className="w-10 h-10 rounded-full items-center justify-center mr-4"
                                        style={{ backgroundColor: durum.bgColor }}
                                    >
                                        <DurumIcon size={20} color={durum.color} />
                                    </View>
                                    <Text
                                        className={`flex-1 font-medium ${isCurrent ? 'text-gray-400' : 'text-gray-900'}`}
                                    >
                                        {durum.label}
                                    </Text>
                                    {isCurrent && (
                                        <View className="bg-gray-200 px-2 py-1 rounded">
                                            <Text className="text-gray-500 text-xs">Mevcut</Text>
                                        </View>
                                    )}
                                </TouchableOpacity>
                            );
                        })}

                        <TouchableOpacity
                            onPress={() => setDurumModalVisible(false)}
                            className="mt-4 py-3 bg-gray-100 rounded-xl items-center"
                        >
                            <Text className="text-gray-600 font-medium">Vazgeç</Text>
                        </TouchableOpacity>
                    </View>
                </View>
            </Modal>
        </View>
    );
}
