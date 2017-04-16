package com.iteye.weimingtom.kurumi.model;


//
// ** $Id: lobject.c,v 2.22.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Some generic functions over Lua objects
// ** See Copyright Notice in lua.h
// 
public class TKey {
	public TKeyNk nk = new TKeyNk();

	public TKey() {
		this.nk = new TKeyNk();
	}

	public TKey(TKey copy) {
		this.nk = new TKeyNk(new Value(copy.nk.value), copy.nk.tt, copy.nk.next);
	}

	public TKey(Value value, int tt, Node next) {
		this.nk = new TKeyNk(new Value(value), tt, next);
	}

	public final TValue getTvk() {
		return this.nk;
	}
}