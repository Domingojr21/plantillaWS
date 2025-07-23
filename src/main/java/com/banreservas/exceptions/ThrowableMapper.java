package com.banreservas.exceptions;

import org.jboss.logging.Logger;

import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.utils.defaults.CodeMessages;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

@Provider
public class ThrowableMapper implements ExceptionMapper<Throwable> {

    @Context
    private ContainerRequestContext requestContext;

    private static final Logger LOG = Logger.getLogger(ThrowableMapper.class);

    @Override
    public Response toResponse(Throwable e) {
        LOG.error(CodeMessages.MESSAGE_INTERNAL_SERVER_ERROR);
        ResponseDto errorResponse = new ResponseDto(
            new ResponseHeaderDto(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), CodeMessages.MESSAGE_INTERNAL_SERVER_ERROR), null);
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                .header("sessionId", requestContext.getHeaders().getFirst("sessionId"))
                .type(MediaType.APPLICATION_JSON)
                .entity(errorResponse)
                .build();
    }

}
