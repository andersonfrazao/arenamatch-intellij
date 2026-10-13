package br.com.arenamatch.enums;

public enum FormatoCampeonato {
    LIGA("Liga — pontos corridos"), GRUPOS_MATA_MATA("Grupos + mata-mata"), MATA_MATA("Mata-mata direto");
    private final String rotulo;
    FormatoCampeonato(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
