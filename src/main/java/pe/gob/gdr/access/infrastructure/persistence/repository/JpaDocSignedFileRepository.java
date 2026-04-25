package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.DocSignedFile;
import pe.gob.gdr.access.domain.repository.DocSignedFileRepository;

@Repository
public interface JpaDocSignedFileRepository extends JpaRepository<DocSignedFile, Long>, DocSignedFileRepository {

    @Override
    @Query("""
            select document
            from DocSignedFile document
            join fetch document.docType type
            join fetch document.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by document.uploadDate desc, document.id desc
            """)
    List<DocSignedFile> findAllInActiveCycle();

    @Override
    @Query("""
            select document
            from DocSignedFile document
            join fetch document.docType type
            join fetch document.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where evaluated.id = :evaluatedId
              and upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by type.name asc, document.uploadDate desc
            """)
    List<DocSignedFile> findActiveByEvaluatedIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);

    @Override
    @Query("""
            select document
            from DocSignedFile document
            join fetch document.docType type
            join fetch document.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatedPerson evaluated
            join fetch assignment.evaluatorPerson evaluator
            where document.id = :documentId
              and upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<DocSignedFile> findActiveById(@Param("documentId") Long documentId);

    @Override
    @Query("""
            select document
            from DocSignedFile document
            join fetch document.docType type
            join fetch document.result result
            join fetch result.assignment assignment
            join fetch assignment.cycle cycle
            where result.id = :resultId
              and type.id = :typeId
              and upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<DocSignedFile> findActiveByResultIdAndTypeId(@Param("resultId") Long resultId, @Param("typeId") Long typeId);
}
