package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.DocSignedFile;

public interface DocSignedFileRepository {

    List<DocSignedFile> findAllInActiveCycle();

    List<DocSignedFile> findActiveByEvaluatedIdInActiveCycle(Long evaluatedId);

    Optional<DocSignedFile> findActiveById(Long documentId);

    Optional<DocSignedFile> findActiveByResultIdAndTypeId(Long resultId, Long typeId);

    DocSignedFile save(DocSignedFile signedFile);
}
