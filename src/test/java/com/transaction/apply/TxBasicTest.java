package com.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.transaction.Transactional;

@Slf4j
@SpringBootTest
public class TxBasicTest {

    @Autowired
    BasicService basicService;

    @Test
    @DisplayName("트랜잭션 탔니")
    void 트랜잭션_탔니() {
        log.info("aop class 는={}", basicService.getClass());
        Assertions.assertThat(AopUtils.isAopProxy(basicService)).isTrue();
    }

    @Test
    @DisplayName("둘다_호출할꺼야")
    void 둘다_호출할꺼야() {
        basicService.tx();
        basicService.nonTx();
    }

    @TestConfiguration
    static class TxApplyBasicConfig {

        @Bean
        BasicService basicService() {
            return new BasicService();
        }
    }

    @Slf4j
    static class BasicService {

        @Transactional
        public void tx() {
            log.info("call tx");
            final boolean txActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("isTransaction={}", txActive);
        }

        public void nonTx() {
            log.info("call non tx");
            final boolean transactionActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("transactionActive={}", transactionActive);
        }
    }
}
