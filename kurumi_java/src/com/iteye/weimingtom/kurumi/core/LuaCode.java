package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.constant.BinOpr;
import com.iteye.weimingtom.kurumi.constant.ExpKind;
import com.iteye.weimingtom.kurumi.constant.OpArgMask;
import com.iteye.weimingtom.kurumi.constant.OpCode;
import com.iteye.weimingtom.kurumi.constant.OpMode;
import com.iteye.weimingtom.kurumi.constant.UnOpr;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.ExpDesc;
import com.iteye.weimingtom.kurumi.model.FuncState;
import com.iteye.weimingtom.kurumi.model.InstructionPtr;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.model.Proto;
import com.iteye.weimingtom.kurumi.model.TString;
import com.iteye.weimingtom.kurumi.model.TValue;
import com.iteye.weimingtom.kurumi.port.ClassType;

//
// ** $Id: lcode.c,v 2.25.1.3 2007/12/28 15:32:23 roberto Exp $
// ** Code generator for Lua
// ** See Copyright Notice in lua.h
// 

//using TValue = Lua.TValue;
//using lua_Number = System.Double;
//using Instruction = System.UInt32;

public class LuaCode {
//        
//		 ** Marks the end of a patch list. It is an invalid value both as an absolute
//		 ** address, and as a list link (would link an element to itself).
//		 
	public static final int NO_JUMP = (-1);

	public static InstructionPtr getcode(FuncState fs, ExpDesc e) {
		return new InstructionPtr(fs.f.code, e.u.s.info);
	}

	public static int luaK_codeAsBx(FuncState fs, OpCode o, int A, int sBx) {
		return LuaCode.luaK_codeABx(fs, o, A, sBx + LuaOpCodes.MAXARG_sBx);
	}

	public static void luaK_setmultret(FuncState fs, ExpDesc e) {
		LuaCode.luaK_setreturns(fs, e, Lua.LUA_MULTRET);
	}

	public static boolean hasjumps(ExpDesc e) {
		return e.t != e.f;
	}

	private static int isnumeral(ExpDesc e) {
		return (e.k == ExpKind.VKNUM && e.t == NO_JUMP && e.f == NO_JUMP) ? 1 : 0;
	}

	public static void luaK_nil(FuncState fs, int from, int n) {
		InstructionPtr previous;
		if (fs.pc > fs.lasttarget) {
			// no jumps to current position? 
			if (fs.pc == 0) {
				// function start? 
				if (from >= fs.nactvar) {
					return; // positions are already clean 
				}
			}
			else {
				previous = new InstructionPtr(fs.f.code, fs.pc-1);
				if (LuaOpCodes.GET_OPCODE(previous) == OpCode.OP_LOADNIL) {
					int pfrom = LuaOpCodes.GETARG_A(previous);
					int pto = LuaOpCodes.GETARG_B(previous);
					if (pfrom <= from && from <= pto+1) {
						// can connect both? 
						if (from+n-1 > pto) {
							LuaOpCodes.SETARG_B(previous, from + n - 1);
						}
						return;
					}
				}
			}
		}
		luaK_codeABC(fs, OpCode.OP_LOADNIL, from, from + n - 1, 0); // else no optimization 
	}

	public static int luaK_jump(FuncState fs) {
		int jpc = fs.jpc; // save list of jumps to here 
		int[] j = new int[1];
		j[0] = 0;
		fs.jpc = NO_JUMP;
		j[0] = luaK_codeAsBx(fs, OpCode.OP_JMP, 0, NO_JUMP);
		luaK_concat(fs, j, jpc); // keep them on hold  - ref
		return j[0];
	}

	public static void luaK_ret(FuncState fs, int first, int nret) {
		luaK_codeABC(fs, OpCode.OP_RETURN, first, nret + 1, 0);
	}

	private static int condjump(FuncState fs, OpCode op, int A, int B, int C) {
		luaK_codeABC(fs, op, A, B, C);
		return luaK_jump(fs);
	}

	private static void fixjump(FuncState fs, int pc, int dest) {
		InstructionPtr jmp = new InstructionPtr(fs.f.code, pc);
		int offset = dest-(pc+1);
		LuaLimits.lua_assert(dest != NO_JUMP);
		if (Math.abs(offset) > LuaOpCodes.MAXARG_sBx) {
			LuaLex.luaX_syntaxerror(fs.ls, CharPtr.toCharPtr("control structure too long"));
		}
		LuaOpCodes.SETARG_sBx(jmp, offset);
	}

//        
//		 ** returns current `pc' and marks it as a jump target (to avoid wrong
//		 ** optimizations with consecutive instructions not in the same basic block).
//		 
	public static int luaK_getlabel(FuncState fs) {
		fs.lasttarget = fs.pc;
		return fs.pc;
	}

	private static int getjump(FuncState fs, int pc) {
		int offset = LuaOpCodes.GETARG_sBx(fs.f.code[pc]);
		if (offset == NO_JUMP) { // point to itself represents end of list 
			return NO_JUMP; // end of list 
		}
		else {
			return (pc+1)+offset; // turn offset into absolute position 
		}
	}

	private static InstructionPtr getjumpcontrol(FuncState fs, int pc) {
		InstructionPtr pi = new InstructionPtr(fs.f.code, pc);
		if (pc >= 1 && (LuaOpCodes.testTMode(LuaOpCodes.GET_OPCODE(pi.get(-1))) != 0)) {
			return new InstructionPtr(pi.codes, pi.pc-1);
		}
		else {
			return new InstructionPtr(pi.codes, pi.pc);
		}
	}

//        
//		 ** check whether list has any jump that do not produce a value
//		 ** (or produce an inverted value)
//		 
	private static int need_value(FuncState fs, int list) {
		for (; list != NO_JUMP; list = getjump(fs, list)) {
			InstructionPtr i = getjumpcontrol(fs, list);
			if (LuaOpCodes.GET_OPCODE(i.get(0)) != OpCode.OP_TESTSET) {
				return 1;
			}
		}
		return 0; // not found 
	}

	private static int patchtestreg(FuncState fs, int node, int reg) {
		InstructionPtr i = getjumpcontrol(fs, node);
		if (LuaOpCodes.GET_OPCODE(i.get(0)) != OpCode.OP_TESTSET) {
			return 0; // cannot patch other instructions 
		}
		if (reg != LuaOpCodes.NO_REG && reg != LuaOpCodes.GETARG_B(i.get(0))) {
			LuaOpCodes.SETARG_A(i, reg);
		}
		else { // no register to put value or register already has the value 
			i.set(0, (long)(LuaOpCodes.CREATE_ABC(OpCode.OP_TEST, LuaOpCodes.GETARG_B(i.get(0)), 0, LuaOpCodes.GETARG_C(i.get(0))) &0xffffffff)); //uint
		}

		return 1;
	}

	private static void removevalues(FuncState fs, int list) {
		for (; list != NO_JUMP; list = getjump(fs, list)) {
			patchtestreg(fs, list, LuaOpCodes.NO_REG);
		}
	}

	private static void patchlistaux(FuncState fs, int list, int vtarget, int reg, int dtarget) {
		while (list != NO_JUMP) {
			int next = getjump(fs, list);
			if (patchtestreg(fs, list, reg) != 0) {
				fixjump(fs, list, vtarget);
			}
			else {
				fixjump(fs, list, dtarget); // jump to default target 
			}
			list = next;
		}
	}

	private static void dischargejpc(FuncState fs) {
		patchlistaux(fs, fs.jpc, fs.pc, LuaOpCodes.NO_REG, fs.pc);
		fs.jpc = NO_JUMP;
	}

	public static void luaK_patchlist(FuncState fs, int list, int target) {
		if (target == fs.pc) {
			luaK_patchtohere(fs, list);
		}
		else {
			LuaLimits.lua_assert(target < fs.pc);
			patchlistaux(fs, list, target, LuaOpCodes.NO_REG, target);
		}
	}

	public static void luaK_patchtohere(FuncState fs, int list) {
		luaK_getlabel(fs);
		int[] jpc_ref = new int[1];
		jpc_ref[0] = fs.jpc;
		luaK_concat(fs, jpc_ref, list); //ref
		fs.jpc = jpc_ref[0];
	}

	public static void luaK_concat(FuncState fs, int[] l1, int l2) { //ref
		if (l2 == NO_JUMP) {
			return;
		}
		else if (l1[0] == NO_JUMP) {
			l1[0] = l2;
		}
		else {
			int list = l1[0];
			int next;
			while ((next = getjump(fs, list)) != NO_JUMP) { // find last element 
				list = next;
			}
			fixjump(fs, list, l2);
		}
	}

	public static void luaK_checkstack(FuncState fs, int n) {
		int newstack = fs.freereg + n;
		if (newstack > fs.f.maxstacksize) {
			if (newstack >= LuaLimits.MAXSTACK) {
				LuaLex.luaX_syntaxerror(fs.ls, CharPtr.toCharPtr("function or expression too complex"));
			}
			fs.f.maxstacksize = LuaLimits.cast_byte(newstack);
		}
	}

	public static void luaK_reserveregs(FuncState fs, int n) {
		luaK_checkstack(fs, n);
		fs.freereg += n;
	}

	private static void freereg(FuncState fs, int reg) {
		if ((LuaOpCodes.ISK(reg) == 0) && reg >= fs.nactvar) {
			fs.freereg--;
			LuaLimits.lua_assert(reg == fs.freereg);
		}
	}

	private static void freeexp(FuncState fs, ExpDesc e) {
		if (e.k == ExpKind.VNONRELOC) {
			freereg(fs, e.u.s.info);
		}
	}

	private static int addk(FuncState fs, TValue k, TValue v) {
		LuaStateObject L = fs.L;
		TValue idx = LuaTable.luaH_set(L, fs.h, k);
		Proto f = fs.f;
		int oldsize = f.sizek;
		if (LuaObject.ttisnumber(idx)) {
			LuaLimits.lua_assert(LuaObject.luaO_rawequalObj(fs.f.k[LuaLimits.cast_int(LuaObject.nvalue(idx))], v));
			return LuaLimits.cast_int(LuaObject.nvalue(idx));
		}
		else {
			// constant not found; create a new entry 
			LuaObject.setnvalue(idx, LuaLimits.cast_num(fs.nk));
			TValue[][] k_ref = new TValue[1][];
			k_ref[0] = f.k;
			int[] sizek_ref = new int[1];
			sizek_ref[0] = f.sizek;
			LuaMem.luaM_growvector_TValue(L, k_ref, fs.nk, sizek_ref, LuaOpCodes.MAXARG_Bx, CharPtr.toCharPtr("constant table overflow"), new ClassType(ClassType.TYPE_TVALUE)); //ref - ref
			f.sizek = sizek_ref[0];
			f.k = k_ref[0];
			while (oldsize < f.sizek) {
				LuaObject.setnilvalue(f.k[oldsize++]);
			}
			LuaObject.setobj(L, f.k[fs.nk], v);
			LuaGC.luaC_barrier(L, f, v);
			return fs.nk++;
		}
	}

	public static int luaK_stringK(FuncState fs, TString s) {
		TValue o = new TValue();
		LuaObject.setsvalue(fs.L, o, s);
		return addk(fs, o, o);
	}

	public static int luaK_numberK(FuncState fs, double r) { //lua_Number
		TValue o = new TValue();
		LuaObject.setnvalue(o, r);
		return addk(fs, o, o);
	}

	private static int boolK(FuncState fs, int b) {
		TValue o = new TValue();
		LuaObject.setbvalue(o, b);
		return addk(fs, o, o);
	}

	private static int nilK(FuncState fs) {
		TValue k = new TValue(), v = new TValue();
		LuaObject.setnilvalue(v);
		// cannot use nil as key; instead use table itself to represent nil 
		LuaObject.sethvalue(fs.L, k, fs.h);
		return addk(fs, k, v);
	}

	public static void luaK_setreturns(FuncState fs, ExpDesc e, int nresults) {
		if (e.k == ExpKind.VCALL) {
			// expression is an open function call? 
			LuaOpCodes.SETARG_C(getcode(fs, e), nresults + 1);
		}
		else if (e.k == ExpKind.VVARARG) {
			LuaOpCodes.SETARG_B(getcode(fs, e), nresults + 1);
			LuaOpCodes.SETARG_A(getcode(fs, e), fs.freereg);
			luaK_reserveregs(fs, 1);
		}
	}

	public static void luaK_setoneret(FuncState fs, ExpDesc e) {
		if (e.k == ExpKind.VCALL) {
			// expression is an open function call? 
			e.k = ExpKind.VNONRELOC;
			e.u.s.info = LuaOpCodes.GETARG_A(getcode(fs, e));
		}
		else if (e.k == ExpKind.VVARARG) {
			LuaOpCodes.SETARG_B(getcode(fs, e), 2);
			e.k = ExpKind.VRELOCABLE; // can relocate its simple result 
		}
	}

	public static void luaK_dischargevars(FuncState fs, ExpDesc e) {
		switch (e.k) {
			case VLOCAL: {
					e.k = ExpKind.VNONRELOC;
					break;
				}
			case VUPVAL: {
					e.u.s.info = luaK_codeABC(fs, OpCode.OP_GETUPVAL, 0, e.u.s.info, 0);
					e.k = ExpKind.VRELOCABLE;
					break;
				}
			case VGLOBAL: {
					e.u.s.info = luaK_codeABx(fs, OpCode.OP_GETGLOBAL, 0, e.u.s.info);
					e.k = ExpKind.VRELOCABLE;
					break;
				}
			case VINDEXED: {
					freereg(fs, e.u.s.aux);
					freereg(fs, e.u.s.info);
					e.u.s.info = luaK_codeABC(fs, OpCode.OP_GETTABLE, 0, e.u.s.info, e.u.s.aux);
					e.k = ExpKind.VRELOCABLE;
					break;
				}
			case VVARARG:
			case VCALL: {
					luaK_setoneret(fs, e);
					break;
				}
			default: {
					break; // there is one value available (somewhere) 
				}
		}
	}

	private static int code_label(FuncState fs, int A, int b, int jump) {
		luaK_getlabel(fs); // those instructions may be jump targets 
		return luaK_codeABC(fs, OpCode.OP_LOADBOOL, A, b, jump);
	}

	private static void discharge2reg(FuncState fs, ExpDesc e, int reg) {
		luaK_dischargevars(fs, e);
		switch (e.k) {
			case VNIL: {
					luaK_nil(fs, reg, 1);
					break;
				}
			case VFALSE:
			case VTRUE: {
					luaK_codeABC(fs, OpCode.OP_LOADBOOL, reg, (e.k == ExpKind.VTRUE) ? 1 : 0, 0);
					break;
				}
			case VK: {
					luaK_codeABx(fs, OpCode.OP_LOADK, reg, e.u.s.info);
					break;
				}
			case VKNUM: {
					luaK_codeABx(fs, OpCode.OP_LOADK, reg, luaK_numberK(fs, e.u.nval));
					break;
				}
			case VRELOCABLE: {
					InstructionPtr pc = getcode(fs, e);
					LuaOpCodes.SETARG_A(pc, reg);
					break;
				}
			case VNONRELOC: {
					if (reg != e.u.s.info) {
						luaK_codeABC(fs, OpCode.OP_MOVE, reg, e.u.s.info, 0);
					}
					break;
				}
			default: {
					LuaLimits.lua_assert(e.k == ExpKind.VVOID || e.k == ExpKind.VJMP);
					return; // nothing to do... 
				}
		}
		e.u.s.info = reg;
		e.k = ExpKind.VNONRELOC;
	}

	private static void discharge2anyreg(FuncState fs, ExpDesc e) {
		if (e.k != ExpKind.VNONRELOC) {
			luaK_reserveregs(fs, 1);
			discharge2reg(fs, e, fs.freereg-1);
		}
	}

	private static void exp2reg(FuncState fs, ExpDesc e, int reg) {
		discharge2reg(fs, e, reg);
		if (e.k == ExpKind.VJMP) {
			int[] t_ref = new int[1];
			t_ref[0] = e.t;
			luaK_concat(fs, t_ref, e.u.s.info); // put this jump in `t' list  - ref
			e.t = t_ref[0];
		}
		if (hasjumps(e)) {
			int final_; // position after whole expression 
			int p_f = NO_JUMP; // position of an eventual LOAD false 
			int p_t = NO_JUMP; // position of an eventual LOAD true 
			if (need_value(fs, e.t) != 0 || need_value(fs, e.f) != 0) {
				int fj = (e.k == ExpKind.VJMP) ? NO_JUMP : luaK_jump(fs);
				p_f = code_label(fs, reg, 0, 1);
				p_t = code_label(fs, reg, 1, 0);
				luaK_patchtohere(fs, fj);
			}
			final_ = luaK_getlabel(fs);
			patchlistaux(fs, e.f, final_, reg, p_f);
			patchlistaux(fs, e.t, final_, reg, p_t);
		}
		e.f = e.t = NO_JUMP;
		e.u.s.info = reg;
		e.k = ExpKind.VNONRELOC;
	}

	public static void luaK_exp2nextreg(FuncState fs, ExpDesc e) {
		luaK_dischargevars(fs, e);
		freeexp(fs, e);
		luaK_reserveregs(fs, 1);
		exp2reg(fs, e, fs.freereg - 1);
	}

	public static int luaK_exp2anyreg(FuncState fs, ExpDesc e) {
		luaK_dischargevars(fs, e);
		if (e.k == ExpKind.VNONRELOC) {
			if (!hasjumps(e)) {
				return e.u.s.info; // exp is already in a register 
			}
			if (e.u.s.info >= fs.nactvar) {
				// reg. is not a local? 
				exp2reg(fs, e, e.u.s.info); // put value on it 
				return e.u.s.info;
			}
		}
		luaK_exp2nextreg(fs, e); // default 
		return e.u.s.info;
	}

	public static void luaK_exp2val(FuncState fs, ExpDesc e) {
		if (hasjumps(e)) {
			luaK_exp2anyreg(fs, e);
		}
		else {
			luaK_dischargevars(fs, e);
		}
	}

	public static int luaK_exp2RK(FuncState fs, ExpDesc e) {
		luaK_exp2val(fs, e);
		switch (e.k) {
			case VKNUM:
			case VTRUE:
			case VFALSE:
			case VNIL: {
					if (fs.nk <= LuaOpCodes.MAXINDEXRK) {
						// constant fit in RK operand? 
						e.u.s.info = (e.k == ExpKind.VNIL) ? nilK(fs) : (e.k == ExpKind.VKNUM) ? luaK_numberK(fs, e.u.nval) : boolK(fs, (e.k == ExpKind.VTRUE) ? 1 : 0);
						e.k = ExpKind.VK;
						return LuaOpCodes.RKASK(e.u.s.info);
					}
					else {
						break;
					}
				}
			case VK: {
					if (e.u.s.info <= LuaOpCodes.MAXINDEXRK) { // constant fit in argC? 
						return LuaOpCodes.RKASK(e.u.s.info);
					}
					else {
						break;
					}
				}
			default: {
					break;
				}
		}
		// not a constant in the right range: put it in a register 
		return luaK_exp2anyreg(fs, e);
	}


	public static void luaK_storevar(FuncState fs, ExpDesc var, ExpDesc ex) {
		switch (var.k) {
			case VLOCAL: {
					freeexp(fs, ex);
					exp2reg(fs, ex, var.u.s.info);
					return;
				}
			case VUPVAL: {
					int e = luaK_exp2anyreg(fs, ex);
					luaK_codeABC(fs, OpCode.OP_SETUPVAL, e, var.u.s.info, 0);
					break;
				}
			case VGLOBAL: {
					int e = luaK_exp2anyreg(fs, ex);
					luaK_codeABx(fs, OpCode.OP_SETGLOBAL, e, var.u.s.info);
					break;
				}
			case VINDEXED: {
					int e = luaK_exp2RK(fs, ex);
					luaK_codeABC(fs, OpCode.OP_SETTABLE, var.u.s.info, var.u.s.aux, e);
					break;
				}
			default: {
					LuaLimits.lua_assert(0); // invalid var kind to store 
					break;
				}
		}
		freeexp(fs, ex);
	}


	public static void luaK_self(FuncState fs, ExpDesc e, ExpDesc key) {
		int func;
		luaK_exp2anyreg(fs, e);
		freeexp(fs, e);
		func = fs.freereg;
		luaK_reserveregs(fs, 2);
		luaK_codeABC(fs, OpCode.OP_SELF, func, e.u.s.info, luaK_exp2RK(fs, key));
		freeexp(fs, key);
		e.u.s.info = func;
		e.k = ExpKind.VNONRELOC;
	}

	private static void invertjump(FuncState fs, ExpDesc e) {
		InstructionPtr pc = getjumpcontrol(fs, e.u.s.info);
		LuaLimits.lua_assert(LuaOpCodes.testTMode(LuaOpCodes.GET_OPCODE(pc.get(0))) != 0 && LuaOpCodes.GET_OPCODE(pc.get(0)) != OpCode.OP_TESTSET && LuaOpCodes.GET_OPCODE(pc.get(0)) != OpCode.OP_TEST);
		LuaOpCodes.SETARG_A(pc, (LuaOpCodes.GETARG_A(pc.get(0)) == 0) ? 1 : 0);
	}


	private static int jumponcond(FuncState fs, ExpDesc e, int cond) {
		if (e.k == ExpKind.VRELOCABLE) {
			InstructionPtr ie = getcode(fs, e);
			if (LuaOpCodes.GET_OPCODE(ie) == OpCode.OP_NOT) {
				fs.pc--; // remove previous OpCode.OP_NOT 
				return condjump(fs, OpCode.OP_TEST, LuaOpCodes.GETARG_B(ie), 0, (cond == 0) ? 1 : 0);
			}
			// else go through 
		}
		discharge2anyreg(fs, e);
		freeexp(fs, e);
		return condjump(fs, OpCode.OP_TESTSET, LuaOpCodes.NO_REG, e.u.s.info, cond);
	}

	public static void luaK_goiftrue(FuncState fs, ExpDesc e) {
		int pc; // pc of last jump 
		luaK_dischargevars(fs, e);
		switch (e.k) {
			case VK:
			case VKNUM:
			case VTRUE: {
					pc = NO_JUMP; // always true; do nothing 
					break;
				}
			case VFALSE: {
					pc = luaK_jump(fs); // always jump 
					break;
				}
			case VJMP: {
					invertjump(fs, e);
					pc = e.u.s.info;
					break;
				}
			default: {
					pc = jumponcond(fs, e, 0);
					break;
				}
		}
		int[] f_ref = new int[1];
		f_ref[0] = e.f;
		luaK_concat(fs, f_ref, pc); // insert last jump in `f' list  - ref
		e.f = f_ref[0];
		luaK_patchtohere(fs, e.t);
		e.t = NO_JUMP;
	}

	private static void luaK_goiffalse(FuncState fs, ExpDesc e) {
		int pc; // pc of last jump 
		luaK_dischargevars(fs, e);
		switch (e.k) {
			case VNIL:
			case VFALSE: {
					pc = LuaCode.NO_JUMP; // always false; do nothing 
					break;
				}
			case VTRUE: {
					pc = luaK_jump(fs); // always jump 
					break;
				}
			case VJMP: {
					pc = e.u.s.info;
					break;
				}
			default: {
					pc = jumponcond(fs, e, 1);
					break;
				}
		}
		int[] t_ref = new int[1];
		t_ref[0] = e.t;
		luaK_concat(fs, t_ref, pc); // insert last jump in `t' list  - ref
		e.t = t_ref[0];
		luaK_patchtohere(fs, e.f);
		e.f = NO_JUMP;
	}

	private static void codenot(FuncState fs, ExpDesc e) {
		luaK_dischargevars(fs, e);
		switch (e.k) {
			case VNIL:
			case VFALSE: {
					e.k = ExpKind.VTRUE;
					break;
				}
			case VK:
			case VKNUM:
			case VTRUE: {
					e.k = ExpKind.VFALSE;
					break;
				}
			case VJMP: {
				invertjump(fs, e);
				break;
			}
			case VRELOCABLE:
			case VNONRELOC: {
					discharge2anyreg(fs, e);
					freeexp(fs, e);
					e.u.s.info = luaK_codeABC(fs, OpCode.OP_NOT, 0, e.u.s.info, 0);
					e.k = ExpKind.VRELOCABLE;
					break;
				}
			default: {
					LuaLimits.lua_assert(0); // cannot happen 
					break;
				}
		}

		//
		// interchange true and false lists
		//

		if (true) {
			int temp = e.f;
			e.f = e.t;
			e.t = temp;
		}
		removevalues(fs, e.f);
		removevalues(fs, e.t);
	}

	public static void luaK_indexed(FuncState fs, ExpDesc t, ExpDesc k) {
		t.u.s.aux = luaK_exp2RK(fs, k);
		t.k = ExpKind.VINDEXED;
	}

	private static int constfolding(OpCode op, ExpDesc e1, ExpDesc e2) {
		double v1, v2, r; //lua_Number
		if ((isnumeral(e1)==0) || (isnumeral(e2)==0)) {
			return 0;
		}
		v1 = e1.u.nval;
		v2 = e2.u.nval;
		switch (op) {
			case OP_ADD: {
					r = LuaConf.luai_numadd(v1, v2);
					break;
				}
			case OP_SUB: {
					r = LuaConf.luai_numsub(v1, v2);
					break;
				}
			case OP_MUL: {
					r = LuaConf.luai_nummul(v1, v2);
					break;
				}
			case OP_DIV: {
					if (v2 == 0) {
						return 0; // do not attempt to divide by 0 
					}
					r = LuaConf.luai_numdiv(v1, v2);
					break;
				}
			case OP_MOD: {
					if (v2 == 0) {
						return 0; // do not attempt to divide by 0 
					}
					r = LuaConf.luai_nummod(v1, v2);
					break;
				}
			case OP_POW: {
					r = LuaConf.luai_numpow(v1, v2);
					break;
				}
			case OP_UNM: {
					r = LuaConf.luai_numunm(v1);
					break;
				}
			case OP_LEN: {
					return 0; // no constant folding for 'len' 
				}
			default: {
					LuaLimits.lua_assert(0);
					r = 0;
					break;
				}
		}
		if (LuaConf.luai_numisnan(r)) {
			return 0; // do not attempt to produce NaN 
		}
		e1.u.nval = r;
		return 1;
	}

	private static void codearith(FuncState fs, OpCode op, ExpDesc e1, ExpDesc e2) {
		if (constfolding(op, e1, e2) != 0) {
			return;
		}
		else {
			int o2 = (op != OpCode.OP_UNM && op != OpCode.OP_LEN) ? luaK_exp2RK(fs, e2) : 0;
			int o1 = luaK_exp2RK(fs, e1);
			if (o1 > o2) {
				freeexp(fs, e1);
				freeexp(fs, e2);
			}
			else {
				freeexp(fs, e2);
				freeexp(fs, e1);
			}
			e1.u.s.info = luaK_codeABC(fs, op, 0, o1, o2);
			e1.k = ExpKind.VRELOCABLE;
		}
	}

	private static void codecomp(FuncState fs, OpCode op, int cond, ExpDesc e1, ExpDesc e2) {
		int o1 = luaK_exp2RK(fs, e1);
		int o2 = luaK_exp2RK(fs, e2);
		freeexp(fs, e2);
		freeexp(fs, e1);
		if (cond == 0 && op != OpCode.OP_EQ) {
			int temp; // exchange args to replace by `<' or `<=' 
			temp = o1;
			o1 = o2;
			o2 = temp; // o1 <==> o2 
			cond = 1;
		}
		e1.u.s.info = condjump(fs, op, cond, o1, o2);
		e1.k = ExpKind.VJMP;
	}


	public static void luaK_prefix(FuncState fs, UnOpr op, ExpDesc e) {
		ExpDesc e2 = new ExpDesc();
		e2.t = e2.f = NO_JUMP;
		e2.k = ExpKind.VKNUM;
		e2.u.nval = 0;
		switch (op) {
			case OPR_MINUS: {
					if (isnumeral(e)==0) {
						luaK_exp2anyreg(fs, e); // cannot operate on non-numeric constants 
					}
					codearith(fs, OpCode.OP_UNM, e, e2);
					break;
				}
			case OPR_NOT: {
					codenot(fs, e);
					break;
				}
			case OPR_LEN: {
					luaK_exp2anyreg(fs, e); // cannot operate on constants 
					codearith(fs, OpCode.OP_LEN, e, e2);
					break;
				}
			default: {
					LuaLimits.lua_assert(0);
					break;
				}
		}
	}


	public static void luaK_infix(FuncState fs, BinOpr op, ExpDesc v) {
		switch (op) {
			case OPR_AND: {
					luaK_goiftrue(fs, v);
					break;
				}
			case OPR_OR: {
					luaK_goiffalse(fs, v);
					break;
				}
			case OPR_CONCAT: {
					luaK_exp2nextreg(fs, v); // operand must be on the `stack' 
					break;
				}
			case OPR_ADD:
			case OPR_SUB:
			case OPR_MUL:
			case OPR_DIV:
			case OPR_MOD:
			case OPR_POW: {
					if ((isnumeral(v)==0)) {
						luaK_exp2RK(fs, v);
					}
					break;
				}
			default: {
					luaK_exp2RK(fs, v);
					break;
				}
		}
	}


	public static void luaK_posfix(FuncState fs, BinOpr op, ExpDesc e1, ExpDesc e2) {
		switch (op) {
			case OPR_AND: {
					LuaLimits.lua_assert(e1.t == NO_JUMP); // list must be closed 
					luaK_dischargevars(fs, e2);
					int[] f_ref = new int[1];
					f_ref[0] = e2.f;
					luaK_concat(fs, f_ref, e1.f); //ref
					e2.f = f_ref[0];
					e1.Copy(e2);
					break;
				}
			case OPR_OR: {
					LuaLimits.lua_assert(e1.f == NO_JUMP); // list must be closed 
					luaK_dischargevars(fs, e2);
					int[] t_ref = new int[1];
					t_ref[0] = e2.t;
					luaK_concat(fs, t_ref, e1.t); //ref
					e2.t = t_ref[0];
					e1.Copy(e2);
					break;
				}
			case OPR_CONCAT: {
					luaK_exp2val(fs, e2);
					if (e2.k == ExpKind.VRELOCABLE && LuaOpCodes.GET_OPCODE(getcode(fs, e2)) == OpCode.OP_CONCAT) {
						LuaLimits.lua_assert(e1.u.s.info == LuaOpCodes.GETARG_B(getcode(fs, e2)) - 1);
						freeexp(fs, e1);
						LuaOpCodes.SETARG_B(getcode(fs, e2), e1.u.s.info);
						e1.k = ExpKind.VRELOCABLE;
						e1.u.s.info = e2.u.s.info;
					}
					else {
						luaK_exp2nextreg(fs, e2); // operand must be on the 'stack' 
						codearith(fs, OpCode.OP_CONCAT, e1, e2);
					}
					break;
				}
			case OPR_ADD: {
					codearith(fs, OpCode.OP_ADD, e1, e2);
					break;
				}
			case OPR_SUB: {
					codearith(fs, OpCode.OP_SUB, e1, e2);
					break;
				}
			case OPR_MUL: {
					codearith(fs, OpCode.OP_MUL, e1, e2);
					break;
				}
			case OPR_DIV: {
					codearith(fs, OpCode.OP_DIV, e1, e2);
					break;
				}
			case OPR_MOD: {
					codearith(fs, OpCode.OP_MOD, e1, e2);
					break;
				}
			case OPR_POW: {
					codearith(fs, OpCode.OP_POW, e1, e2);
					break;
				}
			case OPR_EQ: {
					codecomp(fs, OpCode.OP_EQ, 1, e1, e2);
					break;
				}
			case OPR_NE: {
					codecomp(fs, OpCode.OP_EQ, 0, e1, e2);
					break;
				}
			case OPR_LT: {
					codecomp(fs, OpCode.OP_LT, 1, e1, e2);
					break;
				}
			case OPR_LE: {
					codecomp(fs, OpCode.OP_LE, 1, e1, e2);
					break;
				}
			case OPR_GT: {
					codecomp(fs, OpCode.OP_LT, 0, e1, e2);
					break;
				}
			case OPR_GE: {
					codecomp(fs, OpCode.OP_LE, 0, e1, e2);
					break;
				}
			default: {
					LuaLimits.lua_assert(0);
					break;
				}
		}
	}

	public static void luaK_fixline(FuncState fs, int line) {
		fs.f.lineinfo[fs.pc - 1] = line;
	}

	private static int luaK_code(FuncState fs, int i, int line) {
		Proto f = fs.f;
		dischargejpc(fs); // `pc' will change 
		// put new instruction in code array 
		long[][] code_ref = new long[1][];
		code_ref[0] = f.code;
		int[] sizecode_ref = new int[1];
		sizecode_ref[0] = f.sizecode;
		LuaMem.luaM_growvector_long(fs.L, code_ref, fs.pc, sizecode_ref, LuaLimits.MAX_INT, CharPtr.toCharPtr("code size overflow"), new ClassType(ClassType.TYPE_LONG)); //ref - ref
		f.sizecode = sizecode_ref[0];
		f.code = code_ref[0];
		f.code[fs.pc] = (long)i; //uint
		// save corresponding line information 
		int[][] lineinfo_ref = new int[1][];
		lineinfo_ref[0] = f.lineinfo;
		int[] sizelineinfo_ref = new int[1];
		sizelineinfo_ref[0] = f.sizelineinfo;
		LuaMem.luaM_growvector_int(fs.L, lineinfo_ref, fs.pc, sizelineinfo_ref, LuaLimits.MAX_INT, CharPtr.toCharPtr("code size overflow"), new ClassType(ClassType.TYPE_INT)); //ref - ref
		f.sizelineinfo = sizelineinfo_ref[0];
		f.lineinfo = lineinfo_ref[0];
		f.lineinfo[fs.pc] = line;
		return fs.pc++;
	}

	public static int luaK_codeABC(FuncState fs, OpCode o, int a, int b, int c) {
		LuaLimits.lua_assert(LuaOpCodes.getOpMode(o) == OpMode.iABC);
		LuaLimits.lua_assert(LuaOpCodes.getBMode(o) != OpArgMask.OpArgN || b == 0);
		LuaLimits.lua_assert(LuaOpCodes.getCMode(o) != OpArgMask.OpArgN || c == 0);
		return luaK_code(fs, LuaOpCodes.CREATE_ABC(o, a, b, c), fs.ls.lastline);
	}

	public static int luaK_codeABx(FuncState fs, OpCode o, int a, int bc) {
		LuaLimits.lua_assert(LuaOpCodes.getOpMode(o) == OpMode.iABx || LuaOpCodes.getOpMode(o) == OpMode.iAsBx);
		LuaLimits.lua_assert(LuaOpCodes.getCMode(o) == OpArgMask.OpArgN);
		return luaK_code(fs, LuaOpCodes.CREATE_ABx(o, a, bc), fs.ls.lastline);
	}

	public static void luaK_setlist(FuncState fs, int base_, int nelems, int tostore) {
		int c = (nelems - 1) / LuaOpCodes.LFIELDS_PER_FLUSH + 1;
		int b = (tostore == Lua.LUA_MULTRET) ? 0 : tostore;
		LuaLimits.lua_assert(tostore != 0);
		if (c <= LuaOpCodes.MAXARG_C) {
			luaK_codeABC(fs, OpCode.OP_SETLIST, base_, b, c);
		}
		else {
			luaK_codeABC(fs, OpCode.OP_SETLIST, base_, b, 0);
			luaK_code(fs, c, fs.ls.lastline);
		}
		fs.freereg = base_ + 1; // free registers with list values 
	}
}