package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.LuaStateObject;

public interface LuaCFunction {
	int exec(LuaStateObject L);
}