package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.DocSignatureRequest;

public interface DocSignatureRequestRepository {

    Optional<DocSignatureRequest> findActiveById(Long requestId);

    List<DocSignatureRequest> findByResultIdAndTypeIdOrderByCreatedAtDesc(Long resultId, Long typeId);

    DocSignatureRequest save(DocSignatureRequest request);
}
