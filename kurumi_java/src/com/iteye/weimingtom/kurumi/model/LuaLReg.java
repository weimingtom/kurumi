package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;

//
// ** $Id: lauxlib.c,v 1.159.1.3 2008/01/21 13:20:51 roberto Exp $
// ** Auxiliary functions for building Lua libraries
// ** See Copyright Notice in lua.h
// 
//
// ** #define lauxlib_c
// ** #define LUA_LIB
// 
public class LuaLReg {
	public CharPtr name;
	public LuaCFunction func;

	public LuaLReg(CharPtr name, LuaCFunction func) {
		this.name = name;
		this.func = func;
	}
}