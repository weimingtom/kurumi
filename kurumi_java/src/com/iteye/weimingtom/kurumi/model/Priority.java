﻿package com.iteye.weimingtom.kurumi.model;

//
// ** $Id: lparser.c,v 2.42.1.3 2007/12/28 15:32:23 roberto Exp $
// ** Lua Parser
// ** See Copyright Notice in lua.h
// 
public class Priority {
	public byte left; //Byte
 //lu_byte
 // left priority for each binary operator 
	public byte right; //Byte
 //lu_byte
 // right priority 

	//Byte
 //lu_byte
	public Priority(byte left, byte right) {
		this.left = left;
		this.right = right;
	}
}