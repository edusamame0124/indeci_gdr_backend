package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrEvalQualEmailLog;
import pe.gob.gdr.access.domain.repository.GdrEvalQualEmailLogRepository;

@Repository
public interface JpaGdrEvalQualEmailLogRepository
        extends JpaRepository<GdrEvalQualEmailLog, Long>, GdrEvalQualEmailLogRepository {

    @Override
    List<GdrEvalQualEmailLog> findByFinalEvaluation_IdOrderBySentAtDesc(Long finalEvaluationId);
}
