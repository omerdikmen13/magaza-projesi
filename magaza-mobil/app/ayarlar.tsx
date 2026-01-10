import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    TextInput,
    Alert,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useMutation } from '@tanstack/react-query';
import {
    ArrowLeft,
    User,
    Mail,
    Phone,
    MapPin,
    Save,
    Lock,
    Eye,
    EyeOff,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { useAuthStore } from '../stores/authStore';
import { authApi } from '../services/api';

export default function AyarlarScreen() {
    const { user, setUser, token, isAuthenticated, isAdmin } = useAuthStore();

    const [form, setForm] = useState({
        ad: user?.ad || '',
        soyad: user?.soyad || '',
        email: user?.email || '',
        telefon: user?.telefon || '',
        adres: user?.adres || '',
    });

    const [sifreForm, setSifreForm] = useState({
        mevcutSifre: '',
        yeniSifre: '',
        yeniSifreTekrar: '',
    });

    const [showPasswords, setShowPasswords] = useState(false);

    const updateMutation = useMutation({
        mutationFn: (data: any) => authApi.profilGuncelle(data),
        onSuccess: (data) => {
            if (data.kullanici) {
                setUser(data.kullanici, token!);
            }
            Alert.alert('Başarılı', 'Profil güncellendi');
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Profil güncellenemedi');
        },
    });

    const sifreMutation = useMutation({
        mutationFn: () => authApi.sifreDegistir(sifreForm.mevcutSifre, sifreForm.yeniSifre),
        onSuccess: () => {
            Alert.alert('Başarılı', 'Şifre değiştirildi');
            setSifreForm({ mevcutSifre: '', yeniSifre: '', yeniSifreTekrar: '' });
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Şifre değiştirilemedi');
        },
    });

    const handleSave = () => {
        if (!form.ad || !form.soyad) {
            Alert.alert('Hata', 'Ad ve soyad zorunludur');
            return;
        }
        updateMutation.mutate(form);
    };

    const handleSifreDegistir = () => {
        if (!sifreForm.mevcutSifre || !sifreForm.yeniSifre) {
            Alert.alert('Hata', 'Tüm alanları doldurun');
            return;
        }
        if (sifreForm.yeniSifre !== sifreForm.yeniSifreTekrar) {
            Alert.alert('Hata', 'Yeni şifreler eşleşmiyor');
            return;
        }
        if (sifreForm.yeniSifre.length < 4) {
            Alert.alert('Hata', 'Şifre en az 4 karakter olmalı');
            return;
        }
        sifreMutation.mutate();
    };

    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <Text className="text-gray-900 text-xl font-bold">Giriş yapmanız gerekiyor</Text>
                <TouchableOpacity onPress={() => router.push('/(auth)/login')} className="mt-6">
                    <LinearGradient colors={['#667eea', '#764ba2']} className="px-8 py-4 rounded-2xl">
                        <Text className="text-white font-bold text-lg">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
            </View>
        );
    }

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
                    <Text className="text-gray-900 text-xl font-bold">⚙️ Hesap Ayarları</Text>
                </View>
            </View>

            <ScrollView className="flex-1 px-5 py-4" contentContainerStyle={{ paddingBottom: 50 }}>
                {/* Profil Bilgileri */}
                <MotiView
                    from={{ opacity: 0, translateY: 20 }}
                    animate={{ opacity: 1, translateY: 0 }}
                    className="bg-white rounded-2xl p-5 shadow-sm mb-4"
                    style={{ elevation: 2 }}
                >
                    <Text className="text-gray-900 font-bold text-lg mb-4">👤 Profil Bilgileri</Text>

                    {/* Ad */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <User size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Ad</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Adınız"
                            value={form.ad}
                            onChangeText={(t) => setForm({ ...form, ad: t })}
                        />
                    </View>

                    {/* Soyad */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <User size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Soyad</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Soyadınız"
                            value={form.soyad}
                            onChangeText={(t) => setForm({ ...form, soyad: t })}
                        />
                    </View>

                    {/* Email */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Mail size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">E-posta</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="E-posta adresiniz"
                            value={form.email}
                            onChangeText={(t) => setForm({ ...form, email: t })}
                            keyboardType="email-address"
                        />
                    </View>

                    {/* Telefon - Müşteriler için */}
                    {!isAdmin() && (
                        <View className="mb-4">
                            <View className="flex-row items-center mb-2">
                                <Phone size={16} color="#667eea" />
                                <Text className="text-gray-900 font-semibold ml-2">Telefon</Text>
                            </View>
                            <TextInput
                                className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Telefon numaranız"
                                value={form.telefon}
                                onChangeText={(t) => setForm({ ...form, telefon: t })}
                                keyboardType="phone-pad"
                            />
                        </View>
                    )}

                    {/* Adres - Müşteriler için */}
                    {!isAdmin() && (
                        <View className="mb-4">
                            <View className="flex-row items-center mb-2">
                                <MapPin size={16} color="#667eea" />
                                <Text className="text-gray-900 font-semibold ml-2">Adres</Text>
                            </View>
                            <TextInput
                                className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                                placeholder="Teslimat adresiniz"
                                value={form.adres}
                                onChangeText={(t) => setForm({ ...form, adres: t })}
                                multiline
                                numberOfLines={3}
                            />
                        </View>
                    )}

                    {/* Kaydet Butonu */}
                    <TouchableOpacity onPress={handleSave}>
                        <LinearGradient
                            colors={['#667eea', '#764ba2']}
                            className="py-4 rounded-2xl flex-row items-center justify-center"
                        >
                            <Save size={20} color="white" />
                            <Text className="text-white font-bold text-lg ml-2">Kaydet</Text>
                        </LinearGradient>
                    </TouchableOpacity>
                </MotiView>

                {/* Şifre Değiştir */}
                <MotiView
                    from={{ opacity: 0, translateY: 20 }}
                    animate={{ opacity: 1, translateY: 0 }}
                    transition={{ delay: 100 }}
                    className="bg-white rounded-2xl p-5 shadow-sm"
                    style={{ elevation: 2 }}
                >
                    <View className="flex-row items-center justify-between mb-4">
                        <Text className="text-gray-900 font-bold text-lg">🔒 Şifre Değiştir</Text>
                        <TouchableOpacity onPress={() => setShowPasswords(!showPasswords)}>
                            {showPasswords ? (
                                <EyeOff size={20} color="#667eea" />
                            ) : (
                                <Eye size={20} color="#667eea" />
                            )}
                        </TouchableOpacity>
                    </View>

                    {/* Mevcut Şifre */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Lock size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Mevcut Şifre</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Mevcut şifreniz"
                            value={sifreForm.mevcutSifre}
                            onChangeText={(t) => setSifreForm({ ...sifreForm, mevcutSifre: t })}
                            secureTextEntry={!showPasswords}
                        />
                    </View>

                    {/* Yeni Şifre */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Lock size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Yeni Şifre</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Yeni şifreniz"
                            value={sifreForm.yeniSifre}
                            onChangeText={(t) => setSifreForm({ ...sifreForm, yeniSifre: t })}
                            secureTextEntry={!showPasswords}
                        />
                    </View>

                    {/* Yeni Şifre Tekrar */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Lock size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Yeni Şifre (Tekrar)</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Yeni şifrenizi tekrar girin"
                            value={sifreForm.yeniSifreTekrar}
                            onChangeText={(t) => setSifreForm({ ...sifreForm, yeniSifreTekrar: t })}
                            secureTextEntry={!showPasswords}
                        />
                    </View>

                    {/* Şifre Değiştir Butonu */}
                    <TouchableOpacity onPress={handleSifreDegistir}>
                        <LinearGradient
                            colors={['#11998e', '#38ef7d']}
                            className="py-4 rounded-2xl flex-row items-center justify-center"
                        >
                            <Lock size={20} color="white" />
                            <Text className="text-white font-bold text-lg ml-2">Şifre Değiştir</Text>
                        </LinearGradient>
                    </TouchableOpacity>
                </MotiView>
            </ScrollView>
        </View>
    );
}
