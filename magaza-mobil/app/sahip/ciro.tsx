import React, { useState } from 'react';
import {
    View,
    Text,
    ScrollView,
    TouchableOpacity,
    StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
    ArrowLeft,
    TrendingUp,
    DollarSign,
    ShoppingCart,
    Calendar,
    BarChart3,
    Clock,
    CheckCircle,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi } from '../../services/api';

type Period = 'daily' | 'weekly' | 'monthly' | 'yearly';

export default function SahipCiroScreen() {
    const [selectedPeriod, setSelectedPeriod] = useState<Period>('monthly');

    const { data: panel } = useQuery({
        queryKey: ['sahip-panel'],
        queryFn: magazaSahibiApi.getPanel,
    });

    const magazalar = panel?.magazalar || [];
    const toplamCiro = magazalar.reduce((t: number, m: any) => t + (m.toplamCiro || 0), 0);
    const toplamSiparis = magazalar.reduce((t: number, m: any) => t + (m.siparisSayisi || 0), 0);

    // Dönem bazlı simülasyon (gerçek backend'den gelmeli)
    const getPeriodData = () => {
        switch (selectedPeriod) {
            case 'daily':
                return { ciro: toplamCiro * 0.04, siparis: Math.round(toplamSiparis * 0.04), label: 'Bugün' };
            case 'weekly':
                return { ciro: toplamCiro * 0.25, siparis: Math.round(toplamSiparis * 0.25), label: 'Bu Hafta' };
            case 'monthly':
                return { ciro: toplamCiro * 0.6, siparis: Math.round(toplamSiparis * 0.6), label: 'Bu Ay' };
            case 'yearly':
                return { ciro: toplamCiro, siparis: toplamSiparis, label: 'Bu Yıl' };
            default:
                return { ciro: toplamCiro, siparis: toplamSiparis, label: 'Toplam' };
        }
    };

    const periodData = getPeriodData();

    const periods: { key: Period; label: string; icon: any }[] = [
        { key: 'daily', label: 'Günlük', icon: Clock },
        { key: 'weekly', label: 'Haftalık', icon: Calendar },
        { key: 'monthly', label: 'Aylık', icon: BarChart3 },
        { key: 'yearly', label: 'Yıllık', icon: TrendingUp },
    ];

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
                    <View className="flex-1">
                        <Text className="text-gray-900 text-xl font-bold">📊 Ciro Raporu</Text>
                        <Text className="text-gray-500 text-sm">Gelir analizi</Text>
                    </View>
                </View>
            </View>

            <ScrollView className="flex-1 px-5 py-4" contentContainerStyle={{ paddingBottom: 40 }}>
                {/* Period Selector */}
                <View className="flex-row gap-2 mb-6">
                    {periods.map((period) => {
                        const Icon = period.icon;
                        const isSelected = selectedPeriod === period.key;
                        return (
                            <TouchableOpacity
                                key={period.key}
                                onPress={() => setSelectedPeriod(period.key)}
                                className={`flex-1 py-3 rounded-xl items-center ${isSelected ? '' : 'bg-white'}`}
                                style={!isSelected ? { elevation: 1 } : undefined}
                            >
                                {isSelected ? (
                                    <LinearGradient
                                        colors={['#667eea', '#764ba2']}
                                        className="absolute inset-0 rounded-xl"
                                    />
                                ) : null}
                                <Icon size={18} color={isSelected ? 'white' : '#667eea'} />
                                <Text className={`text-xs font-medium mt-1 ${isSelected ? 'text-white' : 'text-gray-600'}`}>
                                    {period.label}
                                </Text>
                            </TouchableOpacity>
                        );
                    })}
                </View>

                {/* Main Ciro Card */}
                <MotiView
                    from={{ opacity: 0, scale: 0.95 }}
                    animate={{ opacity: 1, scale: 1 }}
                    key={selectedPeriod}
                >
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="rounded-3xl p-6 mb-6"
                    >
                        <View className="flex-row items-center mb-2">
                            <TrendingUp size={24} color="white" />
                            <Text className="text-white/80 ml-2">{periodData.label} Ciro</Text>
                        </View>
                        <Text className="text-white text-5xl font-extrabold">
                            ₺{periodData.ciro.toFixed(2)}
                        </Text>
                        <View className="flex-row items-center mt-3">
                            <CheckCircle size={16} color="#38ef7d" />
                            <Text className="text-white/80 ml-2">
                                {periodData.siparis} sipariş tamamlandı
                            </Text>
                        </View>
                    </LinearGradient>
                </MotiView>

                {/* Stats Grid */}
                <View className="flex-row gap-3 mb-6">
                    <MotiView
                        from={{ opacity: 0, translateX: -20 }}
                        animate={{ opacity: 1, translateX: 0 }}
                        transition={{ delay: 100 }}
                        className="flex-1"
                    >
                        <View className="bg-white rounded-2xl p-4 shadow-sm" style={{ elevation: 2 }}>
                            <DollarSign size={24} color="#667eea" />
                            <Text className="text-gray-500 text-sm mt-2">Ortalama Sipariş</Text>
                            <Text className="text-gray-900 font-bold text-xl">
                                ₺{periodData.siparis > 0 ? (periodData.ciro / periodData.siparis).toFixed(2) : '0.00'}
                            </Text>
                        </View>
                    </MotiView>
                    <MotiView
                        from={{ opacity: 0, translateX: 20 }}
                        animate={{ opacity: 1, translateX: 0 }}
                        transition={{ delay: 150 }}
                        className="flex-1"
                    >
                        <View className="bg-white rounded-2xl p-4 shadow-sm" style={{ elevation: 2 }}>
                            <ShoppingCart size={24} color="#38a169" />
                            <Text className="text-gray-500 text-sm mt-2">Sipariş Sayısı</Text>
                            <Text className="text-gray-900 font-bold text-xl">{periodData.siparis}</Text>
                        </View>
                    </MotiView>
                </View>

                {/* Comparison */}
                <View className="bg-white rounded-2xl p-5 shadow-sm mb-6" style={{ elevation: 2 }}>
                    <Text className="text-gray-900 font-bold text-lg mb-4">📈 Karşılaştırma</Text>
                    <View className="flex-row items-center justify-between py-3 border-b border-gray-100">
                        <Text className="text-gray-600">Günlük</Text>
                        <Text className="text-gray-900 font-bold">₺{(toplamCiro * 0.04).toFixed(2)}</Text>
                    </View>
                    <View className="flex-row items-center justify-between py-3 border-b border-gray-100">
                        <Text className="text-gray-600">Haftalık</Text>
                        <Text className="text-gray-900 font-bold">₺{(toplamCiro * 0.25).toFixed(2)}</Text>
                    </View>
                    <View className="flex-row items-center justify-between py-3 border-b border-gray-100">
                        <Text className="text-gray-600">Aylık</Text>
                        <Text className="text-gray-900 font-bold">₺{(toplamCiro * 0.6).toFixed(2)}</Text>
                    </View>
                    <View className="flex-row items-center justify-between py-3">
                        <Text className="text-gray-600">Yıllık</Text>
                        <Text className="text-primary-500 font-bold text-lg">₺{toplamCiro.toFixed(2)}</Text>
                    </View>
                </View>

                {/* Mağaza Bazlı */}
                <Text className="text-gray-900 font-bold text-lg mb-4">🏪 Mağaza Bazlı Ciro</Text>
                {magazalar.map((magaza: any, index: number) => (
                    <MotiView
                        key={magaza.id}
                        from={{ opacity: 0, translateY: 20 }}
                        animate={{ opacity: 1, translateY: 0 }}
                        transition={{ delay: index * 100 }}
                    >
                        <TouchableOpacity
                            onPress={() => router.push(`/sahip/siparisler?magazaId=${magaza.id}`)}
                            className="bg-white rounded-2xl p-4 mb-3 shadow-sm"
                            style={{ elevation: 2 }}
                        >
                            <View className="flex-row items-center justify-between">
                                <View className="flex-1">
                                    <Text className="text-gray-900 font-bold">{magaza.ad}</Text>
                                    <Text className="text-gray-500 text-sm">{magaza.siparisSayisi || 0} sipariş</Text>
                                </View>
                                <View className="items-end">
                                    <Text className="text-primary-500 font-bold text-xl">₺{magaza.toplamCiro || 0}</Text>
                                    <Text className="text-gray-400 text-xs">tıkla → siparişler</Text>
                                </View>
                            </View>
                        </TouchableOpacity>
                    </MotiView>
                ))}
            </ScrollView>
        </View>
    );
}
