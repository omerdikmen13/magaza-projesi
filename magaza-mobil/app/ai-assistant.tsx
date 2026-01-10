import React, { useState, useRef } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    FlatList,
    KeyboardAvoidingView,
    Platform,
    Image,
    ActivityIndicator,
} from 'react-native';
import { LinearGradient } from 'expo-linear-gradient';
import { router } from 'expo-router';
import { ArrowLeft, Send, Bot, User, ShoppingCart, Store, Tag, Package } from 'lucide-react-native';
import { MotiView } from 'moti';
import { useMutation } from '@tanstack/react-query';

import { aiApi, sepetApi } from '../services/api';
import { useAuthStore } from '../stores/authStore';

interface Message {
    id: string;
    text: string;
    isBot: boolean;
    products?: any[];
    timestamp: Date;
}

export default function AIAssistantScreen() {
    const { isAuthenticated } = useAuthStore();
    const [messages, setMessages] = useState<Message[]>([
        {
            id: '1',
            text: 'Merhaba! 👋 Ben senin alışveriş asistanınım. Ne tür ürünler arıyorsun? "Kış için mont önerin" veya "100 TL altı tişört" gibi sorular sorabilirsin.',
            isBot: true,
            timestamp: new Date(),
        },
    ]);
    const [inputText, setInputText] = useState('');
    const flatListRef = useRef<FlatList>(null);

    // AI Link Parser - [[URUN:ID]], [[MAGAZA:ID]], [[KATEGORI:ID]] formatlarını parse eder
    const parseAIMessage = (text: string) => {
        const parts: Array<{ type: 'text' | 'urun' | 'magaza' | 'kategori'; content: string; id?: number }> = [];
        let lastIndex = 0;

        const regex = /\[\[(URUN|MAGAZA|KATEGORI):(\d+)\]\]/g;
        let match;

        while ((match = regex.exec(text)) !== null) {
            // Önceki text parçası
            if (match.index > lastIndex) {
                parts.push({ type: 'text', content: text.slice(lastIndex, match.index) });
            }

            // Link parçası
            const linkType = match[1].toLowerCase() as 'urun' | 'magaza' | 'kategori';
            parts.push({
                type: linkType,
                id: parseInt(match[2]),
                content: match[0]
            });

            lastIndex = regex.lastIndex;
        }

        // Kalan text
        if (lastIndex < text.length) {
            parts.push({ type: 'text', content: text.slice(lastIndex) });
        }

        return parts;
    };

    // Parse edilen mesajı render et
    const renderParsedText = (text: string) => {
        const parts = parseAIMessage(text);

        return (
            <Text className="text-white">
                {parts.map((part, index) => {
                    if (part.type === 'text') {
                        return <Text key={index}>{part.content}</Text>;
                    }

                    // Link tipleri için renkler ve ikonlar
                    const linkConfig = {
                        urun: { color: '#a855f7', icon: Package, label: '🔗 Ürün', route: `/product/${part.id}` },
                        magaza: { color: '#22c55e', icon: Store, label: '🏪 Mağaza', route: `/(tabs)/magazalar?magazaId=${part.id}` },
                        kategori: { color: '#f97316', icon: Tag, label: '📂 Kategori', route: `/(tabs)/magazalar?kategoriId=${part.id}` },
                    };

                    const config = linkConfig[part.type];

                    return (
                        <Text
                            key={index}
                            onPress={() => router.push(config.route as any)}
                            style={{ color: config.color, textDecorationLine: 'underline' }}
                        >
                            {config.label}
                        </Text>
                    );
                })}
            </Text>
        );
    };

    const aiMutation = useMutation({
        mutationFn: (soru: string) => aiApi.getOneri(soru),
        onSuccess: (data) => {
            const botMessage: Message = {
                id: Date.now().toString(),
                text: data.cevap || 'Yanıt alınamadı.',
                isBot: true,
                products: data.urunler || [],
                timestamp: new Date(),
            };
            setMessages((prev) => [...prev, botMessage]);
        },
        onError: () => {
            const errorMessage: Message = {
                id: Date.now().toString(),
                text: 'Üzgünüm, bir hata oluştu. Lütfen tekrar deneyin.',
                isBot: true,
                timestamp: new Date(),
            };
            setMessages((prev) => [...prev, errorMessage]);
        },
    });

    const handleSend = () => {
        if (!inputText.trim()) return;

        const userMessage: Message = {
            id: Date.now().toString(),
            text: inputText.trim(),
            isBot: false,
            timestamp: new Date(),
        };

        setMessages((prev) => [...prev, userMessage]);
        setInputText('');
        aiMutation.mutate(inputText.trim());
    };

    const renderProductCard = (product: any) => (
        <TouchableOpacity
            key={product.id}
            onPress={() => router.push(`/product/${product.id}`)}
            className="mr-3 w-32"
        >
            <View className="bg-dark-100 rounded-xl overflow-hidden">
                <Image
                    source={{ uri: product.resimUrl || 'https://via.placeholder.com/100' }}
                    className="w-full h-32"
                    resizeMode="cover"
                />
                <View className="p-2">
                    <Text className="text-white text-sm" numberOfLines={1}>{product.ad}</Text>
                    <Text className="text-primary-400 font-bold mt-1">₺{product.fiyat}</Text>
                </View>
            </View>
        </TouchableOpacity>
    );

    const renderMessage = ({ item, index }: { item: Message; index: number }) => (
        <MotiView
            from={{ opacity: 0, translateY: 10 }}
            animate={{ opacity: 1, translateY: 0 }}
            transition={{ delay: 50 }}
            className={`mb-4 ${item.isBot ? 'items-start' : 'items-end'}`}
        >
            <View className={`flex-row ${item.isBot ? 'flex-row' : 'flex-row-reverse'} items-end max-w-[85%]`}>
                {/* Avatar */}
                <View className={`w-8 h-8 rounded-full items-center justify-center ${item.isBot ? 'bg-gradient-to-br from-primary-500 to-accent-500 mr-2' : 'bg-white/10 ml-2'
                    }`}>
                    {item.isBot ? <Bot size={16} color="white" /> : <User size={16} color="white" />}
                </View>

                {/* Message Bubble */}
                <View className={`rounded-2xl px-4 py-3 ${item.isBot ? 'bg-white/10 rounded-bl-md' : 'bg-primary-600 rounded-br-md'
                    }`}>
                    {renderParsedText(item.text)}

                    {/* Product Recommendations */}
                    {item.products && item.products.length > 0 && (
                        <View className="mt-3">
                            <Text className="text-gray-400 text-sm mb-2">Önerilen Ürünler:</Text>
                            <FlatList
                                data={item.products}
                                horizontal
                                showsHorizontalScrollIndicator={false}
                                renderItem={({ item: product }) => renderProductCard(product)}
                                keyExtractor={(product) => product.id.toString()}
                            />
                        </View>
                    )}
                </View>
            </View>

            <Text className={`text-gray-500 text-xs mt-1 ${item.isBot ? 'ml-10' : 'mr-10'}`}>
                {item.timestamp.toLocaleTimeString('tr-TR', { hour: '2-digit', minute: '2-digit' })}
            </Text>
        </MotiView>
    );

    return (
        <View className="flex-1 bg-dark-300">
            <LinearGradient colors={['#1e1e2f', '#0f0f1f']} className="absolute inset-0" />

            {/* Header */}
            <View className="pt-12 pb-4 px-5 border-b border-white/10">
                <View className="flex-row items-center">
                    <TouchableOpacity
                        onPress={() => router.back()}
                        className="w-10 h-10 rounded-full bg-white/10 items-center justify-center mr-3"
                    >
                        <ArrowLeft size={22} color="white" />
                    </TouchableOpacity>
                    <View className="flex-1">
                        <View className="flex-row items-center">
                            <View className="w-10 h-10 rounded-full bg-gradient-to-br from-primary-500 to-accent-500 items-center justify-center mr-3">
                                <Bot size={22} color="white" />
                            </View>
                            <View>
                                <Text className="text-white font-semibold text-lg">AI Asistan</Text>
                                <View className="flex-row items-center">
                                    <View className="w-2 h-2 rounded-full bg-green-500 mr-1" />
                                    <Text className="text-gray-400 text-sm">Çevrimiçi</Text>
                                </View>
                            </View>
                        </View>
                    </View>
                </View>
            </View>

            {/* Messages */}
            <FlatList
                ref={flatListRef}
                data={messages}
                renderItem={renderMessage}
                keyExtractor={(item) => item.id}
                contentContainerStyle={{ padding: 20, paddingBottom: 100 }}
                onContentSizeChange={() => flatListRef.current?.scrollToEnd()}
                ListFooterComponent={
                    aiMutation.isPending ? (
                        <View className="items-start mb-4">
                            <View className="flex-row items-end">
                                <View className="w-8 h-8 rounded-full bg-gradient-to-br from-primary-500 to-accent-500 items-center justify-center mr-2">
                                    <Bot size={16} color="white" />
                                </View>
                                <View className="bg-white/10 rounded-2xl rounded-bl-md px-4 py-3">
                                    <View className="flex-row gap-1">
                                        <MotiView
                                            from={{ opacity: 0.3 }}
                                            animate={{ opacity: 1 }}
                                            transition={{ loop: true, duration: 500 }}
                                            className="w-2 h-2 rounded-full bg-gray-400"
                                        />
                                        <MotiView
                                            from={{ opacity: 0.3 }}
                                            animate={{ opacity: 1 }}
                                            transition={{ loop: true, duration: 500, delay: 150 }}
                                            className="w-2 h-2 rounded-full bg-gray-400"
                                        />
                                        <MotiView
                                            from={{ opacity: 0.3 }}
                                            animate={{ opacity: 1 }}
                                            transition={{ loop: true, duration: 500, delay: 300 }}
                                            className="w-2 h-2 rounded-full bg-gray-400"
                                        />
                                    </View>
                                </View>
                            </View>
                        </View>
                    ) : null
                }
            />

            {/* Input */}
            <KeyboardAvoidingView
                behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
                keyboardVerticalOffset={0}
            >
                <View className="absolute bottom-0 left-0 right-0 bg-dark-100/95 backdrop-blur-lg px-4 py-3 border-t border-white/10">
                    <View className="flex-row items-center bg-white/10 rounded-2xl px-4 py-2">
                        <TextInput
                            className="flex-1 text-white text-base py-2"
                            placeholder="Mesajınızı yazın..."
                            placeholderTextColor="#6b7280"
                            value={inputText}
                            onChangeText={setInputText}
                            onSubmitEditing={handleSend}
                            returnKeyType="send"
                            multiline
                            maxLength={500}
                        />
                        <TouchableOpacity
                            onPress={handleSend}
                            disabled={!inputText.trim() || aiMutation.isPending}
                            className={`ml-2 w-10 h-10 rounded-full items-center justify-center ${inputText.trim() ? 'bg-primary-600' : 'bg-white/10'
                                }`}
                        >
                            <Send size={18} color="white" />
                        </TouchableOpacity>
                    </View>
                </View>
            </KeyboardAvoidingView>
        </View>
    );
}
