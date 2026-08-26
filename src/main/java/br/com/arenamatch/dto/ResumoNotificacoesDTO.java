package br.com.arenamatch.dto;

public class ResumoNotificacoesDTO {

    private int totalConvitesJogo;
    private int totalConvitesLiga;
    private int totalNotificacoes;

    public ResumoNotificacoesDTO() {
        this(0, 0);
    }

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

    public void setTotalConvitesJogo(int totalConvitesJogo) {
        this.totalConvitesJogo = totalConvitesJogo;
    }

    public void setTotalConvitesLiga(int totalConvitesLiga) {
        this.totalConvitesLiga = totalConvitesLiga;
    }

    public void setTotalNotificacoes(int totalNotificacoes) {
        this.totalNotificacoes = totalNotificacoes;
    }
}
