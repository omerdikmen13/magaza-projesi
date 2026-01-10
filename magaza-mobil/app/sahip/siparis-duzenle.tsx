import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Alert,
    ActivityIndicator,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Save,
    Trash2,
    Minus,
    Plus,
    Package,
    AlertCircle,
} from 'lucide-react-native';
import { magazaSahibiApi } from '../../services/api';

interface SiparisDetay {
    id: number;
    urunAd: string;
    bedenAd: string;
    adet: number;
    birimFiyat: number;
    toplamFiyat: number;
}

export default function SiparisDuzenleScreen() {
    const params = useLocalSearchParams<{ id: string, magazaId: string }>();
    const siparisId = params.id ? Number(params.id) : null;
    const magazaId = params.magazaId;
    const queryClient = useQueryClient();

    const [detaylar, setDetaylar] = useState<SiparisDetay[]>([]);
    const [silinecekIds, setSilinecekIds] = useState<number[]>([]);
    const [isModified, setIsModified] = useState(false);

    const { data: siparis, isLoading } = useQuery({
        queryKey: ['sahip-siparis', siparisId],
        queryFn: () => magazaSahibiApi.getSiparis(siparisId!),
        enabled: !!siparisId,
    });

    useEffect(() => {
        if (siparis && siparis.detaylar) {
            setDetaylar(JSON.parse(JSON.stringify(siparis.detaylar))); // Deep copy
            setSilinecekIds([]);
            setIsModified(false);
        }
    }, [siparis]);

    const updateMutation = useMutation({
        mutationFn: (data: any) => magazaSahibiApi.updateSiparisIcerik(siparisId!, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-siparis', siparisId] });
            queryClient.invalidateQueries({ queryKey: ['sahip-siparisler'] });
            Alert.alert('✅ Başarılı', 'Sipariş içeriği güncellendi', [
                { text: 'Tamam', onPress: () => router.back() }
            ]);
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Güncelleme hatası');
        },
    });

    const handleIncrement = (id: number) => {
        setDetaylar(prev => prev.map(d =>
            d.id === id ? { ...d, adet: d.adet + 1 } : d
        ));
        setIsModified(true);
    };

    const handleDecrement = (id: number) => {
        setDetaylar(prev => prev.map(d =>
            d.id === id && d.adet > 1 ? { ...d, adet: d.adet - 1 } : d
        ));
        setIsModified(true);
    };

    const handleDelete = (id: number) => {
        Alert.alert('Sil', 'Bu ürünü siparişten çıkarmak istediğinize emin misiniz?', [
            { text: 'Vazgeç', style: 'cancel' },
            {
                text: 'Sil', style: 'destructive', onPress: () => {
                    setSilinecekIds(prev => [...prev, id]);
                    setDetaylar(prev => prev.filter(d => d.id !== id));
                    setIsModified(true);
                }
            }
        ]);
    };

    const handleSave = () => {
        if (!isModified) return;

        if (detaylar.length === 0) {
            Alert.alert('Uyarı', 'Tüm ürünleri sildiniz. Sipariş iptal edilecek. Onaylıyor musunuz?', [
                { text: 'Vazgeç', style: 'cancel' },
                {
                    text: 'Evet, İptal Et', style: 'destructive', onPress: submitSave
                }
            ]);
        } else {
            submitSave();
        }
    };

    const submitSave = () => {
        const detayIds: number[] = [];
        const adetler: number[] = [];

        // Mevcut listedeki (silinmemiş) detaylar
        detaylar.forEach(d => {
            detayIds.push(d.id);
            adetler.push(d.adet);
        });

        // Silinecekler zaten silinecekIds state'inde

        updateMutation.mutate({
            detayIds,
            adetler,
            silinecekIds
        });
    };

    if (isLoading) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <ActivityIndicator size="large" color="#667eea" />
            </View>
        );
    }

    if (!siparis) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <Text className="text-gray-500">Sipariş bulunamadı</Text>
            </View>
        );
    }

    const toplamTutar = detaylar.reduce((total, item) => total + (item.birimFiyat * item.adet), 0);
    const originalTotal = siparis.detaylar?.reduce((t: number, i: any) => t + (i.birimFiyat * i.adet), 0) || 0;
    const diff = toplamTutar - originalTotal;

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header */}
            <View className="bg-white pt-12 pb-4 px-5 shadow-sm z-10">
                <View className="flex-row items-center justify-between">
                    <View className="flex-row items-center">
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                        >
                            <ArrowLeft size={22} color="#4a5568" />
                        </TouchableOpacity>
                        <View>
                            <Text className="text-gray-900 text-lg font-bold">Sipariş #{siparisId} Düzenle</Text>
                            <Text className="text-gray-500 text-xs">İçerik Düzenleme</Text>
                        </View>
                    </View>
                    <TouchableOpacity
                        onPress={handleSave}
                        disabled={!isModified || updateMutation.isPending}
                    >
                        <LinearGradient
                            colors={isModified ? ['#48bb78', '#38a169'] : ['#e2e8f0', '#cbd5e0']}
                            className="w-10 h-10 rounded-full items-center justify-center"
                        >
                            {updateMutation.isPending ? (
                                <ActivityIndicator color="white" size="small" />
                            ) : (
                                <Save size={20} color={isModified ? 'white' : '#a0aec0'} />
                            )}
                        </LinearGradient>
                    </TouchableOpacity>
                </View>
            </View>

            <ScrollView contentContainerStyle={{ padding: 20 }}>
                {/* Warning */}
                <View className="bg-orange-50 border border-orange-100 rounded-xl p-3 flex-row items-center mb-4">
                    <AlertCircle size={20} color="#ed8936" />
                    <Text className="text-orange-700 ml-2 flex-1 text-sm">
                        Değişiklikler stokları etkileyecektir. Adet azaltma stok iadesi yapar.
                    </Text>
                </View>

                {/* Items */}
                {detaylar.map((item) => (
                    <View key={item.id} className="bg-white rounded-2xl p-4 mb-3 shadow-sm">
                        <View className="flex-row justify-between mb-2">
                            <Text className="text-gray-900 font-bold text-base flex-1 mr-2">
                                {item.urunAd}
                            </Text>
                            <TouchableOpacity onPress={() => handleDelete(item.id)}>
                                <Trash2 size={20} color="#e53e3e" />
                            </TouchableOpacity>
                        </View>

                        <Text className="text-gray-500 text-sm mb-3">
                            Beden: {item.bedenAd} • Birim: ₺{item.birimFiyat}
                        </Text>

                        <View className="flex-row items-center justify-between">
                            <View className="flex-row items-center bg-gray-100 rounded-lg p-1">
                                <TouchableOpacity
                                    onPress={() => handleDecrement(item.id)}
                                    className="w-8 h-8 bg-white rounded-md items-center justify-center shadow-sm"
                                    disabled={item.adet <= 1}
                                >
                                    <Minus size={16} color={item.adet <= 1 ? '#cbd5e0' : '#4a5568'} />
                                </TouchableOpacity>
                                <Text className="w-10 text-center font-bold text-lg">{item.adet}</Text>
                                <TouchableOpacity
                                    onPress={() => handleIncrement(item.id)}
                                    className="w-8 h-8 bg-white rounded-md items-center justify-center shadow-sm"
                                >
                                    <Plus size={16} color="#4a5568" />
                                </TouchableOpacity>
                            </View>

                            <Text className="text-primary-600 font-bold text-lg">
                                ₺{(item.birimFiyat * item.adet).toFixed(2)}
                            </Text>
                        </View>
                    </View>
                ))}

                {detaylar.length === 0 && (
                    <View className="items-center py-10">
                        <Trash2 size={40} color="#e53e3e" />
                        <Text className="text-gray-500 mt-2 font-medium">Tüm ürünler silindi</Text>
                        <Text className="text-gray-400 text-sm text-center">Kaydettiğinizde sipariş iptal edilecektir.</Text>
                    </View>
                )}

                {/* Summary */}
                <View className="bg-white rounded-2xl p-4 mt-2 mb-10 shadow-sm border border-gray-100">
                    <Text className="text-gray-900 font-bold mb-2">Özet Değişiklikler</Text>

                    <View className="flex-row justify-between mb-1">
                        <Text className="text-gray-500">Eski Tutar</Text>
                        <Text className="text-gray-900 font-medium">₺{originalTotal.toFixed(2)}</Text>
                    </View>

                    <View className="flex-row justify-between mb-3 border-b border-gray-100 pb-3">
                        <Text className="text-gray-500">Yeni Tutar</Text>
                        <Text className="text-primary-600 font-bold">₺{toplamTutar.toFixed(2)}</Text>
                    </View>

                    <View className="flex-row justify-between">
                        <Text className="text-gray-900 font-bold">Fark</Text>
                        <Text className={`font-bold ${diff > 0 ? 'text-green-500' : diff < 0 ? 'text-red-500' : 'text-gray-500'}`}>
                            {diff > 0 ? '+' : ''}{diff.toFixed(2)} TL
                        </Text>
                    </View>
                </View>
            </ScrollView>
        </View>
    );
}
