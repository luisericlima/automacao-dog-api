package com.desafio.testeTecnico.tests;

import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Classe base abstrata que fornece hooks comuns para todas as classes de teste de API.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class ApiTestHooks {

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Hook executado uma vez antes de todos os testes da classe.
     * Pode ser sobrescrito pelas classes filhas para configurações específicas.
     */
    @BeforeAll
    public void setUp() {
        logger.info("Iniciando suite de testes: {}", this.getClass().getSimpleName());
        setupTestSuite();
    }

    /**
     * Hook executado antes de cada teste individual.
     * Pode ser sobrescrito pelas classes filhas para preparações específicas.
     */
    @BeforeEach
    public void setUpTest() {
        logger.debug("Preparando novo teste em: {}", this.getClass().getSimpleName());
        prepareTest();
    }

    /**
     * Hook executado após cada teste individual.
     * Pode ser sobrescrito pelas classes filhas para limpezas específicas.
     */
    @AfterEach
    public void tearDownTest() {
        logger.debug("Finalizando teste em: {}", this.getClass().getSimpleName());
        cleanupTest();
    }

    /**
     * Hook executado uma vez após todos os testes da classe.
     * Pode ser sobrescrito pelas classes filhas para limpezas finais.
     */
    @AfterAll
    public void tearDown() {
        logger.info("Finalizando suite de testes: {}", this.getClass().getSimpleName());
        cleanupTestSuite();
    }

    /**
     * Método abstrato para configuração específica da suite de testes.
     * Deve ser implementado pelas classes filhas.
     */
    protected abstract void setupTestSuite();

    /**
     * Método para preparação específica de cada teste.
     * Pode ser sobrescrito pelas classes filhas se necessário.
     */
    protected void prepareTest() {}

    /**
     * Método para limpeza específica de cada teste.
     * Pode ser sobrescrito pelas classes filhas se necessário.
     */
    protected void cleanupTest() {}

    /**
     * Método para limpeza final da suite de testes.
     * Pode ser sobrescrito pelas classes filhas se necessário.
     */
    protected void cleanupTestSuite() {}
} 