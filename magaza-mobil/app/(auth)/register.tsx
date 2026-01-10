import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    KeyboardAvoidingView,
    Platform,
    ScrollView,
    ActivityIndicator,
    Alert,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { BlurView } from 'expo-blur';
import { router } from 'expo-router';
import { UserPlus, Mail, Lock, User, Phone, MapPin, Eye, EyeOff, Store } from 'lucide-react-native';
import { MotiView } from 'moti';

import { authApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function RegisterScreen() {
    const [formData, setFormData] = useState({
        kullaniciAdi: '',
        email: '',
        sifre: '',
        sifreTekrar: '',
        ad: '',
        soyad: '',
        telefon: '',
        adres: '',
        rol: 'MUSTERI', // Varsayılan rol
    });
    const [showPassword, setShowPassword] = useState(false);
    const [isLoading, setIsLoading] = useState(false);
    const { setUser } = useAuthStore();

    const handleRegister = async () => {
        if (!formData.kullaniciAdi || !formData.email || !formData.sifre || !formData.ad || !formData.soyad) {
            Alert.alert('Hata', 'Lütfen zorunlu alanları doldurun');
            return;
        }

        if (formData.sifre !== formData.sifreTekrar) {
            Alert.alert('Hata', 'Şifreler eşleşmiyor');
            return;
        }

        setIsLoading(true);
        try {
            const response = await authApi.register(formData);
            setUser(response.kullanici, response.token || `simple-token-${response.kullanici.id}`);
            Alert.alert('Başarılı', 'Kayıt başarılı!', [
                { text: 'Tamam', onPress: () => router.replace('/(tabs)') }
            ]);
        } catch (error: any) {
            Alert.alert('Kayıt Hatası', error.response?.data?.error || 'Kayıt yapılamadı');
        } finally {
            setIsLoading(false);
        }
    };

    const updateField = (field: string, value: string) => {
        setFormData(prev => ({ ...prev, [field]: value }));
    };

    return (
        <View className="flex-1 bg-dark-300">
            <LinearGradient
                colors={['#1e1e2f', '#0f0f1f', '#0a0a15']}
                className="absolute inset-0"
            />

            <KeyboardAvoidingView
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                className="flex-1"
            >
                <ScrollView
                    contentContainerStyle={{ flexGrow: 1 }}
                    keyboardShouldPersistTaps="handled"
                >
                    <View className="px-6 py-8">
                        {/* Header */}
                        <MotiView
                            from={{ opacity: 0, translateY: -20 }}
                            animate={{ opacity: 1, translateY: 0 }}
                            className="items-center mb-6"
                        >
                            <View className="w-16 h-16 rounded-full bg-gradient-to-br from-accent-500 to-primary-500 items-center justify-center mb-3">
                                <UserPlus size={32} color="white" />
                            </View>
                            <Text className="text-2xl font-bold text-white">Hesap Oluştur</Text>
                        </MotiView>

                        {/* Form */}
                        <MotiView
                            from={{ opacity: 0, translateY: 20 }}
                            animate={{ opacity: 1, translateY: 0 }}
                            transition={{ delay: 100 }}
                        >
                            <BlurView intensity={15} tint="dark" className="rounded-2xl overflow-hidden">
                                <View className="p-5 bg-white/5">

                                    {/* Role Selection */}
                                    <View className="flex-row bg-black/20 p-1 rounded-xl mb-6">
                                        <TouchableOpacity
                                            onPress={() => updateField('rol', 'MUSTERI')}
                                            className={`flex-1 flex-row items-center justify-center py-3 rounded-lg ${formData.rol === 'MUSTERI' ? 'bg-primary-600' : 'bg-transparent'}`}
                                        >
                                            <User size={18} color={formData.rol === 'MUSTERI' ? 'white' : '#9ca3af'} />
                                            <Text className={`ml-2 font-medium ${formData.rol === 'MUSTERI' ? 'text-white' : 'text-gray-400'}`}>Müşteri</Text>
                                        </TouchableOpacity>

                                        <TouchableOpacity
                                            onPress={() => updateField('rol', 'MAGAZA_SAHIBI')}
                                            className={`flex-1 flex-row items-center justify-center py-3 rounded-lg ${formData.rol === 'MAGAZA_SAHIBI' ? 'bg-accent-600' : 'bg-transparent'}`}
                                        >
                                            <Store size={18} color={formData.rol === 'MAGAZA_SAHIBI' ? 'white' : '#9ca3af'} />
                                            <Text className={`ml-2 font-medium ${formData.rol === 'MAGAZA_SAHIBI' ? 'text-white' : 'text-gray-400'}`}>Mağaza Sahibi</Text>
                                        </TouchableOpacity>
                                    </View>

                                    {/* Name Row */}
                                    <View className="flex-row gap-3 mb-3">
                                        <View className="flex-1">
                                            <Text className="text-gray-300 text-sm mb-1">Ad *</Text>
                                            <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                                <User size={18} color="#9ca3af" />
                                                <TextInput
                                                    className="flex-1 ml-2 text-white"
                                                    placeholder="Ad"
                                                    placeholderTextColor="#6b7280"
                                                    value={formData.ad}
                                                    onChangeText={(v) => updateField('ad', v)}
                                                />
                                            </View>
                                        </View>
                                        <View className="flex-1">
                                            <Text className="text-gray-300 text-sm mb-1">Soyad *</Text>
                                            <View className="bg-white/10 rounded-xl px-3 py-2.5">
                                                <TextInput
                                                    className="text-white"
                                                    placeholder="Soyad"
                                                    placeholderTextColor="#6b7280"
                                                    value={formData.soyad}
                                                    onChangeText={(v) => updateField('soyad', v)}
                                                />
                                            </View>
                                        </View>
                                    </View>

                                    {/* Username */}
                                    <View className="mb-3">
                                        <Text className="text-gray-300 text-sm mb-1">Kullanıcı Adı *</Text>
                                        <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                            <User size={18} color="#9ca3af" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="kullanici_adi"
                                                placeholderTextColor="#6b7280"
                                                value={formData.kullaniciAdi}
                                                onChangeText={(v) => updateField('kullaniciAdi', v)}
                                                autoCapitalize="none"
                                            />
                                        </View>
                                    </View>

                                    {/* Email */}
                                    <View className="mb-3">
                                        <Text className="text-gray-300 text-sm mb-1">E-posta *</Text>
                                        <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                            <Mail size={18} color="#9ca3af" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="email@example.com"
                                                placeholderTextColor="#6b7280"
                                                value={formData.email}
                                                onChangeText={(v) => updateField('email', v)}
                                                keyboardType="email-address"
                                                autoCapitalize="none"
                                            />
                                        </View>
                                    </View>

                                    {/* Phone */}
                                    <View className="mb-3">
                                        <Text className="text-gray-300 text-sm mb-1">Telefon</Text>
                                        <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                            <Phone size={18} color="#9ca3af" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="05XX XXX XX XX"
                                                placeholderTextColor="#6b7280"
                                                value={formData.telefon}
                                                onChangeText={(v) => updateField('telefon', v)}
                                                keyboardType="phone-pad"
                                            />
                                        </View>
                                    </View>

                                    {/* Password */}
                                    <View className="mb-3">
                                        <Text className="text-gray-300 text-sm mb-1">Şifre *</Text>
                                        <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                            <Lock size={18} color="#9ca3af" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="••••••••"
                                                placeholderTextColor="#6b7280"
                                                value={formData.sifre}
                                                onChangeText={(v) => updateField('sifre', v)}
                                                secureTextEntry={!showPassword}
                                            />
                                            <TouchableOpacity onPress={() => setShowPassword(!showPassword)}>
                                                {showPassword ? <EyeOff size={18} color="#9ca3af" /> : <Eye size={18} color="#9ca3af" />}
                                            </TouchableOpacity>
                                        </View>
                                    </View>

                                    {/* Password Confirm */}
                                    <View className="mb-4">
                                        <Text className="text-gray-300 text-sm mb-1">Şifre Tekrar *</Text>
                                        <View className="flex-row items-center bg-white/10 rounded-xl px-3 py-2.5">
                                            <Lock size={18} color="#9ca3af" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="••••••••"
                                                placeholderTextColor="#6b7280"
                                                value={formData.sifreTekrar}
                                                onChangeText={(v) => updateField('sifreTekrar', v)}
                                                secureTextEntry={!showPassword}
                                            />
                                        </View>
                                    </View>

                                    {/* Address */}
                                    <View className="mb-5">
                                        <Text className="text-gray-300 text-sm mb-1">Adres</Text>
                                        <View className="flex-row items-start bg-white/10 rounded-xl px-3 py-2.5">
                                            <MapPin size={18} color="#9ca3af" className="mt-1" />
                                            <TextInput
                                                className="flex-1 ml-2 text-white"
                                                placeholder="Teslimat adresi"
                                                placeholderTextColor="#6b7280"
                                                value={formData.adres}
                                                onChangeText={(v) => updateField('adres', v)}
                                                multiline
                                                numberOfLines={2}
                                            />
                                        </View>
                                    </View>

                                    {/* Register Button */}
                                    <TouchableOpacity
                                        onPress={handleRegister}
                                        disabled={isLoading}
                                        className={`${formData.rol === 'MAGAZA_SAHIBI' ? 'bg-accent-600' : 'bg-primary-600'} rounded-xl py-3.5 items-center`}
                                    >
                                        {isLoading ? (
                                            <ActivityIndicator color="white" />
                                        ) : (
                                            <Text className="text-white font-semibold text-lg">
                                                {formData.rol === 'MAGAZA_SAHIBI' ? 'Mağaza Sahibi Ol' : 'Kayıt Ol'}
                                            </Text>
                                        )}
                                    </TouchableOpacity>
                                </View>
                            </BlurView>
                        </MotiView>

                        {/* Login Link */}
                        <View className="mt-6 flex-row justify-center">
                            <Text className="text-gray-400">Zaten hesabınız var mı? </Text>
                            <TouchableOpacity onPress={() => router.back()}>
                                <Text className="text-primary-400 font-semibold">Giriş Yapın</Text>
                            </TouchableOpacity>
                        </View>
                    </View>
                </ScrollView>
            </KeyboardAvoidingView>
        </View>
    );
}
