package com.desafio.testeTecnico.utils;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Utilitário HTTP simplificado para execução de requisições de API.
 * Implementa um método centralizado sendRequest com switch case.
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);
    private static final String BASE_URL = Config.get("base.url");
    
    /**
     * Enum para definir os tipos de requisição HTTP suportados.
     */
    public enum HttpMethod {
        GET, POST, PUT, DELETE
    }

    /**
     * Configuração base para todas as requisições HTTP.
     * 
     * @return RequestSpecification configurado
     */
    private static RequestSpecification getBaseRequest() {
        return RestAssured.given()
                .filter(new AllureRestAssured())
                .contentType(ContentType.JSON)
                .accept(ContentType.JSON)
                .baseUri(BASE_URL)
                .log().all();
    }

    /**
     * Método centralizado para enviar requisições HTTP com switch case.
     * 
     * @param method Método HTTP (GET, POST, PUT, DELETE)
     * @param endpoint Endpoint da API
     * @param body Body da requisição (opcional para GET e DELETE)
     * @param headers Headers customizados (opcional)
     * @return Response da requisição
     */
    public static Response sendRequest(HttpMethod method, String endpoint, String body, Map<String, String> headers) {
        logger.info("Executando {} para: {} com body: {} e headers: {}", method, endpoint, body, headers);
        
        RequestSpecification request = getBaseRequest();
        
        // Adiciona headers customizados se fornecidos
        if (headers != null && !headers.isEmpty()) {
            headers.forEach(request::header);
        }
        
        // Adiciona body se fornecido
        if (body != null && !body.isEmpty()) {
            request.body(body);
        }
        
        Response response = null;
        
        switch (method) {
            case GET:
                response = request
                        .when()
                        .get(endpoint)
                        .then()
                        .log().all()
                        .extract().response();
                break;
                
            case POST:
                response = request
                        .when()
                        .post(endpoint)
                        .then()
                        .log().all()
                        .extract().response();
                break;
                
            case PUT:
                response = request
                        .when()
                        .put(endpoint)
                        .then()
                        .log().all()
                        .extract().response();
                break;
                
            case DELETE:
                response = request
                        .when()
                        .delete(endpoint)
                        .then()
                        .log().all()
                        .extract().response();
                break;
                
            default:
                throw new IllegalArgumentException("Método HTTP não suportado: " + method);
        }
        
        logger.info("Status code: {}", response.getStatusCode());
        return response;
    }

    /**
     * Sobrecarga do método sendRequest sem body e headers.
     * 
     * @param method Método HTTP
     * @param endpoint Endpoint da API
     * @return Response da requisição
     */
    public static Response sendRequest(HttpMethod method, String endpoint) {
        return sendRequest(method, endpoint, null, null);
    }

    /**
     * Sobrecarga do método sendRequest com body mas sem headers.
     * 
     * @param method Método HTTP
     * @param endpoint Endpoint da API
     * @param body Body da requisição
     * @return Response da requisição
     */
    public static Response sendRequest(HttpMethod method, String endpoint, String body) {
        return sendRequest(method, endpoint, body, null);
    }

    /**
     * Sobrecarga do método sendRequest com headers mas sem body.
     * 
     * @param method Método HTTP
     * @param endpoint Endpoint da API
     * @param headers Headers customizados
     * @return Response da requisição
     */
    public static Response sendRequest(HttpMethod method, String endpoint, Map<String, String> headers) {
        return sendRequest(method, endpoint, null, headers);
    }

    /**
     * Obtém o valor de um campo específico do JSON da resposta.
     * 
     * @param response Response da requisição
     * @param jsonPath Caminho do campo no JSON
     * @return Valor do campo
     */
    public static String getJsonFieldValue(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }
}
