package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public interface Pfunc {
	void exec(LuaStateObject L, Object ud);
}