package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocType;
import pe.gob.gdr.access.domain.repository.DocTypeRepository;

@Repository
public interface JpaDocTypeRepository extends JpaRepository<DocType, Long>, DocTypeRepository {

    @Override
    @Query("""
            select type
            from DocType type
            where upper(type.status) = 'ACTIVO'
            order by type.name asc
            """)
    List<DocType> findActiveTypes();

    @Override
    @Query("""
            select type
            from DocType type
            where type.id = :typeId
              and upper(type.status) = 'ACTIVO'
            """)
    Optional<DocType> findActiveById(@Param("typeId") Long typeId);
}
