package com.magazaapp.repository;

import com.magazaapp.model.Odeme;
import com.magazaapp.model.OdemeDurum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OdemeRepository extends JpaRepository<Odeme, Long> {

    // Token ile ödeme bul
    Optional<Odeme> findByIyzicoToken(String token);

    // ConversationId ile ödeme bul
    Optional<Odeme> findByConversationId(String conversationId);

    // Kullanıcının ödemelerini getir
    List<Odeme> findByKullaniciIdOrderByOlusturmaTarihiDesc(Long kullaniciId);

    // Sipariş için ödemeleri getir
    List<Odeme> findBySiparisFisiId(Long siparisId);

    // Duruma göre ödemeleri getir
    List<Odeme> findByDurum(OdemeDurum durum);

    // Kullanıcının bekleyen ödemelerini getir
    List<Odeme> findByKullaniciIdAndDurum(Long kullaniciId, OdemeDurum durum);
}
