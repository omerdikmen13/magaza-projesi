import React from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Image,
    Alert,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ShoppingCart,
    Trash2,
    Plus,
    Minus,
    CreditCard,
    Package,
    ArrowRight,
    Wallet,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { sepetApi, odemeApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function SepetScreen() {
    const { isAuthenticated } = useAuthStore();
    const queryClient = useQueryClient();

    const { data: sepet, isLoading } = useQuery({
        queryKey: ['sepet'],
        queryFn: sepetApi.get,
        enabled: isAuthenticated,
    });

    const updateMutation = useMutation({
        mutationFn: ({ sepetItemId, adet }: { sepetItemId: number; adet: number }) =>
            sepetApi.update(sepetItemId, adet),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sepet'] }),
    });

    const removeMutation = useMutation({
        mutationFn: (sepetItemId: number) => sepetApi.remove(sepetItemId),
        onSuccess: () => queryClient.invalidateQueries({ queryKey: ['sepet'] }),
    });

    const checkoutMutation = useMutation({
        mutationFn: (teslimatAdresi: string) => sepetApi.checkout(teslimatAdresi),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sepet'] });
            Alert.alert('Başarılı', 'Siparişiniz alındı!', [
                { text: 'Tamam', onPress: () => router.push('/siparislerim') }
            ]);
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Sipariş verilemedi');
        }
    });

    // Mock ödeme mutation
    const paymentMutation = useMutation({
        mutationFn: () => odemeApi.basla(),
        onSuccess: (data) => {
            if (data.success && data.token) {
                // Ödeme ekranına yönlendir
                router.push({
                    pathname: '/odeme-webview' as any,
                    params: {
                        token: data.token,
                        tutar: data.tutar?.toString() || '0'
                    }
                });
            } else {
                Alert.alert('Hata', data.error || 'Ödeme başlatılamadı');
            }
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Ödeme başlatılamadı');
        }
    });

    // iyzico ile ödeme
    const handlePayment = () => {
        paymentMutation.mutate();
    };

    // Kapıda ödeme
    const handleCOD = () => {
        Alert.alert(
            'Kapıda Ödeme',
            'Siparişinizi kapıda ödeme ile onaylıyor musunuz?',
            [
                { text: 'İptal', style: 'cancel' },
                {
                    text: 'Onayla',
                    onPress: () => {
                        checkoutMutation.mutate('Varsayılan Adres');
                    }
                }
            ]
        );
    };

    // Giriş yapmamışsa
    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <ShoppingCart size={80} color="#a0aec0" />
                <Text className="text-gray-900 text-2xl font-bold mt-6">Sepetiniz</Text>
                <Text className="text-gray-500 text-center mt-2">
                    Sepetinizi görmek için giriş yapın
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

    const sepetItems = sepet?.sepetItems || [];
    const toplamTutar = sepet?.toplam || sepetItems.reduce((t: number, item: any) => t + (item.toplamFiyat || 0), 0);

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header */}
            <View className="bg-white pt-14 pb-4 px-5 border-b border-gray-200">
                <Text className="text-gray-900 text-2xl font-extrabold">🛒 Sepetim</Text>
            </View>

            {sepetItems.length === 0 ? (
                <View className="flex-1 items-center justify-center px-6">
                    <Package size={80} color="#a0aec0" />
                    <Text className="text-gray-900 text-xl font-bold mt-6">Sepetiniz Boş</Text>
                    <Text className="text-gray-500 text-center mt-2">
                        Alışverişe başlamak için mağazaları keşfedin
                    </Text>
                    <TouchableOpacity
                        onPress={() => router.push('/(tabs)/magazalar')}
                        className="mt-6"
                    >
                        <LinearGradient
                            colors={['#667eea', '#764ba2']}
                            className="px-8 py-4 rounded-2xl flex-row items-center"
                        >
                            <ArrowRight size={20} color="white" />
                            <Text className="text-white font-bold text-lg ml-2">Alışverişe Başla</Text>
                        </LinearGradient>
                    </TouchableOpacity>
                </View>
            ) : (
                <>
                    <ScrollView className="flex-1 px-5 py-4">
                        {sepetItems.map((item: any, index: number) => (
                            <MotiView
                                key={item.id}
                                from={{ opacity: 0, translateX: -20 }}
                                animate={{ opacity: 1, translateX: 0 }}
                                transition={{ delay: index * 50 }}
                                className="bg-white rounded-2xl p-4 mb-3 flex-row shadow-sm"
                                style={{ elevation: 2 }}
                            >
                                {/* Ürün Resmi */}
                                <View className="w-20 h-20 bg-gray-200 rounded-xl items-center justify-center mr-4">
                                    {item.urunResim ? (
                                        <Image source={{ uri: item.urunResim }} className="w-full h-full rounded-xl" />
                                    ) : (
                                        <Package size={32} color="#667eea" />
                                    )}
                                </View>

                                {/* Ürün Bilgisi */}
                                <View className="flex-1">
                                    <Text className="text-gray-900 font-bold" numberOfLines={1}>{item.urunAd}</Text>
                                    <Text className="text-gray-500 text-sm">Beden: {item.bedenAd}</Text>
                                    <Text className="text-primary-500 font-bold text-lg mt-1">{item.urunFiyat} ₺</Text>
                                </View>

                                {/* Adet Kontrol */}
                                <View className="items-end">
                                    <TouchableOpacity
                                        onPress={() => removeMutation.mutate(item.id)}
                                        className="p-2"
                                    >
                                        <Trash2 size={18} color="#e53e3e" />
                                    </TouchableOpacity>
                                    <View className="flex-row items-center mt-2 bg-gray-100 rounded-xl">
                                        <TouchableOpacity
                                            onPress={() => item.adet > 1 && updateMutation.mutate({ sepetItemId: item.id, adet: item.adet - 1 })}
                                            className="p-2"
                                        >
                                            <Minus size={16} color="#4a5568" />
                                        </TouchableOpacity>
                                        <Text className="px-3 font-bold text-gray-900">{item.adet}</Text>
                                        <TouchableOpacity
                                            onPress={() => updateMutation.mutate({ sepetItemId: item.id, adet: item.adet + 1 })}
                                            className="p-2"
                                        >
                                            <Plus size={16} color="#4a5568" />
                                        </TouchableOpacity>
                                    </View>
                                </View>
                            </MotiView>
                        ))}
                    </ScrollView>

                    {/* Footer - Toplam ve Ödeme Seçenekleri */}
                    <View className="bg-white px-5 py-4 border-t border-gray-200">
                        <View className="flex-row justify-between items-center mb-4">
                            <Text className="text-gray-500 text-lg">Toplam:</Text>
                            <Text className="text-gray-900 text-2xl font-extrabold">{toplamTutar.toFixed(2)} ₺</Text>
                        </View>

                        {/* iyzico ile Ödeme */}
                        <TouchableOpacity onPress={handlePayment} disabled={paymentMutation.isPending}>
                            <LinearGradient
                                colors={['#667eea', '#764ba2']}
                                className="py-4 rounded-2xl flex-row items-center justify-center mb-3"
                            >
                                {paymentMutation.isPending ? (
                                    <ActivityIndicator color="white" />
                                ) : (
                                    <>
                                        <CreditCard size={20} color="white" />
                                        <Text className="text-white font-bold text-lg ml-2">Ödeme Yap</Text>
                                    </>
                                )}
                            </LinearGradient>
                        </TouchableOpacity>

                        {/* Kapıda Ödeme */}
                        <TouchableOpacity
                            onPress={handleCOD}
                            className="bg-gray-100 py-3 rounded-xl flex-row items-center justify-center"
                            disabled={checkoutMutation.isPending}
                        >
                            {checkoutMutation.isPending ? (
                                <ActivityIndicator color="#666" />
                            ) : (
                                <>
                                    <Wallet size={18} color="#666" />
                                    <Text className="text-gray-600 font-medium ml-2">Kapıda Ödeme</Text>
                                </>
                            )}
                        </TouchableOpacity>
                    </View>
                </>
            )}
        </View>
    );
}
