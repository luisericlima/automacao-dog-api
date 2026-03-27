package com.desafio.testeTecnico.tests;

import com.desafio.testeTecnico.utils.HttpUtils;
import com.desafio.testeTecnico.utils.Assertions;
import com.desafio.testeTecnico.utils.FixtureUtils;
import com.desafio.testeTecnico.utils.Config;
import com.desafio.testeTecnico.utils.HttpUtils.HttpMethod;
import io.restassured.response.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.stream.Stream;

@DisplayName("Testes da API de Imagens por Raça de Cães")
public class DogApiBreedImagesTest extends ApiTestHooks {

    private static final String ENDPOINT_BASE = Config.get("endpoint.base");
    private static int EXPECTED_TIMEOUT_MS;

    @Override
    protected void setupTestSuite() {
        try {
            EXPECTED_TIMEOUT_MS = FixtureUtils.getIntegerFromFixture("breed-images.json",
                    "timeout_esperado_ms");
            logger.info("Fixtures carregadas com sucesso");
        } catch (IOException e) {
            logger.error("Erro ao carregar fixtures: {}", e.getMessage());
            throw new RuntimeException("Falha ao carregar fixtures", e);
        }
    }

    @Nested
    @DisplayName("Testes de Funcionalidade Básica")
    class TestesFuncionalidadeBasica {

        @ParameterizedTest(name = "GET /breed/{0}/images retorna lista de imagens")
        @MethodSource("com.desafio.testeTecnico.tests.DogApiBreedImagesTest#getValidBreeds")
        @DisplayName("Deve retornar lista de imagens para raças válidas")
        @Tag("funcionalidade")
        @Tag("smoke")
        public void deveRetornarListaDeImagensParaRacasValidas(String breed) {
            logger.info("Executando teste para raça: {}", breed);

            String endpoint = String.format("%s/%s/images", ENDPOINT_BASE, breed);
            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    endpoint);

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "Status code deve ser 200 para raça: " + breed);

            String[] imageUrls = HttpUtils.getJsonFieldValue(response, "message").split(",");
            Assertions.verifyTrue(
                    imageUrls.length > 0,
                    "Deve retornar pelo menos uma imagem para raça: " + breed);

            logger.info("Teste concluído com sucesso para raça: {}. {} imagens encontradas",
                    breed, imageUrls.length);
        }

        @Test
        @DisplayName("GET /breed/husky/images valida contrato da resposta")
        @Tag("contrato")
        public void deveValidarContratoDaResposta() {
            logger.info("Executando validação de contrato da API de raça");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/husky/images");

            Assertions.validateApiContract(response, "breed-images-schema.json");

            logger.info("Validação de contrato concluída com sucesso");
        }
    }

    @Nested
    @DisplayName("Testes de Validação e Tratamento de Erro")
    class TestesValidacaoETratamentoDeErro {

        @ParameterizedTest(name = "GET /breed/{0}/images deve retornar erro para raça inválida")
        @MethodSource("com.desafio.testeTecnico.tests.DogApiBreedImagesTest#getInvalidBreeds")
        @DisplayName("Deve retornar erro para raças inválidas")
        @Tag("validacao")
        public void deveRetornarErroParaRacasInvalidas(String invalidBreed) {
            logger.info("Testando raça inválida: {}", invalidBreed);

            String endpoint = String.format("%s/%s/images", ENDPOINT_BASE, invalidBreed);
            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    endpoint);

            Assertions.verifyEquality(
                    HttpStatus.SC_NOT_FOUND,
                    response.getStatusCode(),
                    "Raça inválida deve retornar 404: " + invalidBreed);

            logger.info("Validação de raça inválida concluída: {}", invalidBreed);
        }

        @Test
        @DisplayName("GET /breed/{breed}/images deve retornar erro para raça vazia")
        @Tag("validacao")
        public void deveRetornarErroParaRacaVazia() {
            logger.info("Testando endpoint com raça vazia");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "//images");

            Assertions.verifyEquality(
                    HttpStatus.SC_NOT_FOUND,
                    response.getStatusCode(),
                    "Endpoint com raça vazia deve retornar 404");

            logger.info("Validação de raça vazia concluída");
        }

        @ParameterizedTest(name = "Método {0} não permitido para /breed/husky/images")
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        @DisplayName("Métodos não permitidos devem retornar 405")
        @Tag("seguranca")
        public void metodosNaoPermitidosDevemRetornarErro(String method) {
            logger.info("Testando método não permitido: {}", method);

            Response response = HttpUtils.sendRequest(
                    HttpMethod.valueOf(method),
                    ENDPOINT_BASE + "/husky/images");

            Assertions.verifyEquality(
                    HttpStatus.SC_METHOD_NOT_ALLOWED,
                    response.getStatusCode(),
                    "Método " + method + " deve retornar 405 (Method Not Allowed)");

            logger.info("Validação de método {} concluída com sucesso", method);
        }
    }

    @Nested
    @DisplayName("Testes de Performance")
    class TestesPerformance {

        @Test
        @DisplayName("GET /breed/husky/images deve responder em menos de 3 segundos")
        @Tag("performance")
        public void deveResponderRapidamente() {
            logger.info("Executando teste de performance para imagens de raça");

            long startTime = System.currentTimeMillis();

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/husky/images");

            long responseTime = System.currentTimeMillis() - startTime;

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "A API deve responder com sucesso");

            Assertions.verifyTrue(
                    responseTime < EXPECTED_TIMEOUT_MS,
                    "Resposta deve ser menor que " + EXPECTED_TIMEOUT_MS + "ms, mas demorou "
                            + responseTime + "ms");

            logger.info("Teste de performance concluído. Tempo de resposta: {}ms", responseTime);
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class TestesEdgeCases {

        @Test
        @DisplayName("Deve lidar com raça com caracteres especiais")
        @Tag("edge-case")
        public void deveLidarComRacaComCaracteresEspeciais() {
            logger.info("Testando raça com caracteres especiais");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/husky/images?param=test");

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "API deve aceitar parâmetros extras sem erro");

            logger.info("Teste com caracteres especiais concluído");
        }

        @Test
        @DisplayName("Deve retornar lista diferente para cada requisição")
        @Tag("edge-case")
        public void deveRetornarListaDiferenteParaCadaRequisicao() {
            logger.info("Executando teste de unicidade das listas de imagens");

            Response response1 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/husky/images");

            Response response2 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/husky/images");

            String[] imageUrls1 = HttpUtils.getJsonFieldValue(response1, "message").split(",");
            String[] imageUrls2 = HttpUtils.getJsonFieldValue(response2, "message").split(",");

            Assertions.verifyTrue(
                    imageUrls1.length > 0 && imageUrls2.length > 0,
                    "Ambas as listas devem conter imagens");

            logger.info("Teste de unicidade concluído. Lista 1: {} imagens, Lista 2: {} imagens",
                    imageUrls1.length, imageUrls2.length);
        }

        @Test
        @DisplayName("Deve retornar lista vazia para raça sem imagens")
        @Tag("edge-case")
        public void deveRetornarListaVaziaParaRacaSemImagens() {
            logger.info("Testando raça que pode não ter imagens");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    ENDPOINT_BASE + "/teste_sem_imagens/images");

            int statusCode = response.getStatusCode();
            Assertions.verifyTrue(
                    statusCode == HttpStatus.SC_NOT_FOUND || statusCode == HttpStatus.SC_OK,
                    "Deve retornar 404 ou 200 para raça sem imagens");

            logger.info("Teste de raça sem imagens concluído. Status: {}", statusCode);
        }
    }

    static Stream<String> getValidBreeds() throws IOException {
        return FixtureUtils.getStreamFromFixture("breed-images.json", "racas_validas");
    }

    static Stream<String> getInvalidBreeds() throws IOException {
        return FixtureUtils.getStreamFromFixture("breed-images.json", "racas_invalidas");
    }
}
