package pe.gob.gdr.access.domain.policy;

public enum QualitativeRating {

    BUEN_RENDIMIENTO("Buen rendimiento"),
    SUJETO_OBSERVACION("Rendimiento sujeto a observación"),
    DISTINGUIDO("Rendimiento distinguido"),
    DESAPROBADO("Desaprobado"),
    NO_CALIFICABLE("No calificable");

    private final String label;

    QualitativeRating(String label) {
        this.label = label;
    }

    public String code() {
        return name();
    }

    public String label() {
        return label;
    }

    public static String labelOf(String code) {
        if (code == null) {
            return null;
        }
        for (QualitativeRating rating : values()) {
            if (rating.name().equals(code)) {
                return rating.label;
            }
        }
        return null;
    }
}
