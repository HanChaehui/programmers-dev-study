package com.example.shop.service;

public class ProductServiceImpl implements ProductService{
    @Override
    public String getProduct(String code) {
        sleep(30);
        return "상품: " + code;
    }
    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ignored) {

        }
    }
}
