package com.example.stock.repository;

import com.example.stock.domain.Stock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 비관적 락(Pessimistic Lock)
    트랜잭션 충돌(동시성문제)이 무조건 발생할 것이라고 가정하고 접근
    모든 트랜잭션은 락을 먼저 획득하고 작업수행. 다른 트랜잭션이 동일한 데이터를 변경하지 못하게 방지
    DB에서 Lock 을 확득할 때까지 대기

 * 낙관적 락(Optimistic Lock)
    데이터 변경 시점에서 버전 검사로 충돌을 감지하고 해결하는 방식
    버전이 불일치 할 경우 예외가 발생하기 때문에 대처 로직이 필요함
    JPA에서는 버전 필드에 @Version 어노테이션을 통해 버저닝 가능
 */
@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
     /** Exclusive Lock (쓰기잠금,배타적 잠금)
        락을 획득하지 못한 트랜잭션은 대상 레코드를 수정,삭제,읽기 불가
     * select for update 구문
        select s1_0.id,s1_0.product_id,s1_0.quantity from stock s1_0 where s1_0.id=? for update
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);


    /** Shared Lock (읽기잠금, 공유 잠금)
        락을 획득한 트랜잭션에서만 대상 레코드를 수정, 삭제 가능. 락을 획득하지 못한 트랜잭션은 읽기만 가능
     * select for share 구문
        select s1_0.id,s1_0.product_id,s1_0.quantity from stock s1_0 where s1_0.id=? for share

     * lock 이 걸린 쓰레드를 update 하려 하면 데드락이 발생
        수정,추가 작업이 없는 읽기 상태를 유지해야할 때 유용함(ex. 계좌 잔액조회, 재고 수량조회)
        동시에 수정을 해야하는 상황에서는 적절하지 않음
     */
    @Lock(LockModeType.PESSIMISTIC_READ)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLockRead(Long id);


    /** Optimistic Lock
     * 1. 업데이트: update stock set product_id=?,quantity=?,version=? where id=? and version=?
     * 2. 버전충돌 에러: Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
     * 3. 최신버전 조회: select s1_0.id,s1_0.product_id,s1_0.quantity,s1_0.version from stock s1_0 where s1_0.id=?
     * 4. 재시도: update stock set product_id=?,quantity=?,version=? where id=? and version=?
     */
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);
}
