package com.desafio.testeTecnico.tests;

import com.desafio.testeTecnico.utils.HttpUtils;
import com.desafio.testeTecnico.utils.Assertions;
import com.desafio.testeTecnico.utils.Config;
import com.desafio.testeTecnico.utils.FixtureUtils;
import com.desafio.testeTecnico.utils.HttpUtils.HttpMethod;

import io.restassured.response.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.List;

@DisplayName("Testes da API de Lista de Raças de Cães")
public class DogApiBreedsListTest extends ApiTestHooks {

    private static final String ENDPOINT_LISTA_RACAS = Config.get("endpoint.lista.racas");
    private static List<String> RACAS_ESPERADAS;
    private static List<String> RACAS_POPULARES;

    @Override
    protected void setupTestSuite() {
        try {
            RACAS_ESPERADAS = FixtureUtils.getListFromFixture("racas.json", "racas_esperadas");
            RACAS_POPULARES = FixtureUtils.getListFromFixture("racas.json", "racas_populares");
            logger.info("Fixtures carregadas com sucesso");
        } catch (IOException e) {
            logger.error("Erro ao carregar fixtures: {}", e.getMessage());
            throw new RuntimeException("Falha ao carregar fixtures", e);
        }
    }

    @Nested
    @DisplayName("Testes de Funcionalidade Básica")
    class TestesFuncionalidadeBasica {

        @Test
        @DisplayName("GET /breeds/list/all retorna lista de raças com sucesso")
        @Tag("funcionalidade")
        @Tag("smoke")
        public void deveRetornarListaDeRacasComSucesso() {
            logger.info("Executando teste de retorno de lista de raças");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "Status code deve ser 200");

            String messageField = HttpUtils.getJsonFieldValue(response, "message");
            Assertions.isNotNull(messageField, "O campo message não deve ser nulo");

            String statusField = HttpUtils.getJsonFieldValue(response, "status");
            Assertions.verifyEquality("success", statusField, "Status deve ser 'success'");

            logger.info("Teste concluído com sucesso. Lista de raças obtida");
        }

        @Test
        @DisplayName("GET /breeds/list/all valida contrato da resposta")
        @Tag("contrato")
        public void deveValidarContratoDaResposta() {
            logger.info("Executando validação de contrato da API de lista de raças");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            Assertions.validateApiContract(response, "breeds-list-schema.json");

            logger.info("Validação de contrato concluída com sucesso");
        }

        @Test
        @DisplayName("GET /breeds/list/all deve conter raças conhecidas")
        @Tag("funcionalidade")
        public void deveConterRacasConhecidas() {
            logger.info("Executando validação de raças conhecidas");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            String messageField = HttpUtils.getJsonFieldValue(response, "message");
            Assertions.isNotNull(messageField, "O campo message não deve ser nulo");

            for (String racaEsperada : RACAS_ESPERADAS.subList(0, Math.min(5, RACAS_ESPERADAS.size()))) {
                Assertions.verifyContains(
                        messageField.toLowerCase(),
                        racaEsperada.toLowerCase(),
                        "A lista deve conter a raça: " + racaEsperada);
            }

            logger.info("Validação de raças conhecidas concluída");
        }
    }

    @Nested
    @DisplayName("Testes de Validação e Tratamento de Erro")
    class TestesValidacaoETratamentoDeErro {

        @ParameterizedTest(name = "Método {0} não permitido para /breeds/list/all")
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        @DisplayName("Métodos não permitidos devem retornar 405")
        @Tag("seguranca")
        public void metodosNaoPermitidosDevemRetornarErro(String metodo) {
            logger.info("Testando método não permitido: {}", metodo);

            Response response = HttpUtils.sendRequest(
                    HttpMethod.valueOf(metodo),
                    ENDPOINT_LISTA_RACAS);

            Assertions.verifyEquality(
                    HttpStatus.SC_METHOD_NOT_ALLOWED,
                    response.getStatusCode(),
                    "Método " + metodo + " deve retornar 405 (Method Not Allowed)");

            logger.info("Validação de método {} concluída com sucesso", metodo);
        }

        @Test
        @DisplayName("GET /breeds/list/all deve retornar erro para endpoint inválido")
        @Tag("validacao")
        public void deveRetornarErroParaEndpointInvalido() {
            logger.info("Testando endpoint inválido");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    "/breeds/list/invalid");

            Assertions.verifyEquality(
                    HttpStatus.SC_NOT_FOUND,
                    response.getStatusCode(),
                    "Endpoint inválido deve retornar 404");

            logger.info("Validação de endpoint inválido concluída");
        }
    }

    @Nested
    @DisplayName("Testes de Performance")
    class TestesPerformance {

        @Test
        @DisplayName("GET /breeds/list/all deve responder em menos de 3 segundos")
        @Tag("performance")
        public void deveResponderRapidamente() {
            logger.info("Executando teste de performance para lista de raças");

            long startTime = System.currentTimeMillis();

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            long responseTime = System.currentTimeMillis() - startTime;

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "A API deve responder com sucesso");

            Assertions.verifyTrue(
                    responseTime < 3000,
                    "Resposta deve ser menor que 3 segundos, mas demorou " + responseTime + "ms");

            logger.info("Teste de performance concluído. Tempo de resposta: {}ms", responseTime);
        }

        @Test
        @DisplayName("GET /breeds/list/all deve responder consistentemente")
        @Tag("performance")
        public void deveResponderConsistentemente() {
            logger.info("Executando teste de consistência de resposta");

            Response response1 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            Response response2 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            Assertions.verifyEquality(
                    response1.getStatusCode(),
                    response2.getStatusCode(),
                    "Ambas as respostas devem ter o mesmo status code");

            String status1 = HttpUtils.getJsonFieldValue(response1, "status");
            String status2 = HttpUtils.getJsonFieldValue(response2, "status");

            Assertions.verifyEquality(
                    status1,
                    status2,
                    "Ambas as respostas devem ter o mesmo status");

            logger.info("Teste de consistência concluído com sucesso");
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class TestesEdgeCases {

        @Test
        @DisplayName("Deve retornar estrutura consistente")
        @Tag("edge-case")
        public void deveRetornarEstruturaConsistente() {
            logger.info("Executando teste de estrutura consistente");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            String messageField = HttpUtils.getJsonFieldValue(response, "message");
            String statusField = HttpUtils.getJsonFieldValue(response, "status");

            Assertions.isNotNull(messageField, "Campo message deve existir");
            Assertions.isNotNull(statusField, "Campo status deve existir");
            Assertions.verifyEquality("success", statusField, "Status deve ser 'success'");

            logger.info("Teste de estrutura consistente concluído");
        }

        @Test
        @DisplayName("Deve conter múltiplas raças")
        @Tag("edge-case")
        public void deveConterMultiplasRacas() {
            logger.info("Executando teste de quantidade de raças");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            String messageField = HttpUtils.getJsonFieldValue(response, "message");

            long contagemRacas = messageField.split(",").length;
            Assertions.verifyTrue(
                    contagemRacas >= 10,
                    "A lista deve conter pelo menos 10 raças, mas contém: " + contagemRacas);

            logger.info("Teste de quantidade de raças concluído. {} raças encontradas", contagemRacas);
        }
    }

    @Nested
    @DisplayName("Testes de Conteúdo")
    class TestesConteudo {

        @Test
        @DisplayName("Deve conter raças populares")
        @Tag("conteudo")
        public void deveConterRacasPopulares() {
            logger.info("Executando validação de raças populares");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            String messageField = HttpUtils.getJsonFieldValue(response, "message");
            String messageLower = messageField.toLowerCase();

            for (String raca : RACAS_POPULARES) {
                Assertions.verifyContains(
                        messageLower,
                        raca,
                        "A lista deve conter a raça popular: " + raca);
            }

            logger.info("Validação de raças populares concluída");
        }

        @Test
        @DisplayName("Deve ter formato de lista válido")
        @Tag("conteudo")
        public void deveTerFormatoDeListaValido() {
            logger.info("Executando validação de formato de lista");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_LISTA_RACAS);

            String messageField = HttpUtils.getJsonFieldValue(response, "message");

            Assertions.verifyTrue(
                    messageField.contains(","),
                    "A resposta deve conter múltiplas raças separadas por vírgula");

            Assertions.verifyTrue(
                    !messageField.contains("null"),
                    "A resposta não deve conter valores null");

            logger.info("Validação de formato de lista concluída");
        }
    }
}
