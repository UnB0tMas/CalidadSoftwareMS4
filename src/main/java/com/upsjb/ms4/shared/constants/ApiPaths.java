// ruta: src/main/java/com/upsjb/ms4/shared/constants/ApiPaths.java
package com.upsjb.ms4.shared.constants;

public final class ApiPaths {

    private ApiPaths() {
    }

    public static final String API = "/api";
    public static final String MS4 = API + "/ms4";
    public static final String INTERNAL = API + "/internal/ms4";

    public static final String CLIENTE = MS4 + "/cliente";
    public static final String EMPLEADO = MS4 + "/empleado";
    public static final String ADMIN = MS4 + "/admin";
    public static final String LOOKUPS = MS4 + "/lookups";
    public static final String WEBHOOKS = MS4 + "/webhooks";

    public static final String CLIENTE_CATALOGO = CLIENTE + "/catalogo";
    public static final String CLIENTE_VENTAS = CLIENTE + "/ventas";
    public static final String CLIENTE_BOLETAS = CLIENTE + "/boletas";
    public static final String CLIENTE_PAGOS_STRIPE = CLIENTE + "/pagos/stripe";

    public static final String EMPLEADO_CLIENTES = EMPLEADO + "/clientes";
    public static final String EMPLEADO_CATALOGO = EMPLEADO + "/catalogo";
    public static final String EMPLEADO_CAJA = EMPLEADO + "/caja";
    public static final String EMPLEADO_VENTAS = EMPLEADO + "/ventas";
    public static final String EMPLEADO_BOLETAS = EMPLEADO + "/boletas";
    public static final String EMPLEADO_REPORTES = EMPLEADO + "/reportes";

    public static final String ADMIN_VENTAS = ADMIN + "/ventas";
    public static final String ADMIN_BOLETAS = ADMIN + "/boletas";
    public static final String ADMIN_CAJAS = ADMIN + "/cajas";
    public static final String ADMIN_REPORTES = ADMIN + "/reportes";
    public static final String ADMIN_CONFIGURACION = ADMIN + "/configuracion";
    public static final String ADMIN_SERIES_BOLETA = ADMIN + "/series-boleta";
    public static final String ADMIN_OUTBOX = ADMIN + "/outbox";
    public static final String ADMIN_CORREOS_OUTBOX = ADMIN + "/correos-outbox";
    public static final String ADMIN_CONTINGENCIA = ADMIN + "/contingencia";
    public static final String ADMIN_SNAPSHOTS = ADMIN + "/snapshots";
    public static final String ADMIN_AUDITORIA = ADMIN + "/auditoria";

    public static final String INTERNAL_STOCK_EVENTS = INTERNAL + "/stock-events";
    public static final String INTERNAL_STOCK_SYNC = INTERNAL + "/stock-sync";
    public static final String STRIPE_WEBHOOK = WEBHOOKS + "/stripe";
}