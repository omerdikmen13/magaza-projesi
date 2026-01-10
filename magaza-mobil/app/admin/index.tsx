import React from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    RefreshControl,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
    ArrowLeft,
    Users,
    Store,
    Package,
    TrendingUp,
    ShoppingCart,
    Clock,
    DollarSign,
    MessageCircle,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { adminApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function AdminDashboard() {
    const { data: dashboard, isLoading, refetch } = useQuery({
        queryKey: ['admin-dashboard'],
        queryFn: adminApi.getDashboard,
    });

    const stats = [
        { title: 'Kullanıcılar', value: dashboard?.toplamKullanici || 0, icon: Users, color: '#3b82f6' },
        { title: 'Mağazalar', value: dashboard?.toplamMagaza || 0, icon: Store, color: '#10b981' },
        { title: 'Ürünler', value: dashboard?.toplamUrun || 0, icon: Package, color: '#f59e0b' },
        { title: 'Siparişler', value: dashboard?.toplamSiparis || 0, icon: ShoppingCart, color: '#ef4444' },
    ];

    const menuItems = [
        { title: 'Kullanıcı Yönetimi', icon: Users, route: '/admin/users', color: '#3b82f6' },
        { title: 'Mağaza Yönetimi', icon: Store, route: '/admin/stores', color: '#10b981' },
        { title: 'Sipariş Yönetimi', icon: ShoppingCart, route: '/admin/orders', color: '#ef4444' },
        { title: 'Mesaj Yönetimi', icon: MessageCircle, route: '/admin/messages', color: '#8b5cf6' },
        { title: 'Ciro Raporu', icon: TrendingUp, route: '/admin/ciro', color: '#f59e0b' },
    ];

    return (
        <View className="flex-1 bg-dark-300">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5 flex-row items-center">
                <TouchableOpacity
                    onPress={() => router.back()}
                    className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                >
                    <ArrowLeft size={22} color="white" />
                </TouchableOpacity>
                <Text className="text-white text-2xl font-bold">Admin Paneli</Text>
            </View>

            <ScrollView
                contentContainerStyle={{ paddingBottom: 100 }}
                refreshControl={
                    <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#d946ef" />
                }
            >
                {/* Stats Grid */}
                <View className="px-5 mb-6">
                    <View className="flex-row flex-wrap gap-3">
                        {stats.map((stat, index) => (
                            <MotiView
                                key={stat.title}
                                from={{ opacity: 0, scale: 0.9 }}
                                animate={{ opacity: 1, scale: 1 }}
                                transition={{ delay: index * 100 }}
                                className="flex-1 min-w-[45%] bg-white/5 rounded-2xl p-4"
                            >
                                <View className="flex-row items-center justify-between">
                                    <View
                                        className="w-10 h-10 rounded-xl items-center justify-center"
                                        style={{ backgroundColor: `${stat.color}20` }}
                                    >
                                        <stat.icon size={20} color={stat.color} />
                                    </View>
                                    <Text className="text-white text-2xl font-bold">{stat.value}</Text>
                                </View>
                                <Text className="text-gray-400 mt-2">{stat.title}</Text>
                            </MotiView>
                        ))}
                    </View>
                </View>

                {/* Revenue Card */}
                <MotiView
                    from={{ opacity: 0, translateY: 20 }}
                    animate={{ opacity: 1, translateY: 0 }}
                    transition={{ delay: 400 }}
                    className="mx-5 mb-6"
                >
                    <View className="bg-gradient-to-br from-primary-600/30 to-accent-600/30 rounded-2xl p-5">
                        <View className="flex-row items-center justify-between">
                            <View>
                                <Text className="text-gray-300">Toplam Ciro</Text>
                                <Text className="text-white text-3xl font-bold mt-1">
                                    ₺{dashboard?.toplamCiro?.toLocaleString('tr-TR') || 0}
                                </Text>
                            </View>
                            <View className="w-14 h-14 rounded-full bg-white/10 items-center justify-center">
                                <TrendingUp size={28} color="#d946ef" />
                            </View>
                        </View>
                        <View className="flex-row mt-4 gap-4">
                            <View className="flex-row items-center">
                                <Clock size={14} color="#facc15" />
                                <Text className="text-yellow-400 ml-1 text-sm">
                                    {dashboard?.bekleyenSiparis || 0} bekleyen
                                </Text>
                            </View>
                        </View>
                    </View>
                </MotiView>

                {/* Menu Items */}
                <View className="px-5">
                    <Text className="text-gray-400 mb-3">Yönetim</Text>
                    {menuItems.map((item, index) => (
                        <MotiView
                            key={item.title}
                            from={{ opacity: 0, translateX: -20 }}
                            animate={{ opacity: 1, translateX: 0 }}
                            transition={{ delay: 500 + index * 100 }}
                        >
                            <TouchableOpacity
                                onPress={() => router.push(item.route as any)}
                                className="flex-row items-center bg-white/5 rounded-xl p-4 mb-3"
                            >
                                <View
                                    className="w-10 h-10 rounded-xl items-center justify-center"
                                    style={{ backgroundColor: `${item.color}20` }}
                                >
                                    <item.icon size={20} color={item.color} />
                                </View>
                                <Text className="flex-1 text-white ml-3 font-medium">{item.title}</Text>
                                <ArrowLeft size={18} color="#6b7280" style={{ transform: [{ rotate: '180deg' }] }} />
                            </TouchableOpacity>
                        </MotiView>
                    ))}
                </View>

                {/* Recent Orders */}
                {dashboard?.sonSiparisler && dashboard.sonSiparisler.length > 0 && (
                    <View className="px-5 mt-4">
                        <Text className="text-gray-400 mb-3">Son Siparişler</Text>
                        {dashboard.sonSiparisler.map((siparis: any, index: number) => (
                            <MotiView
                                key={siparis.id}
                                from={{ opacity: 0, translateY: 10 }}
                                animate={{ opacity: 1, translateY: 0 }}
                                transition={{ delay: 800 + index * 100 }}
                                className="bg-white/5 rounded-xl p-4 mb-2"
                            >
                                <View className="flex-row items-center justify-between">
                                    <View>
                                        <Text className="text-white font-medium">#{siparis.id}</Text>
                                        <Text className="text-gray-400 text-sm">{siparis.magazaAd}</Text>
                                    </View>
                                    <View className="items-end">
                                        <Text className="text-primary-400 font-bold">₺{siparis.toplamTutar}</Text>
                                        <Text className={`text-xs ${siparis.durum === 'BEKLEMEDE' ? 'text-yellow-400' :
                                            siparis.durum === 'TESLIM_EDILDI' ? 'text-green-400' : 'text-gray-400'
                                            }`}>
                                            {siparis.durum}
                                        </Text>
                                    </View>
                                </View>
                            </MotiView>
                        ))}
                    </View>
                )}
            </ScrollView>
        </View>
    );
}
