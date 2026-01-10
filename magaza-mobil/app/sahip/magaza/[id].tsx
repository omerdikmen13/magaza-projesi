import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
    StatusBar,
    Image,
    Alert,
    TextInput,
    Modal,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Store,
    Package,
    ShoppingCart,
    TrendingUp,
    Plus,
    Settings,
    Edit,
    Trash2,
    Power,
    MessageCircle,
    Eye,
    EyeOff,
    Save,
    X,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi, urunlerApi } from '../../../services/api';

export default function SahipMagazaDetayScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const magazaId = Number(id);
    const queryClient = useQueryClient();
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [editForm, setEditForm] = useState({ ad: '', aciklama: '' });

    const { data: panel, isLoading, refetch } = useQuery({
        queryKey: ['sahip-panel'],
        queryFn: magazaSahibiApi.getPanel,
    });

    const { data: urunler = [], isLoading: urunLoading } = useQuery({
        queryKey: ['sahip-magaza-urunler', magazaId],
        queryFn: () => magazaSahibiApi.getMagazaUrunler(magazaId),
    });

    const magazalar = panel?.magazalar || [];
    const magaza = magazalar.find((m: any) => m.id === magazaId);

    // Ürün silme
    const deleteUrunMutation = useMutation({
        mutationFn: (urunId: number) => magazaSahibiApi.deleteUrun(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-magaza-urunler'] });
            Alert.alert('✅ Başarılı', 'Ürün silindi');
        },
        onError: (err: any) => {
            Alert.alert('Hata', err.response?.data?.error || 'Ürün silinemedi');
        }
    });

    // Ürün aktif/pasif
    const toggleUrunMutation = useMutation({
        mutationFn: (urunId: number) => magazaSahibiApi.toggleUrunStatus(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-magaza-urunler'] });
            queryClient.invalidateQueries({ queryKey: ['sahip-panel'] });
            Alert.alert('✅ Başarılı', 'Ürün durumu güncellendi');
        },
        onError: (err: any) => {
            Alert.alert('Hata', err.response?.data?.error || 'Durum güncellenemedi');
        }
    });

    const handleDeleteUrun = (urunId: number, urunAd: string) => {
        Alert.alert(
            '⚠️ Ürün Sil',
            `"${urunAd}" ürününü silmek istediğinize emin misiniz?`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteUrunMutation.mutate(urunId) }
            ]
        );
    };

    const handleToggleUrun = (urunId: number) => {
        toggleUrunMutation.mutate(urunId);
    };

    if (!magaza) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <Store size={64} color="#a0aec0" />
                <Text className="text-gray-900 font-bold text-xl mt-4">Mağaza bulunamadı</Text>
                <TouchableOpacity onPress={() => router.back()} className="mt-4">
                    <Text className="text-primary-500 font-bold">Geri Dön</Text>
                </TouchableOpacity>
            </View>
        );
    }

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="light-content" />

            {/* Header */}
            <LinearGradient
                colors={['#1a1a2e', '#16213e']}
                className="pt-12 pb-6 px-5"
            >
                <View className="flex-row items-center mb-4">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <Text className="text-white text-lg font-bold flex-1">Mağaza Yönetimi</Text>
                    <TouchableOpacity
                        onPress={() => {
                            setEditForm({ ad: magaza.ad || '', aciklama: magaza.aciklama || '' });
                            setEditModalVisible(true);
                        }}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center"
                    >
                        <Edit size={18} color="white" />
                    </TouchableOpacity>
                </View>

                {/* Store Info */}
                <View className="flex-row items-center">
                    {magaza.logoUrl ? (
                        <Image
                            source={{ uri: magaza.logoUrl }}
                            className="w-16 h-16 rounded-full bg-white mr-4"
                            resizeMode="contain"
                        />
                    ) : (
                        <View className="w-16 h-16 rounded-full bg-white/20 items-center justify-center mr-4">
                            <Text className="text-white text-2xl font-bold">{magaza.ad?.charAt(0)}</Text>
                        </View>
                    )}
                    <View className="flex-1">
                        <Text className="text-white text-xl font-bold">{magaza.ad}</Text>
                        <Text className="text-white/70">{magaza.aciklama || 'Moda mağazası'}</Text>
                    </View>
                </View>

                {/* Stats */}
                <View className="flex-row gap-2 mt-4">
                    <View className="flex-1 bg-white/10 rounded-xl p-3 items-center">
                        <Package size={18} color="#6bcb77" />
                        <Text className="text-white font-bold mt-1">{magaza.urunSayisi || 0}</Text>
                        <Text className="text-white/60 text-xs">Ürün</Text>
                    </View>
                    <View className="flex-1 bg-white/10 rounded-xl p-3 items-center">
                        <ShoppingCart size={18} color="#ffd93d" />
                        <Text className="text-white font-bold mt-1">{magaza.siparisSayisi || 0}</Text>
                        <Text className="text-white/60 text-xs">Sipariş</Text>
                    </View>
                    <View className="flex-1 bg-white/10 rounded-xl p-3 items-center">
                        <TrendingUp size={18} color="#38ef7d" />
                        <Text className="text-white font-bold mt-1">₺{magaza.toplamCiro || 0}</Text>
                        <Text className="text-white/60 text-xs">Ciro</Text>
                    </View>
                </View>
            </LinearGradient>

            <ScrollView
                className="flex-1"
                contentContainerStyle={{ paddingBottom: 40 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
            >
                {/* Hızlı İşlemler */}
                <View className="px-5 py-4">
                    <Text className="text-gray-900 font-bold text-lg mb-3">⚡ Hızlı İşlemler</Text>
                    <View className="flex-row gap-3">
                        <TouchableOpacity
                            onPress={() => router.push(`/sahip/urun-ekle?magazaId=${magazaId}`)}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#38a169', '#2f855a']}
                                className="p-4 rounded-xl items-center"
                            >
                                <Plus size={20} color="white" />
                                <Text className="text-white font-bold mt-1 text-sm">Ürün Ekle</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => router.push(`/sahip/siparisler?magazaId=${magazaId}`)}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#ed8936', '#dd6b20']}
                                className="p-4 rounded-xl items-center"
                            >
                                <ShoppingCart size={20} color="white" />
                                <Text className="text-white font-bold mt-1 text-sm">Siparişler</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => router.push(`/sahip/mesajlar?magazaId=${magazaId}`)}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#3182ce', '#2b6cb0']}
                                className="p-4 rounded-xl items-center"
                            >
                                <MessageCircle size={20} color="white" />
                                <Text className="text-white font-bold mt-1 text-sm">Mesajlar</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Ürünler */}
                <View className="px-5">
                    <View className="flex-row items-center justify-between mb-3">
                        <Text className="text-gray-900 font-bold text-lg">📦 Ürünlerim ({urunler.length})</Text>
                    </View>

                    {urunLoading ? (
                        <Text className="text-gray-500 text-center py-8">Yükleniyor...</Text>
                    ) : urunler.length === 0 ? (
                        <View className="bg-white rounded-2xl p-8 items-center">
                            <Package size={48} color="#a0aec0" />
                            <Text className="text-gray-900 font-bold mt-4">Henüz ürün yok</Text>
                            <TouchableOpacity
                                onPress={() => router.push(`/sahip/urun-ekle?magazaId=${magazaId}`)}
                                className="mt-4"
                            >
                                <LinearGradient
                                    colors={['#667eea', '#764ba2']}
                                    className="px-6 py-3 rounded-xl"
                                >
                                    <Text className="text-white font-bold">İlk Ürünü Ekle</Text>
                                </LinearGradient>
                            </TouchableOpacity>
                        </View>
                    ) : (
                        urunler.map((urun: any, index: number) => (
                            <MotiView
                                key={urun.id}
                                from={{ opacity: 0, translateY: 10 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: index * 30 }}
                            >
                                <View
                                    className={`bg-white rounded-2xl mb-3 overflow-hidden ${!urun.aktif ? 'opacity-60' : ''}`}
                                    style={{ elevation: 2 }}
                                >
                                    <View className="flex-row">
                                        {/* Ürün Resmi */}
                                        {urun.resimUrl ? (
                                            <Image
                                                source={{ uri: urun.resimUrl }}
                                                className="w-24 h-24"
                                                resizeMode="cover"
                                            />
                                        ) : (
                                            <View className="w-24 h-24 bg-gray-100 items-center justify-center">
                                                <Package size={32} color="#a0aec0" />
                                            </View>
                                        )}

                                        {/* Ürün Bilgileri */}
                                        <View className="flex-1 p-3">
                                            <View className="flex-row items-start justify-between">
                                                <View className="flex-1 mr-2">
                                                    <Text className="text-gray-900 font-bold" numberOfLines={1}>{urun.ad}</Text>
                                                    <Text className="text-primary-500 font-bold mt-1">₺{urun.fiyat}</Text>
                                                    {!urun.aktif && (
                                                        <View className="bg-red-100 px-2 py-0.5 rounded mt-1 self-start">
                                                            <Text className="text-red-600 text-xs">Pasif</Text>
                                                        </View>
                                                    )}
                                                </View>
                                            </View>

                                            {/* Aksiyon Butonları */}
                                            <View className="flex-row gap-2 mt-2">
                                                <TouchableOpacity
                                                    onPress={() => router.push(`/sahip/urun-ekle?magazaId=${magazaId}&urunId=${urun.id}`)}
                                                    className="bg-blue-100 px-3 py-1.5 rounded-lg flex-row items-center"
                                                >
                                                    <Edit size={14} color="#3182ce" />
                                                    <Text className="text-blue-600 text-xs font-medium ml-1">Düzenle</Text>
                                                </TouchableOpacity>
                                                <TouchableOpacity
                                                    onPress={() => handleToggleUrun(urun.id)}
                                                    className={`px-3 py-1.5 rounded-lg flex-row items-center ${urun.aktif ? 'bg-orange-100' : 'bg-green-100'}`}
                                                >
                                                    {urun.aktif ? (
                                                        <>
                                                            <EyeOff size={14} color="#dd6b20" />
                                                            <Text className="text-orange-600 text-xs font-medium ml-1">Pasif</Text>
                                                        </>
                                                    ) : (
                                                        <>
                                                            <Eye size={14} color="#38a169" />
                                                            <Text className="text-green-600 text-xs font-medium ml-1">Aktif</Text>
                                                        </>
                                                    )}
                                                </TouchableOpacity>
                                                <TouchableOpacity
                                                    onPress={() => handleDeleteUrun(urun.id, urun.ad)}
                                                    className="bg-red-100 px-3 py-1.5 rounded-lg flex-row items-center"
                                                >
                                                    <Trash2 size={14} color="#e53e3e" />
                                                    <Text className="text-red-600 text-xs font-medium ml-1">Sil</Text>
                                                </TouchableOpacity>
                                            </View>
                                        </View>
                                    </View>
                                </View>
                            </MotiView>
                        ))
                    )}
                </View>
            </ScrollView>

            {/* Edit Modal */}
            <Modal
                visible={editModalVisible}
                transparent
                animationType="slide"
                onRequestClose={() => setEditModalVisible(false)}
            >
                <View className="flex-1 bg-black/50 justify-end">
                    <View className="bg-white rounded-t-3xl p-6">
                        <View className="flex-row items-center justify-between mb-6">
                            <Text className="text-gray-900 text-xl font-bold">Mağaza Düzenle</Text>
                            <TouchableOpacity onPress={() => setEditModalVisible(false)}>
                                <X size={24} color="#4a5568" />
                            </TouchableOpacity>
                        </View>

                        <View className="mb-4">
                            <Text className="text-gray-700 font-medium mb-2">Mağaza Adı</Text>
                            <TextInput
                                value={editForm.ad}
                                onChangeText={(t) => setEditForm({ ...editForm, ad: t })}
                                className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Mağaza adı"
                            />
                        </View>

                        <View className="mb-6">
                            <Text className="text-gray-700 font-medium mb-2">Açıklama</Text>
                            <TextInput
                                value={editForm.aciklama}
                                onChangeText={(t) => setEditForm({ ...editForm, aciklama: t })}
                                className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Mağaza açıklaması"
                                multiline
                                numberOfLines={3}
                            />
                        </View>

                        <TouchableOpacity
                            onPress={() => {
                                Alert.alert('Bilgi', 'Mağaza güncelleme özelliği yakında eklenecek');
                                setEditModalVisible(false);
                            }}
                        >
                            <LinearGradient
                                colors={['#667eea', '#764ba2']}
                                className="py-4 rounded-xl flex-row items-center justify-center"
                            >
                                <Save size={20} color="white" />
                                <Text className="text-white font-bold text-lg ml-2">Kaydet</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>
            </Modal>
        </View>
    );
}
