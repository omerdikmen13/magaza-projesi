import React from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    Image,
    RefreshControl,
    StatusBar,
    Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Package,
    Plus,
    Trash2,
    Edit,
    ToggleLeft,
    ToggleRight,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi } from '../../services/api';

export default function SahipUrunlerScreen() {
    const params = useLocalSearchParams<{ magazaId?: string }>();
    const magazaId = params.magazaId ? Number(params.magazaId) : 1;
    const queryClient = useQueryClient();

    const { data: urunler = [], isLoading, refetch } = useQuery({
        queryKey: ['sahip-urunler', magazaId],
        queryFn: () => magazaSahibiApi.getMagazaUrunler(magazaId),
    });

    const toggleMutation = useMutation({
        mutationFn: (urunId: number) => magazaSahibiApi.toggleUrunStatus(urunId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sahip-urunler'] }),
    });

    const deleteMutation = useMutation({
        mutationFn: (urunId: number) => magazaSahibiApi.deleteUrun(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-urunler'] });
            Alert.alert('Başarılı', 'Ürün silindi');
        },
    });

    const handleDelete = (urunId: number, urunAd: string) => {
        Alert.alert(
            'Ürün Sil',
            `"${urunAd}" ürününü silmek istediğinize emin misiniz?`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteMutation.mutate(urunId) },
            ]
        );
    };

    const renderUrun = ({ item, index }: any) => (
        <MotiView
            from={{ opacity: 0, translateY: 20 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: index * 50 }}
            className="mb-3"
        >
            <View className="bg-white rounded-2xl shadow-sm overflow-hidden" style={{ elevation: 2 }}>
                <View className="flex-row p-4">
                    {/* Resim */}
                    <View className="w-20 h-20 bg-gray-200 rounded-xl items-center justify-center mr-4">
                        {item.resimUrl ? (
                            <Image source={{ uri: item.resimUrl }} className="w-full h-full rounded-xl" />
                        ) : (
                            <Package size={32} color="#667eea" />
                        )}
                    </View>

                    {/* Bilgi */}
                    <View className="flex-1">
                        <View className="flex-row items-center mb-1">
                            <Text className="text-gray-900 font-bold flex-1" numberOfLines={1}>{item.ad}</Text>
                            <TouchableOpacity onPress={() => toggleMutation.mutate(item.id)}>
                                {item.aktif ? (
                                    <ToggleRight size={24} color="#38a169" />
                                ) : (
                                    <ToggleLeft size={24} color="#e53e3e" />
                                )}
                            </TouchableOpacity>
                        </View>
                        <Text className="text-gray-500 text-sm" numberOfLines={1}>{item.aciklama}</Text>
                        <View className="flex-row items-center mt-2">
                            <Text className="text-primary-500 font-bold text-lg">{item.fiyat} ₺</Text>
                            <View className="ml-3 bg-gray-100 px-2 py-1 rounded">
                                <Text className="text-gray-600 text-xs">Stok: {item.toplamStok || 0}</Text>
                            </View>
                        </View>
                    </View>
                </View>

                {/* Actions */}
                <View className="flex-row border-t border-gray-100">
                    <TouchableOpacity
                        onPress={() => router.push(`/sahip/urun-ekle?urunId=${item.id}`)}
                        className="flex-1 py-3 flex-row items-center justify-center border-r border-gray-100"
                    >
                        <Edit size={16} color="#667eea" />
                        <Text className="text-primary-500 font-medium ml-2">Düzenle</Text>
                    </TouchableOpacity>
                    <TouchableOpacity
                        onPress={() => handleDelete(item.id, item.ad)}
                        className="flex-1 py-3 flex-row items-center justify-center"
                    >
                        <Trash2 size={16} color="#e53e3e" />
                        <Text className="text-red-500 font-medium ml-2">Sil</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </MotiView>
    );

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            <View className="bg-white pt-12 pb-4 px-5">
                <View className="flex-row items-center justify-between">
                    <View className="flex-row items-center">
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                        >
                            <ArrowLeft size={22} color="#4a5568" />
                        </TouchableOpacity>
                        <Text className="text-gray-900 text-xl font-bold">📦 Ürünler</Text>
                    </View>
                    <TouchableOpacity onPress={() => router.push(`/sahip/urun-ekle?magazaId=${magazaId}`)}>
                        <LinearGradient
                            colors={['#667eea', '#764ba2']}
                            className="px-4 py-2 rounded-xl flex-row items-center"
                        >
                            <Plus size={18} color="white" />
                            <Text className="text-white font-medium ml-1">Ekle</Text>
                        </LinearGradient>
                    </TouchableOpacity>
                </View>
            </View>

            <FlatList
                data={urunler}
                renderItem={renderUrun}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingTop: 16, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
                ListEmptyComponent={
                    <View className="items-center py-20">
                        <Package size={60} color="#a0aec0" />
                        <Text className="text-gray-900 font-bold text-xl mt-4">Henüz ürün yok</Text>
                        <TouchableOpacity
                            onPress={() => router.push(`/sahip/urun-ekle?magazaId=${magazaId}`)}
                            className="mt-4"
                        >
                            <LinearGradient colors={['#667eea', '#764ba2']} className="px-6 py-3 rounded-xl">
                                <Text className="text-white font-semibold">İlk Ürünü Ekle</Text>
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                }
            />
        </View>
    );
}
