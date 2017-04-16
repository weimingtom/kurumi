package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public interface LuaLOptDelegateInteger {
	//Int32
	//lua_Integer
	int exec(LuaStateObject L, int narg);
}