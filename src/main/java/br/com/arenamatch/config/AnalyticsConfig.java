package br.com.arenamatch.config;

import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component("analyticsConfig")
public class AnalyticsConfig {
    private static final Set<String> PAGINAS_PUBLICAS = Set.of(
            "/index.xhtml", "/como-funciona.xhtml", "/privacidade.xhtml",
            "/login.xhtml", "/cadastro.xhtml");

    private final boolean enabled;
    private final String measurementId;

    public AnalyticsConfig(@Value("${arenamatch.analytics.enabled:false}") boolean enabled,
                           @Value("${arenamatch.analytics.measurement-id:}") String measurementId) {
        this.enabled = enabled;
        this.measurementId = measurementId == null ? "" : measurementId.trim();
        if (enabled && !this.measurementId.matches("G-[A-Z0-9]+")) {
            throw new IllegalArgumentException("Informe um ID de metricas GA4 valido para habilitar o Analytics.");
        }
    }

    public String getMeasurementId() {
        return measurementId;
    }

    public boolean habilitadoPara(String viewId) {
        return enabled && PAGINAS_PUBLICAS.contains(viewId == null ? "" : viewId);
    }
}
