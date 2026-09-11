package com.situ.mall.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class ServiceLogAspect {


    /**
     * 切面表达式：
     * execution 代表所要执行的表达式主体
     * 第一处 * 代表方法返回类型, *代表所有类型
     * 第二处 包名代表aop监控的类所在的包
     * 第三处 .. 代表该包以及其子包
     * 第四处 * 代表类名,   *代表所有类
     * 第五处 *(..) *代表类中的方法名，(..)表示方法中的任何参数
     */
    @Around("execution(* com.situ.mall.service.impl.*.*(..))")
    public Object recordTimeLog(ProceedingJoinPoint joinPoint) throws Throwable {
        log.info("=====开始执行 {}.{}========", joinPoint.getTarget().getClass(), joinPoint.getSignature().getName());

        //记录开始时间
        long begin = System.currentTimeMillis();

        //执行目标对象Service中代码
        Object result = joinPoint.proceed();

        long end = System.currentTimeMillis();
        long takeTime = end - begin;
        if (takeTime > 3000) {
            log.error("=====执行结束，耗费 {}毫秒=======", takeTime);
        } else if (takeTime > 2000) {
            log.warn("=====执行结束，耗费 {}毫秒=======", takeTime);
        } else {
            log.info("=====执行结束，耗费 {}毫秒=======", takeTime);

        }

        return result;
    }

}

@Aspect
class TransactionDemo {
    @Pointcut(value="execution(* com.situ.mall.service.impl.*.*(..))")
    public void point(){
    }

    @Before(value="point()")
    public void before(){
        System.out.println("transaction begin");
    }

    @AfterReturning(value = "point()")
    public void after(){
        System.out.println("transaction commit");
    }

    @Around(value = "point()")
    public void around(ProceedingJoinPoint joinPoint) throws Throwable{
        System.out.println("transaction begin");
        joinPoint.proceed();
        System.out.println("transaction commit");
    }
}
