import React, { useState, useRef, useEffect } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    TextInput,
    KeyboardAvoidingView,
    Platform,
    StatusBar,
    ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Send, MessageCircle, Store } from 'lucide-react-native';
import { MotiView } from 'moti';

import { mesajlarApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';

export default function SohbetScreen() {
    const params = useLocalSearchParams<{ magazaId?: string }>();
    const magazaId = params.magazaId ? Number(params.magazaId) : null;
    const { isAuthenticated } = useAuthStore();
    const queryClient = useQueryClient();
    const [message, setMessage] = useState('');
    const flatListRef = useRef<FlatList>(null);

    // Sohbet listesi (magazaId yoksa)
    const { data: sohbetler = [], isLoading: listLoading } = useQuery({
        queryKey: ['musteri-sohbetler'],
        queryFn: mesajlarApi.getMusteriSohbetler,
        enabled: isAuthenticated && !magazaId,
    });

    // Belirli mağaza ile sohbet (magazaId varsa)
    const { data: sohbet, isLoading: chatLoading } = useQuery({
        queryKey: ['musteri-sohbet', magazaId],
        queryFn: () => mesajlarApi.getMusteriSohbet(magazaId!),
        enabled: isAuthenticated && !!magazaId,
        refetchInterval: 5000, // 5 saniyede bir yenile
    });

    const sendMutation = useMutation({
        mutationFn: (icerik: string) => mesajlarApi.musteriMesajGonder(magazaId!, icerik),
        onSuccess: () => {
            setMessage('');
            queryClient.invalidateQueries({ queryKey: ['musteri-sohbet', magazaId] });
        },
    });

    const handleSend = () => {
        if (message.trim() && magazaId) {
            sendMutation.mutate(message.trim());
        }
    };

    useEffect(() => {
        if (sohbet?.mesajlar?.length && flatListRef.current) {
            setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }
    }, [sohbet?.mesajlar?.length]);

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <MessageCircle size={80} color="#a0aec0" />
                <Text className="text-gray-900 text-2xl font-bold mt-6">Mesajlarım</Text>
                <Text className="text-gray-500 text-center mt-2">
                    Mesajlarınızı görmek için giriş yapın
                </Text>
                <TouchableOpacity onPress={() => router.push('/(auth)/login')} className="mt-6">
                    <LinearGradient colors={['#667eea', '#764ba2']} className="px-8 py-4 rounded-2xl">
                        <Text className="text-white font-bold text-lg">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
            </View>
        );
    }

    // Sohbet listesi görünümü
    if (!magazaId) {
        return (
            <View className="flex-1 bg-gray-100">
                <StatusBar barStyle="dark-content" />

                <View className="bg-white pt-12 pb-4 px-5">
                    <View className="flex-row items-center">
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                        >
                            <ArrowLeft size={22} color="#4a5568" />
                        </TouchableOpacity>
                        <Text className="text-gray-900 text-xl font-bold">💬 Mesajlarım</Text>
                    </View>
                </View>

                {listLoading ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator size="large" color="#667eea" />
                    </View>
                ) : sohbetler.length === 0 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <MessageCircle size={60} color="#a0aec0" />
                        <Text className="text-gray-900 font-bold text-xl mt-4">Henüz mesajınız yok</Text>
                        <Text className="text-gray-500 text-center mt-2">
                            Mağazalarla iletişime geçmek için ürün sayfasından mesaj gönderebilirsiniz
                        </Text>
                    </View>
                ) : (
                    <FlatList
                        data={sohbetler}
                        keyExtractor={(item) => item.magazaId.toString()}
                        contentContainerStyle={{ paddingHorizontal: 20, paddingTop: 16 }}
                        renderItem={({ item, index }) => (
                            <MotiView
                                from={{ opacity: 0, translateY: 10 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: index * 50 }}
                            >
                                <TouchableOpacity
                                    onPress={() => router.push(`/sohbet?magazaId=${item.magazaId}`)}
                                    className="bg-white rounded-2xl p-4 mb-3 flex-row items-center"
                                    style={{ elevation: 2 }}
                                >
                                    <LinearGradient
                                        colors={['#667eea', '#764ba2']}
                                        className="w-14 h-14 rounded-full items-center justify-center mr-4"
                                    >
                                        <Store size={24} color="white" />
                                    </LinearGradient>
                                    <View className="flex-1">
                                        <Text className="text-gray-900 font-bold text-base">{item.magazaAd}</Text>
                                        <Text className="text-gray-500 text-sm mt-1" numberOfLines={1}>
                                            {item.sonMesaj}
                                        </Text>
                                    </View>
                                    {!item.okundu && (
                                        <View className="w-3 h-3 rounded-full bg-primary-500" />
                                    )}
                                </TouchableOpacity>
                            </MotiView>
                        )}
                    />
                )}
            </View>
        );
    }

    // Chat görünümü
    return (
        <KeyboardAvoidingView
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
            className="flex-1 bg-gray-100"
        >
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
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="w-10 h-10 rounded-full items-center justify-center mr-3"
                    >
                        <Store size={18} color="white" />
                    </LinearGradient>
                    <Text className="text-gray-900 text-lg font-bold">{sohbet?.magazaAd || 'Mağaza'}</Text>
                </View>
            </View>

            {/* Messages */}
            {chatLoading ? (
                <View className="flex-1 items-center justify-center">
                    <ActivityIndicator size="large" color="#667eea" />
                </View>
            ) : (
                <FlatList
                    ref={flatListRef}
                    data={sohbet?.mesajlar || []}
                    keyExtractor={(item) => item.id.toString()}
                    contentContainerStyle={{ padding: 16, paddingBottom: 100 }}
                    onContentSizeChange={() => flatListRef.current?.scrollToEnd({ animated: true })}
                    renderItem={({ item }) => (
                        <View
                            className={`max-w-[80%] mb-3 ${item.gonderenMusteri ? 'self-end' : 'self-start'}`}
                        >
                            <View
                                className={`px-4 py-3 rounded-2xl ${item.gonderenMusteri
                                        ? 'bg-primary-500 rounded-br-sm'
                                        : 'bg-white rounded-bl-sm'
                                    }`}
                                style={{ elevation: item.gonderenMusteri ? 0 : 1 }}
                            >
                                <Text className={item.gonderenMusteri ? 'text-white' : 'text-gray-900'}>
                                    {item.icerik}
                                </Text>
                            </View>
                            <Text className={`text-xs mt-1 text-gray-400 ${item.gonderenMusteri ? 'text-right' : ''}`}>
                                {new Date(item.tarih).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                            </Text>
                        </View>
                    )}
                    ListEmptyComponent={
                        <View className="items-center py-20">
                            <MessageCircle size={50} color="#a0aec0" />
                            <Text className="text-gray-500 mt-4">Henüz mesaj yok. İlk mesajı siz gönderin!</Text>
                        </View>
                    }
                />
            )}

            {/* Input */}
            <View className="absolute bottom-0 left-0 right-0 bg-white px-4 py-3 border-t border-gray-200">
                <View className="flex-row items-center">
                    <TextInput
                        value={message}
                        onChangeText={setMessage}
                        placeholder="Mesajınızı yazın..."
                        className="flex-1 bg-gray-100 rounded-full px-5 py-3 mr-3 text-gray-900"
                    />
                    <TouchableOpacity
                        onPress={handleSend}
                        disabled={!message.trim() || sendMutation.isPending}
                    >
                        <LinearGradient
                            colors={message.trim() ? ['#667eea', '#764ba2'] : ['#a0aec0', '#a0aec0']}
                            className="w-12 h-12 rounded-full items-center justify-center"
                        >
                            <Send size={20} color="white" />
                        </LinearGradient>
                    </TouchableOpacity>
                </View>
            </View>
        </KeyboardAvoidingView>
    );
}
