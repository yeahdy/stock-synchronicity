package com.example.stock.facade;

import com.example.stock.service.NamedStockService;
import com.example.stock.service.StockService;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class RedissonLockStockFacade {

    private RedissonClient redissonClient;
    private StockService stockService;

    public RedissonLockStockFacade(RedissonClient redissonClient, NamedStockService namedStockService) {
        this.redissonClient = redissonClient;
        this.stockService = namedStockService;
    }

    public void decrease(Long id, Long quantity){
        RLock lock = redissonClient.getLock(id.toString());
        try{
            boolean available = lock.tryLock(5,1, TimeUnit.SECONDS);   //10초동안 락획득 시도, 1초동안 점유
            if(!available){
                log.warn("RedissonLock 획득 실패");
                return;
            }
            stockService.decreaseStock(id, quantity);
        }catch (InterruptedException e){
            log.warn("{} 예외 발생",e.getMessage());
            throw new RuntimeException(e);
        }finally {
            lock.unlock();
        }
    }

}
