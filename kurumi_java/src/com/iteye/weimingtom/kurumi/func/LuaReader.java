package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public interface LuaReader {
	//sz
	//out
	//uint
	CharPtr exec(LuaStateObject L, Object ud, int[] sz);
}