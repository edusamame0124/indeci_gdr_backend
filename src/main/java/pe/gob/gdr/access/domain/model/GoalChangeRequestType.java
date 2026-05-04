package pe.gob.gdr.access.domain.model;

public enum GoalChangeRequestType {
    TITULO("Titulo de la meta"),
    DESCRIPCION("Descripcion"),
    INDICADOR("Indicador"),
    VALOR_ESPERADO("Valor esperado"),
    PESO("Peso"),
    OTRO("Otro");

    private final String displayName;

    GoalChangeRequestType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
