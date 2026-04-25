package pe.gob.gdr.access.infrastructure.persistence.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pe.gob.gdr.access.domain.model.GdrScoreDetail;
import pe.gob.gdr.access.domain.repository.GdrScoreDetailRepository;

@Repository
public interface JpaGdrScoreDetailRepository extends JpaRepository<GdrScoreDetail, Long>, GdrScoreDetailRepository {

    @Override
    @Query("""
            select detail
            from GdrScoreDetail detail
            join fetch detail.finalEvaluation evaluation
            join fetch detail.goal goal
            join fetch goal.indicator indicator
            where evaluation.id = :finalEvaluationId
            order by detail.id asc
            """)
    List<GdrScoreDetail> findByFinalEvaluationId(@Param("finalEvaluationId") Long finalEvaluationId);

    @Override
    @Modifying
    @Query("delete from GdrScoreDetail detail where detail.finalEvaluation.id = :finalEvaluationId")
    void deleteByFinalEvaluationId(@Param("finalEvaluationId") Long finalEvaluationId);
}

