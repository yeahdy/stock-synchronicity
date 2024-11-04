package com.example.stock.service;

import com.example.stock.domain.Stock;

public interface StockService {
    void decreaseStock(Long id, Long quantity);

    Stock selectStock(Long id);
}
