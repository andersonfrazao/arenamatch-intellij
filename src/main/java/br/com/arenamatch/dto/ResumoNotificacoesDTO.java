package br.com.arenamatch.dto;

public class ResumoNotificacoesDTO {

    private final int totalConvitesJogo;
    private final int totalConvitesLiga;
    private final int totalNotificacoes;

    public ResumoNotificacoesDTO(int totalConvitesJogo, int totalConvitesLiga) {
        this.totalConvitesJogo = totalConvitesJogo;
        this.totalConvitesLiga = totalConvitesLiga;
        this.totalNotificacoes = totalConvitesJogo + totalConvitesLiga;
    }

    public int getTotalConvitesJogo() {
        return totalConvitesJogo;
    }

    public int getTotalConvitesLiga() {
        return totalConvitesLiga;
    }

    public int getTotalNotificacoes() {
        return totalNotificacoes;
    }
}
