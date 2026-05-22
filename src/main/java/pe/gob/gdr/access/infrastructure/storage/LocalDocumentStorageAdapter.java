package pe.gob.gdr.access.infrastructure.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import pe.gob.gdr.access.application.port.DocumentStoragePort;
import pe.gob.gdr.access.domain.exception.DomainException;
import pe.gob.gdr.access.infrastructure.config.DocumentStorageProperties;

@Component
public class LocalDocumentStorageAdapter implements DocumentStoragePort {

    private final Path basePath;

    public LocalDocumentStorageAdapter(DocumentStorageProperties properties) {
        if (properties.getStorageBasePath() == null || properties.getStorageBasePath().isBlank()) {
            throw new DomainException("La ruta base de almacenamiento documental no esta configurada.");
        }
        this.basePath = Paths.get(properties.getStorageBasePath()).toAbsolutePath().normalize();
    }

    @Override
    public String store(String category, String extension, byte[] content) {
        try {
            LocalDate today = LocalDate.now();
            String safeExtension = extension == null || extension.isBlank() ? ".bin" : extension;
            String generatedName = UUID.randomUUID() + safeExtension;
            Path relativePath = Paths.get(
                    category,
                    String.valueOf(today.getYear()),
                    String.format("%02d", today.getMonthValue()),
                    generatedName
            );
            Path targetPath = basePath.resolve(relativePath).normalize();
            ensureInsideBasePath(targetPath);
            Files.createDirectories(Objects.requireNonNull(targetPath.getParent()));
            Files.write(targetPath, content);
            return relativePath.toString().replace('\\', '/');
        } catch (IOException exception) {
            throw new DomainException("No se pudo almacenar el documento en el repositorio local configurado.");
        }
    }

    @Override
    public Resource loadAsResource(String fileKey) {
        try {
            Path targetPath = basePath.resolve(fileKey).normalize();
            ensureInsideBasePath(targetPath);
            if (!Files.exists(targetPath) || !Files.isRegularFile(targetPath)) {
                throw new DomainException("El archivo solicitado no se encuentra disponible en el almacenamiento.");
            }
            return new UrlResource(targetPath.toUri());
        } catch (IOException exception) {
            throw new DomainException("No se pudo acceder al archivo solicitado.");
        }
    }

    @Override
    public boolean exists(String fileKey) {
        Path targetPath = basePath.resolve(fileKey).normalize();
        ensureInsideBasePath(targetPath);
        return Files.exists(targetPath) && Files.isRegularFile(targetPath);
    }

    private void ensureInsideBasePath(Path targetPath) {
        if (!targetPath.startsWith(basePath)) {
            throw new DomainException("La clave documental solicitada no es valida.");
        }
    }
}
