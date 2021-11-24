package com.magiplatform.dorahack.service;

import com.magiplatform.dorahack.entity.Auction;

import java.util.List;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 竞拍出价表 服务类
 * </p>
 *
 * @author Michael Ran
 * @since 2021-03-04
 */
public interface IAuctionService extends IService<Auction> {
	public void startAuction(String artId);
	public void bidPriceForArt(String artId, String auctionRound, String bidPrice, String bidUserId);
	public void idPay(String artId, String auctionRound);
	
}
