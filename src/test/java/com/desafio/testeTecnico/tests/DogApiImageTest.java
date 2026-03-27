package com.desafio.testeTecnico.tests;

import com.desafio.testeTecnico.utils.Config;
import com.desafio.testeTecnico.utils.HttpUtils;
import com.desafio.testeTecnico.utils.Assertions;
import com.desafio.testeTecnico.utils.HttpUtils.HttpMethod;
import io.restassured.response.Response;
import org.apache.commons.httpclient.HttpStatus;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("Testes da API de Imagens de Cães")
public class DogApiImageTest extends ApiTestHooks {

    private static final String RANDOM_IMAGE_ENDPOINT = Config.get("endpoint.imagem.aleatoria");

    @Override
    protected void setupTestSuite() {
        logger.info("Configurando suite de testes da API de imagens de cães");
    }

    @Nested
    @DisplayName("Testes da Funcionalidade")
    class TestesFuncionalidadeBasica {

        @Test
        @DisplayName("GET /breeds/image/random retorna imagem com sucesso")
        @Tag("smoke")
        public void deveRetornarImagemAleatoriaComSucesso() {
            logger.info("Executando teste de retorno de imagem aleatória");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "Status code deve ser 200");

            String imageUrl = HttpUtils.getJsonFieldValue(response, "message");
            Assertions.isNotNull(imageUrl, "A URL da imagem não deve ser nula");
            Assertions.verifyContains(imageUrl, "https://", "A URL deve começar com https://");

            logger.info("Teste concluído com sucesso. URL da imagem: {}", imageUrl);
        }

        @Test
        @DisplayName("GET /breeds/image/random valida contrato da resposta")
        @Tag("contrato")
        public void deveValidarContratoDaResposta() {
            logger.info("Executando validação de contrato da API");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            Assertions.validateApiContract(response, "random-image-schema.json");

            logger.info("Validação de contrato concluída com sucesso");
        }
    }

    @Nested
    @DisplayName("Testes de Segurança e Validação")
    class TestesSegurancaEValidacao {

        @Test
        @DisplayName("URL da imagem deve ser segura e válida")
        @Tag("seguranca")
        public void deveRetornarUrlSegura() {
            logger.info("Executando validação de segurança da URL");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            String imageUrl = HttpUtils.getJsonFieldValue(response, "message");

            Assertions.verifyContains(
                    imageUrl,
                    "https://",
                    "URL deve usar HTTPS");

            Assertions.verifyTrue(
                    imageUrl.matches("^https://images\\.dog\\.ceo/.*"),
                    "URL deve ser do domínio correto: " + imageUrl);

            logger.info("Validação de segurança concluída. URL válida: {}", imageUrl);
        }

        @ParameterizedTest(name = "Método {0} não permitido para /breeds/image/random")
        @ValueSource(strings = {"POST", "PUT", "DELETE"})
        @DisplayName("Métodos não permitidos devem retornar 405")
        @Tag("seguranca")
        public void metodosNaoPermitidosDevemRetornarErro(HttpMethod method) {
            logger.info("Testando método não permitido: {}", method);

            Response response = HttpUtils.sendRequest(
                    method,
                    RANDOM_IMAGE_ENDPOINT);

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
        @DisplayName("GET /breeds/image/random deve responder em menos de 3 segundos")
        @Tag("performance")
        public void deveResponderRapidamente() {
            logger.info("Executando teste de performance");

            long startTime = System.currentTimeMillis();

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            long responseTime = System.currentTimeMillis() - startTime;

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "A API deve responder rapidamente");
            Assertions.verifyTrue(responseTime < 3000, "Resposta deve ser menor que 2 segundos, porém a resposta demorou" + responseTime);
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class TestesEdgeCases {

        @Test
        @DisplayName("Deve lidar com parâmetros inválidos")
        @Tag("edge-case")
        public void deveLidarComParametrosInvalidos() {
            logger.info("Executando teste com parâmetros inválidos");

            Response response = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT + "?invalid=param&test=value");

            Assertions.verifyEquality(
                    HttpStatus.SC_OK,
                    response.getStatusCode(),
                    "API deve aceitar parâmetros extras sem erro");

            logger.info("Teste com parâmetros inválidos concluído");
        }

        @Test
        @DisplayName("Deve retornar imagem diferente a cada requisição")
        @Tag("edge-case")
        public void deveRetornarImagensDiferentes() {
            logger.info("Executando teste de unicidade das imagens");

            Response response1 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            Response response2 = HttpUtils.sendRequest(
                    HttpMethod.GET,
                    RANDOM_IMAGE_ENDPOINT);

            String imageUrl1 = HttpUtils.getJsonFieldValue(response1, "message");
            String imageUrl2 = HttpUtils.getJsonFieldValue(response2, "message");

            Assertions.verifyTrue(
                    imageUrl1 != null && imageUrl2 != null && !imageUrl1.equals(imageUrl2),
                    "Ambas as URLs devem ser válidas");

            logger.info("Teste de unicidade concluído. URLs: {} e {}", imageUrl1, imageUrl2);
        }
    }
}
