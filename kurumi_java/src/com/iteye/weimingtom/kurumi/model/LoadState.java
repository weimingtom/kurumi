package com.iteye.weimingtom.kurumi.model;


//
// ** $Id: lundump.c,v 2.7.1.4 2008/04/04 19:51:41 roberto Exp $
// ** load precompiled Lua chunks
// ** See Copyright Notice in lua.h
// 
public class LoadState {
	public LuaStateObject L;
	public ZIO Z;
	public Mbuffer b;
	public CharPtr name;
}