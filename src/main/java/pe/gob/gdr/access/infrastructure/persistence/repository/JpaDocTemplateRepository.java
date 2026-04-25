package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocTemplate;
import pe.gob.gdr.access.domain.repository.DocTemplateRepository;

@Repository
public interface JpaDocTemplateRepository extends JpaRepository<DocTemplate, Long>, DocTemplateRepository {

    @Override
    @Query("""
            select template
            from DocTemplate template
            join fetch template.docType type
            where upper(template.status) = 'ACTIVO'
              and upper(type.status) = 'ACTIVO'
            order by type.name asc, template.templateName asc
            """)
    List<DocTemplate> findActiveTemplates();

    @Override
    @Query("""
            select template
            from DocTemplate template
            join fetch template.docType type
            where template.id = :templateId
              and upper(template.status) = 'ACTIVO'
              and upper(type.status) = 'ACTIVO'
            """)
    Optional<DocTemplate> findActiveById(@Param("templateId") Long templateId);
}
