package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.LuaDebugState;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

// Functions to be called by the debuger in specific events 
//public delegate void lua_Hook(lua_State L, lua_Debug ar);
public interface LuaHook {
	void exec(LuaStateObject L, LuaDebugState ar);
}