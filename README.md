# Proyecto Final


## Equipo

* Morales Flores Pablo
* Lopez Mendoza Aaron

## Arquitectura

El proyecto está formado por tres microservicios que se descubren entre sí mediante
Eureka:

* **servicio-eureka-server** (puerto 8761): servidor de descubrimiento.
* **servicio-productos** (puerto aleatorio): microservicio que provoca las fallas. Su
  endpoint `/ver/{id}` lanza una excepción y su endpoint `/ver-lento/{id}` simula
  latencia con un `sleep` de 5 segundos.
* **servicio-item** (puerto 8002): microservicio que consume a productos mediante un
  cliente Feign. Aquí están las anotaciones `@CircuitBreaker` y `@TimeLimiter` y los
  métodos alternativos.

La configuración de Resilience4J está en
`servicio-item/src/main/resources/application.yml`.

## Requisitos

* Java 11
* Maven (se incluye el wrapper `./mvnw` en cada servicio)

## Ejecución

1. Clona el repositorio en tu máquina local:

```
git clone https://github.com/Aaronlm25/Proyecto-final-nube.git
```

3. Es importante el **orden** en que se levantan los servicios, porque item depende de
   los demás. Desde la terminal:

```
# 1) Primero Eureka
cd servicio-eureka-server
./mvnw spring-boot:run

# 2) Luego productos
cd servicio-productos
./mvnw spring-boot:run

# 3) Al final item
cd servicio-item
./mvnw spring-boot:run
```

4. Podemos visualizar el panel de Eureka en:
   http://localhost:8761

## Pruebas

Las llamadas se hacen directamente al microservicio item, en el puerto 8002.

### Falla por excepción (@CircuitBreaker)

```
http://localhost:8002/ver/1/cantidad/2
```


### Falla por latencia (@TimeLimiter)

```
http://localhost:8002/ver2/1/cantidad/2
```

* La configuración de Eureka y el puerto del item están en su `application.properties`,
  mientras que la configuración de Resilience4J está en el `application.yml` del mismo
  servicio. Tener ambos archivos a la vez es válido en Spring Boot.
