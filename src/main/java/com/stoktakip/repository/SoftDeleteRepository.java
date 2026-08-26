package com.stoktakip.repository;

import com.stoktakip.entity.SoftDeleteEntity;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

@NoRepositoryBean
public interface SoftDeleteRepository<T extends SoftDeleteEntity, ID> extends JpaRepository<T, ID> {

    @Override
    @Query("select entity from #{#entityName} entity where entity.silindi = false")
    List<T> findAll();

    @Override
    @Query("select entity from #{#entityName} entity where entity.silindi = false")
    List<T> findAll(Sort sort);

    @Override
    @Query(
        value = "select entity from #{#entityName} entity where entity.silindi = false",
        countQuery = "select count(entity) from #{#entityName} entity where entity.silindi = false"
    )
    Page<T> findAll(Pageable pageable);

    @Override
    @Query("select entity from #{#entityName} entity where id(entity) = :id and entity.silindi = false")
    Optional<T> findById(@Param("id") ID id);

    @Override
    @Query("select entity from #{#entityName} entity where id(entity) in :ids and entity.silindi = false")
    List<T> findAllById(@Param("ids") Iterable<ID> ids);

    @Override
    @Query("select count(entity) > 0 from #{#entityName} entity where id(entity) = :id and entity.silindi = false")
    boolean existsById(@Param("id") ID id);

    @Override
    @Query("select count(entity) from #{#entityName} entity where entity.silindi = false")
    long count();

    @Override
    default T getReferenceById(ID id) {
        return findById(id).orElseThrow(() -> new EntityNotFoundException("Aktif kayıt bulunamadı: " + id));
    }

    @Override
    default T getById(ID id) {
        return getReferenceById(id);
    }

    @Override
    default T getOne(ID id) {
        return getReferenceById(id);
    }

    @Override
    @Transactional
    default void delete(T entity) {
        entity.softDelete();
        save(entity);
    }

    @Override
    @Transactional
    default void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }

    @Override
    @Transactional
    default void deleteAllById(Iterable<? extends ID> ids) {
        ids.forEach(this::deleteById);
    }

    @Override
    @Transactional
    default void deleteAll(Iterable<? extends T> entities) {
        entities.forEach(this::delete);
    }

    @Override
    @Transactional
    default void deleteInBatch(Iterable<T> entities) {
        deleteAll(entities);
    }

    @Override
    @Transactional
    default void deleteAll() {
        findAll().forEach(this::delete);
    }

    @Override
    @Transactional
    default void deleteAllInBatch(Iterable<T> entities) {
        deleteAll(entities);
    }

    @Override
    @Transactional
    default void deleteAllByIdInBatch(Iterable<ID> ids) {
        deleteAllById(ids);
    }

    @Override
    @Transactional
    default void deleteAllInBatch() {
        deleteAll();
    }
}
