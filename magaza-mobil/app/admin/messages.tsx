import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    StatusBar,
    Modal,
    ScrollView,
    Platform,
    KeyboardAvoidingView,
    TextInput
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    MessageCircle,
    Store,
    User,
    Trash2,
    ChevronRight,
    X,
    Send,
    Pencil,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

interface Sohbet {
    magazaId: number;
    magazaAd: string;
    musteriId: number;
    musteriAd: string;
    musteriKullaniciAdi: string;
    sonMesaj: string;
    sonMesajTarih: string;
    mesajSayisi: number;
}

interface Mesaj {
    id: number;
    icerik: string;
    tarih: string;
    okundu: boolean;
    gonderenMusteri: boolean;
    gonderenAd: string;
}

export default function AdminMessagesScreen() {
    const { isAdmin } = useAuthStore();
    const queryClient = useQueryClient();
    const [selectedSohbet, setSelectedSohbet] = useState<Sohbet | null>(null);
    const [modalVisible, setModalVisible] = useState(false);

    // New state for message operations
    const [messageText, setMessageText] = useState('');
    const [sendAs, setSendAs] = useState<'STORE' | 'CUSTOMER'>('STORE');
    const [editingMessageId, setEditingMessageId] = useState<number | null>(null);

    const sendMessageMutation = useMutation({
        mutationFn: (data: any) => adminApi.sendMessage(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-conversation'] });
            queryClient.invalidateQueries({ queryKey: ['admin-messages'] });
            setMessageText('');
        },
        onError: (error: any) => Alert.alert('Hata', 'Mesaj gönderilemedi'),
    });

    const editMessageMutation = useMutation({
        mutationFn: (data: { id: number, icerik: string }) => adminApi.editMessage(data.id, data.icerik),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-conversation'] });
            setEditingMessageId(null);
            setMessageText('');
        },
        onError: (error: any) => Alert.alert('Hata', 'Mesaj güncellenemedi'),
    });

    const handleSend = () => {
        if (!messageText.trim() || !selectedSohbet) return;

        if (editingMessageId) {
            editMessageMutation.mutate({ id: editingMessageId, icerik: messageText });
        } else {
            sendMessageMutation.mutate({
                magazaId: selectedSohbet.magazaId,
                musteriId: selectedSohbet.musteriId,
                icerik: messageText,
                yon: sendAs === 'STORE' ? 'MAGAZA_TO_MUSTERI' : 'MUSTERI_TO_MAGAZA',
            });
        }
    };

    const startEditing = (msg: Mesaj) => {
        setEditingMessageId(msg.id);
        setMessageText(msg.icerik);
    };

    const cancelEditing = () => {
        setEditingMessageId(null);
        setMessageText('');
    };

    const { data: sohbetler = [], isLoading, refetch, isError, error } = useQuery({
        queryKey: ['admin-messages'],
        queryFn: adminApi.getAllMessages,
        enabled: isAdmin(),
        refetchInterval: 10000, // Liste her 10 saniyede bir güncellenir
    });

    // Debug logging
    useEffect(() => {
        if (isError) {
            console.error('Admin Messages Error:', error);
            const errorMsg = (error as any).response?.data?.error || (error as any).message;
            Alert.alert('Hata', 'Mesajlar yüklenirken bir sorun oluştu: ' + errorMsg);
        }
        if (sohbetler && sohbetler.length > 0) {
            console.log('Loaded conversations:', sohbetler.length);
        } else if (!isLoading && !isError) {
            console.log('No conversations found (empty list)');
        }
    }, [sohbetler, isError, error, isLoading]);

    const { data: conversation, isLoading: convLoading } = useQuery({
        queryKey: ['admin-conversation', selectedSohbet?.magazaId, selectedSohbet?.musteriId],
        queryFn: () => adminApi.getConversation(selectedSohbet!.magazaId, selectedSohbet!.musteriId),
        enabled: !!selectedSohbet,
        refetchInterval: 3000, // Sohbet her 3 saniyede bir güncellenir
    });

    const deleteMutation = useMutation({
        mutationFn: (mesajId: number) => adminApi.deleteMessage(mesajId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-conversation'] });
            queryClient.invalidateQueries({ queryKey: ['admin-messages'] });
            Alert.alert('Başarılı', 'Mesaj silindi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Mesaj silinemedi');
        },
    });

    const handleDeleteMessage = (mesajId: number) => {
        Alert.alert('Mesaj Sil', 'Bu mesajı silmek istediğinize emin misiniz?', [
            { text: 'İptal', style: 'cancel' },
            { text: 'Sil', style: 'destructive', onPress: () => deleteMutation.mutate(mesajId) },
        ]);
    };

    const openConversation = (sohbet: Sohbet) => {
        setSelectedSohbet(sohbet);
        setModalVisible(true);
    };

    if (!isAdmin()) {
        return (
            <View className="flex-1 bg-gray-900 items-center justify-center">
                <Text className="text-white">Admin yetkisi gerekli</Text>
            </View>
        );
    }

    return (
        <View className="flex-1 bg-gray-900">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />
            <StatusBar barStyle="light-content" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5 flex-row items-center">
                <TouchableOpacity
                    onPress={() => router.back()}
                    className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                >
                    <ArrowLeft size={22} color="white" />
                </TouchableOpacity>
                <View className="flex-1">
                    <Text className="text-white text-xl font-bold">💬 Mesaj Yönetimi</Text>
                    <Text className="text-gray-400 text-sm">{sohbetler.length} sohbet</Text>
                </View>
            </View>

            <FlatList
                data={sohbetler}
                keyExtractor={(item) => `${item.magazaId}-${item.musteriId}`}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 50 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
                renderItem={({ item, index }) => (
                    <MotiView
                        from={{ opacity: 0, translateY: 10 }}
                        animate={{ opacity: 1, translateY: 0 }}
                        transition={{ delay: index * 50 }}
                    >
                        <TouchableOpacity
                            onPress={() => openConversation(item)}
                            className="bg-white/5 rounded-2xl p-4 mb-3"
                        >
                            <View className="flex-row items-center">
                                <View className="w-12 h-12 rounded-full bg-purple-500/20 items-center justify-center mr-3">
                                    <MessageCircle size={20} color="#a855f7" />
                                </View>
                                <View className="flex-1">
                                    <View className="flex-row items-center">
                                        <Store size={12} color="#10b981" />
                                        <Text className="text-green-400 text-xs ml-1">{item.magazaAd}</Text>
                                    </View>
                                    <View className="flex-row items-center mt-0.5">
                                        <User size={12} color="#3b82f6" />
                                        <Text className="text-blue-400 text-xs ml-1">
                                            {item.musteriAd} (@{item.musteriKullaniciAdi})
                                        </Text>
                                    </View>
                                    <Text className="text-gray-400 text-sm mt-1" numberOfLines={1}>
                                        {item.sonMesaj}
                                    </Text>
                                </View>
                                <View className="items-end">
                                    <View className="bg-purple-500/30 px-2 py-1 rounded-full">
                                        <Text className="text-purple-300 text-xs">{item.mesajSayisi}</Text>
                                    </View>
                                    <ChevronRight size={18} color="#6b7280" className="mt-2" />
                                </View>
                            </View>
                        </TouchableOpacity>
                    </MotiView>
                )}
                ListEmptyComponent={
                    <View className="items-center py-20">
                        <MessageCircle size={60} color="#6b7280" />
                        <Text className="text-gray-400 mt-4 text-center">
                            {isError ? "Bağlantı Hatası" : "Henüz mesaj bulunmuyor"}
                        </Text>
                        {isError && (
                            <Text className="text-red-500 mt-2 text-xs px-4 text-center">
                                {(error as any)?.message}
                            </Text>
                        )}
                    </View>
                }
            />

            {/* Conversation Modal */}
            <Modal
                visible={modalVisible}
                animationType="slide"
                transparent
                onRequestClose={() => setModalVisible(false)}
            >
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    className="flex-1"
                >
                    <View className="flex-1 bg-black/50 justify-end">
                        <View className="flex-1 mt-10 bg-gray-900 rounded-t-3xl overflow-hidden">
                            {/* Modal Header */}
                            <View className="flex-row items-center justify-between p-4 border-b border-gray-800 bg-gray-900 z-10">
                                <View className="flex-1">
                                    <Text className="text-white font-bold text-lg">{selectedSohbet?.magazaAd}</Text>
                                    <Text className="text-gray-400 text-sm">{selectedSohbet?.musteriAd}</Text>
                                </View>
                                <TouchableOpacity onPress={() => setModalVisible(false)} className="p-2">
                                    <X size={24} color="#9ca3af" />
                                </TouchableOpacity>
                            </View>

                            {/* Messages */}
                            <ScrollView
                                className="flex-1 px-4"
                                contentContainerStyle={{ paddingVertical: 20 }}
                                ref={ref => ref?.scrollToEnd()}
                            >
                                {convLoading ? (
                                    <Text className="text-gray-400 text-center mt-10">Yükleniyor...</Text>
                                ) : (
                                    conversation?.mesajlar?.map((mesaj: Mesaj) => (
                                        <View
                                            key={mesaj.id}
                                            className={`mb-3 w-full flex-row ${mesaj.gonderenMusteri ? 'justify-start' : 'justify-end'}`}
                                        >
                                            <View className={`max-w-[85%] group`}>
                                                <View
                                                    className={`p-3 rounded-2xl ${mesaj.gonderenMusteri
                                                        ? 'bg-gray-800 rounded-bl-none'
                                                        : 'bg-purple-600 rounded-br-none'
                                                        }`}
                                                >
                                                    <Text className="text-white text-base leading-5">{mesaj.icerik}</Text>
                                                    <Text className="text-white/50 text-[10px] mt-1 text-right">
                                                        {mesaj.gonderenAd} • {new Date(mesaj.tarih).toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
                                                    </Text>
                                                </View>

                                                {/* Actions */}
                                                <View className={`flex-row mt-1 gap-3 ${mesaj.gonderenMusteri ? 'justify-start' : 'justify-end'}`}>
                                                    <TouchableOpacity onPress={() => startEditing(mesaj)} className="flex-row items-center">
                                                        <Pencil size={12} color="#3b82f6" />
                                                        <Text className="text-blue-500 text-[10px] ml-1">Düzenle</Text>
                                                    </TouchableOpacity>
                                                    <TouchableOpacity onPress={() => handleDeleteMessage(mesaj.id)} className="flex-row items-center">
                                                        <Trash2 size={12} color="#ef4444" />
                                                        <Text className="text-red-500 text-[10px] ml-1">Sil</Text>
                                                    </TouchableOpacity>
                                                </View>
                                            </View>
                                        </View>
                                    ))
                                )}
                            </ScrollView>

                            {/* Input Area */}
                            <View className="p-4 bg-gray-800 border-t border-gray-700">
                                {editingMessageId ? (
                                    <View className="flex-row justify-between items-center mb-2 bg-blue-500/10 p-2 rounded-lg border border-blue-500/20">
                                        <Text className="text-blue-400 text-xs">Mesaj Düzenleniyor</Text>
                                        <TouchableOpacity onPress={cancelEditing}>
                                            <X size={14} color="#60a5fa" />
                                        </TouchableOpacity>
                                    </View>
                                ) : (
                                    <View className="flex-row mb-3 bg-gray-900 rounded-lg p-1 border border-gray-700">
                                        <TouchableOpacity
                                            onPress={() => setSendAs('STORE')}
                                            className={`flex-1 py-1.5 items-center rounded-md ${sendAs === 'STORE' ? 'bg-purple-600' : ''}`}
                                        >
                                            <Text className={sendAs === 'STORE' ? 'text-white font-bold text-xs' : 'text-gray-400 text-xs'}>
                                                Mağaza Adına
                                            </Text>
                                        </TouchableOpacity>
                                        <TouchableOpacity
                                            onPress={() => setSendAs('CUSTOMER')}
                                            className={`flex-1 py-1.5 items-center rounded-md ${sendAs === 'CUSTOMER' ? 'bg-blue-600' : ''}`}
                                        >
                                            <Text className={sendAs === 'CUSTOMER' ? 'text-white font-bold text-xs' : 'text-gray-400 text-xs'}>
                                                Müşteri Adına
                                            </Text>
                                        </TouchableOpacity>
                                    </View>
                                )}

                                <View className="flex-row items-end gap-2">
                                    <TextInput
                                        value={messageText}
                                        onChangeText={setMessageText}
                                        placeholder={editingMessageId ? "Mesajı düzenle..." : `${sendAs === 'STORE' ? 'Mağaza' : 'Müşteri'} adına mesaj yaz...`}
                                        placeholderTextColor="#9ca3af"
                                        multiline
                                        className="flex-1 bg-gray-900 text-white rounded-xl px-4 py-3 min-h-[48px] max-h-32"
                                        style={{ textAlignVertical: 'center' }}
                                    />
                                    <TouchableOpacity
                                        onPress={handleSend}
                                        disabled={!messageText.trim() || sendMessageMutation.isPending || editMessageMutation.isPending}
                                        className={`w-12 h-12 rounded-full items-center justify-center ${messageText.trim() ? 'bg-purple-600' : 'bg-gray-700'
                                            }`}
                                    >
                                        <Send size={20} color="white" />
                                    </TouchableOpacity>
                                </View>
                            </View>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </View>
    );
}
