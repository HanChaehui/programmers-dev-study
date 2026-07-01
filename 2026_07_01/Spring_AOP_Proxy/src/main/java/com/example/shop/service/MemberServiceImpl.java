package com.example.shop.service;

public class MemberServiceImpl implements MemberService {
    @Override
    public String register(String id) {
        sleep(50);                       // 실제 작업 흉내
        return "가입완료: " + id;
    }
    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException ignored) {}
    }
}