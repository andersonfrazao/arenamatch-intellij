package br.com.arenamatch.service;

import br.com.arenamatch.entity.MensagemChat;
import br.com.arenamatch.entity.Partida;
import br.com.arenamatch.entity.Time;
import br.com.arenamatch.repository.MensagemChatRepository;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Service;

@Service
public class PartidaMensagemService {

    private final MensagemChatRepository mensagemChatRepository;

    public PartidaMensagemService(MensagemChatRepository mensagemChatRepository) {
        this.mensagemChatRepository = mensagemChatRepository;
    }

    public void criarMensagemInicialDoDesafio(Partida partida, Time desafiante, String texto) {
        if (texto == null || texto.trim().isEmpty()) {
            return;
        }

        criarMensagemDaPartida(
                partida,
                desafiante,
                texto.trim(),
                partida.getDataSolicitacao() != null ? partida.getDataSolicitacao() : LocalDateTime.now()
        );
    }

    public void criarMensagemCancelamento(Partida partida, Time solicitante, String motivo) {
        String dataDoJogo = formatarData(partida);
        String texto = "Solicitacao de cancelamento do jogo do dia " + dataDoJogo + ". Motivo: " + motivo;
        criarMensagemDaPartida(partida, solicitante, texto, LocalDateTime.now());
    }

    public void criarMensagemRespostaCancelamento(Partida partida, Time respondente, boolean aceitou) {
        String dataDoJogo = formatarData(partida);
        String texto = aceitou
                ? "Cancelamento do jogo do dia " + dataDoJogo + " aceito."
                : "Cancelamento do jogo do dia " + dataDoJogo + " recusado. O jogo continua agendado.";
        criarMensagemDaPartida(partida, respondente, texto, LocalDateTime.now());
    }

    private void criarMensagemDaPartida(Partida partida, Time remetente, String texto, LocalDateTime dataHora) {
        MensagemChat mensagem = new MensagemChat();
        mensagem.setPartida(partida);
        mensagem.setRemetente(remetente);
        mensagem.setTexto(texto);
        mensagem.setDataHora(dataHora);
        mensagem.setLida(false);

        mensagemChatRepository.save(mensagem);
    }

    private String formatarData(Partida partida) {
        if (partida.getDataHora() == null) {
            return "data indefinida";
        }
        return partida.getDataHora().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
    }
}
