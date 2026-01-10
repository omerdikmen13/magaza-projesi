import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    Image,
    Modal,
    TextInput,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Package,
    Eye,
    EyeOff,
    Trash2,
    Pencil,
    X,
    Save,
    Plus,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../../../services/api';
import { useAuthStore } from '../../../../stores/authStore';

export default function AdminStoreProductsScreen() {
    const { id } = useLocalSearchParams<{ id: string }>();
    const storeId = parseInt(id);
    const { isAdmin } = useAuthStore();
    const queryClient = useQueryClient();

    // Edit state
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [editingProduct, setEditingProduct] = useState<any>(null);
    const [editForm, setEditForm] = useState({
        ad: '',
        fiyat: '',
        aciklama: '',
    });

    // Add state
    const [addModalVisible, setAddModalVisible] = useState(false);
    const [addForm, setAddForm] = useState({
        ad: '',
        fiyat: '',
        aciklama: '',
        renk: '',
        resimUrl: '',
    });

    const { data, isLoading, refetch } = useQuery({
        queryKey: ['admin-store-products', storeId],
        queryFn: () => adminApi.getStoreProducts(storeId),
        enabled: isAdmin() && !!storeId,
    });

    const toggleStatusMutation = useMutation({
        mutationFn: (urunId: number) => adminApi.toggleProductStatus(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-store-products'] });
            Alert.alert('Başarılı', 'Ürün durumu güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        },
    });

    const deleteMutation = useMutation({
        mutationFn: (urunId: number) => adminApi.deleteProduct(urunId),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-store-products'] });
            queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] });
            Alert.alert('Başarılı', 'Ürün silindi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Silme başarısız');
        },
    });

    const updateMutation = useMutation({
        mutationFn: (data: { id: number, updates: any }) => adminApi.updateProduct(data.id, data.updates),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-store-products'] });
            setEditModalVisible(false);
            Alert.alert('Başarılı', 'Ürün güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Güncelleme başarısız');
        },
    });

    const addMutation = useMutation({
        mutationFn: (product: any) => adminApi.addProduct(storeId, product),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-store-products'] });
            setAddModalVisible(false);
            setAddForm({ ad: '', fiyat: '', aciklama: '', renk: '', resimUrl: '' });
            Alert.alert('Başarılı', 'Ürün eklendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Ekleme başarısız');
        },
    });

    const handleDelete = (urun: any) => {
        Alert.alert(
            '⚠️ Ürün Sil',
            `"${urun.ad}" ürününü silmek istediğinize emin misiniz?`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteMutation.mutate(urun.id) }
            ]
        );
    };

    const handleEdit = (product: any) => {
        setEditingProduct(product);
        setEditForm({
            ad: product.ad,
            fiyat: product.fiyat.toString(),
            aciklama: product.aciklama || '',
        });
        setEditModalVisible(true);
    };

    const handleSaveEdit = () => {
        if (!editingProduct) return;
        updateMutation.mutate({
            id: editingProduct.id,
            updates: {
                ad: editForm.ad,
                fiyat: parseFloat(editForm.fiyat),
                aciklama: editForm.aciklama,
            }
        });
    };

    const handleAdd = () => {
        if (!addForm.ad || !addForm.fiyat) {
            Alert.alert('Hata', 'Ürün adı ve fiyatı zorunludur');
            return;
        }
        addMutation.mutate({
            ad: addForm.ad,
            fiyat: parseFloat(addForm.fiyat),
            aciklama: addForm.aciklama,
            renk: addForm.renk,
            resimUrl: addForm.resimUrl,
            // stoklar: [{ bedenId: 1, adet: 100 }] // Optional: Default stock?
        });
    };

    const renderProduct = ({ item, index }: any) => (
        <MotiView
            from={{ opacity: 0, translateY: 20 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: index * 50 }}
            className="mb-3"
        >
            <View className="bg-white/5 rounded-xl overflow-hidden">
                <View className="flex-row p-4">
                    {item.resimUrl ? (
                        <Image
                            source={{ uri: item.resimUrl }}
                            className="w-16 h-16 rounded-xl"
                            resizeMode="cover"
                        />
                    ) : (
                        <View className="w-16 h-16 rounded-xl bg-gray-600 items-center justify-center">
                            <Package size={24} color="#9ca3af" />
                        </View>
                    )}

                    <View className="flex-1 ml-3">
                        <View className="flex-row items-center justify-between">
                            <Text className="text-white font-semibold flex-1" numberOfLines={1}>
                                {item.ad}
                            </Text>
                            <View className={`px-2 py-1 rounded-lg ${item.aktif ? 'bg-green-500/20' : 'bg-red-500/20'}`}>
                                <Text className={`text-xs ${item.aktif ? 'text-green-400' : 'text-red-400'}`}>
                                    {item.aktif ? 'Aktif' : 'Pasif'}
                                </Text>
                            </View>
                        </View>
                        <Text className="text-gray-400 text-sm" numberOfLines={1}>
                            {item.kategori || 'Kategori yok'}
                        </Text>
                        <View className="flex-row items-center justify-between mt-1">
                            <Text className="text-primary-400 font-bold">₺{item.fiyat}</Text>
                            {item.renk && (
                                <Text className="text-gray-500 text-xs">Renk: {item.renk}</Text>
                            )}
                        </View>
                    </View>
                </View>

                {/* Actions */}
                <View className="flex-row border-t border-white/10">
                    <TouchableOpacity
                        onPress={() => toggleStatusMutation.mutate(item.id)}
                        className="flex-1 py-3 items-center flex-row justify-center"
                    >
                        {item.aktif ? (
                            <>
                                <EyeOff size={16} color="#f59e0b" />
                                <Text className="text-yellow-400 ml-2">Pasif</Text>
                            </>
                        ) : (
                            <>
                                <Eye size={16} color="#10b981" />
                                <Text className="text-green-400 ml-2">Aktif</Text>
                            </>
                        )}
                    </TouchableOpacity>
                    <View className="w-px bg-white/10" />
                    <TouchableOpacity
                        onPress={() => handleEdit(item)}
                        className="flex-1 py-3 items-center flex-row justify-center"
                    >
                        <Pencil size={16} color="#3b82f6" />
                        <Text className="text-blue-400 ml-2">Düzenle</Text>
                    </TouchableOpacity>
                    <View className="w-px bg-white/10" />
                    <TouchableOpacity
                        onPress={() => handleDelete(item)}
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
        <View className="flex-1 bg-gray-900">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5">
                <View className="flex-row items-center mb-4 justify-between">
                    <View className="flex-row items-center flex-1">
                        <TouchableOpacity
                            onPress={() => router.back()}
                            className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                        >
                            <ArrowLeft size={22} color="white" />
                        </TouchableOpacity>
                        <View className="flex-1">
                            <Text className="text-white text-xl font-bold">Mağaza Ürünleri</Text>
                            <Text className="text-gray-400 text-sm">{data?.magaza?.ad}</Text>
                        </View>
                    </View>

                    {/* Add Product Button */}
                    <TouchableOpacity
                        onPress={() => setAddModalVisible(true)}
                        className="w-10 h-10 rounded-full bg-green-500/20 items-center justify-center border border-green-500/30"
                    >
                        <Plus size={24} color="#4ade80" />
                    </TouchableOpacity>
                </View>
            </View>

            {/* Stats */}
            <View className="flex-row px-5 mb-4 gap-3">
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Toplam</Text>
                    <Text className="text-white text-xl font-bold">{data?.urunler?.length || 0}</Text>
                </View>
                <View className="flex-1 bg-green-500/10 rounded-xl p-3">
                    <Text className="text-green-400 text-sm">Aktif</Text>
                    <Text className="text-green-400 text-xl font-bold">
                        {data?.urunler?.filter((u: any) => u.aktif).length || 0}
                    </Text>
                </View>
                <View className="flex-1 bg-red-500/10 rounded-xl p-3">
                    <Text className="text-red-400 text-sm">Pasif</Text>
                    <Text className="text-red-400 text-xl font-bold">
                        {data?.urunler?.filter((u: any) => !u.aktif).length || 0}
                    </Text>
                </View>
            </View>

            {/* List */}
            <FlatList
                data={data?.urunler || []}
                renderItem={renderProduct}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
                ListEmptyComponent={
                    <View className="items-center py-10">
                        <Package size={48} color="#6b7280" />
                        <Text className="text-gray-400 mt-4">Ürün bulunamadı</Text>
                    </View>
                }
            />

            {/* Edit Modal */}
            <Modal
                visible={editModalVisible}
                animationType="slide"
                transparent={true}
                onRequestClose={() => setEditModalVisible(false)}
            >
                <KeyboardAvoidingView
                    behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                    className="flex-1 justify-end"
                >
                    <View className="flex-1 justify-end bg-black/60">
                        <View className="bg-gray-900 rounded-t-3xl">
                            <View className="p-5 border-b border-gray-800 flex-row justify-between items-center">
                                <Text className="text-white text-xl font-bold">Ürün Düzenle</Text>
                                <TouchableOpacity
                                    onPress={() => setEditModalVisible(false)}
                                    className="w-8 h-8 rounded-full bg-gray-800 items-center justify-center"
                                >
                                    <X size={18} color="#9ca3af" />
                                </TouchableOpacity>
                            </View>

                            <ScrollView className="p-5">
                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Ürün Adı</Text>
                                    <TextInput
                                        value={editForm.ad}
                                        onChangeText={(text) => setEditForm({ ...editForm, ad: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Fiyat (TL)</Text>
                                    <TextInput
                                        value={editForm.fiyat}
                                        onChangeText={(text) => setEditForm({ ...editForm, fiyat: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        keyboardType="numeric"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-6">
                                    <Text className="text-gray-500 mb-2 text-sm">Açıklama</Text>
                                    <TextInput
                                        value={editForm.aciklama}
                                        onChangeText={(text) => setEditForm({ ...editForm, aciklama: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white min-h-[100px]"
                                        multiline
                                        textAlignVertical="top"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="flex-row gap-3 mb-8">
                                    <TouchableOpacity
                                        onPress={() => setEditModalVisible(false)}
                                        className="flex-1 py-4 rounded-xl bg-gray-800 items-center"
                                    >
                                        <Text className="text-gray-400 font-semibold">İptal</Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        onPress={handleSaveEdit}
                                        className="flex-1 py-4 rounded-xl bg-blue-600 items-center flex-row justify-center"
                                    >
                                        <Save size={18} color="white" />
                                        <Text className="text-white font-bold ml-2">Kaydet</Text>
                                    </TouchableOpacity>
                                </View>
                            </ScrollView>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>

            {/* Add Product Modal */}
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
                        <View className="bg-gray-900 rounded-t-3xl h-[80%]">
                            <View className="p-5 border-b border-gray-800 flex-row justify-between items-center">
                                <Text className="text-white text-xl font-bold">Yeni Ürün Ekle</Text>
                                <TouchableOpacity
                                    onPress={() => setAddModalVisible(false)}
                                    className="w-8 h-8 rounded-full bg-gray-800 items-center justify-center"
                                >
                                    <X size={18} color="#9ca3af" />
                                </TouchableOpacity>
                            </View>

                            <ScrollView className="p-5">
                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Ürün Adı *</Text>
                                    <TextInput
                                        value={addForm.ad}
                                        onChangeText={(text) => setAddForm({ ...addForm, ad: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholder="Örn: Mavi Tişört"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-4 flex-row gap-4">
                                    <View className="flex-1">
                                        <Text className="text-gray-500 mb-2 text-sm">Fiyat (TL) *</Text>
                                        <TextInput
                                            value={addForm.fiyat}
                                            onChangeText={(text) => setAddForm({ ...addForm, fiyat: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            keyboardType="numeric"
                                            placeholder="0.00"
                                            placeholderTextColor="#6b7280"
                                        />
                                    </View>
                                    <View className="flex-1">
                                        <Text className="text-gray-500 mb-2 text-sm">Renk</Text>
                                        <TextInput
                                            value={addForm.renk}
                                            onChangeText={(text) => setAddForm({ ...addForm, renk: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            placeholder="Örn: Mavi"
                                            placeholderTextColor="#6b7280"
                                        />
                                    </View>
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Resim URL</Text>
                                    <TextInput
                                        value={addForm.resimUrl}
                                        onChangeText={(text) => setAddForm({ ...addForm, resimUrl: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholder="https://..."
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-6">
                                    <Text className="text-gray-500 mb-2 text-sm">Açıklama</Text>
                                    <TextInput
                                        value={addForm.aciklama}
                                        onChangeText={(text) => setAddForm({ ...addForm, aciklama: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white min-h-[100px]"
                                        multiline
                                        textAlignVertical="top"
                                        placeholder="Ürün açıklaması..."
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="flex-row gap-3 mb-12">
                                    <TouchableOpacity
                                        onPress={() => setAddModalVisible(false)}
                                        className="flex-1 py-4 rounded-xl bg-gray-800 items-center"
                                    >
                                        <Text className="text-gray-400 font-semibold">İptal</Text>
                                    </TouchableOpacity>
                                    <TouchableOpacity
                                        onPress={handleAdd}
                                        className="flex-1 py-4 rounded-xl bg-green-600 items-center flex-row justify-center"
                                    >
                                        <Plus size={18} color="white" />
                                        <Text className="text-white font-bold ml-2">Ekle</Text>
                                    </TouchableOpacity>
                                </View>
                            </ScrollView>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </View>
    );
}
