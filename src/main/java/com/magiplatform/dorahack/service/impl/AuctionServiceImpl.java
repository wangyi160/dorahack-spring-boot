package com.magiplatform.dorahack.service.impl;

import com.magiplatform.dorahack.constants.ArtworkConstants;
import com.magiplatform.dorahack.constants.AuctionConstants;
import com.magiplatform.dorahack.constants.PaymentConstants;
import com.magiplatform.dorahack.dto.base.ResultDto;
import com.magiplatform.dorahack.entity.Artwork;
import com.magiplatform.dorahack.entity.Auction;
import com.magiplatform.dorahack.entity.TransHistory;
import com.magiplatform.dorahack.mapper.AuctionMapper;
import com.magiplatform.dorahack.service.IArtworkService;
import com.magiplatform.dorahack.service.IAuctionService;
import com.magiplatform.dorahack.service.ITransHistoryService;
import com.magiplatform.dorahack.utils.CustomIdGenerator;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * <p>
 * 竞拍出价表 服务实现类
 * </p>
 *
 * @author Michael Ran
 * @since 2021-03-04
 */
@Service
public class AuctionServiceImpl extends ServiceImpl<AuctionMapper, Auction> implements IAuctionService {

	@Autowired
    private IArtworkService artworkService;
	
	@Autowired
    private CustomIdGenerator customIdGenerator;
	
	@Autowired
    private ITransHistoryService transHistoryService;
	
	@Transactional
	public void startAuction(String artId) {
		// 更新状态
        Artwork artwork = new Artwork();
        artwork.setId(artId);
        artwork.setStatus(ArtworkConstants.StatusEnum.ON_AUCTION.getCode());
        artwork.setUpdateTime(LocalDateTime.now());
        boolean b = artworkService.updateById(artwork);

        if(!b) {
        	throw new RuntimeException("artwork update failed");
        }
        
        // 查询藏品id最大轮次的竞拍记录
        QueryWrapper<Auction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auction::getArtId, artId)
                .eq(Auction::getIsHighestBid, "true")
                .orderByDesc(Auction::getAuctionRound)
                .last("limit 0,1");
        List<Auction> list = this.list(queryWrapper);

        // 初始化竞拍
        LocalDateTime now = LocalDateTime.now();

        Auction auction = new Auction();
        auction.setId(String.valueOf(customIdGenerator.nextUUID(auction)));
        auction.setArtId(artId);
        auction.setStartTime(now);
        auction.setEndTime(now.plusHours(AuctionConstants.AUCTION_PERIOD_HOURS));
        auction.setStatus(AuctionConstants.StatusEnum.HAPPENING.getCode());
        auction.setCreateTime(now);
        
        if (CollectionUtils.isEmpty(list)) {
            auction.setAuctionRound("1");
            auction.setStartBidPrice(new BigDecimal(AuctionConstants.AUCTION_DEFAULT_INITIAL_PRICE));
            auction.setBidCapPrice(new BigDecimal(AuctionConstants.AUCTION_DEFAULT_INITIAL_PRICE * AuctionConstants.AUCTION_PRICE_CAP_RATIO));
        } else {
            Auction maxAuction = list.get(0);
            
            if(maxAuction.getBidPrice().compareTo(maxAuction.getStartBidPrice())<0) {
            	throw new RuntimeException("上轮竞价未完成");
            }
            
            auction.setAuctionRound(String.valueOf(Integer.parseInt(maxAuction.getAuctionRound()) + 1));
            auction.setStartBidPrice(maxAuction.getBidPrice());
            auction.setBidCapPrice(maxAuction.getBidPrice().multiply(new BigDecimal(AuctionConstants.AUCTION_PRICE_CAP_RATIO)));
        }
        auction.setBidUserId("0");
        auction.setBidPrice(new BigDecimal("0"));
        auction.setIsHighestBid("true");

        this.save(auction);
	}
	
	@Transactional
	public void bidPriceForArt(String artId, String auctionRound, String bidPrice, String bidUserId) {
		
		BigDecimal bidPriceBig = new BigDecimal(bidPrice);
        
        // 查询藏品id最大轮次的竞拍记录
        QueryWrapper<Auction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auction::getArtId, artId)
                .eq(Auction::getAuctionRound, auctionRound)
                .eq(Auction::getIsHighestBid, "true")
                .last("limit 1");
        Auction currentHighestBid = this.getOne(queryWrapper);

        if(currentHighestBid==null) {
        	throw new RuntimeException( "未找到artId对应的auction，出价失败" );
        }
        
        if (AuctionConstants.StatusEnum.FINISHED.getCode().equals(currentHighestBid.getStatus())) {
        	throw new RuntimeException("This round is already closed.");
        }
        
        else if (bidPriceBig.compareTo(currentHighestBid.getBidCapPrice()) > 0) {
            // user's bid > price cap
        	throw new RuntimeException("Price cap for round " + currentHighestBid.getAuctionRound()
                    + " is " + currentHighestBid.getBidCapPrice());
        }
        else if (bidPriceBig.compareTo(currentHighestBid.getStartBidPrice()) < 0 ||
        		currentHighestBid.getBidPrice().compareTo(bidPriceBig) > 0  ||
                currentHighestBid.getBidPrice().compareTo(bidPriceBig) == 0 && bidPriceBig.compareTo(currentHighestBid.getBidCapPrice()) < 0) {
            // current highest bid > user's bid
            // Or current highest bid = user's bid < price cap
        	throw new RuntimeException("Your bid price is too low.");
        }

        // 新增竞拍出价
        Auction newHighestBid = new Auction();
        BeanUtils.copyProperties(currentHighestBid, newHighestBid);
        newHighestBid.setId(String.valueOf(customIdGenerator.nextUUID(newHighestBid)));
        newHighestBid.setBidUserId(bidUserId);
        newHighestBid.setBidPrice(bidPriceBig);
        newHighestBid.setBidTime(LocalDateTime.now());
        newHighestBid.setCreateTime(LocalDateTime.now());
        this.save(newHighestBid);

        // 更新前一次竞拍出价false
        if (currentHighestBid.getBidPrice().compareTo(bidPriceBig) < 0) {
            // if previous highest bid < user's bid, previous bid is no longer the highest bid
            currentHighestBid.setIsHighestBid("false");
            this.updateById(currentHighestBid);
        } else {
            // in this, previous highest bid = user's bid = price cap
            // there are multiple rows with isHighestBid flat = true
        }
		
		
	}
	
	@Transactional
	public void idPay(String artId, String auctionRound) {
		// 1. update owner_id, status of artwork_table
        Artwork artwork = artworkService.getById(artId);
        
        if(artwork==null) {
        	throw new RuntimeException("未找到artid对应的artwork");
        }
        
        System.out.println("artId is " + artId);
        
        // 2. update auction_status of auction_table (happening -> finished)
        QueryWrapper<Auction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auction::getArtId, artId)
                .eq(Auction::getAuctionRound, auctionRound)
                .eq(Auction::getIsHighestBid, "true")
                .ne(Auction::getBidUserId, "0")
                .last("limit 1");
        Auction currentHighestBid = this.getOne(queryWrapper);
        
        if(currentHighestBid==null) {
        	throw new RuntimeException("未找到artId对应的auction，付款失败");
        }
                        
        LambdaUpdateWrapper<Auction> lambda = new UpdateWrapper().lambda();
        
        lambda
                .eq(Auction::getArtId, artId)
                .eq(Auction::getAuctionRound, auctionRound)
                .eq(Auction::getStatus, AuctionConstants.StatusEnum.HAPPENING.getCode())
                .set(Auction::getStatus, AuctionConstants.StatusEnum.FINISHED.getCode());
        boolean save = this.update(lambda);
        
        if (!save) {
        	throw new RuntimeException("写入auction_table失败");
        }
        
        String currentOwner = artwork.getUserId();
        artwork.setUserId(currentHighestBid.getBidUserId()); // the bid user ID is user ID in artwork table
        artwork.setStatus(ArtworkConstants.StatusEnum.FINISHED.getCode());
        save = artworkService.updateById(artwork);
        if (!save) {
        	throw new RuntimeException("写入artwork_table失败");
        }

        // 3. add new record in trans_history
        TransHistory transHistory = new TransHistory();
        transHistory.setId(String.valueOf(customIdGenerator.nextId(currentHighestBid.getBidPrice())));
        transHistory.setArtId(artId);
        transHistory.setAuctionRound(auctionRound);
        transHistory.setSellerId(currentOwner);
        transHistory.setHighestBidUserId(currentHighestBid.getBidUserId());
        transHistory.setHighestBidPrice(currentHighestBid.getBidPrice());
        transHistory.setPaymentEndTime(LocalDateTime.now());
        transHistory.setPaymentStatus(PaymentConstants.StatusEnum.SUCCESS.getCode());

        save = transHistoryService.save(transHistory);
	}
	
}













