package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SynchronizedStockService implements StockService {

    private final StockRepository stockRepository;

    /**
     * @Transactional 을 사용해서 비동기처리를 할 경우 트랜잭션이 끝나는 시점에 다른 쓰레드가 메소드에 접근할 수 있게 된다.
     * 따라서 DB에 갱신되기 전에 다른 쓰레드가 같은 재고를 조회하게 되면 재고 갱신이 올바르게 처리가 되지 않게 된다.
     * 즉, 스프링의 트렌젝션은 별도의 쓰레드간의 트랜잭션 전파가 이뤄지지 않기 때문에 트랜잭션이 유지되지 않는다.
     */
    //@Transactional
    public synchronized void decreaseStock(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

}
