package com.iteye.weimingtom.kurumi.core;

import com.iteye.weimingtom.kurumi.model.CallInfo;
import com.iteye.weimingtom.kurumi.model.CharPtr;
import com.iteye.weimingtom.kurumi.model.Closure;
import com.iteye.weimingtom.kurumi.model.GCObject;
import com.iteye.weimingtom.kurumi.model.LocVar;
import com.iteye.weimingtom.kurumi.model.LuaStateObject;
import com.iteye.weimingtom.kurumi.model.Node;
import com.iteye.weimingtom.kurumi.model.Proto;
import com.iteye.weimingtom.kurumi.model.TString;
import com.iteye.weimingtom.kurumi.model.TValue;
import com.iteye.weimingtom.kurumi.model.Table;
import com.iteye.weimingtom.kurumi.model.Udata;
import com.iteye.weimingtom.kurumi.model.UpVal;
import com.iteye.weimingtom.kurumi.port.ClassType;
import com.iteye.weimingtom.kurumi.proto.ArrayElement;

//
// ** $Id: lmem.c,v 1.70.1.1 2007/12/27 13:02:25 roberto Exp $
// ** Interface to Memory Manager
// ** See Copyright Notice in lua.h
// 

public class LuaMem {
	public static final String MEMERRMSG = "not enough memory";

	//-------------------------------

	public static char[] luaM_reallocv_char(LuaStateObject L, char[] block, int new_size, ClassType t) {
		return (char[])luaM_realloc__char(L, block, new_size, t);
	}

	public static TValue[] luaM_reallocv_TValue(LuaStateObject L, TValue[] block, int new_size, ClassType t) {
		return (TValue[])luaM_realloc__TValue(L, block, new_size, t);
	}

	public static TString[] luaM_reallocv_TString(LuaStateObject L, TString[] block, int new_size, ClassType t) {
		return (TString[])luaM_realloc__TString(L, block, new_size, t);
	}

	public static CallInfo[] luaM_reallocv_CallInfo(LuaStateObject L, CallInfo[] block, int new_size, ClassType t) {
		return (CallInfo[])luaM_realloc__CallInfo(L, block, new_size, t);
	}

	public static long[] luaM_reallocv_long(LuaStateObject L, long[] block, int new_size, ClassType t) {
		return (long[])luaM_realloc__long(L, block, new_size, t);
	}

	public static int[] luaM_reallocv_int(LuaStateObject L, int[] block, int new_size, ClassType t) {
		return (int[])luaM_realloc__int(L, block, new_size, t);
	}

	public static Proto[] luaM_reallocv_Proto(LuaStateObject L, Proto[] block, int new_size, ClassType t) {
		return (Proto[])luaM_realloc__Proto(L, block, new_size, t);
	}

	public static LocVar[] luaM_reallocv_LocVar(LuaStateObject L, LocVar[] block, int new_size, ClassType t) {
		return (LocVar[])luaM_realloc__LocVar(L, block, new_size, t);
	}

	public static Node[] luaM_reallocv_Node(LuaStateObject L, Node[] block, int new_size, ClassType t) {
		return (Node[])luaM_realloc__Node(L, block, new_size, t);
	}

	public static GCObject[] luaM_reallocv_GCObject(LuaStateObject L, GCObject[] block, int new_size, ClassType t) {
		return (GCObject[])luaM_realloc__GCObject(L, block, new_size, t);
	}

	//-------------------------------

	///#define luaM_freemem(L, b, s)	luaM_realloc_(L, (b), (s), 0)
	///#define luaM_free(L, b)		luaM_realloc_(L, (b), sizeof(*(b)), 0)
	//public static void luaM_freearray(lua_State L, object b, int n, Type t) { luaM_reallocv(L, b, n, 0, Marshal.SizeOf(b)); }

	// C# has it's own gc, so nothing to do here...in theory...
	public static void luaM_freemem_Udata(LuaStateObject L, Udata b, ClassType t) {
		luaM_realloc__Udata(L, new Udata[] {b}, 0, t);
	}

	public static void luaM_freemem_TString(LuaStateObject L, TString b, ClassType t) {
		luaM_realloc__TString(L, new TString[] { b }, 0, t);
	}

	//-------------------------------

	public static void luaM_free_Table(LuaStateObject L, Table b, ClassType t) {
		luaM_realloc__Table(L, new Table[] {b}, 0, t);
	}

	public static void luaM_free_UpVal(LuaStateObject L, UpVal b, ClassType t) {
		luaM_realloc__UpVal(L, new UpVal[] { b }, 0, t);
	}

	public static void luaM_free_Proto(LuaStateObject L, Proto b, ClassType t) {
		luaM_realloc__Proto(L, new Proto[] { b }, 0, t);
	}

	//-------------------------------

	public static void luaM_freearray_long(LuaStateObject L, long[] b, ClassType t) {
		luaM_reallocv_long(L, b, 0, t);
	}

	public static void luaM_freearray_Proto(LuaStateObject L, Proto[] b, ClassType t) {
		luaM_reallocv_Proto(L, b, 0, t);
	}

	public static void luaM_freearray_TValue(LuaStateObject L, TValue[] b, ClassType t) {
		luaM_reallocv_TValue(L, b, 0, t);
	}

	public static void luaM_freearray_int(LuaStateObject L, int[] b, ClassType t) {
		luaM_reallocv_int(L, b, 0, t);
	}

	public static void luaM_freearray_LocVar(LuaStateObject L, LocVar[] b, ClassType t) {
		luaM_reallocv_LocVar(L, b, 0, t);
	}

	public static void luaM_freearray_TString(LuaStateObject L, TString[] b, ClassType t) {
		luaM_reallocv_TString(L, b, 0, t);
	}

	public static void luaM_freearray_Node(LuaStateObject L, Node[] b, ClassType t) {
		luaM_reallocv_Node(L, b, 0, t);
	}

	public static void luaM_freearray_CallInfo(LuaStateObject L, CallInfo[] b, ClassType t) {
		luaM_reallocv_CallInfo(L, b, 0, t);
	}

	public static void luaM_freearray_GCObject(LuaStateObject L, GCObject[] b, ClassType t) {
		luaM_reallocv_GCObject(L, b, 0, t);
	}

	//-------------------------------


	//public static T luaM_malloc<T>(lua_State L, ClassType t) 
	//{ 
	//	return (T)luaM_realloc_<T>(L, t); 
	//}

	public static Proto luaM_new_Proto(LuaStateObject L, ClassType t) {
		return (Proto)luaM_realloc__Proto(L, t);
	}

	public static Closure luaM_new_Closure(LuaStateObject L, ClassType t) {
		return (Closure)luaM_realloc__Closure(L, t);
	}

	public static UpVal luaM_new_UpVal(LuaStateObject L, ClassType t) {
		return (UpVal)luaM_realloc__UpVal(L, t);
	}

	public static LuaStateObject luaM_new_lua_State(LuaStateObject L, ClassType t) {
		return (LuaStateObject)luaM_realloc__lua_State(L, t);
	}

	public static Table luaM_new_Table(LuaStateObject L, ClassType t) {
		return (Table)luaM_realloc__Table(L, t);
	}


	//-------------------------------

	public static long[] luaM_newvector_long(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_long(L, null, n, t);
	}

	public static TString[] luaM_newvector_TString(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_TString(L, null, n, t);
	}

	public static LocVar[] luaM_newvector_LocVar(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_LocVar(L, null, n, t);
	}

	public static int[] luaM_newvector_int(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_int(L, null, n, t);
	}

	public static Proto[] luaM_newvector_Proto(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_Proto(L, null, n, t);
	}

	public static TValue[] luaM_newvector_TValue(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_TValue(L, null, n, t);
	}

	public static CallInfo[] luaM_newvector_CallInfo(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_CallInfo(L, null, n, t);
	}

	public static Node[] luaM_newvector_Node(LuaStateObject L, int n, ClassType t) {
		return luaM_reallocv_Node(L, null, n, t);
	}

	//-------------------------------







	public static void luaM_growvector_long(LuaStateObject L, long[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (long[])luaM_growaux__long(L, v, size, limit, e, t); //ref - ref
		}
	}

	public static void luaM_growvector_Proto(LuaStateObject L, Proto[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (Proto[])luaM_growaux__Proto(L, v, size, limit, e, t); //ref - ref
		}
	}

	public static void luaM_growvector_TString(LuaStateObject L, TString[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (TString[])luaM_growaux__TString(L, v, size, limit, e, t); //ref - ref
		}
	}

	public static void luaM_growvector_TValue(LuaStateObject L, TValue[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (TValue[])luaM_growaux__TValue(L, v, size, limit, e, t); //ref - ref
		}
	}

	public static void luaM_growvector_LocVar(LuaStateObject L, LocVar[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (LocVar[])luaM_growaux__LocVar(L, v, size, limit, e, t); //ref - ref
		}
	}

	public static void luaM_growvector_int(LuaStateObject L, int[][] v, int nelems, int[] size, int limit, CharPtr e, ClassType t) { //ref - ref
		if (nelems + 1 > size[0]) {
			v[0] = (int[])luaM_growaux__int(L, v, size, limit, e, t); //ref - ref
		}
	}

	//-------------------------------



	public static char[] luaM_reallocvector_char(LuaStateObject L, char[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_char(L, v[0], n, t);
		return v[0];
	}

	public static TValue[] luaM_reallocvector_TValue(LuaStateObject L, TValue[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_TValue(L, v[0], n, t);
		return v[0];
	}

	public static TString[] luaM_reallocvector_TString(LuaStateObject L, TString[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_TString(L, v[0], n, t);
		return v[0];
	}

	public static CallInfo[] luaM_reallocvector_CallInfo(LuaStateObject L, CallInfo[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_CallInfo(L, v[0], n, t);
		return v[0];
	}

	public static long[] luaM_reallocvector_long(LuaStateObject L, long[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_long(L, v[0], n, t);
		return v[0];
	}

	public static int[] luaM_reallocvector_int(LuaStateObject L, int[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_int(L, v[0], n, t);
		return v[0];
	}

	public static Proto[] luaM_reallocvector_Proto(LuaStateObject L, Proto[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_Proto(L, v[0], n, t);
		return v[0];
	}

	public static LocVar[] luaM_reallocvector_LocVar(LuaStateObject L, LocVar[][] v, int oldn, int n, ClassType t) { //ref
		ClassType.Assert((v[0] == null && oldn == 0) || (v[0].length == oldn));
		v[0] = luaM_reallocv_LocVar(L, v[0], n, t);
		return v[0];
	}

	//-------------------------------


//        
//		 ** About the realloc function:
//		 ** void * frealloc (void *ud, void *ptr, uint osize, uint nsize);
//		 ** (`osize' is the old size, `nsize' is the new size)
//		 **
//		 ** Lua ensures that (ptr == null) iff (osize == 0).
//		 **
//		 ** * frealloc(ud, null, 0, x) creates a new block of size `x'
//		 **
//		 ** * frealloc(ud, p, x, 0) frees the block `p'
//		 ** (in this specific case, frealloc must return null).
//		 ** particularly, frealloc(ud, null, 0, 0) does nothing
//		 ** (which is equivalent to free(null) in ANSI C)
//		 **
//		 ** frealloc returns null if it cannot create or reallocate the area
//		 ** (any reallocation to an equal or smaller size cannot fail!)
//		 
	public static final int MINSIZEARRAY = 4;


	public static long[] luaM_growaux__long(LuaStateObject L, long[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		long[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_long(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	public static Proto[] luaM_growaux__Proto(LuaStateObject L, Proto[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		Proto[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_Proto(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	public static TString[] luaM_growaux__TString(LuaStateObject L, TString[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		TString[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_TString(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	public static TValue[] luaM_growaux__TValue(LuaStateObject L, TValue[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		TValue[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_TValue(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	public static LocVar[] luaM_growaux__LocVar(LuaStateObject L, LocVar[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		LocVar[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_LocVar(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	public static int[] luaM_growaux__int(LuaStateObject L, int[][] block, int[] size, int limit, CharPtr errormsg, ClassType t) { //ref - ref
		int[] newblock;
		int newsize;
		if (size[0] >= limit / 2) {
			// cannot double it? 
			if (size[0] >= limit) { // cannot grow even a little? 
				LuaDebug.luaG_runerror(L, errormsg);
			}
			newsize = limit; // still have at least one free place 
		}
		else {
			newsize = size[0] * 2;
			if (newsize < MINSIZEARRAY) {
				newsize = MINSIZEARRAY; // minimum size 
			}
		}
		newblock = luaM_reallocv_int(L, block[0], newsize, t);
		size[0] = newsize; // update only when everything else is OK 
		return newblock;
	}

	//-------------------------------

	public static Object luaM_toobig(LuaStateObject L) {
		LuaDebug.luaG_runerror(L, CharPtr.toCharPtr("memory allocation error: block too big"));
		return null; // to avoid warnings 
	}

//        
//		 ** generic allocation routine.
//		 
	public static Object luaM_realloc_(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)LuaConf.GetUnmanagedSize(t);
		int nsize = unmanaged_size;
		Object new_obj = t.Alloc();
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	public static Object luaM_realloc__Proto(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int nsize = unmanaged_size;
		Proto new_obj = (Proto)t.Alloc(); //System.Activator.CreateInstance(typeof(T));
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	public static Object luaM_realloc__Closure(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int nsize = unmanaged_size;
		Closure new_obj = (Closure)t.Alloc(); //System.Activator.CreateInstance(typeof(T));
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	public static Object luaM_realloc__UpVal(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int nsize = unmanaged_size;
		UpVal new_obj = (UpVal)t.Alloc(); //System.Activator.CreateInstance(typeof(T));
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	public static Object luaM_realloc__lua_State(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int nsize = unmanaged_size;
		LuaStateObject new_obj = (LuaStateObject)t.Alloc(); //System.Activator.CreateInstance(typeof(T));
		AddTotalBytes(L, nsize);
		return new_obj;
	}

	public static Object luaM_realloc__Table(LuaStateObject L, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int nsize = unmanaged_size;
		Table new_obj = (Table)t.Alloc(); //System.Activator.CreateInstance(typeof(T));
		AddTotalBytes(L, nsize);
		return new_obj;
	}







	//---------------------------------


	//public static object luaM_realloc_<T>(lua_State L, T obj, ClassType t)
	//{
	//	int unmanaged_size = (int)t.GetUnmanagedSize();//LuaConf.GetUnmanagedSize(typeof(T))
	//	int old_size = (obj == null) ? 0 : unmanaged_size;
	//	int osize = old_size * unmanaged_size;
	//	int nsize = unmanaged_size;
	//   T new_obj = (T)t.Alloc(); //System.Activator.CreateInstance(typeof(T))
	//	SubtractTotalBytes(L, osize);
	//	AddTotalBytes(L, nsize);
	//	return new_obj;
	//}

	//public static object luaM_realloc_<T>(lua_State L, T[] old_block, int new_size, ClassType t)
	//{
	//	int unmanaged_size = (int)t.GetUnmanagedSize();//LuaConf.GetUnmanagedSize(typeof(T));
	//	int old_size = (old_block == null) ? 0 : old_block.Length;
	//	int osize = old_size * unmanaged_size;
	//	int nsize = new_size * unmanaged_size;
	//	T[] new_block = new T[new_size];
	//	for (int i = 0; i < Math.Min(old_size, new_size); i++)
	//	{
	//		new_block[i] = old_block[i];
	//	}
	//	for (int i = old_size; i < new_size; i++)
	//	{
	//       new_block[i] = (T)t.Alloc();// System.Activator.CreateInstance(typeof(T));
	//	}
	//	if (CanIndex(t))
	//	{
	//		for (int i = 0; i < new_size; i++)
	//		{
	//			ArrayElement elem = new_block[i] as ArrayElement;
	//			ClassType.Assert(elem != null, String.Format("Need to derive type {0} from ArrayElement", t.GetTypeString()));
	//			elem.set_index(i);
	//			elem.set_array(new_block);
	//		}
	//	}
	//	SubtractTotalBytes(L, osize);
	//	AddTotalBytes(L, nsize);
	//	return new_block;
	//}

	public static Object luaM_realloc__Table(LuaStateObject L, Table[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		Table[] new_block = new Table[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (Table)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__UpVal(LuaStateObject L, UpVal[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		UpVal[] new_block = new UpVal[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (UpVal)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__char(LuaStateObject L, char[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		char[] new_block = new char[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = ((Character)t.Alloc()).charValue(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) { // FIXME:not necessary
//                
//                for (int i = 0; i < new_size; i++)
//                {
//                    ArrayElement elem = new_block[i] as ArrayElement;
//                    ClassType.Assert(elem != null, String.Format("Need to derive type {0} from ArrayElement", t.GetTypeString()));
//                    elem.set_index(i);
//                    elem.set_array(new_block);
//                }
//                
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__TValue(LuaStateObject L, TValue[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		TValue[] new_block = new TValue[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (TValue)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__TString(LuaStateObject L, TString[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		TString[] new_block = new TString[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (TString)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__Udata(LuaStateObject L, Udata[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		Udata[] new_block = new Udata[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (Udata)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__CallInfo(LuaStateObject L, CallInfo[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		CallInfo[] new_block = new CallInfo[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (CallInfo)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__long(LuaStateObject L, long[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		long[] new_block = new long[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = ((Long)t.Alloc()).longValue(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) { //FIXME: not necessary
//                
//                for (int i = 0; i < new_size; i++)
//                {
//                    ArrayElement elem = new_block[i] as ArrayElement;
//                    ClassType.Assert(elem != null, String.Format("Need to derive type {0} from ArrayElement", t.GetTypeString()));
//                    elem.set_index(i);
//                    elem.set_array(new_block);
//                }
//                
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__int(LuaStateObject L, int[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		int[] new_block = new int[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = ((Integer)t.Alloc()).intValue(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) { //FIXME: not necessary
//                
//                for (int i = 0; i < new_size; i++)
//                {
//                    ArrayElement elem = new_block[i] as ArrayElement;
//                    ClassType.Assert(elem != null, String.Format("Need to derive type {0} from ArrayElement", t.GetTypeString()));
//                    elem.set_index(i);
//                    elem.set_array(new_block);
//                }
//                
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__Proto(LuaStateObject L, Proto[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		Proto[] new_block = new Proto[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (Proto)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__LocVar(LuaStateObject L, LocVar[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		LocVar[] new_block = new LocVar[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (LocVar)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__Node(LuaStateObject L, Node[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		Node[] new_block = new Node[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (Node)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}

	public static Object luaM_realloc__GCObject(LuaStateObject L, GCObject[] old_block, int new_size, ClassType t) {
		int unmanaged_size = (int)t.GetUnmanagedSize(); //LuaConf.GetUnmanagedSize(typeof(T));
		int old_size = (old_block == null) ? 0 : old_block.length;
		int osize = old_size * unmanaged_size;
		int nsize = new_size * unmanaged_size;
		GCObject[] new_block = new GCObject[new_size];
		for (int i = 0; i < Math.min(old_size, new_size); i++) {
			new_block[i] = old_block[i];
		}
		for (int i = old_size; i < new_size; i++) {
			new_block[i] = (GCObject)t.Alloc(); // System.Activator.CreateInstance(typeof(T));
		}
		if (CanIndex(t)) {
			for (int i = 0; i < new_size; i++) {
				ArrayElement elem = (ArrayElement)((new_block[i] instanceof ArrayElement) ? new_block[i] : null);
				ClassType.Assert(elem != null, String.format("Need to derive type %1$s from ArrayElement", t.GetTypeString()));
				elem.set_index(i);
				elem.set_array(new_block);
			}
		}
		SubtractTotalBytes(L, osize);
		AddTotalBytes(L, nsize);
		return new_block;
	}


	public static boolean CanIndex(ClassType t) {
		return t.CanIndex();
	}

	public static void AddTotalBytes(LuaStateObject L, int num_bytes) {
		LuaState.G(L).totalbytes += (int)num_bytes; //uint
	}

	public static void SubtractTotalBytes(LuaStateObject L, int num_bytes) {
		LuaState.G(L).totalbytes -= (int)num_bytes; //uint
	}

	//static void AddTotalBytes(lua_State L, uint num_bytes) {G(L).totalbytes += num_bytes;}
	//static void SubtractTotalBytes(lua_State L, uint num_bytes) {G(L).totalbytes -= num_bytes;}
}