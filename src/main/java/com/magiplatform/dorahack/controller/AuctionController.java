package com.magiplatform.dorahack.controller;
import java.math.BigDecimal;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.conditions.update.LambdaUpdateChainWrapper;
import com.magiplatform.dorahack.configuration.SwaggerApiVersion;
import com.magiplatform.dorahack.configuration.SwaggerApiVersionConstant;
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
import com.magiplatform.dorahack.service.IUserService;
import com.magiplatform.dorahack.utils.CustomIdGenerator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 竞拍出价表 前端控制器
 * </p>
 *
 * @author Michael Ran
 * @since 2021-03-04
 */

@Slf4j
@Api(tags = "auction")
@ApiOperation(value = "竞拍有关")
@RestController
@RequestMapping("/auction")
public class AuctionController {
    @Autowired
    private IArtworkService artworkService;

    @Autowired
    private IUserService userService;

    @Autowired
    private IAuctionService auctionService;

    @Autowired
    private ITransHistoryService transHistoryService;

    @Autowired
    private CustomIdGenerator customIdGenerator;

    @ApiOperation(value = "最近一轮竞拍的历史和当前数据")
    @SwaggerApiVersion(group = SwaggerApiVersionConstant.WEB_1_0)
    @GetMapping("/id/round/latest")
    public ResultDto<List<Auction>> getIdRoundId(
            HttpServletRequest request,
            @RequestParam String artId
    ) {
        // 查询藏品id最大轮次的竞拍记录
        QueryWrapper<Auction> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auction::getArtId, artId)
                .eq(Auction::getIsHighestBid, "true")
                .orderByDesc(Auction::getAuctionRound)
                .last("limit 0,1");
        List<Auction> list = auctionService.list(queryWrapper);
        
        if(list.size()==0) {
        	return ResultDto.success(list);
        }
        
        String largestRound = list.get(0).getAuctionRound(); // this logic is not optimized

        queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda()
                .eq(Auction::getArtId, artId)
                .eq(Auction::getAuctionRound, largestRound)
                .orderByDesc(Auction::getBidPrice);
        list = auctionService.list(queryWrapper);
        return ResultDto.success(list);
    }

    @ApiOperation(value = "开始上架拍卖")
    @SwaggerApiVersion(group = SwaggerApiVersionConstant.WEB_1_0)
    @PostMapping("/id/start-auction")
    public ResultDto idStartAuction(HttpServletRequest request, @RequestParam String artId) {
        
    	try {
    		auctionService.startAuction(artId);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return ResultDto.failure("-1", e.getMessage());
    	}
        return ResultDto.success("上架成功");
        
    }

    @ApiOperation(value = "给拍卖品出价")
    @SwaggerApiVersion(group = SwaggerApiVersionConstant.WEB_1_0)
    @PostMapping("/id/round/id/bid")
    public ResultDto<List<Auction>> bidPriceForArt(
            HttpServletRequest request,
            @RequestParam String artId,
            @RequestParam String auctionRound,
            @RequestParam String bidPrice,
            @RequestParam String bidUserId
    ) {
        
    	try {
    		auctionService.bidPriceForArt(artId, auctionRound, bidPrice, bidUserId);
    	}
    	catch(Exception e) {
    		e.printStackTrace();
    		return ResultDto.failure("-1", e.getMessage());
    	}

        return ResultDto.success("出价成功");
    }

    @ApiOperation(value = "给拍卖品支付（需要调用bsc接口）")
    @SwaggerApiVersion(group = SwaggerApiVersionConstant.WEB_1_0)
    @PostMapping("/id/pay")
    public ResultDto<List<Auction>> idPay(
            HttpServletRequest request,
            @RequestParam String artId,
            @RequestParam String auctionRound
            
    ) {
        // TODO: call BSC to process the payment
        // Assuming payment is successful, here.
    
        // 1. update owner_id, status of artwork_table
        Artwork artwork = artworkService.getById(artId);
        
        if(artwork==null) {
        	return ResultDto.failure("-1", "未找到artid对应的artwork");
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
        Auction currentHighestBid = auctionService.getOne(queryWrapper);
        
        if(currentHighestBid==null) {
        	return ResultDto.failure("-1", "未找到artId对应的auction，付款失败");
        }
        
        
                
        LambdaUpdateWrapper<Auction> lambda = new UpdateWrapper().lambda();
        lambda
                .eq(Auction::getArtId, artId)
                .eq(Auction::getAuctionRound, auctionRound)
                .eq(Auction::getStatus, AuctionConstants.StatusEnum.HAPPENING.getCode())
                .set(Auction::getStatus, AuctionConstants.StatusEnum.FINISHED.getCode());
        boolean save = auctionService.update(lambda);
        if (!save) {
        	return ResultDto.failure("-1", "写入auction_table失败");
        }
        
        String currentOwner = artwork.getUserId();
        artwork.setUserId(currentHighestBid.getBidUserId()); // the bid user ID is user ID in artwork table
        artwork.setStatus(ArtworkConstants.StatusEnum.FINISHED.getCode());
        save = artworkService.updateById(artwork);
        if (!save) {
        	return ResultDto.failure("-1", "写入artwork_table失败");
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
        return save
                ? ResultDto.success("支付流水记录完成")
                : ResultDto.failure("-1", "写入trans_history失败");
        
        
    }

}
