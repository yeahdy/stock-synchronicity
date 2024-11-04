package com.example.stock.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
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
public class StockServicePessimisticTest {

    @Autowired
    @Qualifier("pessimisticLockStockService")
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
    @DisplayName("Exclusive Lock 동시에 100개 재고 요청하기")
    void Exclusive_Lock_동시에_100개_재고_요청하기() throws InterruptedException {
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
               try{
                   stockService.decreaseStock(1L,1L);
               }finally {
                   latch.countDown();
               }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0,stock.getQuantity());
    }

    @Test
    @DisplayName("Shared Lock 동시에 재고 조회하기")
    void Shared_Lock_동시에_재고_조회하기() throws InterruptedException{
        //given
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for(int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    stockService.selectStock(1L);
                }finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(100,stock.getQuantity());
    }

}
