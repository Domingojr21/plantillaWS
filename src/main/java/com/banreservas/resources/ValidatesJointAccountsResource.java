package com.banreservas.resources;

import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseDto;
import com.banreservas.dtos.inbound.ResponseHeaderDto;
import com.banreservas.services.contracts.IValidatesJointAccountsService;
import com.banreservas.utils.defaults.CodeMessages;
import com.banreservas.utils.defaults.RequestHeadersValidator;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.quarkus.security.Authenticated;
import io.smallrye.mutiny.Uni;
import jakarta.annotation.security.RolesAllowed;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;

/**
 *
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 28-01-2025
 * @version 1.0
 */

@Path("/v1")
@Authenticated
@ApplicationScoped
public class ValidatesJointAccountsResource {

	@Inject
	private IValidatesJointAccountsService _validaCuentasMancomunadasService;

	@POST
	@RolesAllowed("valida-cuentas-mancomunadas")
	@Path("/valida-cuentas-mancomunadas")
	@Produces(MediaType.APPLICATION_JSON)
	@Consumes(MediaType.APPLICATION_JSON)
	public Uni<Response> validateAccounts(@Valid RequestDto requestDto, @Context HttpHeaders httpHeaders)
			throws JsonProcessingException {

		// Validación de los encabezados de la solicitud
		String responseHeaderValidation = RequestHeadersValidator.validateRequestHeaders(httpHeaders);
		if (!responseHeaderValidation.equalsIgnoreCase("valid")) {
			return Uni.createFrom().item(Response.status(Response.Status.BAD_REQUEST)
					.entity(new ResponseDto(
							new ResponseHeaderDto(Response.Status.BAD_REQUEST.getStatusCode(),
									responseHeaderValidation),
							null))
					.build());
		}

		MultivaluedMap<String, String> headers = httpHeaders.getRequestHeaders();

		// Llamada reactiva al servicio
		return _validaCuentasMancomunadasService.validateAccounts(requestDto,
				headers)
				.onItem().transform(responseDto -> {
					// Construir la respuesta cuando el resultado esté disponible
					return Response.ok(new ResponseDto(
							new ResponseHeaderDto(Response.Status.OK
									.getStatusCode(),
									CodeMessages.MESSAGE_SUCCESS),
							responseDto))
							.header("id_consumidor", headers.getFirst("id_consumidor"))
							.header("usuario", headers.getFirst("usuario"))
							.header("fecha_hora", headers.getFirst("fecha_hora"))
							.header("terminal", headers.getFirst("terminal"))
							.header("operacion", headers.getFirst("operacion"))
							.header("sessionId", headers.getFirst("sessionId"))
							.build();
				});
	}
}
