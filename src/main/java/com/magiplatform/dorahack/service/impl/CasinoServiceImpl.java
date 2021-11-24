package com.magiplatform.dorahack.service.impl;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameterNumber;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.EthBlockNumber;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.EthBlock.TransactionObject;
import org.web3j.protocol.http.HttpService;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.esaulpaugh.headlong.abi.Function;
import com.esaulpaugh.headlong.abi.Tuple;
import com.esaulpaugh.headlong.util.FastHex;
import com.magiplatform.dorahack.contract.CasinoContract;
import com.magiplatform.dorahack.contract.CasinoContract.WonEventResponse;
import com.magiplatform.dorahack.entity.Blockchain;
import com.magiplatform.dorahack.entity.Casino;

import com.magiplatform.dorahack.mapper.CasinoMapper;
import com.magiplatform.dorahack.service.IBlockchainService;
import com.magiplatform.dorahack.service.ICasinoService;


@Service
public class CasinoServiceImpl extends ServiceImpl<CasinoMapper, Casino> implements ICasinoService {

	@Autowired
	private Environment env;
	
	@Autowired
	private IBlockchainService blockchainService;
	
	@Transactional
	public void updateData() throws IOException  {
						
		String httpProvider = env.getProperty("web3.http-provider");
		String contractAddress = env.getProperty("web3.contract-address");
		String privateKey = env.getProperty("web3.private-key");
		
		Web3j web3 = Web3j.build(new HttpService(httpProvider));
		
		Blockchain blockchain = blockchainService.getById(contractAddress);
		
		System.out.println(blockchain);
		
		if(blockchain==null) {
			
			System.out.println("block height: 0");
			
			// 创建一个新元素
			blockchain = new Blockchain();
			blockchain.setContractAddress(contractAddress);
			blockchain.setBlockHeight(0);
			blockchain.setBalance("0");
			
			blockchainService.save(blockchain);
		}
		else {
			// 增加blockNumber, 并获取数据
			System.out.println("block height: " + (blockchain.getBlockHeight()+1));
			
			// 获取最新的block height
			EthBlockNumber ethBLockNumber = web3.ethBlockNumber().send();
			int latestBlockNumber = ethBLockNumber.getBlockNumber().intValue();
			
			int nextBlockNumber = blockchain.getBlockHeight() + 1;
			if(nextBlockNumber <= latestBlockNumber) {
				
				// 返回casinos, 顺便更新了blockchain
				List<Casino> casinos = parseContract(blockchain, privateKey, web3);
				
				for(Casino casino: casinos) {
					this.save(casino);
				}
				
				blockchainService.updateById(blockchain);
			}
			
		}
				
	}
	
	
	public List<Casino> parseContract(Blockchain blockchain, String privateKey, Web3j web3) throws IOException {
		
		int nextBlockNumber = blockchain.getBlockHeight() + 1;
		BigInteger balance = new BigInteger(blockchain.getBalance());
		BigInteger currentBlockNumber = BigInteger.valueOf(nextBlockNumber);
		
		EthBlock ethBlock = web3.ethGetBlockByNumber(new DefaultBlockParameterNumber(currentBlockNumber), true).send();
		if (ethBlock == null || ethBlock.getBlock() == null) {
            throw new RuntimeException("Block number: " + nextBlockNumber + " was not found");
        }
		
		List<TransactionObject> list = ethBlock.getBlock().getTransactions().stream()
            .map(TransactionObject.class::cast)
            .collect(Collectors.toList());
		
		Credentials credentials = Credentials.create(privateKey);
		CasinoContract contract = CasinoContract.load(blockchain.getContractAddress(), web3, credentials, 
				BigInteger.valueOf(200000000), BigInteger.valueOf(2100000));
		
		
		List<Casino> ret = new ArrayList<>();
		
		for(TransactionObject to: list) {
			
			System.out.println(to.getTo());
			
			// 创建合约的交易
			if(to.getTo()==null) {
				EthGetTransactionReceipt tr = web3.ethGetTransactionReceipt(to.getHash()).send();
												
				if (tr.getTransactionReceipt().isPresent() && 
						tr.getTransactionReceipt().get().getContractAddress().toLowerCase().equals(blockchain.getContractAddress().toLowerCase())) {
				    
					// hack, 由于headlong并不能正确的解析constructor，因此提取参数，并附加上d7777c36
					String parameters = "d7777c36" + to.getInput().substring(to.getInput().length()-128);					
					System.out.println(parameters);
					
				    String params = decodeConstructorInput(parameters);
				    
				    balance = balance.add(to.getValue());
				    System.out.println(balance);
				    
				    Casino casino = new Casino();
				    casino.setAddress(blockchain.getContractAddress());
				    casino.setBlockHeight(nextBlockNumber);
				    casino.setFromAddress(to.getFrom());
				    casino.setMethod("constructor");
				    casino.setParams(params);
				    casino.setTransactionHash(to.getHash());
				    casino.setValue(to.getValue().toString());
				    
				    ret.add(casino);
				}
				
				
			}
			
			// to的地址与contract相同，说明是call合约
			else if(to.getTo()!=null && to.getTo().toLowerCase().equals(blockchain.getContractAddress().toLowerCase())) {
//				ret.add(to);
				
				// 解析合约的input
				String parameters = to.getInput().substring(2);
				System.out.println(parameters);
				String params = decodeFunctionInput(parameters);
				
				balance = balance.add(to.getValue());
			    System.out.println(balance);
			    
			    Casino casino = new Casino();
			    casino.setAddress(blockchain.getContractAddress());
			    casino.setBlockHeight(nextBlockNumber);
			    casino.setFromAddress(to.getFrom());
			    casino.setMethod("bet");
			    casino.setParams(params);
			    casino.setTransactionHash(to.getHash());
			    casino.setValue(to.getValue().toString());
			    
			    
								
				// 解析合约的log
				EthGetTransactionReceipt tr = web3.ethGetTransactionReceipt(to.getHash()).send();
				
				if (tr.getTransactionReceipt().isPresent()) {
					List<WonEventResponse> responses = contract.getWonEvents(tr.getTransactionReceipt().get());
					
					for(WonEventResponse resp: responses) {
						System.out.println(resp._amount);
						System.out.println(resp._status);
						
						casino.setWonAmount(resp._amount.toString());
						casino.setStatus(resp._status.toString());
						
						balance = balance.subtract(resp._amount);
					    System.out.println(balance);
						
					}
				}
				
				ret.add(casino);
								
			}
			
		}
		
		blockchain.setBalance(balance.toString());
		blockchain.setBlockHeight(nextBlockNumber);
				
		return ret;
	}
	
	public String decodeConstructorInput(String input) {
		
		Function foo = Function.fromJson(
			  "{\r\n"
			  + "			\"inputs\": [\r\n"
			  + "				{\r\n"
			  + "					\"internalType\": \"uint256\",\r\n"
			  + "					\"name\": \"_minBet\",\r\n"
			  + "					\"type\": \"uint256\"\r\n"
			  + "				},\r\n"
			  + "				{\r\n"
			  + "					\"internalType\": \"uint256\",\r\n"
			  + "					\"name\": \"_houseEdge\",\r\n"
			  + "					\"type\": \"uint256\"\r\n"
			  + "				}\r\n"
			  + "			],\r\n"
			  + "			\"stateMutability\": \"payable\",\r\n"
			  + "			\"type\": \"constructor\"\r\n"
			  + "		}"
		);
		
		Tuple decoded = foo.decodeCall(	FastHex.decode(input));
		
		for(Object o: decoded) {
			System.out.println(o.getClass());
		}
		
		System.out.println(decoded);
		return decoded.toString();
		
	}
	
	public String decodeFunctionInput(String input) {
		
		Function foo = Function.fromJson(
	
				"{\r\n"
				+ "			\"inputs\": [\r\n"
				+ "				{\r\n"
				+ "					\"internalType\": \"uint256\",\r\n"
				+ "					\"name\": \"_number\",\r\n"
				+ "					\"type\": \"uint256\"\r\n"
				+ "				}\r\n"
				+ "			],\r\n"
				+ "			\"name\": \"bet\",\r\n"
				+ "			\"outputs\": [],\r\n"
				+ "			\"stateMutability\": \"payable\",\r\n"
				+ "			\"type\": \"function\"\r\n"
				+ "		}"
				);
		
		Tuple decoded = foo.decodeCall(	FastHex.decode(input));
		
		for(Object o: decoded) {
			System.out.println(o.getClass());
		}
		
		System.out.println(decoded);
		return decoded.toString();
	}
	
	
	
}


















