package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.constant.OpCode;
import com.iteye.weimingtom.kurumi.constant.TMS;
import com.iteye.weimingtom.kurumi.exception.LuaException;
import com.iteye.weimingtom.kurumi.func.FParserDelegate;
import com.iteye.weimingtom.kurumi.func.LuaHook;
import com.iteye.weimingtom.kurumi.func.Pfunc;
import com.iteye.weimingtom.kurumi.func.ResumeDelegate;
import com.iteye.weimingtom.kurumi.model.CallInfo;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.Closure;
import com.iteye.weimingtom.kurumi.model.InstructionPtr;
import com.iteye.weimingtom.kurumi.model.LClosure;
import com.iteye.weimingtom.kurumi.model.LuaDebugState;
import com.iteye.weimingtom.kurumi.model.LuaLongjmp;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.model.Proto;
import com.iteye.weimingtom.kurumi.model.SParser;
import com.iteye.weimingtom.kurumi.model.TValue;
import com.iteye.weimingtom.kurumi.model.Table;
import com.iteye.weimingtom.kurumi.model.ZIO;
import com.iteye.weimingtom.kurumi.port.ClassType;

//
// ** $Id: ldo.c,v 2.38.1.3 2008/01/18 22:31:22 roberto Exp $
// ** Stack and Call structure of Lua
// ** See Copyright Notice in lua.h
// 

//using lua_Integer = System.Int32;
//using ptrdiff_t = System.Int32;
//using TValue = Lua.TValue;
//using StkId = TValue;
//using lu_byte = System.Byte;

public class LuaDo {
	public static void luaD_checkstack(LuaStateObject L, int n) {
		if (TValue.minus(L.stack_last, L.top) <= n) {
			luaD_growstack(L, n);
		}
		else {
			///#if HARDSTACKTESTS
//				luaD_reallocstack(L, L.stacksize - EXTRA_STACK - 1);
			///#endif
		}
	}

	public static void incr_top(LuaStateObject L) {
		luaD_checkstack(L, 1);
		TValue[] top = new TValue[1];
		top[0] = L.top;
		//StkId
TValue.inc(top); //ref
		L.top = top[0];
	}

	// in the original C code these values save and restore the stack by number of bytes. marshalling sizeof
	// isn't that straightforward in managed languages, so i implement these by index instead.
	public static int savestack(LuaStateObject L, TValue p) { //StkId
		return TValue.toInt(p);
	}

	public static TValue restorestack(LuaStateObject L, int n) { //StkId
		return L.stack[n];
	}

	public static int saveci(LuaStateObject L, CallInfo p) {
		return CallInfo.minus(p, L.base_ci);
	}

	public static CallInfo restoreci(LuaStateObject L, int n) {
		return L.base_ci[n];
	}

	// results from luaD_precall 
	public static final int PCRLUA = 0; // initiated a call to a Lua function 
	public static final int PCRC = 1; // did a call to a C function 
	public static final int PCRYIELD = 2; // C funtion yielded 

	public static void luaD_seterrorobj(LuaStateObject L, int errcode, TValue oldtop) { //StkId
		switch (errcode) {
			case Lua.LUA_ERRMEM: {
					LuaObject.setsvalue2s(L, oldtop, LuaString.luaS_newliteral(L, CharPtr.toCharPtr(LuaMem.MEMERRMSG)));
					break;
				}
			case Lua.LUA_ERRERR: {
					LuaObject.setsvalue2s(L, oldtop, LuaString.luaS_newliteral(L, CharPtr.toCharPtr("error in error handling")));
					break;
				}
			case Lua.LUA_ERRSYNTAX:
			case Lua.LUA_ERRRUN: {
					LuaObject.setobjs2s(L, oldtop, TValue.minus(L.top, 1)); // error message on current top 
					break;
				}
		}
		L.top = TValue.plus(oldtop, 1);
	}

	private static void restore_stack_limit(LuaStateObject L) {
		LuaLimits.lua_assert(TValue.toInt(L.stack_last) == L.stacksize - LuaState.EXTRA_STACK - 1);
		if (L.size_ci > LuaConf.LUAI_MAXCALLS) {
			// there was an overflow? 
			int inuse = CallInfo.minus(L.ci, L.base_ci);
			if (inuse + 1 < LuaConf.LUAI_MAXCALLS) { // can `undo' overflow? 
				luaD_reallocCI(L, LuaConf.LUAI_MAXCALLS);
			}
		}
	}

	private static void resetstack(LuaStateObject L, int status) {
		L.ci = L.base_ci[0];
		L.base_ = L.ci.base_;
		LuaFunc.luaF_close(L, L.base_); // close eventual pending closures 
		luaD_seterrorobj(L, status, L.base_);
		L.nCcalls = L.baseCcalls;
		L.allowhook = 1;
		restore_stack_limit(L);
		L.errfunc = 0;
		L.errorJmp = null;
	}

	public static void luaD_throw(LuaStateObject L, int errcode) {
		if (L.errorJmp != null) {
			L.errorJmp.status = errcode;
			LuaConf.LUAI_THROW(L, L.errorJmp);
		}
		else {
			L.status = LuaLimits.cast_byte(errcode);
			if (LuaState.G(L).panic != null) {
				resetstack(L, errcode);
				LuaLimits.lua_unlock(L);
				LuaState.G(L).panic.exec(L);
			}
			System.exit(LuaConf.EXIT_FAILURE);
		}
	}

	public static int luaD_rawrunprotected(LuaStateObject L, Pfunc f, Object ud) {
		LuaLongjmp lj = new LuaLongjmp();
		lj.status = 0;
		lj.previous = L.errorJmp; // chain new error handler 
		L.errorJmp = lj;
		//LUAI_TRY(L, lj,
		//f(L, ud)
		  //);
		if (LuaConf.CATCH_EXCEPTIONS) {
			try {
				f.exec(L, ud);
			}
			catch (java.lang.Exception e) {
				if (lj.status == 0) {
					lj.status = -1;
				}
			}
		}
		else {
			try {
				f.exec(L, ud);
			}
			catch (LuaException e) {
				if (lj.status == 0) {
					lj.status = -1;
				}
			}
		}
		L.errorJmp = lj.previous; // restore old error handler 
		return lj.status;
	}

	// }====================================================== 

	private static void correctstack(LuaStateObject L, TValue[] oldstack) {
//             don't need to do this
//		  CallInfo ci;
//		  GCObject up;
//		  L.top = L.stack[L.top - oldstack];
//		  for (up = L.openupval; up != null; up = up.gch.next)
//			gco2uv(up).v = L.stack[gco2uv(up).v - oldstack];
//		  for (ci = L.base_ci[0]; ci <= L.ci; CallInfo.inc(ref ci)) {
//			  ci.top = L.stack[ci.top - oldstack];
//			ci.base_ = L.stack[ci.base_ - oldstack];
//			ci.func = L.stack[ci.func - oldstack];
//		  }
//		  L.base_ = L.stack[L.base_ - oldstack];
//			 * 
	}

	public static void luaD_reallocstack(LuaStateObject L, int newsize) {
		TValue[] oldstack = L.stack;
		int realsize = newsize + 1 + LuaState.EXTRA_STACK;
		LuaLimits.lua_assert(TValue.toInt(L.stack_last) == L.stacksize - LuaState.EXTRA_STACK - 1);
		TValue[][] stack = new TValue[1][];
		stack[0] = L.stack;
		LuaMem.luaM_reallocvector_TValue(L, stack, L.stacksize, realsize, new ClassType(ClassType.TYPE_TVALUE)); //, TValue - ref
		L.stack = stack[0];
		L.stacksize = realsize;
		L.stack_last = L.stack[newsize];
		correctstack(L, oldstack);
	}

	public static void luaD_reallocCI(LuaStateObject L, int newsize) {
		CallInfo oldci = L.base_ci[0];
		CallInfo[][] base_ci = new CallInfo[1][];
		base_ci[0] = L.base_ci;
		LuaMem.luaM_reallocvector_CallInfo(L, base_ci, L.size_ci, newsize, new ClassType(ClassType.TYPE_CALLINFO)); //, CallInfo - ref
		L.base_ci = base_ci[0];
		L.size_ci = newsize;
		L.ci = L.base_ci[CallInfo.minus(L.ci, oldci)];
		L.end_ci = L.base_ci[L.size_ci - 1];
	}

	public static void luaD_growstack(LuaStateObject L, int n) {
		if (n <= L.stacksize) { // double size is enough? 
			luaD_reallocstack(L, 2*L.stacksize);
		}
		else {
			luaD_reallocstack(L, L.stacksize + n);
		}
	}

	private static CallInfo growCI(LuaStateObject L) {
		if (L.size_ci > LuaConf.LUAI_MAXCALLS) { // overflow while handling overflow? 
			luaD_throw(L, Lua.LUA_ERRERR);
		}
		else {
			luaD_reallocCI(L, 2*L.size_ci);
			if (L.size_ci > LuaConf.LUAI_MAXCALLS) {
				LuaDebug.luaG_runerror(L, CharPtr.toCharPtr("stack overflow"));
			}
		}
		CallInfo[] ci_ref = new CallInfo[1];
		ci_ref[0] = L.ci;
		CallInfo.inc(ci_ref); //ref
		L.ci = ci_ref[0];
		return L.ci;
	}

	public static void luaD_callhook(LuaStateObject L, int event_, int line) {
		LuaHook hook = L.hook;
		if ((hook!=null) && (L.allowhook!=0)) {
			int top = savestack(L, L.top); //ptrdiff_t - Int32
			int ci_top = savestack(L, L.ci.top); //ptrdiff_t - Int32
			LuaDebugState ar = new LuaDebugState();
			ar.event_ = event_;
			ar.currentline = line;
			if (event_ == Lua.LUA_HOOKTAILRET) {
				ar.i_ci = 0; // tail call; no debug information about it 
			}
			else {
				ar.i_ci = CallInfo.minus(L.ci, L.base_ci);
			}
			luaD_checkstack(L, Lua.LUA_MINSTACK); // ensure minimum stack size 
			L.ci.top = TValue.plus(L.top, Lua.LUA_MINSTACK);
			LuaLimits.lua_assert(TValue.lessEqual(L.ci.top, L.stack_last));
			L.allowhook = 0; // cannot call hooks inside a hook 
			LuaLimits.lua_unlock(L);
			hook.exec(L, ar);
			LuaLimits.lua_lock(L);
			LuaLimits.lua_assert(L.allowhook == 0);
			L.allowhook = 1;
			L.ci.top = restorestack(L, ci_top);
			L.top = restorestack(L, top);
		}
	}


	private static TValue adjust_varargs(LuaStateObject L, Proto p, int actual) { //StkId
		int i;
		int nfixargs = p.numparams;
		Table htab = null;
		TValue base_, fixed_; //StkId
		for (; actual < nfixargs; ++actual) {
			TValue[] top = new TValue[1];
			top[0] = L.top;
			TValue ret = TValue.inc(top); //ref - StkId
			L.top = top[0];
			LuaObject.setnilvalue(ret);
		}
		///#if LUA_COMPAT_VARARG
		if ((p.is_vararg & LuaObject.VARARG_NEEDSARG) != 0)
		{ // compat. with old-style vararg? 
			int nvar = actual - nfixargs; // number of extra arguments 
			LuaLimits.lua_assert(p.is_vararg & LuaObject.VARARG_HASARG);
			LuaGC.luaC_checkGC(L);
			htab = LuaTable.luaH_new(L, nvar, 1); // create `arg' table 
			for (i=0; i<nvar; i++) { // put extra arguments into `arg' table 
			LuaObject.setobj2n(L, LuaTable.luaH_setnum(L, htab, i + 1), TValue.plus(TValue.minus(L.top, nvar), i)); //FIXME:
			}
			// store counter in field `n' 
			LuaObject.setnvalue(LuaTable.luaH_setstr(L, htab, LuaString.luaS_newliteral(L, CharPtr.toCharPtr("n"))), LuaLimits.cast_num(nvar));
		}
		///#endif
		// move fixed parameters to final position 
		fixed_ = TValue.minus(L.top, actual); // first fixed argument 
		base_ = L.top; // final position of first argument 
		for (i = 0; i < nfixargs; i++) {
			TValue[] top = new TValue[1];
			top[0] = L.top;
			TValue ret = TValue.inc(top); //ref - StkId
			L.top = top[0];
			LuaObject.setobjs2s(L, ret, TValue.plus(fixed_, i));
			LuaObject.setnilvalue(TValue.plus(fixed_, i));
		}
		// add `arg' parameter 
		if (htab != null) {
			TValue top = L.top; //StkId
			TValue[] top_ref = new TValue[1];
			top_ref[0] = L.top;
			//StkId
TValue.inc(top_ref); //ref
			L.top = top_ref[0];
			LuaObject.sethvalue(L, top, htab);
			LuaLimits.lua_assert(LuaGC.iswhite(LuaState.obj2gco(htab)));
		}
		return base_;
	}


	private static TValue tryfuncTM(LuaStateObject L, TValue func) { //StkId - StkId
		//const
		TValue tm = LuaTM.luaT_gettmbyobj(L, func, TMS.TM_CALL);
		TValue[] p = new TValue[1]; //StkId
		p[0] = new TValue();
		int funcr = savestack(L, func); //ptrdiff_t - Int32
		if (!LuaObject.ttisfunction(tm)) {
			LuaDebug.luaG_typeerror(L, func, CharPtr.toCharPtr("call"));
		}
		// Open a hole inside the stack at `func' 
		for (p[0] = L.top; TValue.greaterThan(p[0], func); TValue.dec(p)) { //ref - StkId
			LuaObject.setobjs2s(L, p[0], TValue.minus(p[0], 1));
		}
		incr_top(L);
		func = restorestack(L, funcr); // previous call may change stack 
		LuaObject.setobj2s(L, func, tm); // tag method is the new function to be called 
		return func;
	}



	public static CallInfo inc_ci(LuaStateObject L) {
		if (L.ci == L.end_ci) {
			return growCI(L);
		}
		//   (condhardstacktests(luaD_reallocCI(L, L.size_ci)), ++L.ci))
		CallInfo[] ci_ref = new CallInfo[1];
		ci_ref[0] = L.ci;
		CallInfo.inc(ci_ref); //ref
		L.ci = ci_ref[0];
		return L.ci;
	}


	public static int luaD_precall(LuaStateObject L, TValue func, int nresults) { //StkId
		LClosure cl;
		int funcr; //ptrdiff_t - Int32
		if (!LuaObject.ttisfunction(func)) { // `func' is not a function? 
		func = tryfuncTM(L, func); // check the `function' tag method 
		}
		funcr = savestack(L, func);
		cl = LuaObject.clvalue(func).l;
		L.ci.savedpc = InstructionPtr.Assign(L.savedpc);
		if (cl.getIsC() == 0) {
			// Lua function? prepare its call 
			CallInfo ci;
			TValue[] st = new TValue[1]; //StkId
			st[0] = new TValue();
			TValue base_; //StkId
			Proto p = cl.p;
			luaD_checkstack(L, p.maxstacksize);
			func = restorestack(L, funcr);
			if (p.is_vararg == 0) {
				// no varargs? 
				base_ = L.stack[TValue.toInt(TValue.plus(func, 1))];
				if (TValue.greaterThan(L.top, TValue.plus(base_, p.numparams))) {
					L.top = TValue.plus(base_, p.numparams);
				}
			}
			else {
				// vararg function 
				int nargs = TValue.minus(L.top, func) - 1;
				base_ = adjust_varargs(L, p, nargs);
				func = restorestack(L, funcr); // previous call may change the stack 
			}
			ci = inc_ci(L); // now `enter' new function 
			ci.func = func;
			L.base_ = ci.base_ = base_;
			ci.top = TValue.plus(L.base_, p.maxstacksize);
			LuaLimits.lua_assert(TValue.lessEqual(ci.top, L.stack_last));
			L.savedpc = new InstructionPtr(p.code, 0); // starting point 
			ci.tailcalls = 0;
			ci.nresults = nresults;
			for (st[0] = L.top; TValue.lessThan(st[0], ci.top); TValue.inc(st)) { //ref - StkId
				LuaObject.setnilvalue(st[0]);
			}
			L.top = ci.top;
			if ((L.hookmask & Lua.LUA_MASKCALL) != 0) {
				InstructionPtr[] savedpc_ref = new InstructionPtr[1];
				savedpc_ref[0] = L.savedpc;
				InstructionPtr.inc(savedpc_ref); // hooks assume 'pc' is already incremented  - ref
				L.savedpc = savedpc_ref[0];
				luaD_callhook(L, Lua.LUA_HOOKCALL, -1);
				savedpc_ref[0] = L.savedpc;
				InstructionPtr.dec(savedpc_ref); // correct 'pc'  - ref
				L.savedpc = savedpc_ref[0];
			}
			return PCRLUA;
		}
		else { // if is a C function, call it 
			CallInfo ci;
			int n;
			luaD_checkstack(L, Lua.LUA_MINSTACK); // ensure minimum stack size 
			ci = inc_ci(L); // now `enter' new function 
			ci.func = restorestack(L, funcr);
			L.base_ = ci.base_ = TValue.plus(ci.func, 1);
			ci.top = TValue.plus(L.top, Lua.LUA_MINSTACK);
			LuaLimits.lua_assert(TValue.lessEqual(ci.top, L.stack_last));
			ci.nresults = nresults;
			if ((L.hookmask & Lua.LUA_MASKCALL) != 0) {
				luaD_callhook(L, Lua.LUA_HOOKCALL, -1);
			}
			LuaLimits.lua_unlock(L);
			n = LuaState.curr_func(L).c.f.exec(L); // do the actual call 
			LuaLimits.lua_lock(L);
			if (n < 0) { // yielding? 
				return PCRYIELD;
			}
			else {
				luaD_poscall(L, TValue.minus(L.top, n));
				return PCRC;
			}
		}
	}


	private static TValue callrethooks(LuaStateObject L, TValue firstResult) { //StkId - StkId
		int fr = savestack(L, firstResult); // next call may change stack  - ptrdiff_t - Int32
		luaD_callhook(L, Lua.LUA_HOOKRET, -1);
		if (LuaState.f_isLua(L.ci))
		{ // Lua function? 
			while (((L.hookmask & Lua.LUA_MASKRET) != 0) && (L.ci.tailcalls-- != 0)) { // tail calls 
			luaD_callhook(L, Lua.LUA_HOOKTAILRET, -1);
			}
		}
		return restorestack(L, fr);
	}


	public static int luaD_poscall(LuaStateObject L, TValue firstResult) { //StkId
		TValue res; //StkId
		int wanted, i;
		CallInfo ci;
		if ((L.hookmask & Lua.LUA_MASKRET) != 0) {
			firstResult = callrethooks(L, firstResult);
		}
		CallInfo[] ci_ref = new CallInfo[1];
		ci_ref[0] = L.ci;
		ci = CallInfo.dec(ci_ref); //ref
		L.ci = ci_ref[0];
		res = ci.func; // res == final position of 1st result 
		wanted = ci.nresults;
		L.base_ = CallInfo.minus(ci, 1).base_; // restore base 
		L.savedpc = InstructionPtr.Assign(CallInfo.minus(ci, 1).savedpc); // restore savedpc 
		// move results to correct place 
		for (i = wanted; i != 0 && TValue.lessThan(firstResult, L.top); i--) {
			LuaObject.setobjs2s(L, res, firstResult);
			res = TValue.plus(res, 1);
			firstResult = TValue.plus(firstResult, 1);
		}
		while (i-- > 0) {
			TValue[] res_ref = new TValue[1];
			res_ref[0] = res;
			TValue ret = TValue.inc(res_ref); //ref - StkId
			res = res_ref[0];
			LuaObject.setnilvalue(ret);
		}
		L.top = res;
		return (wanted - Lua.LUA_MULTRET); // 0 iff wanted == LUA_MULTRET 
	}


//        
//		 ** Call a function (C or Lua). The function to be called is at *func.
//		 ** The arguments are on the stack, right after the function.
//		 ** When returns, all the results are on the stack, starting at the original
//		 ** function position.
//		 
	//private
	public static void luaD_call(LuaStateObject L, TValue func, int nResults) { //StkId
		if (++L.nCcalls >= LuaConf.LUAI_MAXCCALLS) {
			if (L.nCcalls == LuaConf.LUAI_MAXCCALLS) {
				LuaDebug.luaG_runerror(L, CharPtr.toCharPtr("C stack overflow"));
			}
			else if (L.nCcalls >= (LuaConf.LUAI_MAXCCALLS + (LuaConf.LUAI_MAXCCALLS >> 3))) {
				luaD_throw(L, Lua.LUA_ERRERR); // error while handing stack error 
			}
		}
		if (luaD_precall(L, func, nResults) == PCRLUA) { // is a Lua function? 
		LuaVM.luaV_execute(L, 1); // call it 
		}
		L.nCcalls--;
		LuaGC.luaC_checkGC(L);
	}

	public static void resume(LuaStateObject L, Object ud) {
		TValue firstArg = (TValue)ud; //StkId - StkId
		CallInfo ci = L.ci;
		if (L.status == 0) {
			// start coroutine? 
			LuaLimits.lua_assert(ci == L.base_ci[0] && TValue.greaterThan(firstArg, L.base_));
			if (luaD_precall(L, TValue.minus(firstArg, 1), Lua.LUA_MULTRET) != PCRLUA) {
				return;
			}
		}
		else {
			// resuming from previous yield 
			LuaLimits.lua_assert(L.status == Lua.LUA_YIELD);
			L.status = 0;
			if (!LuaState.f_isLua(ci)) {
				// `common' yield? 
				// finish interrupted execution of `OP_CALL' 
				LuaLimits.lua_assert(LuaOpCodes.GET_OPCODE(CallInfo.minus(ci, 1).savedpc.get(-1)) == OpCode.OP_CALL || LuaOpCodes.GET_OPCODE(CallInfo.minus(ci, 1).savedpc.get(-1)) == OpCode.OP_TAILCALL);
				if (luaD_poscall(L, firstArg) != 0) {
					// complete it... 
					L.top = L.ci.top; // and correct top if not multiple results 
				}
			}
			else {
				// yielded inside a hook: just continue its execution 
				L.base_ = L.ci.base_;
			}
		}
		LuaVM.luaV_execute(L, CallInfo.minus(L.ci, L.base_ci));
	}

	private static int resume_error(LuaStateObject L, CharPtr msg) {
		L.top = L.ci.base_;
		LuaObject.setsvalue2s(L, L.top, LuaString.luaS_new(L, msg));
		incr_top(L);
		LuaLimits.lua_unlock(L);
		return Lua.LUA_ERRRUN;
	}


	public static int lua_resume(LuaStateObject L, int nargs) {
		int status;
		LuaLimits.lua_lock(L);
		if (L.status != Lua.LUA_YIELD && (L.status != 0 || (L.ci != L.base_ci[0]))) {
			return resume_error(L, CharPtr.toCharPtr("cannot resume non-suspended coroutine"));
		}
		if (L.nCcalls >= LuaConf.LUAI_MAXCCALLS) {
			return resume_error(L, CharPtr.toCharPtr("C stack overflow"));
		}
		LuaConf.luai_userstateresume(L, nargs);
		LuaLimits.lua_assert(L.errfunc == 0);
		L.baseCcalls = ++L.nCcalls;
		status = luaD_rawrunprotected(L, new ResumeDelegate(), TValue.minus(L.top, nargs));
		if (status != 0) {
			// error? 
			L.status = LuaLimits.cast_byte(status); // mark thread as `dead' 
			luaD_seterrorobj(L, status, L.top);
			L.ci.top = L.top;
		}
		else {
			LuaLimits.lua_assert(L.nCcalls == L.baseCcalls);
			status = L.status;
		}
		--L.nCcalls;
		LuaLimits.lua_unlock(L);
		return status;
	}

	public static int lua_yield(LuaStateObject L, int nresults) {
		LuaConf.luai_userstateyield(L, nresults);
		LuaLimits.lua_lock(L);
		if (L.nCcalls > L.baseCcalls) {
			LuaDebug.luaG_runerror(L, CharPtr.toCharPtr("attempt to yield across metamethod/C-call boundary"));
		}
		L.base_ = TValue.minus(L.top, nresults); // protect stack slots below 
		L.status = Lua.LUA_YIELD;
		LuaLimits.lua_unlock(L);
		return -1;
	}

	public static int luaD_pcall(LuaStateObject L, Pfunc func, Object u, int old_top, int ef) { //ptrdiff_t - Int32 - ptrdiff_t - Int32
		int status;
		int oldnCcalls = L.nCcalls; //ushort
		int old_ci = saveci(L, L.ci); //ptrdiff_t - Int32
		byte old_allowhooks = L.allowhook; //lu_byte - Byte
		int old_errfunc = L.errfunc; //ptrdiff_t - Int32
		L.errfunc = ef;
		status = luaD_rawrunprotected(L, func, u);
		if (status != 0) {
			// an error occurred? 
			TValue oldtop = restorestack(L, old_top); //StkId
			LuaFunc.luaF_close(L, oldtop); // close eventual pending closures 
			luaD_seterrorobj(L, status, oldtop);
			L.nCcalls = oldnCcalls;
			L.ci = restoreci(L, old_ci);
			L.base_ = L.ci.base_;
			L.savedpc = InstructionPtr.Assign(L.ci.savedpc);
			L.allowhook = old_allowhooks;
			restore_stack_limit(L);
		}
		L.errfunc = old_errfunc;
		return status;
	}

	public static void f_parser(LuaStateObject L, Object ud) {
		int i;
		Proto tf;
		Closure cl;
		SParser p = (SParser)ud;
		int c = LuaZIO.luaZ_lookahead(p.z);
		LuaGC.luaC_checkGC(L);
		tf = (c == Lua.LUA_SIGNATURE.charAt(0)) ? LuaUndump.luaU_undump(L, p.z, p.buff, p.name) : LuaParser.luaY_parser(L, p.z, p.buff, p.name);
		cl = LuaFunc.luaF_newLclosure(L, tf.nups, LuaObject.hvalue(LuaState.gt(L)));
		cl.l.p = tf;
		for (i = 0; i < tf.nups; i++) { // initialize eventual upvalues 
			cl.l.upvals[i] = LuaFunc.luaF_newupval(L);
		}
		LuaObject.setclvalue(L, L.top, cl);
		incr_top(L);
	}

	public static int luaD_protectedparser(LuaStateObject L, ZIO z, CharPtr name) {
		SParser p = new SParser();
		int status;
		p.z = z;
		p.name = new CharPtr(name);
		LuaZIO.luaZ_initbuffer(L, p.buff);
		status = luaD_pcall(L, new FParserDelegate(), p, savestack(L, L.top), L.errfunc);
		LuaZIO.luaZ_freebuffer(L, p.buff);
		return status;
	}
}
// type of protected functions, to be ran by `runprotected' 
//public delegate void Pfunc(lua_State L, object ud);
//public delegate void luai_jmpbuf(int/*Int32*//*lua_Integer*/ b);
