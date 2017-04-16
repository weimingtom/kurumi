package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.proto.GCObjectRef;

//
// ** $Id: lstate.c,v 2.36.1.2 2008/01/03 15:20:39 roberto Exp $
// ** Global State
// ** See Copyright Notice in lua.h
// 
public class OpenValRef implements GCObjectRef {
	private LuaStateObject L;

	public OpenValRef(LuaStateObject L) {
		this.L = L;
	}

	public final void set(GCObject value) {
		this.L.openupval = value;
	}

	public final GCObject get() {
		return this.L.openupval;
	}
}