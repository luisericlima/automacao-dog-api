package com.desafio.testeTecnico.utils;

import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Utilitário para carregar dados de fixture dos arquivos JSON.
 */
public class FixtureUtils {

    private static final Logger logger = LoggerFactory.getLogger(FixtureUtils.class);

    /**
     * Carrega um arquivo JSON de fixture do diretório resources/fixtures/.
     * 
     * @param fileName Nome do arquivo JSON de fixture
     * @return JSONObject com o conteúdo do arquivo
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public static JSONObject loadFixture(String fileName) throws IOException {
        String resourcePath = "fixtures/" + fileName;
        
        try (InputStream inputStream = FixtureUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (inputStream == null) {
                throw new IOException("Arquivo de fixture não encontrado: " + resourcePath);
            }
            
            String content = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            return new JSONObject(content);
        }
    }

    /**
     * Obtém uma lista de strings de um campo específico do JSON de fixture.
     * 
     * @param fileName Nome do arquivo JSON de fixture
     * @param fieldName Nome do campo que contém a lista
     * @return Lista de strings do campo especificado
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public static List<String> getListFromFixture(String fileName, String fieldName) throws IOException {
        JSONObject fixture = loadFixture(fileName);
        JSONArray array = fixture.getJSONArray(fieldName);
        
        List<String> list = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            list.add(array.getString(i));
        }
        
        logger.debug("Carregadas {} entradas do campo '{}' do arquivo '{}'", 
                list.size(), fieldName, fileName);
        
        return list;
    }

    /**
     * Obtém um inteiro de um campo específico do JSON de fixture.
     * 
     * @param fileName Nome do arquivo JSON de fixture
     * @param fieldName Nome do campo
     * @return Inteiro do campo especificado
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public static int getIntegerFromFixture(String fileName, String fieldName) throws IOException {
        JSONObject fixture = loadFixture(fileName);
        return fixture.getInt(fieldName);
    }

    /**
     * Retorna um Stream de strings de um campo específico do JSON de fixture.
     * Útil para testes parametrizados com @MethodSource.
     * 
     * @param fileName Nome do arquivo JSON de fixture
     * @param fieldName Nome do campo que contém a lista
     * @return Stream de strings do campo especificado
     * @throws IOException Se houver erro ao ler o arquivo
     */
    public static Stream<String> getStreamFromFixture(String fileName, String fieldName) throws IOException {
        List<String> list = getListFromFixture(fileName, fieldName);
        return list.stream();
    }
} 
