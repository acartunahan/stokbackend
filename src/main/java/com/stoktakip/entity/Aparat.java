    package com.stoktakip.entity;

    import jakarta.persistence.Column;
    import jakarta.persistence.Entity;
    import jakarta.persistence.FetchType;
    import jakarta.persistence.Id;
    import jakarta.persistence.JoinColumn;
    import jakarta.persistence.ManyToOne;
    import jakarta.persistence.Table;

    @Entity
    @Table(name = "APARAT")
    public class Aparat extends BaseEntity {

        @Id
        @Column(name = "GUID", length = 128)
        private String guid;

        @ManyToOne(fetch = FetchType.LAZY)
        @JoinColumn(name = "ENVANTER_GUID", referencedColumnName = "GUID", nullable = false)
        private Envanter envanter;

        @Column(name = "APARAT_ADI", length = 255, nullable = false)
        private String aparatAdi;

        @Column(name = "ACIKLAMA", length = 1000)
        private String aciklama;

        public String getGuid() {
            return guid;
        }

        public void setGuid(String guid) {
            this.guid = guid;
        }

        public Envanter getEnvanter() {
            return envanter;
        }

        public void setEnvanter(Envanter envanter) {
            this.envanter = envanter;
        }

        public String getAparatAdi() {
            return aparatAdi;
        }

        public void setAparatAdi(String aparatAdi) {
            this.aparatAdi = aparatAdi;
        }

        public String getAciklama() {
            return aciklama;
        }

        public void setAciklama(String aciklama) {
            this.aciklama = aciklama;
        }
    }
