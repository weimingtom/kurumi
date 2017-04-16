package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.func.LuaiJmpbuf;

//
// ** $Id: ldo.c,v 2.38.1.3 2008/01/18 22:31:22 roberto Exp $
// ** Stack and Call structure of Lua
// ** See Copyright Notice in lua.h
// 
//    
//	 ** {======================================================
//	 ** Error-recovery functions
//	 ** =======================================================
//	 
// chain list of long jump buffers 
public class LuaLongjmp {
	public LuaLongjmp previous;
	public LuaiJmpbuf b;
	public volatile int status; // error code 
}