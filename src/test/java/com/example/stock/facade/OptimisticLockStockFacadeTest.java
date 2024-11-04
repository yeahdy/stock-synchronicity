package com.example.stock.facade;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import com.example.stock.service.StockService;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class OptimisticLockStockFacadeTest {

    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;

    @Autowired
    @Qualifier("optimisticLockStockService") 
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void beforeEach() {
        stockRepository.saveAndFlush(new Stock(1L,100L));
    }

    @AfterEach
    public void afterEach() {
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("Optimistic Lock 동시에 100개 재고 요청하기")
    void Optimistic_Lock_동시에_100개_재고_요청하기() throws InterruptedException {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    stockService.decreaseStock(1L,1L);
                }catch (Exception e) {
                    //Row was updated or deleted by another transaction (or unsaved-value mapping was incorrect)
                    System.out.printf("ERROR :: %s, %n",e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertNotEquals(0,stock.getQuantity());
    }

    @Test
    @DisplayName("Optimistic Lock 재시도 처리 100개 재고 요청하기")
    void Optimistic_Lock_재시도_처리_100개_재고_요청하기() throws InterruptedException{
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    try {
                        optimisticLockStockFacade.decrease(1L, 1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0,stock.getQuantity());
    }

}
