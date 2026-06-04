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
public class ItemsFallbackProvider implements FallbackProvider {

	private static final Logger log = LoggerFactory.getLogger(ItemsFallbackProvider.class);

	@Override
	public String getRoute() {
		return "servicio-items";
	}

	@Override
	public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
		log.error("[ZUUL][FALLBACK] Ruta '{}' fallo. Causa: {}", route, cause != null ? cause.getMessage() : "desconocida");

		String body;
		HttpStatus status = HttpStatus.OK;

		if (cause != null && cause.getCause() != null
				&& cause.getCause().getMessage() != null
				&& cause.getCause().getMessage().toLowerCase().contains("timed-out")) {
			status = HttpStatus.GATEWAY_TIMEOUT;
			body = "{\"error\":\"TIMEOUT\",\"mensaje\":\"servicio-items tardo mas de 1 segundo. Respuesta alternativa de Zuul.\",\"ruta\":\"" + route + "\"}";
		} else {
			body = "{\"error\":\"SERVICIO_NO_DISPONIBLE\",\"mensaje\":\"servicio-items no responde. Respuesta alternativa de Zuul.\",\"ruta\":\"" + route + "\"}";
		}

		final HttpStatus finalStatus = status;
		final String finalBody = body;

		return new ClientHttpResponse() {

			@Override
			public HttpStatus getStatusCode() throws IOException {
				return finalStatus;
			}

			@Override
			public int getRawStatusCode() throws IOException {
				return finalStatus.value();
			}

			@Override
			public String getStatusText() throws IOException {
				return finalStatus.getReasonPhrase();
			}

			@Override
			public void close() {
				// nada que cerrar
			}

			@Override
			public InputStream getBody() throws IOException {
				return new ByteArrayInputStream(finalBody.getBytes(StandardCharsets.UTF_8));
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
