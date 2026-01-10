import React, { useState, useEffect } from 'react';
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
    Store,
    Eye,
    User,
    Shirt,
    Baby,
    Grid,
    ArrowRight,
    Package,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { kategorilerApi, magazalarApi, urunlerApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function MagazalarScreen() {
    const { user, isAdmin } = useAuthStore();
    const params = useLocalSearchParams<{ kategoriId?: string }>();
    const [selectedKategori, setSelectedKategori] = useState<number | null>(
        params.kategoriId ? Number(params.kategoriId) : null
    );

    useEffect(() => {
        if (params.kategoriId) {
            setSelectedKategori(Number(params.kategoriId));
        }
    }, [params.kategoriId]);

    const { data: kategoriler = [] } = useQuery({
        queryKey: ['kategoriler'],
        queryFn: kategorilerApi.getAll,
    });

    const { data: magazalar = [], isLoading: magazaLoading, refetch } = useQuery({
        queryKey: ['magazalar'],
        queryFn: magazalarApi.getAll,
    });

    const { data: allUrunler = [], isLoading: urunLoading } = useQuery({
        queryKey: ['urunler'],
        queryFn: urunlerApi.getAll,
    });

    const selectedKategoriAd = kategoriler.find((k: any) => k.id === selectedKategori)?.ad;
    const urunler = selectedKategori
        ? allUrunler.filter((u: any) => u.kategoriId === selectedKategori || u.kategoriAd === selectedKategoriAd)
        : [];

    const isLoading = magazaLoading || urunLoading;

    const categoryIcons: any = {
        1: User,
        2: Shirt,
        3: Baby,
    };

    const handleStorePress = (magazaId: number) => {
        if (isAdmin()) {
            router.push(`/admin/stores/${magazaId}/products` as any);
        } else {
            router.push(`/magaza/${magazaId}` as any);
        }
    };

    return (
        <View className={`flex-1 ${isAdmin() ? 'bg-gray-900' : 'bg-gray-100'}`}>
            <StatusBar barStyle={isAdmin() ? 'light-content' : 'dark-content'} />

            {isAdmin() && (
                <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />
            )}

            <ScrollView
                contentContainerStyle={{ paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor={isAdmin() ? "#d946ef" : "#667eea"} />
                }
            >
                {/* FILTER BUTTONS */}
                <View className="px-5 pt-14 pb-4">
                    <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                        <View className="flex-row gap-3">
                            <TouchableOpacity
                                onPress={() => setSelectedKategori(null)}
                                style={selectedKategori === null ? (isAdmin() ? undefined : { elevation: 2 }) : undefined}
                            >
                                {selectedKategori === null ? (
                                    <LinearGradient
                                        colors={isAdmin() ? ['#d946ef', '#8b5cf6'] : ['#667eea', '#764ba2']}
                                        className="px-6 py-3 rounded-full flex-row items-center"
                                    >
                                        <Grid size={16} color="white" />
                                        <Text className="ml-2 font-semibold text-white">Tümü</Text>
                                    </LinearGradient>
                                ) : (
                                    <View className={`px-6 py-3 rounded-full border-2 flex-row items-center ${isAdmin() ? 'bg-white/10 border-white/10' : 'bg-white border-gray-300'}`}>
                                        <Grid size={16} color={isAdmin() ? "#9ca3af" : "#4a5568"} />
                                        <Text className={`ml-2 font-semibold ${isAdmin() ? 'text-gray-300' : 'text-gray-700'}`}>Tümü</Text>
                                    </View>
                                )}
                            </TouchableOpacity>

                            {kategoriler.map((kategori: any) => {
                                const Icon = categoryIcons[kategori.id] || Package;
                                const isActive = selectedKategori === kategori.id;

                                return (
                                    <TouchableOpacity
                                        key={kategori.id}
                                        onPress={() => setSelectedKategori(kategori.id)}
                                        style={!isActive && !isAdmin() ? { elevation: 2 } : undefined}
                                    >
                                        {isActive ? (
                                            <LinearGradient
                                                colors={isAdmin() ? ['#d946ef', '#8b5cf6'] : ['#667eea', '#764ba2']}
                                                className="px-6 py-3 rounded-full flex-row items-center"
                                            >
                                                <Icon size={16} color="white" />
                                                <Text className="ml-2 font-semibold text-white">{kategori.ad}</Text>
                                            </LinearGradient>
                                        ) : (
                                            <View className={`px-6 py-3 rounded-full border-2 flex-row items-center ${isAdmin() ? 'bg-white/10 border-white/10' : 'bg-white border-gray-300'}`}>
                                                <Icon size={16} color={isAdmin() ? "#9ca3af" : "#4a5568"} />
                                                <Text className={`ml-2 font-semibold ${isAdmin() ? 'text-gray-300' : 'text-gray-700'}`}>{kategori.ad}</Text>
                                            </View>
                                        )}
                                    </TouchableOpacity>
                                );
                            })}
                        </View>
                    </ScrollView>
                </View>

                {/* PAGE HEADER */}
                {isAdmin() ? (
                    <View className="mx-5 p-6 rounded-3xl items-center mb-6 bg-white/5 border border-white/10">
                        <Text className="text-white text-2xl font-extrabold text-center">
                            Mağaza Yönetimi
                        </Text>
                        <Text className="text-gray-400 text-center mt-1">
                            Mağazaları incele ve yönet
                        </Text>
                    </View>
                ) : (
                    <LinearGradient
                        colors={['rgba(102,126,234,0.1)', 'rgba(118,75,162,0.1)']}
                        className="mx-5 p-6 rounded-3xl items-center mb-6"
                    >
                        <Text className="text-dark-900 text-2xl font-extrabold text-center">
                            {selectedKategori
                                ? `${kategoriler.find((k: any) => k.id === selectedKategori)?.ad || ''} Ürünleri`
                                : '🛍️ Mağazalar'}
                        </Text>
                        <Text className="text-gray-600 text-center mt-1">
                            {selectedKategori
                                ? 'Tüm mağazalardan ürünler'
                                : 'En sevdiğiniz markalardan alışveriş yapın'}
                        </Text>
                    </LinearGradient>
                )}

                {/* CONTENT */}
                <View className="px-5">
                    {selectedKategori !== null ? (
                        /* PRODUCT LIST */
                        urunler.length > 0 ? (
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
                                            onPress={() => isAdmin()
                                                ? router.push(`/admin/stores/${urun.magazaId}/products` as any)
                                                : router.push(`/product/${urun.id}`)
                                            }
                                            className={`rounded-2xl overflow-hidden shadow-lg ${isAdmin() ? 'bg-white/5 border border-white/10' : 'bg-white'}`}
                                            style={isAdmin() ? undefined : { elevation: 4 }}
                                        >
                                            <View className={`h-40 items-center justify-center ${isAdmin() ? 'bg-white/5' : 'bg-gray-200'}`}>
                                                {urun.resimUrl ? (
                                                    <Image source={{ uri: urun.resimUrl }} className="w-full h-full" resizeMode="cover" />
                                                ) : (
                                                    <Shirt size={40} color={isAdmin() ? "#6b7280" : "#667eea"} style={{ opacity: 0.7 }} />
                                                )}
                                            </View>
                                            <View className="p-4">
                                                <View className="self-start mb-2">
                                                    <View className={`px-2 py-1 rounded-md ${isAdmin() ? 'bg-purple-500/20' : ''}`}>
                                                        {isAdmin() ? (
                                                            <Text className="text-purple-300 text-xs font-medium">{urun.magazaAd}</Text>
                                                        ) : (
                                                            <LinearGradient colors={['#667eea', '#764ba2']} className="absolute inset-0 rounded-md" />
                                                        )}
                                                        {!isAdmin() && <Text className="text-white text-xs font-medium">{urun.magazaAd}</Text>}
                                                    </View>
                                                </View>
                                                <Text className={`font-bold text-base ${isAdmin() ? 'text-white' : 'text-gray-900'}`} numberOfLines={1}>{urun.ad}</Text>
                                                <Text className="text-gray-500 text-sm mb-3" numberOfLines={2}>{urun.aciklama}</Text>

                                                {isAdmin() ? (
                                                    <View className="pt-3 border-t border-white/10 flex-row items-center justify-center">
                                                        <Text className="text-purple-400 font-bold">Yönet</Text>
                                                    </View>
                                                ) : (
                                                    <View className="flex-row items-center justify-between pt-3 border-t border-gray-200">
                                                        <Text className="text-primary-500 font-extrabold text-lg">{urun.fiyat} ₺</Text>
                                                        <View className="bg-primary-500 px-3 py-1.5 rounded-lg flex-row items-center">
                                                            <Eye size={14} color="white" />
                                                            <Text className="text-white text-xs font-medium ml-1">İncele</Text>
                                                        </View>
                                                    </View>
                                                )}
                                            </View>
                                        </TouchableOpacity>
                                    </MotiView>
                                ))}
                            </View>
                        ) : (
                            <View className="items-center py-16">
                                <Package size={64} color="#6b7280" />
                                <Text className={`font-bold text-xl mt-4 ${isAdmin() ? 'text-white' : 'text-gray-900'}`}>Bu kategoride ürün yok</Text>
                            </View>
                        )
                    ) : (
                        /* STORE LIST */
                        magazalar.length > 0 ? (
                            <View className="flex-row flex-wrap -mx-1.5">
                                {magazalar.map((magaza: any, index: number) => (
                                    <MotiView
                                        key={magaza.id}
                                        from={{ opacity: 0, scale: 0.95 }}
                                        animate={{ opacity: 1, scale: 1 }}
                                        transition={{ delay: index * 100 }}
                                        className="w-1/2 px-1.5 mb-3"
                                    >
                                        <TouchableOpacity
                                            onPress={() => handleStorePress(magaza.id)}
                                            className={`rounded-3xl p-5 items-center shadow-lg ${isAdmin() ? 'bg-white/5 border border-white/10' : 'bg-white'}`}
                                            style={isAdmin() ? undefined : { elevation: 4 }}
                                        >
                                            {magaza.logoUrl ? (
                                                <Image source={{ uri: magaza.logoUrl }} className="w-20 h-20 rounded-full mb-4" resizeMode="contain" />
                                            ) : (
                                                <View className={`w-20 h-20 rounded-full items-center justify-center mb-4 ${isAdmin() ? 'bg-purple-500/20' : ''}`}>
                                                    {!isAdmin() && <LinearGradient colors={['#667eea', '#764ba2']} className="absolute inset-0 rounded-full" />}
                                                    <Text className="text-white text-2xl font-extrabold">{magaza.ad?.charAt(0)}</Text>
                                                </View>
                                            )}
                                            <Text className={`font-bold text-lg text-center ${isAdmin() ? 'text-white' : 'text-gray-900'}`}>{magaza.ad}</Text>
                                            <Text className="text-gray-500 text-sm text-center mt-1 mb-4" numberOfLines={2}>
                                                {magaza.aciklama || 'Moda mağazası'}
                                            </Text>

                                            {isAdmin() ? (
                                                <View className="w-full py-3 rounded-xl flex-row items-center justify-center bg-purple-500/20">
                                                    <Store size={16} color="#d946ef" />
                                                    <Text className="text-purple-400 font-semibold ml-2">Yönet</Text>
                                                </View>
                                            ) : (
                                                <LinearGradient
                                                    colors={['#667eea', '#764ba2']}
                                                    className="w-full py-3 rounded-xl flex-row items-center justify-center"
                                                >
                                                    <ArrowRight size={16} color="white" />
                                                    <Text className="text-white font-semibold ml-2">Ürünleri Gör</Text>
                                                </LinearGradient>
                                            )}
                                        </TouchableOpacity>
                                    </MotiView>
                                ))}
                            </View>
                        ) : (
                            <View className="items-center py-16">
                                <Store size={64} color="#6b7280" />
                                <Text className={`font-bold text-xl mt-4 ${isAdmin() ? 'text-white' : 'text-gray-900'}`}>Mağaza bulunamadı</Text>
                            </View>
                        )
                    )}
                </View>
            </ScrollView>
        </View>
    );
}
