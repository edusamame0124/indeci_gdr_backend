package pe.gob.gdr.access.infrastructure.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.documentos")
public class DocumentStorageProperties {

    private String storageBasePath;
    private long maxFileSizeBytes;
    private List<String> allowedMimeTypes = new ArrayList<>();

    public String getStorageBasePath() {
        return storageBasePath;
    }

    public void setStorageBasePath(String storageBasePath) {
        this.storageBasePath = storageBasePath;
    }

    public long getMaxFileSizeBytes() {
        return maxFileSizeBytes;
    }

    public void setMaxFileSizeBytes(long maxFileSizeBytes) {
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public List<String> getAllowedMimeTypes() {
        return allowedMimeTypes;
    }

    public void setAllowedMimeTypes(List<String> allowedMimeTypes) {
        this.allowedMimeTypes = allowedMimeTypes;
    }
}
