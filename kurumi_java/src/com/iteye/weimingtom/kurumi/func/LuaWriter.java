package com.iteye.weimingtom.kurumi.func;

import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

// functions that read/write blocks when loading/dumping Lua chunks
//public delegate int lua_Writer(lua_State L, CharPtr p, int//uint// sz, object ud);
public interface LuaWriter {
	//uint sz
	int exec(LuaStateObject L, CharPtr p, int sz, Object ud);
}