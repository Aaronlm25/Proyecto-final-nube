package com.formacionbdi.springboot.app.zuul.filters;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

@Component
public class PostTiempoTranscurridoFilter extends ZuulFilter {

	private static final Logger log = LoggerFactory.getLogger(PostTiempoTranscurridoFilter.class);

	@Override
	public Object run() throws ZuulException {
		RequestContext ctx = RequestContext.getCurrentContext();
		HttpServletRequest request = ctx.getRequest();

		log.info("Entrando al filtro post (tiempo transcurrido)");

		Long tiempoInicio = (Long) request.getAttribute("tiempoInicio");
		if (tiempoInicio != null) {
			Long tiempoFinal = System.currentTimeMillis();
			Long tiempoTranscurrido = tiempoFinal - tiempoInicio;

			log.info(String.format("Tiempo transcurrido en segundos: %s seg.", tiempoTranscurrido.doubleValue() / 1000.00));
			log.info(String.format("Tiempo transcurrido en milisegundos: %s ms.", tiempoTranscurrido));
		}

		return null;
	}

	@Override
	public boolean shouldFilter() {
		return true;
	}

	@Override
	public int filterOrder() {
		return 1;
	}

	@Override
	public String filterType() {
		return FilterConstants.POST_TYPE; // "post"
	}

}
