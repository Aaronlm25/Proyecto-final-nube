package com.formacionbdi.springboot.app.zuul.fallback;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.route.FallbackProvider;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

@Component
public class ProductosFallbackProvider implements FallbackProvider {

	private static final Logger log = LoggerFactory.getLogger(ProductosFallbackProvider.class);

	@Override
	public String getRoute() {
		return "servicio-productos";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		log.error("[ZUUL][FALLBACK] Ruta '{}' fallo. Causa: {}", route, cause != null ? cause.getMessage() : "desconocida");

		if (cause != null && cause.getCause() != null) {
			String mensaje = cause.getCause().getMessage();
			if (mensaje != null && mensaje.toLowerCase().contains("timed-out")) {
				return buildResponse(HttpStatus.GATEWAY_TIMEOUT,
						"{\"error\":\"TIMEOUT\",\"mensaje\":\"El microservicio servicio-productos tardo mas de 1 segundo. Hystrix abrio el circuito y devolvio esta respuesta alternativa.\",\"ruta\":\"" + route + "\"}");
			}
		}
		return buildResponse(HttpStatus.OK,
				"{\"error\":\"SERVICIO_NO_DISPONIBLE\",\"mensaje\":\"El microservicio servicio-productos no esta disponible. Respuesta alternativa devuelta por el FallbackProvider de Zuul.\",\"ruta\":\"" + route + "\"}");
	}

	private ClientHttpResponse buildResponse(final HttpStatus status, final String body) {
		return new ClientHttpResponse() {

			@Override
			public HttpStatus getStatusCode() throws IOException {
				return status;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return status.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return status.getReasonPhrase();
			}

			@Override
			public void close() {
				// nada que cerrar
			}

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));
			}

			@Override
			public HttpHeaders getHeaders() {
				HttpHeaders headers = new HttpHeaders();
				headers.setContentType(MediaType.APPLICATION_JSON);
				return headers;
			}
		};
	}

}
