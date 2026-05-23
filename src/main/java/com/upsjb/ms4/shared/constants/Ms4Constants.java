// ruta: src/main/java/com/upsjb/ms4/shared/constants/Ms4Constants.java
package com.upsjb.ms4.shared.constants;

import java.math.BigDecimal;

public final class Ms4Constants {

    private Ms4Constants() {
    }

    public static final String SERVICE_NAME = "ms-ventas-facturacion";
    public static final String SERVICE_CODE = "MS4";
    public static final String SOURCE_SERVICE = SERVICE_NAME;

    public static final String DEFAULT_MONEDA = "PEN";
    public static final String DEFAULT_SIMBOLO_MONEDA = "S/";
    public static final String REFERENCIA_TIPO_VENTA_MS4 = "VENTA_MS4";

    public static final int DEFAULT_PAGE = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;
    public static final String DEFAULT_SORT_BY = "createdAt";
    public static final String DEFAULT_SORT_DIRECTION = "DESC";

    public static final int MAX_REQUEST_ID_LENGTH = 100;
    public static final int MAX_CORRELATION_ID_LENGTH = 100;
    public static final int MAX_IP_LENGTH = 80;
    public static final int MAX_HEADER_VALUE_LENGTH = 1000;
    public static final int MAX_USER_AGENT_LENGTH = 1000;
    public static final int MAX_PATH_LENGTH = 1000;

    public static final int OUTBOX_MAX_ATTEMPTS = 5;
    public static final int CORREO_MAX_ATTEMPTS = 5;
    public static final int STRIPE_METADATA_MAX_LENGTH = 500;

    public static final BigDecimal ZERO_MONEY = BigDecimal.ZERO.setScale(2);

    public static final String BOLETA_TEMPLATE_HTML = "boleta/boleta";
    public static final String BOLETA_TEMPLATE_MAIL = "mail/boleta-compra";
    public static final String BOLETA_PDF_CONTENT_TYPE = "application/pdf";
    public static final String HTML_CONTENT_TYPE = "text/html;charset=UTF-8";

    public static final String ROLE_ADMIN = "ADMIN";
    public static final String ROLE_EMPLEADO = "EMPLEADO";
    public static final String ROLE_CLIENTE = "CLIENTE";
}