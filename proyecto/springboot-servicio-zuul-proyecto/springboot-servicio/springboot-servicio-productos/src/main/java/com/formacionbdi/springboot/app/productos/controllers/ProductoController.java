package com.formacionbdi.springboot.app.productos.controllers;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.springboot.app.productos.models.entity.Producto;
import com.formacionbdi.springboot.app.productos.models.service.IProductoService;

@RestController
public class ProductoController {

    @Autowired
    private Environment env;

    @Value("${server.port}")
    private Integer port;

    @Autowired
    private IProductoService productoService;

    @GetMapping("/listar")
    public List<Producto> listar() {
        return productoService.findAll().stream().map(producto -> {
            producto.setPort(Integer.parseInt(env.getProperty("local.server.port")));
            return producto;
        }).collect(Collectors.toList());
    }

    // endpoint normal para probar @CircuitBreaker
    @GetMapping("/ver/{id}")
    public Producto detalle(@PathVariable Long id) {
        throw new IllegalStateException("Producto no encontrado!");
        // Cuando quieras que funcione normal, comenta la línea de arriba y descomenta esto:
        // Producto producto = productoService.findById(id);
        // producto.setPort(Integer.parseInt(env.getProperty("local.server.port")));
        // return producto;
    }

    // endpoint lento para probar @TimeLimiter
    @GetMapping("/ver-lento/{id}")
    public Producto detalleLento(@PathVariable Long id) throws InterruptedException {
        TimeUnit.SECONDS.sleep(5L);  
        Producto producto = productoService.findById(id);
        producto.setPort(Integer.parseInt(env.getProperty("local.server.port")));
        return producto;
    }
}