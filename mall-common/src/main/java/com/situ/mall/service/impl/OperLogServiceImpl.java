package com.situ.mall.service.impl;

import com.situ.mall.pojo.entity.OperLog;
import com.situ.mall.mapper.OperLogMapper;
import com.situ.mall.service.IOperLogService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 操作日志记录 服务实现类
 * </p>
 *
 * @author Gao
 * @since 2026-09-14
 */
@Service
public class OperLogServiceImpl extends ServiceImpl<OperLogMapper, OperLog> implements IOperLogService {

}
