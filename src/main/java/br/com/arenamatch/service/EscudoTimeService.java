package br.com.arenamatch.service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.primefaces.model.file.UploadedFile;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EscudoTimeService {

    private static final Set<String> EXTENSOES_PERMITIDAS = Set.of("jpg", "jpeg", "png", "webp");
    private static final long TAMANHO_MAXIMO = 2 * 1024 * 1024;

    @Value("${arenamatch.uploads.escudos-dir:/app/uploads/escudos}")
    private String diretorioEscudos;

    public String salvar(UploadedFile arquivo) {
        if (arquivo == null || arquivo.getSize() <= 0) {
            return null;
        }

        validarArquivo(arquivo);

        String extensao = extrairExtensao(arquivo.getFileName());
        String nomeArquivo = UUID.randomUUID() + "." + extensao;
        Path diretorio = Paths.get(diretorioEscudos).toAbsolutePath().normalize();
        Path destino = diretorio.resolve(nomeArquivo).normalize();

        if (!destino.startsWith(diretorio)) {
            throw new RuntimeException("Nome de arquivo invalido.");
        }

        try {
            Files.createDirectories(diretorio);
            try (InputStream inputStream = arquivo.getInputStream()) {
                Files.copy(inputStream, destino, StandardCopyOption.REPLACE_EXISTING);
            }
            return "/uploads/escudos/" + nomeArquivo;
        } catch (IOException e) {
            throw new RuntimeException("Nao foi possivel salvar o escudo do time.");
        }
    }

    private void validarArquivo(UploadedFile arquivo) {
        if (arquivo.getSize() > TAMANHO_MAXIMO) {
            throw new RuntimeException("O escudo deve ter no maximo 2 MB.");
        }

        String extensao = extrairExtensao(arquivo.getFileName());
        if (!EXTENSOES_PERMITIDAS.contains(extensao)) {
            throw new RuntimeException("Envie um escudo em JPG, PNG ou WEBP.");
        }

        String contentType = arquivo.getContentType();
        if (contentType == null || !contentType.toLowerCase(Locale.ROOT).startsWith("image/")) {
            throw new RuntimeException("O arquivo enviado precisa ser uma imagem.");
        }
    }

    private String extrairExtensao(String nomeArquivo) {
        if (nomeArquivo == null || !nomeArquivo.contains(".")) {
            return "";
        }

        return nomeArquivo.substring(nomeArquivo.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
    }
}
