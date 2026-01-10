package com.magazaapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "urun_stok", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "urun_id", "beden_id" })
})
public class UrunStok {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "urun_id", nullable = false)
    private Urun urun;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "beden_id", nullable = false)
    private Beden beden;

    @Column(nullable = false)
    private Integer adet = 0;

    // Constructors
    public UrunStok() {
    }

    public UrunStok(Urun urun, Beden beden, Integer adet) {
        this.urun = urun;
        this.beden = beden;
        this.adet = adet;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Urun getUrun() {
        return urun;
    }

    public void setUrun(Urun urun) {
        this.urun = urun;
    }

    public Beden getBeden() {
        return beden;
    }

    public void setBeden(Beden beden) {
        this.beden = beden;
    }

    public Integer getAdet() {
        return adet;
    }

    public void setAdet(Integer adet) {
        this.adet = adet;
    }
}
