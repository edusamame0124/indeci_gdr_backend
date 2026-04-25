package pe.gob.gdr.access.domain.repository;

import java.util.List;
import java.util.Optional;
import pe.gob.gdr.access.domain.model.DocTemplate;

public interface DocTemplateRepository {

    List<DocTemplate> findActiveTemplates();

    Optional<DocTemplate> findActiveById(Long templateId);
}
