import React, { useState } from 'react';
import {
    View,
    Text,
    FlatList,
    TouchableOpacity,
    RefreshControl,
    Alert,
    TextInput,
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
    User,
    Shield,
    Store,
    ShoppingBag,
    Search,
    CheckCircle,
    XCircle,
    Edit,
    Trash2,
    X,
    Save,
    Plus,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

const ROLES = ['MUSTERI', 'MAGAZA_SAHIBI', 'ADMIN'];

export default function AdminUsersScreen() {
    const { isAdmin } = useAuthStore();
    const queryClient = useQueryClient();
    const [searchQuery, setSearchQuery] = useState('');
    const [selectedUser, setSelectedUser] = useState<number | null>(null);
    const [editModalVisible, setEditModalVisible] = useState(false);
    const [editingUser, setEditingUser] = useState<any>(null);
    const [editForm, setEditForm] = useState({ ad: '', soyad: '', email: '', telefon: '' });

    // Add User State
    const [addModalVisible, setAddModalVisible] = useState(false);
    const [addForm, setAddForm] = useState({
        kullaniciAdi: '',
        email: '',
        sifre: '',
        ad: '',
        soyad: '',
        telefon: '',
        adres: '',
        rol: 'MUSTERI'
    });

    const { data: kullanicilar = [], isLoading, refetch } = useQuery({
        queryKey: ['admin-users'],
        queryFn: adminApi.getKullanicilar,
        enabled: isAdmin(),
    });

    const toggleStatusMutation = useMutation({
        mutationFn: (id: number) => adminApi.toggleUserStatus(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            Alert.alert('Başarılı', 'Kullanıcı durumu güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        },
    });

    const changeRoleMutation = useMutation({
        mutationFn: ({ id, rol }: { id: number; rol: string }) => adminApi.changeUserRole(id, rol),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            setSelectedUser(null);
            Alert.alert('Başarılı', 'Rol güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        },
    });

    const deleteUserMutation = useMutation({
        mutationFn: (id: number) => adminApi.deleteUser(id),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            queryClient.invalidateQueries({ queryKey: ['admin-dashboard'] });
            setSelectedUser(null);
            Alert.alert('Başarılı', 'Kullanıcı silindi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Silme başarısız');
        },
    });

    const updateUserMutation = useMutation({
        mutationFn: ({ id, data }: { id: number; data: any }) => adminApi.updateUser(id, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            setEditModalVisible(false);
            setEditingUser(null);
            Alert.alert('Başarılı', 'Kullanıcı bilgileri güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Güncelleme başarısız');
        },
    });

    const addUserMutation = useMutation({
        mutationFn: (data: any) => adminApi.createUser(data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['admin-users'] });
            setAddModalVisible(false);
            setAddForm({ kullaniciAdi: '', email: '', sifre: '', ad: '', soyad: '', telefon: '', adres: '', rol: 'MUSTERI' });
            Alert.alert('Başarılı', 'Kullanıcı oluşturuldu');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Oluşturma başarısız');
        },
    });

    const handleSaveAdd = () => {
        if (!addForm.kullaniciAdi || !addForm.sifre || !addForm.email) {
            Alert.alert('Hata', 'Lütfen zorunlu alanları doldurun (Kullanıcı Adı, Email, Şifre)');
            return;
        }
        addUserMutation.mutate(addForm);
    };

    const handleEditUser = (user: any) => {
        setEditingUser(user);
        setEditForm({
            ad: user.ad || '',
            soyad: user.soyad || '',
            email: user.email || '',
            telefon: user.telefon || '',
        });
        setEditModalVisible(true);
    };

    const handleSaveEdit = () => {
        if (!editingUser) return;
        updateUserMutation.mutate({
            id: editingUser.id,
            data: editForm,
        });
    };

    const handleDeleteUser = (user: any) => {
        if (user.rol === 'ADMIN') {
            Alert.alert('Hata', 'Admin kullanıcıları silinemez!');
            return;
        }
        Alert.alert(
            '⚠️ Kullanıcı Sil',
            `"${user.ad} ${user.soyad}" kullanıcısını silmek istediğinize emin misiniz?`,
            [
                { text: 'İptal', style: 'cancel' },
                { text: 'Sil', style: 'destructive', onPress: () => deleteUserMutation.mutate(user.id) }
            ]
        );
    };

    const filteredUsers = kullanicilar.filter((k: any) =>
        k.kullaniciAdi?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        k.email?.toLowerCase().includes(searchQuery.toLowerCase()) ||
        k.ad?.toLowerCase().includes(searchQuery.toLowerCase())
    );

    const getRolIcon = (rol: string) => {
        switch (rol) {
            case 'ADMIN': return Shield;
            case 'MAGAZA_SAHIBI': return Store;
            default: return User;
        }
    };

    const getRolColor = (rol: string) => {
        switch (rol) {
            case 'ADMIN': return '#ef4444';
            case 'MAGAZA_SAHIBI': return '#3b82f6';
            default: return '#10b981';
        }
    };

    const renderUser = ({ item, index }: any) => {
        const RolIcon = getRolIcon(item.rol);
        const rolColor = getRolColor(item.rol);
        const isExpanded = selectedUser === item.id;

        return (
            <MotiView
                from={{ opacity: 0, translateY: 20 }}
                animate={{ opacity: 1, translateY: 0 }}
                transition={{ delay: index * 50 }}
                className="mb-3"
            >
                <TouchableOpacity
                    onPress={() => setSelectedUser(isExpanded ? null : item.id)}
                    className="bg-white/5 rounded-xl overflow-hidden"
                >
                    <View className="p-4">
                        <View className="flex-row items-center">
                            <View className="w-12 h-12 rounded-full bg-white/10 items-center justify-center">
                                <Text className="text-white font-bold">
                                    {item.ad?.charAt(0)}{item.soyad?.charAt(0)}
                                </Text>
                            </View>
                            <View className="flex-1 ml-3">
                                <View className="flex-row items-center">
                                    <Text className="text-white font-semibold">{item.ad} {item.soyad}</Text>
                                    <View
                                        className="ml-2 flex-row items-center px-2 py-1 rounded-lg"
                                        style={{ backgroundColor: `${rolColor}20` }}
                                    >
                                        <RolIcon size={12} color={rolColor} />
                                        <Text className="ml-1 text-xs" style={{ color: rolColor }}>
                                            {item.rol}
                                        </Text>
                                    </View>
                                </View>
                                <Text className="text-gray-400 text-sm">@{item.kullaniciAdi}</Text>
                                <Text className="text-gray-500 text-xs">{item.email}</Text>
                            </View>
                            <View className="items-end">
                                {item.aktif ? (
                                    <View className="flex-row items-center">
                                        <CheckCircle size={16} color="#10b981" />
                                        <Text className="text-green-400 text-sm ml-1">Aktif</Text>
                                    </View>
                                ) : (
                                    <View className="flex-row items-center">
                                        <XCircle size={16} color="#ef4444" />
                                        <Text className="text-red-400 text-sm ml-1">Pasif</Text>
                                    </View>
                                )}
                            </View>
                        </View>
                    </View>

                    {/* Actions */}
                    {isExpanded && (
                        <MotiView
                            from={{ opacity: 0 }}
                            animate={{ opacity: 1 }}
                            className="px-4 pb-4 border-t border-white/10 pt-3"
                        >
                            {/* Rol Değiştir */}
                            <Text className="text-gray-400 text-sm mb-2">Rol Değiştir:</Text>
                            <View className="flex-row gap-2 mb-4">
                                {ROLES.map((rol) => (
                                    <TouchableOpacity
                                        key={rol}
                                        onPress={() => changeRoleMutation.mutate({ id: item.id, rol })}
                                        disabled={item.rol === rol}
                                        className={`px-3 py-2 rounded-lg ${item.rol === rol ? 'opacity-50' : ''}`}
                                        style={{ backgroundColor: `${getRolColor(rol)}20` }}
                                    >
                                        <Text style={{ color: getRolColor(rol) }} className="text-sm">{rol}</Text>
                                    </TouchableOpacity>
                                ))}
                            </View>

                            {/* Action Buttons */}
                            <View className="flex-row gap-2">
                                <TouchableOpacity
                                    onPress={() => toggleStatusMutation.mutate(item.id)}
                                    className="flex-1 py-3 rounded-lg bg-yellow-600/20 flex-row items-center justify-center"
                                >
                                    {item.aktif ? (
                                        <>
                                            <XCircle size={16} color="#eab308" />
                                            <Text className="text-yellow-400 ml-2">Pasif Yap</Text>
                                        </>
                                    ) : (
                                        <>
                                            <CheckCircle size={16} color="#22c55e" />
                                            <Text className="text-green-400 ml-2">Aktif Yap</Text>
                                        </>
                                    )}
                                </TouchableOpacity>

                                <TouchableOpacity
                                    onPress={() => handleEditUser(item)}
                                    className="flex-1 py-3 rounded-lg bg-blue-600/20 flex-row items-center justify-center"
                                >
                                    <Edit size={16} color="#3b82f6" />
                                    <Text className="text-blue-400 ml-2">Düzenle</Text>
                                </TouchableOpacity>

                                {item.rol !== 'ADMIN' && (
                                    <TouchableOpacity
                                        onPress={() => handleDeleteUser(item)}
                                        className="flex-1 py-3 rounded-lg bg-red-600/20 flex-row items-center justify-center"
                                    >
                                        <Trash2 size={16} color="#ef4444" />
                                        <Text className="text-red-400 ml-2">Sil</Text>
                                    </TouchableOpacity>
                                )}
                            </View>
                        </MotiView>
                    )}
                </TouchableOpacity>
            </MotiView>
        );
    };

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
                    <Text className="text-white text-2xl font-bold flex-1">Kullanıcı Yönetimi</Text>
                    <TouchableOpacity
                        onPress={() => setAddModalVisible(true)}
                        className="w-10 h-10 rounded-full bg-blue-600 items-center justify-center"
                    >
                        <Plus size={24} color="white" />
                    </TouchableOpacity>
                </View>

                {/* Search */}
                <View className="flex-row items-center bg-white/10 rounded-xl px-4 py-3">
                    <Search size={20} color="#9ca3af" />
                    <TextInput
                        value={searchQuery}
                        onChangeText={setSearchQuery}
                        placeholder="Kullanıcı ara..."
                        placeholderTextColor="#6b7280"
                        className="flex-1 text-white ml-3"
                    />
                </View>
            </View>

            {/* Stats */}
            <View className="flex-row px-5 mb-4 gap-3">
                <View className="flex-1 bg-white/5 rounded-xl p-3">
                    <Text className="text-gray-400 text-sm">Toplam</Text>
                    <Text className="text-white text-xl font-bold">{kullanicilar.length}</Text>
                </View>
                <View className="flex-1 bg-green-500/10 rounded-xl p-3">
                    <Text className="text-green-400 text-sm">Aktif</Text>
                    <Text className="text-green-400 text-xl font-bold">
                        {kullanicilar.filter((k: any) => k.aktif).length}
                    </Text>
                </View>
                <View className="flex-1 bg-red-500/10 rounded-xl p-3">
                    <Text className="text-red-400 text-sm">Pasif</Text>
                    <Text className="text-red-400 text-xl font-bold">
                        {kullanicilar.filter((k: any) => !k.aktif).length}
                    </Text>
                </View>
            </View>

            {/* List */}
            <FlatList
                data={filteredUsers}
                renderItem={renderUser}
                keyExtractor={(item) => item.id.toString()}
                contentContainerStyle={{ paddingHorizontal: 20, paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
                ListEmptyComponent={
                    <View className="items-center py-10">
                        <User size={48} color="#6b7280" />
                        <Text className="text-gray-400 mt-4">Kullanıcı bulunamadı</Text>
                    </View>
                }
            />

            {/* Edit Modal - Clean Dark Design */}
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
                            {/* Header */}
                            <View className="p-5 border-b border-gray-800">
                                <View className="flex-row items-center justify-between">
                                    <Text className="text-white text-xl font-bold">Kullanıcı Düzenle</Text>
                                    <TouchableOpacity
                                        onPress={() => setEditModalVisible(false)}
                                        className="w-8 h-8 rounded-full bg-gray-800 items-center justify-center"
                                    >
                                        <X size={18} color="#9ca3af" />
                                    </TouchableOpacity>
                                </View>
                            </View>

                            {editingUser && (
                                <ScrollView className="p-5">
                                    {/* Form Fields */}
                                    <View className="flex-row gap-3 mb-4">
                                        <View className="flex-1">
                                            <Text className="text-gray-500 mb-2 text-sm">Ad</Text>
                                            <TextInput
                                                value={editForm.ad}
                                                onChangeText={(text) => setEditForm({ ...editForm, ad: text })}
                                                className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                                placeholderTextColor="#6b7280"
                                                style={{ color: 'white' }}
                                            />
                                        </View>
                                        <View className="flex-1">
                                            <Text className="text-gray-500 mb-2 text-sm">Soyad</Text>
                                            <TextInput
                                                value={editForm.soyad}
                                                onChangeText={(text) => setEditForm({ ...editForm, soyad: text })}
                                                className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                                placeholderTextColor="#6b7280"
                                                style={{ color: 'white' }}
                                            />
                                        </View>
                                    </View>

                                    <View className="mb-4">
                                        <Text className="text-gray-500 mb-2 text-sm">E-posta</Text>
                                        <TextInput
                                            value={editForm.email}
                                            onChangeText={(text) => setEditForm({ ...editForm, email: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            keyboardType="email-address"
                                            placeholderTextColor="#6b7280"
                                            style={{ color: 'white' }}
                                        />
                                    </View>

                                    <View className="mb-5">
                                        <Text className="text-gray-500 mb-2 text-sm">Telefon</Text>
                                        <TextInput
                                            value={editForm.telefon}
                                            onChangeText={(text) => setEditForm({ ...editForm, telefon: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            keyboardType="phone-pad"
                                            placeholderTextColor="#6b7280"
                                            style={{ color: 'white' }}
                                        />
                                    </View>

                                    {/* Role Selection */}
                                    <View className="mb-6">
                                        <Text className="text-gray-500 mb-3 text-sm">Rol Değiştir</Text>
                                        <View className="flex-row gap-2">
                                            {ROLES.map((rol) => {
                                                const isSelected = editingUser.rol === rol;
                                                return (
                                                    <TouchableOpacity
                                                        key={rol}
                                                        onPress={() => {
                                                            if (!isSelected) {
                                                                changeRoleMutation.mutate({ id: editingUser.id, rol });
                                                            }
                                                        }}
                                                        disabled={isSelected}
                                                        className={`flex-1 py-3 rounded-xl items-center ${isSelected ? 'bg-primary-600' : 'bg-gray-800'}`}
                                                    >
                                                        <Text className={`text-sm ${isSelected ? 'text-white font-bold' : 'text-gray-400'}`}>
                                                            {rol === 'MUSTERI' ? 'Müşteri' : rol === 'MAGAZA_SAHIBI' ? 'Mağaza' : 'Admin'}
                                                        </Text>
                                                    </TouchableOpacity>
                                                );
                                            })}
                                        </View>
                                    </View>

                                    {/* Action Buttons */}
                                    <View className="flex-row gap-3 mb-8">
                                        <TouchableOpacity
                                            onPress={() => setEditModalVisible(false)}
                                            className="flex-1 py-4 rounded-xl bg-gray-800 items-center"
                                        >
                                            <Text className="text-gray-400 font-semibold">İptal</Text>
                                        </TouchableOpacity>
                                        <TouchableOpacity
                                            onPress={handleSaveEdit}
                                            className="flex-1 py-4 rounded-xl bg-green-600 items-center"
                                        >
                                            <Text className="text-white font-bold">Kaydet</Text>
                                        </TouchableOpacity>
                                    </View>
                                </ScrollView>
                            )}
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
            {/* Add User Modal */}
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
                        <View className="bg-gray-900 rounded-t-3xl h-[85%]">
                            <View className="p-5 border-b border-gray-800 flex-row items-center justify-between">
                                <Text className="text-white text-xl font-bold">Yeni Kullanıcı Ekle</Text>
                                <TouchableOpacity
                                    onPress={() => setAddModalVisible(false)}
                                    className="w-8 h-8 rounded-full bg-gray-800 items-center justify-center"
                                >
                                    <X size={18} color="#9ca3af" />
                                </TouchableOpacity>
                            </View>

                            <ScrollView className="p-5">
                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Kullanıcı Adı *</Text>
                                    <TextInput
                                        value={addForm.kullaniciAdi}
                                        onChangeText={(text) => setAddForm({ ...addForm, kullaniciAdi: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">E-posta *</Text>
                                    <TextInput
                                        value={addForm.email}
                                        onChangeText={(text) => setAddForm({ ...addForm, email: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        keyboardType="email-address"
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Şifre *</Text>
                                    <TextInput
                                        value={addForm.sifre}
                                        onChangeText={(text) => setAddForm({ ...addForm, sifre: text })}
                                        className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                        secureTextEntry
                                        placeholderTextColor="#6b7280"
                                    />
                                </View>

                                <View className="flex-row gap-3 mb-4">
                                    <View className="flex-1">
                                        <Text className="text-gray-500 mb-2 text-sm">Ad</Text>
                                        <TextInput
                                            value={addForm.ad}
                                            onChangeText={(text) => setAddForm({ ...addForm, ad: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            placeholderTextColor="#6b7280"
                                        />
                                    </View>
                                    <View className="flex-1">
                                        <Text className="text-gray-500 mb-2 text-sm">Soyad</Text>
                                        <TextInput
                                            value={addForm.soyad}
                                            onChangeText={(text) => setAddForm({ ...addForm, soyad: text })}
                                            className="bg-gray-800 rounded-xl px-4 py-3 text-white"
                                            placeholderTextColor="#6b7280"
                                        />
                                    </View>
                                </View>

                                <View className="mb-4">
                                    <Text className="text-gray-500 mb-2 text-sm">Rol</Text>
                                    <View className="flex-row gap-2">
                                        {['MUSTERI', 'MAGAZA_SAHIBI'].map((r) => (
                                            <TouchableOpacity
                                                key={r}
                                                onPress={() => setAddForm({ ...addForm, rol: r })}
                                                className={`flex-1 py-3 rounded-xl items-center ${addForm.rol === r ? 'bg-primary-600' : 'bg-gray-800'}`}
                                            >
                                                <Text className={`text-sm ${addForm.rol === r ? 'text-white font-bold' : 'text-gray-400'}`}>
                                                    {r === 'MUSTERI' ? 'Müşteri' : 'Mağaza Sahibi'}
                                                </Text>
                                            </TouchableOpacity>
                                        ))}
                                    </View>
                                </View>

                                <TouchableOpacity
                                    onPress={handleSaveAdd}
                                    className="w-full py-4 rounded-xl bg-blue-600 items-center mt-4 mb-8"
                                >
                                    <Text className="text-white font-bold text-lg">Kullanıcı Oluştur</Text>
                                </TouchableOpacity>
                            </ScrollView>
                        </View>
                    </View>
                </KeyboardAvoidingView>
            </Modal>
        </View>
    );
}
