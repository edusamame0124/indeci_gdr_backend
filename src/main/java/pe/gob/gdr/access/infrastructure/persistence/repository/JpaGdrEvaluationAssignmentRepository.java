package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvaluationAssignment;
import pe.gob.gdr.access.domain.repository.GdrEvaluationAssignmentRepository;

@Repository
public interface JpaGdrEvaluationAssignmentRepository
        extends JpaRepository<GdrEvaluationAssignment, Long>, GdrEvaluationAssignmentRepository {

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            where upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by evaluated.displayName asc, evaluator.displayName asc
            """)
    List<GdrEvaluationAssignment> findActiveAssignmentsForActiveCycle();

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            where evaluated.id = :evaluatedId
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByEvaluatedIdInActiveCycle(@Param("evaluatedId") Long evaluatedId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            where (evaluator.id = :personId or evaluated.id = :personId)
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            order by assignment.id asc
            """)
    List<GdrEvaluationAssignment> findActiveByPersonIdInActiveCycle(@Param("personId") Long personId);

    @Override
    @Query("""
            select assignment
            from GdrEvaluationAssignment assignment
            join fetch assignment.cycle cycle
            join fetch assignment.evaluatorPerson evaluator
            join fetch evaluator.orgUnit evaluatorOrg
            join fetch assignment.evaluatedPerson evaluated
            join fetch evaluated.orgUnit evaluatedOrg
            where assignment.id = :assignmentId
              and upper(assignment.status) = 'ACTIVE'
              and upper(cycle.status) = 'ACTIVE'
            """)
    Optional<GdrEvaluationAssignment> findActiveByIdInActiveCycle(@Param("assignmentId") Long assignmentId);
}
