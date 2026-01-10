import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    TextInput,
    Image,
    Modal,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Store,
    Search,
    CheckCircle,
    XCircle,
    Package,
    User,
    Trash2,
    Plus,
    X,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function AdminStoresScreen() {
    const { isAdmin } = useAuthStore();
    const queryClient = useQueryClient();
    const [searchQuery, setSearchQuery] = useState('');
    const [addModalVisible, setAddModalVisible] = useState(false);
    const [addForm, setAddForm] = useState({ ad: '', aciklama: '', logoUrl: '', sahipId: '' });

    const { data: magazalar = [], isLoading, refetch } = useQuery({
        queryKey: ['admin-stores'],
        queryFn: adminApi.getMagazalar,
        enabled: isAdmin(),
    });

    const toggleStatusMutation = useMutation({
        mutationFn: (id: number) => adminApi.toggleMagazaStatus(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-stores'] });
            Alert.alert('Başarılı', 'Mağaza durumu güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        },
    });

    const deleteStoreMutation = useMutation({
        mutationFn: (id: number) => adminApi.deleteStore(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-stores'] });
            queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] });
            Alert.alert('Başarılı', 'Mağaza silindi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Silme başarısız');
        },
    });

    const createStoreMutation = useMutation({
        mutationFn: (data: any) => adminApi.createStore({
            ...data,
            sahipId: data.sahipId ? Number(data.sahipId) : null
        }),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-stores'] });
            setAddModalVisible(false);
            setAddForm({ ad: '', aciklama: '', logoUrl: '', sahipId: '' });
            Alert.alert('Başarılı', 'Mağaza oluşturuldu');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Oluşturma başarısız');
        },
    });

    const handleCreateStore = () => {
        if (!addForm.ad) {
            Alert.alert('Hata', 'Mağaza adı zorunludur');
            return;
        }
        createStoreMutation.mutate(addForm);
    };

    const handleDeleteStore = (store: any) => {
        Alert.alert(
            '⚠️ Mağaza Sil',
            `"${store.ad}" mağazasını silmek istediğinize emin misiniz? Tüm ürünleri de silinecek!`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteStoreMutation.mutate(store.id) }
            ]
        );
    };

    const filteredStores = magazalar.filter((m: any) =>
        m.ad?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        m.sahipAd?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const renderStore = ({ item, index }: any) => (
        <MotiView
            from={{ opacity: 0, translateY: 20 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: index * 50 }}
            className="mb-3"
        >
            <View className="bg-white/5 rounded-xl overflow-hidden">
                {/* Header with Logo */}
                <View className="flex-row p-4">
                    {item.logoUrl ? (
                        <Image
                            source={{ uri: item.logoUrl }}
                            className="w-14 h-14 rounded-xl"
                        />
                    ) : (
                        <View className="w-14 h-14 rounded-xl bg-primary-500/20 items-center justify-center">
                            <Store size={24} color="#d946ef" />
                        </View>
                    )}

                    <View className="flex-1 ml-3">
                        <View className="flex-row items-center justify-between">
                            <Text className="text-white font-semibold text-lg">{item.ad}</Text>
                            <View className={`flex-row items-center px-2 py-1 rounded-lg ${item.aktif ? 'bg-green-500/20' : 'bg-red-500/20'
                                }`}>
                                {item.aktif ? (
                                    <CheckCircle size={12} color="#10b981" />
                                ) : (
                                    <XCircle size={12} color="#ef4444" />
                                )}
                                <Text className={`ml-1 text-xs ${item.aktif ? 'text-green-400' : 'text-red-400'}`}>
                                    {item.aktif ? 'Aktif' : 'Pasif'}
                                </Text>
                            </View>
                        </View>
                        <Text className="text-gray-400 text-sm" numberOfLines={1}>{item.aciklama || 'Açıklama yok'}</Text>
                        <View className="flex-row items-center mt-1">
                            <User size={12} color="#9ca3af" />
                            <Text className="text-gray-500 text-xs ml-1">{item.sahipAd}</Text>
                        </View>
                    </View>
                </View>

                {/* Actions */}
                <View className="flex-row border-t border-white/10">
                    <TouchableOpacity
                        onPress={() => toggleStatusMutation.mutate(item.id)}
                        className="flex-1 py-3 items-center"
                    >
                        <Text className={item.aktif ? 'text-red-400' : 'text-green-400'}>
                            {item.aktif ? 'Pasif Yap' : 'Aktif Yap'}
                        </Text>
                    </TouchableOpacity>
                    <View className="w-px bg-white/10" />
                    <TouchableOpacity
                        onPress={() => router.push(`/admin/stores/${item.id}/products` as any)}
                        className="flex-1 py-3 items-center flex-row justify-center"
                    >
                        <Package size={16} color="#d946ef" />
                        <Text className="text-primary-400 ml-2">Ürünler</Text>
                    </TouchableOpacity>
                    <View className="w-px bg-white/10" />
                    <TouchableOpacity
                        onPress={() => handleDeleteStore(item)}
                        className="flex-1 py-3 items-center flex-row justify-center"
                    >
                        <Trash2 size={16} color="#ef4444" />
                        <Text className="text-red-400 ml-2">Sil</Text>
                    </TouchableOpacity>
                </View>
            </View>
        </MotiView>
    );

    return (
        <View className="flex-1 bg-dark-300">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5">
                <View className="flex-row items-center mb-4">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <Text className="text-white text-2xl font-bold flex-1">Mağaza Yönetimi</Text>
                    <TouchableOpacity
                        onPress={() => setAddModalVisible(true)}
                        className="w-10 h-10 rounded-full bg-purple-600 items-center justify-center"
                    >
                        <Plus size={24} color="white" />
                    </TouchableOpacity>
                </View>

                {/* Search */}
                <View className="flex-row items-center bg-white/10 rounded-xl px-4 py-3">
                    <Search size={20} color="#9ca3af" />
                    <TextInput
                        className="flex-1 ml-3 text-white"
                        placeholder="Mağaza ara..."
                        placeholderTextColor="#6b7280"
                        value={searchQuery}
                        onChangeText={setSearchQuery}
                    />
                </View>
            </View>

            {/* Stats */}
            <View className="flex-row px-5 mb-4 gap-3">
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Toplam</Text>
                    <Text className="text-white text-xl font-bold">{magazalar.length}</Text>
                </View>
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Aktif</Text>
                    <Text className="text-green-400 text-xl font-bold">
                        {magazalar.filter((m: any) => m.aktif).length}
                    </Text>
                </View>
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Pasif</Text>
                    <Text className="text-red-400 text-xl font-bold">
                        {magazalar.filter((m: any) => !m.aktif).length}
                    </Text>
                </View>
            </View>

            {/* List */}
            <FlatList
                data={filteredStores}
                renderItem={renderStore}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
                ListEmptyComponent={
                    <View className="items-center py-10">
                        <Store size={48} color="#6b7280" />
                        <Text className="text-gray-400 mt-4">Mağaza bulunamadı</Text>
                    </View>
                }
            />
            {/* Add Store Modal */}
            <Modal
                visible={addModalVisible}
                animationType="slide"
                transparent={true}
                onRequestClose={() => setAddModalVisible(false)}
            >
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    className="flex-1 justify-end"
                >
                    <View className="flex-1 justify-end bg-black/60">
                        <View className="bg-gray-900 rounded-t-3xl">
                            <View className="p-5 border-b border-gray-800 flex-row items-center justify-between">
                                <Text className="text-white text-xl font-bold">Yeni Mağaza Ekle</Text>
                                <TouchableOpacity
                                    onPress={() => setAddModalVisible(false)}
                                    className="w-8 h-8 rounded-full bg-gray-800 items-center justify-center"
                                >
                                    <X size={18} color="#9ca3af" />
                                </TouchableOpacity>
                            </View>

                            <ScrollView className="p-5">
                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Mağaza Adı *</Text>
                                    <TextInput
                                        value={addForm.ad}
                                        onChangeText={(text) => setAddForm({ ...addForm, ad: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Açıklama</Text>
                                    <TextInput
                                        value={addForm.aciklama}
                                        onChangeText={(text) => setAddForm({ ...addForm, aciklama: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                        multiline
                                        numberOfLines={3}
                                        style={{ height: 80, textAlignVertical: 'top' }}
                                    />
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Sahip ID (Opsiyonel)</Text>
                                    <TextInput
                                        value={addForm.sahipId}
                                        onChangeText={(text) => setAddForm({ ...addForm, sahipId: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                        keyboardType="numeric"
                                    />
                                </View>

                                <View className="mb-6">
                                    <Text className="text-gray-500 mb-2 text-sm">Logo URL (Opsiyonel)</Text>
                                    <TextInput
                                        value={addForm.logoUrl}
                                        onChangeText={(text) => setAddForm({ ...addForm, logoUrl: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <TouchableOpacity
                                    onPress={handleCreateStore}
                                    className="w-full py-4 rounded-xl bg-purple-600 items-center mb-8"
                                >
                                    <Text className="text-white font-bold text-lg">Mağaza Oluştur</Text>
                                </TouchableOpacity>
                            </ScrollView>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </View>
    );
}
