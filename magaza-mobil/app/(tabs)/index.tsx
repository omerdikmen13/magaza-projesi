import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  Image,
  RefreshControl,
  StatusBar,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { useQuery } from '@tanstack/react-query';
import {
  Store,
  Sparkles,
  ShoppingBag,
  User,
  Shirt,
  Baby,
  ArrowRight,
  Truck,
  Shield,
  RotateCcw,
  Search,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { kategorilerApi, magazalarApi, urunlerApi } from '../../services/api';
import { useAuthStore } from '../../stores/authStore';

export default function HomeScreen() {
  const { user, isAuthenticated, isAdmin } = useAuthStore();

  const { data: kategoriler = [], isLoading: kategoriLoading } = useQuery({
    queryKey: ['kategoriler'],
    queryFn: kategorilerApi.getAll,
  });

  const { data: magazalar = [], isLoading: magazaLoading, refetch } = useQuery({
    queryKey: ['magazalar'],
    queryFn: magazalarApi.getAll,
  });

  const { data: urunler = [] } = useQuery({
    queryKey: ['urunler'],
    queryFn: urunlerApi.getAll,
  });

  const isLoading = kategoriLoading || magazaLoading;

  const categoryIcons: any = {
    1: User,    // Erkek
    2: Shirt,   // Kadın
    3: Baby,    // Çocuk
  };

  const categoryGradients: any = {
    1: ['#667eea', '#764ba2'],
    2: ['#f093fb', '#f5576c'],
    3: ['#11998e', '#38ef7d'],
  };

  // --- ADMIN VIEW ---
  if (isAuthenticated && isAdmin()) {
    return (
      <View className="flex-1 bg-gray-900">
        <StatusBar barStyle="light-content" />
        <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

        <ScrollView contentContainerStyle={{ paddingBottom: 100 }}>
          {/* Header */}
          <View className="pt-16 pb-8 px-6">
            <Text className="text-white text-3xl font-extrabold">Admin Paneli</Text>
            <Text className="text-gray-400 mt-2">Sistem durumu ve yönetimi</Text>
          </View>

          {/* Stats Grid */}
          <View className="flex-row flex-wrap px-3 mb-8">
            {/* Mağaza Sayısı */}
            <MotiView
              from={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 100 }}
              className="w-1/2 p-3"
            >
              <LinearGradient colors={['#667eea', '#764ba2']} className="p-5 rounded-3xl h-32 justify-between">
                <Store size={24} color="white" />
                <View>
                  <Text className="text-white text-3xl font-bold">{magazalar.length}</Text>
                  <Text className="text-white/80 text-sm font-medium">Toplam Mağaza</Text>
                </View>
              </LinearGradient>
            </MotiView>

            {/* Ürün Sayısı */}
            <MotiView
              from={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ delay: 200 }}
              className="w-1/2 p-3"
            >
              <LinearGradient colors={['#f093fb', '#f5576c']} className="p-5 rounded-3xl h-32 justify-between">
                <Shirt size={24} color="white" />
                <View>
                  <Text className="text-white text-3xl font-bold">{urunler.length}</Text>
                  <Text className="text-white/80 text-sm font-medium">Toplam Ürün</Text>
                </View>
              </LinearGradient>
            </MotiView>
          </View>

          {/* Quick Actions */}
          <View className="px-6 mb-8">
            <Text className="text-white text-xl font-bold mb-4">Hızlı İşlemler</Text>

            <TouchableOpacity
              onPress={() => router.push('/admin/users')}
              className="bg-white/5 border border-white/10 p-4 rounded-2xl mb-3 flex-row items-center"
            >
              <View className="w-10 h-10 rounded-full bg-blue-500/20 items-center justify-center mr-4">
                <User size={20} color="#3b82f6" />
              </View>
              <View className="flex-1">
                <Text className="text-white font-bold text-lg">Kullanıcı Yönetimi</Text>
                <Text className="text-gray-400 text-sm">Kullanıcıları düzenle ve rolleri yönet</Text>
              </View>
              <ArrowRight size={20} color="#6b7280" />
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => router.push('/admin/orders')}
              className="bg-white/5 border border-white/10 p-4 rounded-2xl mb-3 flex-row items-center"
            >
              <View className="w-10 h-10 rounded-full bg-green-500/20 items-center justify-center mr-4">
                <ShoppingBag size={20} color="#22c55e" />
              </View>
              <View className="flex-1">
                <Text className="text-white font-bold text-lg">Sipariş Yönetimi</Text>
                <Text className="text-gray-400 text-sm">Tüm siparişleri görüntüle ve düzenle</Text>
              </View>
              <ArrowRight size={20} color="#6b7280" />
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => router.push('/(tabs)/magazalar')}
              className="bg-white/5 border border-white/10 p-4 rounded-2xl mb-3 flex-row items-center"
            >
              <View className="w-10 h-10 rounded-full bg-purple-500/20 items-center justify-center mr-4">
                <Store size={20} color="#a855f7" />
              </View>
              <View className="flex-1">
                <Text className="text-white font-bold text-lg">Mağaza Yönetimi</Text>
                <Text className="text-gray-400 text-sm">Mağazaları ve ürünlerini yönet</Text>
              </View>
              <ArrowRight size={20} color="#6b7280" />
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => router.push('/admin/messages')}
              className="bg-white/5 border border-white/10 p-4 rounded-2xl mb-3 flex-row items-center"
            >
              <View className="w-10 h-10 rounded-full bg-orange-500/20 items-center justify-center mr-4">
                <User size={20} color="#f97316" />
              </View>
              <View className="flex-1">
                <Text className="text-white font-bold text-lg">Mesaj Yönetimi</Text>
                <Text className="text-gray-400 text-sm">Müşteri ve mağaza sohbetleri</Text>
              </View>
              <ArrowRight size={20} color="#6b7280" />
            </TouchableOpacity>
          </View>
        </ScrollView>
      </View>
    );
  }

  // --- CONSUMER VIEW (Existing) ---
  return (
    <View className="flex-1 bg-gray-100">
      <StatusBar barStyle="light-content" />

      <ScrollView
        contentContainerStyle={{ paddingBottom: 100 }}
        refreshControl={
          <RefreshControl refreshing={isLoading} onRefresh={refetch} tintColor="#667eea" />
        }
      >
        {/* HERO SECTION - LinearGradient */}
        <LinearGradient
          colors={['#667eea', '#764ba2', '#f093fb']}
          start={{ x: 0, y: 0 }}
          end={{ x: 1, y: 1 }}
          className="px-6 pt-16 pb-12"
        >
          <MotiView
            from={{ opacity: 0, translateY: 20 }}
            animate={{ opacity: 1, translateY: 0 }}
            className="items-center"
          >
            <Text className="text-white text-4xl font-extrabold text-center mb-3">
              🛍️ Alışverişin Yeni Adresi
            </Text>
            <Text className="text-white/90 text-lg text-center mb-6 px-4">
              Türkiye'nin en sevilen markalarından binlerce ürün, tek platformda!
            </Text>

            {/* ARAMA BARI */}
            <TouchableOpacity
              onPress={() => router.push('/search' as any)}
              className="w-full bg-white rounded-2xl px-5 py-4 flex-row items-center mb-4 shadow-lg"
            >
              <Search size={20} color="#9ca3af" />
              <Text className="flex-1 ml-3 text-gray-400 text-base">
                Ürün, marka veya kategori ara...
              </Text>
            </TouchableOpacity>

            <TouchableOpacity
              onPress={() => router.push('/(tabs)/magazalar')}
              className="bg-white px-8 py-4 rounded-2xl shadow-lg"
            >
              <View className="flex-row items-center">
                <ShoppingBag size={20} color="#667eea" />
                <Text className="text-primary-500 font-bold ml-2">Alışverişe Başla</Text>
              </View>
            </TouchableOpacity>
          </MotiView>

          {/* Hoş Geldin */}
          {isAuthenticated && user && (
            <MotiView
              from={{ opacity: 0 }}
              animate={{ opacity: 1 }}
              transition={{ delay: 300 }}
              className="mt-6 bg-white/20 rounded-xl px-4 py-3 self-center"
            >
              <Text className="text-white text-center">
                Hoş geldin, <Text className="font-bold">{user.ad}</Text> 👋
              </Text>
            </MotiView>
          )}
        </LinearGradient>

        <View className="px-5 pt-8">
          {/* KATEGORİLER - Web ile aynı */}
          <View className="mb-8">
            <Text className="text-dark-900 text-2xl font-extrabold text-center mb-6">
              <Text className="text-primary-500">🏷️</Text> Kategorilere Göz At
            </Text>

            <View className="flex-row gap-3">
              {kategoriler.slice(0, 3).map((kategori: any, index: number) => {
                const Icon = categoryIcons[kategori.id] || ShoppingBag;
                const colors = categoryGradients[kategori.id] || ['#667eea', '#764ba2'];

                return (
                  <MotiView
                    key={kategori.id}
                    from={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: index * 100 }}
                    className="flex-1"
                  >
                    <TouchableOpacity
                      onPress={() => router.push(`/(tabs)/magazalar?kategoriId=${kategori.id}`)}
                      className="bg-white rounded-3xl p-5 items-center shadow-lg"
                      style={{ elevation: 4 }}
                    >
                      <LinearGradient
                        colors={colors}
                        className="w-20 h-20 rounded-full items-center justify-center mb-4"
                        style={{ elevation: 8 }}
                      >
                        <Icon size={32} color="white" />
                      </LinearGradient>
                      <Text className="text-dark-900 font-bold text-lg">{kategori.ad}</Text>
                      <Text className="text-gray-500 text-sm text-center mt-1">
                        {kategori.id === 1 && 'Tişört, Gömlek, Pantolon'}
                        {kategori.id === 2 && 'Elbise, Bluz, Etek'}
                        {kategori.id === 3 && 'Tişört, Sweatshirt'}
                      </Text>
                    </TouchableOpacity>
                  </MotiView>
                );
              })}
            </View>
          </View>

          {/* TÜM MAĞAZALARI KEŞFET BUTONU */}
          <MotiView
            from={{ opacity: 0, translateY: 10 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: 400 }}
            className="mb-10"
          >
            <TouchableOpacity
              onPress={() => router.push('/(tabs)/magazalar')}
              className="items-center"
            >
              <LinearGradient
                colors={['#667eea', '#764ba2']}
                start={{ x: 0, y: 0 }}
                end={{ x: 1, y: 0 }}
                className="px-8 py-4 rounded-2xl flex-row items-center"
              >
                <Store size={20} color="white" />
                <Text className="text-white font-bold text-lg ml-2">Tüm Mağazaları Keşfet</Text>
              </LinearGradient>
            </TouchableOpacity>
          </MotiView>

          {/* AI ASİSTAN - Web ai-chat-widget */}
          <MotiView
            from={{ opacity: 0, translateY: 20 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: 500 }}
            className="mb-8"
          >
            <TouchableOpacity
              onPress={() => router.push('/ai-assistant')}
              className="bg-white rounded-3xl p-5 shadow-lg flex-row items-center"
              style={{ elevation: 4 }}
            >
              <LinearGradient
                colors={['#667eea', '#764ba2']}
                className="w-14 h-14 rounded-full items-center justify-center"
              >
                <Sparkles size={24} color="white" />
              </LinearGradient>
              <View className="flex-1 ml-4">
                <Text className="text-dark-900 font-bold text-lg">AI Asistan</Text>
                <Text className="text-gray-500">"Ne arıyorsun?" yazarak ürün önerisi al</Text>
              </View>
              <ArrowRight size={20} color="#667eea" />
            </TouchableOpacity>
          </MotiView>

          {/* POPÜLER MARKALAR - Web ile aynı */}
          <View className="bg-white rounded-3xl p-6 mb-8 shadow-sm" style={{ elevation: 2 }}>
            <Text className="text-dark-900 text-xl font-extrabold text-center mb-6">
              <Text className="text-primary-500">⭐</Text> Popüler Markalar
            </Text>

            <View className="flex-row flex-wrap">
              {magazalar.slice(0, 4).map((magaza: any, index: number) => {
                const brandColors: any = {
                  'Mavi': ['#0052cc', '#0066ff'],
                  'Koton': ['#ff6b35', '#ff9a56'],
                  'LC Waikiki': ['#00b894', '#55efc4'],
                  'Zara': ['#e74c3c', '#c0392b'],
                };
                const colors = brandColors[magaza.ad] || ['#667eea', '#764ba2'];

                return (
                  <MotiView
                    key={magaza.id}
                    from={{ opacity: 0, scale: 0.9 }}
                    animate={{ opacity: 1, scale: 1 }}
                    transition={{ delay: 600 + index * 100 }}
                    className="w-1/2 p-2"
                  >
                    <TouchableOpacity
                      onPress={() => router.push(`/magaza/${magaza.id}`)}
                      className="items-center py-4"
                    >
                      <LinearGradient
                        colors={colors}
                        className="w-16 h-16 rounded-full items-center justify-center mb-3"
                      >
                        <Store size={24} color="white" />
                      </LinearGradient>
                      <Text className="text-dark-900 font-bold">{magaza.ad}</Text>
                      <Text className="text-gray-500 text-sm">{magaza.aciklama?.slice(0, 15) || 'Moda'}</Text>
                    </TouchableOpacity>
                  </MotiView>
                );
              })}
            </View>
          </View>

          {/* NEDEN BİZ - Web ile aynı */}
          <LinearGradient
            colors={['rgba(102,126,234,0.08)', 'rgba(118,75,162,0.08)']}
            className="rounded-3xl p-6 mb-8"
          >
            <Text className="text-dark-900 text-xl font-extrabold text-center mb-2">
              🏆 Neden Biz?
            </Text>
            <Text className="text-gray-600 text-center mb-6">
              Güvenli ödeme, hızlı teslimat ve geniş ürün yelpazesi
            </Text>

            <View className="flex-row">
              <View className="flex-1 items-center px-2">
                <LinearGradient
                  colors={['#667eea', '#764ba2']}
                  className="w-14 h-14 rounded-full items-center justify-center mb-3"
                >
                  <Truck size={24} color="white" />
                </LinearGradient>
                <Text className="text-dark-900 font-bold text-center">Hızlı Kargo</Text>
                <Text className="text-gray-500 text-xs text-center">1-3 iş günü</Text>
              </View>
              <View className="flex-1 items-center px-2">
                <LinearGradient
                  colors={['#11998e', '#38ef7d']}
                  className="w-14 h-14 rounded-full items-center justify-center mb-3"
                >
                  <Shield size={24} color="white" />
                </LinearGradient>
                <Text className="text-dark-900 font-bold text-center">Güvenli</Text>
                <Text className="text-gray-500 text-xs text-center">SSL şifreleme</Text>
              </View>
              <View className="flex-1 items-center px-2">
                <LinearGradient
                  colors={['#f093fb', '#f5576c']}
                  className="w-14 h-14 rounded-full items-center justify-center mb-3"
                >
                  <RotateCcw size={24} color="white" />
                </LinearGradient>
                <Text className="text-dark-900 font-bold text-center">Kolay İade</Text>
                <Text className="text-gray-500 text-xs text-center">14 gün</Text>
              </View>
            </View>
          </LinearGradient>

          {/* GİRİŞ YAP CTA - Giriş yapmamışsa */}
          {!isAuthenticated && (
            <MotiView
              from={{ opacity: 0, translateY: 20 }}
              animate={{ opacity: 1, translateY: 0 }}
              transition={{ delay: 800 }}
              className="mb-8"
            >
              <TouchableOpacity
                onPress={() => router.push('/(auth)/login')}
                className="bg-white rounded-3xl p-5 shadow-lg flex-row items-center"
                style={{ elevation: 4 }}
              >
                <View className="w-12 h-12 rounded-full bg-gray-100 items-center justify-center">
                  <User size={24} color="#667eea" />
                </View>
                <View className="flex-1 ml-4">
                  <Text className="text-dark-900 font-bold">Giriş Yap veya Kayıt Ol</Text>
                  <Text className="text-gray-500 text-sm">Sepet ve sipariş özellikleri için</Text>
                </View>
                <ArrowRight size={20} color="#667eea" />
              </TouchableOpacity>
            </MotiView>
          )}
        </View>
      </ScrollView>
    </View>
  );
}
