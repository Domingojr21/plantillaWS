package com.banreservas.services.implementations;

import org.eclipse.microprofile.faulttolerance.CircuitBreaker;
import org.eclipse.microprofile.faulttolerance.Retry;

import com.banreservas.dtos.inbound.RequestDto;
import com.banreservas.dtos.inbound.ResponseValidatesJointAccountsDto;
import com.banreservas.dtos.outbound.ResponseWsDto;
import com.banreservas.services.contracts.IValidatesJointAccountsService;
import com.brrd.service.services.IValidarRequest;
import com.brrd.service.services.ServiceResponse;
import com.brrd.service.services.ValidarRequest;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;

import io.quarkiverse.cxf.annotation.CXFClient;
import io.quarkus.logging.Log;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.infrastructure.Infrastructure;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 *
 * @author Ing. Victor Diaz - vjdiaz@banreservas.com
 * @since 28-01-2025
 * @version 1.0
 */

@ApplicationScoped
public class ValidatesJointAccountsService implements IValidatesJointAccountsService {

	@Inject
	@CXFClient("validaCuentasMancomunadas")
	private IValidarRequest _validarRequest;

	@Override
	@Retry(maxRetries = 3, delay = 500, retryOn = {
			RuntimeException.class
	})
	@CircuitBreaker(requestVolumeThreshold = 4, failureRatio = 0.5, delay = 1000)
	public Uni<ResponseValidatesJointAccountsDto> validateAccounts(RequestDto request,
			MultivaluedMap<String, String> headers)
			throws JsonProcessingException {

		// Construyendo el request para el backend soap
		ValidarRequest validarRequest = new ValidarRequest();

		validarRequest.setChannel(headers.getFirst("id_consumidor"));
		validarRequest.setDate(headers.getFirst("fecha_hora"));
		validarRequest.setOperationName(headers.getFirst("operacion"));
		validarRequest.setTerminal(headers.getFirst("terminal"));
		validarRequest.setUser(headers.getFirst("usuario"));
		validarRequest.setCuentaOrigen(request.originAccount());
		validarRequest.setCuentaDestino(request.destinyAccount());

		Log.infov("Soap Request, cuenta origen: {0}, cuenta destino: {1}", request.originAccount().toString(),
				request.destinyAccount().toString());

		return Uni.createFrom().item(() -> {
			try {
				ServiceResponse soapResponse = _validarRequest.validar(validarRequest);

				if ("000".equals(soapResponse.getErrorCode())) {
					XmlMapper xmlMapper = new XmlMapper();
					ResponseWsDto responseWsDto = xmlMapper.readValue(soapResponse.getXMLReresponse(),
							ResponseWsDto.class);

					ResponseValidatesJointAccountsDto response = new ResponseValidatesJointAccountsDto(
							"TRUE".equals(responseWsDto.getValidProduct().getFirst().getProductIsValid()));

					return response;

				} else {
					throw new IllegalArgumentException(soapResponse.getErrorMessage());
				}

			} catch (Exception e) {
				throw new RuntimeException(e.getMessage());
			}
		}).runSubscriptionOn(Infrastructure.getDefaultExecutor());
	}
}
