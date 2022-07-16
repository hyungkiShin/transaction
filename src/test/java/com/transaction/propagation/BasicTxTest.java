package com.transaction.propagation;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;


@Slf4j
@SpringBootTest
class BasicTxTest {

    @Autowired
    PlatformTransactionManager txManager;

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

    @Test
    void inner_commit() {
        final TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer is new Transaction={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        final TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner is new Transaction={}", inner.isNewTransaction());

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }

    @Test
    void inner_rollback() {

        log.info("외부 트랜잭션 시작");
        final TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("내부 트랜잭션 시작");
        final TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(tx2);
        log.info("외부 트랜잭션 커밋");
        txManager.commit(tx1);
    }

    @Test
    void inner_rollback_requires_new() {

        log.info("외부 트랜잭션 시작");
        final TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}", outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        final DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        final TransactionStatus inner = txManager.getTransaction(definition);
        log.info("inner.isNewTransaction()={}", inner.isNewTransaction()); // true

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); // 롤백

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); // 커밋

    }

    /*
    스프링 트랜잭션 전파8 - 다양한 전파 옵션
    스프링은 다양한 트랜잭션 전파 옵션을 제공한다. 전파 옵션에 별도의 설정을 하지 않으면 REQUIRED 가 기본으로 사용된다.
    참고로 실무에서는 대부분 REQUIRED 옵션을 사용한다. 그리고 아주 가끔 REQUIRES_NEW 을 사용하고, 나머지는 거의 사용하지 않는다. 그래서 나머지 옵션은 이런 것이 있다는 정도로만 알아두고 필요할 때 찾아보자.
    REQUIRED
    가장 많이 사용하는 기본 설정이다. 기존 트랜잭션이 없으면 생성하고, 있으면 참여한다. 트랜잭션이 필수라는 의미로 이해하면 된다. (필수이기 때문에 없으면 만들고, 있으면 참여한다.) 기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다.
    기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
    REQUIRES_NEW
    항상 새로운 트랜잭션을 생성한다.
    기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다. 기존 트랜잭션 있음: 새로운 트랜잭션을 생성한다.

    SUPPORT
    트랜잭션을 지원한다는 뜻이다. 기존 트랜잭션이 없으면, 없는대로 진행하고, 있으면 참여한다. 기존 트랜잭션 없음: 트랜잭션 없이 진행한다.
    기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
    NOT_SUPPORT
    트랜잭션을 지원하지 않는다는 의미이다.
    기존 트랜잭션 없음: 트랜잭션 없이 진행한다.
    기존 트랜잭션 있음: 트랜잭션 없이 진행한다. (기존 트랜잭션은 보류한다)
    MANDATORY
    의무사항이다. 트랜잭션이 반드시 있어야 한다. 기존 트랜잭션이 없으면 예외가 발생한다. 기존 트랜잭션 없음: IllegalTransactionStateException 예외 발생
    기존 트랜잭션 있음: 기존 트랜잭션에 참여한다.
    NEVER
    트랜잭션을 사용하지 않는다는 의미이다. 기존 트랜잭션이 있으면 예외가 발생한다. 기존 트랜잭션도 허용하지 않는 강한 부정의 의미로 이해하면 된다.
    기존 트랜잭션 없음: 트랜잭션 없이 진행한다.
    기존 트랜잭션 있음: IllegalTransactionStateException 예외 발생
    NESTED
    기존 트랜잭션 없음: 새로운 트랜잭션을 생성한다.
    기존 트랜잭션 있음: 중첩 트랜잭션을 만든다.
    중첩 트랜잭션은 외부 트랜잭션의 영향을 받지만, 중첩 트랜잭션은 외부에 영향을 주지 않는다. 중첩 트랜잭션이 롤백 되어도 외부 트랜잭션은 커밋할 수 있다.
    외부 트랜잭션이 롤백 되면 중첩 트랜잭션도 함께 롤백된다.
    참고
    JDBC savepoint 기능을 사용한다. DB 드라이버에서 해당 기능을 지원하는지 확인이 필요하다. 중첩 트랜잭션은 JPA에서는 사용할 수 없다.
    트랜잭션 전파와 옵션
    isolation , timeout , readOnly 는 트랜잭션이 처음 시작될 때만 적용된다. 트랜잭션에 참여하는 경우에는 적용되지 않는다.
    예를 들어서 REQUIRED 를 통한 트랜잭션 시작, REQUIRES_NEW 를 통한 트랜잭션 시작 시점에만 적용된다.
     */
}
