package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocSignatureRequest;
import pe.gob.gdr.access.domain.repository.DocSignatureRequestRepository;

@Repository
public interface JpaDocSignatureRequestRepository extends JpaRepository<DocSignatureRequest, Long>, DocSignatureRequestRepository {

    @Override
    @Query("""
            select request
            from DocSignatureRequest request
            join fetch request.docType type
            join fetch request.template template
            join fetch request.flowStatus flowStatus
            join fetch request.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            left join fetch request.signedDocument signedDocument
            where request.id = :requestId
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<DocSignatureRequest> findActiveById(@Param("requestId") Long requestId);

    @Override
    @Query("""
            select request
            from DocSignatureRequest request
            join fetch request.docType type
            join fetch request.template template
            join fetch request.flowStatus flowStatus
            join fetch request.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            left join fetch request.signedDocument signedDocument
            where result.id = :resultId
              and type.id = :typeId
              and upper(request.recordStatus) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by request.createdAt desc
            """)
    List<DocSignatureRequest> findByResultIdAndTypeIdOrderByCreatedAtDesc(
            @Param("resultId") Long resultId,
            @Param("typeId") Long typeId
    );
}
