﻿package com.iteye.weimingtom.kurumi.model;

import com.iteye.weimingtom.kurumi.core.LuaConf;

//
// ** $Id: lparser.c,v 2.42.1.3 2007/12/28 15:32:23 roberto Exp $
// ** Lua Parser
// ** See Copyright Notice in lua.h
// 
// state needed to generate code for a given function 
public class FuncState {
	public Proto f; // current function header 
	public Table h; // table to find (and reuse) elements in `k' 
	public FuncState prev; // enclosing function 
	public LexState ls; // lexical state 
	public LuaStateObject L; // copy of the Lua state 
	public BlockCnt bl; // chain of current blocks 
	public int pc; // next position to code (equivalent to `ncode') 
	public int lasttarget; // `pc' of last `jump target' 
	public int jpc; // list of pending jumps to `pc' 
	public int freereg; // first free register 
	public int nk; // number of elements in `k' 
	public int np; // number of elements in `p' 
	public short nlocvars; // number of elements in `locvars' 
	public byte nactvar; //Byte
 //lu_byte
 // number of active local variables 
	public UpvalDesc[] upvalues = new UpvalDesc[LuaConf.LUAI_MAXUPVALUES]; // upvalues 
	public int[] actvar = new int[LuaConf.LUAI_MAXVARS]; // declared-variable stack  - ushort - ushort[]

	public FuncState() {
		for (int i = 0; i < this.upvalues.length; i++) {
			this.upvalues[i] = new UpvalDesc();
		}
	}
}