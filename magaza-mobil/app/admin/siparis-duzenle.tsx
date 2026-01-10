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
    AlertCircle,
} from 'lucide-react-native';
import { adminApi } from '../../services/api';

interface SiparisDetay {
    id: number;
    urunAd: string;
    bedenAd: string;
    adet: number;
    birimFiyat: number;
    toplamFiyat: number;
}

export default function AdminSiparisDuzenleScreen() {
    const params = useLocalSearchParams<{ id: string }>();
    const siparisId = params.id ? Number(params.id) : null;
    const queryClient = useQueryClient();

    const [detaylar, setDetaylar] = useState<SiparisDetay[]>([]);
    const [silinecekIds, setSilinecekIds] = useState<number[]>([]);
    const [isModified, setIsModified] = useState(false);

    const { data: siparis, isLoading } = useQuery({
        queryKey: ['admin-siparis', siparisId],
        queryFn: () => adminApi.getSiparis(siparisId!),
        enabled: !!siparisId,
    });

    useEffect(() => {
        if (siparis && siparis.detaylar) {
            setDetaylar(JSON.parse(JSON.stringify(siparis.detaylar)));
            setSilinecekIds([]);
            setIsModified(false);
        }
    }, [siparis]);

    const updateMutation = useMutation({
        mutationFn: (data: any) => adminApi.updateSiparisIcerik(siparisId!, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-siparis', siparisId] });
            queryClient.invalidateQueries({ queryKey: ['admin-orders'] });
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

        detaylar.forEach(d => {
            detayIds.push(d.id);
            adetler.push(d.adet);
        });

        updateMutation.mutate({
            detayIds,
            adetler,
            silinecekIds
        });
    };

    if (isLoading) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center">
                <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />
                <ActivityIndicator size="large" color="#d946ef" />
            </View>
        );
    }

    if (!siparis) {
        return (
            <View className="flex-1 bg-dark-300 items-center justify-center">
                <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />
                <Text className="text-gray-400">Sipariş bulunamadı</Text>
            </View>
        );
    }

    const toplamTutar = detaylar.reduce((total, item) => total + (item.birimFiyat * item.adet), 0);
    const originalTotal = siparis.detaylar?.reduce((t: number, i: any) => t + (i.birimFiyat * i.adet), 0) || 0;
    const diff = toplamTutar - originalTotal;

    return (
        <View className="flex-1 bg-dark-300">
            <StatusBar barStyle="light-content" />
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5">
                <View className="flex-row items-center justify-between">
                    <View className="flex-row items-center">
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                        >
                            <ArrowLeft size={22} color="white" />
                        </TouchableOpacity>
                        <View>
                            <Text className="text-white text-lg font-bold">Sipariş #{siparisId} Düzenle</Text>
                            <Text className="text-gray-400 text-xs">{siparis.kullaniciAd} • {siparis.magazaAd}</Text>
                        </View>
                    </View>
                    <TouchableOpacity
                        onPress={handleSave}
                        disabled={!isModified || updateMutation.isPending}
                        className={`w-10 h-10 rounded-full items-center justify-center ${isModified ? 'bg-green-500' : 'bg-white/10'}`}
                    >
                        {updateMutation.isPending ? (
                            <ActivityIndicator color="white" size="small" />
                        ) : (
                            <Save size={20} color={isModified ? 'white' : '#6b7280'} />
                        )}
                    </TouchableOpacity>
                </View>
            </View>

            <ScrollView contentContainerStyle={{ padding: 20, paddingBottom: 100 }}>
                {/* Warning */}
                <View className="bg-orange-500/10 border border-orange-500/20 rounded-xl p-3 flex-row items-center mb-4">
                    <AlertCircle size={20} color="#f59e0b" />
                    <Text className="text-orange-400 ml-2 flex-1 text-sm">
                        Değişiklikler stokları etkileyecektir. Adet azaltma stok iadesi yapar.
                    </Text>
                </View>

                {/* Items */}
                {detaylar.map((item) => (
                    <View key={item.id} className="bg-white/5 rounded-2xl p-4 mb-3">
                        <View className="flex-row justify-between mb-2">
                            <Text className="text-white font-bold text-base flex-1 mr-2">
                                {item.urunAd}
                            </Text>
                            <TouchableOpacity onPress={() => handleDelete(item.id)}>
                                <Trash2 size={20} color="#ef4444" />
                            </TouchableOpacity>
                        </View>

                        <Text className="text-gray-400 text-sm mb-3">
                            Beden: {item.bedenAd} • Birim: ₺{item.birimFiyat}
                        </Text>

                        <View className="flex-row items-center justify-between">
                            <View className="flex-row items-center bg-white/10 rounded-lg p-1">
                                <TouchableOpacity
                                    onPress={() => handleDecrement(item.id)}
                                    className="w-8 h-8 bg-white/10 rounded-md items-center justify-center"
                                    disabled={item.adet <= 1}
                                >
                                    <Minus size={16} color={item.adet <= 1 ? '#4b5563' : 'white'} />
                                </TouchableOpacity>
                                <Text className="w-10 text-center font-bold text-lg text-white">{item.adet}</Text>
                                <TouchableOpacity
                                    onPress={() => handleIncrement(item.id)}
                                    className="w-8 h-8 bg-white/10 rounded-md items-center justify-center"
                                >
                                    <Plus size={16} color="white" />
                                </TouchableOpacity>
                            </View>

                            <Text className="text-primary-400 font-bold text-lg">
                                ₺{(item.birimFiyat * item.adet).toFixed(2)}
                            </Text>
                        </View>
                    </View>
                ))}

                {detaylar.length === 0 && (
                    <View className="items-center py-10">
                        <Trash2 size={40} color="#ef4444" />
                        <Text className="text-gray-400 mt-2 font-medium">Tüm ürünler silindi</Text>
                        <Text className="text-gray-500 text-sm text-center">Kaydettiğinizde sipariş iptal edilecektir.</Text>
                    </View>
                )}

                {/* Summary */}
                <View className="bg-white/5 rounded-2xl p-4 mt-2 border border-white/10">
                    <Text className="text-white font-bold mb-2">Özet Değişiklikler</Text>

                    <View className="flex-row justify-between mb-1">
                        <Text className="text-gray-400">Eski Tutar</Text>
                        <Text className="text-gray-300 font-medium">₺{originalTotal.toFixed(2)}</Text>
                    </View>

                    <View className="flex-row justify-between mb-3 border-b border-white/10 pb-3">
                        <Text className="text-gray-400">Yeni Tutar</Text>
                        <Text className="text-primary-400 font-bold">₺{toplamTutar.toFixed(2)}</Text>
                    </View>

                    <View className="flex-row justify-between">
                        <Text className="text-white font-bold">Fark</Text>
                        <Text className={`font-bold ${diff > 0 ? 'text-green-400' : diff < 0 ? 'text-red-400' : 'text-gray-400'}`}>
                            {diff > 0 ? '+' : ''}{diff.toFixed(2)} TL
                        </Text>
                    </View>
                </View>
            </ScrollView>
        </View>
    );
}
