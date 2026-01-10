import React, { useState, useEffect } from 'react';
import {
    View,
    Text,
    TextInput,
    ScrollView,
    TouchableOpacity,
    Alert,
    StatusBar,
    ActivityIndicator,
    Image,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router, useLocalSearchParams } from 'expo-router';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ArrowLeft, Save, Package, Camera, Plus, Minus, Image as ImageIcon } from 'lucide-react-native';
import * as ExpoImagePicker from 'expo-image-picker';

import { magazaSahibiApi, urunlerApi } from '../../services/api';

export default function UrunEkleScreen() {
    const params = useLocalSearchParams<{ magazaId?: string; urunId?: string }>();
    const magazaId = params.magazaId ? Number(params.magazaId) : 1;
    const urunId = params.urunId ? Number(params.urunId) : null;
    const queryClient = useQueryClient();

    const [form, setForm] = useState({
        ad: '',
        aciklama: '',
        fiyat: '',
        kategoriId: '',
        altKategoriId: '',
        resimUrl: '',
    });

    const [stoklar, setStoklar] = useState<{ beden: string; adet: number }[]>([
        { beden: 'S', adet: 0 },
        { beden: 'M', adet: 0 },
        { beden: 'L', adet: 0 },
        { beden: 'XL', adet: 0 },
    ]);

    const { data: formData } = useQuery({
        queryKey: ['sahip-form-data'],
        queryFn: magazaSahibiApi.getFormData,
    });

    // Mevcut ürün verisini yükle (düzenleme modunda)
    const { data: existingUrun } = useQuery({
        queryKey: ['urun', urunId],
        queryFn: () => urunlerApi.getById(urunId!),
        enabled: !!urunId,
    });

    useEffect(() => {
        if (existingUrun) {
            setForm({
                ad: existingUrun.ad || '',
                aciklama: existingUrun.aciklama || '',
                fiyat: existingUrun.fiyat?.toString() || '',
                kategoriId: existingUrun.kategoriId?.toString() || '',
                altKategoriId: existingUrun.altKategoriId?.toString() || '',
                resimUrl: existingUrun.resimUrl || '',
            });
            // Stokları yükle
            if (existingUrun.stoklar && existingUrun.stoklar.length > 0) {
                setStoklar(existingUrun.stoklar.map((s: any) => ({
                    beden: s.bedenAd || s.beden,
                    adet: s.adet || 0
                })));
            }
        }
    }, [existingUrun]);

    const pickImage = async () => {
        const permissionResult = await ExpoImagePicker.requestMediaLibraryPermissionsAsync();
        if (permissionResult.granted === false) {
            Alert.alert('İzin Gerekli', 'Galeri erişim izni gerekli');
            return;
        }

        const result = await ExpoImagePicker.launchImageLibraryAsync({
            mediaTypes: ExpoImagePicker.MediaTypeOptions.Images,
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.8,
        });

        if (!result.canceled && result.assets[0]) {
            setForm({ ...form, resimUrl: result.assets[0].uri });
        }
    };

    const takePhoto = async () => {
        const permissionResult = await ExpoImagePicker.requestCameraPermissionsAsync();
        if (permissionResult.granted === false) {
            Alert.alert('İzin Gerekli', 'Kamera erişim izni gerekli');
            return;
        }

        const result = await ExpoImagePicker.launchCameraAsync({
            allowsEditing: true,
            aspect: [1, 1],
            quality: 0.8,
        });

        if (!result.canceled && result.assets[0]) {
            setForm({ ...form, resimUrl: result.assets[0].uri });
        }
    };

    const updateStok = (index: number, delta: number) => {
        setStoklar(prev => prev.map((s, i) =>
            i === index ? { ...s, adet: Math.max(0, s.adet + delta) } : s
        ));
    };

    const saveMutation = useMutation({
        mutationFn: (data: any) =>
            urunId
                ? magazaSahibiApi.updateUrun(urunId, data)
                : magazaSahibiApi.addUrun(magazaId, data),
        onSuccess: () => {
            queryClient.invalidateQueries({ queryKey: ['sahip-urunler'] });
            queryClient.invalidateQueries({ queryKey: ['sahip-magaza-urunler'] });
            Alert.alert('✅ Başarılı', urunId ? 'Ürün güncellendi' : 'Ürün eklendi');
            router.back();
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'İşlem başarısız');
        }
    });

    const handleSave = () => {
        if (!form.ad || !form.fiyat) {
            Alert.alert('Hata', 'Ürün adı ve fiyat zorunludur');
            return;
        }
        saveMutation.mutate({
            ad: form.ad,
            aciklama: form.aciklama,
            fiyat: parseFloat(form.fiyat),
            kategoriId: form.kategoriId ? Number(form.kategoriId) : null,
            altKategoriId: form.altKategoriId ? Number(form.altKategoriId) : null,
            resimUrl: form.resimUrl,
            stoklar: stoklar.filter(s => s.adet > 0),
        });
    };

    return (
        <View className="flex-1 bg-gray-100">
            <StatusBar barStyle="dark-content" />

            <View className="bg-white pt-12 pb-4 px-5">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-gray-100 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="#4a5568" />
                    </TouchableOpacity>
                    <Text className="text-gray-900 text-xl font-bold">
                        {urunId ? '✏️ Ürün Düzenle' : '➕ Ürün Ekle'}
                    </Text>
                </View>
            </View>

            <ScrollView className="flex-1 px-5 py-4" contentContainerStyle={{ paddingBottom: 40 }}>
                {/* Resim Seçimi */}
                <View className="bg-white rounded-2xl p-5 shadow-sm mb-4" style={{ elevation: 2 }}>
                    <Text className="text-gray-900 font-bold mb-3">📷 Ürün Görseli</Text>

                    {form.resimUrl ? (
                        <View className="items-center">
                            <Image
                                source={{ uri: form.resimUrl }}
                                className="w-40 h-40 rounded-2xl mb-3"
                                resizeMode="cover"
                            />
                            <TouchableOpacity
                                onPress={() => setForm({ ...form, resimUrl: '' })}
                                className="bg-red-100 px-4 py-2 rounded-xl"
                            >
                                <Text className="text-red-600 font-medium">Resmi Kaldır</Text>
                            </TouchableOpacity>
                        </View>
                    ) : (
                        <View className="flex-row gap-3">
                            <TouchableOpacity
                                onPress={pickImage}
                                className="flex-1 bg-gray-50 border-2 border-dashed border-gray-300 rounded-xl p-6 items-center"
                            >
                                <ImageIcon size={32} color="#667eea" />
                                <Text className="text-gray-600 mt-2 text-center">Galeriden Seç</Text>
                            </TouchableOpacity>
                            <TouchableOpacity
                                onPress={takePhoto}
                                className="flex-1 bg-gray-50 border-2 border-dashed border-gray-300 rounded-xl p-6 items-center"
                            >
                                <Camera size={32} color="#667eea" />
                                <Text className="text-gray-600 mt-2 text-center">Fotoğraf Çek</Text>
                            </TouchableOpacity>
                        </View>
                    )}
                </View>

                {/* Temel Bilgiler */}
                <View className="bg-white rounded-2xl p-5 shadow-sm mb-4" style={{ elevation: 2 }}>
                    <Text className="text-gray-900 font-bold mb-3">📝 Temel Bilgiler</Text>

                    {/* Ürün Adı */}
                    <View className="mb-4">
                        <Text className="text-gray-700 font-medium mb-2">Ürün Adı *</Text>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Ürün adını girin"
                            value={form.ad}
                            onChangeText={(t) => setForm({ ...form, ad: t })}
                        />
                    </View>

                    {/* Açıklama */}
                    <View className="mb-4">
                        <Text className="text-gray-700 font-medium mb-2">Açıklama</Text>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Ürün açıklaması"
                            value={form.aciklama}
                            onChangeText={(t) => setForm({ ...form, aciklama: t })}
                            multiline
                            numberOfLines={3}
                        />
                    </View>

                    {/* Fiyat */}
                    <View>
                        <Text className="text-gray-700 font-medium mb-2">Fiyat (₺) *</Text>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="0.00"
                            value={form.fiyat}
                            onChangeText={(t) => setForm({ ...form, fiyat: t })}
                            keyboardType="decimal-pad"
                        />
                    </View>
                </View>

                {/* Kategori */}
                <View className="bg-white rounded-2xl p-5 shadow-sm mb-4" style={{ elevation: 2 }}>
                    <Text className="text-gray-900 font-bold mb-3">🏷️ Kategori</Text>
                    <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                        <View className="flex-row gap-2">
                            {formData?.kategoriler?.map((k: any) => (
                                <TouchableOpacity
                                    key={k.id}
                                    onPress={() => setForm({ ...form, kategoriId: k.id.toString() })}
                                >
                                    {form.kategoriId === k.id.toString() ? (
                                        <LinearGradient colors={['#667eea', '#764ba2']} className="px-4 py-2 rounded-full">
                                            <Text className="text-white font-medium">{k.ad}</Text>
                                        </LinearGradient>
                                    ) : (
                                        <View className="px-4 py-2 rounded-full bg-gray-100">
                                            <Text className="text-gray-700">{k.ad}</Text>
                                        </View>
                                    )}
                                </TouchableOpacity>
                            ))}
                        </View>
                    </ScrollView>
                </View>

                {/* Stok Yönetimi */}
                <View className="bg-white rounded-2xl p-5 shadow-sm mb-4" style={{ elevation: 2 }}>
                    <Text className="text-gray-900 font-bold mb-3">📦 Stok Yönetimi</Text>

                    {stoklar.map((stok, index) => (
                        <View key={stok.beden} className="flex-row items-center justify-between py-3 border-b border-gray-100">
                            <View className="bg-gray-100 px-4 py-2 rounded-full">
                                <Text className="text-gray-900 font-bold">{stok.beden}</Text>
                            </View>
                            <View className="flex-row items-center">
                                <TouchableOpacity
                                    onPress={() => updateStok(index, -1)}
                                    className="w-10 h-10 rounded-full bg-red-100 items-center justify-center"
                                >
                                    <Minus size={18} color="#e53e3e" />
                                </TouchableOpacity>
                                <Text className="text-gray-900 font-bold text-xl mx-4 w-8 text-center">
                                    {stok.adet}
                                </Text>
                                <TouchableOpacity
                                    onPress={() => updateStok(index, 1)}
                                    className="w-10 h-10 rounded-full bg-green-100 items-center justify-center"
                                >
                                    <Plus size={18} color="#38a169" />
                                </TouchableOpacity>
                            </View>
                        </View>
                    ))}
                </View>

                {/* Save Button */}
                <TouchableOpacity onPress={handleSave} disabled={saveMutation.isPending}>
                    <LinearGradient
                        colors={['#667eea', '#764ba2']}
                        className="py-4 rounded-2xl flex-row items-center justify-center"
                    >
                        {saveMutation.isPending ? (
                            <ActivityIndicator color="white" />
                        ) : (
                            <>
                                <Save size={20} color="white" />
                                <Text className="text-white font-bold text-lg ml-2">
                                    {urunId ? 'Güncelle' : 'Kaydet'}
                                </Text>
                            </>
                        )}
                    </LinearGradient>
                </TouchableOpacity>
            </ScrollView>
        </View>
    );
}
