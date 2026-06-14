package br.com.arenamatch.service;

import br.com.arenamatch.dto.NotificacaoDTO;
import br.com.arenamatch.dto.ResumoNotificacoesDTO;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class NotificacaoClassificacaoService {

    private static final Set<String> TIPOS_JOGO = Set.of(
            "JOGO",
            "CANCELAMENTO_JOGO",
            "PLACAR",
            "PLACAR_PENDENTE"
    );

    public ResumoNotificacoesDTO calcularResumo(List<NotificacaoDTO> notificacoes) {
        if (notificacoes == null || notificacoes.isEmpty()) {
            return new ResumoNotificacoesDTO(0, 0);
        }

        int totalJogo = (int) notificacoes.stream()
                .filter(notificacao -> TIPOS_JOGO.contains(notificacao.getTipo()))
                .count();
        int totalLiga = (int) notificacoes.stream()
                .filter(this::isLiga)
                .count();

        return new ResumoNotificacoesDTO(totalJogo, totalLiga);
    }

    public boolean isLiga(NotificacaoDTO notificacao) {
        return notificacao != null && "LIGA".equals(notificacao.getTipo());
    }

    public boolean isJogo(NotificacaoDTO notificacao) {
        return notificacao != null && "JOGO".equals(notificacao.getTipo());
    }

    public boolean isCancelamentoJogo(NotificacaoDTO notificacao) {
        return notificacao != null && "CANCELAMENTO_JOGO".equals(notificacao.getTipo());
    }

    public boolean isPlacar(NotificacaoDTO notificacao) {
        return notificacao != null && "PLACAR".equals(notificacao.getTipo());
    }

    public boolean isPlacarPendente(NotificacaoDTO notificacao) {
        return notificacao != null && "PLACAR_PENDENTE".equals(notificacao.getTipo());
    }
}
