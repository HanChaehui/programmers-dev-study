package com.example.shop;

import com.example.shop.service.*;
import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.framework.autoproxy.DefaultAdvisorAutoProxyCreator;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AopConfig {

    @Bean
    public static DefaultAdvisorAutoProxyCreator advisorAutoProxyCreator() {
        return new DefaultAdvisorAutoProxyCreator();
    }

    @Bean
    public Advisor monitorAdvisor() {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        pointcut.setExpression("execution(* com.example.shop.service ..*.*(..))");
        return new DefaultPointcutAdvisor(pointcut, new PerformanceMonitorAdvice());
    }

    @Bean
    public MemberService memberService() { return new MemberServiceImpl(); }
    @Bean
    public OrderService orderService() { return new OrderServiceImpl(); }
    @Bean
    public ProductService productService() { return new ProductServiceImpl(); }
}
