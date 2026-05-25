package br.com.arenamatch.service;

import java.security.SecureRandom;
import org.springframework.stereotype.Service;

@Service
public class CodigoAtivacaoService {

    private final SecureRandom random = new SecureRandom();

    public String gerarCodigoNumerico() {
        return String.format("%05d", random.nextInt(100000));
    }
}
