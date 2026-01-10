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
import { ArrowLeft, Send, MessageCircle, User } from 'lucide-react-native';
import { MotiView } from 'moti';

import { mesajlarApi } from '../../services/api';

export default function SahipMesajlarScreen() {
    const params = useLocalSearchParams<{ magazaId?: string; musteriId?: string }>();
    const magazaId = params.magazaId ? Number(params.magazaId) : 1;
    const musteriId = params.musteriId ? Number(params.musteriId) : null;
    const queryClient = useQueryClient();
    const [message, setMessage] = useState('');
    const flatListRef = useRef<FlatList>(null);

    // Müşteri listesi (musteriId yoksa)
    const { data: musteriler = [], isLoading: listLoading } = useQuery({
        queryKey: ['sahip-musteriler', magazaId],
        queryFn: () => mesajlarApi.getSahipMusteriler(magazaId),
        enabled: !musteriId,
    });

    // Belirli müşteri ile sohbet (musteriId varsa)
    const { data: sohbet, isLoading: chatLoading } = useQuery({
        queryKey: ['sahip-sohbet', magazaId, musteriId],
        queryFn: () => mesajlarApi.getSahipSohbet(magazaId, musteriId!),
        enabled: !!musteriId,
        refetchInterval: 5000,
    });

    const sendMutation = useMutation({
        mutationFn: (icerik: string) => mesajlarApi.sahipMesajGonder(magazaId, musteriId!, icerik),
        onSuccess: () => {
            setMessage('');
            queryClient.invalidateQueries({ queryKey: ['sahip-sohbet', magazaId, musteriId] });
        },
    });

    const handleSend = () => {
        if (message.trim() && musteriId) {
            sendMutation.mutate(message.trim());
        }
    };

    useEffect(() => {
        if (sohbet?.mesajlar?.length && flatListRef.current) {
            setTimeout(() => flatListRef.current?.scrollToEnd({ animated: true }), 100);
        }
    }, [sohbet?.mesajlar?.length]);

    // Müşteri listesi görünümü
    if (!musteriId) {
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
                        <Text className="text-gray-900 text-xl font-bold">💬 Müşteri Mesajları</Text>
                    </View>
                </View>

                {listLoading ? (
                    <View className="flex-1 items-center justify-center">
                        <ActivityIndicator size="large" color="#667eea" />
                    </View>
                ) : musteriler.length === 0 ? (
                    <View className="flex-1 items-center justify-center px-6">
                        <MessageCircle size={60} color="#a0aec0" />
                        <Text className="text-gray-900 font-bold text-xl mt-4">Henüz mesajınız yok</Text>
                        <Text className="text-gray-500 text-center mt-2">
                            Müşterileriniz sizinle iletişime geçtiğinde burada görünecek
                        </Text>
                    </View>
                ) : (
                    <FlatList
                        data={musteriler}
                        keyExtractor={(item) => item.id.toString()}
                        contentContainerStyle={{ paddingHorizontal: 20, paddingTop: 16 }}
                        renderItem={({ item, index }) => (
                            <MotiView
                                from={{ opacity: 0, translateY: 10 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: index * 50 }}
                            >
                                <TouchableOpacity
                                    onPress={() => router.push(`/sahip/mesajlar?magazaId=${magazaId}&musteriId=${item.id}`)}
                                    className="bg-white rounded-2xl p-4 mb-3 flex-row items-center"
                                    style={{ elevation: 2 }}
                                >
                                    <LinearGradient
                                        colors={['#667eea', '#764ba2']}
                                        className="w-14 h-14 rounded-full items-center justify-center mr-4"
                                    >
                                        <Text className="text-white text-xl font-bold">
                                            {item.kullaniciAdi?.charAt(0).toUpperCase() || 'M'}
                                        </Text>
                                    </LinearGradient>
                                    <View className="flex-1">
                                        <Text className="text-gray-900 font-bold text-base">{item.kullaniciAdi}</Text>
                                        <Text className="text-gray-500 text-sm mt-1">
                                            {item.ad} {item.soyad}
                                        </Text>
                                    </View>
                                    <MessageCircle size={20} color="#667eea" />
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
                        onPress={() => router.push(`/sahip/mesajlar?magazaId=${magazaId}`)}
                        className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="#4a5568" />
                    </TouchableOpacity>
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="w-10 h-10 rounded-full items-center justify-center mr-3"
                    >
                        <User size={18} color="white" />
                    </LinearGradient>
                    <View>
                        <Text className="text-gray-900 text-lg font-bold">
                            {sohbet?.musteriKullaniciAdi || 'Müşteri'}
                        </Text>
                        <Text className="text-gray-500 text-sm">{sohbet?.musteriAd}</Text>
                    </View>
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
                            className={`max-w-[80%] mb-3 ${!item.gonderenMusteri ? 'self-end' : 'self-start'}`}
                        >
                            <View
                                className={`px-4 py-3 rounded-2xl ${!item.gonderenMusteri
                                        ? 'bg-primary-500 rounded-br-sm'
                                        : 'bg-white rounded-bl-sm'
                                    }`}
                                style={{ elevation: !item.gonderenMusteri ? 0 : 1 }}
                            >
                                <Text className={!item.gonderenMusteri ? 'text-white' : 'text-gray-900'}>
                                    {item.icerik}
                                </Text>
                            </View>
                            <Text className={`text-xs mt-1 text-gray-400 ${!item.gonderenMusteri ? 'text-right' : ''}`}>
                                {new Date(item.tarih).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                            </Text>
                        </View>
                    )}
                    ListEmptyComponent={
                        <View className="items-center py-20">
                            <MessageCircle size={50} color="#a0aec0" />
                            <Text className="text-gray-500 mt-4">Henüz mesaj yok</Text>
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
