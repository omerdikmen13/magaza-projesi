import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    Image,
    TouchableOpacity,
    Alert,
    ActivityIndicator,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { useLocalSearchParams, router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    ShoppingCart,
    Minus,
    Plus,
    Store,
    Check,
    MessageCircle,
    Heart,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { urunlerApi, sepetApi, favorilerApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function ProductDetailScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const { isAuthenticated, isAdmin } = useAuthStore();
    const queryClient = useQueryClient();

    const [selectedBeden, setSelectedBeden] = useState<number | null>(null);
    const [quantity, setQuantity] = useState(1);

    const { data: urun, isLoading } = useQuery({
        queryKey: ['urun', id],
        queryFn: () => urunlerApi.getById(Number(id)),
        enabled: !!id,
    });

    // Favori IDs sorgusu
    const { data: favoriIds = [] } = useQuery({
        queryKey: ['favori-ids'],
        queryFn: () => favorilerApi.getIds(),
        enabled: isAuthenticated,
    });

    const isFavorite = favoriIds.includes(Number(id));

    const addToCartMutation = useMutation({
        mutationFn: () => sepetApi.add(Number(id), selectedBeden!, quantity),
        onSuccess: () => {
            Alert.alert('Başarılı', 'Ürün sepete eklendi!');
            queryClient.invalidateQueries({ queryKey: ['sepet'] });
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Sepete eklenemedi');
        },
    });

    // Favori toggle mutation
    const favoriToggleMutation = useMutation({
        mutationFn: () => favorilerApi.toggle(Number(id)),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['favori-ids'] });
            queryClient.invalidateQueries({ queryKey: ['favoriler'] });
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Favori işlemi başarısız');
        },
    });

    const handleAddToCart = () => {
        if (!isAuthenticated) {
            Alert.alert('Giriş Gerekli', 'Sepete eklemek için giriş yapmalısınız', [
                { text: 'İptal', style: 'cancel' },
                { text: 'Giriş Yap', onPress: () => router.push('/(auth)/login') },
            ]);
            return;
        }

        if (!selectedBeden) {
            Alert.alert('Uyarı', 'Lütfen beden seçin');
            return;
        }

        addToCartMutation.mutate();
    };

    const handleFavoriToggle = () => {
        if (!isAuthenticated) {
            Alert.alert('Giriş Gerekli', 'Favorilere eklemek için giriş yapmalısınız', [
                { text: 'İptal', style: 'cancel' },
                { text: 'Giriş Yap', onPress: () => router.push('/(auth)/login') },
            ]);
            return;
        }
        favoriToggleMutation.mutate();
    };

    if (isLoading) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <ActivityIndicator size="large" color="#667eea" />
            </View>
        );
    }

    if (!urun) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center">
                <Text className="text-gray-900">Ürün bulunamadı</Text>
            </View>
        );
    }

    const stoklar = urun.stoklar || [];
    const selectedStok = stoklar.find((s: any) => s.bedenId === selectedBeden);
    const maxQuantity = selectedStok?.adet || 1;

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header */}
            <View className="bg-white pt-12 pb-4 px-5 flex-row items-center justify-between">
                <TouchableOpacity
                    onPress={() => router.back()}
                    className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center"
                >
                    <ArrowLeft size={22} color="#4a5568" />
                </TouchableOpacity>
                <Text className="text-gray-900 text-lg font-bold">Ürün Detayı</Text>

                {/* Favori Butonu */}
                <TouchableOpacity
                    onPress={handleFavoriToggle}
                    disabled={favoriToggleMutation.isPending}
                    className="w-10 h-10 rounded-full items-center justify-center"
                    style={{ backgroundColor: isFavorite ? '#fef2f2' : '#f3f4f6' }}
                >
                    <Heart
                        size={22}
                        color={isFavorite ? '#f43f5e' : '#9ca3af'}
                        fill={isFavorite ? '#f43f5e' : 'transparent'}
                    />
                </TouchableOpacity>
            </View>

            <ScrollView contentContainerStyle={{ paddingBottom: 120 }}>
                {/* Product Image */}
                <View className="bg-white mx-5 mt-4 rounded-3xl overflow-hidden shadow-lg" style={{ elevation: 4 }}>
                    <Image
                        source={{ uri: urun.resimUrl || 'https://via.placeholder.com/400' }}
                        className="w-full h-80"
                        resizeMode="cover"
                    />
                </View>

                <View className="px-5 pt-6">
                    {/* Store Badge */}
                    <MotiView from={{ opacity: 0 }} animate={{ opacity: 1 }}>
                        <View className="flex-row items-center gap-2 mb-3">
                            <TouchableOpacity className="flex-row items-center">
                                <LinearGradient
                                    colors={['#667eea', '#764ba2']}
                                    className="flex-row items-center px-3 py-1.5 rounded-full"
                                >
                                    <Store size={14} color="white" />
                                    <Text className="text-white ml-1 text-sm font-medium">{urun.magazaAd}</Text>
                                </LinearGradient>
                            </TouchableOpacity>
                            <TouchableOpacity
                                onPress={() => router.push(`/sohbet?magazaId=${urun.magazaId}`)}
                                className="flex-row items-center bg-gray-100 px-3 py-1.5 rounded-full"
                            >
                                <MessageCircle size={14} color="#667eea" />
                                <Text className="text-primary-500 ml-1 text-sm font-medium">Mesaj</Text>
                            </TouchableOpacity>
                        </View>
                    </MotiView>

                    {/* Title & Price */}
                    <MotiView from={{ opacity: 0, translateY: 10 }} animate={{ opacity: 1, translateY: 0 }}>
                        <Text className="text-gray-900 text-2xl font-extrabold">{urun.ad}</Text>
                        <Text className="text-primary-500 text-3xl font-extrabold mt-2">₺{urun.fiyat}</Text>
                    </MotiView>

                    {/* Description */}
                    <MotiView
                        from={{ opacity: 0, translateY: 10 }}
                        animate={{ opacity: 1, translateY: 0 }}
                        transition={{ delay: 100 }}
                        className="mt-4"
                    >
                        <Text className="text-gray-600 leading-6">{urun.aciklama || 'Ürün açıklaması bulunmuyor.'}</Text>
                    </MotiView>

                    {/* Color */}
                    {urun.renk && (
                        <MotiView
                            from={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            transition={{ delay: 150 }}
                            className="mt-5"
                        >
                            <Text className="text-gray-900 font-bold mb-2">Renk</Text>
                            <View className="bg-white px-4 py-3 rounded-xl self-start shadow-sm" style={{ elevation: 2 }}>
                                <Text className="text-gray-700">{urun.renk}</Text>
                            </View>
                        </MotiView>
                    )}

                    {/* Size Selection */}
                    <MotiView
                        from={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 200 }}
                        className="mt-6"
                    >
                        <Text className="text-gray-900 font-bold mb-3">Beden Seçin</Text>
                        <View className="flex-row flex-wrap gap-3">
                            {stoklar.map((stok: any) => (
                                <TouchableOpacity
                                    key={stok.bedenId}
                                    onPress={() => {
                                        setSelectedBeden(stok.bedenId);
                                        setQuantity(1);
                                    }}
                                    disabled={stok.adet === 0}
                                    className={`min-w-[56px] h-14 rounded-xl items-center justify-center px-3 ${selectedBeden === stok.bedenId
                                        ? ''
                                        : stok.adet > 0
                                            ? 'bg-white border-2 border-gray-200'
                                            : 'bg-gray-100 border-2 border-gray-200'
                                        }`}
                                    style={selectedBeden !== stok.bedenId ? { elevation: 2 } : undefined}
                                >
                                    {selectedBeden === stok.bedenId ? (
                                        <LinearGradient
                                            colors={['#667eea', '#764ba2']}
                                            className="absolute inset-0 rounded-xl"
                                        />
                                    ) : null}
                                    <Text
                                        className={`font-bold ${selectedBeden === stok.bedenId
                                            ? 'text-white'
                                            : stok.adet > 0
                                                ? 'text-gray-900'
                                                : 'text-gray-400'
                                            }`}
                                    >
                                        {stok.bedenAd}
                                    </Text>
                                    {stok.adet === 0 && (
                                        <Text className="text-xs text-red-500">Tükendi</Text>
                                    )}
                                </TouchableOpacity>
                            ))}
                        </View>
                        {selectedStok && (
                            <Text className="text-green-600 text-sm mt-2 flex-row items-center">
                                <Check size={14} color="#38a169" /> Stokta: {selectedStok.adet} adet
                            </Text>
                        )}
                    </MotiView>

                    {/* Quantity */}
                    <MotiView
                        from={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 250 }}
                        className="mt-6"
                    >
                        <Text className="text-gray-900 font-bold mb-3">Adet</Text>
                        <View className="flex-row items-center bg-white rounded-xl self-start shadow-sm" style={{ elevation: 2 }}>
                            <TouchableOpacity
                                onPress={() => setQuantity(Math.max(1, quantity - 1))}
                                className="p-4"
                            >
                                <Minus size={18} color="#4a5568" />
                            </TouchableOpacity>
                            <Text className="text-gray-900 text-lg font-bold w-12 text-center">{quantity}</Text>
                            <TouchableOpacity
                                onPress={() => setQuantity(Math.min(maxQuantity, quantity + 1))}
                                className="p-4"
                            >
                                <Plus size={18} color="#4a5568" />
                            </TouchableOpacity>
                        </View>
                    </MotiView>

                    {/* Category Tags */}
                    <MotiView
                        from={{ opacity: 0 }}
                        animate={{ opacity: 1 }}
                        transition={{ delay: 300 }}
                        className="mt-6 flex-row gap-2"
                    >
                        <View className="px-3 py-1.5 bg-gray-200 rounded-full">
                            <Text className="text-gray-600 text-sm">{urun.kategoriAd}</Text>
                        </View>
                        {urun.altKategoriAd && (
                            <View className="px-3 py-1.5 bg-gray-200 rounded-full">
                                <Text className="text-gray-600 text-sm">{urun.altKategoriAd}</Text>
                            </View>
                        )}
                    </MotiView>
                </View>
            </ScrollView>

            {/* Bottom Action Bar - Hide for Admin */}
            {!isAdmin() && (
                <View className="absolute bottom-0 left-0 right-0 bg-white px-5 py-4 border-t border-gray-200">
                    <View className="flex-row items-center gap-4">
                        <View className="flex-1">
                            <Text className="text-gray-500 text-sm">Toplam</Text>
                            <Text className="text-gray-900 text-2xl font-extrabold">
                                ₺{(urun.fiyat * quantity).toFixed(2)}
                            </Text>
                        </View>
                        <TouchableOpacity
                            onPress={handleAddToCart}
                            disabled={addToCartMutation.isPending || !selectedBeden}
                            className="flex-1"
                        >
                            <LinearGradient
                                colors={selectedBeden ? ['#667eea', '#764ba2'] : ['#a0aec0', '#718096']}
                                className="flex-row items-center justify-center py-4 rounded-xl"
                            >
                                {addToCartMutation.isPending ? (
                                    <ActivityIndicator color="white" />
                                ) : (
                                    <>
                                        <ShoppingCart size={20} color="white" />
                                        <Text className="text-white font-bold ml-2">Sepete Ekle</Text>
                                    </>
                                )}
                            </LinearGradient>
                        </TouchableOpacity>
                    </View>
                </View>
            )}
        </View>
    );
}
