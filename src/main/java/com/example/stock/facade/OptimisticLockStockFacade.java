package com.example.stock.facade;

import com.example.stock.service.OptimisticLockStockService;
import com.example.stock.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockStockFacade {
    private final StockService stockService;

    public OptimisticLockStockFacade(OptimisticLockStockService optimisticLockStockService) {
        this.stockService = optimisticLockStockService;
    }

    public void decrease(Long id, Long quantity) throws InterruptedException {
        while(true){    //재고감소 실패를 위한 처리
            try{
                stockService.decreaseStock(id, quantity);
                break;
            }catch (Exception e){
                //Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect) : [com.example.stock.domain.Stock#1], 다시 시도합니다.
                System.out.printf("ERROR :: %s, 다시 시도합니다.%n",e.getMessage());
                Thread.sleep(50);
            }
            //번외: 만약 @Transaction 을 선언하지 않으면 버전 충돌을 감지 하지 못함
            //ERROR :: Query requires transaction be in progress, but no transaction is known to be in progress, 다시 시도합니다.
        }
    }

}
