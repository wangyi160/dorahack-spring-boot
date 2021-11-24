package com.magiplatform.dorahack.schedulingtasks;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.magiplatform.dorahack.service.ICasinoService;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class ScheduledTasks {

//	private static final Logger log = LoggerFactory.getLogger(ScheduledTasks.class);

	@Autowired
	private ICasinoService gethService;
	
	private static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

	@Scheduled(fixedDelay = 1000)
	public void reportCurrentTime() {
		log.info("The time is now {}", dateFormat.format(new Date()));
		
		
		
		try {
			gethService.updateData();
//			Thread.sleep(2000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
//		catch (InterruptedException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}
}


