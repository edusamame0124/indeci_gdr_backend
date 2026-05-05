package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import pe.gob.gdr.access.domain.model.DocSignedFile;

public interface DocSignedFileRepository {

    List<DocSignedFile> findAllInActiveCycle();

    List<DocSignedFile> findActiveByEvaluatedIdInActiveCycle(Long evaluatedId);

    Page<DocSignedFile> findPageActiveByEvaluatedIdInActiveCycle(Long evaluatedId, Pageable pageable);

    Optional<DocSignedFile> findActiveById(Long documentId);

    Optional<DocSignedFile> findActiveByResultIdAndTypeId(Long resultId, Long typeId);

    DocSignedFile save(DocSignedFile signedFile);
}
