package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrCieConformacion;
import pe.gob.gdr.access.domain.repository.GdrCieConformacionRepository;

@Repository
public interface JpaGdrCieConformacionRepository
        extends JpaRepository<GdrCieConformacion, Long>, GdrCieConformacionRepository {

    @Override
    @Query("select c from GdrCieConformacion c order by c.vigenciaInicio desc")
    List<GdrCieConformacion> findAllOrderByVigenciaInicioDesc();

    @Override
    @Query("""
            select c from GdrCieConformacion c
            where c.cycle.id = :cycleId
            order by c.vigenciaInicio desc
            """)
    List<GdrCieConformacion> findByCycleIdOrderByVigenciaInicioDesc(@Param("cycleId") Long cycleId);

    @Override
    @Query("""
            select c from GdrCieConformacion c
            where c.cycle is null
            order by c.vigenciaInicio desc
            """)
    List<GdrCieConformacion> findInstitucionalesOrderByVigenciaInicioDesc();

    @Override
    @Query("""
            select case when count(c) > 0 then true else false end
            from GdrCieConformacion c
            where c.cycle.id = :cycleId
              and c.estado = 'VIGENTE'
            """)
    boolean existsConformacionVigenteParaCiclo(@Param("cycleId") Long cycleId);
}
