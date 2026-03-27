package com.desafio.testeTecnico.utils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Classe utilitária para carregar propriedades do arquivo test.properties.
 */
public class Config {

    private static final Properties propriedades = new Properties();

    static {
        try (InputStream entrada = Config.class.getClassLoader().getResourceAsStream("test.properties")) {
            if (entrada == null) {
                throw new RuntimeException("Arquivo test.properties não encontrado no classpath!");
            }
            propriedades.load(entrada);
        } catch (IOException ex) {
            throw new RuntimeException("Erro ao carregar arquivo test.properties", ex);
        }
    }

    /**
     * Recupera o valor associado a uma chave do arquivo test.properties.
     *
     * @param chave Chave da propriedade a ser buscada.
     * @return Valor da propriedade, ou null se a chave não existir.
     */
    public static String get(String chave) {
        return propriedades.getProperty(chave);
    }
}
