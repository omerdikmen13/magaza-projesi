import React from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Image,
    RefreshControl,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
    ArrowLeft,
    Shirt,
    Eye,
    Package,
    MessageCircle,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazalarApi, urunlerApi } from '../../services/api';

export default function MagazaDetayScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const magazaId = Number(id);

    const { data: magaza, isLoading: magazaLoading } = useQuery({
        queryKey: ['magaza', magazaId],
        queryFn: () => magazalarApi.getById(magazaId),
        enabled: !!magazaId,
    });

    const { data: urunler = [], isLoading: urunLoading, refetch } = useQuery({
        queryKey: ['magaza-urunler', magazaId],
        queryFn: () => urunlerApi.getByMagaza(magazaId),
        enabled: !!magazaId,
    });

    const isLoading = magazaLoading || urunLoading;

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
                        <Text className="text-gray-900 text-xl font-bold">{magaza?.ad || 'Mağaza'}</Text>
                        <Text className="text-gray-500 text-sm">{urunler.length} ürün</Text>
                    </View>
                </View>
            </View>

            <ScrollView
                contentContainerStyle={{ paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
                }
            >
                {/* Mağaza Bilgisi */}
                {magaza && (
                    <LinearGradient
                        colors={['rgba(102,126,234,0.1)', 'rgba(118,75,162,0.1)']}
                        className="mx-5 mt-4 p-6 rounded-3xl items-center"
                    >
                        {magaza.logoUrl ? (
                            <Image
                                source={{ uri: magaza.logoUrl }}
                                className="w-20 h-20 rounded-full mb-3"
                                resizeMode="contain"
                            />
                        ) : (
                            <LinearGradient
                                colors={['#667eea', '#764ba2']}
                                className="w-20 h-20 rounded-full items-center justify-center mb-3"
                            >
                                <Text className="text-white text-2xl font-bold">{magaza.ad?.charAt(0)}</Text>
                            </LinearGradient>
                        )}
                        <Text className="text-gray-900 text-xl font-bold">{magaza.ad}</Text>
                        <Text className="text-gray-500 text-center mt-1">{magaza.aciklama}</Text>
                    </LinearGradient>
                )}

                {/* Ürünler */}
                <View className="px-5 pt-6">
                    {urunler.length > 0 ? (
                        <View className="flex-row flex-wrap -mx-1.5">
                            {urunler.map((urun: any, index: number) => (
                                <MotiView
                                    key={urun.id}
                                    from={{ opacity: 0, translateY: 20 }}
                                    animate={{ opacity: 1, translateY: 0 }}
                                    transition={{ delay: index * 50 }}
                                    className="w-1/2 px-1.5 mb-3"
                                >
                                    <TouchableOpacity
                                        onPress={() => router.push(`/product/${urun.id}`)}
                                        className="bg-white rounded-2xl overflow-hidden shadow-lg"
                                        style={{ elevation: 4 }}
                                    >
                                        <View className="h-40 bg-gray-200 items-center justify-center">
                                            {urun.resimUrl ? (
                                                <Image
                                                    source={{ uri: urun.resimUrl }}
                                                    className="w-full h-full"
                                                    resizeMode="cover"
                                                />
                                            ) : (
                                                <Shirt size={40} color="#667eea" style={{ opacity: 0.7 }} />
                                            )}
                                        </View>

                                        <View className="p-4">
                                            <Text className="text-gray-900 font-bold text-base" numberOfLines={1}>{urun.ad}</Text>
                                            <Text className="text-gray-500 text-sm mb-3" numberOfLines={2}>{urun.aciklama}</Text>

                                            <View className="flex-row items-center justify-between pt-3 border-t border-gray-200">
                                                <Text className="text-primary-500 font-extrabold text-lg">{urun.fiyat} ₺</Text>
                                                <View className="bg-primary-500 px-3 py-1.5 rounded-lg flex-row items-center">
                                                    <Eye size={14} color="white" />
                                                    <Text className="text-white text-xs font-medium ml-1">İncele</Text>
                                                </View>
                                            </View>
                                        </View>
                                    </TouchableOpacity>
                                </MotiView>
                            ))}
                        </View>
                    ) : (
                        <View className="items-center py-16">
                            <Package size={64} color="#a0aec0" />
                            <Text className="text-gray-900 font-bold text-xl mt-4">Henüz ürün yok</Text>
                            <Text className="text-gray-500 mt-2">Bu mağazada henüz ürün bulunmuyor</Text>
                        </View>
                    )}
                </View>
            </ScrollView>

            {/* Floating Message Button */}
            <TouchableOpacity
                onPress={() => router.push(`/sohbet?magazaId=${magazaId}`)}
                className="absolute bottom-6 right-6"
                style={{ elevation: 8 }}
            >
                <LinearGradient
                    colors={['#667eea', '#764ba2']}
                    className="w-16 h-16 rounded-full items-center justify-center"
                    style={{ shadowColor: '#667eea', shadowOffset: { width: 0, height: 4 }, shadowOpacity: 0.4, shadowRadius: 8 }}
                >
                    <MessageCircle size={26} color="white" />
                </LinearGradient>
            </TouchableOpacity>
        </View>
    );
}
