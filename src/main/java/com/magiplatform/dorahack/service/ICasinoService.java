package com.magiplatform.dorahack.service;

import java.io.IOException;

import com.baomidou.mybatisplus.extension.service.IService;
import com.magiplatform.dorahack.entity.Casino;

public interface ICasinoService extends IService<Casino>{

	public void updateData() throws IOException;
	
}
