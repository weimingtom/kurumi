package com.iteye.weimingtom.kurumi.model;


//
// ** $Id: lparser.c,v 2.42.1.3 2007/12/28 15:32:23 roberto Exp $
// ** Lua Parser
// ** See Copyright Notice in lua.h
// 
//    
//	 ** structure to chain all variables in the left-hand side of an
//	 ** assignment
//	 
public class LHSAssign {
	public LHSAssign prev;
	public ExpDesc v = new ExpDesc(); // variable (global, local, upvalue, or indexed) 
}