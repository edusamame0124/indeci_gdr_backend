package pe.gob.gdr.access.application.port;

import org.springframework.core.io.Resource;

public interface DocumentStoragePort {

    String store(String category, String extension, byte[] content);

    Resource loadAsResource(String fileKey);

    boolean exists(String fileKey);
}
