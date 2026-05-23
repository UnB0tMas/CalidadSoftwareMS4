// ruta: src/main/java/com/upsjb/ms4/service/contract/config/NumeroBoletaReservado.java
package com.upsjb.ms4.service.contract.config;

public record NumeroBoletaReservado(
        Long idSerie,
        String serie,
        Long numero,
        String codigoBoleta
) {
}