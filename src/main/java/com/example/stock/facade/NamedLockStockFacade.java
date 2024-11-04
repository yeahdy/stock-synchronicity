package com.example.stock.facade;

import com.example.stock.repository.LockRepository;
import com.example.stock.service.NamedStockService;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {
    private final LockRepository lockRepository;

    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, NamedStockService namedStockService) {
        this.lockRepository = lockRepository;
        this.stockService = namedStockService;
    }

    @Transactional
    public void decrease(Long id, Long quantity){
        try{
            //재고id 로 lock 획득
            lockRepository.getLock(id.toString());
            stockService.decreaseStock(id, quantity);   //★트랜잭션 분리
        }finally {
            //재고id 로 lock 해제
            lockRepository.releaseLock(id.toString());
        }
    }

}
