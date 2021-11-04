package com.backend.hospitalward.util.etag;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.Provider;

@Provider
@DTOSignatureValidator
public class RequestHeaderFilter implements ContainerRequestFilter {

    /**
     * Metoda sprawdzająca obecność i poprawność podpisu przekazanego w żądaniu.
     *
     * @param requestContext Obiekt typu {@link ContainerRequestContext}.
     */
    @Override
    public void filter(ContainerRequestContext requestContext) {
        String header = requestContext.getHeaderString("If-Match");
        if (header == null || header.isEmpty()) {
            requestContext.abortWith(Response.status(Response.Status.BAD_REQUEST).build());
        } else if (ETagValidator.validateDTOSignature(header)) {
            requestContext.abortWith(Response.status(Response.Status.PRECONDITION_FAILED).build());
        }
    }

}
