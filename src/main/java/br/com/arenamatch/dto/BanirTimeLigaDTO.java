package br.com.arenamatch.dto;

import lombok.Data;

@Data
public class BanirTimeLigaDTO {
    private Long idTime;
    private Long idTimeAdmin;
    private String motivo;
}
