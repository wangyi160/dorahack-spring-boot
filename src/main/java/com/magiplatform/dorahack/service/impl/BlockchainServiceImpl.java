package com.magiplatform.dorahack.service.impl;


import com.magiplatform.dorahack.entity.Blockchain;

import com.magiplatform.dorahack.mapper.BlockchainMapper;

import com.magiplatform.dorahack.service.IBlockchainService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 藏品表 服务实现类
 * </p>
 *
 * @author Michael Ran
 * @since 2021-03-04
 */
@Service
public class BlockchainServiceImpl extends ServiceImpl<BlockchainMapper, Blockchain> implements IBlockchainService {

}
