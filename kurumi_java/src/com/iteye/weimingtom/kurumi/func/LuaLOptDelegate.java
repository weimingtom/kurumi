package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public interface LuaLOptDelegate {
	//Double
	//lua_Number
	double exec(LuaStateObject L, int narg);
}