import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    ScrollView,
    Alert,
    ActivityIndicator,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useMutation } from '@tanstack/react-query';
import {
    User,
    Lock,
    Store,
    Shield,
    LogIn,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { authApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

type TabType = 'musteri' | 'magaza' | 'admin';

export default function LoginScreen() {
    const [activeTab, setActiveTab] = useState<TabType>('musteri');
    const [kullaniciAdi, setKullaniciAdi] = useState('');
    const [sifre, setSifre] = useState('');
    const { setUser } = useAuthStore();

    const loginMutation = useMutation({
        mutationFn: () => authApi.login(kullaniciAdi, sifre),
        onSuccess: async (data) => {
            // Backend 'kullanici' döndürüyor, biz 'user' olarak kullanıyoruz
            const user = data.kullanici || data.user;
            const token = data.token;

            console.log('[Login] Response data:', JSON.stringify(data, null, 2));
            console.log('[Login] User:', user);
            console.log('[Login] Token:', token);

            if (!user || !token) {
                Alert.alert('Hata', 'Sunucudan geçersiz yanıt alındı');
                return;
            }

            setUser(user, token);
            console.log('[Login] User and token saved to store');

            // Role göre yönlendirme
            if (user.rol === 'ADMIN') {
                router.replace('/admin');
            } else if (user.rol === 'MAGAZA_SAHIBI') {
                router.replace('/sahip' as any);
            } else {
                router.replace('/(tabs)');
            }
        },
        onError: (error: any) => {
            console.log('Login error:', error);
            Alert.alert(
                'Giriş Başarısız',
                error.response?.data?.error || error.message || 'Kullanıcı adı veya şifre hatalı!'
            );
        },
    });

    const handleLogin = () => {
        if (!kullaniciAdi.trim() || !sifre.trim()) {
            Alert.alert('Hata', 'Lütfen kullanıcı adı ve şifre girin');
            return;
        }
        loginMutation.mutate();
    };

    const tabs = [
        { id: 'musteri' as TabType, label: 'Müşteri', icon: User, desc: 'Alışveriş yapmak için giriş yapın' },
        { id: 'magaza' as TabType, label: 'Mağaza', icon: Store, desc: 'Mağazanızı yönetmek için giriş yapın' },
        { id: 'admin' as TabType, label: 'Admin', icon: Shield, desc: 'Sistem yönetimi için giriş yapın' },
    ];

    const currentTab = tabs.find(t => t.id === activeTab)!;

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            {/* Navbar */}
            <View className="bg-white/90 h-20 flex-row items-center justify-between px-6 border-b border-gray-200">
                <TouchableOpacity onPress={() => router.push('/')} className="flex-row items-center">
                    <Store size={24} color="#667eea" />
                    <Text className="text-primary-500 font-extrabold text-xl ml-2">Mağaza Sistemi</Text>
                </TouchableOpacity>
                <TouchableOpacity onPress={() => router.push('/(auth)/register')}>
                    <Text className="text-gray-600 font-medium">Üye Ol</Text>
                </TouchableOpacity>
            </View>

            <ScrollView
                contentContainerStyle={{ flexGrow: 1, justifyContent: 'center', padding: 20 }}
                keyboardShouldPersistTaps="handled"
            >
                {/* Auth Card - Web ile aynı */}
                <MotiView
                    from={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    className="bg-white rounded-3xl p-8 shadow-xl mx-auto w-full max-w-md"
                    style={{ elevation: 10 }}
                >
                    {/* TABS - Web ile aynı */}
                    <View className="flex-row border-b-2 border-gray-200 mb-6">
                        {tabs.map((tab) => (
                            <TouchableOpacity
                                key={tab.id}
                                onPress={() => setActiveTab(tab.id)}
                                className={`flex-1 py-4 items-center border-b-2 -mb-0.5 ${activeTab === tab.id ? 'border-primary-500' : 'border-transparent'
                                    }`}
                            >
                                <View className="flex-row items-center">
                                    <tab.icon
                                        size={16}
                                        color={activeTab === tab.id ? '#667eea' : '#a0aec0'}
                                    />
                                    <Text className={`ml-2 font-semibold ${activeTab === tab.id ? 'text-primary-500' : 'text-gray-500'
                                        }`}>
                                        {tab.label}
                                    </Text>
                                </View>
                            </TouchableOpacity>
                        ))}
                    </View>

                    {/* Header */}
                    <MotiView
                        key={activeTab}
                        from={{ opacity: 0, translateY: 10 }}
                        animate={{ opacity: 1, translateY: 0 }}
                        className="items-center mb-8"
                    >
                        <Text className="text-dark-900 text-2xl font-extrabold mb-2">
                            {activeTab === 'musteri' && 'Müşteri Girişi'}
                            {activeTab === 'magaza' && 'Mağaza Yöneticisi'}
                            {activeTab === 'admin' && 'Yönetici Girişi'}
                        </Text>
                        <Text className="text-gray-600 text-center">{currentTab.desc}</Text>
                    </MotiView>

                    {/* Form */}
                    <View className="mb-6">
                        <Text className="text-dark-900 font-semibold mb-2">Kullanıcı Adı</Text>
                        <View className="bg-white border-2 border-gray-300 rounded-2xl px-4 py-4 flex-row items-center">
                            <User size={20} color="#a0aec0" />
                            <TextInput
                                className="flex-1 ml-3 text-dark-900 text-base"
                                placeholder={
                                    activeTab === 'musteri' ? 'Müşteri adınız' :
                                        activeTab === 'magaza' ? 'Mağaza kullanıcı adınız' :
                                            'Admin kullanıcı adınız'
                                }
                                placeholderTextColor="#a0aec0"
                                value={kullaniciAdi}
                                onChangeText={setKullaniciAdi}
                                autoCapitalize="none"
                            />
                        </View>
                    </View>

                    <View className="mb-6">
                        <Text className="text-dark-900 font-semibold mb-2">Şifre</Text>
                        <View className="bg-white border-2 border-gray-300 rounded-2xl px-4 py-4 flex-row items-center">
                            <Lock size={20} color="#a0aec0" />
                            <TextInput
                                className="flex-1 ml-3 text-dark-900 text-base"
                                placeholder="******"
                                placeholderTextColor="#a0aec0"
                                value={sifre}
                                onChangeText={setSifre}
                                secureTextEntry
                            />
                        </View>
                    </View>

                    {/* Submit Button */}
                    <TouchableOpacity
                        onPress={handleLogin}
                        disabled={loginMutation.isPending}
                    >
                        <LinearGradient
                            colors={['#667eea', '#764ba2']}
                            start={{ x: 0, y: 0 }}
                            end={{ x: 1, y: 0 }}
                            className="py-4 rounded-2xl flex-row items-center justify-center"
                        >
                            {loginMutation.isPending ? (
                                <ActivityIndicator color="white" />
                            ) : (
                                <>
                                    <LogIn size={20} color="white" />
                                    <Text className="text-white font-bold text-lg ml-2">Giriş Yap</Text>
                                </>
                            )}
                        </LinearGradient>
                    </TouchableOpacity>

                    {/* Register Link - sadece müşteri tabında */}
                    {activeTab !== 'admin' && (
                        <View className="mt-6 pt-6 border-t border-gray-200 items-center">
                            <Text className="text-gray-500">
                                Hesabın yok mu?{' '}
                                <Text
                                    onPress={() => router.push('/(auth)/register')}
                                    className="text-primary-500 font-semibold"
                                >
                                    Kaydol
                                </Text>
                            </Text>
                        </View>
                    )}
                </MotiView>
            </ScrollView>
        </View>
    );
}
