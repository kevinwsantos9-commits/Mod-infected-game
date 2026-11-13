package com.nivek.eclipse.client.gui;

/** Abas do ECLIPSE SYSTEM. A nota diz quando o conteudo da aba chega. */
public enum EclipseTab {
    OVERVIEW("OVERVIEW", ""),
    EVOLUTION("EVOLUTION", "Fase 08"),
    BODY("BODY", "Fase 05"),
    ABILITIES("ABILITIES", "Fase 06"),
    RESEARCH("RESEARCH", "Fase 08"),
    IMPLANTS("IMPLANTS", "Fase 07"),
    TRANSFORMATION("TRANSFORMATION", "Fase 10"),
    COMBAT("COMBAT", "Fase 06"),
    BESTIARY("BESTIARY", "Fase 09"),
    QUESTS("QUESTS", "fase futura"),
    SETTINGS("SETTINGS", "fase futura");

    public final String label;
    public final String note;

    EclipseTab(String label, String note) {
        this.label = label;
        this.note = note;
    }
}
