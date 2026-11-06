package br.com.arenamatch.enums;

public enum PlanoAssinatura {
    BASICO,
    TRIAL,
    PRO,
    PREMIUM;

    public boolean incluiPro() {
        return this == PRO || this == PREMIUM;
    }
}
