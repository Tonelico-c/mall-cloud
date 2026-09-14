package com.situ.mall.aspect;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.situ.mall.annotation.MyLog;
import com.situ.mall.pojo.entity.OperLog;
import com.situ.mall.service.IOperLogService;
import com.situ.mall.utils.LoginContext;
import com.situ.mall.utils.Result;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.log4j.Log4j2;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

@Log4j2
@Aspect
@Component
public class OperLogAspect {
    /**
     * 日志级别-INFO
     */
    String LOG_INFO = "INFO";

    /**
     * 日志级别-DEBUG
     */
    String LOG_DEBUG = "DEBUG";

    /**
     * 日志级别-ERROR
     */
    String LOG_ERROR = "ERROR";


    //ThreadLocal是线程隔离的
    private ThreadLocal<OperLog> operLogThreadLocal = new ThreadLocal<>();

    @Autowired
    private IOperLogService operLogService;


    /**
     * 日志切点
     */
//    @Pointcut("execution(public * com.situ.mall.controller.*.*(..))")
    @Pointcut("@annotation(com.situ.mall.annotation.MyLog))")
    public void operLogAspect() {
    }

    /**
     * 前置通知
     *
     * @param joinPoint
     * @throws Throwable
     */
    @Before(value = "operLogAspect()")
    public void doBefore(JoinPoint joinPoint) throws Throwable {
        HttpServletRequest request = ((ServletRequestAttributes) Objects
                .requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
        OperLog operLog = new OperLog();

        Map<String, Object> map = LoginContext.getLoginInfo();
        if (map != null) {
            Long id = (Long) map.get("id");
            String name = (String) map.get("name");
            operLog.setAdminId(id);
            operLog.setAdminName(name);
        }

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        MyLog myLog = method.getAnnotation(MyLog.class);
        if (myLog != null && myLog.module() != null) {
            operLog.setModule(myLog.module());
        }

        operLog.setStartTime(new Date());
        operLog.setRequestUri(URLUtil.getPath(request.getRequestURI()));
        operLog.setRequestParams(formatParams(request.getParameterMap()));
        operLog.setRequestMethod(request.getMethod());
        operLog.setRequestIp(JakartaServletUtil.getClientIP(request));
        operLog.setServerAddress(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort());
        String userAgentStr = request.getHeader("User-Agent");
        operLog.setUserAgent(userAgentStr);
        UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
        operLog.setDeviceName(userAgent.getOs().getName());
        operLog.setBrowserName(userAgent.getBrowser().getName());

        operLogThreadLocal.set(operLog);

        log.info("开始计时: {}  URI: {}  IP: {}", operLog.getStartTime(), operLog.getRequestUri(), operLog.getRequestIp());
    }

    /**
     * 正常返回通知
     *
     * @param ret
     */
    @AfterReturning(pointcut = "operLogAspect()", returning = "ret")
    public void doAfterReturning(Object ret) {
        OperLog operLog = operLogThreadLocal.get();
        operLog.setLogType(LOG_INFO);
        operLog.setEndTime(new Date());
        operLog.setExecuteTime(Math.toIntExact(Long.valueOf(ChronoUnit.MILLIS.between(LocalDateTime.ofInstant(operLog.getStartTime().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(operLog.getEndTime().toInstant(), ZoneId.systemDefault())))));
        operLog.setException(0);
        if (ret instanceof Result) {
            Result r = Convert.convert(Result.class, ret);
            try {
                operLog.setResponseParams(new ObjectMapper().writeValueAsString(r));
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }

        } else if (ret instanceof String) {//转发界面
            operLog.setResponseParams((String) ret);
        }
        operLogService.save(operLog);
        operLogThreadLocal.remove();

        Runtime runtime = Runtime.getRuntime();
        log.info("计时结束: {}  用时: {}ms  URI: {} ", operLog.getEndTime(), operLog.getExecuteTime(),
                operLog.getRequestUri());
    }

    /**
     * 异常通知
     *
     * @param e
     */
    @AfterThrowing(pointcut = "operLogAspect()", throwing = "e")
    public void doAfterThrowable(Throwable e) {
        OperLog operLog = operLogThreadLocal.get();
        operLog.setLogType(LOG_ERROR);
        operLog.setEndTime(new Date());
        //operLog.setExecuteTime(Long.valueOf(ChronoUnit.MINUTES.between(operLog.getStartTime(), operLog.getEndTime())));
        operLog.setExecuteTime(Math.toIntExact(Long.valueOf(ChronoUnit.MILLIS.between(LocalDateTime.ofInstant(operLog.getStartTime().toInstant(), ZoneId.systemDefault()),
                LocalDateTime.ofInstant(operLog.getEndTime().toInstant(), ZoneId.systemDefault())))));
        operLog.setException(1);
        operLog.setExceptionMsg(e.getMessage());
        operLogService.save(operLog);
        operLogThreadLocal.remove();

        log.info("计时结束: {}  用时: {}ms  URI: {} ", operLog.getEndTime(), operLog.getExecuteTime(),
                operLog.getRequestUri());
    }

    /**
     * 格式化参数
     *
     * @param parameterMap
     * @return
     */
    private String formatParams(Map<String, String[]> parameterMap) {
        if (parameterMap == null) {
            return null;
        }
        StringBuilder params = new StringBuilder();
        for (Map.Entry<String, String[]> param : (parameterMap).entrySet()) {
            if (params.length() != 0) {
                params.append("&");
            }
            params.append(param.getKey() + "=");
            if (StrUtil.endWithIgnoreCase(param.getKey(), "password")) {
                params.append("*");
            } else if (param.getValue() != null) {
                params.append(ArrayUtil.join(param.getValue(), ","));
            }
        }
        return params.toString();
    }

}
