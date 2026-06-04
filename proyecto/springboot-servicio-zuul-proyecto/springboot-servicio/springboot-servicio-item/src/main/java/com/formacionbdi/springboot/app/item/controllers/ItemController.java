package com.formacionbdi.springboot.app.item.controllers;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.formacionbdi.springboot.app.item.models.Item;
import com.formacionbdi.springboot.app.item.models.Producto;
import com.formacionbdi.springboot.app.item.models.service.ItemService;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;

@RestController
public class ItemController {

    private final Logger logger = LoggerFactory.getLogger(ItemController.class);

    @Autowired
    @Qualifier("serviceFeign")
    private ItemService itemService;

    @GetMapping("/listar")
    public List<Item> listar(@RequestParam(name="nombre", required=false) String nombre,
                             @RequestHeader(name="token-request", required=false) String token) {
        System.out.println(nombre);
        System.out.println(token);
        return itemService.findAll();
    }

    // @CircuitBreaker (falla por excepción)
    @CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativo")
    @GetMapping("/ver/{id}/cantidad/{cantidad}")
    public Item detalle(@PathVariable Long id, @PathVariable Integer cantidad) {
        return itemService.findById(id, cantidad);
    }

    public Item metodoAlternativo(Long id, Integer cantidad, Throwable e) {
        logger.error("[CircuitBreaker] Falla en servicio-productos, id={}, error: {}", id, e.getMessage());
        Item item = new Item();
        Producto producto = new Producto();
        item.setCantidad(cantidad);
        producto.setId(id);
        producto.setNombre("Camara Sony");
        producto.setPrecio(500.00);
        item.setProducto(producto);
        return item;
    }

    // @TimeLimiter (falla por latencia)
    @CircuitBreaker(name = "items", fallbackMethod = "metodoAlternativoTimeout")
    @TimeLimiter(name = "items")
    @GetMapping("/ver2/{id}/cantidad/{cantidad}")
    public CompletableFuture<Item> detalle2(@PathVariable Long id, @PathVariable Integer cantidad) {
        return CompletableFuture.supplyAsync(() -> itemService.findByIdLento(id, cantidad));
    }

    public CompletableFuture<Item> metodoAlternativoTimeout(Long id, Integer cantidad, Throwable e) {
        logger.error("[TimeLimiter] Timeout en servicio-productos, id={}, error: {}", id, e.getMessage());
        Item item = new Item();
        Producto producto = new Producto();
        item.setCantidad(cantidad);
        producto.setId(id);
        producto.setNombre("Camara Sony");
        producto.setPrecio(500.00);
        item.setProducto(producto);
        return CompletableFuture.completedFuture(item);
    }
}