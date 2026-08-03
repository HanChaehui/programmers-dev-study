package com.example.spring.boardtoken.aop;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

@Aspect
@Slf4j
@Component
public class LoggingAspect {

    @Pointcut("execution(* com.example.spring.boardtoken.controller..*(..))")
    public void controllerLog() {
        // 메서드 본문(body)은 비워둔다. 실제 로직이 아니라, "대상을 가르키는 이름표"역할만 하기 때문
    }
    @Around("controllerLog()")
    public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {

        String method = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();

        String httpInfo = "";
        // - RequestContextHolder : 스프링이 "지금 이 요청"의 정보를 담아두는 보관소. 어디서든 꺼낼 수 있다
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if ( attributes != null ) {
            HttpServletRequest request = attributes.getRequest();
            httpInfo = request.getMethod() + " " + request.getRequestURI();
        }

        // === 대상 메서드 실행 "전" 로깅 ===
        System.out.println("[요청 시작] " + httpInfo + " -> " + method);

        System.out.println("[파라미터] " + Arrays.toString(joinPoint.getArgs()) );

        long start = System.currentTimeMillis();

        try {
            // 이 한 줄을 기준으로 요청받은 메서드 실행 전, 실행 후 로 나뉜다.
            Object result = joinPoint.proceed();

            // === 대상 메서드가 "정상 종료"된 후 로깅 ===
            long end = System.currentTimeMillis() - start; // 걸린시간
            System.out.println("[요청 완료] " + method + " : " + end + "ms");

            return result;
        } catch ( Throwable e ) {
            // === 대상 메서드가 "예외를 던졌을 때" 로깅 ===
            long end = System.currentTimeMillis() - start; // 걸린시간
            System.out.println("[요청 실패] " + method + " : " + end + "ms" + " : 예외메시지 " + e.getMessage());

            // 잡은 예외를 다시 던진다.
            // - 여기서 예외를 삼켜버리면 컨트롤러는 정상 처리된 것처럼 보여 버그가 된다.
            throw e;
        }
    }

}