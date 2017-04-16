package com.iteye.weimingtom.kurumi.model;


//
// ** $Id: lobject.c,v 2.22.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Some generic functions over Lua objects
// ** See Copyright Notice in lua.h
// 
//    
//	 ** Tables
//	 
public class TKeyNk extends TValue {
	public Node next; // for chaining 

	public TKeyNk() {

	}

	public TKeyNk(Value value, int tt, Node next) {
		super(new Value(value), tt);
		this.next = next;
	}
}