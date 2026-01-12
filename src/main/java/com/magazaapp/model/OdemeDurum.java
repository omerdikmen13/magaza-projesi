package com.magazaapp.model;

/**
 * Ödeme işlemi durumları
 */
public enum OdemeDurum {
    BEKLEMEDE, // Ödeme başlatıldı, henüz tamamlanmadı
    BASARILI, // Ödeme başarıyla tamamlandı
    BASARISIZ, // Ödeme başarısız oldu
    IPTAL, // Kullanıcı tarafından iptal edildi
    IADE // İade yapıldı
}
