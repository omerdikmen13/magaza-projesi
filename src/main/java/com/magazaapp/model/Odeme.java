package com.magazaapp.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Ödeme işlemi kaydı
 * Her ödeme girişimi için bir kayıt oluşturulur
 */
@Entity
@Table(name = "odeme", indexes = {
        @Index(name = "idx_odeme_token", columnList = "iyzico_token"),
        @Index(name = "idx_odeme_siparis", columnList = "siparis_fisi_id")
})
public class Odeme {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "siparis_fisi_id")
    private SiparisFisi siparisFisi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "kullanici_id", nullable = false)
    private Kullanici kullanici;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tutar;

    @Column(name = "iyzico_payment_id")
    private String iyzicoPaymentId;

    @Column(name = "iyzico_token", unique = true)
    private String iyzicoToken;

    @Column(name = "conversation_id")
    private String conversationId;

    @Column(name = "basket_id")
    private String basketId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OdemeDurum durum = OdemeDurum.BEKLEMEDE;

    @Column(name = "hata_mesaji", columnDefinition = "TEXT")
    private String hataMesaji;

    @Column(name = "kart_tipi")
    private String kartTipi;

    @Column(name = "kart_no_son_dort")
    private String kartNoSonDort;

    @Column(name = "taksit_sayisi")
    private Integer taksitSayisi;

    @Column(name = "olusturma_tarihi")
    private LocalDateTime olusturmaTarihi = LocalDateTime.now();

    @Column(name = "tamamlanma_tarihi")
    private LocalDateTime tamamlanmaTarihi;

    // Constructors
    public Odeme() {
    }

    public Odeme(Kullanici kullanici, BigDecimal tutar) {
        this.kullanici = kullanici;
        this.tutar = tutar;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public SiparisFisi getSiparisFisi() {
        return siparisFisi;
    }

    public void setSiparisFisi(SiparisFisi siparisFisi) {
        this.siparisFisi = siparisFisi;
    }

    public Kullanici getKullanici() {
        return kullanici;
    }

    public void setKullanici(Kullanici kullanici) {
        this.kullanici = kullanici;
    }

    public BigDecimal getTutar() {
        return tutar;
    }

    public void setTutar(BigDecimal tutar) {
        this.tutar = tutar;
    }

    public String getIyzicoPaymentId() {
        return iyzicoPaymentId;
    }

    public void setIyzicoPaymentId(String iyzicoPaymentId) {
        this.iyzicoPaymentId = iyzicoPaymentId;
    }

    public String getIyzicoToken() {
        return iyzicoToken;
    }

    public void setIyzicoToken(String iyzicoToken) {
        this.iyzicoToken = iyzicoToken;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getBasketId() {
        return basketId;
    }

    public void setBasketId(String basketId) {
        this.basketId = basketId;
    }

    public OdemeDurum getDurum() {
        return durum;
    }

    public void setDurum(OdemeDurum durum) {
        this.durum = durum;
    }

    public String getHataMesaji() {
        return hataMesaji;
    }

    public void setHataMesaji(String hataMesaji) {
        this.hataMesaji = hataMesaji;
    }

    public String getKartTipi() {
        return kartTipi;
    }

    public void setKartTipi(String kartTipi) {
        this.kartTipi = kartTipi;
    }

    public String getKartNoSonDort() {
        return kartNoSonDort;
    }

    public void setKartNoSonDort(String kartNoSonDort) {
        this.kartNoSonDort = kartNoSonDort;
    }

    public Integer getTaksitSayisi() {
        return taksitSayisi;
    }

    public void setTaksitSayisi(Integer taksitSayisi) {
        this.taksitSayisi = taksitSayisi;
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }

    public LocalDateTime getTamamlanmaTarihi() {
        return tamamlanmaTarihi;
    }

    public void setTamamlanmaTarihi(LocalDateTime tamamlanmaTarihi) {
        this.tamamlanmaTarihi = tamamlanmaTarihi;
    }
}
