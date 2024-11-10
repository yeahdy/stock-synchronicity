package com.example.stock.facade;

import com.example.stock.repository.RedisLockRepository;
import com.example.stock.service.NamedStockService;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {
    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, NamedStockService namedStockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = namedStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while(!redisLockRepository.lock(id)){   //spin lock: 락을 획득할때 까지 시도
            Thread.sleep(1000);
        }

        try{
            stockService.decreaseStock(id, quantity);
        }finally {
            redisLockRepository.unlock(id);
        }
    }

}
