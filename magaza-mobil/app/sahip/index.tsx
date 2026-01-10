import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
    StatusBar,
    Image,
    TextInput,
    Modal,
    Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    Store,
    Package,
    ShoppingCart,
    TrendingUp,
    Plus,
    ArrowRight,
    MessageCircle,
    Settings,
    BarChart3,
    Briefcase,
    Clock,
    LogOut,
    X,
    Trash2,
    Edit,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function SahipPanelScreen() {
    const { user, logout } = useAuthStore();
    const queryClient = useQueryClient();
    const [createModalVisible, setCreateModalVisible] = useState(false);
    const [newMagazaForm, setNewMagazaForm] = useState({ ad: '', aciklama: '' });

    const { data: panel, isLoading, refetch } = useQuery({
        queryKey: ['sahip-panel'],
        queryFn: magazaSahibiApi.getPanel,
    });

    const createMagazaMutation = useMutation({
        mutationFn: (data: { ad: string; aciklama: string }) => magazaSahibiApi.createMagaza(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-panel'] });
            setCreateModalVisible(false);
            setNewMagazaForm({ ad: '', aciklama: '' });
            Alert.alert('✅ Başarılı', 'Mağaza oluşturuldu!');
        },
        onError: (err: any) => {
            Alert.alert('Hata', err.response?.data?.error || 'Mağaza oluşturulamadı');
        }
    });

    const deleteMagazaMutation = useMutation({
        mutationFn: (magazaId: number) => magazaSahibiApi.deleteMagaza(magazaId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-panel'] });
            Alert.alert('✅ Başarılı', 'Mağaza silindi');
        },
        onError: (err: any) => {
            Alert.alert('Hata', err.response?.data?.error || 'Mağaza silinemedi');
        }
    });

    const handleDeleteMagaza = (magazaId: number, magazaAd: string) => {
        Alert.alert(
            '⚠️ Mağaza Sil',
            `"${magazaAd}" mağazasını ve tüm ürünlerini silmek istediğinize emin misiniz? Bu işlem geri alınamaz!`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteMagazaMutation.mutate(magazaId) }
            ]
        );
    };

    const handleCreateMagaza = () => {
        if (!newMagazaForm.ad.trim()) {
            Alert.alert('Hata', 'Mağaza adı zorunludur');
            return;
        }
        createMagazaMutation.mutate(newMagazaForm);
    };

    const magazalar = panel?.magazalar || [];
    const toplamCiro = magazalar.reduce((t: number, m: any) => t + (m.toplamCiro || 0), 0);
    const toplamSiparis = magazalar.reduce((t: number, m: any) => t + (m.siparisSayisi || 0), 0);
    const toplamUrun = magazalar.reduce((t: number, m: any) => t + (m.urunSayisi || 0), 0);
    const bekleyenSiparis = magazalar.reduce((t: number, m: any) => t + (m.bekleyenSiparis || 0), 0);

    const handleLogout = () => {
        Alert.alert('Çıkış Yap', 'Çıkış yapmak istediğinize emin misiniz?', [
            { text: 'İptal', style: 'cancel' },
            {
                text: 'Çıkış Yap', style: 'destructive', onPress: () => {
                    logout();
                    router.replace('/(tabs)/hesabim');
                }
            },
        ]);
    };

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
                        onPress={handleLogout}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <LogOut size={20} color="white" />
                    </TouchableOpacity>
                    <View className="flex-1">
                        <View className="flex-row items-center">
                            <Briefcase size={14} color="#667eea" />
                            <Text className="text-primary-400 text-xs ml-2 font-medium">MAĞAZA YÖNETİM PANELİ</Text>
                        </View>
                        <Text className="text-white text-xl font-bold mt-1">Merhaba, {user?.ad} 👋</Text>
                    </View>
                    <TouchableOpacity
                        onPress={() => router.push('/sahip/ayarlar')}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center"
                    >
                        <Settings size={18} color="white" />
                    </TouchableOpacity>
                </View>

                {/* Stats */}
                <View className="flex-row gap-2 mt-2">
                    <TouchableOpacity
                        onPress={() => router.push('/sahip/ciro')}
                        className="flex-1 bg-white/10 rounded-xl p-3 items-center"
                    >
                        <TrendingUp size={18} color="#38ef7d" />
                        <Text className="text-white font-bold text-lg mt-1">₺{toplamCiro.toFixed(0)}</Text>
                        <Text className="text-white/60 text-xs">Toplam Ciro</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                        onPress={() => router.push('/sahip/siparisler')}
                        className="flex-1 bg-white/10 rounded-xl p-3 items-center"
                    >
                        <ShoppingCart size={18} color="#ffd93d" />
                        <Text className="text-white font-bold text-lg mt-1">{toplamSiparis}</Text>
                        <Text className="text-white/60 text-xs">Sipariş</Text>
                    </TouchableOpacity>
                    <View className="flex-1 bg-white/10 rounded-xl p-3 items-center">
                        <Package size={18} color="#6bcb77" />
                        <Text className="text-white font-bold text-lg mt-1">{toplamUrun}</Text>
                        <Text className="text-white/60 text-xs">Ürün</Text>
                    </View>
                </View>
            </LinearGradient>

            <ScrollView
                contentContainerStyle={{ paddingBottom: 40 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
            >
                {/* Hızlı İşlemler */}
                <View className="px-5 pt-6">
                    <Text className="text-gray-900 font-bold text-lg mb-4">⚡ Hızlı İşlemler</Text>

                    <View className="flex-row gap-3 mb-3">
                        <TouchableOpacity
                            onPress={() => router.push('/sahip/urun-ekle')}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#38a169', '#2f855a']}
                                className="p-4 rounded-2xl items-center"
                            >
                                <Plus size={24} color="white" />
                                <Text className="text-white font-bold mt-2">Ürün Ekle</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => router.push('/sahip/siparisler')}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#ed8936', '#dd6b20']}
                                className="p-4 rounded-2xl items-center"
                            >
                                <ShoppingCart size={24} color="white" />
                                <Text className="text-white font-bold mt-2">Siparişler</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>

                    <View className="flex-row gap-3">
                        <TouchableOpacity
                            onPress={() => router.push('/sahip/ciro')}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#805ad5', '#6b46c1']}
                                className="p-4 rounded-2xl items-center"
                            >
                                <BarChart3 size={24} color="white" />
                                <Text className="text-white font-bold mt-2">Ciro Raporu</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                        <TouchableOpacity
                            onPress={() => {
                                if (magazalar.length > 0) {
                                    router.push(`/sahip/mesajlar?magazaId=${magazalar[0].id}`);
                                } else {
                                    Alert.alert('Uyarı', 'Mesajları görmek için önce bir mağaza oluşturmalısınız.');
                                }
                            }}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={['#3182ce', '#2b6cb0']}
                                className="p-4 rounded-2xl items-center"
                            >
                                <MessageCircle size={24} color="white" />
                                <Text className="text-white font-bold mt-2">Mesajlar</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>

                {/* Mağazalarım */}
                <View className="px-5 pt-8">
                    <View className="flex-row items-center justify-between mb-4">
                        <Text className="text-gray-900 font-bold text-lg">🏪 Mağazalarım</Text>
                        <TouchableOpacity
                            onPress={() => setCreateModalVisible(true)}
                            className="bg-primary-500 px-3 py-2 rounded-xl flex-row items-center"
                        >
                            <Plus size={16} color="white" />
                            <Text className="text-white font-medium ml-1">Yeni Mağaza</Text>
                        </TouchableOpacity>
                    </View>

                    {magazalar.length === 0 ? (
                        <TouchableOpacity
                            onPress={() => setCreateModalVisible(true)}
                            className="bg-white rounded-2xl p-8 items-center border-2 border-dashed border-gray-300"
                        >
                            <Plus size={48} color="#667eea" />
                            <Text className="text-gray-900 font-bold text-xl mt-4">Yeni Mağaza Oluştur</Text>
                            <Text className="text-gray-500 text-center mt-2">
                                İlk mağazanızı oluşturarak satışa başlayın
                            </Text>
                        </TouchableOpacity>
                    ) : (
                        magazalar.map((magaza: any, index: number) => (
                            <MotiView
                                key={magaza.id}
                                from={{ opacity: 0, translateY: 20 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: index * 100 }}
                            >
                                <View className="bg-white rounded-2xl mb-4 overflow-hidden shadow-sm" style={{ elevation: 2 }}>
                                    {/* Store Header */}
                                    <TouchableOpacity
                                        onPress={() => router.push(`/sahip/magaza/${magaza.id}` as any)}
                                    >
                                        <LinearGradient
                                            colors={['#667eea', '#764ba2']}
                                            className="p-4 flex-row items-center"
                                        >
                                            {magaza.logoUrl ? (
                                                <Image
                                                    source={{ uri: magaza.logoUrl }}
                                                    className="w-14 h-14 rounded-full bg-white mr-4"
                                                    resizeMode="contain"
                                                />
                                            ) : (
                                                <View className="w-14 h-14 rounded-full bg-white/20 items-center justify-center mr-4">
                                                    <Text className="text-white text-xl font-bold">
                                                        {magaza.ad?.charAt(0)}
                                                    </Text>
                                                </View>
                                            )}
                                            <View className="flex-1">
                                                <Text className="text-white font-bold text-lg">{magaza.ad}</Text>
                                                <Text className="text-white/80 text-sm" numberOfLines={1}>
                                                    {magaza.aciklama || 'Yönetmek için tıklayın'}
                                                </Text>
                                            </View>
                                            <ArrowRight size={20} color="white" />
                                        </LinearGradient>
                                    </TouchableOpacity>

                                    {/* Stats */}
                                    <View className="flex-row p-4 border-b border-gray-100">
                                        <View className="flex-1 items-center">
                                            <Text className="text-gray-500 text-xs">Ürün</Text>
                                            <Text className="text-gray-900 font-bold text-lg">{magaza.urunSayisi || 0}</Text>
                                        </View>
                                        <View className="flex-1 items-center border-x border-gray-100">
                                            <Text className="text-gray-500 text-xs">Sipariş</Text>
                                            <Text className="text-gray-900 font-bold text-lg">{magaza.siparisSayisi || 0}</Text>
                                        </View>
                                        <View className="flex-1 items-center">
                                            <Text className="text-gray-500 text-xs">Ciro</Text>
                                            <Text className="text-primary-500 font-bold text-lg">₺{magaza.toplamCiro || 0}</Text>
                                        </View>
                                    </View>

                                    {/* Actions */}
                                    <View className="flex-row p-3 gap-2">
                                        <TouchableOpacity
                                            onPress={() => router.push(`/sahip/magaza/${magaza.id}` as any)}
                                            className="flex-1 bg-primary-500 py-2 rounded-xl items-center"
                                        >
                                            <Text className="text-white font-bold">Yönet</Text>
                                        </TouchableOpacity>
                                        <TouchableOpacity
                                            onPress={() => handleDeleteMagaza(magaza.id, magaza.ad)}
                                            className="bg-red-100 px-4 py-2 rounded-xl items-center"
                                        >
                                            <Trash2 size={18} color="#e53e3e" />
                                        </TouchableOpacity>
                                    </View>
                                </View>
                            </MotiView>
                        ))
                    )}
                </View>
            </ScrollView>

            {/* Create Store Modal */}
            <Modal
                visible={createModalVisible}
                transparent
                animationType="slide"
                onRequestClose={() => setCreateModalVisible(false)}
            >
                <View className="flex-1 bg-black/50 justify-end">
                    <View className="bg-white rounded-t-3xl p-6">
                        <View className="flex-row items-center justify-between mb-6">
                            <Text className="text-gray-900 text-xl font-bold">🏪 Yeni Mağaza Oluştur</Text>
                            <TouchableOpacity onPress={() => setCreateModalVisible(false)}>
                                <X size={24} color="#4a5568" />
                            </TouchableOpacity>
                        </View>

                        <View className="mb-4">
                            <Text className="text-gray-700 font-medium mb-2">Mağaza Adı *</Text>
                            <TextInput
                                value={newMagazaForm.ad}
                                onChangeText={(t) => setNewMagazaForm({ ...newMagazaForm, ad: t })}
                                className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Mağaza adını girin"
                            />
                        </View>

                        <View className="mb-6">
                            <Text className="text-gray-700 font-medium mb-2">Açıklama</Text>
                            <TextInput
                                value={newMagazaForm.aciklama}
                                onChangeText={(t) => setNewMagazaForm({ ...newMagazaForm, aciklama: t })}
                                className="bg-gray-100 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Mağaza açıklaması"
                                multiline
                                numberOfLines={3}
                            />
                        </View>

                        <TouchableOpacity
                            onPress={handleCreateMagaza}
                            disabled={createMagazaMutation.isPending}
                        >
                            <LinearGradient
                                colors={['#667eea', '#764ba2']}
                                className="py-4 rounded-xl items-center"
                            >
                                <Text className="text-white font-bold text-lg">
                                    {createMagazaMutation.isPending ? 'Oluşturuluyor...' : 'Mağaza Oluştur'}
                                </Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>
            </Modal>
        </View>
    );
}
