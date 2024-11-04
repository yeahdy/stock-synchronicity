package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NamedStockService implements StockService {

    private final StockRepository stockRepository;

    /** @Transactional(propagation = Propagation.REQUIRES_NEW)
        부모/자식 트랜잭션을 분리해서 부모 트랜잭션에서 락을 해제하기 전에 자식 트랜잭션에서 먼저 commit 해서 데이터 갱신하기
        "Lock 획득- 재고감소 - Lock 해제"을 부모 트랜잭션에서 한번에 작업할 경우
        Database 에 commit 되기전에 락이 풀리는 현상이 발생함 (그럼 다른 쓰레드가 점유해서 데이터가 갱신되지 않고 유실될 가능성이 있음)
     */

    /** @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 3)
        부모 트랜잭션에서 모든 커넥션 풀을 점유하면, 자식 트랜잭션에서는 자원이 없어서 Race Condition 발생
        따라서 timeout 설정을 통해 시간 내 자원을 할당받지 못할 경우 예외를 발생시켜서 데드락 방지 & 트랜잭션 롤백

        maximum-pool-size: 30 설정했을 경우 30개의 커넥션이 모두 사용 중일 때, 새로운 요청은 대기 상태로 들어감.
        타임아웃 예외 로그
        SqlExceptionHelper   : HikariPool-1 - Connection is not available, request timed out after 30008ms (total=30, active=30, idle=0, waiting=1)

     */
    @Transactional(propagation = Propagation.REQUIRES_NEW, timeout = 3)
    public void decreaseStock(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

    @Override
    public Stock selectStock(Long id) {return null;}
}
