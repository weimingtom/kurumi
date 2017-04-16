package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.func.LuaWriter;

//
//** $Id: ldump.c,v 2.8.1.1 2007/12/27 13:02:25 roberto Exp $
//** save precompiled Lua chunks
//** See Copyright Notice in lua.h
//
public class DumpState {
	public LuaStateObject L;
	public LuaWriter writer;
	public Object data;
	public int strip;
	public int status;
}