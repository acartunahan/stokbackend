package com.stoktakip.repository;

import com.stoktakip.entity.PersonelZimmetHareket;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonelZimmetHareketRepository extends JpaRepository<PersonelZimmetHareket, Long> {

    Optional<PersonelZimmetHareket> findFirstByEnvanterGuidAndAktifTrueOrderByIdDesc(String guid);

    List<PersonelZimmetHareket> findByEnvanterGuidOrderByIdDesc(String guid);
}
