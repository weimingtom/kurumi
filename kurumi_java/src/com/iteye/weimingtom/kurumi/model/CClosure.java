package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.func.LuaCFunction;

//
// ** $Id: lobject.c,v 2.22.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Some generic functions over Lua objects
// ** See Copyright Notice in lua.h
// 
public class CClosure extends ClosureType {
	public LuaCFunction f;
	public TValue[] upvalue;

	public CClosure(ClosureHeader header) {
		super(header);

	}
}