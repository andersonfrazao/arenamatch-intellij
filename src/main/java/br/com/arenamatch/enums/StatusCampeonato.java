package br.com.arenamatch.enums;

public enum StatusCampeonato {
    RASCUNHO, CONVITES, PRONTO_PARA_GERAR, EM_ANDAMENTO, FINALIZADO, CANCELADO;
    public boolean ocupaVaga() { return this != FINALIZADO && this != CANCELADO; }
}
