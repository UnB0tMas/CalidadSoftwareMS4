// ruta: src/main/java/com/upsjb/ms4/integration/cloudinary/CloudinaryExceptionTranslator.java
package com.upsjb.ms4.integration.cloudinary;

import com.cloudinary.api.AuthorizationRequired;
import com.cloudinary.api.exceptions.AlreadyExists;
import com.cloudinary.api.exceptions.ApiException;
import com.cloudinary.api.exceptions.BadRequest;
import com.cloudinary.api.exceptions.GeneralError;
import com.cloudinary.api.exceptions.NotAllowed;
import com.cloudinary.api.exceptions.NotFound;
import com.cloudinary.api.exceptions.RateLimited;
import com.upsjb.ms4.shared.exception.ExternalServiceException;
import org.springframework.stereotype.Component;

@Component
public class CloudinaryExceptionTranslator {

    public ExternalServiceException translate(Exception ex) {
        if (ex instanceof ExternalServiceException externalServiceException) {
            return externalServiceException;
        }

        if (ex instanceof AuthorizationRequired) {
            return new ExternalServiceException("Credenciales Cloudinary inválidas o sin autorización.", ex);
        }

        if (ex instanceof BadRequest) {
            return new ExternalServiceException("La solicitud enviada a Cloudinary no es válida.", ex);
        }

        if (ex instanceof NotAllowed) {
            return new ExternalServiceException("Cloudinary no permite esta operación para el asset visual.", ex);
        }

        if (ex instanceof AlreadyExists) {
            return new ExternalServiceException("Ya existe un asset visual con el publicId indicado en Cloudinary.", ex);
        }

        if (ex instanceof NotFound) {
            return new ExternalServiceException("El asset visual no existe en Cloudinary.", ex);
        }

        if (ex instanceof RateLimited) {
            return new ExternalServiceException("Cloudinary limitó temporalmente la solicitud.", ex);
        }

        if (ex instanceof GeneralError) {
            return new ExternalServiceException("Cloudinary respondió con un error general al procesar el asset visual.", ex);
        }

        if (ex instanceof ApiException) {
            return new ExternalServiceException("Cloudinary rechazó la operación del asset visual.", ex);
        }

        return new ExternalServiceException("No se pudo procesar el asset visual en Cloudinary.", ex);
    }
}