import React, { useState, useCallback } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    FlatList,
    Image,
    StatusBar,
    ActivityIndicator,
    Keyboard,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import { ArrowLeft, Search, X, ShoppingBag, Store } from 'lucide-react-native';
import { MotiView } from 'moti';
import { debounce } from 'lodash';

import { urunlerApi } from '../services/api';

export default function SearchScreen() {
    const params = useLocalSearchParams<{ q?: string }>();
    const [searchQuery, setSearchQuery] = useState(params.q || '');
    const [debouncedQuery, setDebouncedQuery] = useState(params.q || '');

    // Debounced search
    const debouncedSearch = useCallback(
        debounce((query: string) => {
            setDebouncedQuery(query);
        }, 500),
        []
    );

    const handleSearchChange = (text: string) => {
        setSearchQuery(text);
        debouncedSearch(text);
    };

    const { data, isLoading, error } = useQuery({
        queryKey: ['search', debouncedQuery],
        queryFn: () => urunlerApi.ara(debouncedQuery),
        enabled: debouncedQuery.length >= 2,
    });

    const sonuclar = data?.sonuclar || [];

    const renderProduct = ({ item, index }: { item: any; index: number }) => (
        <MotiView
            from={{ opacity: 0, translateY: 10 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: index * 50 }}
        >
            <TouchableOpacity
                onPress={() => router.push(`/product/${item.id}`)}
                className="bg-white rounded-2xl mb-3 overflow-hidden flex-row"
                style={{ elevation: 2 }}
            >
                <Image
                    source={{ uri: item.resimUrl || 'https://via.placeholder.com/120' }}
                    className="w-28 h-28"
                    resizeMode="cover"
                />
                <View className="flex-1 p-3 justify-between">
                    <View>
                        <Text className="text-gray-900 font-bold text-base" numberOfLines={2}>
                            {item.ad}
                        </Text>
                        <View className="flex-row items-center mt-1">
                            <Store size={12} color="#6b7280" />
                            <Text className="text-gray-500 text-sm ml-1">{item.magazaAd}</Text>
                        </View>
                    </View>
                    <View className="flex-row justify-between items-center">
                        <Text className="text-primary-600 font-bold text-lg">₺{item.fiyat}</Text>
                        <Text className="text-gray-400 text-xs">{item.kategoriAd}</Text>
                    </View>
                </View>
            </TouchableOpacity>
        </MotiView>
    );

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Header with Search */}
            <View className="bg-white pt-12 pb-4 px-4 border-b border-gray-200">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="#4a5568" />
                    </TouchableOpacity>

                    <View className="flex-1 flex-row items-center bg-gray-100 rounded-2xl px-4 py-3">
                        <Search size={20} color="#9ca3af" />
                        <TextInput
                            value={searchQuery}
                            onChangeText={handleSearchChange}
                            placeholder="Ürün, marka veya kategori ara..."
                            placeholderTextColor="#9ca3af"
                            className="flex-1 ml-3 text-gray-900 text-base"
                            autoFocus
                            returnKeyType="search"
                            onSubmitEditing={() => Keyboard.dismiss()}
                        />
                        {searchQuery.length > 0 && (
                            <TouchableOpacity onPress={() => { setSearchQuery(''); setDebouncedQuery(''); }}>
                                <X size={20} color="#9ca3af" />
                            </TouchableOpacity>
                        )}
                    </View>
                </View>
            </View>

            {/* Results */}
            <View className="flex-1 px-4 pt-4">
                {isLoading && debouncedQuery.length >= 2 ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator size="large" color="#667eea" />
                        <Text className="text-gray-500 mt-4">Aranıyor...</Text>
                    </View>
                ) : debouncedQuery.length < 2 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <Search size={60} color="#d1d5db" />
                        <Text className="text-gray-900 font-bold text-xl mt-4 text-center">
                            Arama yapın
                        </Text>
                        <Text className="text-gray-500 text-center mt-2">
                            En az 2 karakter yazarak ürün, marka veya kategori arayabilirsiniz
                        </Text>
                    </View>
                ) : sonuclar.length === 0 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <ShoppingBag size={60} color="#d1d5db" />
                        <Text className="text-gray-900 font-bold text-xl mt-4 text-center">
                            Sonuç bulunamadı
                        </Text>
                        <Text className="text-gray-500 text-center mt-2">
                            "{debouncedQuery}" için ürün bulunamadı. Farklı bir arama deneyin.
                        </Text>
                    </View>
                ) : (
                    <>
                        <Text className="text-gray-600 mb-3">
                            "{debouncedQuery}" için <Text className="font-bold">{sonuclar.length}</Text> sonuç bulundu
                        </Text>
                        <FlatList
                            data={sonuclar}
                            renderItem={renderProduct}
                            keyExtractor={(item) => item.id.toString()}
                            showsVerticalScrollIndicator={false}
                            contentContainerStyle={{ paddingBottom: 100 }}
                        />
                    </>
                )}
            </View>
        </View>
    );
}
