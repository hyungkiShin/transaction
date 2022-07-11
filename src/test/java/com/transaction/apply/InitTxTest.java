package com.transaction.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
public class InitTxTest {

    @Autowired Hello hello;

    @Test
    @DisplayName("go")
    void go() {
        // 초기화 코드는 스프링이 초기화 시점에 호출된다.
//        hello.initV1(); 직점 호출하면 된다
    }

    @TestConfiguration
    static class InitTxTestConfig {

        @Bean
        Hello hello() {
            return new Hello();
        }
    }

    @Slf4j
    static class Hello {

        @PostConstruct
        @Transactional
        public void initV1() {
            final boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstructor tx Active={}", isActive);
        }

        /*
            spring 이 적절한 타이밍에 넣어주는데
            ApplicationReadyEvent 는 spring container 가 완전히 다 떳다 싶을때
            initV2 를 호출한다.
        */
        @EventListener(ApplicationReadyEvent.class)
        @Transactional
        public void initV2() {
            final boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init ApplicationReadyEvent tx Active={}", isActive);
        }
    }
}
