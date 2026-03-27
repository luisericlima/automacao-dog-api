package com.desafio.testeTecnico.utils;

import static org.junit.jupiter.api.Assertions.*;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchema;
import io.restassured.response.Response;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class Assertions {

    /**
     * @param expected Valor esperado.
     * @param actual   Valor atual.
     * @param message  Mensagem a ser exibida se os valores forem diferentes.
     */
    public static void verifyEquality(Object expected, Object actual, String message) {
        assertEquals(expected, actual, message);
    }

    /**
     * @param fullText Texto completo a ser analisado.
     * @param subText  Subtexto que deve estar presente.
     * @param message  Mensagem a ser exibida se o subtexto não estiver presente.
     */
    public static void verifyContains(String fullText, String subText, String message) {
        assertTrue(fullText.contains(subText), message);
    }

    /**
     * @param condition Condição a ser verificada.
     * @param message   Mensagem de erro se a condição não for verdadeira.
     */
    public static void verifyTrue(boolean condition, String message) {
        assertTrue(condition, message);
    }

    /**
     * @param object  Objeto a ser verificado.
     * @param message Mensagem de erro a ser exibida se o objeto for nulo.
     */
    public static void isNotNull(Object object, String message) {
        if (object == null) {
            fail(message);
        }
    }

    /**
     * Valida o contrato da API comparando a resposta com um JSON schema.
     *
     * @param response       Response da requisição HTTP
     * @param schemaFileName Nome do arquivo JSON schema em resources/response/
     * @param message        Mensagem de erro caso a validação falhe
     */
    public static void validateApiContract(Response response, String schemaFileName, String message) {
        try {
            // Carrega o schema JSON do arquivo em resources/response/
            String schemaJson = loadJsonSchema(schemaFileName);

            // Valida se a resposta corresponde ao schema
            response.then().assertThat().body(matchesJsonSchema(schemaJson));

        } catch (Exception e) {
            fail(message + " - Erro na validação do schema: " + e.getMessage());
        }
    }

    /**
     * Valida o contrato da API comparando a resposta com um JSON schema.
     *
     * @param response       Response da requisição HTTP
     * @param schemaFileName Nome do arquivo JSON schema em resources/response/
     */
    public static void validateApiContract(Response response, String schemaFileName) {
        validateApiContract(response, schemaFileName,
                "Resposta não corresponde ao schema esperado: " + schemaFileName);
    }

    /**
     * Carrega um arquivo JSON schema do diretório resources/response/.
     *
     * @param fileName Nome do arquivo JSON schema
     * @return Conteúdo do arquivo como String
     * @throws IOException Se houver erro ao ler o arquivo
     */
    private static String loadJsonSchema(String fileName) throws IOException {
        String resourcePath = "response/" + fileName;

        try (InputStream inputStream = Assertions.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Arquivo de schema não encontrado: " + resourcePath);
            }

            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
