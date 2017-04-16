package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;

//
// ** $Id: lapi.c,v 2.55.1.5 2008/07/04 18:41:18 roberto Exp $
// ** Lua API
// ** See Copyright Notice in lua.h
// 
//    
//	 ** Execute a protected C call.
//	 
public class CCallS {
	// data to `f_Ccall' 
	public LuaCFunction func;
	public Object ud;
}