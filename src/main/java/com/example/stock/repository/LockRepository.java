package com.example.stock.repository;

import com.example.stock.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Named Lock
    MySQL의 USER-LEVEL LOCK 관련 함수를 사용 해 분산 락을 처리할 때 주로 사용

 * GET_LOCK(str,timeout)
    입력받은 str 으로 timeout 초 동안 잠금 획득을 시도
    비관적 락(Pessimistic Lock)에 비해 timeout 을 쉽게 구현할 수 있음
    GET_LOCK으로 획득한 잠금은 트랜잭션의 상태와 무관하게 유지되기 때문에
    Transaction 이 commit 되거나 rollback 되어도 종료되지 않고 수동 락 해제가 필요

 * RELEASE_LOCK(str)
    입력받은 str 의 잠금 해제

 * IS_FREE_LOCK(str)
    입력한 str 에 해당하는 잠금이 획득 가능한지 확인 (1,0,null 반환)
    1 : 입력한 이름의 잠금이 없을때 / 0 : 입력한 이름의 잠금이 있을때
 */
public interface LockRepository extends JpaRepository<Stock,Long> {
    @Query(value = "select get_lock(:key,3000)", nativeQuery = true)
    void getLock(@Param("key") String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(@Param("key") String key);
}
