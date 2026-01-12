import apiClient from './apiClient';

// =============== AUTH API ===============
export const authApi = {
    login: async (kullaniciAdi: string, sifre: string) => {
        const response = await apiClient.post('/api/auth/login', { kullaniciAdi, sifre });
        return response.data;
    },

    register: async (data: {
        kullaniciAdi: string;
        email: string;
        sifre: string;
        ad: string;
        soyad: string;
        telefon?: string;
        adres?: string;
    }) => {
        const response = await apiClient.post('/api/auth/register', data);
        return response.data;
    },

    profilGuncelle: async (data: { ad?: string; soyad?: string; email?: string; telefon?: string; adres?: string }) => {
        const response = await apiClient.put('/api/auth/profil', data);
        return response.data;
    },

    sifreDegistir: async (mevcutSifre: string, yeniSifre: string) => {
        const response = await apiClient.put('/api/auth/sifre-degistir', { mevcutSifre, yeniSifre });
        return response.data;
    },
};

// =============== PRODUCTS API ===============
export const urunlerApi = {
    getAll: async () => {
        const response = await apiClient.get('/api/urunler');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await apiClient.get(`/api/urunler/${id}`);
        return response.data;
    },

    getByMagaza: async (magazaId: number, kategoriId?: number, altKategoriId?: number) => {
        const params = new URLSearchParams();
        if (kategoriId) params.append('kategoriId', kategoriId.toString());
        if (altKategoriId) params.append('altKategoriId', altKategoriId.toString());

        const response = await apiClient.get(`/api/urunler/magaza/${magazaId}?${params}`);
        return response.data;
    },

    // Ürün arama
    ara: async (query: string) => {
        const response = await apiClient.get(`/api/urunler/ara?q=${encodeURIComponent(query)}`);
        return response.data;
    },
};

// =============== CATEGORIES API ===============
export const kategorilerApi = {
    getAll: async () => {
        const response = await apiClient.get('/api/kategoriler');
        return response.data;
    },

    getAltKategoriler: async (kategoriId: number) => {
        const response = await apiClient.get(`/api/kategoriler/${kategoriId}/alt-kategoriler`);
        return response.data;
    },
};

// =============== STORES API ===============
export const magazalarApi = {
    getAll: async () => {
        const response = await apiClient.get('/api/magazalar');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await apiClient.get(`/api/magazalar/${id}`);
        return response.data;
    },
};

// =============== CART API ===============
export const sepetApi = {
    get: async () => {
        const response = await apiClient.get('/api/sepet');
        return response.data;
    },

    add: async (urunId: number, bedenId: number, adet: number = 1) => {
        const response = await apiClient.post('/api/sepet/ekle', { urunId, bedenId, adet });
        return response.data;
    },

    update: async (id: number, adet: number) => {
        const response = await apiClient.put(`/api/sepet/${id}`, { adet });
        return response.data;
    },

    remove: async (id: number) => {
        const response = await apiClient.delete(`/api/sepet/${id}`);
        return response.data;
    },

    clear: async () => {
        const response = await apiClient.delete('/api/sepet/bosalt');
        return response.data;
    },

    checkout: async (teslimatAdresi?: string) => {
        const response = await apiClient.post('/api/sepet/siparis-ver', { teslimatAdresi });
        return response.data;
    },
};

// =============== ORDERS API ===============
export const siparislerApi = {
    getAll: async () => {
        const response = await apiClient.get('/api/siparisler');
        return response.data;
    },

    getById: async (id: number) => {
        const response = await apiClient.get(`/api/siparisler/${id}`);
        return response.data;
    },
};

// =============== AI API ===============
export const aiApi = {
    getOneri: async (soru: string) => {
        const response = await apiClient.post('/api/ai/oneri', { soru });
        // Backend 'response' gönderiyor, 'cevap' olarak map'liyoruz
        return {
            cevap: response.data.response || response.data.cevap || 'Yanıt alınamadı.',
            urunler: response.data.urunler || [],
            magazaSayisi: response.data.magazaSayisi || 0,
            urunSayisi: response.data.urunSayisi || 0,
        };
    },
};

// =============== FAVORİLER API ===============
export const favorilerApi = {
    // Tüm favorileri getir
    getAll: async () => {
        const response = await apiClient.get('/api/favoriler');
        return response.data;
    },

    // Favori ID'lerini getir (hızlı kontrol için)
    getIds: async () => {
        const response = await apiClient.get('/api/favoriler/ids');
        return response.data.favoriIds || [];
    },

    // Favoriye ekle
    add: async (urunId: number) => {
        const response = await apiClient.post(`/api/favoriler/${urunId}`);
        return response.data;
    },

    // Favoriden kaldır
    remove: async (urunId: number) => {
        const response = await apiClient.delete(`/api/favoriler/${urunId}`);
        return response.data;
    },

    // Toggle (ekle/kaldır)
    toggle: async (urunId: number) => {
        const response = await apiClient.post(`/api/favoriler/toggle/${urunId}`);
        return response.data;
    },
};

// =============== ADMIN API ===============
export const adminApi = {
    getDashboard: async () => {
        const response = await apiClient.get('/api/admin/dashboard');
        return response.data;
    },

    getKullanicilar: async () => {
        const response = await apiClient.get('/api/admin/kullanicilar');
        return response.data;
    },

    changeUserRole: async (id: number, rol: string) => {
        const response = await apiClient.put(`/api/admin/kullanicilar/${id}/rol`, { rol });
        return response.data;
    },

    toggleUserStatus: async (id: number) => {
        const response = await apiClient.put(`/api/admin/kullanicilar/${id}/durum`);
        return response.data;
    },

    getMagazalar: async () => {
        const response = await apiClient.get('/api/admin/magazalar');
        return response.data;
    },

    toggleMagazaStatus: async (id: number) => {
        const response = await apiClient.put(`/api/admin/magazalar/${id}/durum`);
        return response.data;
    },

    getSiparisler: async (durum?: string) => {
        const url = durum ? `/api/admin/siparisler?durum=${durum}` : '/api/admin/siparisler';
        const response = await apiClient.get(url);
        return response.data;
    },

    updateSiparisDurum: async (id: number, durum: string) => {
        const response = await apiClient.put(`/api/admin/siparisler/${id}/durum`, { durum });
        return response.data;
    },

    getCiro: async (donem: string = 'TUMU') => {
        const response = await apiClient.get(`/api/admin/ciro?donem=${donem}`);
        return response.data;
    },

    deleteUser: async (id: number) => {
        const response = await apiClient.delete(`/api/admin/kullanicilar/${id}`);
        return response.data;
    },

    deleteStore: async (id: number) => {
        const response = await apiClient.delete(`/api/admin/magazalar/${id}`);
        return response.data;
    },

    updateUser: async (id: number, data: { ad?: string; soyad?: string; email?: string; telefon?: string }) => {
        const response = await apiClient.put(`/api/admin/kullanicilar/${id}`, data);
        return response.data;
    },

    updateStore: async (id: number, data: { ad?: string; aciklama?: string; logoUrl?: string }) => {
        const response = await apiClient.put(`/api/admin/magazalar/${id}`, data);
        return response.data;
    },

    getStoreProducts: async (id: number) => {
        const response = await apiClient.get(`/api/admin/magazalar/${id}/urunler`);
        return response.data;
    },

    addProduct: async (magazaId: number, urun: any) => {
        const response = await apiClient.post(`/api/admin/magazalar/${magazaId}/urun`, urun);
        return response.data;
    },

    toggleProductStatus: async (id: number) => {
        const response = await apiClient.put(`/api/admin/urunler/${id}/durum`);
        return response.data;
    },

    deleteProduct: async (id: number) => {
        const response = await apiClient.delete(`/api/admin/urunler/${id}`);
        return response.data;
    },

    // Admin Mesaj Yönetimi
    getAllMessages: async () => {
        const response = await apiClient.get('/api/mesajlar/admin/tumMesajlar');
        return response.data;
    },

    getConversation: async (magazaId: number, musteriId: number) => {
        const response = await apiClient.get(`/api/mesajlar/admin/sohbet/${magazaId}/${musteriId}`);
        return response.data;
    },

    deleteMessage: async (mesajId: number) => {
        const response = await apiClient.delete(`/api/mesajlar/admin/mesaj/${mesajId}`);
        return response.data;
    },

    // Admin Sipariş İçerik Yönetimi
    getSiparis: async (id: number) => {
        const response = await apiClient.get(`/api/admin/siparis/${id}`);
        return response.data;
    },

    updateSiparisIcerik: async (id: number, data: { detayIds: number[]; adetler: number[]; silinecekIds: number[] }) => {
        const params = new URLSearchParams();
        data.detayIds.forEach(detayId => params.append('detayIds', detayId.toString()));
        data.adetler.forEach(adet => params.append('adetler', adet.toString()));
        data.silinecekIds.forEach(silId => params.append('silinecekIds', silId.toString()));
        const response = await apiClient.post(`/api/admin/siparis/${id}/icerik-guncelle`, params.toString(), {
            headers: {
                'Content-Type': 'application/x-www-form-urlencoded'
            }
        });
        return response.data;
    },

    updateProduct: async (id: number, data: { ad?: string; fiyat?: number; aciklama?: string }) => {
        const response = await apiClient.post(`/api/admin/urun/${id}/guncelle`, data);
        return response.data;
    },

    // Admin Chat Operations
    sendMessage: async (data: { magazaId: number; musteriId: number; icerik: string; yon: 'MAGAZA_TO_MUSTERI' | 'MUSTERI_TO_MAGAZA' }) => {
        const response = await apiClient.post('/api/mesajlar/admin/sohbet/gonder', data);
        return response.data;
    },

    editMessage: async (id: number, icerik: string) => {
        const response = await apiClient.put(`/api/mesajlar/admin/mesaj/${id}`, { icerik });
        return response.data;
    },

    createUser: async (data: any) => {
        const response = await apiClient.post('/api/admin/kullanicilar', data);
        return response.data;
    },

    createStore: async (data: any) => {
        const response = await apiClient.post('/api/admin/magazalar', data);
        return response.data;
    },
};

// =============== STORE OWNER API ===============
export const magazaSahibiApi = {
    getPanel: async () => {
        const response = await apiClient.get('/api/magaza-sahibi/panel');
        return response.data;
    },

    getFormData: async () => {
        const response = await apiClient.get('/api/magaza-sahibi/form-data');
        return response.data;
    },

    getMagazaUrunler: async (magazaId: number) => {
        const response = await apiClient.get(`/api/magaza-sahibi/magaza/${magazaId}/urunler`);
        return response.data;
    },

    addUrun: async (magazaId: number, urun: any) => {
        const response = await apiClient.post(`/api/magaza-sahibi/magaza/${magazaId}/urun`, urun);
        return response.data;
    },

    updateUrun: async (urunId: number, urun: any) => {
        const response = await apiClient.put(`/api/magaza-sahibi/urun/${urunId}`, urun);
        return response.data;
    },

    toggleUrunStatus: async (urunId: number) => {
        const response = await apiClient.put(`/api/magaza-sahibi/urun/${urunId}/durum`);
        return response.data;
    },

    deleteUrun: async (urunId: number) => {
        const response = await apiClient.delete(`/api/magaza-sahibi/urun/${urunId}`);
        return response.data;
    },

    getMagazaSiparisler: async (magazaId: number) => {
        const response = await apiClient.get(`/api/magaza-sahibi/magaza/${magazaId}/siparisler`);
        return response.data;
    },

    updateSiparisDurum: async (siparisId: number, durum: string) => {
        const response = await apiClient.put(`/api/magaza-sahibi/siparis/${siparisId}/durum`, { durum });
        return response.data;
    },

    getSiparis: async (siparisId: number) => {
        const response = await apiClient.get(`/api/magaza-sahibi/siparis/${siparisId}`);
        return response.data;
    },

    updateSiparisIcerik: async (siparisId: number, data: { detayIds: number[], adetler: number[], silinecekIds: number[] }) => {
        const response = await apiClient.put(`/api/magaza-sahibi/siparis/${siparisId}/icerik`, data);
        return response.data;
    },

    // Mağaza oluştur
    createMagaza: async (data: { ad: string; aciklama: string }) => {
        const response = await apiClient.post('/api/magaza-sahibi/magaza', data);
        return response.data;
    },

    // Mağaza sil
    deleteMagaza: async (magazaId: number) => {
        const response = await apiClient.delete(`/api/magaza-sahibi/magaza/${magazaId}`);
        return response.data;
    },
};

// =============== MESAJLAR API ===============
export const mesajlarApi = {
    // Müşteri: Sohbet listesi
    getMusteriSohbetler: async () => {
        const response = await apiClient.get('/api/mesajlar/musteri/sohbetler');
        return response.data;
    },

    // Müşteri: Mağaza ile sohbet
    getMusteriSohbet: async (magazaId: number) => {
        const response = await apiClient.get(`/api/mesajlar/musteri/sohbet/${magazaId}`);
        return response.data;
    },

    // Müşteri: Mesaj gönder
    musteriMesajGonder: async (magazaId: number, icerik: string) => {
        const response = await apiClient.post('/api/mesajlar/musteri/gonder', { magazaId, icerik });
        return response.data;
    },

    // Mağaza Sahibi: Müşteri listesi
    getSahipMusteriler: async (magazaId: number) => {
        const response = await apiClient.get(`/api/mesajlar/sahip/magaza/${magazaId}/musteriler`);
        return response.data;
    },

    // Mağaza Sahibi: Müşteri ile sohbet
    getSahipSohbet: async (magazaId: number, musteriId: number) => {
        const response = await apiClient.get(`/api/mesajlar/sahip/magaza/${magazaId}/musteri/${musteriId}`);
        return response.data;
    },

    // Mağaza Sahibi: Mesaj gönder
    sahipMesajGonder: async (magazaId: number, musteriId: number, icerik: string) => {
        const response = await apiClient.post('/api/mesajlar/sahip/gonder', { magazaId, musteriId, icerik });
        return response.data;
    },
};

// =============== ÖDEME API (Mock) ===============
export const odemeApi = {
    /**
     * Ödeme başlat - token ve tutar döner
     */
    basla: async (): Promise<{
        success: boolean;
        token?: string;
        odemeId?: number;
        tutar?: number;
        error?: string;
    }> => {
        const response = await apiClient.post('/api/odeme/basla');
        return response.data;
    },

    /**
     * Ödeme tamamla - kart bilgileri ile
     */
    tamamla: async (
        token: string,
        kartNo: string,
        sonKullanma: string,
        cvv: string,
        kartSahibi: string
    ): Promise<{
        success: boolean;
        message: string;
        siparisId?: number;
        odemeId?: number;
        error?: string;
    }> => {
        const response = await apiClient.post('/api/odeme/tamamla', {
            token,
            kartNo,
            sonKullanma,
            cvv,
            kartSahibi
        });
        return response.data;
    },

    /**
     * Ödeme durumunu sorgula
     */
    durum: async (token: string): Promise<{
        success: boolean;
        durum: 'BEKLEMEDE' | 'BASARILI' | 'BASARISIZ' | 'IPTAL' | 'IADE';
        odemeId?: number;
        siparisId?: number;
        error?: string;
    }> => {
        const response = await apiClient.get(`/api/odeme/durum/${token}`);
        return response.data;
    },
};
