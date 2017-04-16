package com.iteye.weimingtom.kurumi.exception;

import com.iteye.weimingtom.kurumi.model.LuaLongjmp;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;

//
// ** $Id: luaconf.h,v 1.82.1.7 2008/02/11 16:25:08 roberto Exp $
// ** Configuration file for Lua
// ** See Copyright Notice in lua.h
// 

public class LuaException extends RuntimeException {
	public LuaStateObject L;
	public LuaLongjmp c;

	public LuaException(LuaStateObject L, LuaLongjmp c) {
		this.L = L;
		this.c = c;
	}
}