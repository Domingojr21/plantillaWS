package com.banreservas.exceptions;

import org.jboss.logging.Logger;

import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.utils.defaults.CodeMessages;

import io.quarkus.security.AuthenticationFailedException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class AuthenticationFailedExceptionMapper implements ExceptionMapper<AuthenticationFailedException> {

    @Context
    private ContainerRequestContext requestContext;

    private static final Logger LOG = Logger.getLogger(AuthenticationFailedExceptionMapper.class);

    @Override
    public Response toResponse(AuthenticationFailedException e) {
        LOG.error(CodeMessages.MESSAGE_UNAUTHORIZED);
        ResponseDto errorResponse = new ResponseDto(
            new ResponseHeaderDto(Response.Status.UNAUTHORIZED.getStatusCode(), CodeMessages.MESSAGE_UNAUTHORIZED), null);
        return Response.status(Response.Status.UNAUTHORIZED)
                .header("sessionId", requestContext.getHeaders().getFirst("sessionId"))
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }
}
