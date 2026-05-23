// ruta: src/main/java/com/upsjb/ms4/shared/audit/AuditContextHolder.java
package com.upsjb.ms4.shared.audit;

public final class AuditContextHolder {

    private static final ThreadLocal<AuditContext> CONTEXT = new ThreadLocal<>();

    private AuditContextHolder() {
    }

    public static void set(AuditContext context) {
        if (context == null) {
            clear();
            return;
        }

        CONTEXT.set(context);
    }

    public static AuditContext get() {
        return CONTEXT.get();
    }

    public static AuditContext getOrEmpty() {
        AuditContext context = CONTEXT.get();
        return context == null ? AuditContext.empty() : context;
    }

    public static void clear() {
        CONTEXT.remove();
    }

    public record AuditContext(
            Long actorIdUsuarioMs1,
            String actorRol,
            String actorUsername,
            String ip,
            String userAgent,
            String requestId,
            String correlationId
    ) {

        public static AuditContext empty() {
            return new AuditContext(null, null, null, null, null, null, null);
        }
    }
}