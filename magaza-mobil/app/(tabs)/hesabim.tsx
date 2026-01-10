import React from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    Alert,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import {
    User,
    Users,
    Package,
    Store,
    Shield,
    LogOut,
    ChevronRight,
    Settings,
    MessageCircle,
    ShoppingCart,
    TrendingUp,
    BarChart3,
    Heart,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { useAuthStore } from '../../stores/authStore';

export default function HesabimScreen() {
    const { user, isAuthenticated, logout, isAdmin, isMagazaSahibi } = useAuthStore();

    const handleLogout = () => {
        Alert.alert('Çıkış Yap', 'Çıkış yapmak istediğinize emin misiniz?', [
            { text: 'İptal', style: 'cancel' },
            {
                text: 'Çıkış Yap',
                style: 'destructive',
                onPress: () => {
                    logout();
                    router.replace('/(tabs)');
                },
            },
        ]);
    };

    // Giriş yapmamışsa
    if (!isAuthenticated) {
        return (
            <View className="flex-1 bg-gray-100 items-center justify-center px-6">
                <StatusBar barStyle="dark-content" />
                <View className="w-24 h-24 rounded-full bg-gray-200 items-center justify-center mb-4">
                    <User size={48} color="#a0aec0" />
                </View>
                <Text className="text-gray-900 text-2xl font-bold">Hesabınız Yok mu?</Text>
                <Text className="text-gray-500 text-center mt-2">
                    Giriş yapın veya kayıt olun
                </Text>
                <TouchableOpacity
                    onPress={() => router.push('/(auth)/login')}
                    className="mt-6"
                >
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="px-8 py-4 rounded-2xl"
                    >
                        <Text className="text-white font-bold text-lg">Giriş Yap</Text>
                    </LinearGradient>
                </TouchableOpacity>
                <TouchableOpacity
                    onPress={() => router.push('/(auth)/register')}
                    className="mt-3"
                >
                    <Text className="text-primary-500 font-semibold">Kayıt Ol</Text>
                </TouchableOpacity>
            </View>
        );
    }

    // ADMIN için özel görünüm
    if (isAdmin()) {
        const adminMenuItems = [
            { icon: Users, title: 'Kullanıcı Yönetimi', route: '/admin/users', color: '#3b82f6' },
            { icon: Store, title: 'Mağaza Yönetimi', route: '/admin/stores', color: '#10b981' },
            { icon: Package, title: 'Ürün Yönetimi', route: '/admin/stores', color: '#8b5cf6', desc: 'Mağaza seç' },
            { icon: ShoppingCart, title: 'Sipariş Yönetimi', route: '/admin/orders', color: '#ef4444' },
            { icon: MessageCircle, title: 'Mesaj Yönetimi', route: '/admin/messages', color: '#a855f7' },
            { icon: TrendingUp, title: 'Ciro Raporu', route: '/admin/ciro', color: '#f59e0b' },
        ];

        return (
            <View className="flex-1 bg-dark-300">
                <StatusBar barStyle="light-content" />
                <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

                <ScrollView contentContainerStyle={{ paddingBottom: 100 }}>
                    {/* Admin Header */}
                    <LinearGradient
                        colors={['#d946ef', '#9333ea']}
                        className="pt-16 pb-8 px-5"
                    >
                        <MotiView
                            from={{ opacity: 0, scale: 0.9 }}
                            animate={{ opacity: 1, scale: 1 }}
                            className="items-center"
                        >
                            <View className="w-24 h-24 rounded-full bg-white/20 items-center justify-center mb-3">
                                <Shield size={40} color="white" />
                            </View>
                            <Text className="text-white text-xl font-bold">
                                {user?.ad} {user?.soyad}
                            </Text>
                            <Text className="text-white/80">@{user?.kullaniciAdi}</Text>
                            <View className="mt-2 px-4 py-1 bg-white/20 rounded-full">
                                <Text className="text-white text-sm font-bold">👑 ADMIN</Text>
                            </View>
                        </MotiView>
                    </LinearGradient>

                    {/* Admin Dashboard Button */}
                    <View className="px-5 pt-6">
                        <MotiView
                            from={{ opacity: 0, translateY: 20 }}
                            animate={{ opacity: 1, translateY: 0 }}
                        >
                            <TouchableOpacity
                                onPress={() => router.push('/admin')}
                                className="mb-4"
                            >
                                <LinearGradient
                                    colors={['#d946ef', '#9333ea']}
                                    className="rounded-2xl p-4 flex-row items-center"
                                >
                                    <View className="w-12 h-12 rounded-full bg-white/20 items-center justify-center">
                                        <BarChart3 size={24} color="white" />
                                    </View>
                                    <View className="flex-1 ml-3">
                                        <Text className="text-white font-bold text-lg">Admin Dashboard</Text>
                                        <Text className="text-white/80 text-sm">Tüm istatistikleri gör</Text>
                                    </View>
                                    <ChevronRight size={24} color="white" />
                                </LinearGradient>
                            </TouchableOpacity>
                        </MotiView>

                        {/* Quick Actions */}
                        <Text className="text-gray-400 text-sm mb-3 font-medium">HIZLI ERİŞİM</Text>
                        <View className="flex-row flex-wrap gap-3 mb-6">
                            {adminMenuItems.map((item, index) => (
                                <MotiView
                                    key={item.title}
                                    from={{ opacity: 0, scale: 0.9 }}
                                    animate={{ opacity: 1, scale: 1 }}
                                    transition={{ delay: index * 100 }}
                                    style={{ width: '48%' }}
                                >
                                    <TouchableOpacity
                                        onPress={() => router.push(item.route as any)}
                                        className="bg-white/5 rounded-2xl p-4"
                                    >
                                        <View
                                            className="w-10 h-10 rounded-full items-center justify-center mb-2"
                                            style={{ backgroundColor: `${item.color}20` }}
                                        >
                                            <item.icon size={20} color={item.color} />
                                        </View>
                                        <Text className="text-white font-medium">{item.title}</Text>
                                    </TouchableOpacity>
                                </MotiView>
                            ))}
                        </View>

                        {/* Other Options */}
                        <Text className="text-gray-400 text-sm mb-3 font-medium">DİĞER</Text>
                        <TouchableOpacity
                            onPress={() => router.push('/ayarlar')}
                            className="flex-row items-center bg-white/5 rounded-2xl p-4 mb-3"
                        >
                            <View className="w-10 h-10 rounded-full bg-gray-500/20 items-center justify-center">
                                <Settings size={20} color="#9ca3af" />
                            </View>
                            <Text className="flex-1 text-white ml-3 font-medium">Ayarlar</Text>
                            <ChevronRight size={20} color="#6b7280" />
                        </TouchableOpacity>

                        {/* Çıkış Butonu */}
                        <MotiView
                            from={{ opacity: 0, translateX: -20 }}
                            animate={{ opacity: 1, translateX: 0 }}
                            transition={{ delay: 500 }}
                        >
                            <TouchableOpacity
                                onPress={handleLogout}
                                className="flex-row items-center bg-red-500/10 rounded-2xl p-4 mt-4"
                            >
                                <View className="w-10 h-10 rounded-full bg-red-500/20 items-center justify-center">
                                    <LogOut size={20} color="#ef4444" />
                                </View>
                                <Text className="flex-1 text-red-400 ml-3 font-medium">Çıkış Yap</Text>
                            </TouchableOpacity>
                        </MotiView>
                    </View>
                </ScrollView>
            </View>
        );
    }

    // Normal kullanıcı için menu
    const menuItems = [
        { icon: User, title: 'Profil Düzenle', route: '/profil-duzenle', show: true },
        { icon: Heart, title: 'Favorilerim', route: '/favorilerim', show: true },
        { icon: Package, title: 'Siparişlerim', route: '/siparislerim', show: true },
        { icon: MessageCircle, title: 'Mesajlarım', route: '/sohbet', show: true },
        { icon: Settings, title: 'Hesap Ayarları', route: '/ayarlar', show: true },
        { icon: Store, title: 'Mağaza Paneli', route: '/sahip', show: isMagazaSahibi() },
    ];

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            <ScrollView contentContainerStyle={{ paddingBottom: 100 }}>
                {/* Profil Header */}
                <LinearGradient
                    colors={['#667eea', '#764ba2']}
                    className="pt-16 pb-8 px-5"
                >
                    <MotiView
                        from={{ opacity: 0, scale: 0.9 }}
                        animate={{ opacity: 1, scale: 1 }}
                        className="items-center"
                    >
                        <View className="w-24 h-24 rounded-full bg-white/20 items-center justify-center mb-3">
                            <Text className="text-white text-3xl font-bold">
                                {user?.ad?.charAt(0)}{user?.soyad?.charAt(0)}
                            </Text>
                        </View>
                        <Text className="text-white text-xl font-bold">
                            {user?.ad} {user?.soyad}
                        </Text>
                        <Text className="text-white/80">@{user?.kullaniciAdi}</Text>
                        <View className="mt-2 px-3 py-1 bg-white/20 rounded-full">
                            <Text className="text-white text-sm">{user?.rol}</Text>
                        </View>
                    </MotiView>
                </LinearGradient>

                {/* Menu Items */}
                <View className="px-5 pt-6">
                    {menuItems
                        .filter((item) => item.show)
                        .map((item, index) => (
                            <MotiView
                                key={item.title}
                                from={{ opacity: 0, translateX: -20 }}
                                animate={{ opacity: 1, translateX: 0 }}
                                transition={{ delay: index * 100 }}
                            >
                                <TouchableOpacity
                                    onPress={() => item.route && router.push(item.route as any)}
                                    className="flex-row items-center bg-white rounded-2xl p-4 mb-3 shadow-sm"
                                    style={{ elevation: 2 }}
                                >
                                    <View className="w-10 h-10 rounded-full bg-primary-500/10 items-center justify-center">
                                        <item.icon size={20} color="#667eea" />
                                    </View>
                                    <Text className="flex-1 text-gray-900 ml-3 font-medium">{item.title}</Text>
                                    <ChevronRight size={20} color="#a0aec0" />
                                </TouchableOpacity>
                            </MotiView>
                        ))}

                    {/* Çıkış Butonu */}
                    <MotiView
                        from={{ opacity: 0, translateX: -20 }}
                        animate={{ opacity: 1, translateX: 0 }}
                        transition={{ delay: 500 }}
                    >
                        <TouchableOpacity
                            onPress={handleLogout}
                            className="flex-row items-center bg-red-50 rounded-2xl p-4 mt-4"
                        >
                            <View className="w-10 h-10 rounded-full bg-red-100 items-center justify-center">
                                <LogOut size={20} color="#e53e3e" />
                            </View>
                            <Text className="flex-1 text-red-500 ml-3 font-medium">Çıkış Yap</Text>
                        </TouchableOpacity>
                    </MotiView>
                </View>
            </ScrollView>
        </View>
    );
}
