import React, { useState } from 'react';
import {
    View,
    Text,
    TextInput,
    TouchableOpacity,
    StyleSheet,
    ActivityIndicator,
    Alert,
    ScrollView,
    KeyboardAvoidingView,
    Platform,
} from 'react-native';
import { useLocalSearchParams, useRouter } from 'expo-router';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useMutation } from '@tanstack/react-query';
import { odemeApi } from '../services/api';

/**
 * Mock Ödeme Ekranı - Kart Formu
 * WebView gerektirmez - direkt Native form
 */
export default function OdemeScreen() {
    const router = useRouter();
    const params = useLocalSearchParams<{ token: string; tutar: string }>();
    const { token, tutar } = params;

    const [kartNo, setKartNo] = useState('');
    const [sonKullanma, setSonKullanma] = useState('');
    const [cvv, setCvv] = useState('');
    const [kartSahibi, setKartSahibi] = useState('');

    const odemeMutation = useMutation({
        mutationFn: () => odemeApi.tamamla(token, kartNo, sonKullanma, cvv, kartSahibi),
        onSuccess: (data) => {
            if (data.success) {
                Alert.alert(
                    'Ödeme Başarılı! ✓',
                    data.siparisId ? `Sipariş No: #${data.siparisId}` : 'Siparişiniz oluşturuldu.',
                    [
                        {
                            text: 'Siparişlerime Git',
                            onPress: () => router.replace('/siparislerim')
                        }
                    ]
                );
            } else {
                Alert.alert('Ödeme Başarısız', data.message || 'Bir hata oluştu');
            }
        },
        onError: (error: any) => {
            Alert.alert('Hata', error.response?.data?.error || 'Ödeme işlemi başarısız');
        }
    });

    const formatKartNo = (text: string) => {
        const cleaned = text.replace(/\D/g, '');
        const groups = cleaned.match(/.{1,4}/g);
        return groups ? groups.join(' ').substring(0, 19) : cleaned;
    };

    const formatSonKullanma = (text: string) => {
        const cleaned = text.replace(/\D/g, '');
        if (cleaned.length >= 2) {
            return cleaned.substring(0, 2) + '/' + cleaned.substring(2, 4);
        }
        return cleaned;
    };

    const handleOdeme = () => {
        if (!kartNo || !sonKullanma || !cvv || !kartSahibi) {
            Alert.alert('Hata', 'Lütfen tüm alanları doldurun');
            return;
        }
        odemeMutation.mutate();
    };

    return (
        <KeyboardAvoidingView
            style={styles.container}
            behavior={Platform.OS === 'ios' ? 'padding' : 'height'}
        >
            <ScrollView contentContainerStyle={styles.scrollContent}>
                {/* Header */}
                <View style={styles.header}>
                    <TouchableOpacity onPress={() => router.back()} style={styles.backButton}>
                        <Ionicons name="arrow-back" size={24} color="#333" />
                    </TouchableOpacity>
                    <Text style={styles.headerTitle}>Güvenli Ödeme</Text>
                    <Ionicons name="shield-checkmark" size={24} color="#00b894" />
                </View>

                {/* Secure Badge */}
                <View style={styles.secureBadge}>
                    <Ionicons name="lock-closed" size={18} color="#fff" />
                    <Text style={styles.secureBadgeText}>256-bit SSL ile korunan güvenli ödeme</Text>
                </View>

                {/* Tutar */}
                <View style={styles.tutarBox}>
                    <Text style={styles.tutarLabel}>Ödenecek Tutar</Text>
                    <Text style={styles.tutarValue}>{tutar || '0.00'} ₺</Text>
                </View>

                {/* Card Preview */}
                <LinearGradient colors={['#667eea', '#764ba2']} style={styles.cardPreview}>
                    <View style={styles.cardChip} />
                    <Text style={styles.cardNumber}>
                        {kartNo || '•••• •••• •••• ••••'}
                    </Text>
                    <View style={styles.cardBottom}>
                        <View>
                            <Text style={styles.cardLabel}>KART SAHİBİ</Text>
                            <Text style={styles.cardValue}>{kartSahibi.toUpperCase() || 'AD SOYAD'}</Text>
                        </View>
                        <View>
                            <Text style={styles.cardLabel}>SON KULLANMA</Text>
                            <Text style={styles.cardValue}>{sonKullanma || 'MM/YY'}</Text>
                        </View>
                    </View>
                </LinearGradient>

                {/* Form */}
                <View style={styles.form}>
                    <View style={styles.inputGroup}>
                        <Text style={styles.inputLabel}>Kart Sahibi</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="Ad Soyad"
                            value={kartSahibi}
                            onChangeText={setKartSahibi}
                            autoCapitalize="characters"
                        />
                    </View>

                    <View style={styles.inputGroup}>
                        <Text style={styles.inputLabel}>Kart Numarası</Text>
                        <TextInput
                            style={styles.input}
                            placeholder="1234 5678 9012 3456"
                            value={kartNo}
                            onChangeText={(text) => setKartNo(formatKartNo(text))}
                            keyboardType="numeric"
                            maxLength={19}
                        />
                    </View>

                    <View style={styles.row}>
                        <View style={[styles.inputGroup, { flex: 1, marginRight: 10 }]}>
                            <Text style={styles.inputLabel}>Son Kullanma</Text>
                            <TextInput
                                style={styles.input}
                                placeholder="MM/YY"
                                value={sonKullanma}
                                onChangeText={(text) => setSonKullanma(formatSonKullanma(text))}
                                keyboardType="numeric"
                                maxLength={5}
                            />
                        </View>
                        <View style={[styles.inputGroup, { flex: 1 }]}>
                            <Text style={styles.inputLabel}>CVV</Text>
                            <TextInput
                                style={styles.input}
                                placeholder="•••"
                                value={cvv}
                                onChangeText={setCvv}
                                keyboardType="numeric"
                                maxLength={4}
                                secureTextEntry
                            />
                        </View>
                    </View>

                    {/* Pay Button */}
                    <TouchableOpacity
                        onPress={handleOdeme}
                        disabled={odemeMutation.isPending}
                        style={styles.payButton}
                    >
                        <LinearGradient
                            colors={['#667eea', '#764ba2']}
                            style={styles.payButtonGradient}
                        >
                            {odemeMutation.isPending ? (
                                <ActivityIndicator color="#fff" />
                            ) : (
                                <>
                                    <Ionicons name="lock-closed" size={20} color="#fff" />
                                    <Text style={styles.payButtonText}>Ödemeyi Tamamla</Text>
                                </>
                            )}
                        </LinearGradient>
                    </TouchableOpacity>
                </View>

                {/* Test Cards Info */}
                <View style={styles.testInfo}>
                    <Text style={styles.testTitle}>🧪 Demo Modu</Text>
                    <Text style={styles.testText}>Test Kartı: 4111 1111 1111 1111</Text>
                    <Text style={styles.testText}>SKT: 12/26 | CVV: 123</Text>
                </View>
            </ScrollView>
        </KeyboardAvoidingView>
    );
}

const styles = StyleSheet.create({
    container: {
        flex: 1,
        backgroundColor: '#f5f5f5',
    },
    scrollContent: {
        padding: 20,
        paddingTop: 50,
    },
    header: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'space-between',
        marginBottom: 20,
    },
    backButton: {
        padding: 8,
    },
    headerTitle: {
        fontSize: 20,
        fontWeight: '700',
        color: '#333',
    },
    secureBadge: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: '#00b894',
        padding: 12,
        borderRadius: 12,
        gap: 8,
        marginBottom: 20,
    },
    secureBadgeText: {
        color: '#fff',
        fontSize: 13,
        fontWeight: '500',
    },
    tutarBox: {
        alignItems: 'center',
        marginBottom: 20,
    },
    tutarLabel: {
        color: '#666',
        fontSize: 14,
    },
    tutarValue: {
        fontSize: 32,
        fontWeight: '700',
        color: '#667eea',
    },
    cardPreview: {
        borderRadius: 16,
        padding: 24,
        marginBottom: 24,
    },
    cardChip: {
        width: 45,
        height: 32,
        backgroundColor: '#ffd700',
        borderRadius: 6,
        marginBottom: 20,
    },
    cardNumber: {
        fontSize: 22,
        color: '#fff',
        letterSpacing: 2,
        fontFamily: Platform.OS === 'ios' ? 'Courier' : 'monospace',
        marginBottom: 20,
    },
    cardBottom: {
        flexDirection: 'row',
        justifyContent: 'space-between',
    },
    cardLabel: {
        fontSize: 10,
        color: 'rgba(255,255,255,0.7)',
        marginBottom: 4,
    },
    cardValue: {
        fontSize: 14,
        color: '#fff',
        fontWeight: '600',
    },
    form: {
        backgroundColor: '#fff',
        borderRadius: 16,
        padding: 20,
        marginBottom: 20,
    },
    inputGroup: {
        marginBottom: 16,
    },
    inputLabel: {
        fontSize: 14,
        fontWeight: '600',
        color: '#333',
        marginBottom: 8,
    },
    input: {
        borderWidth: 2,
        borderColor: '#e0e0e0',
        borderRadius: 12,
        padding: 14,
        fontSize: 16,
    },
    row: {
        flexDirection: 'row',
    },
    payButton: {
        marginTop: 8,
    },
    payButtonGradient: {
        flexDirection: 'row',
        alignItems: 'center',
        justifyContent: 'center',
        padding: 16,
        borderRadius: 12,
        gap: 10,
    },
    payButtonText: {
        color: '#fff',
        fontSize: 18,
        fontWeight: '700',
    },
    testInfo: {
        backgroundColor: '#fff9e6',
        borderWidth: 1,
        borderColor: '#ffd93d',
        borderRadius: 12,
        padding: 16,
        alignItems: 'center',
    },
    testTitle: {
        fontSize: 14,
        fontWeight: '600',
        color: '#e17055',
        marginBottom: 8,
    },
    testText: {
        fontSize: 12,
        color: '#666',
    },
});
