package com.iteye.weimingtom.kurumi.model;


//
// ** $Id: lstate.c,v 2.36.1.2 2008/01/03 15:20:39 roberto Exp $
// ** Global State
// ** See Copyright Notice in lua.h
// 
//    
//	 ** Main thread combines a thread state and the global state
//	 
public class LG extends LuaStateObject {
	public GlobalState g = new GlobalState();

	public final LuaStateObject getL() {
		return this;
	}
}