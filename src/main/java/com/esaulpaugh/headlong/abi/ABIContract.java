package com.esaulpaugh.headlong.abi;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import com.esaulpaugh.headlong.abi.util.JsonUtils;
import com.esaulpaugh.headlong.abi.util.WrappedKeccak;
import com.esaulpaugh.headlong.util.FastHex;
import com.esaulpaugh.headlong.util.Strings;
import com.google.gson.JsonArray;

public class ABIContract {
	
	private List<ABIObject> abiObjectList = new ArrayList<>();
	private List<String> jsonList = new ArrayList<>();
	
	public ABIContract(File file) {
		
		try {
			FileReader fileReader = new FileReader(file);
			BufferedReader br = new BufferedReader(fileReader);
		    StringBuilder sb = new StringBuilder();
		    String temp = "";
		    while ((temp = br.readLine()) != null) {
		    	// 拼接换行符
		    	sb.append(temp + "\n");
		    }
		    br.close();
		    String json = sb.toString();
		    
		    JsonArray contractArray = JsonUtils.parseArray(json);
			
			
			final int n = contractArray.size();
	        for (int j = 0; j < n; j++) {
	        	jsonList.add(JsonUtils.toPrettyPrint(contractArray.get(j).getAsJsonObject()) );
	        }
	        
	        for (String originalJson : jsonList) {
	            ABIObject orig = ABIJSON.parseABIObject(JsonUtils.parseObject(originalJson));
	            abiObjectList.add(orig);
	        }
		}  catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    
	}
	
	public ABIContract(String jsonContract) {
		JsonArray contractArray = JsonUtils.parseArray(jsonContract);
		
				
		final int n = contractArray.size();
        for (int j = 0; j < n; j++) {
        	jsonList.add(JsonUtils.toPrettyPrint(contractArray.get(j).getAsJsonObject()) );
        }
        
        for (String originalJson : jsonList) {
            ABIObject orig = ABIJSON.parseABIObject(JsonUtils.parseObject(originalJson));
            abiObjectList.add(orig);
        }
	}

	
	
	public List<ABIObject> getAbiObjectList() {
		return abiObjectList;
	}



	public void setAbiObjectList(List<ABIObject> abiObjectList) {
		this.abiObjectList = abiObjectList;
	}



	public List<String> getJsonList() {
		return jsonList;
	}



	public void setJsonList(List<String> jsonList) {
		this.jsonList = jsonList;
	}



	public FunctionResult decodeCall(String input) {
		
		input = input.indexOf("0x")==0 ? input.substring(2) : input;
		
		if(input.length() < 8 ) {
			System.err.println("input too short");
			return null;
		}
				
		String selector = input.substring(0,8);
		
		for(ABIObject abi: abiObjectList) {
			if(abi.getType() == TypeEnum.FUNCTION) {
				
				Function function = (Function)abi;
								
				
				if(selector.equals(function.selectorHex())) {
					byte[] array = FastHex.decode(input);
					ByteBuffer abiBuffer = ByteBuffer.wrap(array);
					
					Tuple result = function.decodeCall(abiBuffer);
					FunctionResult fr = new FunctionResult(function.getName(), result);
					
					return fr;
		           	
		        }
				
			}
		}
		
		return null;
		
	}
	
	public List<EventResult> decodeEvent(TransactionReceipt tr) {
		
		
		List<EventResult> list = new ArrayList<>();
		
		for(ABIObject abi: abiObjectList) {
			if(abi.getType() == TypeEnum.EVENT) {
				
				Event event = (Event)abi;
				
				byte[] utf8 = Strings.decode(event.getCanonicalSignature(), Strings.UTF_8);
		        byte[] hash = new WrappedKeccak(256).digest(utf8);
		        System.out.println(Strings.encode(hash, 0));
		        
		        String sig = "0x"+Strings.encode(hash, 0);
								
				for(Log log: tr.getLogs()) {
					
					List<String> topics = log.getTopics();
					
					if(topics.size()>0 && topics.get(0).equals(sig)) {
						
						// 继续解析其他的参数，首先是indexed
						
						TupleType indexed = event.getIndexedParams();
						Tuple indexedResult = null;
						
						if(!indexed.isEmpty()) {
						
							StringBuffer sb = new StringBuffer();
							for(int i=1;i<topics.size();i++) {
								
								String topic = topics.get(i).indexOf("0x")==0 ? topics.get(i).substring(2) : topics.get(i);
								sb.append(topic);
								
							}
							
							System.out.println(sb.toString());
							
							byte[] array = FastHex.decode(sb.toString());
							ByteBuffer abiBuffer = ByteBuffer.wrap(array);
							
							indexedResult = indexed.decode(abiBuffer);
						}
												
						//  其次是data
						TupleType nonIndexed = event.getNonIndexedParams();
						Tuple nonIndexedResult = null;
						
						if(!nonIndexed.isEmpty()) {
							
							String data = log.getData().indexOf("0x")==0 ? log.getData().substring(2) : log.getData();
							System.out.println(data);
							byte[] array = FastHex.decode(data);
							ByteBuffer abiBuffer = ByteBuffer.wrap(array);
							
							nonIndexedResult = nonIndexed.decode(abiBuffer);
							
						}
						
//						System.out.println(indexedResult);
//						System.out.println(nonIndexedResult);
						
						EventResult er = new EventResult(event.getName(), indexedResult, nonIndexedResult);
						
						list.add(er);
					}
						
					
				}
				
			}
		}
		
		return list;
		
	}
	
	
	
}













