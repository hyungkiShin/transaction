package com.transaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;


@Slf4j
@SpringBootTest
class BasicTxTest {

    @Autowired PlatformTransactionManager txManager;

    @TestConfiguration
    static class Config {

        /*
            원래는 스프링 부트가 직접 트랜잭션 매니저도 자동으로 등록해주는데,
            직접 등록하게 되면 자동으로 등록할것 대신에 직접 등록한것을 쓰개 된다.
         */
        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new DataSourceTransactionManager(dataSource);
        }
    }

    @Test
    void commit() {
        log.info("트랜잭션 시작");
        final TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("커밋 시작");
        txManager.commit(status);
        log.info("트랜잭션 커밋 완료");
    }

    @Test
    void rollback() {
        log.info("트랜잭션 시작");
        final TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 롤백 시작");
        txManager.rollback(status);
        log.info("트랜잭션 롤백 완료");
    }

    @Test
    void double_commit() {

        /*
            Connection pull 을 같은걸 쓴다.
            다른걸 쓸수 있는 방법은 없을까 ? 라고 생각했는데

            사실 달랐다 ㅎ
            같은걸 사용 했다고 하는 이유는 커넥션이 같아서 인데.
            hikari connection pool 에서 커넥션을 획득하면, 실제 커넥션을 그대로 반환하는게 아니라
            내부 관리를 위해 hikari proxy connection 이라는 객체를 생성해서 반환한다.
            물론 내부에선 실제 커넥션이 포함되어 있다. 이 객체의 주소를 확인하면 커넥션 풀에서 획득한 커넥션을 구분할 수 있다.
            결론. -> 커넥션만 같은걸 사용했지 ( 반납하면 다시 그걸써 ) 객체는 달랐다. :)
         */

        log.info("트랜잭션1 시작");
        final TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        final TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx2);
    }

    @Test
    void commit_and_rollback() {
        log.info("트랜잭션1 시작");
        final TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 1 커밋");
        txManager.commit(tx1);

        log.info("트랜잭션 1 록백 시작");
        final TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("트랜잭션 1 록백");
        txManager.rollback(tx2);


    }
}
