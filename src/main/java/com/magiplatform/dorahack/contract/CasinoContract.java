package com.magiplatform.dorahack.contract;

import io.reactivex.Flowable;
import io.reactivex.functions.Function;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.web3j.abi.EventEncoder;
import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Event;
//import org.web3j.abi.datatypes.MyFunction;
import org.web3j.abi.datatypes.Type;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.RemoteCall;
import org.web3j.protocol.core.RemoteFunctionCall;
import org.web3j.protocol.core.methods.request.EthFilter;
import org.web3j.protocol.core.methods.response.BaseEventResponse;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.tx.Contract;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;

/**
 * <p>Auto generated code.
 * <p><strong>Do not modify!</strong>
 * <p>Please use the <a href="https://docs.web3j.io/command_line.html">web3j command line tools</a>,
 * or the org.web3j.codegen.SolidityFunctionWrapperGenerator in the 
 * <a href="https://github.com/web3j/web3j/tree/master/codegen">codegen module</a> to update.
 *
 * <p>Generated with web3j version 4.8.4.
 */
@SuppressWarnings("rawtypes")
public class CasinoContract extends Contract {
    public static final String BINARY = "6080604052604051610666380380610666833981016040819052610022916100a4565b61002b33610054565b6000821161003857600080fd5b606481111561004657600080fd5b6001919091556002556100c8565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b600080604083850312156100b757600080fd5b505080516020909101519092909150565b61058f806100d76000396000f3fe6080604052600436106100555760003560e01c806341c0e1b51461005a57806350312c9e14610071578063715018a6146100995780637365870b146100ae5780638da5cb5b146100c1578063f2fde38b146100e9575b600080fd5b34801561006657600080fd5b5061006f610109565b005b34801561007d57600080fd5b5061008661014a565b6040519081526020015b60405180910390f35b3480156100a557600080fd5b5061006f61017a565b61006f6100bc366004610439565b6101b0565b3480156100cd57600080fd5b506000546040516001600160a01b039091168152602001610090565b3480156100f557600080fd5b5061006f610104366004610452565b61034e565b6000546001600160a01b0316331461013c5760405162461bcd60e51b815260040161013390610482565b60405180910390fd5b6000546001600160a01b0316ff5b600080546001600160a01b031633146101755760405162461bcd60e51b815260040161013390610482565b504790565b6000546001600160a01b031633146101a45760405162461bcd60e51b815260040161013390610482565b6101ae60006103e9565b565b6000811180156101c15750600a8111155b6102045760405162461bcd60e51b81526020600482015260146024820152736e756d626572206e6f7420696e205b312c31305d60601b6044820152606401610133565b6001543410156102615760405162461bcd60e51b815260206004820152602260248201527f76616c75652073686f756c6420626520626967676572207468616e206d696e42604482015261195d60f21b6064820152608401610133565b600061026e600a436104cd565b6102799060016104f7565b905080821415610311576000600a6002546064610296919061050f565b6102a09034610526565b6102aa9190610545565b604051909150339082156108fc029083906000818181858888f193505050506102d257600080fd5b6040805160018152602081018390527f2fda3525453650e1c58417dd4764ce9fc5c5f0f77f345a43758186d3d01909d1910160405180910390a1505050565b60408051600080825260208201527f2fda3525453650e1c58417dd4764ce9fc5c5f0f77f345a43758186d3d01909d1910160405180910390a15050565b6000546001600160a01b031633146103785760405162461bcd60e51b815260040161013390610482565b6001600160a01b0381166103dd5760405162461bcd60e51b815260206004820152602660248201527f4f776e61626c653a206e6577206f776e657220697320746865207a65726f206160448201526564647265737360d01b6064820152608401610133565b6103e6816103e9565b50565b600080546001600160a01b038381166001600160a01b0319831681178455604051919092169283917f8be0079c531659141344cd1fd0a4f28419497f9722a3daafe3b4186f6b6457e09190a35050565b60006020828403121561044b57600080fd5b5035919050565b60006020828403121561046457600080fd5b81356001600160a01b038116811461047b57600080fd5b9392505050565b6020808252818101527f4f776e61626c653a2063616c6c6572206973206e6f7420746865206f776e6572604082015260600190565b634e487b7160e01b600052601260045260246000fd5b6000826104dc576104dc6104b7565b500690565b634e487b7160e01b600052601160045260246000fd5b6000821982111561050a5761050a6104e1565b500190565b600082821015610521576105216104e1565b500390565b6000816000190483118215151615610540576105406104e1565b500290565b600082610554576105546104b7565b50049056fea26469706673582212203f3f85bd997f597ed56a80f3b175a15e2c3bfac29777f4de4929a4056e16fee764736f6c634300080a0033";

    public static final String FUNC_BET = "bet";

    public static final String FUNC_CHECKCONTRACTBALANCE = "checkContractBalance";

    public static final String FUNC_KILL = "kill";

    public static final String FUNC_OWNER = "owner";

    public static final String FUNC_RENOUNCEOWNERSHIP = "renounceOwnership";

    public static final String FUNC_TRANSFEROWNERSHIP = "transferOwnership";

    public static final Event OWNERSHIPTRANSFERRED_EVENT = new Event("OwnershipTransferred", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Address>(true) {}, new TypeReference<Address>(true) {}));
    ;

    public static final Event WON_EVENT = new Event("Won", 
            Arrays.<TypeReference<?>>asList(new TypeReference<Bool>() {}, new TypeReference<Uint256>() {}));
    ;

    @Deprecated
    protected CasinoContract(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    protected CasinoContract(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, credentials, contractGasProvider);
    }

    @Deprecated
    protected CasinoContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        super(BINARY, contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    protected CasinoContract(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        super(BINARY, contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public List<OwnershipTransferredEventResponse> getOwnershipTransferredEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, transactionReceipt);
        ArrayList<OwnershipTransferredEventResponse> responses = new ArrayList<OwnershipTransferredEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
            typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, OwnershipTransferredEventResponse>() {
            @Override
            public OwnershipTransferredEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(OWNERSHIPTRANSFERRED_EVENT, log);
                OwnershipTransferredEventResponse typedResponse = new OwnershipTransferredEventResponse();
                typedResponse.log = log;
                typedResponse.previousOwner = (String) eventValues.getIndexedValues().get(0).getValue();
                typedResponse.newOwner = (String) eventValues.getIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<OwnershipTransferredEventResponse> ownershipTransferredEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(OWNERSHIPTRANSFERRED_EVENT));
        return ownershipTransferredEventFlowable(filter);
    }

    public List<WonEventResponse> getWonEvents(TransactionReceipt transactionReceipt) {
        List<Contract.EventValuesWithLog> valueList = extractEventParametersWithLog(WON_EVENT, transactionReceipt);
        ArrayList<WonEventResponse> responses = new ArrayList<WonEventResponse>(valueList.size());
        for (Contract.EventValuesWithLog eventValues : valueList) {
            WonEventResponse typedResponse = new WonEventResponse();
            typedResponse.log = eventValues.getLog();
            typedResponse._status = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
            typedResponse._amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
            responses.add(typedResponse);
        }
        return responses;
    }

    public Flowable<WonEventResponse> wonEventFlowable(EthFilter filter) {
        return web3j.ethLogFlowable(filter).map(new Function<Log, WonEventResponse>() {
            @Override
            public WonEventResponse apply(Log log) {
                Contract.EventValuesWithLog eventValues = extractEventParametersWithLog(WON_EVENT, log);
                WonEventResponse typedResponse = new WonEventResponse();
                typedResponse.log = log;
                typedResponse._status = (Boolean) eventValues.getNonIndexedValues().get(0).getValue();
                typedResponse._amount = (BigInteger) eventValues.getNonIndexedValues().get(1).getValue();
                return typedResponse;
            }
        });
    }

    public Flowable<WonEventResponse> wonEventFlowable(DefaultBlockParameter startBlock, DefaultBlockParameter endBlock) {
        EthFilter filter = new EthFilter(startBlock, endBlock, getContractAddress());
        filter.addSingleTopic(EventEncoder.encode(WON_EVENT));
        return wonEventFlowable(filter);
    }

    public RemoteFunctionCall<TransactionReceipt> bet(BigInteger _number) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_BET, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_number)), 
                Collections.<TypeReference<?>>emptyList());
                
        return executeRemoteCallTransaction(function);
    }
    
//    public MyFunction getBetFunction() {
//    	final MyFunction function = new MyFunction(
//                FUNC_BET, 
//                Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(0)), 
//                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}),
//                Collections.<TypeReference<?>>emptyList());
//    	    	
//    	return function;
//    }

    public RemoteFunctionCall<BigInteger> checkContractBalance() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_CHECKCONTRACTBALANCE, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Uint256>() {}));
        return executeRemoteCallSingleValueReturn(function, BigInteger.class);
    }

    public RemoteFunctionCall<TransactionReceipt> kill() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_KILL, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<String> owner() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(FUNC_OWNER, 
                Arrays.<Type>asList(), 
                Arrays.<TypeReference<?>>asList(new TypeReference<Address>() {}));
        return executeRemoteCallSingleValueReturn(function, String.class);
    }

    public RemoteFunctionCall<TransactionReceipt> renounceOwnership() {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_RENOUNCEOWNERSHIP, 
                Arrays.<Type>asList(), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    public RemoteFunctionCall<TransactionReceipt> transferOwnership(String newOwner) {
        final org.web3j.abi.datatypes.Function function = new org.web3j.abi.datatypes.Function(
                FUNC_TRANSFEROWNERSHIP, 
                Arrays.<Type>asList(new org.web3j.abi.datatypes.Address(160, newOwner)), 
                Collections.<TypeReference<?>>emptyList());
        return executeRemoteCallTransaction(function);
    }

    @Deprecated
    public static CasinoContract load(String contractAddress, Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoContract(contractAddress, web3j, credentials, gasPrice, gasLimit);
    }

    @Deprecated
    public static CasinoContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit) {
        return new CasinoContract(contractAddress, web3j, transactionManager, gasPrice, gasLimit);
    }

    public static CasinoContract load(String contractAddress, Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider) {
        return new CasinoContract(contractAddress, web3j, credentials, contractGasProvider);
    }

    public static CasinoContract load(String contractAddress, Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider) {
        return new CasinoContract(contractAddress, web3j, transactionManager, contractGasProvider);
    }

    public static RemoteCall<CasinoContract> deploy(Web3j web3j, Credentials credentials, ContractGasProvider contractGasProvider, BigInteger _minBet, BigInteger _houseEdge) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_minBet), 
                new org.web3j.abi.datatypes.generated.Uint256(_houseEdge)));
        return deployRemoteCall(CasinoContract.class, web3j, credentials, contractGasProvider, BINARY, encodedConstructor);
    }

    public static RemoteCall<CasinoContract> deploy(Web3j web3j, TransactionManager transactionManager, ContractGasProvider contractGasProvider, BigInteger _minBet, BigInteger _houseEdge) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_minBet), 
                new org.web3j.abi.datatypes.generated.Uint256(_houseEdge)));
        return deployRemoteCall(CasinoContract.class, web3j, transactionManager, contractGasProvider, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CasinoContract> deploy(Web3j web3j, Credentials credentials, BigInteger gasPrice, BigInteger gasLimit, BigInteger _minBet, BigInteger _houseEdge) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_minBet), 
                new org.web3j.abi.datatypes.generated.Uint256(_houseEdge)));
        return deployRemoteCall(CasinoContract.class, web3j, credentials, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    @Deprecated
    public static RemoteCall<CasinoContract> deploy(Web3j web3j, TransactionManager transactionManager, BigInteger gasPrice, BigInteger gasLimit, BigInteger _minBet, BigInteger _houseEdge) {
        String encodedConstructor = FunctionEncoder.encodeConstructor(Arrays.<Type>asList(new org.web3j.abi.datatypes.generated.Uint256(_minBet), 
                new org.web3j.abi.datatypes.generated.Uint256(_houseEdge)));
        return deployRemoteCall(CasinoContract.class, web3j, transactionManager, gasPrice, gasLimit, BINARY, encodedConstructor);
    }

    public static class OwnershipTransferredEventResponse extends BaseEventResponse {
        public String previousOwner;

        public String newOwner;
    }

    public static class WonEventResponse extends BaseEventResponse {
        public Boolean _status;

        public BigInteger _amount;
    }
}
