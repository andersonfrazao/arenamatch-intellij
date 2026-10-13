package br.com.arenamatch.enums;

public enum CriterioDesempate {
    VITORIAS("Vitórias"), SALDO_GOLS("Saldo de gols"), GOLS_PRO("Gols pró");
    private final String rotulo;
    CriterioDesempate(String rotulo) { this.rotulo = rotulo; }
    public String getRotulo() { return rotulo; }
}
