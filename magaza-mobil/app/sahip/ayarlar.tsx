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
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';
import {
    ArrowLeft,
    Building,
    Phone,
    MapPin,
    Save,
    Store,
} from 'lucide-react-native';
import { MotiView } from 'moti';

import { magazaSahibiApi } from '../../services/api';

export default function SahipAyarlarScreen() {
    const queryClient = useQueryClient();

    const { data: panel } = useQuery({
        queryKey: ['sahip-panel'],
        queryFn: magazaSahibiApi.getPanel,
    });

    const magazalar = panel?.magazalar || [];
    const [selectedMagaza, setSelectedMagaza] = useState<number>(magazalar[0]?.id || 0);
    const magaza = magazalar.find((m: any) => m.id === selectedMagaza);

    const [form, setForm] = useState({
        kurumTelefon: magaza?.kurumTelefon || '',
        kurumAdres: magaza?.kurumAdres || '',
        aciklama: magaza?.aciklama || '',
    });

    // TODO: Backend'de mağaza güncelleme endpoint'i eklenecek
    const handleSave = () => {
        Alert.alert('Bilgi', 'Mağaza ayarları kaydetme özelliği yakında eklenecek');
    };

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
                        <Text className="text-gray-900 text-xl font-bold">🏪 Mağaza Ayarları</Text>
                        <Text className="text-gray-500 text-sm">Kurum bilgileri</Text>
                    </View>
                </View>
            </View>

            <ScrollView className="flex-1 px-5 py-4" contentContainerStyle={{ paddingBottom: 40 }}>
                {/* Mağaza Seçici */}
                {magazalar.length > 1 && (
                    <View className="mb-6">
                        <Text className="text-gray-900 font-bold mb-3">Mağaza Seç</Text>
                        <ScrollView horizontal showsHorizontalScrollIndicator={false}>
                            {magazalar.map((m: any) => (
                                <TouchableOpacity
                                    key={m.id}
                                    onPress={() => setSelectedMagaza(m.id)}
                                    className={`mr-3 px-4 py-3 rounded-xl ${selectedMagaza === m.id ? 'bg-primary-500' : 'bg-white'
                                        }`}
                                >
                                    <Text className={selectedMagaza === m.id ? 'text-white font-bold' : 'text-gray-900'}>
                                        {m.ad}
                                    </Text>
                                </TouchableOpacity>
                            ))}
                        </ScrollView>
                    </View>
                )}

                {/* Mağaza Bilgileri */}
                <MotiView
                    from={{ opacity: 0, translateY: 20 }}
                    animate={{ opacity: 1, translateY: 0 }}
                    className="bg-white rounded-2xl p-5 shadow-sm mb-4"
                    style={{ elevation: 2 }}
                >
                    <View className="flex-row items-center mb-4">
                        <Store size={20} color="#667eea" />
                        <Text className="text-gray-900 font-bold text-lg ml-2">{magaza?.ad || 'Mağaza'}</Text>
                    </View>

                    {/* Mağaza Açıklaması */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Building size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Mağaza Açıklaması</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Mağaza açıklaması"
                            value={form.aciklama}
                            onChangeText={(t) => setForm({ ...form, aciklama: t })}
                            multiline
                            numberOfLines={3}
                        />
                    </View>

                    {/* Kurum Telefonu */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <Phone size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Kurum Telefonu</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="0212 123 45 67"
                            value={form.kurumTelefon}
                            onChangeText={(t) => setForm({ ...form, kurumTelefon: t })}
                            keyboardType="phone-pad"
                        />
                    </View>

                    {/* Kurum Adresi */}
                    <View className="mb-4">
                        <View className="flex-row items-center mb-2">
                            <MapPin size={16} color="#667eea" />
                            <Text className="text-gray-900 font-semibold ml-2">Kurum Adresi</Text>
                        </View>
                        <TextInput
                            className="bg-gray-50 border-2 border-gray-200 rounded-xl px-4 py-3 text-gray-900"
                            placeholder="Mağaza adresi"
                            value={form.kurumAdres}
                            onChangeText={(t) => setForm({ ...form, kurumAdres: t })}
                            multiline
                            numberOfLines={3}
                        />
                    </View>
                </MotiView>

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
            </ScrollView>
        </View>
    );
}
