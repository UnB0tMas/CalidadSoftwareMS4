// ruta: src/main/java/com/upsjb/ms4/service/contract/boleta/BoletaTemplateModelFactory.java
package com.upsjb.ms4.service.contract.boleta;

import com.upsjb.ms4.domain.entity.boleta.Boleta;
import com.upsjb.ms4.domain.entity.boleta.BoletaDetalle;

import java.util.List;
import java.util.Map;

public interface BoletaTemplateModelFactory {

    Map<String, Object> construirModeloBoleta(Long idBoleta);

    Map<String, Object> construirModeloBoleta(Boleta boleta, List<BoletaDetalle> detalles);

    Map<String, Object> construirModeloCorreoBoleta(Long idBoleta);

    String resolverRutaTemplateBoleta(Boleta boleta);

    String resolverRutaTemplateCorreo(Boleta boleta);
}