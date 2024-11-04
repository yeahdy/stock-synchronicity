package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PessimisticLockStockService implements StockService{

    private final StockRepository stockRepository;

    @Transactional
    public void decreaseStock(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        stock.decrease(quantity);

        stockRepository.save(stock);
    }

    @Transactional
    public Stock selectStock(Long id){
        return stockRepository.findByIdWithPessimisticLockRead(id);
    }

}
