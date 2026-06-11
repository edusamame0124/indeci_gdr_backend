package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    @Query(
            value = """
                    select document
                    from DocSignedFile document
                    join document.docType type
                    join document.result result
                    join result.assignment assignment
                    join assignment.cycle cycle
                    join assignment.evaluatedPerson evaluated
                    where evaluated.id = :evaluatedId
                      and upper(document.status) = 'ACTIVO'
                      and upper(result.status) = 'ACTIVE'
                      and upper(assignment.status) = 'ACTIVE'
                      and upper(cycle.status) = 'ACTIVE'
                    order by type.name asc, document.uploadDate desc, document.id desc
                    """,
            countQuery = """
                    select count(document)
                    from DocSignedFile document
                    join document.docType type
                    join document.result result
                    join result.assignment assignment
                    join assignment.cycle cycle
                    join assignment.evaluatedPerson evaluated
                    where evaluated.id = :evaluatedId
                      and upper(document.status) = 'ACTIVO'
                      and upper(result.status) = 'ACTIVE'
                      and upper(assignment.status) = 'ACTIVE'
                      and upper(cycle.status) = 'ACTIVE'
                    """
    )
    Page<DocSignedFile> findPageActiveByEvaluatedIdInActiveCycle(
            @Param("evaluatedId") Long evaluatedId,
            Pageable pageable
    );

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

    // ── Implementaciones cycle-aware (P2) ─────────────────────────────────────

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
            where result.assignment.cycle.id = :cycleId
              and upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by document.uploadDate desc, document.id desc
            """)
    List<DocSignedFile> findAllByCycleId(@Param("cycleId") Long cycleId);

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
              and result.assignment.cycle.id = :cycleId
              and upper(document.status) = 'ACTIVO'
              and upper(result.status) = 'ACTIVE'
              and upper(assignment.status) = 'ACTIVE'
            order by type.name asc, document.uploadDate desc
            """)
    List<DocSignedFile> findActiveByEvaluatedIdAndCycle(
            @Param("evaluatedId") Long evaluatedId,
            @Param("cycleId") Long cycleId
    );

    @Override
    @Query(
            value = """
                    select document
                    from DocSignedFile document
                    join document.docType type
                    join document.result result
                    join result.assignment assignment
                    join assignment.cycle cycle
                    join assignment.evaluatedPerson evaluated
                    where evaluated.id = :evaluatedId
                      and cycle.id = :cycleId
                      and upper(document.status) = 'ACTIVO'
                      and upper(result.status) = 'ACTIVE'
                      and upper(assignment.status) = 'ACTIVE'
                    order by type.name asc, document.uploadDate desc, document.id desc
                    """,
            countQuery = """
                    select count(document)
                    from DocSignedFile document
                    join document.result result
                    join result.assignment assignment
                    join assignment.cycle cycle
                    join assignment.evaluatedPerson evaluated
                    where evaluated.id = :evaluatedId
                      and cycle.id = :cycleId
                      and upper(document.status) = 'ACTIVO'
                      and upper(result.status) = 'ACTIVE'
                      and upper(assignment.status) = 'ACTIVE'
                    """
    )
    Page<DocSignedFile> findPageActiveByEvaluatedIdAndCycle(
            @Param("evaluatedId") Long evaluatedId,
            @Param("cycleId") Long cycleId,
            Pageable pageable
    );
}
